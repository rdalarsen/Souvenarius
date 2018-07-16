package me.worric.souvenarius.data.db;

import android.arch.persistence.room.TypeConverter;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

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
        return Arrays.asList(strings);
    }

}
