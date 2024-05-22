package com.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@TableName(value ="random_word")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RandomWord implements Serializable {
    /**
     *
     */
    @TableId(value = "word_id", type = IdType.AUTO)
    private Integer wordId;

    /**
     *
     */
    private String word;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RandomWord that = (RandomWord) o;
        return Objects.equals(wordId, that.wordId) && Objects.equals(word, that.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordId, word);
    }

    @Override
    public String toString() {
        return "RandomWord{" +
                "wordId=" + wordId +
                ", word='" + word + '\'' +
                '}';
    }
}
