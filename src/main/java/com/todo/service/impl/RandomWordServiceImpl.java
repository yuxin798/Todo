package com.todo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.entity.RandomWord;
import com.todo.mapper.RandomWordMapper;
import com.todo.service.RandomWordService;
import com.todo.vo.Result;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RandomWordServiceImpl extends ServiceImpl<RandomWordMapper, RandomWord> implements RandomWordService {

    @Override
    public Result<String> insertRandomWord(Integer times, Integer length) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://uapis.cn/api/say")
                .get()
                .build();

        Set<String> words = this.list()
                .stream()
                .map(RandomWord::getWord)
                .collect(Collectors.toSet());

        Set<String> beforeWords = new HashSet<>(words);

        for (int i = 0; i < times; i++){
            Call call = client.newCall(request);
            try (Response response = call.execute()) {
                String word = response.body().string();
                if (word.length() <= length && words.add(word)){
                    RandomWord randomWord = new RandomWord();
                    randomWord.setWord(word);
                    this.save(randomWord);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // 求差集
        words.removeAll(beforeWords);
        return Result.success("新增" + words.size() + "条数据   " + words);
    }

    @Override
    public Result<String> getRandomWord() {
        List<RandomWord> list = this.list();
        Random random = new Random(Instant.now().toEpochMilli());
        return Result.success(list.get(random.nextInt(list.size())).getWord());
    }
}
