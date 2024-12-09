package com.example.demo.repo;

import com.example.demo.domain.JobConfig;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobConfigRepository extends JpaRepository<JobConfig, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE JobConfig jc SET jc.cron = :cron WHERE jc.jobName = :name")
    void updateCronForJob(
            @Param("name") String name,
            @Param("cron") String cron);
}
