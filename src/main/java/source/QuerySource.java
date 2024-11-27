package source;

import com.starter.common.Query;
import lombok.Data;

@Data
public class QuerySource implements Source {
    private Query query;
}
