package source;


import lombok.Data;

@Data
public class TableSource implements Source {
    private String tableName;
}
