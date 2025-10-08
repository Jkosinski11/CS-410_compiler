import transitionTable.*;

public class Transitions {
    public static State currentState;
    public static Alphabet currentAlphabet;

    public static State invalid() {
        return new State(EnumState.Invalid);
    }

    public static State newIdentifier() {
        return new State(EnumState.Identifier).setValue(String.valueOf(currentAlphabet.getChar()));
    }

    public static State extendIdentifier() {
        String extended = new StringBuilder()
                .append(currentState.getValue())
                .append(currentAlphabet.getChar())
                .toString();
        return switch (extended) {
            case "if" -> new State(EnumState.Keyword).setKeyword(Keyword.If);
            case "else" -> new State(EnumState.Keyword).setKeyword(Keyword.Else);
            case "for" -> new State(EnumState.Keyword).setKeyword(Keyword.For);
            case "while" -> new State(EnumState.Keyword).setKeyword(Keyword.While);
            case "elif" -> new State(EnumState.Keyword).setKeyword(Keyword.Elif);
            default -> new State(EnumState.Identifier).setValue(extended);
        };
    }

    public static State keywordToIdentifier() {
        String newValue = new StringBuilder()
                .append(currentState.getKeyword().value)
                .append(currentAlphabet.getChar())
                .toString();
        return new State(EnumState.Identifier).setValue(newValue);
    }

    public static State newInteger() {
        return new State(EnumState.Integer).setValue(String.valueOf(currentAlphabet.getChar()));
    }

    public static State extendInteger() {
        String newValue = new StringBuilder()
                .append(currentState.getValue())
                .append(currentAlphabet.getChar())
                .toString();
        return new State(EnumState.Integer).setValue(newValue);
    }

    public static State integerToPoint() {
        String newValue = new StringBuilder()
                .append(currentState.getValue())
                .append('.')
                .toString();
        return new State(EnumState.Point).setValue(newValue);
    }

    public static State toFloat() {
        String newValue = new StringBuilder()
                .append(currentState.getValue())
                .append(currentAlphabet.getChar())
                .toString();
        return new State(EnumState.Float).setValue(newValue);
    }

    public static State tab() {
        return new State(EnumState.Tab);
    }

    public static State whitespace() {
        return new State(EnumState.Whitespace).setCount(1);
    }

    public static State extendWhitespace() {
        int newCount = currentState.getCount() + 1;
        if (newCount == Main.SPACES_IN_TAB) {
            return new State(EnumState.Tab);
        }
        return new State(EnumState.Whitespace).setCount(newCount);
    }

    public static State newline() {
        return new State(EnumState.Newline);
    }

    public static State parenthesis() {
        return new State(EnumState.Parentheses).setParentheses(currentAlphabet.getParentheses());
    }

    public static State newOperator() {
        return new State(EnumState.Operator).setOperator(currentAlphabet.getOperator());
    }

    public static State extendOperator() {
        if (currentAlphabet.getOperator() == Operator.Equals) {
            return switch (currentState.getOperator()) {
                case Assign -> new State(EnumState.Operator).setOperator(Operator.Equals);
                case Greater -> new State(EnumState.Operator).setOperator(Operator.GreaterEquals);
                case Less -> new State(EnumState.Operator).setOperator(Operator.LessEquals);
                case Bang -> new State(EnumState.Operator).setOperator(Operator.NotEquals);
                default -> new State(EnumState.Invalid);
            };
        } else {
            return new State(EnumState.Invalid);
        }
    }
}
