package hs.mediasystem.ext.screens.collection.tree;

import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.UserLayout;
import hs.mediasystem.screens.collection.CollectionSelectorPresentation;
import hs.mediasystem.screens.collection.DuoPaneCollectionSelector;
import hs.mediasystem.screens.collection.detail.DetailPanePresentation;
import hs.mediasystem.screens.collection.detail.DetailView;
import hs.mediasystem.screens.collection.detail.StandardDetailPaneAreaLayout;

import java.util.Set;

import javafx.scene.Node;

import javax.inject.Inject;
import javax.inject.Provider;

public class DetailAndListLayout implements UserLayout<MediaRoot, CollectionSelectorPresentation> {
  private final Provider<TreeListPane> treeListPaneProvider;
  private final Set<Layout<? extends Object, ? extends DetailPanePresentation>> layouts;
  private final Provider<CollectionSelectorPresentation> presentationProvider;

  @Inject
  public DetailAndListLayout(Provider<CollectionSelectorPresentation> presentationProvider, Provider<TreeListPane> treeListPaneProvider, Set<Layout<? extends Object, ? extends DetailPanePresentation>> layouts) {
    this.presentationProvider = presentationProvider;
    this.treeListPaneProvider = treeListPaneProvider;
    this.layouts = layouts;
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
  public Class<MediaRoot> getContentClass() {
    return MediaRoot.class;
  }

  @Override
  public CollectionSelectorPresentation createPresentation() {
    return presentationProvider.get();
  }

  @Override
  public Node createView(CollectionSelectorPresentation presentation) {
    DuoPaneCollectionSelector pane = new DuoPaneCollectionSelector();

    TreeListPane listPane = treeListPaneProvider.get();
    DetailView detailPane = new DetailView(layouts, false, new StandardDetailPaneAreaLayout());

    detailPane.content.bind(presentation.focusedMediaNode);

    listPane.rootMediaNode.bindBidirectional(presentation.rootMediaNode);
    listPane.focusedMediaNode.bindBidirectional(presentation.focusedMediaNode);
    listPane.onNodeSelected.bindBidirectional(presentation.onSelect);
    listPane.onNodeAlternateSelect.set(presentation.onInfoSelect);

    pane.placeLeft(detailPane);
    pane.placeRight(listPane);

    presentation.defaultInputFocus.set(listPane);

    return pane;
  }
}
