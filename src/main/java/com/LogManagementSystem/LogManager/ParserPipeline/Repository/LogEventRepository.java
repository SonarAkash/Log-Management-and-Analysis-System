package com.LogManagementSystem.LogManager.ParserPipeline.Repository;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogEventRepository extends JpaRepository<LogEvent, Integer> {
}
