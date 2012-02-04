package hs.mediasystem;

public class RomanLiteral {
  public static String toRomanLiteral(int value) {
    StringBuilder builder = new StringBuilder();
    String[] digits = {"L", "IL", "XL", "X", "IX", "V", "IV", "I"};
    int[] limits = {50, 49, 40, 10, 9, 5, 4, 1};
    int v = value;

    for(int limit = 0; limit < limits.length; limit++) {
      while(v > 0) {
        if(v < limits[limit]) {
          break;
        }

        builder.append(digits[limit]);
        v -= limits[limit];
      }
    }

    return builder.toString();
  }
}
