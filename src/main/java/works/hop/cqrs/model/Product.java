package works.hop.cqrs.model;

import java.util.Objects;

public class Product implements Comparable<Product>{

    public final String name;
    public final Float price;

    public Product(String name) {
        this(name, 0.0f);
    }

    public Product(String name, Float price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return name.equals(product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Product o) {
        if (this == o) return 0;
        if(this.price > o.price) return 1;
        if(this.price < o.price) return -1;
        return this.name.compareTo(o.name);
    }

    public static Product get(String name){
        return new Product(name);
    }
}
