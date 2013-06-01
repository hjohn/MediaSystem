package hs.mediasystem.ext.selectmedia.tree;

import java.util.Set;

import hs.mediasystem.screens.selectmedia.AbstractDuoPaneStandardLayout;
import hs.mediasystem.screens.selectmedia.DetailPaneDecoratorFactory;

import javax.inject.Inject;

public class ListStandardLayout extends AbstractDuoPaneStandardLayout {

  @Inject
  public ListStandardLayout(TreeListPane treeListPane, Set<DetailPaneDecoratorFactory> detailPaneDecoratorFactories) {
    super(treeListPane, detailPaneDecoratorFactories);
  }
}