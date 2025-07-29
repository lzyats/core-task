package com.platform.modules.quartz.config;

import com.platform.common.enums.YesOrNoEnum;
import com.platform.modules.quartz.domain.QuartzJob;
import com.platform.modules.quartz.factory.QuartzJobFactory;
import com.platform.modules.quartz.service.QuartzJobService;
import com.platform.modules.quartz.utils.ScheduleUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 定时任务配置
 */
@Configuration
public class ScheduleConfig {

    @Autowired
    private QuartzJobFactory quartzJobFactory;

    @Resource
    @Lazy
    private QuartzJobService quartzJobService;

    @Resource
    @Lazy
    private Scheduler scheduler;

    @Value("${platform.task:Y}")
    private String jobTask;

    /**
     * 项目启动时，初始化定时器 主要是防止手动修改数据库导致未同步到定时任务处理（注：不能手动修改数据库ID和任务组名，否则会导致脏数据）
     */
    public void init() throws SchedulerException {
        if (!YesOrNoEnum.YES.getCode().equals(jobTask)) {
            return;
        }
        scheduler.clear();
        List<QuartzJob> jobList = quartzJobService.queryList(new QuartzJob());
        for (QuartzJob quartzJob : jobList) {
            ScheduleUtils.createScheduleJob(scheduler, quartzJob);
        }
    }

    @Bean
    @ConditionalOnProperty(name = "spring.shardingsphere.props.dataSource", havingValue = "shardingDataSource", matchIfMissing = true)
    public SchedulerFactoryBean schedulerFactoryBean1(@Qualifier("shardingDataSource") DataSource dataSource) {
        return doFactory(dataSource);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.shardingsphere.props.dataSource", havingValue = "masterSlaveDataSource")
    public SchedulerFactoryBean schedulerFactoryBean2(@Qualifier("masterSlaveDataSource") DataSource dataSource) {
        return doFactory(dataSource);
    }

    // 执行
    private SchedulerFactoryBean doFactory(DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJobFactory(quartzJobFactory);
        // quartz参数
        Properties prop = new Properties();
        prop.put("org.quartz.scheduler.instanceName", "AppScheduler");
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        // 线程池配置
        prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        prop.put("org.quartz.threadPool.threadCount", "20");
        prop.put("org.quartz.threadPool.threadPriority", "5");
        // JobStore配置
        prop.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        // 集群配置
        prop.put("org.quartz.jobStore.isClustered", "true");
        prop.put("org.quartz.jobStore.clusterCheckinInterval", "15000");
        prop.put("org.quartz.jobStore.maxMisfiresToHandleAtATime", "1");
        prop.put("org.quartz.jobStore.txIsolationLevelSerializable", "true");
        prop.put("org.quartz.jobStore.misfireThreshold", "12000");
        prop.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
        factory.setQuartzProperties(prop);

        factory.setSchedulerName("AppScheduler");
        // 延时启动
        factory.setStartupDelay(1);
        factory.setApplicationContextSchedulerContextKey("applicationContextKey");
        // 可选，QuartzScheduler
        // 启动时更新己存在的Job，这样就不用每次修改targetObject后删除qrtz_job_details表对应记录了
        factory.setOverwriteExistingJobs(YesOrNoEnum.YES.getCode().equals(jobTask));
        // 设置自动启动，默认为true
        factory.setAutoStartup(YesOrNoEnum.YES.getCode().equals(jobTask));
        return factory;
    }

    @Bean("taskExecutor") // bean的名称，默认为首字母小写的方法名
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);// 核心线程数（默认线程数）
        executor.setMaxPoolSize(500);// 最大线程数
        executor.setQueueCapacity(500); // 缓冲队列数
        executor.setKeepAliveSeconds(10);// 允许线程空闲时间（单位：默认为秒）
        executor.setThreadNamePrefix("Async-Service-");// 线程池名前缀
        /**
         * 线程池对拒绝任务的处理策略:
         * 1. CallerRunsPolicy ：这个策略重试添加当前的任务，他会自动重复调用 execute() 方法，直到成功。
         *
         * 2. AbortPolicy ：对拒绝任务抛弃处理，并且抛出异常。
         *
         * 3. DiscardPolicy ：对拒绝任务直接无声抛弃，没有异常信息。
         *
         * 4. DiscardOldestPolicy ：对拒绝任务不抛弃，而是抛弃队列里面等待最久的一个线程，然后把拒绝任务加到队列。
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        // 初始化
        executor.initialize();
        return executor;
    }

    /**
     * 任务调度器，解决和WebSocket冲突
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(50);
        scheduler.initialize();
        return scheduler;
    }

}
