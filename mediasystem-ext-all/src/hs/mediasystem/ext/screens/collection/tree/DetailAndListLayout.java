package hs.mediasystem.ext.screens.collection.tree;

import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.collection.CollectionSelectorLayout;
import hs.mediasystem.screens.collection.CollectionSelectorPresentation;
import hs.mediasystem.screens.collection.AbstractDetailPane;
import hs.mediasystem.screens.collection.DuoPaneCollectionSelector;
import hs.mediasystem.screens.collection.SmallDetailPane;
import hs.mediasystem.util.MapBindings;
import javafx.scene.Node;

import javax.inject.Inject;
import javax.inject.Provider;

public class DetailAndListLayout implements CollectionSelectorLayout {
  private final Provider<TreeListPane> treeListPaneProvider;
  private final Provider<SmallDetailPane> detailPaneProvider;

  @Inject
  public DetailAndListLayout(Provider<TreeListPane> treeListPaneProvider, Provider<SmallDetailPane> detailPaneProvider) {
    this.treeListPaneProvider = treeListPaneProvider;
    this.detailPaneProvider = detailPaneProvider;
  }

  @Override
  public String getId() {
    return "detailAndList";
  }

  @Override
  public String getTitle() {
    return "Detail and List";
  }

  @Override
  public boolean isSuitableFor(MediaRoot mediaRoot) {
    return true;
  }

  @Override
  public Node create(CollectionSelectorPresentation presentation) {
    DuoPaneCollectionSelector layout = new DuoPaneCollectionSelector();

    TreeListPane listPane = treeListPaneProvider.get();
    AbstractDetailPane detailPane = detailPaneProvider.get();

    detailPane.content.bind(MapBindings.select(presentation.focusedMediaNode, "media"));

    listPane.rootMediaNode.bindBidirectional(presentation.rootMediaNode);
    listPane.focusedMediaNode.bindBidirectional(presentation.focusedMediaNode);
    listPane.onNodeSelected.set(presentation.onSelect);
    listPane.onNodeAlternateSelect.set(presentation.onInfoSelect);

    layout.placeLeft(detailPane);
    layout.placeRight(listPane);

    presentation.defaultInputFocus.set(listPane);

    return layout;
  }
}
