package com.sina.data.alarm.warning;

import java.util.List;

public interface WarningManagerIface {

	public  int smsWarning(List<String> recipients, String body);
	public  int emailWarning(List<String> recipients, String body,
							 String subject, String from);
}
