package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.PackageOptionType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageOptionTypeRepository extends JpaRepository<PackageOptionType, Long> {
    Optional<PackageOptionType> findByCode(String code);
}
