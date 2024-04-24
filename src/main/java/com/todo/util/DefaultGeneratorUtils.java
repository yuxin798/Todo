package com.todo.util;

import java.util.ArrayList;
import java.util.Random;

public class DefaultGeneratorUtils {
    //TODO默认头像
    static final ArrayList<String> defaultAvatars;
    static final ArrayList<String> defaultSignatures;
    static {
        defaultAvatars = new ArrayList<>();
        defaultAvatars.add("http://8.130.17.7:9000/todo-bucket/th.png");
        defaultAvatars.add("http://8.130.17.7:9000/todo-bucket/1.jpeg");
        defaultAvatars.add("http://8.130.17.7:9000/todo-bucket/2.jpeg");
        defaultAvatars.add("http://8.130.17.7:9000/todo-bucket/favicon.png");
    }
    static {
        defaultSignatures = new ArrayList<>();
        defaultSignatures.add("千里之行，始于足下");
        defaultSignatures.add("学而不厌，诲人不倦");
        defaultSignatures.add("学而不思则罔，思而不学则殆");
        defaultSignatures.add("不积跬步，无以至千里");
        defaultSignatures.add("不积小流，无以成江海");
    }

    /**
     * 生成随机默认头像
     * @return
     */
    public static String getRandomDefaultAvatar() {
        int size = defaultAvatars.size();
        Random random = new Random();
        int randomNumber = random.nextInt(size);
        return defaultAvatars.get(randomNumber);
    }

    /**
     * 生成随机默认个性签名
     * @return
     */
    public static String getRandomDefaultSignature() {
        int size = defaultSignatures.size();
        Random random = new Random();
        int randomNumber = random.nextInt(size);
        return defaultSignatures.get(randomNumber);
    }
}
