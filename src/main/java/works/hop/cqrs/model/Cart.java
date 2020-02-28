package works.hop.cqrs.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Cart {

    public final Map<Product, Integer> list = new ConcurrentHashMap<>();

    public Integer add(String item, Integer quantity){
        Product key = new Product(item);
        Integer count = Optional.ofNullable(list.get(key)).orElse(0);
        return this.list.put(key, count + quantity);
    }

    public Integer remove(String item){
        Product key = new Product(item);
        Integer quantity = Optional.ofNullable(list.get(key)).orElse(0);
        if(quantity > 0) {
            return this.list.put(key, --quantity);
        }
        else{
            throw new RuntimeException("You do not have this item in the cart");
        }
    }
}
