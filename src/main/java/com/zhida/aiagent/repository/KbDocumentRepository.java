package com.zhida.aiagent.repository;

import com.zhida.aiagent.model.entity.KbDocument;
import com.zhida.aiagent.model.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KbDocumentRepository extends JpaRepository<KbDocument, Long> {

    List<KbDocument> findByCategoryOrderByCreatedAtDesc(String category);

    List<KbDocument> findAllByOrderByCreatedAtDesc();

    List<KbDocument> findByStatus(DocumentStatus status);

    long countByStatus(DocumentStatus status);
}
