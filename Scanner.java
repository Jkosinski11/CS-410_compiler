/*
 * CS 410 - Phase 1 Scanner
 * Authors: Seth McBee and Christian Williams
 * Reviewer: Jacob Kosinski
 */

/*
 * The scanner must be compiled with "javac Main.java" and then you must create a text file
 * with any name you want (and fill it with lexemes) and then add it as a command line argument such 
 * as "java Main lexemefile.txt"
 */

import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration containing all the accepting states, the start state, and an invalid / error state for tokens
 * that are not part of our dialect
 */
enum State {
    START(null), // q0, the initial state
    IN_IDENTIFIER("IDENTIFIER"), // Variable name token
    IN_INTEGER("INTEGER_LITERAL"), // Integer token
    SAW_POINT(null), // Saw a '.', will treat it as part of a float if it is followed by numbers. Otherwise considered incomplete
    IN_FLOAT("FLOAT_LITERAL"), // Reading the decimal part of a float
    SAW_LESS("<"), // Saw '<', could be '<='
    SAW_GREATER(">"), // Saw '>', could be '>='
    SAW_ASSIGN("="), // Saw '=', could be '==' for equality
    SAW_BANG(null), // Saw '!', must be followed by '=' for '!='
    LESS_EQUAL("<="), // Final state for <=
    GREATER_EQUAL(">="), // Final state for >=
    EQUAL("=="), // Final state for ==
    NOT_EQUAL("!="), // Final state for !=
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    COLON("COLON"),
    SPACE("SPACE"),
    TAB("TAB"),
    NEWLINE("NEWLINE"),
    ERROR(null); // Represents an invalid transition

    public final String token;

    State(String token) {
        this.token = token;
    }

    // Checks if the token is not null (as in it is in an accepting state)
    public boolean isAccepting() {
        return this.token != null;
    }
}

/**
 * The InputAlphabet enumeration categorizes every possible input character into groups which
 * simplifies the transition table
 */
enum InputAlphabet {
    LETTER,
    DIGIT,
    UNDERSCORE,
    POINT,
    LESS_THAN,
    GREATER_THAN,
    EQUALS,
    BANG,
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    LEFT_PAREN,
    RIGHT_PAREN,
    COLON,
    SPACE,
    TAB,
    NEWLINE,
    OTHER // Any character that is not part of our alphabet
}

public class Main {
    // State transition table in the form of a 2D array
    static State[][] transitions = new State[State.values().length][InputAlphabet.values().length];
    // Map for mapping our keywords (if, while, for, elif, else)
    static Map<String, String> keywords = new HashMap<>();

