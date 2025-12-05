import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator {

    // ==========================================
    // (I) DATA STRUCTURES (Copied from Phase 2)
    // ==========================================

    enum Opcode { ADD, SUB, MUL, DIV, JMP, NEG, LBL, TST, MOV } 
    // Note: I added LOD, STO, HLT, CLR, CMP purely for internal machine code generation mapping, 
    // though your input file might only contain the original set.

    // cmp ∈ {0..6}: 0=always, 1==, 2<, 3>, 4<=, 5>=, 6!=
    enum Cmp { ALW, EQ, LT, GT, LE, GE, NE }

    static final class Atom {
        final Opcode op;
        final String left;
        final String right;
        final String result;
        final Cmp cmp;
        final String dest;

        
        public Atom(Opcode op, String left, String right, String result, Cmp cmp, String dest) {
            this.op = op; 
            this.left = left; 
            this.right = right; 
            this.result = result; 
            this.cmp = cmp; 
            this.dest = dest;
        }
        
       
        
        @Override public String toString() {
            // You can keep this for debugging
            return String.format("(%s, %s, %s, %s)", op, left, right, result);
        }
    }

    //Maps variables/literals to Memory Addresses
    // Start addresses at 1000 to leave plenty of space for instructions to be stored
    static Map<String, Integer> memoryMap = new HashMap<>();
    static int nextAddress = 1000;
    
    static Map<String, Integer> labelTable = new HashMap<>();
    
    static int instructionCounter = 0;

    // Opcodes for Phase 3 (based on instructions)
    static final int OP_CLR = 0;
    static final int OP_ADD = 1;
    static final int OP_SUB = 2;
    static final int OP_MUL = 3;
    static final int OP_DIV = 4;
    static final int OP_JMP = 5;
    static final int OP_CMP = 6;
    static final int OP_LOD = 7;
    static final int OP_STO = 8;
    static final int OP_HLT = 9;

    public static void main(String[] args) {
        String fileName = args[0];
        List<Atom> atoms = new ArrayList<>();

        // parses out the atom strings as Atom objects
        // TBD: may change in phase 4 to just accept the Atom objects from phase 2 directly by changing
        // phase 2 to output the atoms to phase 3 rather than to text
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                atoms.add(parseAtom(line));
            }
        } catch (Error | IOException e) {
            e.printStackTrace();
        } 

        //Assigns memory addresses to the literals and vars 
        //(first pass in other code will handle label table for JMP labels)
        firstPass(atoms); 
        
        for(Atom a: atoms) {
            mapData(a.left);
            mapData(a.right);
            mapData(a.result);
            // we are ignoring the destination in this step because it will contain labels
            // we will handle these when we build the label table
        }

        // generates the instructions for all the atoms
        for (Atom a : atoms) {
            generateInstructions(a);
        }
    }

    // Assigns a memory address to a variable or literal if it doesn't have one
    private static void mapData(String s) {
        if (s != null && !s.equals("_") && !memoryMap.containsKey(s)) {
            memoryMap.put(s, nextAddress++);
        }
    }

    // Retrieves the address. If it's a Label (not in map), returns 0 for Part A
    private static int getAddr(String s) {
    	Integer dataAddr = memoryMap.get(s);
        if (dataAddr != null) {
            return dataAddr;
        }

        // Then check labels
        Integer labelAddr = labelTable.get(s);
        if (labelAddr != null) {
            return labelAddr;
        }

        return memoryMap.getOrDefault(s, 0); 
        
    }

    // Bitwise construction of the 32-bit instruction
    // Format: [Address 20 bits] [Reg 4 bits] [Mode/Cmp 4 bits] [Opcode 4 bits]
    private static void emit(int opcode, int cmpMode, int reg, int addr) {
        long instruction = 0;
        
        // Masks to ensure we adhere to the bit limits
        long opMask   = opcode & 0xF;           // 4 bits
        long modeMask = cmpMode & 0xF;          // 4 bits
        long regMask  = reg & 0xF;              // 4 bits
        long addrMask = addr & 0xFFFFF;         // 20 bits

        // Shift and OR
        instruction = (addrMask << 12) | (regMask << 8) | (modeMask << 4) | opMask;

        // Print as 32-bit Binary String (padded with leading zeros to reach 32 bits)
        String binary = String.format("%32s", Long.toBinaryString(instruction)).replace(' ', '0');
        System.out.println(binary);
    }
    
    private static void firstPass(List<Atom> atoms) {
        //Assign memory addresses to variables and literals
        for (Atom a : atoms) {
            mapData(a.left);
            mapData(a.right);
            mapData(a.result);
        }

        //Walk atoms, calculate addresses
        instructionCounter = 0;

        for (Atom a : atoms) {
            switch (a.op) {
                case LBL:
                    if (a.dest != null && !"_".equals(a.dest)) {
                        labelTable.put(a.dest, instructionCounter);
                    }
                    break;

                case ADD:
                case SUB:
                case MUL:
                case DIV:
                case NEG: // CLR, SUB, STO = 3 instructions
                    instructionCounter += 3;
                    break;

                case MOV: // LOD, STO = 2 instructions
                    instructionCounter += 2;
                    break;

                case JMP:
                    instructionCounter += 2; 
                    break;

                case TST:
                    instructionCounter += 3; 
                    break;
            }
        }
    }
    // Main translation. Basically the 2nd pass because we save the generation and emitting of our
    // binary for here for after the label table is made
    private static void generateInstructions(Atom a) {
        // We use Register 0 (r0) as our default accumulator for all operations.
        int r0 = 0; 
        
        switch (a.op) {
            case ADD: 
                emit(OP_LOD, 0, r0, getAddr(a.left));
                emit(OP_ADD, 0, r0, getAddr(a.right));
                emit(OP_STO, 0, r0, getAddr(a.result));
                break;

            case SUB: 
                emit(OP_LOD, 0, r0, getAddr(a.left));
                emit(OP_SUB, 0, r0, getAddr(a.right));
                emit(OP_STO, 0, r0, getAddr(a.result));
                break;

            case MUL: 
                emit(OP_LOD, 0, r0, getAddr(a.left));
                emit(OP_MUL, 0, r0, getAddr(a.right));
                emit(OP_STO, 0, r0, getAddr(a.result));
                break;

            case DIV:
                emit(OP_LOD, 0, r0, getAddr(a.left));
                emit(OP_DIV, 0, r0, getAddr(a.right));
                emit(OP_STO, 0, r0, getAddr(a.result));
                break;
            case JMP: // Made jump unconditional by forcing an always true comparison since the atom for JMP is unconditional in phase 2 
                emit(OP_CMP, 0, r0, 0); // Compare r0 to 0 with Mode 0 (Always True)
                emit(OP_JMP, 0, 0, getAddr(a.dest));
                break;

            case LBL:
                // Labels generate NO code. They are just markers
                // In Part B we record the instruction pointer here
                break;

            case MOV:
                emit(OP_LOD, 0, r0, getAddr(a.left));
                emit(OP_STO, 0, r0, getAddr(a.result));
                break;

            case NEG: 
                emit(OP_CLR, 0, r0, 0); 
                emit(OP_SUB, 0, r0, getAddr(a.left));
                emit(OP_STO, 0, r0, getAddr(a.result));
                break;

            case TST: 
                // TST atoms in phase 2 imply a conditional jump so it has been added
                emit(OP_LOD, 0, r0, getAddr(a.left));
                
                // We map the atom's CMP enum ordinal to the Machine CMP code
                int cmpCode = a.cmp.ordinal();
                emit(OP_CMP, cmpCode, r0, getAddr(a.right));

                // Note: For Part A, getAddr(a.dest) returns 0 because labels aren't mapped yet.
                // This is expected behavior for Part A
                emit(OP_JMP, 0, 0, getAddr(a.dest)); 
                break;
        }
    }

    // Adapter to parse the text file string back into an Atom object
    private static Atom parseAtom(String line) {
        line = line.replace("(", "").replace(")", "");
        String[] parts = line.split(",");
        for(int i=0; i<parts.length; i++) parts[i] = parts[i].trim();

        Opcode op = Opcode.valueOf(parts[0]);
        String left = null, right = null, result = null, dest = null;
        Cmp cmp = Cmp.ALW; // Default

        switch (op) {
            case ADD: case SUB: case MUL: case DIV:
                left = parts[1]; right = parts[2]; result = parts[3];
                break;
            case NEG: case MOV:
                left = parts[1]; result = parts[3]; 
                break;
            case JMP: case LBL:
                dest = parts[5]; 
                break;
            case TST:
                left = parts[1]; right = parts[2];
                // parts[3] is "_"
                int cmpOrd = Integer.parseInt(parts[4]);
                cmp = Cmp.values()[cmpOrd];
                dest = parts[5];
                break;
        }
        
        if ("_".equals(left)) left = null;
        if ("_".equals(right)) right = null;
        if ("_".equals(result)) result = null;

        return new Atom(op, left, right, result, cmp, dest);
    }
}
