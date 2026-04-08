package com.cookiesstore.admin.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cookiesstore.admin.config.ProductSearchProperties;
import com.cookiesstore.admin.web.controllers.ProductsController;
import com.cookiesstore.admin.web.dto.products.CreateProductForm;
import com.cookiesstore.admin.web.dto.products.UpdateProductForm;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.services.products.ProductService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class ProductsControllerBindingTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductSourceRepository productSourceRepository;

    @Mock
    private ProductService<CreateProductForm, UpdateProductForm> productService;

    @Mock
    private MessageSource messageSource;

    private MockMvc mockMvc;
    private ProductsController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ProductSearchProperties productSearchProperties = new ProductSearchProperties();
        controller = new ProductsController(
            productRepository,
            productSourceRepository,
            productService,
            productSearchProperties,
            messageSource
        );

        when(messageSource.getMessage(any(String.class), any(Object[].class), any(Locale.class)))
            .thenReturn("ok");

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createProductPayloadWithEmptySourcePricesCreatesProduct() throws Exception {
        Product created = new Product();
        when(productService.createProduct(any(CreateProductForm.class))).thenReturn(created);

        mockMvc.perform(post("/admin/products")
                .param("sku", "final")
                .param("name", "final")
                .param("slug", "final")
                .param("categoryId", "2")
                .param("productTypeCode", "SIMPLE")
                .param("isListable", "true")
                .param("isPurchasable", "true")
                .param("isPurchasableAlone", "true")
                .param("description", "sadfsd")
                .param("templateValues[occasion]", "ocassional")
                .param("templateValues[servings]", "12")
                .param("templateValues[flavor]", "chocolate")
                .param("templateValues[message_on_cake]", "Happy birthday")
                .param("templateValues[delivery_date]", "2026-04-07")
                .param("mainImageUrl", "")
                .param("price", "12")
                .param("stockQuantity", "0")
                .param("lowStockThreshold", "20")
                .param("sourcePrices[2]", "")
                .param("sourceStockQuantities[2]", "0")
                .param("sourceLowStockThresholds[2]", "20")
                .param("sourcePrices[1]", "")
                .param("sourceStockQuantities[1]", "0")
                .param("sourceLowStockThresholds[1]", "20")
                .param("active", "on")
                .param("visible", "on"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/products"));

        verify(productService).createProduct(any(CreateProductForm.class));
    }

    @Test
    void createProductDirectlyCallsServiceWhenFormIsValid() {
        Product created = new Product();
        when(productService.createProduct(any(CreateProductForm.class))).thenReturn(created);
        Map<String, String> templateValues = new HashMap<>();
        templateValues.put("occasion", "ocassional");
        templateValues.put("servings", "12");
        templateValues.put("flavor", "chocolate");
        templateValues.put("message_on_cake", "Happy birthday");
        templateValues.put("delivery_date", "2026-04-07");

        CreateProductForm form = new CreateProductForm(
            "final",
            "final",
            "final",
            "sadfsd",
            2L,
            "",
            true,
            true,
            List.of(),
            0,
            20,
            new BigDecimal("12"),
            "SIMPLE",
            List.of(),
            List.of(),
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>(),
            templateValues,
            true,
            true,
            true,
            null,
            List.of()
        );

        var bindingResult = new BeanPropertyBindingResult(form, "form");
        var redirectAttributes = new RedirectAttributesModelMap();

        String viewName = controller.createProduct(form, bindingResult, redirectAttributes);
        assertEquals("redirect:/admin/products", viewName);

        ArgumentCaptor<CreateProductForm> captor = ArgumentCaptor.forClass(CreateProductForm.class);
        verify(productService).createProduct(captor.capture());

        CreateProductForm capturedForm = captor.getValue();
        assertNotNull(capturedForm);
        assertEquals("final", capturedForm.sku());
        assertEquals("final", capturedForm.name());
        assertEquals("final", capturedForm.slug());
        assertEquals(2L, capturedForm.categoryId());
        assertEquals("SIMPLE", capturedForm.productTypeCode());
        assertTrue(capturedForm.isListable());
        assertTrue(capturedForm.isPurchasable());
        assertTrue(capturedForm.isPurchasableAlone());
        assertTrue(capturedForm.active());
        assertTrue(capturedForm.visible());
        assertEquals("ocassional", capturedForm.templateValues().get("occasion"));
        assertEquals("12", capturedForm.templateValues().get("servings"));
        assertEquals("chocolate", capturedForm.templateValues().get("flavor"));
        assertEquals("Happy birthday", capturedForm.templateValues().get("message_on_cake"));
        assertEquals("2026-04-07", capturedForm.templateValues().get("delivery_date"));
    }

    @Test
    void createProductDoesNotCallServiceWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/admin/products")
                .param("sku", "")
                .param("name", "")
                .param("slug", "")
                .param("price", "12"))
            .andExpect(status().isOk());

        verify(productService, never()).createProduct(any(CreateProductForm.class));
    }
}
