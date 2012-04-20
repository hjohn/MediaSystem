package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.fs.NosMediaTree;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Provider;

public class NosMainMenuExtension implements MainMenuExtension {
  private final Provider<SelectMediaPresentation> selectMediaPresentationProvider;

  @Inject
  public NosMainMenuExtension(Provider<SelectMediaPresentation> selectMediaPresentationProvider) {
    this.selectMediaPresentationProvider = selectMediaPresentationProvider;
  }

  @Override
  public String getTitle() {
    return "NOS";
  }

  @Override
  public Image getImage() {
    return new Image("images/video.png");
  }

  @Override
  public Destination getDestination(final ProgramController controller) {
    return new Destination(getTitle()) {
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
    };
  }

  @Override
  public double order() {
    return 0.4;
  }
}
