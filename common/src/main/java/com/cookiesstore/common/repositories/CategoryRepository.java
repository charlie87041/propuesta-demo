package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    Optional<Category> findByCode(String code);

    Optional<Category> findBySlug(String slug);

    List<Category> findByActiveTrueOrderBySortOrderAscNameAsc();
}
