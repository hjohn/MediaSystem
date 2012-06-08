package hs.mediasystem.db;

import hs.mediasystem.persist.PersistTrigger;
import hs.mediasystem.persist.Persistable;

import java.util.Date;

public class Setting implements Persistable {
  public enum PersistLevel {PERMANENT, TEMPORARY, SESSION}

  private int id;

  private String system;
  private PersistLevel persistLevel;
  private String key;
  private String value;

  private Date lastUpdated;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getSystem() {
    return system;
  }

  public void setSystem(String system) {
    this.system = system;
  }

  public PersistLevel getPersistLevel() {
    return persistLevel;
  }

  public void setPersistLevel(PersistLevel persistLevel) {
    this.persistLevel = persistLevel;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Override
  public void setPersistTrigger(PersistTrigger persistTrigger) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "Setting[id=" + id + "; system='" + system + "'; persistLevel=" + persistLevel + "; key='" + key + "'; value='" + value +"']";
  }
}
