package me.worric.souvenarius.data.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import androidx.room.TypeConverter;

public class Converters {

    @TypeConverter
    public static String fromListToString(List<String> photos) {
        if (photos == null || photos.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        ListIterator<String> it = photos.listIterator();

        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(":");
            }
        }

        return sb.toString();
    }

    @TypeConverter
    public static List<String> fromStringToList(String photos) {
        String[] strings = photos.split(":");
        List<String> photoNames = new ArrayList<>(strings.length);
        if (Collections.addAll(photoNames, strings)) {
            return photoNames;
        }
        throw new RuntimeException("Could not convert photo name strings in the db to List");
    }

}
