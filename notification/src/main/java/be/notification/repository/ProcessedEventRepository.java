package be.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.notification.domain.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}
