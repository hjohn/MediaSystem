package hs.mediasystem;

public class RomanLiteral {
  public static String toRomanLiteral(int value) {
    StringBuilder builder = new StringBuilder();
    String[] digits = {"L", "IL", "XL", "X", "IX", "V", "IV", "I"};
    int[] limits = {50, 49, 40, 10, 9, 5, 4, 1};

    for(int limit = 0; limit < limits.length; limit++) {
      while(value > 0) {
        if(value >= limits[limit]) {
          builder.append(digits[limit]);
          value -= limits[limit];
        }
        else {
          break;
        }
      }
    }

    return builder.toString();
  }
}
