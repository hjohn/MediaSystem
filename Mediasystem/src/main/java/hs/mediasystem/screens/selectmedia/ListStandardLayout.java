package hs.mediasystem.screens.selectmedia;

public class ListStandardLayout extends AbstractDuoPaneStandardLayout {

  public ListStandardLayout() {
    super(new TreeListPane(), new DetailPane());
  }
}