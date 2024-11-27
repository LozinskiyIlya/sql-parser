import com.starter.common.clause.WhereClause;
import com.starter.common.source.Source;
import lombok.Data;

import java.util.List;


@Data
public class Query {
    private List<String> columns;
    private List<Source> fromSources;
    private List<Join> joins;
    private List<WhereClause> whereClauses;
    private List<String> groupByColumns;
    private List<Sort> sortColumns;
    private Integer limit;
    private Integer offset;
}
