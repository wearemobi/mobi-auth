package com.wearemobi.auth.repository;


import com.wearemobi.auth.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<ClientEntity, UUID> {
    Optional<ClientEntity> findByClientId(String clientId);
}