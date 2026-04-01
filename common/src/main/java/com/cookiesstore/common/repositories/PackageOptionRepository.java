package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.PackageOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageOptionRepository extends JpaRepository<PackageOption, Long> {
    List<PackageOption> findByPackageProductIdOrderBySortOrderAsc(Long packageProductId);
}
