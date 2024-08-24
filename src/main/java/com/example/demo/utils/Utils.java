package com.example.demo.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utils {

    private static final String DEFAULT_FORMAT_DATE = "dd/MM/yyyy";

    private Utils() {}
    public static <T> List<List<T>> partition(List<T> dataList, int size) {
        List<List<T>> subLists = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i += size) {
            subLists.add(dataList.subList(i, Math.min(i + size, dataList.size())));
        }

        return subLists;
    }

    public static <T> boolean isNullOrEmpty(List<T> dataList) {
        return dataList == null || dataList.isEmpty();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String NVL(String text) {
        return NVL(text, "");
    }

    public static <T> T NVL(T number, T defaultValue) {
        return number == null ? defaultValue : number;
    }

    public static Long NVL(Long number) {
        return NVL(number, 0L);
    }

    public static Integer NVL(Integer number) {
        return NVL(number, 0);
    }

    public static String formatNumber(Long d) {
        if (d == null) {
            return "";
        } else {
            DecimalFormat format = new DecimalFormat("###,###");
            return format.format(d);
        }
    }

    public static String formatNumber(Integer d) {
        if (d == null) {
            return "";
        } else {
            DecimalFormat format = new DecimalFormat("###,###");
            return format.format(d);
        }
    }

    /**
     * Format so.
     *
     * @param d       So
     * @param pattern
     * @return Xau
     */
    public static String formatNumber(Object d, String pattern) {
        if (d == null) {
            return "";
        } else {
            DecimalFormat format = new DecimalFormat(pattern);
            return format.format(d);
        }
    }

    /**
     * Format so.
     *
     * @param d So
     * @return Xau
     */
    public static String formatNumber(Double d) {
        if (d == null) {
            return null;
        } else {
            DecimalFormat format = new DecimalFormat("###,###.#####");
            return format.format(d);
        }
    }

    /**
     * Chuyen doi tuong Date thanh doi tuong String.
     *
     * @param date Doi tuong Date
     * @return Xau ngay, co dang dd/MM/yyyy
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        } else {
            return new SimpleDateFormat(DEFAULT_FORMAT_DATE).format(date);
        }
    }

    public static void main(String[] args) {
        List<Integer> dataList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dataList.add(i);
        }

        List<List<Integer>> subList = partition(dataList, 3);
        System.out.println(subList);
    }
}
