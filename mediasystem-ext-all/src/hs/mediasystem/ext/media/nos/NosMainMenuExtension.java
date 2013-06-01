package hs.mediasystem.ext.media.nos;

import javax.inject.Named;

import hs.mediasystem.framework.MediaRootType;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.selectmedia.SelectMediaLocation;
import hs.mediasystem.screens.selectmedia.StandardView;
import javafx.scene.image.Image;

@Named
public class NosMainMenuExtension implements MainMenuExtension {

  public NosMainMenuExtension() {
    StandardView.registerLayout(NosMediaTree.class, MediaRootType.MOVIES);
  }

  @Override
  public String getTitle() {
    return "NOS";
  }

  @Override
  public Image getImage() {
    return new Image(getClass().getResourceAsStream("/hs/mediasystem/ext/media/nos/nos.png"));
  }

  @Override
  public void select(final ProgramController controller) {
    controller.setLocation(new SelectMediaLocation(new NosMediaTree()));
  }

  @Override
  public double order() {
    return 0.4;
  }
}
