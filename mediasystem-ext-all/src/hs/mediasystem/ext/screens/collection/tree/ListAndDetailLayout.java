package hs.mediasystem.ext.screens.collection.tree;

import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.collection.CollectionSelectorPresentation;
import hs.mediasystem.screens.collection.DuoPaneCollectionSelector;
import hs.mediasystem.screens.collection.detail.DetailPanePresentation;
import hs.mediasystem.screens.collection.detail.DetailView;
import hs.mediasystem.screens.collection.detail.StandardDetailPaneAreaLayout;

import java.util.Set;

import javafx.scene.Node;

import javax.inject.Inject;
import javax.inject.Provider;

public class ListAndDetailLayout implements Layout<MediaRoot, CollectionSelectorPresentation> {
  private final Provider<TreeListPane> treeListPaneProvider;
  private final Set<Layout<? extends Object, DetailPanePresentation>> layouts;

  @Inject
  public ListAndDetailLayout(Provider<TreeListPane> treeListPaneProvider, Set<Layout<? extends Object, DetailPanePresentation>> layouts) {
    this.treeListPaneProvider = treeListPaneProvider;
    this.layouts = layouts;
  }

  @Override
  public Class<MediaRoot> getContentClass() {
    return MediaRoot.class;
  }

  @Override
  public Node create(CollectionSelectorPresentation presentation) {
    DuoPaneCollectionSelector pane = new DuoPaneCollectionSelector();

    TreeListPane listPane = treeListPaneProvider.get();
    DetailView detailPane = new DetailView(layouts, false, new StandardDetailPaneAreaLayout());

    detailPane.content.bind(presentation.focusedMediaNode);

    listPane.rootMediaNode.bindBidirectional(presentation.rootMediaNode);
    listPane.focusedMediaNode.bindBidirectional(presentation.focusedMediaNode);
    listPane.onNodeSelected.set(presentation.onSelect);
    listPane.onNodeAlternateSelect.set(presentation.onInfoSelect);

    pane.placeLeft(listPane);
    pane.placeRight(detailPane);

    presentation.defaultInputFocus.set(listPane);

    return pane;
  }
}
