package com.todo.util;

import java.util.ArrayList;
import java.util.Random;

public class DefaultImageUtils {


    static final ArrayList<String> defaultImages;
    static {
        defaultImages = new ArrayList<>();
        defaultImages.add("http://8.130.17.7:9000/todo-bucket/th.png");
        defaultImages.add("http://8.130.17.7:9000/todo-bucket/1.jpeg");
        defaultImages.add("http://8.130.17.7:9000/todo-bucket/2.jpeg");
        defaultImages.add("http://8.130.17.7:9000/todo-bucket/favicon.png");
    }

    /**
     * 生成随机默认头像
     * @return
     */
    public static String getRandomDefaultAvatar() {
        int size = defaultImages.size();
        Random random = new Random();
        int randomNumber = random.nextInt(size);
        return defaultImages.get(randomNumber);
    }
}
