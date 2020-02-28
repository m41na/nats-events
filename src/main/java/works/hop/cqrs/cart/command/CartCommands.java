package works.hop.cqrs.cart.command;

public class CartCommands {

    public static class AddToCardCommand {

        public String itemId;

        public AddToCardCommand(String itemId) {
            this.itemId = itemId;
        }
    }

    public static class DropFromCardCommand {

        public String itemId;

        public DropFromCardCommand(String itemId) {
            this.itemId = itemId;
        }
    }
}
