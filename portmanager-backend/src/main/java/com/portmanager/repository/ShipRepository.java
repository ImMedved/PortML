package com.portmanager.repository;

import com.portmanager.entity.ShipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipRepository extends JpaRepository<ShipEntity, String> {}
