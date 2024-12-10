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
     * Scheduling Factory jobs during the initialization
     */
    @PostConstruct
    public void setup() {
        try {
            List<JobConfig> jobConfigs = jobConfigRepository.findAll();
            jobConfigs.forEach(config -> {
                try {
                    ScheduledFuture<?> scheduledFuture = Objects.requireNonNull(taskScheduler.schedule(
                            () -> executeJob(config),
                            new CronTrigger(config.getCron())
                    ));
                    scheduledJobs.put(config.getJobName(), scheduledFuture);
                } catch (IllegalArgumentException e) {
                    System.err.println("[" + LocalDateTime.now() + "] Invalid cron expression for job: "
                            + config.getJobName() + " - " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("[" + LocalDateTime.now() + "] Error scheduling job: "
                            + config.getJobName() + " - " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now() + "] Error initializing scheduler: " + e.getMessage());
        }
    }

    /**
     * Updating a running job
     * @param jobConfig: Job configurations such as job name, cron expression, etc
     */
    public void updateJob(JobConfig jobConfig) {
        System.out.println("[" + LocalDateTime.now() + "] Updating job " + jobConfig.getJobName()
                + " with new cron " + jobConfig.getCron());
        try {
            if (scheduledJobs.containsKey(jobConfig.getJobName())) {
                cancelJob(jobConfig.getJobName());
            }
            scheduleJob(jobConfig);
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now() + "] Error updating job: "
                    + jobConfig.getJobName() + " - " + e.getMessage());
        }
    }

    /**
     * Cancelling a running job
     * @param jobName: Name of the job that is being cancelled
     */
    public void cancelJob(String jobName) {
        System.out.println("[" + LocalDateTime.now() + "] Cancelling job " + jobName);
        try {
            ScheduledFuture<?> future = scheduledJobs.get(jobName);
            if (future != null) {
                future.cancel(true);
                scheduledJobs.remove(jobName);
            } else {
                System.err.println("[" + LocalDateTime.now() + "] Job not found: " + jobName);
            }
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now() + "] Error cancelling job: "
                    + jobName + " - " + e.getMessage());
        }
    }

    /**
     * Scheduling a new job/Rescheduling the previous job with new cron
     * @param jobConfig: Job configurations such as job name, cron expression, etc
     */
    public void scheduleJob(JobConfig jobConfig) {
        System.out.println("[" + LocalDateTime.now() + "] Scheduling job " + jobConfig.getJobName()
                + " with cron " + jobConfig.getCron());
        try {
            ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                    () -> executeJob(jobConfig),
                    new CronTrigger(jobConfig.getCron()));
            scheduledJobs.put(jobConfig.getJobName(), scheduledFuture);
        } catch (IllegalArgumentException e) {
            System.err.println("[" + LocalDateTime.now() + "] Invalid cron expression for job: "
                    + jobConfig.getJobName() + " - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now() + "] Error scheduling job: "
                    + jobConfig.getJobName() + " - " + e.getMessage());
        }
    }

    /**
     * Executing the scheduled job based on cron interval time
     * @param jobConfig: Job configurations such as job name, cron expression, etc
     */
    public void executeJob(JobConfig jobConfig) {
        try {
            System.out.println("[" + LocalDateTime.now() + "] Executing job " + jobConfig.getJobName()
                    + " with cron " + jobConfig.getCron());
            // Add actual job logic here.
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now() + "] Error executing job: "
                    + jobConfig.getJobName() + " - " + e.getMessage());
        }
    }
}
