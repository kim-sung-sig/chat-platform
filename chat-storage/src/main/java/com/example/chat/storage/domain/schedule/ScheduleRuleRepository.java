package com.example.chat.storage.domain.schedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ScheduleRule Repository 인터페이스
 */
public interface ScheduleRuleRepository {

    /**
     * 스케줄 저장
     */
    ScheduleRule save(ScheduleRule scheduleRule);

    /**
     * ID로 조회
     */
    Optional<ScheduleRule> findById(Long scheduleId);

    /**
     * ID로 조회 (락 획득)
     */
    Optional<ScheduleRule> findByIdWithLock(Long scheduleId);

    /**
     * 실행 대기 중인 스케줄 목록 조회
     */
    List<ScheduleRule> findExecutableSchedules(LocalDateTime currentTime);

    /**
     * 사용자의 활성 스케줄 목록 조회
     */
    List<ScheduleRule> findActiveBySenderId(Long senderId);

    /**
     * 채팅방의 활성 스케줄 목록 조회
     */
    List<ScheduleRule> findActiveByRoomId(String roomId);

    /**
     * 스케줄 삭제
     */
    void delete(Long scheduleId);

    /**
     * 스케줄 존재 여부 확인
     */
    boolean existsById(Long scheduleId);
}
