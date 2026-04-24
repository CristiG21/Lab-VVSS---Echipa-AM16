package drinkshop.service.it;

import drinkshop.domain.Product;
import drinkshop.domain.Reteta;
import drinkshop.repository.Repository;
import drinkshop.service.ProductService;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Integration: ProductService <-> ProductValidator")
class ProductServiceLevel1ValidatorIntTest {

    private Repository<Integer, Product> productRepo;
    private Repository<Integer, Reteta> retetaRepo;
    private ProductValidator productValidator;
    private ProductService productService;
    private Product product;

    @BeforeEach
    void setUp() {
        productRepo = mock(Repository.class);
        retetaRepo = mock(Repository.class);
        productValidator = spy(new ProductValidator(productRepo, retetaRepo));
        productService = new ProductService(productRepo, productValidator);
        product = mock(Product.class);
    }

    @Test
    @DisplayName("addProduct - valid product should be saved and validator called")
    void addProduct_valid() {
        // arrange
        Reteta reteta = mock(Reteta.class);
        when(product.getNume()).thenReturn("OK");
        when(product.getPret()).thenReturn(1.0);
        when(product.getReteta()).thenReturn(reteta);
        when(reteta.getId()).thenReturn(101);

        when(retetaRepo.findOne(101)).thenReturn(reteta);
        when(productRepo.findAll()).thenReturn(Collections.emptyList());
        when(productRepo.save(product)).thenReturn(product);

        // act
        assertDoesNotThrow(() -> productService.addProduct(product));

        // assert/verify
        verify(productValidator, times(1)).validate(product);
        verify(productRepo, times(1)).save(product);
    }

    @Test
    @DisplayName("addProduct - null name should throw ValidationException and not call repo")
    void addProduct_nullName() {
        // arrange
        Reteta reteta = mock(Reteta.class);
        when(product.getNume()).thenReturn(null);
        when(product.getPret()).thenReturn(1.0);
        when(product.getReteta()).thenReturn(reteta);
        when(reteta.getId()).thenReturn(101);

        when(retetaRepo.findOne(101)).thenReturn(reteta);

        // act & assert
        ValidationException ex = assertThrows(ValidationException.class, () -> productService.addProduct(product));
        assertTrue(ex.getMessage().toLowerCase().contains("numele"));

        verify(productValidator, times(1)).validate(product);
        verify(productRepo, never()).save(any());
    }

    @Test
    @DisplayName("addProduct - zero price should throw ValidationException")
    void addProduct_zeroPrice() {
        // arrange
        Reteta reteta = mock(Reteta.class);
        when(product.getNume()).thenReturn("Name");
        when(product.getPret()).thenReturn(0.0);
        when(product.getReteta()).thenReturn(reteta);
        when(reteta.getId()).thenReturn(101);

        when(retetaRepo.findOne(101)).thenReturn(reteta);

        // act & assert
        assertThrows(ValidationException.class, () -> productService.addProduct(product));
        verify(productRepo, never()).save(any());
        verify(productValidator, times(1)).validate(product);
    }

    @Test
    @DisplayName("addProduct - missing recipe should throw ValidationException")
    void addProduct_missingRecipe() {
        // arrange
        when(product.getNume()).thenReturn("Name");
        when(product.getPret()).thenReturn(1.0);
        when(product.getReteta()).thenReturn(null);

        // act & assert
        assertThrows(ValidationException.class, () -> productService.addProduct(product));
        verify(productValidator, times(1)).validate(product);
        verify(productRepo, never()).save(any());
    }

    @Test
    @DisplayName("addProduct - recipe id not found should throw ValidationException")
    void addProduct_recipeNotFound() {
        // arrange
        Reteta reteta = mock(Reteta.class);
        when(product.getNume()).thenReturn("Name");
        when(product.getPret()).thenReturn(1.0);
        when(product.getReteta()).thenReturn(reteta);
        when(reteta.getId()).thenReturn(999);

        when(retetaRepo.findOne(999)).thenReturn(null);

        // act & assert
        assertThrows(ValidationException.class, () -> productService.addProduct(product));
        verify(productValidator, times(1)).validate(product);
        verify(productRepo, never()).save(any());
    }

    @Test
    @DisplayName("addProduct - recipe already used by another product should throw ValidationException")
    void addProduct_recipeAlreadyUsed() {
        // arrange
        Reteta reteta = mock(Reteta.class);
        Product existing = mock(Product.class);

        when(product.getNume()).thenReturn("Name");
        when(product.getPret()).thenReturn(1.0);
        when(product.getReteta()).thenReturn(reteta);
        when(product.getId()).thenReturn(10);
        when(reteta.getId()).thenReturn(555);

        when(existing.getReteta()).thenReturn(reteta);
        when(existing.getId()).thenReturn(11); // different id -> indicates used by another product

        when(retetaRepo.findOne(555)).thenReturn(reteta);
        when(productRepo.findAll()).thenReturn(Collections.singletonList(existing));

        // act & assert
        ValidationException ex = assertThrows(ValidationException.class, () -> productService.addProduct(product));
        assertTrue(ex.getMessage().toLowerCase().contains("reteta"));

        verify(productValidator, times(1)).validate(product);
        verify(productRepo, never()).save(any());
    }
}
