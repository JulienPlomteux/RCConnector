package com.plomteux.rcconnector.repository;

import com.plomteux.rcconnector.entity.SailingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SailingsRepository extends JpaRepository<SailingsEntity, Long>, SailingsRepositoryCustom{}