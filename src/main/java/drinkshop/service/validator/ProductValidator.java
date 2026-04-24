package drinkshop.service.validator;

import drinkshop.domain.Product;
import drinkshop.domain.Reteta;
import drinkshop.repository.Repository;

public class ProductValidator implements Validator<Product> {
    private final Repository<Integer, Product> productRepo;
    private final Repository<Integer, Reteta> retetaRepo;

    public ProductValidator(Repository<Integer, Product> productRepo, Repository<Integer, Reteta> retetaRepo) {
        this.productRepo = productRepo;
        this.retetaRepo = retetaRepo;
    }

    @Override
    public void validate(Product product) {
        if (product == null) {
            throw new ValidationException("Produsul nu poate fi null");
        }

        if (product.getNume() == null || product.getNume().isBlank()) {
            throw new ValidationException("Numele trebuie sa aiba cel putin 1 caracter");
        }

        if (product.getNume().length() > 255) {
            throw new ValidationException("Numele nu poate depasi 255 de caractere");
        }

        if (product.getPret() <= 0) {
            throw new ValidationException("Pretul trebuie sa fie pozitiv");
        }

        if (product.getReteta() == null) {
            throw new ValidationException("Reteta este obligatorie");
        }

        Integer recipeId = product.getReteta().getId();
        if (recipeId == null) {
            throw new ValidationException("Reteta invalida");
        }
        if(retetaRepo.findOne(recipeId) == null)
            throw new ValidationException("Reteta nu exista");

        boolean recipeAlreadyUsed = productRepo.findAll().stream()
                .filter(prod -> prod.getReteta() != null)
                .anyMatch(prod -> prod.getReteta().getId() == recipeId && prod.getId() != product.getId());

        if (recipeAlreadyUsed) {
            throw new ValidationException("Reteta este deja folosita");
        }
    }
}
