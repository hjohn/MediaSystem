package hs.mediasystem.ext.screens.collection.tree;

import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.collection.CollectionSelectorLayoutConf;
import hs.mediasystem.screens.collection.CollectionSelectorPresentation;
import hs.mediasystem.screens.collection.AbstractDetailPane;
import hs.mediasystem.screens.collection.DuoPaneCollectionSelectorLayout;
import hs.mediasystem.screens.collection.SmallDetailPane;
import hs.mediasystem.util.MapBindings;
import javafx.scene.Node;

import javax.inject.Inject;
import javax.inject.Provider;

public class ListAndDetailLayoutConf implements CollectionSelectorLayoutConf {
  private final Provider<TreeListPane> treeListPaneProvider;
  private final Provider<SmallDetailPane> detailPaneProvider;

  @Inject
  public ListAndDetailLayoutConf(Provider<TreeListPane> treeListPaneProvider, Provider<SmallDetailPane> detailPaneProvider) {
    this.treeListPaneProvider = treeListPaneProvider;
    this.detailPaneProvider = detailPaneProvider;
  }

  @Override
  public String getId() {
    return "listAndDetail";
  }

  @Override
  public String getTitle() {
    return "List and Detail";
  }

  @Override
  public boolean isSuitableFor(MediaRoot mediaRoot) {
    return true;
  }

  @Override
  public Node create(CollectionSelectorPresentation presentation) {
    DuoPaneCollectionSelectorLayout layout = new DuoPaneCollectionSelectorLayout();

    TreeListPane listPane = treeListPaneProvider.get();
    AbstractDetailPane detailPane = detailPaneProvider.get();

    detailPane.content.bind(MapBindings.select(presentation.focusedMediaNode, "media"));

    listPane.rootMediaNode.bindBidirectional(presentation.rootMediaNode);
    listPane.focusedMediaNode.bindBidirectional(presentation.focusedMediaNode);
    listPane.onNodeSelected.set(presentation.onSelect);
    listPane.onNodeAlternateSelect.set(presentation.onInfoSelect);

    layout.placeLeft(listPane);
    layout.placeRight(detailPane);

    presentation.defaultInputFocus.set(listPane);

    return layout;
  }
}
