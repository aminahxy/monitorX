package com.sina.data.service.impl;


import com.sina.data.entry.AlarmEntry;
import com.sina.data.service.AlarmInterface;

public class GenericAlarmImpl extends AlarmInterface {

  @Override
  public AlarmEntry alarmInfo(AlarmEntry entry) {
    return entry;
  }

}
