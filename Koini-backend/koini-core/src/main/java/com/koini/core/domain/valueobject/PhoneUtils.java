package com.koini.core.domain.valueobject;

public final class PhoneUtils {

  private PhoneUtils() {
  }

  public static String normalize(String phone) {
    if (phone == null) {
      return null;
    }
    String digits = phone.replaceAll("[^0-9]", "");
    if (digits.startsWith("0") && digits.length() == 10) {
      return "263" + digits.substring(1);
    }
    return digits;
  }

  public static boolean isValid(String phone) {
    String normalized = normalize(phone);
    return normalized != null && normalized.matches("^[0-9]{10,15}$");
  }

  public static String mask(String phone) {
    String normalized = normalize(phone);
    if (normalized == null) {
      return null;
    }
    if (normalized.length() <= 8) {
      return "****" + normalized.substring(Math.max(0, normalized.length() - 2));
    }
    String start = normalized.substring(0, 4);
    String end = normalized.substring(normalized.length() - 4);
    return start + "****" + end;
  }
}
