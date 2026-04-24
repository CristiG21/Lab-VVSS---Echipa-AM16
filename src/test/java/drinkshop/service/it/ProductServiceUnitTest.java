package drinkshop.service.it;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import drinkshop.service.ProductService;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ProductService - Mockito unit tests")
class ProductServiceUnitTest {

    private Repository<Integer, Product> productRepo;
    private ProductValidator productValidator;
    private ProductService productService;
    private Product product;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        // create mocks for repo, validator and a sample Product
        productRepo = (Repository<Integer, Product>) mock(Repository.class);
        productValidator = mock(ProductValidator.class);
        product = mock(Product.class);

        productService = new ProductService(productRepo, productValidator);
    }

    @Test
    @DisplayName("getAllProducts should return all products from repo and not call validator")
    void testGetAllProducts() {
        Product p1 = mock(Product.class);
        Product p2 = mock(Product.class);
        when(productRepo.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Product> all = productService.getAllProducts();

        assertEquals(2, all.size());
        assertTrue(all.contains(p1));
        verify(productRepo, times(1)).findAll();
        verify(productValidator, never()).validate(any());
    }

    @Test
    @DisplayName("addProduct should throw when validator fails and must not call repo.save")
    void testAddProductInvalid() {
        // arrange: validator will throw for our mocked product
        doThrow(new ValidationException("invalid")).when(productValidator).validate(product);
        when(productRepo.save(product)).thenReturn(product); // not expected to be called

        // act & assert
        assertThrows(ValidationException.class, () -> productService.addProduct(product));

        // verify interactions
        verify(productValidator, times(1)).validate(product);
        verify(productRepo, never()).save(any());
    }

    @Test
    @DisplayName("addProduct should call validator then repo.save on valid product")
    void testAddProductValid() {
        // arrange: validator does nothing (no exception)
        doNothing().when(productValidator).validate(product);
        when(productRepo.save(product)).thenReturn(product);

        // act
        assertDoesNotThrow(() -> productService.addProduct(product));

        // verify that both validator and repo.save were called
        verify(productValidator, times(1)).validate(product);
        verify(productRepo, times(1)).save(product);
    }

    @Test
    @DisplayName("findById should delegate to repo.findOne and return the product")
    void testFindById() {
        when(productRepo.findOne(1)).thenReturn(product);

        Product result = productService.findById(1);

        assertSame(product, result);
        verify(productRepo, times(1)).findOne(1);
    }

    @Test
    @DisplayName("filterByCategorie should filter returned products by categorie")
    void testFilterByCategorie() {
        Product p1 = mock(Product.class);
        Product p2 = mock(Product.class);
        Product p3 = mock(Product.class);

        when(p1.getCategorie()).thenReturn(CategorieBautura.JUICE);
        when(p2.getCategorie()).thenReturn(CategorieBautura.CLASSIC_COFFEE);
        when(p3.getCategorie()).thenReturn(CategorieBautura.JUICE);

        when(productRepo.findAll()).thenReturn(Arrays.asList(p1, p2, p3));

        List<Product> filtered = productService.filterByCategorie(CategorieBautura.JUICE);

        assertEquals(2, filtered.size());
        assertTrue(filtered.contains(p1));
        assertTrue(filtered.contains(p3));
        verify(productRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("filterByTip should filter returned products by tip")
    void testFilterByTip() {
        Product p1 = mock(Product.class);
        Product p2 = mock(Product.class);
        Product p3 = mock(Product.class);

        when(p1.getTip()).thenReturn(TipBautura.BASIC);
        when(p2.getTip()).thenReturn(TipBautura.DAIRY);
        when(p3.getTip()).thenReturn(TipBautura.BASIC);

        when(productRepo.findAll()).thenReturn(Arrays.asList(p1, p2, p3));

        List<Product> filtered = productService.filterByTip(TipBautura.BASIC);

        assertEquals(2, filtered.size());
        assertTrue(filtered.contains(p1));
        assertTrue(filtered.contains(p3));
        verify(productRepo, times(1)).findAll();
    }
}
