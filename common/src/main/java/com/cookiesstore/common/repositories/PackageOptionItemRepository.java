package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.PackageOptionItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageOptionItemRepository extends JpaRepository<PackageOptionItem, Long> {
    List<PackageOptionItem> findByPackageOptionIdOrderBySortOrderAsc(Long packageOptionId);
}
