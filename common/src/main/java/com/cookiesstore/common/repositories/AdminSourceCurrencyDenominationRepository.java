package com.cookiesstore.common.repositories;

import com.cookiesstore.common.entities.AdminSourceCurrencyDenomination;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSourceCurrencyDenominationRepository extends JpaRepository<AdminSourceCurrencyDenomination, Long> {

    List<AdminSourceCurrencyDenomination> findByCurrencyCodeAndActiveTrueOrderByDisplayOrderAsc(String currencyCode);

    Optional<AdminSourceCurrencyDenomination> findByIdAndCurrencyCodeAndActiveTrue(Long id, String currencyCode);
}
