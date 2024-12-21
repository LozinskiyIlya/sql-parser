# SQL Query Parser

## Chosen Pattern and Why



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