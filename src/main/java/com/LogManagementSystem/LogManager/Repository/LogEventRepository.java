package com.LogManagementSystem.LogManager.Repository;

import com.LogManagementSystem.LogManager.Entity.LogEvent;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface LogEventRepository extends JpaRepository<LogEvent, UUID> {


    @Modifying
    @Transactional
    @Query("DELETE FROM LogEvent l WHERE l.ingestedAt  < :cutoffTimestamp")
    long deleteByIngestedAtBefore(Instant cutoffTimestamp);
}
