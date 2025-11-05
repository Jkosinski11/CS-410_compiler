import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * ParseAtomsFromTokenFolder
 *
 * WHAT IT DOES
 * ------------
 * - Reads *token dump files* (the lines your scanner prints like
 *   "Token Class: IDENTIFIER Value: x") from a file or a directory.
 * - Converts those lines into parser tokens (adapter).
 * - Runs a hand-written recursive-descent parser.
 * - Emits 3-address "atoms" exactly as your rubric specifies and prints them.
 *
 * HOW TO RUN
 * ----------
 * javac IRParser.java
 *
  token file:
 * java IRParser test.txt
 *
 *
 * SUPPORTED "Token Class:" NAMES
 * ------------------------------
 *   IDENTIFIER, INTEGER_LITERAL, FLOAT_LITERAL, OPERATOR,
 *   IF_KEYWORD, ELIF_KEYWORD, ELSE_KEYWORD, WHILE_KEYWORD, FOR_KEYWORD,
 *   (, ), SPACE, NEWLINE, COLON
 * Other classes are ignored with a warning (you can extend the map below).
 */
public class IRParser {

    /* ===========================
     * (I) DATA STRUCTURES / MODEL
     * =========================== */

    enum Opcode { ADD, SUB, MUL, DIV, JMP, NEG, LBL, TST, MOV }

    // cmp ∈ {0..6}: 0=always, 1==, 2<, 3>, 4<=, 5>=, 6!=
    enum Cmp { ALW, EQ, LT, GT, LE, GE, NE }

    static final class Atom {
        final Opcode op;
        final String left;
        final String right;
        final String result;
        final Cmp cmp;
        final String dest;

        private Atom(Opcode op, String left, String right, String result, Cmp cmp, String dest) {
            this.op = op; this.left = left; this.right = right; this.result = result; this.cmp = cmp; this.dest = dest;
        }

        static Atom add(String l,String r,String res){ return new Atom(Opcode.ADD,l,r,res,null,null); }
        static Atom sub(String l,String r,String res){ return new Atom(Opcode.SUB,l,r,res,null,null); }
        static Atom mul(String l,String r,String res){ return new Atom(Opcode.MUL,l,r,res,null,null); }
        static Atom div(String l,String r,String res){ return new Atom(Opcode.DIV,l,r,res,null,null); }
        static Atom neg(String l,String res){ return new Atom(Opcode.NEG,l,null,res,null,null); }
        static Atom mov(String s,String d){ return new Atom(Opcode.MOV,s,null,d,null,null); }
        static Atom jmp(String dest){ return new Atom(Opcode.JMP,null,null,null,null,dest); }
        static Atom lbl(String dest){ return new Atom(Opcode.LBL,null,null,null,null,dest); }
        static Atom tst(String l,String r,Cmp c,String dest){ return new Atom(Opcode.TST,l,r,null,c,dest); }

        @Override public String toString() {
            switch (op) {
                case ADD: return "(ADD, " + left + ", " + right + ", " + result + ")";
                case SUB: return "(SUB, " + left + ", " + right + ", " + result + ")";
                case MUL: return "(MUL, " + left + ", " + right + ", " + result + ")";
                case DIV: return "(DIV, " + left + ", " + right + ", " + result + ")";
                case NEG: return "(NEG, " + left + ",_, " + result + ")";
                case MOV: return "(MOV, " + left + ",_, " + result + ")";
                case JMP: return "(JMP,_,_,_,_, " + dest + ")";
                case LBL: return "(LBL,_,_ ,_,_, " + dest + ")";
                case TST: return "(TST, " + left + ", " + right + ",_, " + cmp.ordinal() + ", " + dest + ")";
                default: throw new AssertionError(op);
            }
        }
    }

    /* =======================
     * TOKEN TYPES FOR PARSER
     * ======================= */

    enum TokType {
        IDENT, INT, FLOAT,
        OP,         
        LPAREN, RPAREN,
        KW_IF, KW_ELIF, KW_ELSE, KW_WHILE, KW_FOR,
        EOF
    }

    static final class Tok {
        final TokType type;
        final String lexeme; 
        Tok(TokType t, String lx) { this.type = t; this.lexeme = lx; }
        public String toString(){ return type + (lexeme!=null?(":"+lexeme):""); }
    }


    static final class TokenDumpAdapter {

