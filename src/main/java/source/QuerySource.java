package source;

import lombok.Data;

import javax.management.Query;

@Data
public class QuerySource implements Source {
    private Query query;
}
