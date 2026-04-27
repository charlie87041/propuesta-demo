package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.AdminSourceTransferIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSourceTransferIncidentRepository extends JpaRepository<AdminSourceTransferIncident, Long> {
}