    /**
     * Initializes the state-transition table that defines our FSM.
     * This function is the code representation of the FSM diagram.
     */
    static void initializeTransitionTable() {
        // Default all transitions to ERROR
        for (int i = 0; i < State.values().length; i++) {
            for (int j = 0; j < InputAlphabet.values().length; j++) {
                transitions[i][j] = State.ERROR;
            }
        }

        // Define valid transitions from the START state
        transitions[State.START.ordinal()][InputAlphabet.LETTER.ordinal()] = State.IN_IDENTIFIER;
        transitions[State.START.ordinal()][InputAlphabet.UNDERSCORE.ordinal()] = State.IN_IDENTIFIER;
        transitions[State.START.ordinal()][InputAlphabet.DIGIT.ordinal()] = State.IN_INTEGER;
        transitions[State.START.ordinal()][InputAlphabet.LESS_THAN.ordinal()] = State.SAW_LESS;
        transitions[State.START.ordinal()][InputAlphabet.GREATER_THAN.ordinal()] = State.SAW_GREATER;
        transitions[State.START.ordinal()][InputAlphabet.EQUALS.ordinal()] = State.SAW_ASSIGN;
        transitions[State.START.ordinal()][InputAlphabet.BANG.ordinal()] = State.SAW_BANG;
        transitions[State.START.ordinal()][InputAlphabet.PLUS.ordinal()] = State.ADD;
        transitions[State.START.ordinal()][InputAlphabet.MINUS.ordinal()] = State.SUB;
        transitions[State.START.ordinal()][InputAlphabet.MULTIPLY.ordinal()] = State.MUL;
        transitions[State.START.ordinal()][InputAlphabet.DIVIDE.ordinal()] = State.DIV;
        transitions[State.START.ordinal()][InputAlphabet.LEFT_PAREN.ordinal()] = State.LEFT_PAREN;
        transitions[State.START.ordinal()][InputAlphabet.RIGHT_PAREN.ordinal()] = State.RIGHT_PAREN;
        transitions[State.START.ordinal()][InputAlphabet.COLON.ordinal()] = State.COLON;
        transitions[State.START.ordinal()][InputAlphabet.SPACE.ordinal()] = State.SPACE;
        transitions[State.START.ordinal()][InputAlphabet.TAB.ordinal()] = State.TAB;
        transitions[State.START.ordinal()][InputAlphabet.NEWLINE.ordinal()] = State.NEWLINE;


        // Transitions for building identifiers
        transitions[State.IN_IDENTIFIER.ordinal()][InputAlphabet.LETTER.ordinal()] = State.IN_IDENTIFIER;
        transitions[State.IN_IDENTIFIER.ordinal()][InputAlphabet.DIGIT.ordinal()] = State.IN_IDENTIFIER;
        transitions[State.IN_IDENTIFIER.ordinal()][InputAlphabet.UNDERSCORE.ordinal()] = State.IN_IDENTIFIER;

        // Transitions for building numbers
        transitions[State.IN_INTEGER.ordinal()][InputAlphabet.DIGIT.ordinal()] = State.IN_INTEGER;
        transitions[State.IN_INTEGER.ordinal()][InputAlphabet.POINT.ordinal()] = State.SAW_POINT;
        transitions[State.SAW_POINT.ordinal()][InputAlphabet.DIGIT.ordinal()] = State.IN_FLOAT;
        transitions[State.IN_FLOAT.ordinal()][InputAlphabet.DIGIT.ordinal()] = State.IN_FLOAT;

        // Transitions for comparison operators
        transitions[State.SAW_LESS.ordinal()][InputAlphabet.EQUALS.ordinal()] = State.LESS_EQUAL;
        transitions[State.SAW_GREATER.ordinal()][InputAlphabet.EQUALS.ordinal()] = State.GREATER_EQUAL;
        transitions[State.SAW_ASSIGN.ordinal()][InputAlphabet.EQUALS.ordinal()] = State.EQUAL;
        transitions[State.SAW_BANG.ordinal()][InputAlphabet.EQUALS.ordinal()] = State.NOT_EQUAL;
    }

    /**
     * Initializes a map of the keywords for the dialect (reserved identifiers)
     */
    static void initializeKeywords() {
        keywords.put("if", "IF_KEYWORD");
        keywords.put("elif", "ELIF_KEYWORD");
        keywords.put("else", "ELSE_KEYWORD");
        keywords.put("for", "FOR_KEYWORD");
        keywords.put("while", "WHILE_KEYWORD");
    }

    /**
     * Classifies a single character into an InputAlphabet category.
     * @param c The character to classify.
     * @return The corresponding InputAlphabet enum.
     */
    static InputAlphabet classifyChar(char c) {
        if (Character.isLetter(c)) return InputAlphabet.LETTER;
        if (Character.isDigit(c)) return InputAlphabet.DIGIT;

        return switch (c) {
            case '_' -> InputAlphabet.UNDERSCORE;
            case '.' -> InputAlphabet.POINT;
            case '<' -> InputAlphabet.LESS_THAN;
            case '>' -> InputAlphabet.GREATER_THAN;
            case '=' -> InputAlphabet.EQUALS;
            case '!' -> InputAlphabet.BANG;
            case '+' -> InputAlphabet.PLUS;
            case '-' -> InputAlphabet.MINUS;
            case '*' -> InputAlphabet.MULTIPLY;
            case '/' -> InputAlphabet.DIVIDE;
            case '(' -> InputAlphabet.LEFT_PAREN;
            case ')' -> InputAlphabet.RIGHT_PAREN;
            case ':' -> InputAlphabet.COLON;
            case ' ' -> InputAlphabet.SPACE;
            case '\t' -> InputAlphabet.TAB;
            case '\n' -> InputAlphabet.NEWLINE;
            default -> InputAlphabet.OTHER;
        };
    }

