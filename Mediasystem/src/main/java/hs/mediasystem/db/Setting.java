package hs.mediasystem.db;

import hs.mediasystem.framework.DefaultEnrichable;

import java.util.Date;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Setting extends DefaultEnrichable<Setting> {
  public enum PersistLevel {PERMANENT, TEMPORARY, SESSION}

  private int id;

  private String system;
  private PersistLevel persistLevel;
  private String key;

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

  private final StringProperty value = new SimpleStringProperty() {
    @Override
    public void set(String s) {
      super.set(s);
      queueAsDirty();
    }
  };
  public String getValue() { return value.get(); }
  public void setValue(String value) { this.value.set(value); }
  public StringProperty valueProperty() { return value; }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Override
  public String toString() {
    return "Setting[id=" + id + "; system='" + system + "'; persistLevel=" + persistLevel + "; key='" + key + "'; value='" + getValue() +"']";
  }
}
