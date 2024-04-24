package com.todo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ListUtil<T> {
    public static <T> String toCommaSeparateString(List<T> list, Function<T, String> toString) {
        StringBuilder sb = new StringBuilder();
        for (T i : list) {
            sb.append(toString.apply(i));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static <T> List<T> commaSeparateStringToList(String str, Function<String, T> toObject) {
        ArrayList<T> list = new ArrayList<>();
        for (String i : str.split(",")) {
            list.add(toObject.apply(i));
        }
        return list;
    }
}
