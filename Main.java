public class Main {
    public static void main(String[] args) {
        System.out.println("CS-410 Compiler");
        
        if (args.length == 0) {
            System.out.println("Usage: java Main <source_file>");
            System.exit(1);
        }
        
        String sourceFile = args[0];
        System.out.println("Compiling: " + sourceFile);
    }
}
