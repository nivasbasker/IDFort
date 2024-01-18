package com.zio.idfort.utils;

import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DataExtractor {
    public static Map<String, String> adhaarReader(String input) {
        String text = input.replaceAll("\\\\r", "\r").replaceAll("\\\\n", "\n");
        String[] res = text.split("[\\s\\r\\n]+");
        String name = null;
        String dob = null;
        String adh = null;
        String sex;

        for (String x : res)
            Log.d(Constants.TAG, x);

        String[] text0;
        String[] text1 = new String[0];
        String[] lines = text.split("\n");

        for (String lin : lines) {
            String s;
            s = lin.replace("\n", "");
            s = s.trim();
            text1 = append(text1, s);
        }

        if (text.toLowerCase().contains("female")) {
            sex = "FEMALE";
        } else {
            sex = "MALE";
        }

        text1 = removeEmptyStrings(text1);
        text0 = text1.clone();

        try {
            // Cleaning first names
            name = text0[0].trim().replace("8", "B").replace("0", "D").replace("6", "G")
                    .replace("1", "I").replaceAll("[^a-zA-Z]+", " ");

            // Cleaning DOB
            dob = text0[1].substring(text0[1].length() - 10).trim().replace('l', '/')
                    .replace('L', '/').replace('I', '/').replace('i', '/')
                    .replace('|', '/').replace("\"", "/1").replace(":", "")
                    .replace(" ", "");

            // Cleaning Aadhar number details
            StringBuilder aadharNumber = new StringBuilder();
            int cnt=0;
            for (String word : res) {
                if(cnt>3)break;
                if (word.length() == 4 && word.matches("\\d+")) {
                    aadharNumber.append(word).append(' ');
                    cnt++;
                }
            }
            adh = aadharNumber.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> data = new HashMap<>();
        data.put("Name", name);
        data.put("DOB", dob);
        data.put("UID", adh);
        data.put("Sex", sex);
        return data;
    }

    private static String[] append(String[] array, String element) {
        String[] newArray = new String[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = element;
        return newArray;
    }

    private static String[] removeEmptyStrings(String[] array) {
        return Arrays.stream(array).filter(s -> !s.trim().isEmpty()).toArray(String[]::new);
    }
}
