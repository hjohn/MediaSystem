package hs.mediasystem;

import hs.models.Model;
import hs.models.ValueModel;
import hs.ui.controls.DynamicLabel;
import hs.ui.controls.Label;

import java.awt.Color;
import java.awt.Font;

public class Constants {
  public static final Model<Color> MAIN_TEXT_COLOR = new ValueModel<Color>(new Color(155, 190, 255, 200));
  public static final Color SCROLL_BAR_COLOR = new Color(155, 190, 255, 128);
  public static final Model<Font> HEADER_FONT = new ValueModel<Font>(new Font("sans serif", Font.BOLD, 14));
  public static final Model<Font> INFO_HEADER_FONT = new ValueModel<Font>(new Font("sans serif", Font.BOLD, 12));
  public static final Model<Font> INFO_TEXT_FONT = new ValueModel<Font>(new Font("sans serif", Font.PLAIN, 14));
  
  public static final int HEIGHT = 48;
  public static final int SUBTITLE_HEIGHT = 16;
  public static final int TITLE_HEIGHT = HEIGHT - SUBTITLE_HEIGHT;
  public static final Font LIST_LARGE_FONT = new Font("Sans Serif", Font.PLAIN, TITLE_HEIGHT - 4);
  public static final Font LIST_SMALL_FONT = new Font("Sans Serif", Font.PLAIN, SUBTITLE_HEIGHT - 4);
  
  public static void styleInfoHeader(Label label) {
    label.fgColor().set(MAIN_TEXT_COLOR.get());
    label.font().set(INFO_HEADER_FONT.get());
  }
  
  public static void styleInfoText(DynamicLabel label) {
    label.fgColor().set(MAIN_TEXT_COLOR.get().brighter());
    label.font().set(INFO_TEXT_FONT.get());
  }
}
