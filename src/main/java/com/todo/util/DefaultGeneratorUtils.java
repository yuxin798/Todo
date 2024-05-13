package com.todo.util;

import java.util.ArrayList;
import java.util.Random;

public class DefaultGeneratorUtils {
    //TODO默认头像
    static final ArrayList<String> defaultAvatars;
    static final ArrayList<String> defaultSignatures;
    static final ArrayList<String> defaultBackgrounds;
    static {
        defaultAvatars = new ArrayList<>();
        for (int i = 1; i <= 19; i++) {
            defaultAvatars.add("http://8.130.17.7:9000/todo-bucket/user_default_avatar/random_avator" + i + ".png");
        }
    }
    static {
        defaultSignatures = new ArrayList<>();
        defaultSignatures.add("千里之行，始于足下");
        defaultSignatures.add("学而不厌，诲人不倦");
        defaultSignatures.add("学而不思则罔，思而不学则殆");
        defaultSignatures.add("不积跬步，无以至千里");
        defaultSignatures.add("不积小流，无以成江海");
    }

    static {
        defaultBackgrounds = new ArrayList<>();
        for (int i = 1; i <= 556; i++) {
            defaultBackgrounds.add("http://8.130.17.7:9000/todo-bucket/background/" + i + ".jpg");
        }
    }

    /**
     * 生成随机默认头像
     */
    public static String getRandomDefaultAvatar() {
        int size = defaultAvatars.size();
        Random random = new Random();
        int randomNumber = random.nextInt(size);
        return defaultAvatars.get(randomNumber);
    }

    /**
     * 生成随机默认个性签名
     */
    public static String getRandomDefaultSignature() {
        int size = defaultSignatures.size();
        Random random = new Random();
        int randomNumber = random.nextInt(size);
        return defaultSignatures.get(randomNumber);
    }

    /**
     * 生成随机任务背景图片
     */
    public static String getRandomDefaultBackground() {
        int size = defaultBackgrounds.size();
        Random random = new Random();
        int randomNumber = random.nextInt(size);
        return defaultBackgrounds.get(randomNumber);
    }
}
