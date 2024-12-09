package com.example.demo.controller;

import com.example.demo.domain.JobConfig;
import com.example.demo.dto.ScheduleRequestDto;
import com.example.demo.repo.JobConfigRepository;
import com.example.demo.service.DynamicSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/scheduler")
public class DynamicSchedulerController {
    private final DynamicSchedulerService dynamicSchedulerService;
    private final JobConfigRepository jobConfigRepository;

    @PostMapping("/add-job")
    public String addJob(@RequestBody ScheduleRequestDto scheduleRequestDto) {
        dynamicSchedulerService.scheduleJob(JobConfig
                .builder()
                        .jobName(scheduleRequestDto.getJobName())
                        .cron(scheduleRequestDto.getCronExpression())
                .build());
        return "Added job "
                + scheduleRequestDto.getJobName()
                + " with interval "
                + scheduleRequestDto.getCronExpression()
                + "successfully";
    }

    @PostMapping("/update-job")
    public String updateJob(@RequestBody ScheduleRequestDto scheduleRequestDto) {
        jobConfigRepository.updateCronForJob(
                scheduleRequestDto.getJobName(),
                scheduleRequestDto.getCronExpression());
        System.out.println("DB Updated");

        dynamicSchedulerService.updateJob(JobConfig
                .builder()
                .jobName(scheduleRequestDto.getJobName())
                .cron(scheduleRequestDto.getCronExpression())
                .build());
        return "Updated job "
                + scheduleRequestDto.getJobName()
                + " with interval "
                + scheduleRequestDto.getCronExpression()
                + "successfully";
    }

    @PostMapping("/cancel-job/{jobName}")
    public String cancelJob(@PathVariable("jobName") String jobName) {
        dynamicSchedulerService.cancelJob(jobName);
        return "Cancelled job " + jobName + "successfully";
    }
}