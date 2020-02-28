package works.hop.cqrs.search.event;

import java.util.List;

public class SearchEvents {

    public static class SearchResultsEvent<T>{

        public List<T> result;

        public SearchResultsEvent(List<T> result) {
            this.result = result;
        }
    }
}
