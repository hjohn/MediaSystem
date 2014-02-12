package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MediaData extends Entity {
  public final StringProperty uri = stringProperty("uri");
  public final LongProperty fileLength = longProperty("fileLength");
  public final ObjectProperty<Long> osHash = object("osHash");
  public final IntegerProperty resumePosition = integerProperty("resumePosition");
  public final BooleanProperty viewed = booleanProperty("viewed");

  public final ObjectProperty<ObservableList<Identifier>> identifiers = list("identifiers", Identifier.class);

  public MediaData setAll(String uri, long fileLength, Long osHash, int resumePosition, boolean viewed, ObservableList<Identifier> identifiers) {
    this.uri.set(uri);
    this.fileLength.set(fileLength);
    this.osHash.set(osHash);
    this.resumePosition.set(resumePosition);
    this.viewed.set(viewed);
    this.identifiers.set(identifiers);

    return this;
  }

  public void addIdentifier(Identifier identifier) {
    // TODO trick here to always change the identifiers object property in order to trigger MediaDataPersister
    ObservableList<Identifier> currentIdentifiers = identifiers.get();

    ObservableList<Identifier> newIdentifiers = currentIdentifiers == null ? FXCollections.observableArrayList() : FXCollections.observableArrayList(currentIdentifiers);

    newIdentifiers.add(identifier);

    identifiers.set(newIdentifiers);
  }

  public Identifier findIdentifier(String mediaType, String provider) {
    if(identifiers.get() != null) {
      for(Identifier identifier : identifiers.get()) {
        if(identifier.providerId.get().getProvider().equals(provider) && identifier.providerId.get().getType().equals(mediaType)) {
          return identifier;
        }
      }
    }

    return null;
  }
}
