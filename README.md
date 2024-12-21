# SQL Query Parser

## Query Structure

## Chain of Responsibility

The implementation delegates SQL query parsing to different handlers (crawlers), where each crawler handles a specific part of the query.

#### Crawler
This serves as the base handler in the chain. Each crawler is responsible for parsing a specific part of the SQL query (e.g., SELECT, FROM, JOIN). After doing its part, it delegates the responsibility to the next crawler in the chain.

#### CrawlContext
The CrawlContext keeps track of the current state of the parsing. It holds the current lexeme being processed, the query, and other parsing-related data such as opened/closed brackets for nested cases

#### LexemeHandler
The @LexemeHandler is used to associate a specific crawler implementation with a particular SQL clause or lexeme (like SELECT, JOIN, ON, etc.). This allows the system to wire up the right crawler for each part of the SQL query, based on the lexeme currently being parsed.

With this architecture, each crawler is responsible for parsing its specific clause or lexeme. When it finishes its job, it passes control to the next appropriate crawler, ensuring that the entire SQL query is parsed step by step, with each handler knowing exactly which part it is responsible for.

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

- **Stream Support**. Using PushbackReader, this parser can handle streams with `parseQuery(PushbackReader reader)`. 


- **Spring IOC**. Spring Context is used to streamline dependency injection. This setup is minimal, pulling only the necessary parts of Spring required for IoC functionality.


## Terminal Copy-Pasting

If you're using a `main` method and pasting a query, make sure to paste it with the `;` at the end to avoid any weird parsing issues as some terminal might lag on pasting.
The full functionality is thoroughly demonstrated through the provided tests