package clause;

import com.starter.common.Query;
import lombok.Data;

@Data
public class QueryOperand implements Operand {
    private Query query;
}
