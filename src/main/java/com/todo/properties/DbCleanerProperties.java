package com.todo.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties("todo.task.db.cleaner")
public class DbCleanerProperties {
    private String cronSchedule;
    private Integer daysOfDeleted;
    private List<String> tableNames;
}
