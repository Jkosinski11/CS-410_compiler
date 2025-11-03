import transitionTable.*;

import java.io.*;
import java.util.ArrayList;
import java.util.function.BiFunction;

public class Main {
    static final int SPACES_IN_TAB = 4;

    static ArrayList<State> outputStates = new ArrayList<>();

    static BiFunction<State, Alphabet, State>[][] table =
            new BiFunction[StateEnum.values().length][AlphaEnum.values().length];

    static void newTransition(StateEnum newState, AlphaEnum newAlphabet, BiFunction<State, Alphabet, State> func) {
        table[newState.ordinal()][newAlphabet.ordinal()] = func;
    }

    static void newTransitions(StateEnum[] newStates, AlphaEnum newAlphabet, BiFunction<State, Alphabet, State> func) {
        for (StateEnum newState : newStates) {
            table[newState.ordinal()][newAlphabet.ordinal()] = func;
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < StateEnum.values().length; i++) {
            for (int j = 0; j < AlphaEnum.values().length; j++) {
                table[i][j] = Transitions::invalid;
            }
        }

        //fill transition table
        {
            var excludePoint = new StateEnum[]{
                    StateEnum.Start,
                    StateEnum.Newline,
                    StateEnum.Tab,
                    StateEnum.Invalid,
                    StateEnum.Keyword,
                    StateEnum.Identifier,
                    StateEnum.Float,
                    StateEnum.Integer,
                    StateEnum.Parentheses,
                    StateEnum.Operator,
                    StateEnum.Whitespace,
            };

            newTransitions(new StateEnum[]{
                            StateEnum.Start,
                            StateEnum.Newline,
                            StateEnum.Tab,
                            StateEnum.Invalid,
                            StateEnum.Whitespace,
                            StateEnum.Operator,
                            StateEnum.Parentheses,
                    },
                    AlphaEnum.Letter,
                    Transitions::newIdentifier
            );

            newTransition(StateEnum.Identifier, AlphaEnum.Letter, Transitions::extendIdentifier);
            newTransition(StateEnum.Identifier, AlphaEnum.Digit, Transitions::extendIdentifier);
            newTransition(StateEnum.Keyword, AlphaEnum.Letter, Transitions::keywordToIdentifier);
            newTransition(StateEnum.Keyword, AlphaEnum.Digit, Transitions::keywordToIdentifier);

            newTransitions(new StateEnum[]{
                            StateEnum.Start,
                            StateEnum.Newline,
                            StateEnum.Tab,
                            StateEnum.Invalid,
                            StateEnum.Whitespace,
                            StateEnum.Operator,
                            StateEnum.Parentheses,
                    },
                    AlphaEnum.Digit,
                    Transitions::newInteger
            );

            newTransition(StateEnum.Integer, AlphaEnum.Digit, Transitions::extendInteger);
            newTransition(StateEnum.Integer, AlphaEnum.Point, Transitions::integerToPoint);
            newTransition(StateEnum.Point, AlphaEnum.Digit, Transitions::toFloat);
            newTransition(StateEnum.Float, AlphaEnum.Digit, Transitions::toFloat);

            newTransitions(excludePoint, AlphaEnum.Operator, Transitions::newOperator);
            newTransition(StateEnum.Operator, AlphaEnum.Operator, Transitions::extendOperator);

            newTransitions(excludePoint, AlphaEnum.Space, Transitions::whitespace);
            newTransition(StateEnum.Whitespace, AlphaEnum.Space, Transitions::extendWhitespace);

            newTransitions(excludePoint, AlphaEnum.Return, Transitions::newline);

            newTransitions(excludePoint, AlphaEnum.Tab, Transitions::tab);

            newTransitions(excludePoint, AlphaEnum.Parenthesis, Transitions::parenthesis);
        }

        State currentState = new State(StateEnum.Start);

        File f = new File(args[0]);



        try (FileReader fr = new FileReader(f.getAbsolutePath())) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("output.csv"))) {
                int n = fr.read();
                while (n != -1) {
                    Alphabet currentAlphabet = new Alphabet((char) n);
                    State nextState = table[currentState.getEnum().ordinal()][currentAlphabet.getEnum().ordinal()]
                            .apply(currentState, currentAlphabet);

                    //print current state when different from next (with exceptions)
                    if (currentState.getEnum() != nextState.getEnum() || currentState.getEnum() == StateEnum.Parentheses) {
                        switch (currentState.getEnum()) {
                            case Identifier -> {
                                if (nextState.getEnum() != StateEnum.Keyword) {
                                    currentState.print(bw);
                                    outputStates.add(currentState);
                                }
                            }
                            case Whitespace -> {
                                if (nextState.getEnum() != StateEnum.Tab) {
                                    currentState.print(bw);
                                    outputStates.add(currentState);
                                }
                            }
                            case Integer -> {
                                if (nextState.getEnum() != StateEnum.Point) {
                                    currentState.print(bw);
                                    outputStates.add(currentState);
                                }
                            }
                            case Point -> {
                            }
                            default -> {
                                currentState.print(bw);
                                outputStates.add(currentState);
                            }
                        }
                        if (nextState.getEnum() == StateEnum.Newline) {
                            int ignored = fr.read();
                        }
                    }

                    currentState = nextState;

                    n = fr.read();
                    if (n == -1) {
                        currentState.print(bw);
                        outputStates.add(currentState);
                    }
                }
            }
            Parser parser = new Parser(outputStates.iterator());
            parser.program();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
