package hs.mediasystem.dao;

import hs.mediasystem.db.Column;
import hs.mediasystem.db.Id;
import hs.mediasystem.db.Table;
import hs.mediasystem.framework.DefaultEnrichable;

import java.util.Date;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Table(name = "settings")
public class Setting extends DefaultEnrichable<Setting> {
  public enum PersistLevel {PERMANENT, TEMPORARY, SESSION}

  @Id
  private Integer id;

  @Column
  private String system;

  @Column
  private PersistLevel persistLevel;

  @Column
  private String key;

  @Column
  private Date lastUpdated;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
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
    protected void invalidated() {
      queueAsDirty();
    }
  };
  @Column
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
