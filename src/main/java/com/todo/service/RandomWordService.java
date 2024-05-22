package com.todo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.todo.entity.RandomWord;
import com.todo.vo.Result;

public interface RandomWordService extends IService<RandomWord> {
    Result<String> insertRandomWord(Integer times, Integer length);

    Result<String> getRandomWord();

}
