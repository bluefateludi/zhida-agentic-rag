package com.zhida.aiagent.model.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResearchBriefTest {

    @Test
    void shouldNormalizeNullCollectionsToImmutableEmptyLists() {
        Instant generatedAt = Instant.parse("2026-05-02T10:15:30Z");

        ResearchBrief brief = new ResearchBrief(
                "原始问题",
                "改写问题",
                null,
                null,
                null,
                null,
                generatedAt
        );

        assertThat(brief.subQuestions()).isEmpty();
        assertThat(brief.kbEvidence()).isEmpty();
        assertThat(brief.webEvidence()).isEmpty();
        assertThat(brief.informationGaps()).isEmpty();
        assertThat(brief.generatedAt()).isEqualTo(generatedAt);
        assertThrows(UnsupportedOperationException.class, () -> brief.subQuestions().add("new"));
    }

    @Test
    void shouldDefensivelyCopyEvidenceLists() {
        List<String> subQuestions = new ArrayList<>(List.of("子问题 1"));
        List<SourceReference> kbEvidence = new ArrayList<>(List.of(
                new SourceReference(1L, "知识库文档", "kb.md", "证据片段", 0.91)
        ));
        List<String> informationGaps = new ArrayList<>(List.of("缺少最新公开数据"));

        ResearchBrief brief = new ResearchBrief(
                "原始问题",
                "改写问题",
                subQuestions,
                kbEvidence,
                List.of(),
                informationGaps,
                Instant.parse("2026-05-02T10:15:30Z")
        );

        subQuestions.add("子问题 2");
        kbEvidence.clear();
        informationGaps.add("更多缺口");

        assertThat(brief.subQuestions()).containsExactly("子问题 1");
        assertThat(brief.kbEvidence()).hasSize(1);
        assertThat(brief.informationGaps()).containsExactly("缺少最新公开数据");
    }
}
