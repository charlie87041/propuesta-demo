package com.cookiesstore.admin.repository;

import com.cookiesstore.admin.domain.AdminUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AdminUser> findByActiveTrueOrderByCreatedAtDesc();
}
