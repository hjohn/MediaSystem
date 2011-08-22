package hs.mediasystem;

import hs.mediasystem.screens.AbstractBlock;
import hs.ui.controls.AbstractGroup;

public class Screen {
  private final AbstractBlock block;
  private final Extensions extensions;

  public Screen(AbstractBlock block, Extensions extensions) {
    this.block = block;
    this.extensions = extensions;
  }

  public Screen(AbstractBlock block) {
    this(block, new Extensions());
  }

  public AbstractGroup<?> getContent(Controller controller) {
    return block.getContent(controller, extensions);
  }
}
