package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.Price;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    Optional<Price> findFirstByProductIdAndSourceIdIsNullAndCurrencyAndValidToIsNull(
        Long productId,
        String currency
    );

    Optional<Price> findFirstByProductIdAndSourceIdAndCurrencyAndValidToIsNull(
        Long productId,
        Long sourceId,
        String currency
    );

    List<Price> findByProductIdAndSourceIdIsNullAndCurrencyOrderByValidFromDesc(Long productId, String currency);

    List<Price> findByProductIdAndSourceIdAndCurrencyOrderByValidFromDesc(
        Long productId,
        Long sourceId,
        String currency
    );
}
