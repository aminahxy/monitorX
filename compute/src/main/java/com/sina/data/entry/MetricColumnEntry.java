package com.sina.data.entry;

public class MetricColumnEntry {
  public final String name;
  public final String value;
  
  public MetricColumnEntry(String name,String value){
    this.name = name;
    this.value = value;
  }

  @Override
  public String toString() {
    return "[name=" + name + ", value=" + value + "]";
  }
  
  
  
}