    /**
     * Processes a finalized set of lexemes and prints the corresponding token.
     * This method now contains all the logic for classifying and formatting token output.
     * @param finalState The accepting state the FSM ended in.
     * @param lexeme The character sequence that forms the token.
     */
    static void processToken(State finalState, String lexeme) {
        if (lexeme.isEmpty()) return;

        String tokenType;
        String valueToPrint;

        // Determine Token Type and Value based on the final state
        switch (finalState) {
            // Identifiers and Keywords
            case IN_IDENTIFIER:
                if (keywords.containsKey(lexeme)) {
                    tokenType = keywords.get(lexeme);
                } else {
                    tokenType = "IDENTIFIER";
                }
                valueToPrint = lexeme;
                break;

            // Numeric Literals
            case IN_INTEGER:
            case IN_FLOAT:
                tokenType = finalState.token;
                valueToPrint = lexeme;
                break;

            // Operators
            case SAW_LESS, SAW_GREATER, SAW_ASSIGN, LESS_EQUAL, GREATER_EQUAL, EQUAL, NOT_EQUAL, ADD, SUB, MUL, DIV:
                tokenType = "OPERATOR";
                valueToPrint = lexeme;
                break;

            // Colon
            case COLON:
                tokenType = "COLON";
                valueToPrint = lexeme;
                break;

            // Tab
            case TAB:
                tokenType = "TAB";
                valueToPrint = "\\t";
                break;

            // All other tokens (parentheses, space, newline)
            default:
                tokenType = finalState.token;
                valueToPrint = "N/A";
                break;
        }
        System.out.printf("Token Class: %-20s Value: %s%n", tokenType, valueToPrint);
    }

    public static void main(String[] args) {
        System.out.println("CS-410 Compiler - Scanner Phase");

        if (args.length == 0) {
            System.err.println("Proper Usage: java Main <source_file>");
            System.exit(1);
        }

        String sourceFile = args[0];
        System.out.println("Scanning: " + sourceFile);
        System.out.println("------------------------------------");

        initializeTransitionTable();
        initializeKeywords();

        try (PushbackReader pr = new PushbackReader(new FileReader(sourceFile), 10)) {
            State currentState = State.START;
            StringBuilder currentLexeme = new StringBuilder();
            int charCode;

            while ((charCode = pr.read()) != -1) {
                char c = (char) charCode;

                if (c == '\r') {
                    continue;
                }

                InputAlphabet inputType = classifyChar(c);

                State nextState = transitions[currentState.ordinal()][inputType.ordinal()];

                if (nextState == State.ERROR) {
                    if (currentState.isAccepting()) {
                        processToken(currentState, currentLexeme.toString());
                        pr.unread(c);
                    } else if (currentState == State.SAW_POINT && currentLexeme.length() > 1) {
                        pr.unread(c);
                        pr.unread(currentLexeme.charAt(currentLexeme.length() - 1));
                        currentLexeme.setLength(currentLexeme.length() - 1);
                        processToken(State.IN_INTEGER, currentLexeme.toString());
                    } else {
                        if (!currentLexeme.isEmpty()) {
                            System.err.println("Error: Invalid sequence starting with '" + currentLexeme + "' followed by '" + c + "'");
                            pr.unread(c);
                        } else {
                            System.err.println("Error: Invalid starting character '" + c + "'");
                        }
                    }
                    currentState = State.START;
                    currentLexeme.setLength(0);
                } else {
                    currentLexeme.append(c);
                    currentState = nextState;
                }
            }

            // After the file ends, process any token that was being built
            if (currentState.isAccepting() && !currentLexeme.isEmpty()) {
                processToken(currentState, currentLexeme.toString());
            } else if (!currentLexeme.isEmpty()) {
                 System.err.println("Error: Unfinished token '" + currentLexeme + "'");
            }

            System.out.println("------------------------------------");
            System.out.println("Scanning complete.");

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}