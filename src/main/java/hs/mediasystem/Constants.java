package hs.mediasystem;

import hs.models.Model;
import hs.models.ValueModel;

import java.awt.Color;
import java.awt.Font;

public class Constants {
  public static final Model<Color> MAIN_TEXT_COLOR = new ValueModel<Color>(new Color(155, 190, 255, 200));
  public static final Model<Font> HEADER_FONT = new ValueModel<Font>(new Font("sans serif", Font.BOLD, 14));
}
