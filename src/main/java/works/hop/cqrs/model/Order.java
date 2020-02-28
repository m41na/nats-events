package works.hop.cqrs.model;

import java.util.function.Function;

public class Order {

    public final Cart items;
    public final String status;

    public Order(Cart items, String status) {
        this.items = items;
        this.status = status;
    }

    public Float calcTotal(Function<Cart, Float> total){
        return total.apply(this.items);
    }
}
