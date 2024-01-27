package com.plomteux.rcconnector.repository;

import com.plomteux.rcconnector.entity.CruiseDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CruiseDetailsRepository extends JpaRepository<CruiseDetailsEntity, Long> { }
