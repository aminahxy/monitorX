package com.sina.data.alarm.warning;


import com.sina.data.util.ConfUtils;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class WarningManagerImpl implements WarningManagerIface {
    public static final Logger LOG = LoggerFactory.getLogger(WarningManagerImpl.class);
    private String baseSmsUrl;
    private String baseEmailUrl;
    private static WarningManagerImpl warningManagerImpl;

    public static final String URL_PREFIX_KEY = "sms.url.prefix";
    public static final String MAIL_URL_PREFIX_KEY = "mail.url.prefix";

    // only use to send mail
    // private Properties props = System.getProperties();

    // -- Attaching to default Session, or we could start a new one --
    private WarningManagerImpl() {
        String configuredUrl = ConfUtils.getString(URL_PREFIX_KEY, null);
        baseSmsUrl = configuredUrl != null ? configuredUrl
                : "http://monitor.erp.sina.com.cn/index.php/interface/sendsms/"
                + "sendSinaWatch?users=$users&service=SinaScheduler&object="
                + "Warnning&subject=$content&sendType=sms";
        String mail_url = ConfUtils.getString(MAIL_URL_PREFIX_KEY, null);
        baseEmailUrl = mail_url != null ? mail_url
                : "http://monitor.erp.sina.com.cn/index.php/interface/sendsms/" +
                "sendSinaWatch?users=$users&service=SinaScheduler&object=" +
                "Warnning&subject=$subject&sendType=email&content=$content";

        // props.put("mail.smtp.host", MAIL_SMTP_HOST);
        // use to test
        // props.put("mail.smtp.host", "staff.sina.com.cn");
        // props.put("mail.smtp.auth", "true");
    }

    /**
     * Only entrance to get a warning manager. The parameter is used only during
     * first successful calling.
     *
     * @return
     */
    synchronized public static WarningManagerImpl getWarningManager() {
        if (warningManagerImpl != null) {
            return warningManagerImpl;
        }
        warningManagerImpl = new WarningManagerImpl();
        return warningManagerImpl;
    }

    /**
     * send warning info to mobile phone
     *
     * @param recipients User's staff email prefix
     * @param body       waring info
     * @return
     */
    public int smsWarning(List<String> recipients, String body) {
        StringBuilder sb = new StringBuilder();
        for (String emailPrefix : recipients) {
            sb.append(emailPrefix).append(',');
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        String realUrl = baseSmsUrl.replace("$users", sb.toString()).replace("$content", body)
                .replaceAll("\\s", "%20");
        try {
            URL url = new URL(realUrl);
            HttpURLConnection httpURL = (HttpURLConnection) url.openConnection();
            httpURL.connect();
            InputStream is = httpURL.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            sb.delete(0, sb.length());
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            sb.delete(0, sb.lastIndexOf("{"));
            JSONObject jsonObject = JSONObject.fromObject(sb.toString());
            if (jsonObject.get("message") instanceof JSONNull) {
                LOG.info("Success when sending sms \"" + body + "\"  to " + recipients);
                return recipients.size();
            } else {
                LOG.error("Error when sending sms \"" + body + "\". Message is " + sb.toString());
                return 0;
            }
        } catch (Exception e) {
            LOG.error("Error when sending sms \"" + body + "\" to " + recipients, e);
            return 0;
        }
    }

    /**
     * send warning info to user email
     *
     * @param recipients receive emails
     * @param body       waring info
     * @param subject
     * @param from
     * @return
     */
    public int emailWarning(List<String> recipients, String body, String subject, String from) {
        StringBuilder sb = new StringBuilder();
        for (String email : recipients) {
            String[] emailPrefixs = email.split("@");
            sb.append(emailPrefixs[0]).append(',');
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        String realUrl = baseEmailUrl.replace("$users", sb.toString()).replace("$content", body).replace("$subject", subject)
                .replaceAll("\\s", "%20");
        LOG.info("boyan add   "+realUrl);
        try {
            URL url = new URL(realUrl);
            HttpURLConnection httpURL = (HttpURLConnection) url.openConnection();
            httpURL.connect();
            InputStream is = httpURL.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            sb.delete(0, sb.length());
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            sb.delete(0, sb.lastIndexOf("{"));
            JSONObject jsonObject = JSONObject.fromObject(sb.toString());
            if (jsonObject.get("message") instanceof JSONNull) {
                LOG.info("Success when sending email \"" + body + "\"  to " + recipients);
                return recipients.size();
            } else {
                LOG.error("Error when sending email \"" + body + "\". Message is " + sb.toString());
                return 0;
            }
        } catch (Exception e) {
            LOG.error("Error when sending email \"" + body + "\" to " + recipients, e);
            return 0;
        }
    }

}
