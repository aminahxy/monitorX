package com.sina.data.common;

public enum JudgeType {
  le("less than or equal to"), 
  lt("less than"),
  ge("greater than or equal to"), 
  gt("greater than"), 
  eq("equal to"), 
  ne("not equal to"),
  offline("app offline"),
  no_data("no fetched data");

  public String desc;

  JudgeType(String desc) {
    this.desc = desc;
  }
  
}
