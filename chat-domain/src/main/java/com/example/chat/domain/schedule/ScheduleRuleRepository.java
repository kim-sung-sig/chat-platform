package com.example.chat.domain.schedule;

import java.util.List;
import java.util.Optional;

/**
 * 스케줄 규칙 Repository 인터페이스 (포트)
 */
public interface ScheduleRuleRepository {

    /**
     * 스케줄 규칙 저장
     */
    ScheduleRule save(ScheduleRule scheduleRule);

    /**
     * ID로 스케줄 규칙 조회
     */
    Optional<ScheduleRule> findById(ScheduleId id);

    /**
     * 비관적 락으로 스케줄 규칙 조회 (동시성 제어)
     */
    Optional<ScheduleRule> findByIdWithLock(ScheduleId id);

    /**
     * 실행 가능한 단발성 스케줄 목록 조회
     */
    List<ScheduleRule> findExecutableOneTimeSchedules();

    /**
     * 활성화된 주기적 스케줄 목록 조회
     */
    List<ScheduleRule> findActiveRecurringSchedules();

    /**
     * 사용자의 활성 스케줄 목록 조회
     */
    List<ScheduleRule> findActiveBySenderId(String senderId);

    /**
     * 채널의 활성 스케줄 목록 조회
     */
    List<ScheduleRule> findActiveByChannelId(String channelId);

    /**
     * 스케줄 규칙 삭제
     */
    void delete(ScheduleId id);
}
