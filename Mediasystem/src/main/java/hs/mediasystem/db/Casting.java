package hs.mediasystem.db;

public class Casting {
  private Person person;
  private Item item;

  private String role;
  private String characterName;
  private int index = Integer.MAX_VALUE;

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }

  public Item getItem() {
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
