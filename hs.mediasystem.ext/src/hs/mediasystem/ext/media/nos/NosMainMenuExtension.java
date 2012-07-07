package hs.mediasystem.ext.media.nos;

import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentation;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;
import hs.mediasystem.screens.selectmedia.StandardView;
import javafx.scene.image.Image;

public class NosMainMenuExtension implements MainMenuExtension {
  private volatile SelectMediaPresentationProvider selectMediaPresentationProvider;

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
    controller.getNavigator().navigateTo(new Destination("nos", getTitle()) {
      private SelectMediaPresentation presentation;
      private NosMediaTree mediaTree;

      @Override
      protected void init() {
        presentation = selectMediaPresentationProvider.get();
      }

      @Override
      protected void intro() {
        controller.showScreen(presentation.getView());
        if(mediaTree == null) {
          mediaTree = new NosMediaTree();
          presentation.setMediaTree(mediaTree);
        }
      }
    });
  }

  @Override
  public double order() {
    return 0.4;
  }
}
