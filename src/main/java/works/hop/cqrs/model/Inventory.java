package works.hop.cqrs.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Inventory {

    public final Map<Product, Integer> products = new ConcurrentHashMap<>();
    public final Integer min = 1, max = 100;
    public final Supplier<Integer> rand = () -> ThreadLocalRandom.current().nextInt(min, max + min);

    public Inventory(){
        init();
    }

    private void init(){
        List<Integer> quantities = Arrays.stream(new String[]{"milk", "bread", "butter", "bacon", "sausage", "coffee", "sugar"})
                .map(item -> products.put(new Product(item, 10.0f), rand.get()))
                .collect(Collectors.toList());
        quantities.stream().forEach(System.out::println);
    }

    public Integer availability(String item){
        return Optional.ofNullable(products.get(Product.get(item))).orElse(0);
    }

    public Integer fulfill(String item, Integer quantity) {
        Integer available = availability(item);
        if(available > 0) {
            Product key = Product.get(item);
            if (available >= quantity) {
                return this.products.put(key, available - quantity);
            } else {
                return this.products.put(key, 0);
            }
        }
        else{
            throw new RuntimeException("You do not have this product in stock");
        }
    }

    public void replenish(String item, Float price, Integer count){
        Integer available = availability(item);
        if(available > 0){
            this.products.put(new Product(item, price), available + count);
        }
        else{
            this.products.put(new Product(item, price), count);
        }
    }
}
