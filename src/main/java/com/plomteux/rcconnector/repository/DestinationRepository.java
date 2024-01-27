package com.plomteux.rcconnector.repository;

import com.plomteux.rcconnector.entity.DestinationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationRepository extends JpaRepository<DestinationCodeEntity, Long> {
}