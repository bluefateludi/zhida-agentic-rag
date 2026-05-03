package com.zhida.aiagent.repository;

import com.zhida.aiagent.model.entity.RagTraceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RagTraceLogRepository extends JpaRepository<RagTraceLog, String> {

    List<RagTraceLog> findTop20ByOrderByCreatedAtDesc();
}
