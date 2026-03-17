package com.spoons.popparazzi.popup.repository;

import com.spoons.popparazzi.popup.entity.PopupViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

// 조회 이력 저장용
public interface PopupViewHistoryRepository extends JpaRepository<PopupViewHistory, Long> {
}