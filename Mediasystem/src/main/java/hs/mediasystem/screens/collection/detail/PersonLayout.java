package hs.mediasystem.screens.collection.detail;

import hs.mediasystem.framework.Person;

public class PersonLayout extends AbstractDetailViewLayout<Person> {

  @Override
  public Class<?> getContentClass() {
    return Person.class;
  }

  @Override
  protected DetailPane<Person> createDetailPane(DetailPanePresentation presentation) {
    return PersonDetailPane.create(presentation.getAreaLayout(), presentation.isInteractive());
  }
}
