package com.diploma.backend;

import com.diploma.backend.services.ProductService;
import com.diploma.backend.model.Product;
import com.diploma.backend.repository.ProductRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private ProductService productService;

    @Test
    void listProducts_WithTitle_ReturnsFilteredList() {
        Product p1 = new Product();
        p1.setTitle("Laptop ASUS");
        when(productRepo.findByTitleContainingIgnoreCase("ASUS")).thenReturn(Arrays.asList(p1));

        List<Product> result = productService.listProducts("ASUS");

        assertEquals(1, result.size());
        assertEquals("Laptop ASUS", result.get(0).getTitle());
    }

    @Test
    void listProducts_EmptyTitle_ReturnsAllProducts() {
        when(productRepo.findAll()).thenReturn(Arrays.asList(new Product(), new Product()));
        List<Product> result = productService.listProducts("");
        assertEquals(2, result.size());
    }

    @Test
    void listProducts_NullTitle_ReturnsAllProducts() {
        when(productRepo.findAll()).thenReturn(Arrays.asList(new Product(), new Product(), new Product()));
        List<Product> result = productService.listProducts(null);
        assertEquals(3, result.size());
    }

    @Test
    void listProducts_NoProductsFound_ReturnsEmptyList() {
        when(productRepo.findByTitleContainingIgnoreCase("Unknown")).thenReturn(Collections.emptyList());
        List<Product> result = productService.listProducts("Unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    void getProductById_Found_ReturnsProduct() {
        Product p = new Product();
        p.setId(1L);
        when(productRepo.findById(1L)).thenReturn(Optional.of(p));

        Product result = productService.getProductById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getProductById_NotFound_ReturnsNull() {
        when(productRepo.findById(99L)).thenReturn(Optional.empty());
        Product result = productService.getProductById(99L);
        assertNull(result);
    }

    @Test
    void saveProduct_WithoutImages_Success() throws IOException {
        Product p = new Product();
        p.setTitle("Test Product");
        when(productRepo.save(any(Product.class))).thenReturn(p);

        productService.saveProduct(p, null, null, null);
        verify(productRepo, times(2)).save(p);
    }

    @Test
    void deleteProduct_Success() {
        doNothing().when(productRepo).deleteById(1L);
        productService.deleteProduct(1L);
        verify(productRepo, times(1)).deleteById(1L);
    }
}