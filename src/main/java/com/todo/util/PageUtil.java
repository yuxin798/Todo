package com.todo.util;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class PageUtil<T> {
    public static <T> Page<T> of(Page<?> page, List<T> records) {
        Page<T> p = new Page<>();
        p.setRecords(records);
        p.setTotal(page.getTotal());
        p.setSize(page.getSize());
        p.setCurrent(page.getCurrent());
        p.setPages(page.getPages());
        return p;
    }
}
