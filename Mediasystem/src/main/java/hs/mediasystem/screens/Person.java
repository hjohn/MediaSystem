package hs.mediasystem.screens;

import hs.mediasystem.framework.EnrichCallback;
import hs.mediasystem.framework.EnricherBuilder;
import hs.mediasystem.framework.Entity;
import hs.mediasystem.framework.FinishEnrichCallback;
import hs.mediasystem.framework.Media;
import hs.mediasystem.fs.SourceImageHandle;
import hs.mediasystem.util.ImageHandle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Person extends Entity<Person> {
  public final ObjectProperty<hs.mediasystem.dao.Person> personRecord = object();

  public final StringProperty name = string();
  public final StringProperty birthPlace = string();
  public final StringProperty biography = string();
  public final ObjectProperty<Date> birthDate = object();
  public final ObjectProperty<ImageHandle> photo = object();

  public final ObjectProperty<ObservableList<Casting>> castings = list(new EnricherBuilder<Person, List<Casting>>()
    .require(personRecord)
    .enrich(new EnrichCallback<List<Casting>>() {
      @Override
      public List<Casting> enrich(Object... parameters) {
        hs.mediasystem.dao.Person person = (hs.mediasystem.dao.Person)parameters[0];

        List<hs.mediasystem.dao.Casting> castings = person.getCastings();
        List<Casting> result = new ArrayList<>();

        for(final hs.mediasystem.dao.Casting casting : castings) {
          Casting c = new Casting();

          c.media.set(create(Media.class, casting.getItem()));
          c.person.set(Person.this);
          c.characterName.set(casting.getCharacterName());
          c.index.set(casting.getIndex());
          c.role.set(casting.getRole());

          result.add(c);
        }

        return result;
      }
    })
    .finish(new FinishEnrichCallback<List<Casting>>() {
      @Override
      public void update(List<Casting> result) {
        castings.set(FXCollections.observableList(result));
      }
    })
    .build()
  );

  public Person() {
    personRecord.addListener(new ChangeListener<hs.mediasystem.dao.Person>() {
      @Override
      public void changed(ObservableValue<? extends hs.mediasystem.dao.Person> observableValue, hs.mediasystem.dao.Person old, hs.mediasystem.dao.Person current) {
        name.set(current.getName());
        birthPlace.set(current.getBirthPlace());
        biography.set(current.getBiography());
        birthDate.set(current.getBirthDate());

        if(current.getPhoto() != null) {
          photo.set(new SourceImageHandle(current.getPhoto(), "Person:/" + current.getName()));
        }
        else {
          photo.set(null);
        }
      }
    });
  }
}
