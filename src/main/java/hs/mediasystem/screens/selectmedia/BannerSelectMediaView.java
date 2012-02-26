package hs.mediasystem.screens.selectmedia;

public class BannerSelectMediaView extends AbstractDuoPaneSelectMediaView {

  public BannerSelectMediaView() {
    super(new BannerListPane(), new DetailPane(), new BackgroundPane());
  }
}