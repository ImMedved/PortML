package com.portmanager.repository;

import com.portmanager.entity.WeatherEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherEventRepository extends JpaRepository<WeatherEventEntity, Long> {}
