package com.group6.nhms.util;

/**
 * Tiện ích so sánh chuỗi theo thứ tự tự nhiên (Natural Alphanumeric Sort),
 * giúp sắp xếp đúng thứ tự số phòng và số giường (VD: P99 < P101, G101-2 < G101-10).
 */
public final class NaturalSortUtils {

    private NaturalSortUtils() {
    }

    public static int compareNatural(String a, String b) {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }

        int i = 0;
        int j = 0;
        int lenA = a.length();
        int lenB = b.length();

        while (i < lenA && j < lenB) {
            boolean isDigitA = Character.isDigit(a.charAt(i));
            boolean isDigitB = Character.isDigit(b.charAt(j));

            if (isDigitA && isDigitB) {
                int startA = i;
                int startB = j;
                while (i < lenA && Character.isDigit(a.charAt(i))) {
                    i++;
                }
                while (j < lenB && Character.isDigit(b.charAt(j))) {
                    j++;
                }

                String numA = stripLeadingZeros(a, startA, i);
                String numB = stripLeadingZeros(b, startB, j);

                int cmp = Integer.compare(numA.length(), numB.length());
                if (cmp != 0) {
                    return cmp;
                }
                cmp = numA.compareTo(numB);
                if (cmp != 0) {
                    return cmp;
                }
                // Tie-breaker nếu chỉ khác số lượng chữ số 0 ở đầu (VD: "01" vs "1")
                cmp = Integer.compare(i - startA, j - startB);
                if (cmp != 0) {
                    return cmp;
                }
            } else {
                char chA = a.charAt(i);
                char chB = b.charAt(j);
                int cmp = Character.compare(Character.toLowerCase(chA), Character.toLowerCase(chB));
                if (cmp != 0) {
                    return cmp;
                }
                cmp = Character.compare(chA, chB);
                if (cmp != 0) {
                    return cmp;
                }
                i++;
                j++;
            }
        }

        return Integer.compare(lenA - i, lenB - j);
    }

    private static String stripLeadingZeros(String s, int start, int end) {
        int idx = start;
        while (idx < end - 1 && s.charAt(idx) == '0') {
            idx++;
        }
        return s.substring(idx, end);
    }
}
