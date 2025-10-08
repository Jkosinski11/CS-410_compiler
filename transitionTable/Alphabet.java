package transitionTable;

public class Alphabet {
    private EnumAlphabet e;
    private char c;
    private Operator op;
    private Parentheses p;

    public Alphabet (char in) {
        if (Character.isLetter(in) || in == '_') {
            e = EnumAlphabet.Letter;
            c = in;
        } else if (Character.isDigit(in)) {
            e = EnumAlphabet.Digit;
            c = in;
        } else {
            switch (in) {
                case '.' -> e = EnumAlphabet.Point;
                case ' ' -> e = EnumAlphabet.Space;
                case '\t' -> e = EnumAlphabet.Tab;
                case '\r' -> e = EnumAlphabet.Return;
                case '(' -> {
                    e = EnumAlphabet.Parenthesis;
                    p = Parentheses.LeftParenthesis;
                }
                case ')' -> {
                    e = EnumAlphabet.Parenthesis;
                    p = Parentheses.RightParenthesis;
                }
                case '+' -> {
                    e = EnumAlphabet.Operator;
                    op = Operator.Plus;
                }
                case '-' -> {
                    e = EnumAlphabet.Operator;
                    op = Operator.Minus;
                }
                case '*' -> {
                    e = EnumAlphabet.Operator;
                    op = Operator.Multiply;
                }
                case '/' -> {
                    e = EnumAlphabet.Operator;
                    op = Operator.Divide;
                }
                case '=' -> {
                    e = EnumAlphabet.Operator;
                    op = Operator.Assign;
                }
                case '!' -> {
                    e = EnumAlphabet.Operator;
                    op = Operator.Bang;
                }
                case '>' -> {
                    e = EnumAlphabet.Operator;
                    op = Operator.Greater;
                }
                case '<' -> {
                    e = EnumAlphabet.Operator;
                    op = Operator.Less;
                }
                case ':' -> {
                    e = EnumAlphabet.Operator;
                    op = Operator.Colon;
                }
                default -> e = EnumAlphabet.Other;
            }
        }
    }

    public EnumAlphabet getEnum() {
        return e;
    }

    public Alphabet setEnum(EnumAlphabet e) {
        this.e = e;
        return this;
    }

    public char getChar() {
        return switch (e) {
            case Letter, Digit -> c;
            default -> throw new IllegalStateException();
        };
    }

    public Parentheses getParentheses() {
        if (e == EnumAlphabet.Parenthesis) return p;
        else throw new IllegalStateException();
    }

    public Alphabet setParentheses(Parentheses p) {
        if  (e == EnumAlphabet.Parenthesis) this.p = p;
        else throw new IllegalStateException();
        return this;
    }

    public Operator getOperator() {
        if (e == EnumAlphabet.Operator) return op;
        else throw new IllegalStateException();
    }
    public Alphabet setOperator(Operator op) {
        if (e == EnumAlphabet.Operator) this.op = op;
        else throw new IllegalStateException();
        return this;
    }
}
