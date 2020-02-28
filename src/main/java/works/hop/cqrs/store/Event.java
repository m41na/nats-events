package works.hop.cqrs.store;

public class Event<T> {

    public final String type;
    public final T data;

    public Event(String type, T data) {
        this.type = type;
        this.data = data;
    }
}
