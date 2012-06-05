package hs.mediasystem.ext.nos;

import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.fs.StandardTitleComparator;
import hs.mediasystem.screens.DefaultMediaGroup;
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

    SelectMediaPresentation.registerMediaGroup(NosMediaTree.class, new DefaultMediaGroup("Alphabetically", null, StandardTitleComparator.INSTANCE, false, false));
  }

  @Override
  public String getTitle() {
    return "NOS";
  }

  @Override
  public Image getImage() {
    return new Image(getClass().getResourceAsStream("/hs/mediasystem/ext/nos/nos.png"));
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
