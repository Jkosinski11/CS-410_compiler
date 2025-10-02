import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

//TODO: add all states

enum State {
    q0(null), //start
    q1("VARIABLE_NAME"),
    q2("INTEGER_LITERAL"),
    q3(null),
    q4(null),;

    public final String token;
    State(String token) {
        this.token = token;
    }
}


enum InputAlphabet {
    IDENTIFIER,
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
    ADD,
    SUB,
    MUL,
    DIV,
    POINT,
    EOL,
    INVALID
}

public class Main {
    static StringBuilder number = new StringBuilder();
    static StringBuilder identifier = new StringBuilder();

    static int nextChar;

    static void getNext(FileReader fr) throws IOException {
        nextChar = fr.read();
        if (nextChar == -1) {
            System.out.println("End of file");
            System.exit(0);
        }
    }

    static InputAlphabet analyzeInput(FileReader fr) throws IOException {

        getNext(fr);

        char current = (char)nextChar;
        switch (current) {
            case '(' -> {return InputAlphabet.LEFT_PAREN;}
            case ')' -> {return InputAlphabet.RIGHT_PAREN;}
            case '+' -> {return InputAlphabet.ADD;}
            case '-' -> {return InputAlphabet.SUB;}
            case '*' -> {return InputAlphabet.MUL;}
            case '/' -> {return InputAlphabet.DIV;}
            case '>' -> {return InputAlphabet.GREATER_THAN;}
            case '<' -> {return InputAlphabet.LESS_THAN;}
            case '=' -> {return InputAlphabet.ASSIGN;}
            case '.' -> {return InputAlphabet.POINT;}
            case ' ', '\t' -> {return InputAlphabet.WHITESPACE;}
            case '\n' -> {return InputAlphabet.EOL;} // if this gets run on linux for whatever reason
            case '\r' -> {
                fr.read(); //consume newline character
                return InputAlphabet.EOL;
            }
            case '_' -> {
                if (identifier.isEmpty()) {
                    return InputAlphabet.INVALID;
                } else  {
                    identifier.append(current);
                    return InputAlphabet.IDENTIFIER;
                }
            }
            default -> {}
        }
        if (Character.isDigit(current)) {
            if (identifier.isEmpty()) {
                number.append(current);
                return InputAlphabet.INTEGER_LITERAL;
            } else {
                identifier.append(current);
                return InputAlphabet.IDENTIFIER;
            }
        } else if (Character.isLetter(current)) {
            identifier.append(current);
            return InputAlphabet.IDENTIFIER;
        } else {
            return InputAlphabet.INVALID;
        }
    }

    List<State>[][] transitions = new List[State.values().length][InputAlphabet.values().length];
    // positions in the table can be accessed with enum ordinals
    // a warning may be thrown when accessing ordinal 0 don't worry about it
    State testState = State.q1;
    int ordinal = testState.ordinal();
    InputAlphabet testInput = InputAlphabet.ADD;
    int ordinal2 = testInput.ordinal();
    // accessing token for a given state, null if transition state
    String token = testState.token;

    /*
        TODO: create transition table proper
            MAKE SURE TO PUT STATES IN THEIR PROPER POSITIONS
            IF YOU DON'T IT WON'T WORK
            Make a factory for cells in the transition table probably
     */


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
        State currentState = State.q0;
        InputAlphabet nextInput;

        try (FileReader fr = new FileReader(file.getAbsolutePath())) {
            while(true) {
                nextInput = analyzeInput(fr);
                /*
                TODO: Process current state
                    Use ordinal of current state and ordinal of input to find next state in transition table
                    Create special case for keywords
                    Reset on whitespace/EOL and print out last token
                 */
            }
        } catch (IOException e) {
            System.out.println("File not found: " + sourceFile);
        }
    }
}
