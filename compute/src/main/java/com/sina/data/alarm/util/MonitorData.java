package com.sina.data.alarm.util;



import com.sina.data.common.JudgeType;

import java.util.ArrayList;

public class MonitorData {
	
	public SmsBean sms;
	public EmailBean email;
	public RegisterBean register;
	
	public MonitorData() {
		sms = new SmsBean();
		email = new EmailBean();
		register = new RegisterBean();
	}
	

	@Override
  public String toString() {
    return "sms : " + sms + " email : " + email + " register : " + register;
  }


  public class SmsBean {
		private boolean enable;
		private String content;
		private int intervalTime;
		private int lastTime;
		private int extend;
//		private ArrayList<String> receiveList;
		private String receiveList;

		public String getReceiveList() {
			return receiveList;
		}

		public void setReceiveList(String receiveList) {
			this.receiveList = receiveList;
		}

		public boolean isEnable() {
			return enable;
		}

		public void setEnable(boolean enable) {
			this.enable = enable;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public int getIntervalTime() {
			return intervalTime;
		}

		public void setIntervalTime(int intervalTime) {
			this.intervalTime = intervalTime;
		}

		public int getLastTime() {
			return lastTime;
		}

		public void setLastTime(int lastTime) {
			this.lastTime = lastTime;
		}

		public int getExtend() {
			return extend;
		}

		public void setExtend(int extend) {
			this.extend = extend;
		}
		
		public String toString() {
			return enable+","+content+","+intervalTime+","+lastTime+","+extend;
		}

	}

	public class RegisterBean {
	  private String registerName;
		private String metricName;
		private ArrayList<String> hostList;
		private float threshold;
		private float mobileThreshold;
		private JudgeType judgeType;
		private String extend;
		private String monitorType;
		
		public String toString() {
			return registerName + ","+ metricName+","+hostList.toString()+","+threshold+","+judgeType+","+extend;
		}

		public String getRegisterName() {
      return registerName;
    }

    public void setRegisterName(String registerName) {
      this.registerName = registerName;
    }


    public float getMobileThreshold() {
			return mobileThreshold;
		}

		public void setMobileThreshold(float mobileThreshold) {
			this.mobileThreshold = mobileThreshold;
		}

		public String getMetricName() {
			return metricName;
		}

		public void setMetricName(String metricName) {
			this.metricName = metricName;
		}

		public ArrayList<String> getHostList() {
			return hostList;
		}

		public void setHostList(ArrayList<String> hostList) {
			this.hostList = hostList;
		}

		public float getThreshold() {
			return threshold;
		}

		public void setThreshold(float threshold) {
			this.threshold = threshold;
		}

		public JudgeType getJudgeType() {
      return judgeType;
    }

    public void setJudgeType(JudgeType judgeType) {
      this.judgeType = judgeType;
    }

    public String getExtend() {
			return extend;
		}

		public void setExtend(String extend) {
			this.extend = extend;
		}

    public String getMonitorType() {
      return monitorType;
    }

    public void setMonitorType(String monitorType) {
      this.monitorType = monitorType;
    }
		
	}

	public class EmailBean {
		private boolean enable;
		private ArrayList<String> receiveList;
		private String sentPeople;
		private String theme;
		private String content;
		private int intervalTime;
		private int lastTime;
		private int extend;

		public String toString(){
			return receiveList.toString()+","+sentPeople+","+theme+","+content+","+intervalTime+","+lastTime+","+extend+","+enable;
		}
		public ArrayList<String> getReceiveList() {
			return receiveList;
		}

		public boolean isEnable() {
			return enable;
		}
		public void setEnable(boolean enable) {
			this.enable = enable;
		}
		public void setReceiveList(ArrayList<String> receiveList) {
			this.receiveList = receiveList;
		}

		public String getSentPeople() {
			return sentPeople;
		}

		public void setSentPeople(String sentPeople) {
			this.sentPeople = sentPeople;
		}

		public String getTheme() {
			return theme;
		}

		public void setTheme(String theme) {
			this.theme = theme;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public int getIntervalTime() {
			return intervalTime;
		}

		public void setIntervalTime(int intervalTime) {
			this.intervalTime = intervalTime;
		}

		public int getLastTime() {
			return lastTime;
		}

		public void setLastTime(int lastTime) {
			this.lastTime = lastTime;
		}

		public int getExtend() {
			return extend;
		}

		public void setExtend(int extend) {
			this.extend = extend;
		}

	}
}
