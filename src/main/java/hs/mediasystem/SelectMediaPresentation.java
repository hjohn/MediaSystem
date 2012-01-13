package hs.mediasystem;

import hs.mediasystem.framework.MediaItem;
import javafx.scene.control.TreeItem;

public class SelectMediaPresentation {
  private final SelectItemScene view;
  private final ProgramController controller;

  public SelectMediaPresentation(ProgramController controller, SelectItemScene view) {
    this.controller = controller;
    this.view = view;
  }

  public void itemFocused(TreeItem<MediaItem> treeItem) {
    if(treeItem != null) {
      final MediaItem item = treeItem.getValue();

      view.update(item);
    }
  }

  public void itemSelected(TreeItem<MediaItem> treeItem) {
    MediaItem mediaItem = treeItem.getValue();

    if(mediaItem.isLeaf()) {
      controller.play(mediaItem);
    }
    else if(mediaItem.isRoot()) {
      view.setMediaTree(mediaItem.getRoot());
    }
    else if(mediaItem instanceof hs.mediasystem.framework.Group) {
      System.out.println("Expanding " + mediaItem.getTitle());
      treeItem.setExpanded(true);
    }
  }
}
