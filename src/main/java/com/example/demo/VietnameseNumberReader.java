package com.example.demo;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.WordUtils;

public class VietnameseNumberReader {
    public String readNumberInVietnamese(int number) {
        String numberInVietnamese = WordUtils.capitalize(WordUtils.uncapitalize(String.valueOf(number)));
//        NumberUtils.
        return numberInVietnamese;
    }

    public static void main(String[] args) {
        VietnameseNumberReader reader = new VietnameseNumberReader();
        int number = 123456; // Thay đổi số cần đọc ở đây
        String numberInVietnamese = reader.readNumberInVietnamese(number);
        System.out.println("Số " + number + " được đọc là: " + numberInVietnamese);


    }
}
