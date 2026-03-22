package drinkshop.service;

import drinkshop.domain.*;
import drinkshop.repository.Repository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService {

    private final Repository<Integer, Order> orderRepo;
    private final Repository<Integer, Product> productRepo;
    private final Repository<Integer, Reteta> retetaRepo;

    public OrderService(Repository<Integer, Order> orderRepo,
                        Repository<Integer, Product> productRepo,
                        Repository<Integer, Reteta> retetaRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.retetaRepo = retetaRepo;
    }

    public void addOrder(Order o, StocService stocService) throws Exception {
        Map<String, Double> requiredIngredients = new HashMap<>();

        for (OrderItem item : o.getItems()) {
            Reteta r = retetaRepo.findAll().stream()
                    .filter(ret -> ret.getId() == item.getProduct().getId())
                    .findFirst()
                    .orElse(null);

            if (r != null) {
                for (IngredientReteta ir : r.getIngrediente()) {
                    String name = ir.getDenumire();
                    double amount = ir.getCantitate() * item.getQuantity();
                    requiredIngredients.put(name, requiredIngredients.getOrDefault(name, 0.0) + amount);
                }
            }
        }


        List<Stoc> allStocks = stocService.getAll();
        for (Map.Entry<String, Double> entry : requiredIngredients.entrySet()) {
            String numeIng = entry.getKey();
            double necesarTotal = entry.getValue();

            double disponibil = allStocks.stream()
                    .filter(s -> s.getIngredient().equalsIgnoreCase(numeIng))
                    .mapToDouble(Stoc::getCantitate)
                    .sum();

            if (disponibil < necesarTotal) {
                throw new Exception("Stoc insuficient pentru: " + numeIng);
            }
        }

        for (Map.Entry<String, Double> entry : requiredIngredients.entrySet()) {
            String numeIng = entry.getKey();
            double ramasDeScazut = entry.getValue();

            for (Stoc s : allStocks) {
                if (ramasDeScazut <= 0) break;
                if (s.getIngredient().equalsIgnoreCase(numeIng)) {
                    double deLuat = Math.min(s.getCantitate(), ramasDeScazut);
                    s.setCantitate((int)(s.getCantitate() - deLuat));
                    ramasDeScazut -= deLuat;

                    stocService.update(s);
                }
            }
        }

        orderRepo.save(o);
    }

    public void updateOrder(Order o) {
        orderRepo.update(o);
    }

    public void deleteOrder(int id) {
        orderRepo.delete(id);
    }

    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    public Order findById(int id) {
        return orderRepo.findOne(id);
    }

    public double computeTotal(Order o) {
        return o.getItems().stream()
                .mapToDouble(i -> productRepo.findOne(i.getProduct().getId()).getPret() * i.getQuantity())
                .sum(); // [cite: 36]
    }

    public void addItem(Order o, OrderItem item) {
        o.getItems().add(item);
        orderRepo.update(o);
    }

    public void removeItem(Order o, OrderItem item) {
        o.getItems().remove(item);
        orderRepo.update(o);
    }
}