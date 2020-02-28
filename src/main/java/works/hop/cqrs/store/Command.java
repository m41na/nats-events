package works.hop.cqrs.store;

public class Command<T> {

    public final String type;
    public final T data;

    public Command(String type, T data) {
        this.type = type;
        this.data = data;
    }
}
