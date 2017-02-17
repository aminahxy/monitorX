package com.sina.data.alarm;


import com.sina.data.alarm.util.MonitorCache;
import com.sina.data.alarm.util.MonitorData;
import com.sina.data.alarm.util.ReceiveBean;
import com.sina.data.alarm.warning.WarningManagerIface;
import com.sina.data.alarm.warning.WarningManagerImpl;
import com.sina.data.common.AppType;
import com.sina.data.common.JudgeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class WarnningServer {
    public static final Logger LOG = LoggerFactory
            .getLogger(WarnningServer.class);
    public static int THREAD_NUMBER = 5;
    private ConcurrentHashMap<String, MonitorData> data;
    private ConcurrentHashMap<String, Receive> smsReceive;
    private ConcurrentHashMap<String, Receive> emailReceive;
    private BlockingQueue<ReceiveBean> smsWarning;
    private BlockingQueue<ReceiveBean> emailWarning;
    private static String urlPrefix = "http://10.88.15.146/process/index.php/interface/sendsms/send?myPhone=";
    private static String urlPrefixNew = "http://monitor.erp.sina.com.cn/index.php/interface/sendsms/sendSinaWatch?users=$users&service="
            + "hadoopWarnning&object=Warnning&subject=$content&sendType=sms";
    private SmsMonitor[] smsMonitor;
    private Thread[] smsThread;
    private EmailMonitor[] emailMonitor;
    private Thread[] emailThread;
    private static final String SINA_STAFF = "@staff.sina.com.cn";

    private Properties silenceProp = new Properties();
    private Map<String, String[]> silenceMap = new HashMap<String, String[]>();
    WarningManagerIface warningManager;

    public WarnningServer() {
        smsReceive = new ConcurrentHashMap<String, Receive>();
        emailReceive = new ConcurrentHashMap<String, Receive>();
        data = MonitorCache.getInstance().getData();
        smsWarning = new LinkedBlockingQueue<ReceiveBean>();
        emailWarning = new LinkedBlockingQueue<ReceiveBean>();
        smsMonitor = new SmsMonitor[THREAD_NUMBER];
        smsThread = new Thread[THREAD_NUMBER];
        emailMonitor = new EmailMonitor[THREAD_NUMBER];
        emailThread = new Thread[THREAD_NUMBER];
        for (int i = 0; i < THREAD_NUMBER; i++) {
            smsMonitor[i] = new SmsMonitor();
            smsThread[i] = new Thread(smsMonitor[i]);
            smsThread[i].setDaemon(true);
            smsThread[i].start();
            emailMonitor[i] = new EmailMonitor();
            emailThread[i] = new Thread(emailMonitor[i]);
            emailThread[i].setDaemon(true);
            emailThread[i].start();
        }
        warningManager = WarningManagerImpl.getWarningManager();

        /*
        try {
            silenceProp.load(WarnningServer.class.getClassLoader().getResourceAsStream("silence.properties"));
            for (Object key : silenceProp.keySet()) {
                String value = silenceProp.getProperty(key + "");
                silenceMap.put(key + "", StringUtils.split(value, ','));
            }
        } catch (Exception e) {
            LOG.error("error load silence.properties exit!");
            System.exit(-1);
            e.printStackTrace();
        }
        */
    }

    class Receive {
        private LinkedList<ReceiveBean> smsList;
        private LinkedList<ReceiveBean> emaList;
        private long smsLastWarning;
        private long emailLastWarnning;
        private int smsCounter;
        private int emaCounter;

        public Receive() {
            smsCounter = 0;
            emaCounter = 0;
            smsLastWarning = 0;
            emailLastWarnning = 0;
            smsList = new LinkedList<ReceiveBean>();
            emaList = new LinkedList<ReceiveBean>();
        }

        public void putSmsForJob(ReceiveBean rec) {

            LOG.info(rec.getMetric() + ": [job] sms put receive "
                    + rec.getTimestamp());

            if (rec.getJudgeType() == JudgeType.no_data) {
                return;
            }

            synchronized (smsList) {
                if (smsList.size() == 0) {
                    MonitorData.SmsBean smsBean = data.get(rec.getRegister_name()).sms;
                    if (smsBean.getLastTime() == 1) {
                        smsWarning.offer(rec);
                        return;
                    }
                    LOG.info("smsList.size = 0");
                    smsList.add(rec);
                } else {
                    MonitorData.SmsBean smsBean = data.get(rec.getRegister_name()).sms;
                    if (rec.getTimestamp() - smsLastWarning < smsBean.getExtend()) {
                        LOG.info("LastWarnning < " + smsBean.getExtend());
                        return;
                    }
                    ReceiveBean last = smsList.getLast();
                    if (last.getAppId().equals(rec.getAppId())
                            && last.getTimestamp() == rec.getTimestamp()
                            && rec.getMetric().equals(last.getMetric())) {
                        LOG.info("Double time Warnning for appId : " + rec.getAppId());
                        return;
                    }
                    int interval = smsBean.getIntervalTime();
                    if ((rec.getTimestamp() - last.getTimestamp()) >= interval
                            && (rec.getTimestamp() - last.getTimestamp()) < 2 * interval) {
                        smsList.add(rec);
                        smsCounter++;
                        if (smsCounter == smsBean.getLastTime()) {
                            smsWarning.offer(rec);
                            smsLastWarning = rec.getTimestamp();
                            smsCounter = 0;
                            resetSms();
                        } else {
                            LOG.info("Unexpect lasttime : " + emaCounter);
                        }
                    } else {
                        LOG.info("Unexpect interval : " + interval + "<="
                                + (rec.getTimestamp() - last.getTimestamp()) + " <= "
                                + (2 * interval));
                        smsCounter = 0;
                        resetSms();
                        smsList.add(rec);
                    }
                }
            }
        }

        public void putEmailForJob(ReceiveBean rec) {

            LOG.info(rec.getMetric() + ":  [job]  email put receive "
                    + rec.getTimestamp());

            if (rec.getJudgeType() == JudgeType.offline) {
                emailWarning.offer(rec);
                return;
            }
            synchronized (emaList) {
                if (emaList.size() == 0) {
                    MonitorData.EmailBean emaBean = data.get(rec.getRegister_name()).email;
                    if (emaBean.getLastTime() == 1) {
                        LOG.info("will send mail");
                        emailWarning.offer(rec);
                        return;
                    }
                    LOG.info("emaList.size() == 0");
                    emaList.add(rec);
                } else {
                    MonitorData.EmailBean emaBean = data.get(rec.getRegister_name()).email;
                    if (rec.getTimestamp() - emailLastWarnning < emaBean.getExtend()) {
                        LOG.info("LastWarnning < " + emaBean.getExtend());
                        return;
                    } else {
                        ReceiveBean last = emaList.getLast();
                        if (last.getAppId().equals(rec.getAppId())
                                && last.getTimestamp() == rec.getTimestamp()
                                && rec.getMetric().equals(last.getMetric())) {
                            LOG.info("Double time Warnning for appId : " + rec.getAppId() + " same node:"+rec.getNode());
                            return;
                        }
                        int interval = emaBean.getIntervalTime();
                        if ((rec.getTimestamp() - last.getTimestamp()) >= interval
                                && (rec.getTimestamp() - last.getTimestamp()) < 2 * interval) {
                            emaList.add(rec);
                            emaCounter++;
                            if (emaCounter == emaBean.getLastTime()) {
                                LOG.info("will send mail");
                                emailWarning.offer(rec);
                                emailLastWarnning = rec.getTimestamp();
                                emaCounter = 0;
                                resetEmail();
                            } else {
                                LOG.info("Unexpect lasttime : " + emaCounter);
                            }
                        } else {
                            LOG.info("Unexpect interval : "
                                    + (rec.getTimestamp() - last.getTimestamp()) + " expect : "
                                    + interval);
                            emaCounter = 0;
                            resetEmail();
                            emaList.add(rec);
                        }
                    }
                }
            }
        }

        public void putSms(ReceiveBean rec) {

            LOG.info(rec.getNode()+ "  "+ rec.getMetric() + ":sms put receive " + rec.getTimestamp());

            if (rec.getJudgeType() == JudgeType.no_data) {
                return;
            }

            if (rec.getJudgeType() == JudgeType.offline) {

                String[] times = silenceMap.get(rec.getMonitorId());
                if (times != null) {
                    for (String time : times) {
                        int t = Integer.valueOf(time);
                        if (t == getCurrentHourOfDay()) {
                            LOG.info(rec.getAppId() + " monitorId : " + rec.getMonitorId() + " offline but silence!");
                            return;
                        }
                    }
                }

                smsWarning.offer(rec);
                return;
            }

            synchronized (smsList) {
                if (smsList.size() == 0) {
                    MonitorData.SmsBean smsBean = data.get(rec.getRegister_name()).sms;
                    if (smsBean.getLastTime() == 1) {
                        smsWarning.offer(rec);
                        return;
                    }
                    smsList.add(rec);
                    smsCounter++;
                    LOG.info("sms warn one:" + smsCounter);
                } else {
                    MonitorData.SmsBean smsBean = data.get(rec.getRegister_name()).sms;
                    if (rec.getTimestamp() - smsLastWarning < smsBean.getExtend()) {
                        LOG.info("LastWarnning < " + smsBean.getExtend());
                        return;
                    }
                    ReceiveBean last = smsList.getLast();
                    if (last.getAppId().equals(rec.getAppId())
                            && last.getTimestamp() == rec.getTimestamp()) {
                        LOG.info("Double time Warnning for appId : " + rec.getAppId() + " "+ rec.getRegister_name());
                        return;
                    }
                    int interval = smsBean.getIntervalTime();
                    if ((rec.getTimestamp() - last.getTimestamp()) >= interval  //internal Time 两次告警数据推送过来的间隔
                            && (rec.getTimestamp() - last.getTimestamp()) < 2 * interval) {
                        smsList.add(rec);
                        smsCounter++;
                        if (smsCounter == smsBean.getLastTime()) {
                            smsWarning.offer(rec);
                            smsLastWarning = rec.getTimestamp();
                            smsCounter = 0;
                            resetSms();
                        } else {
                            LOG.info("Unexpect lasttime : " + smsCounter);
                        }
                    } else {
                        LOG.info("Unexpect interval : "
                                + (rec.getTimestamp() - last.getTimestamp()));
                        smsCounter = 0;
                        resetSms();
                        smsList.add(rec);
                    }
                }
            }
        }

        public void putEmail(ReceiveBean rec) {

            LOG.info(rec.getNode() + "  " +rec.getMetric() + ":email put receive " + rec.getTimestamp());

            if (rec.getJudgeType() == JudgeType.offline) {
                LOG.info("will send offline alarm.");
                emailWarning.offer(rec);
                return;
            }
            synchronized (emaList) {
                if (emaList.size() == 0) {
                    MonitorData.EmailBean emaBean = data.get(rec.getRegister_name()).email;
                    if (emaBean.getLastTime() == 1) {//lastTime 连续告警次数
                        emailWarning.offer(rec);
                        return;
                    }

                    emaList.add(rec);
                    emaCounter++;
                    LOG.info("email warn one:"+emaCounter);
                }
                else {
                    MonitorData.EmailBean emaBean = data.get(rec.getRegister_name()).email;
                    LOG.info("boyan add rec.getTimestamp  "+rec.getTimestamp());
                    LOG.info("boyan add emailLastWarnning "+emailLastWarnning);
                    LOG.info("boyan add emaBean.getExtend() "+emaBean.getExtend());
                    if (rec.getTimestamp() - emailLastWarnning < emaBean.getExtend()) {//extends 两次报警间隔时间
                        LOG.info("LastWarnning < " + emaBean.getExtend());
                        return;
                    } else {
                        ReceiveBean last = emaList.getLast();
                        LOG.info("boyan add emaList.getLast() "+emaList.getLast().getTimestamp());
                        if (last.getAppId().equals(rec.getAppId())
                                && last.getTimestamp() == rec.getTimestamp()) {
                            LOG.info("Double time Warnning for appId : " + rec.getAppId() +" same node:"+ rec.getNode());
                            return;
                        }
                        int interval = emaBean.getIntervalTime();
                        LOG.info("boyan add "+interval);
                        if ((rec.getTimestamp() - last.getTimestamp()) >= interval
                                && (rec.getTimestamp() - last.getTimestamp()) < 2 * interval) {
                            emaList.add(rec);
                            emaCounter++;
                            if (emaCounter == emaBean.getLastTime()) {
                                emailWarning.offer(rec);
                                emailLastWarnning = rec.getTimestamp();
                                LOG.info("---emailLastWarnning---"+emailLastWarnning);
                                emaCounter = 0;
                                resetEmail();
                            } else {
                                LOG.info("Unexpect lasttime : " + emaCounter);  //还没到lasttime 连续多少次才报警,所以不报警
                            }
                        } else {
                            LOG.info("Unexpect interval : "
                                    + (rec.getTimestamp() - last.getTimestamp()));
                            emaCounter = 0;
                            resetEmail();
                            emaList.add(rec);
                        }
                    }
                }
            }
        }

        public void resetSms() {
            synchronized (smsList) {
                smsList = new LinkedList<ReceiveBean>();
            }
        }

        public void resetEmail() {
            synchronized (emaList) {
                emaList = new LinkedList<ReceiveBean>();
            }
        }
    }

    public void putReceive(ReceiveBean bean) {
        LOG.info("boyan add haha");
        if (data.get(bean.getRegister_name()) == null)
            return;

        String key = "";

        if (AppType.job == bean.getAppType()) {
            key = bean.getAppId() + "#" + bean.getMetric();
        } else if (AppType.ols == bean.getAppType()) {
            key = bean.getRegister_name();
        } else {
            key = bean.getRegister_name()+"#"+bean.getMetric()+"#"+bean.getNode();
        }

        // job offline
        if (AppType.job == bean.getAppType()
                && JudgeType.offline == bean.getJudgeType()) {
            LOG.info("Do not need warning for job offline.");
            return;
        }

        LOG.info("Receive Alarm id : " + bean.getRegister_name() + " appId : "
                + bean.getAppId() + " appType : " + bean.getAppType() + " threshold : "
                + bean.getThreshold() + " value : " + bean.getCurrent_value()
                + " timestamp : " + bean.getTimestamp() + " key = " + key
                + " JudgeType = " + bean.getJudgeType());

        Receive rece = smsReceive.get(key);
        Receive receE = emailReceive.get(key);
        if (receE == null) {
            Receive newRece = new Receive();
            emailReceive.put(key, newRece);
            if (AppType.job == bean.getAppType()) {
                newRece.putEmailForJob(bean);
            } else if (AppType.ols == bean.getAppType()) {
                newRece.putEmail(bean);
            }else{
                newRece.putEmail(bean);
            }
        } else {
            if (AppType.job == bean.getAppType()) {
                receE.putEmailForJob(bean);
            } else if (AppType.ols == bean.getAppType()) {
                receE.putEmail(bean);
            }else{
                receE.putEmail(bean);
            }
        }

        MonitorData.RegisterBean rb = data.get(bean.getRegister_name()).register;
        JudgeType judgeType = rb.getJudgeType();
        float mobileTr = rb.getMobileThreshold();
        LOG.info("---JudgeType---" + judgeType + "---mobileTr--" + mobileTr);


        if (JudgeType.lt == judgeType) {
            if (bean.getCurrent_value() >= mobileTr) {
                return;
            }
        } else if (JudgeType.le == judgeType) {
            if (bean.getCurrent_value() > mobileTr) {
                return;
            }
        } else if (JudgeType.eq == judgeType) {
            if (bean.getCurrent_value() != mobileTr) {
                return;
            }
        } else if (JudgeType.ge == judgeType) {
            if (bean.getCurrent_value() < mobileTr) {
                return;
            }
        } else if (JudgeType.gt == judgeType) {
            if (bean.getCurrent_value() <= mobileTr) {
                return;
            }
        }

//    else if (JudgeType.offline == judgeType || JudgeType.no_data == judgeType) {
//      return;
//    }

        else {
            if (bean.getCurrent_value() != null && bean.getCurrent_value() == mobileTr) {
                return;
            }
        }


        if (rece == null) {
            Receive newRece = new Receive();
            smsReceive.put(key, newRece);
            if (AppType.job == bean.getAppType()) {
                newRece.putSmsForJob(bean);
            } else if (AppType.ols == bean.getAppType()) {
                newRece.putSms(bean);
            }else{
                newRece.putSms(bean);
            }
        } else {
            if (AppType.job == bean.getAppType()) {
                rece.putSmsForJob(bean);
            } else if (AppType.ols == bean.getAppType()) {
                rece.putSms(bean);
            }else{
                rece.putSms(bean);
            }
        }

    }

    public void smsWarning(ReceiveBean bean) {


        MonitorData.SmsBean sms = data.get(bean.getRegister_name()).sms;
        if (!sms.isEnable())
            return;
        if (bean.getCurrent_value() != null && bean.getCurrent_value() == -Integer.MAX_VALUE) {
            return;
        }
        String content = new String(sms.getContent());

        if (AppType.ols == bean.getAppType()) {

            if (JudgeType.offline == bean.getJudgeType()) {
                content = "AppId : " + bean.getAppId() + " monitorId : " + bean.getMonitorId() + " regName : " + bean.getMetric() + " is offline !";
            } else {
                content = content.replace("%registName%",
                        data.get(bean.getRegister_name()).register.getRegisterName());
                content = content.replace("%appId%", bean.getAppId());
                content = content.replace("%metric%", bean.getMetric());
                content = content.replace("%user%", bean.getUser());
                content = content.replace("%appType%", bean.getAppType().toString());
                if (bean.getCurrent_value() == -Integer.MAX_VALUE) {
                    content = content.replace("%current_value%", "");
                    // content = content.replace("%others%",
                    // "no data has been received so far.");
                } else {
                    content = content.replace(
                            "%current_value%",
                            NumberFormat.getInstance(Locale.CHINA).format(
                                    new Double(bean.getCurrent_value()).longValue()));
                    // content = content.replace("%others%", bean.getOthers());
                }
                content = content.replace(
                        "%threshold%",
                        NumberFormat.getInstance(Locale.CHINA).format(
                                new Double(bean.getThreshold()).longValue()));
                content = content.replace("%node%", String.valueOf(bean.getNode()));
                content = content.replace("%component%",
                        String.valueOf(bean.getComponent()));
            }

        } else if (AppType.job == bean.getAppType()) {
            content = content.replace(
                    "%current_value%",
                    NumberFormat.getInstance(Locale.CHINA).format(
                            new Double(bean.getCurrent_value()).longValue()));
            content = content.replace("%appId%", bean.getAppId());
            content = content.replace(
                    "%threshold%",
                    NumberFormat.getInstance(Locale.CHINA).format(
                            new Double(bean.getThreshold()).longValue()));
            content = content.replace("%extended%", bean.getExtended());
        } else {
            content = content.replace("%metric%", "registerName:" + bean.getRegister_name()
                    + "  metricName:" + bean.getMetric() + "  node:"+bean.getNode());
            content = content.replace(
                    "%threshold%",
                    NumberFormat.getInstance(Locale.CHINA).format(
                            new Double(bean.getThreshold()).longValue()));
            content = content.replace(
                    "%current_value%",
                    NumberFormat.getInstance(Locale.CHINA).format(
                            new Double(bean.getCurrent_value()).longValue()));
        }

        content = content.replaceAll(" ", "%20");

        String receiveList = null;
        String userMapping = MonitorCache.getInstance().smsUserMapping.get(bean
                .getUser());


        if (JudgeType.offline == bean.getJudgeType()) {
            receiveList = MonitorCache.getInstance().regNameToSmsUsers.get(bean.getMetric());
        }

        if (receiveList == null) {
            if (userMapping != null) {
                receiveList = userMapping;
            }
            receiveList = receiveList + "," + sms.getReceiveList();

            receiveList = receiveList + "," + bean.getUser();
        }

        try {
            LOG.info(content);
            String urlContent = urlPrefixNew.replace("$content", content);
            urlContent = urlContent.replace("$users", receiveList);
            URL url = new URL(urlContent);
            HttpURLConnection httpURL = (HttpURLConnection) url.openConnection();
            httpURL.connect();
            httpURL.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info("sent message " + content + " to : " + receiveList + " id : "
                + bean.getRegister_name());



    }

    public void emailWarning(ReceiveBean bean) {

        MonitorData.EmailBean email = data.get(bean.getRegister_name()).email;
        if (!email.isEnable()) {
            LOG.info("!email.isEnable()");
            return;
        }

        LOG.info("EmailBean : " + email);

        String content = new String(email.getContent());
        if (data.get(bean.getRegister_name()) != null
                && data.get(bean.getRegister_name()).register != null
                && data.get(bean.getRegister_name()).register.getRegisterName() != null) {
            content = content.replace("%registName%", bean.getRegister_name()+" " + bean.getMetric());
        } else {
            LOG.warn("id : " + bean.getRegister_name() + "no register info");
        }

        content = content.replace("%appId%", bean.getAppId());
        content = content.replace("%appType%", bean.getAppType().toString());
        content = content.replace("%extended%", bean.getExtended());



        if (JudgeType.offline == bean.getJudgeType()
                || JudgeType.no_data == bean.getJudgeType()) {
            content = content.replace("%ex_type%", bean.getJudgeType().desc);
        } else if (AppType.ols == bean.getAppType()) {
            content = content.replace("%metric%", bean.getMetric());
            if (bean.getCurrent_value() == -Integer.MAX_VALUE) {
                content = content.replace("%current_value%", "");
                // content = content.replace("%others%",
                // "no data has been received so far.");
            } else {
                content = content.replace(
                        "%current_value%",
                        NumberFormat.getInstance(Locale.CHINA).format(
                                new Double(bean.getCurrent_value()).longValue()));
                // content = content.replace("%others%", bean.getOthers());
            }
            content = content.replace(
                    "%threshold%",
                    NumberFormat.getInstance(Locale.CHINA).format(
                            new Double(bean.getThreshold()).longValue()));
            content = content.replace("%node%", bean.getNode());
            content = content.replace("%component%",
                    String.valueOf(bean.getComponent()));
        } else if (AppType.job == bean.getAppType()) {
            content = content.replace("%metric%", bean.getMetric());
            content = content.replace(
                    "%threshold%",
                    NumberFormat.getInstance(Locale.CHINA).format(
                            new Double(bean.getThreshold()).longValue()));
            content = content.replace(
                    "%current_value%",
                    NumberFormat.getInstance(Locale.CHINA).format(
                            new Double(bean.getCurrent_value()).longValue()));
            content = content.replace("%appId%", bean.getAppId());
        } else {
            content = content.replace("%metric%", "registerName:" + bean.getRegister_name()
                    + "  metricName:" + bean.getMetric() + "  node:"+bean.getNode());
            content = content.replace(
                    "%threshold%",
                    NumberFormat.getInstance(Locale.CHINA).format(
                            new Double(bean.getThreshold()).longValue()));
            content = content.replace(
                    "%current_value%",
                    NumberFormat.getInstance(Locale.CHINA).format(
                            new Double(bean.getCurrent_value()).longValue()));
        }

        String theme = email.getTheme();
        final String from = email.getSentPeople();

        ArrayList<String> lists = new ArrayList<String>();

        String userMapping = MonitorCache.getInstance().emailUserMapping.get(bean.getUser());
        if (userMapping != null) {
            String[] uLists = userMapping.split(",");
            for (String list : uLists) {
                lists.add(list);
            }
        }

        lists.addAll(email.getReceiveList());

        warningManager.emailWarning(lists,content,theme,from);


        /*
        Properties props = System.getProperties();
        // -- Attaching to default Session, or we could start a new one --
//    props.put("mail.smtp.host", "localhost");

        props.put("mail.smtp.host", "mail.staff.sina.com.cn");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props, new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, "DGM.com2");
            }
        });

        Message msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(from));

            for (String to : lists) {
                msg.addRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(to, false));
            }
            msg.setSubject(theme);
            msg.setText(content);
            // -- Set some other header information --
            msg.setHeader("X-Mailer", "LOTONtechEmail");
            msg.setSentDate(new Date());
            // -- Send the message --
            Transport.send(msg);

            // Transport t = session.getTransport("smtp");
            // try {
            // t.sendMessage(msg, msg.getAllRecipients());
            // } finally {
            // t.close();
            // }
            // LOG.info("Message sent OK.");
           */
//        } catch (Exception e) {
//            System.out.println("MessagingException");
//            e.printStackTrace();
//            LOG.error(e.getMessage());
//        }
        LOG.info("sent email " + content + " to : " + lists + " id : "
                + bean.getRegister_name());
    }

    class SmsMonitor implements Runnable {

        public void run() {
            while (true) {
                try {
                    ReceiveBean bean = smsWarning.take();
                    smsWarning(bean);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    class EmailMonitor implements Runnable {

        public void run() {
            while (true) {
                try {
                    ReceiveBean bean = emailWarning.take();
                    emailWarning(bean);
                } catch (Exception e) {
                    LOG.error("alarm exception : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

    }

    private static int getCurrentHourOfDay() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public static void main(String[] args) throws IOException {
        String content = "it is a test lile1";
        content = content.replaceAll(" ", "%20");
        System.out.println(content);
        String urlContent = urlPrefixNew.replace("$content", content);
        urlContent = urlContent.replace("$users", "lile1");
        System.out.println(urlContent);
        URL url = new URL(urlContent);
        HttpURLConnection httpURL = (HttpURLConnection) url.openConnection();
        httpURL.connect();
        httpURL.getInputStream();
    }
}
