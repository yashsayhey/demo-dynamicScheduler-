package com.example.demo.service;

import com.example.demo.domain.JobConfig;
import com.example.demo.repo.JobConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

/**
 * Library to dynamically schedule jobs
 */
@Service
@RequiredArgsConstructor
public class DynamicSchedulerService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final JobConfigRepository jobConfigRepository;
    private final HashMap<String, ScheduledFuture<?>> scheduledJobs = new HashMap<>();

    /**
     * Scheduling Factory jobs during the initialisation
     */
    @PostConstruct
    public void setup() {
        List<JobConfig> jobConfigs = jobConfigRepository.findAll();
        jobConfigs.forEach(config -> {
            ScheduledFuture<?> scheduledFuture = Objects.requireNonNull(taskScheduler.schedule(
                    () -> executeJob(config),
                    new CronTrigger(config.getCron())
            ));
            scheduledJobs.put(config.getJobName(), scheduledFuture);
        });
    }

    /**
     * Updating a running job
     * @param jobConfig: Job configurations such as job name, cron expression, etc
     */
    public void updateJob(JobConfig jobConfig) {
        System.out.println("[" + LocalDateTime.now() + "] Updating job " + jobConfig.getJobName()
                + " with new cron " + jobConfig.getCron());
        if(scheduledJobs.containsKey(jobConfig.getJobName())) {
            cancelJob(jobConfig.getJobName());
            scheduleJob(jobConfig);
        }
    }

    /**
     * Cancelling a running job
     * @param jobName: Name of the job that is being cancelled
     */
    public void cancelJob(String jobName) {
        System.out.println("[" + LocalDateTime.now() + "] Cancelling job " + jobName);
        ScheduledFuture<?> future = scheduledJobs.get(jobName);
        if (future != null) {
            future.cancel(true);
            scheduledJobs.remove(jobName);
        }
    }

    /**
     * Scheduling a new job/Rescheduling the previous job with new cron
     * @param jobConfig: Job configurations such as job name, cron expression, etc
     */
    public void scheduleJob(JobConfig jobConfig) {
        System.out.println("[" + LocalDateTime.now() + "] Scheduling job " + jobConfig.getJobName()
                + " with cron " + jobConfig.getCron());
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                () -> executeJob(jobConfig),
                new CronTrigger(jobConfig.getCron()));
        scheduledJobs.put(jobConfig.getJobName(), scheduledFuture);
    }

    /**
     * Executing the scheduled job based on cron interval time
     * @param jobConfig: Job configurations such as job name, cron expression, etc
     */
    public void executeJob(JobConfig jobConfig) {
        System.out.println("[" + LocalDateTime.now() + "] Executing job " + jobConfig.getJobName()
        + " with cron " + jobConfig.getCron());
    }
}