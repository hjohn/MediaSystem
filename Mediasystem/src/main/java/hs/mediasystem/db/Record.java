package hs.mediasystem.db;

import java.util.Date;
import java.util.Map;

public class Record {
  private final Object[] data;
  private final Map<String, Integer> fieldMapping;

  public Record(Object[] data, Map<String, Integer> fieldMapping) {
    this.data = data;
    this.fieldMapping = fieldMapping;
  }

  public Object get(String fieldName) {
    return data[fieldMapping.get(fieldName)];
  }

  public String getString(String fieldName) {
    return (String)get(fieldName);
  }

  public Boolean getBoolean(String fieldName) {
    return (Boolean)get(fieldName);
  }

  public Integer getInteger(String fieldName) {
    return (Integer)get(fieldName);
  }

  public Long getLong(String fieldName) {
    return (Long)get(fieldName);
  }

  public Float getFloat(String fieldName) {
    return (Float)get(fieldName);
  }

  public byte[] getBytes(String fieldName) {
    return (byte[])get(fieldName);
  }

  public Date getDate(String fieldName) {
    return (Date)get(fieldName);
  }
}
