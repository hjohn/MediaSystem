package hs.mediasystem.screens.selectmedia;

public class ListSelectMediaView extends AbstractDuoPaneSelectMediaView {

  public ListSelectMediaView() {
    super(new TreeListPane(), new DetailPane(), new BackgroundPane());
  }
}