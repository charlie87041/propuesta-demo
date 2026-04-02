package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.common.entities.ProductSource;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import jakarta.persistence.criteria.Join;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class CatalogStockController {

    private final ProductSourceRepository productSourceRepository;

    public CatalogStockController(ProductSourceRepository productSourceRepository) {
        this.productSourceRepository = productSourceRepository;
    }

    @GetMapping(value = "/admin/catalog/stock", produces = "text/html", name = "admin.catalog.stock.list")
    public String listStock(
        @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
        @ModelAttribute("searchQuery") String searchQuery,
        Model model
    ) {
        var stockPage = StringUtils.hasText(searchQuery)
            ? productSourceRepository.findAll(stockSearch(searchQuery), pageable)
            : productSourceRepository.findAll(pageable);

        model.addAttribute("stockPage", stockPage);
        return "backoffice/catalog/stock/index";
    }

    private Specification<ProductSource> stockSearch(String query) {
        return (root, cq, cb) -> {
            String normalized = query == null ? "" : query.trim().toLowerCase();
            if (normalized.isBlank()) {
                return cb.conjunction();
            }

            Join<Object, Object> product = root.join("product");
            Join<Object, Object> source = root.join("source");
            String pattern = "%" + normalized + "%";

            return cb.or(
                cb.like(cb.lower(product.get("name")), pattern),
                cb.like(cb.lower(product.get("sku")), pattern),
                cb.like(cb.lower(product.get("slug")), pattern),
                cb.like(cb.lower(source.get("name")), pattern),
                cb.like(cb.lower(source.get("code")), pattern)
            );
        };
    }
}
