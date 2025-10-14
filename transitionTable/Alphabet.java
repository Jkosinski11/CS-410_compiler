package transitionTable;

public class Alphabet {
    private final AlphaEnum e;
    private char c;
    private Operator op;
    private Parentheses p;

    public Alphabet (char in) {
        if (Character.isLetter(in) || in == '_') {
            e = AlphaEnum.Letter;
            c = in;
        } else if (Character.isDigit(in)) {
            e = AlphaEnum.Digit;
            c = in;
        } else {
            switch (in) {
                case '.' -> e = AlphaEnum.Point;
                case ' ' -> e = AlphaEnum.Space;
                case '\t' -> e = AlphaEnum.Tab;
                case '\r' -> e = AlphaEnum.Return;
                case '(' -> {
                    e = AlphaEnum.Parenthesis;
                    p = Parentheses.LeftParenthesis;
                }
                case ')' -> {
                    e = AlphaEnum.Parenthesis;
                    p = Parentheses.RightParenthesis;
                }
                case '+' -> {
                    e = AlphaEnum.Operator;
                    op = Operator.Plus;
                }
                case '-' -> {
                    e = AlphaEnum.Operator;
                    op = Operator.Minus;
                }
                case '*' -> {
                    e = AlphaEnum.Operator;
                    op = Operator.Multiply;
                }
                case '/' -> {
                    e = AlphaEnum.Operator;
                    op = Operator.Divide;
                }
                case '=' -> {
                    e = AlphaEnum.Operator;
                    op = Operator.Assign;
                }
                case '!' -> {
                    e = AlphaEnum.Operator;
                    op = Operator.Bang;
                }
                case '>' -> {
                    e = AlphaEnum.Operator;
                    op = Operator.Greater;
                }
                case '<' -> {
                    e = AlphaEnum.Operator;
                    op = Operator.Less;
                }
                case ':' -> {
                    e = AlphaEnum.Operator;
                    op = Operator.Colon;
                }
                default -> e = AlphaEnum.Other;
            }
        }
    }

    public AlphaEnum getEnum() {
        return e;
    }

    public char getChar() {
        return switch (e) {
            case Letter, Digit -> c;
            default -> throw new IllegalStateException();
        };
    }

    public Parentheses getParentheses() {
        if (e == AlphaEnum.Parenthesis) return p;
        else throw new IllegalStateException();
    }

    public Operator getOperator() {
        if (e == AlphaEnum.Operator) return op;
        else throw new IllegalStateException();
    }
}
