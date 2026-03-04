package com.spoons.popparazzi.popup.repository;

import com.spoons.popparazzi.popup.entity.Popup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopupRepository extends JpaRepository<Popup, String> {
}
