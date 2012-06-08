package hs.mediasystem.ext.selectmedia.tree;

import hs.mediasystem.screens.selectmedia.AbstractDuoPaneStandardLayout;
import hs.mediasystem.screens.selectmedia.DetailPane;

public class ListStandardLayout extends AbstractDuoPaneStandardLayout {

  public ListStandardLayout() {
    super(new TreeListPane(), new DetailPane());
  }
}