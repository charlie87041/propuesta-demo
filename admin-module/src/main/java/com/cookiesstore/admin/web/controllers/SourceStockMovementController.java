package com.cookiesstore.admin.web.controllers;


import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.jpa.domain.Specification;

import com.cookiesstore.admin.config.StockMovementProperties;
import com.cookiesstore.admin.search.EntitySearchSpecifications;
import com.cookiesstore.admin.service.sources.SourceService;
import com.cookiesstore.common.entities.AdminSourceStockMovement;
import com.cookiesstore.common.repositories.AdminSourceStockMovementRepository;

@Controller
public class SourceStockMovementController
{
    private final AdminSourceStockMovementRepository stockMovementRepository;
    private final StockMovementProperties searchProperties;
    private final SourceService sourceService;


    public SourceStockMovementController(
        AdminSourceStockMovementRepository stockMovementRepository,
        StockMovementProperties searchProperties,
        SourceService sourceService
    )
    {
        this.stockMovementRepository = stockMovementRepository;
        this.searchProperties = searchProperties;
        this.sourceService = sourceService;
    }

    @GetMapping(value = "/admin/product-sources/{sourceId}/manage/movements", name = "admin.sources.manage.list")
    public String listStockMovements(
        Model model,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)  Pageable page,
        @RequestParam(value = "q", required = false) String searchQuery,
        @PathVariable("sourceId") Long sourceId
    ){
        Specification<AdminSourceStockMovement> sourceScope = (root, query, cb) ->
            cb.equal(root.get("source").get("id"), sourceId);
        Specification<AdminSourceStockMovement> spec = StringUtils.hasText(searchQuery)
            ? sourceScope.and(EntitySearchSpecifications.globalSearch(searchQuery, searchProperties.getSearchableFields()))
            : sourceScope;

        var stockPage = stockMovementRepository.findAll(spec, page);
        model.addAttribute("source", sourceService.getSource(sourceId));
        model.addAttribute("stockPage", stockPage);
        return "backoffice/product-sources/manage/movements_list.html";
    }
    
}
