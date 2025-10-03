/*
 * CS 410 - Phase 1 Scanner
 * Authors: Seth McBee and Christian Williams
 * Reviewer: Jacob Kosinski
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
    COLON(":"), 
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
    WHITESPACE,
    EOL,
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
        if (Character.isWhitespace(c)) return InputAlphabet.WHITESPACE;

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
            case '\n', '\r' -> InputAlphabet.EOL;
            default -> InputAlphabet.OTHER;
        };
    }

    /**
     * Processes a finalized set of lexemes and prints the corresponding token. 
     * It also checks if an identifier is actually one of our reserved keywords
     * @param finalState The accepting state the FSM ended in.
     * @param lexeme The character sequence that forms the token.
     */
    static void processToken(State finalState, String lexeme) {
        if (lexeme.isEmpty()) return;

        String tokenType = finalState.token;
        // If the token is an identifier, check if it's a keyword
        if ("IDENTIFIER".equals(tokenType) && keywords.containsKey(lexeme)) {
            tokenType = keywords.get(lexeme);
        }
        
        System.out.printf("Token Class: %-20s Value: %s%n", tokenType, lexeme); 
    }

    public static void main(String[] args) {
        System.out.println("CS-410 Compiler - Scanner Phase");

        if (args.length == 0) {
            System.err.println("Usage: java Main <source_file>");
            System.exit(1);
        }
        
        String sourceFile = args[0];
        System.out.println("Scanning: " + sourceFile);
        System.out.println("------------------------------------");

        initializeTransitionTable(); 
        initializeKeywords();

        // We use PushbackReader because we can place characters like "." back in the stream while processing
        // if needed
        try (PushbackReader pr = new PushbackReader(new FileReader(sourceFile))) { 
            State currentState = State.START;
            StringBuilder currentLexeme = new StringBuilder();
            int charCode;

            while ((charCode = pr.read()) != -1) {
                char c = (char) charCode;
                InputAlphabet inputType = classifyChar(c);

                if (inputType == InputAlphabet.WHITESPACE || inputType == InputAlphabet.EOL) {
                    if (currentState.isAccepting()) {
                        processToken(currentState, currentLexeme.toString());
                    } else if (!currentLexeme.isEmpty()) {
                        System.err.println("Error: Unfinished token '" + currentLexeme + "'");
                    }
                    currentState = State.START;
                    currentLexeme.setLength(0);
                    continue;
                }

                State nextState = transitions[currentState.ordinal()][inputType.ordinal()];

                if (nextState == State.ERROR) {
                    if (currentState.isAccepting()) {
                        processToken(currentState, currentLexeme.toString());
                        pr.unread(c);
                    } else if (currentState == State.SAW_POINT && !currentLexeme.isEmpty()) {
                        // Handle cases like "123." followed by a non-digit.
                        // Backtrack to process the integer, leaving the '.' in the stream.
                        pr.unread(c); // Push back the non-digit character.
                        pr.unread(currentLexeme.charAt(currentLexeme.length() - 1)); // Push back the '.'.
                        currentLexeme.setLength(currentLexeme.length() - 1); // Remove the '.' from the lexeme.
                        processToken(State.IN_INTEGER, currentLexeme.toString()); // Process the integer part.
                    } else {
                        // This is a true syntax error.
                        if (!currentLexeme.isEmpty()) {
                            System.err.println("Error: Invalid sequence starting with '" + currentLexeme + "' followed by '" + c + "'");
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
            } else if (!currentLexeme.isEmpty()) { // for edge case where lexeme string ends in an incomplete token
                System.err.println("Error: Unfinished token '" + currentLexeme + "'"); 
            }

            System.out.println("------------------------------------");
            System.out.println("Scanning complete.");

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}