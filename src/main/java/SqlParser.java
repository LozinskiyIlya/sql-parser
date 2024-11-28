import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SqlParser {

    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        System.out.println("Type next query");
        String lex;
        while ((lex = nextLex()) != null) {
            System.out.println("next lex = " + lex);
        }
        System.out.println();
    }

    private static String nextLex() {
        int character;
        try {
            final var lex = new StringBuilder();
            while ((character = reader.read()) != -1) {
                char c = (char) character;
                if (Character.isWhitespace(c)) {
                    if (lex.isEmpty()) {
                        continue;
                    }
                    return lex.toString();
                }
                lex.append(c);
            }
        } catch (IOException e) {
            System.out.println("Error reading input");
            e.printStackTrace();
        }
        return null;
    }


    private static Query parse() {
        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("Error reading input");
            e.printStackTrace();
        }
        return null;
    }
}
