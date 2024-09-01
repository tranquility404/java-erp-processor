package com.tranquility.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {

    public static String getCurrentTime() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    public static int[] getCurrentMonthAndYear() {
        LocalDate currentDate = LocalDate.now();

        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear();

        return new int[]{currentMonth, currentYear};
    }

    public static <T> String getNonNullString(T object) {
        Map<String, Object> nonNullProperties = new HashMap<>();
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true); // Allow access to private fields
            try {
                Object value = field.get(object);
                if (value != null && !value.toString().isEmpty()) {
                    nonNullProperties.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return nonNullProperties.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "='" + entry.getValue() + "'")
                .collect(Collectors.joining(", "));
    }

    public static int calculateRemainingTime(String futureDateStr) {
        Date futureDate = new Date(futureDateStr);
        Date now = new Date();
        long timeDiff = futureDate.getTime() - now.getTime();

        int hours = (int)Math.floor(timeDiff / (1000 * 60 * 60));
//        int minutes = (int)Math.floor(timeDiff / (1000 * 60)) % 60;

        return hours;
    }

    public static <T> T stringToData(T object, String str) throws IllegalAccessException {
        Map<String, String> properties = stringToMap(str);

        // Set fields on the instance
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (properties.containsKey(fieldName)) {
                String value = properties.get(fieldName);
                if (field.getType() == String.class) {
                    field.set(object, value);
                }
//                else if (field.getType() == LoginCookies.class) {
//                    field.set(object, stringToMap(value));
//                }
                // Add more type handling as needed
            }
        }
        return object;
    }

    public static Map<String, String> stringToMap(String str) {
        Map<String, String> map = new HashMap<>();
        String[] entries = str.split("', ");

        for (String entry : entries) {
            String[] keyValue = entry.split("='");
            if (keyValue.length == 2) {
                map.put(keyValue[0].trim(), keyValue[1].trim());
                System.out.println(keyValue[0].trim() + " : " + keyValue[1].trim());
            }
        }
        return map;
    }

    public static void saveFileData(byte[] data, File filePathWithName) {
        try (FileOutputStream out = new FileOutputStream(filePathWithName)) {
            out.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
