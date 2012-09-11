package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemsDao {
  private final Database database;
  private final PersonsDao personsDao;

  @Inject
  public ItemsDao(Database database, PersonsDao personsDao) {
    this.database = database;
    this.personsDao = personsDao;

    Database.registerFetcher(new CastingsFetcher(database));
  }

  public Item loadItem(final ProviderId providerId) throws ItemNotFoundException {
    assert providerId != null;

    try(Transaction transaction = database.beginTransaction()) {
      System.out.println("[FINE] ItemsDao.getItem() - Selecting Item with " + providerId);

      Item item = transaction.selectUnique(Item.class, "type=? AND provider=? AND providerid=?", providerId.getType(), providerId.getProvider(), providerId.getId());

      if(item == null) {
        throw new ItemNotFoundException(providerId);
      }

      return item;
    }
  }

  public void storeItem(Item item) {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.insert(item);

      storeCastings(item, transaction);

      transaction.commit();
    }
  }

  public void updateItem(Item item) {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.update(item);

      if(item.isCastingsLoaded()) {
        transaction.deleteChildren("castings", "items", item.getId());

        storeCastings(item, transaction);
      }

      transaction.commit();
    }
  }

  private void storeCastings(Item item, Transaction transaction) {
    for(Casting casting : item.getCastings()) {
      Person person = personsDao.findByName(casting.getPerson().getName());

      if(person != null) {
        casting.getPerson().setId(person.getId());
        if(casting.getPerson().getPhotoURL() == null) {
          casting.getPerson().setPhotoURL(person.getPhotoURL());
        }
      }

      transaction.merge(casting.getPerson());
      transaction.insert(casting);
    }
  }
}