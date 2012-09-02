package hs.mediasystem.dao;

import hs.mediasystem.db.AnnotatedRecordMapper;
import hs.mediasystem.db.Column;
import hs.mediasystem.db.Table;

@Table(name = "castings")
public class Casting {

  @Column(name = "persons_id")
  private Person person;

  @Column(name = "items_id")
  private Item item;

  @Column
  private String role;

  @Column
  private String characterName;

  @Column
  private int index = Integer.MAX_VALUE;

  public Person getPerson() {
    person = AnnotatedRecordMapper.fetch(person);
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }

  public Item getItem() {
    item = AnnotatedRecordMapper.fetch(item);
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getCharacterName() {
    return characterName;
  }

  public void setCharacterName(String characterName) {
    this.characterName = characterName;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }
}
