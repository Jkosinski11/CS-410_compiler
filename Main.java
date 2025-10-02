import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.*;

enum State {
    q0(null), //start
    q1("VARIABLE_NAME"),
    q2("INTEGER_LITERAL"),
    q3(null),
    q4(null),;

    public final String token;
    State(String label) {
        this.token = label;
    }
}


enum InputAlphabet {
    VARIABLE_NAME,
    INTEGER_LITERAL,
    FLOAT_LITERAL,
    WHILE,
    IF,
    FOR,
    ASSIGN,
    WHITESPACE,
    LESS_THAN,
    GREATER_THAN,
    EQUALS,
    LESS_THAN_OR_EQUALS,
    GREATER_THAN_OR_EQUALS,
    LEFT_PAREN,
    RIGHT_PAREN,
    EL,
    ADD,
    SUB,
    MUL,
    DIV,
    POINT,
    EOL,
    EOF,
    INVALID
}

public class Main {
    static Pattern variableName =  Pattern.compile("^[a-zA-Z]\\w*");
    static Pattern matchDigit = Pattern.compile("\\d");
    static Pattern matchFloat = Pattern.compile("^\\.\\d+");

    static StringBuilder number = new StringBuilder();
    static int nextChar;

    static InputAlphabet analyzeInput(FileReader fr) throws IOException {
        StringBuilder sb = new StringBuilder();
        nextChar = fr.read();
        if (nextChar == -1) {
            return InputAlphabet.EOF;
        }
        while (nextChar != ' ' && nextChar != '\t') {
            sb.append((char) nextChar);
            //handle compound tokens and end of line
            String current = sb.toString();
            switch (current) {
                case String s when matchDigit.matcher(s).find() -> {
                    number.append(current);
                    return InputAlphabet.INTEGER_LITERAL;
                }
                case ">" -> {return InputAlphabet.GREATER_THAN;}
                case "<" -> {return InputAlphabet.LESS_THAN;}
                case "=" -> {return InputAlphabet.ASSIGN;}
                case "\r\n", "\n" -> {return InputAlphabet.EOL;}
                default -> {}
            }
            nextChar = fr.read();
        }
        String current = sb.toString();
        return switch (current) {
            case "while" -> InputAlphabet.WHILE;
            case "for" -> InputAlphabet.FOR;
            case "if" -> InputAlphabet.IF;
            case "(" -> InputAlphabet.LEFT_PAREN;
            case ")" -> InputAlphabet.RIGHT_PAREN;
            case "+" -> InputAlphabet.ADD;
            case "-" -> InputAlphabet.SUB;
            case "*" -> InputAlphabet.MUL;
            case "/" -> InputAlphabet.DIV;
            case String s when variableName.matcher(s).find() -> InputAlphabet.VARIABLE_NAME;
            default -> InputAlphabet.INVALID;
        };

    }

    // [state][value]
    List<State>[][] transitions = new List[23][23];

    public static void main(String[] args) {
        System.out.println("CS-410 Compiler");

        if (args.length == 0) {
            System.out.println("Usage: java Main <source_file>");
            System.exit(1);
        }
        
        String sourceFile = args[0];
        System.out.println("Compiling: " + sourceFile);

        File file = new File(sourceFile);

        int q_index = 0;
        int s_index = 0;

        try (FileReader fr = new FileReader(file.getAbsolutePath())) {

        } catch (IOException e) {
            System.out.println("File not found: " + sourceFile);
        }
    }
}
