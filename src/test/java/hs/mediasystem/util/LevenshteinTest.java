package hs.mediasystem.util;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LevenshteinTest {
  private final String text;
  private final String matchText;

  public LevenshteinTest(String text, String matchText) {
    this.text = text;
    this.matchText = matchText;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]
      {
        {"Star Wars", "Star Warz"},
        {"Star Wars", "Staar Wars"},
        {"Star Wars", "Satr Wars"},
        {"Star Wars", "Star Wars - 04 - A new hope"},
        {"Star Wars Episode IV: A new hope", "Star Wars - 04 - A new hope"},
        {"Star Wars", "Stargate"},
        {"Star Wars", "Fantasic Four"},
        {"Star Wars: Clone Wars", "Star Wars"},
        {"Star Wars: Clone Wars", "Star Wars 4"},
        {"Star Wars: Clone Wars", "Star Wars 4 A New Hope"},
        {"Star Wars: Episode IV - A New Hope", "Star Wars"},
        {"Star Wars: Episode IV - A New Hope", "Star Wars 4"},
        {"Star Wars: Episode IV - A New Hope", "Star Wars 4 A New Hope"}
      }
    );
  }

  @Test
  public void test() {
    double compare = Levenshtein.compare(text.toLowerCase(), matchText.toLowerCase());
    System.out.println(text + " + " + matchText + " -> " + compare);
  }
}
