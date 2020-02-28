package works.hop.cqrs.search.command;

public class SearchCommands {

    public static class SearchItemsCommand {

        public String name;
        public String model;
        public String description;
        public String supplier;

        public SearchItemsCommand(String name, String model, String description, String supplier) {
            this.name = name;
            this.model = model;
            this.description = description;
            this.supplier = supplier;
        }
    }

    public static class GetItemDetailsCommand {

        public String itemId;

        public GetItemDetailsCommand(String itemId) {
            this.itemId = itemId;
        }
    }
}
