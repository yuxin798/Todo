package com.todo.config.scheduler;

import com.todo.properties.DbCleanerProperties;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.TimeZone;

/**
 * 清理数据库
 */
@Slf4j
@Configuration
public class DbCleaner {

    private final JdbcClient jdbcClient;

    private final DbCleanerProperties dbCleanerProperties;

    public DbCleaner(JdbcClient jdbcClient, DbCleanerProperties dbCleanerProperties) {
        this.jdbcClient = jdbcClient;
        this.dbCleanerProperties = dbCleanerProperties;
    }

    @Bean(name = "dbCleanerJobDetail")
    public JobDetail jobDetail() {
        return JobBuilder.newJob(DbCleanerDeletedJob.class)
                .withIdentity("DbCleanerDeletedJob", "clean")
                .storeDurably(true)
                .build();
    }

    @Bean(name = "dbCleanerTrigger")
    public Trigger trigger() {
         return TriggerBuilder.newTrigger()
                 .forJob(jobDetail())
                 .withIdentity("DbCleanerDeletedTrigger", "clean")
                 .startNow()
                 .withSchedule(
                         CronScheduleBuilder
                                 .cronSchedule(dbCleanerProperties.getCronSchedule())
                                 .inTimeZone(TimeZone.getDefault())
                 )
                 .build();
    }

    public class DbCleanerDeletedJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            String sql;
            for (String tableName : dbCleanerProperties.getTableNames()) {
                log.info("==> Cleaning table: {}", tableName);
                sql = "delete from " + tableName +
                        " where deleted = 1" +
                        " and DATEDIFF(now(), " + tableName + ".updated_at) > " + dbCleanerProperties.getDaysOfDeleted();
                log.info("==> sql: {}", sql);
                jdbcClient
                        .sql(sql)
                        .update();
                log.info("==> Cleaning table completed");
            }
        }
    }
}
