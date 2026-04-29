package com.cookiesstore.pos.repository;

import com.cookiesstore.pos.domain.PosAdminUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosAdminUserRepository extends JpaRepository<PosAdminUser, Long> {

    Optional<PosAdminUser> findByEmail(String email);
}
