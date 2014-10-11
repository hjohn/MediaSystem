package hs.mediasystem.ext.screens.collection.banner;

import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.UserLayout;
import hs.mediasystem.screens.collection.CollectionSelectorPresentation;
import hs.mediasystem.screens.collection.DuoPaneCollectionSelector;
import hs.mediasystem.screens.collection.detail.DetailPanePresentation;
import hs.mediasystem.screens.collection.detail.DetailView;
import hs.mediasystem.screens.collection.detail.StandardDetailPaneAreaLayout;

import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.scene.Node;

import javax.inject.Inject;
import javax.inject.Provider;

public class DetailAndBannersLayout implements UserLayout<MediaRoot, CollectionSelectorPresentation> {
  private final Provider<BannerListPane> bannerListPaneProvider;
  private final Set<Layout<? extends Object, ? extends DetailPanePresentation>> layouts;
  private final Provider<CollectionSelectorPresentation> presentationProvider;

  @Inject
  public DetailAndBannersLayout(Provider<CollectionSelectorPresentation> presentationProvider, Provider<BannerListPane> bannerListPaneProvider, Set<Layout<? extends Object, ? extends DetailPanePresentation>> layouts) {
    this.presentationProvider = presentationProvider;
    this.bannerListPaneProvider = bannerListPaneProvider;
    this.layouts = layouts;
  }

  @Override
  public String getId() {
    return "detailAndBanners";
  }

  @Override
  public String getTitle() {
    return "Detail and Banners";
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

    BannerListPane listPane = bannerListPaneProvider.get();
    DetailView detailPane = new DetailView(layouts, false, new StandardDetailPaneAreaLayout());

    detailPane.content.bind(presentation.focusedMediaNode);

    Bindings.bindContentBidirectional(listPane.mediaNodes, presentation.mediaNodes);

    listPane.focusedMediaNode.bindBidirectional(presentation.focusedMediaNode);
    listPane.onNodeSelected.bindBidirectional(presentation.onSelect);

    pane.placeLeft(detailPane);
    pane.placeRight(listPane);

    presentation.defaultInputFocus.set(listPane);

    return pane;
  }
}
