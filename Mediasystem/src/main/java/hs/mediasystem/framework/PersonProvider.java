package hs.mediasystem.framework;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import hs.mediasystem.entity.EnrichCallback;
import hs.mediasystem.entity.EnricherBuilder;
import hs.mediasystem.entity.EntityProvider;
import hs.mediasystem.entity.FinishEnrichCallback;

public class PersonProvider implements EntityProvider<hs.mediasystem.dao.Person, Person> {
  @Override
  public Person get(final hs.mediasystem.dao.Person dbPerson) {
    final Person person = new Person(dbPerson.getName(), dbPerson.getBirthPlace(), dbPerson.getBiography(), dbPerson.getBirthDate(), dbPerson.getPhoto() == null ? null : new SourceImageHandle(dbPerson.getPhoto(), "Person:/" + dbPerson.getName()));

    person.castings.setEnricher(new EnricherBuilder<Person, List<Casting>>(List.class)
      .enrich(new EnrichCallback<List<Casting>>() {
        @Override
        public List<Casting> enrich(Object... parameters) {
          List<hs.mediasystem.dao.Casting> castings = dbPerson.getCastings();
          List<Casting> result = new ArrayList<>();

          for(final hs.mediasystem.dao.Casting casting : castings) {
            Casting c = new Casting();

            c.media.set(person.create(Media.class, casting.getItem()));
            c.person.set(person);
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
          person.castings.set(FXCollections.observableList(result));
        }
      })
      .build()
    );

    return person;
  }
}