        /** Reads a "Token Class: ... Value: ..." file and converts to parser tokens.
         *  Skips SPACE / NEWLINE / COLON. */
        static List<Tok> readTokenFile(Path file) throws IOException {
            List<Tok> out = new ArrayList<>();
            try (BufferedReader br = Files.newBufferedReader(file)) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    // Expect formats like:
                    // Token Class: IDENTIFIER           Value: x
                 

                    if (!line.startsWith("Token Class:")) continue; // skip headers like "anning: test.txt" etc.

                    String cls = null, val = null;

                    // Extract after "Token Class:"
                    int idxClass = line.indexOf("Token Class:");
                    int idxValue = line.indexOf("Value:");
                    if (idxClass >= 0) {
                        if (idxValue >= 0) {
                            cls = line.substring("Token Class:".length(), idxValue).trim();
                            val = line.substring(idxValue + "Value:".length()).trim();
                        } else {
                            cls = line.substring("Token Class:".length()).trim();
                            val = "N/A";
                        }
                    }
                    if (cls == null) continue;

                    // Normalize "N/A"
                    if ("N/A".equals(val)) val = null;

                    // Map classes
                    switch (cls) {
                        case "SPACE":
                        case "NEWLINE":
                        case "COLON":
                            break;

                        case "(":
                            out.add(new Tok(TokType.LPAREN, "("));
                            break;
                        case ")":
                            out.add(new Tok(TokType.RPAREN, ")"));
                            break;

                        case "IDENTIFIER":
                            if (val == null) warn(file, line, "IDENTIFIER without value");
                            else out.add(new Tok(TokType.IDENT, val));
                            break;

                        case "INTEGER_LITERAL":
                            if (val == null) warn(file, line, "INTEGER_LITERAL without value");
                            else out.add(new Tok(TokType.INT, val));
                            break;

                        case "FLOAT_LITERAL":
                            if (val == null) warn(file, line, "FLOAT_LITERAL without value");
                            else out.add(new Tok(TokType.FLOAT, val));
                            break;

                        case "OPERATOR":
                            if (val == null) warn(file, line, "OPERATOR without value");
                            else out.add(new Tok(TokType.OP, val));
                            break;

                        case "IF_KEYWORD":
                            out.add(new Tok(TokType.KW_IF, "if"));
                            break;
                        case "ELIF_KEYWORD":
                            out.add(new Tok(TokType.KW_ELIF, "elif"));
                            break;
                        case "ELSE_KEYWORD":
                            out.add(new Tok(TokType.KW_ELSE, "else"));
                            break;
                        case "WHILE_KEYWORD":
                            out.add(new Tok(TokType.KW_WHILE, "while"));
                            break;
                        case "FOR_KEYWORD":
                            out.add(new Tok(TokType.KW_FOR, "for"));
                            break;

                        default:
                            // Unknown token class -> ignore or warn
                            warn(file, line, "Unmapped token class: " + cls);
                    }
                }
            }
            out.add(new Tok(TokType.EOF, null));
            return out;
        }

        private static void warn(Path file, String line, String msg) {
            System.err.println("[warn] " + file.getFileName() + ": " + msg + " | line=\"" + line + "\"");
        }
    }

    static final class Parser {
        private final List<Atom> out = new ArrayList<>();
        private final List<Tok> tokens;
        private int pos=0;
        private int tempId=0, labelId=0;

        Parser(List<Tok> tokens){ this.tokens = tokens; }

        List<Atom> getAtoms(){ return out; }

        private Tok la(){ return tokens.get(pos); }
        private TokType lat(){ return la().type; }
        private boolean match(TokType t){ if (lat()==t){ pos++; return true;} return false; }
        private Tok expect(TokType t, String msg){ if(lat()!=t) throw err(msg); return tokens.get(pos++); }
        private RuntimeException err(String msg){ return new RuntimeException("Parse error at token "+la()+" : "+msg); }
        private String newTemp(){ return "t"+(tempId++); }
        private String newLabel(){ return "L"+(labelId++); }

        /* Entry */
        void program() {
            while (lat()!=TokType.EOF) {
                statement();
            }
        }

        /* Statements */
        private void statement() {
            switch (lat()) {
                case KW_IF: ifStmt(); return;
                case KW_WHILE: whileStmt(); return;
                case KW_FOR: forStmt(); return;
                case IDENT: {
                    // lookahead for assignment
                    if (peekAssign()) { assign(); return; }
                }
                default:
                    // expression statement (result unused)
                    expr();
            }
        }

        private boolean peekAssign() {
            // IDENT '=' ...
            if (lat()!=TokType.IDENT) return false;
            if (pos+1 < tokens.size()) {
                Tok t2 = tokens.get(pos+1);
                return (t2.type==TokType.OP && "=".equals(t2.lexeme));
            }
            return false;
        }

        private void assign() {
            String id = expect(TokType.IDENT,"identifier").lexeme;
            Tok eq = expect(TokType.OP,"'='");
            if (!"=".equals(eq.lexeme)) throw err("expected '=' after identifier");
            String rhs = expr();
            out.add(Atom.mov(rhs, id));
        }

        private void ifStmt() {
            expect(TokType.KW_IF,"'if'");
            String endIf = newLabel();
            String jf = newLabel();

            condToTSTJump(jf);
            statement();
            out.add(Atom.jmp(endIf));
            out.add(Atom.lbl(jf));

            while (lat()==TokType.KW_ELIF) {
                pos++;
                String nx = newLabel();
                condToTSTJump(nx);
                statement();
                out.add(Atom.jmp(endIf));
                out.add(Atom.lbl(nx));
            }
            if (lat()==TokType.KW_ELSE) {
                pos++;
                statement();
            }
            out.add(Atom.lbl(endIf));
        }

        private void whileStmt() {
            expect(TokType.KW_WHILE,"'while'");
            String start = newLabel(), end = newLabel();
            out.add(Atom.lbl(start));
            condToTSTJump(end);
            statement();
            out.add(Atom.jmp(start));
            out.add(Atom.lbl(end));
        }

        private void forStmt() {
            // Minimal form handled: for ( init ; cond ; step ) stmt
            expect(TokType.KW_FOR,"'for'");
            expect(TokType.LPAREN,"'('");


            if (lat()!=TokType.RPAREN && !(lat()==TokType.OP && ";".equals(la().lexeme))) {
                if (peekAssign()) assign(); else expr();
            }
            if (lat()==TokType.OP && ";".equals(la().lexeme)) pos++;

            String start = newLabel(), end = newLabel(), stepLbl = newLabel();
            out.add(Atom.lbl(start));

         
            boolean hasCond = (lat()!=TokType.RPAREN && !(lat()==TokType.OP && ";".equals(la().lexeme)));
            if (hasCond) condToTSTJump(end); else out.add(Atom.tst("0","0",Cmp.ALW, stepLbl));

   
            if (lat()==TokType.OP && ";".equals(la().lexeme)) pos++;

            if (lat()!=TokType.RPAREN) expr();

            expect(TokType.RPAREN,"')'");
            statement();
            out.add(Atom.lbl(stepLbl));
            out.add(Atom.jmp(start));
            out.add(Atom.lbl(end));
        }

        /* Conditions → emits TST(left,right,cmp,dest) */
        private void condToTSTJump(String dest) {
            expect(TokType.LPAREN,"'('");
            String left = expr();
            Cmp cmp = Cmp.ALW;
            String right = "0";
            if (lat()==TokType.OP) {
                Cmp m = mapCmp(la().lexeme);
                if (m==null) throw err("expected comparison operator");
                cmp = m; pos++;
                right = expr();
            }
            expect(TokType.RPAREN,"')'");
            out.add(Atom.tst(left, right, cmp, dest));
        }

        private Cmp mapCmp(String op) {
            if ("==".equals(op)) return Cmp.EQ;
            if ("!=".equals(op)) return Cmp.NE;
            if ("<".equals(op))  return Cmp.LT;
            if (">".equals(op))  return Cmp.GT;
            if ("<=".equals(op)) return Cmp.LE;
            if (">=".equals(op)) return Cmp.GE;
            return null;
        }

        /* Expressions (with precedence) */
        private String expr() {
            String place = term();
            while (lat()==TokType.OP && ("+".equals(la().lexeme) || "-".equals(la().lexeme))) {
                String op = la().lexeme; pos++;
                String rhs = term();
                String t = newTemp();
                if ("+".equals(op)) out.add(Atom.add(place, rhs, t));
                else                out.add(Atom.sub(place, rhs, t));
                place = t;
            }
            return place;
        }

        private String term() {
            String place = factor();
            while (lat()==TokType.OP && ("*".equals(la().lexeme) || "/".equals(la().lexeme))) {
                String op = la().lexeme; pos++;
                String rhs = factor();
                String t = newTemp();
                if ("*".equals(op)) out.add(Atom.mul(place, rhs, t));
                else                out.add(Atom.div(place, rhs, t));
                place = t;
            }
            return place;
        }

        private String factor() {
            switch (lat()) {
                case INT:   { String v = la().lexeme; pos++; return v; }
                case FLOAT: { String v = la().lexeme; pos++; return v; }
                case IDENT: { String id = la().lexeme; pos++; return id; }
                case LPAREN: {
                    pos++; // '('
                    String v = expr();
                    expect(TokType.RPAREN,"')'");
                    return v;
                }
                case OP: {
                    if ("-".equals(la().lexeme)) {
                        pos++;
                        String x = factor();
                        String t = newTemp();
                        out.add(Atom.neg(x, t));
                        return t;
                    }
                    // fall through
                }
                default:
                    throw err("expected factor (int/float/ident/(expr)/-factor)");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java ParseAtomsFromTokenFolder <token-file-or-folder>");
            System.exit(1);
        }
        Path p = Paths.get(args[0]);
        if (!Files.exists(p)) {
            System.err.println("Path not found: " + p);
            System.exit(2);
        }

        if (Files.isDirectory(p)) {
            
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(p, path -> {
                String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                return Files.isRegularFile(path) && (name.endsWith(".txt") || name.endsWith(".tokens"));
            })) {
                List<Path> all = new ArrayList<>();
                for (Path f : ds) all.add(f);
                all.sort(Comparator.comparing(a -> a.getFileName().toString()));

                if (all.isEmpty()) {
                    System.err.println("No .txt or .tokens files in " + p);
                    System.exit(3);
                }

                for (Path f : all) {
                    System.out.println("=== " + f.getFileName() + " ===");
                    runOneFile(f);
                    System.out.println();
                }
            }
        } else {
            runOneFile(p);
        }
    }

    private static void runOneFile(Path file) throws IOException {
        List<Tok> toks = TokenDumpAdapter.readTokenFile(file);
        Parser parser = new Parser(toks);
        parser.program();
        for (Atom a : parser.getAtoms()) {
            System.out.println(a);
        }
    }
}
