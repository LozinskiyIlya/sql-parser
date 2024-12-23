# SQL Parser

## Query Structure

To divide the SQL query into its constituent parts the following interface hierarchy is used:

![](https://mermaid.ink/img/pako:eNp1UtFqwjAU_ZVyn2tpNa01D4MxGQy2wdCnkZdrG21Zc1PShM2J_77UqqjbQh7OPfdwzuUmOyh0KYHDutGfRYXGBsu5oEeDGyXJBqPRXXDf1NjhqpGCzvDQWGhnil_sKyr5l_pBU2eRrKCT4kg3TtENuRwMhoB_mDcnzfZm1FPGc935HEGdW20MtlXwpNpG9rJOUODPKbXHR-seHj0HxaXVJSNIUgkhKGkU1qVf3q4XCLCVjxDAPSzRfAgQtPc6dFYvtlQAt8bJEIx2mwr4GpvOV64t0cp5jX5QdWZbpHetr2rgO_gCnifRLGMszWczf5NJFsIWeJIlUcrYeBKzaZL65j6E74NBHE0Zi1meZyyejJN8zEKQZW21eRle_vAB9j8QHqma?type=png "Interfaces")

The **Query** class serves as the central structure, containing all parts of the SQL query. Its fields are designed to
represent the specific 'fragment types' that can be used in different SQL clauses:

```java
public class Query implements Source {
    private List<Fragment> columns = new LinkedList<>();
    private List<Source> sources = new LinkedList<>();
    private List<Join> joins = new LinkedList<>();
    private List<Condition> filters = new LinkedList<>();
    private List<Fragment> groupings = new LinkedList<>();
    private List<Sort> sorts = new LinkedList<>();
    private Integer limit = LIMIT_ALL;
    private Integer offset = NO_OFFSET;
}
```

## Chain of Responsibility

The implementation delegates SQL query parsing to different handlers (crawlers), where each crawler handles a specific
part of the query. To achieve this, we use the following core components:

#### FragmentCrawler

Since each SQL clause can include one or more fragments (Table, Column, Query, Constant, ConstantList), 
the `abstract class FragmentCrawler` handles most of the parsing, while the subclasses simply specify
which part of the query to update when the next clause or fragment is reached.

#### CrawlContext

The `CrawlContext` keeps track of the current parsing state. It holds the current lexeme being processed, the
query, and other parsing-related data such as opened/closed brackets counter for nested cases.

#### LexemeHandler

The `@LexemeHandler` annotation is used to associate a specific crawler implementation with a particular SQL clause or
lexeme (like SELECT, JOIN, ON, etc.). This allows the system to wire up the right crawler for each part of the SQL query, based on
the lexeme currently being parsed.

With this pattern, each crawler is responsible for parsing its specific clause or lexeme. When it finishes its job,
it passes control to the next appropriate crawler, ensuring that the entire SQL query is parsed step by step, with each
handler knowing exactly which part it is responsible for.

## Key features

- **Nested Joins**. It supports nested joins, so this can be parsed without issues:

    ```sql
    SELECT *
    FROM a
    JOIN (b JOIN c ON b.id = c.id) d
    ON a.id = d.id;
    ```

- **Subqueries in columns**. The chosen algorithm naturally supports subqueries in `SELECT` clause:

    ```sql
    SELECT 
    author.name,
    (SELECT COUNT(*) FROM book WHERE book.author_id = author.id) AS book_count
    FROM author;
    ```

- **Streaming Support**. Using PushbackReader, this parser can handle streams with `parseQuery(PushbackReader reader)`.


- **Spring IOC**. Spring Context is used to streamline dependency injection. This setup is minimal, pulling only the
  necessary parts of Spring required for IoC functionality.

## Terminal Copy-Pasting

If you're using a `main` method and pasting a query, make sure to paste it with the `;` at the end to avoid any weird
parsing issues as some terminal might lag on pasting.
The full functionality is thoroughly demonstrated through the provided test suite of 180+ tests.