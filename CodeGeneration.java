import absyn.*;
import symbol.*;
import java.io.*;
import java.util.ArrayList;

public class CodeGeneration {
    public static final int IN_ADDR = 4;
    public static final int OUT_ADDR = 7;
    public static final int AC = 0;
    public static final int FP = 5;
    public static final int GP = 6;
    public static final int PC = 7;
    public int emitLoc = 0;
    public int highLoc = 0;
    public int offset = 0;

    private SemanticAnalyzer analyzer;
    public String filename;

    public CodeGeneration(String filename) {
        this.filename = filename + CM.EXT_TM;
        analyzer = new SemanticAnalyzer(false, "");
    }

    public void generate(DecList list) {
        analyzer.enterScope();

		// generate code for input and output functions
        SymbolNode input = new FunctionSymbol(NameTy.INT, "input", new ArrayList<SymbolNode>(), IN_ADDR);
        analyzer.addSymbol("input", input);
        ArrayList<SymbolNode> params = new ArrayList<SymbolNode>();
        params.add(new VarSymbol(NameTy.INT, "value"));
        SymbolNode output = new FunctionSymbol(NameTy.VOID, "output", params, OUT_ADDR);
        analyzer.addSymbol("output", output);

        emitRM("LD", GP, 0, AC, "Load GP with max address");
        emitRM("LDA", FP, 0, GP, "Copy GP to FP");
        emitRM("ST", 0, 0, 0, "Clear location 0");
        int savedLoc1 = emitSkip(1);
		// input function
        generateCode("* Jump around I/O functions here\n");
        generateCode("* Code for input routine\n");
        emitRM("ST", 0, -1, FP, "Store return");
        emitOp("IN", 0, 0, 0, "");
        emitRM("LD", PC, -1, FP, "Return caller");
		// output function
        generateCode("* Code for output routine\n");
        emitRM("ST", 0, -1, FP, "Store return");
        emitRM("LD", 0, -2, FP, "Load output value");
        emitOp("OUT", 0, 0, 0, "");
        emitRM("LD", 7, -1, FP, "Return caller");
        int savedLoc2 = emitSkip(0);
        if (savedLoc1 > highLoc) {
            generateCode("* Bug in emmitBackup\n");
        }
        emitLoc = savedLoc1;
        emitRMAbs("LDA", PC, savedLoc2, "Jump around I/O functions");
        emitLoc = highLoc;
        generateCode("* End of standard prelude\n");
		
        while (list != null) {
            if (list.head != null) {
                codeGen(list.head);
            }
            list = list.tail;
        }

        FunctionSymbol funcSymbol = (FunctionSymbol) analyzer.getFunction("main");
        emitRM("ST", FP, this.offset, FP, "Push old frame pointer");
        emitRM("LDA", FP, this.offset, FP, "Push frame");
        emitRM("LDA", 0, 1, PC, "Load ac with ret ptr");
		// main function
        emitRMAbs("LDA", PC, funcSymbol.addr, "Jump to main");
        emitRM("LD", FP, 0, FP, "Pop frame");
        emitOp("HALT", 0, 0, 0, "");

        analyzer.exitScope();
    }

    /*
     * SimpleVar
     */
    public void codeGen(SimpleVar var, int offset, boolean isAddr) {
        SimpleVar simpleVar = (SimpleVar) var;
        VarSymbol varSymbol = (VarSymbol) analyzer.getSymbol(simpleVar.name);
        generateCode("* -> id\n");
        generateCode("* looking up id: " + simpleVar.name + "\n");
        if (analyzer.symbolExists(simpleVar.name) == 0) {
            if (isAddr) {
				emitRM("LDA", 0, varSymbol.offset, GP, "load id address");
			} else {
                emitRM("LD", 0, varSymbol.offset, GP, "load id value");
            }
        } else {
            if (isAddr) {
                emitRM("LDA", 0, varSymbol.offset, FP, "load id address");
            } else {
                emitRM("LD", 0, varSymbol.offset, FP, "load id value");
            }
        }
        generateCode("* <- id\n");
    }

    /*
     * IndexVar
     */
    public void codeGen(IndexVar var, int offset, boolean isAddr) {
        IndexVar indexVar = (IndexVar) var;
        ArraySymbol arraySymbol = (ArraySymbol) analyzer.getSymbol(indexVar.name);
        generateCode("* -> subs\n");
        if (analyzer.symbolExists(indexVar.name) == 0) {
            emitRM("LD", AC, arraySymbol.offset, GP, "load id value");
            emitRM("ST", AC, offset, GP, "store array addr");
            offset--;
            codeGen(indexVar.index, offset, false);
            generateCode("* <- subs\n");
        } else {
            emitRM("LD", AC, arraySymbol.offset, FP, "load id value");
            emitRM("ST", AC, offset, FP, "store array addr");
            offset--;
            codeGen(indexVar.index, offset, false);
            generateCode("* <- subs\n");
        }
    }

    /*
     * VarExp
     */
    public void codeGen(VarExp exp, int offset, boolean isAddr) {
        if (exp.name instanceof SimpleVar) {
            SimpleVar simpleVar = (SimpleVar) exp.name;
            VarSymbol varSymbol = (VarSymbol) analyzer.getSymbol(simpleVar.name);
            generateCode("* -> id\n");
            generateCode("* looking up id: " + simpleVar.name +"\n");
            if (analyzer.symbolExists(simpleVar.name) == 0) {
                if (isAddr) {
                    emitRM("LDA", 0, varSymbol.offset, GP, "load id address");
                } else {
                    emitRM("LD", 0, varSymbol.offset, GP, "load id value");
                }
            } else {
                if (isAddr) {
                    emitRM("LDA", 0, varSymbol.offset, FP, "load id address");
                } else {
                    emitRM("LD", 0, varSymbol.offset, FP, "load id value");
                }
            }
            generateCode("* <- id\n");
        } else if (exp.name instanceof IndexVar) {
            IndexVar indexVar = (IndexVar) exp.name;
            ArraySymbol arraySymbol = (ArraySymbol) analyzer.getSymbol(indexVar.name);
            generateCode("* -> subs\n");
            if (analyzer.symbolExists(indexVar.name) == 0) {
                emitRM("LD", AC, arraySymbol.offset, GP, "load id value");
                emitRM("ST", AC, offset, GP, "store array addr");
                offset--;
                codeGen(indexVar.index, offset, false);
                generateCode("* <- subs\n");
            } else {
                emitRM("LD", AC, arraySymbol.offset, FP, "load id value");
                emitRM("ST", AC, offset, FP, "store array addr");
                offset--;
                codeGen(indexVar.index, offset, false);
                generateCode("* <- subs\n");
            }
        }
    }

    /*
     * IntExp
     */
    public void codeGen(IntExp exp) {
        generateCode("* -> constant\n");
        emitRM("LDC", AC, exp.value, 0, "load const");
        generateCode("* <- constant\n");
    }

    /*
     * CallExp
     */
    public void codeGen(CallExp exp, int offset) {
        int param = -2;
        FunctionSymbol funcSymbol = (FunctionSymbol) analyzer.getFunction(exp.func);
        generateCode("* -> call\n");
        generateCode("* call of function: " + exp.func + "\n");

        while (exp.args != null) {
            if (exp.args.head != null) {
                codeGen(exp.args.head, offset, false);
                emitRM("ST", AC, offset + param, FP, "op: push left");
                param--;
            }
            exp.args = exp.args.tail;
        }

        emitRM("ST", FP, offset, FP, "push ofp");
        emitRM("LDA", FP, offset, FP, "Push frame");
        emitRM("LDA", 0, 1, PC, "Load ac with ret ptr");
        emitRMAbs("LDA", PC, funcSymbol.addr, "jump to fun loc");
        emitRM("LD", FP, 0, FP, "Pop frame");
        generateCode("* <- call\n");
    }

    /*
     * OpExp
     */
    public void codeGen(OpExp exp, int offset) {
        generateCode("* -> op\n");
        if (exp.left instanceof IntExp) {
            codeGen(exp.left, offset, false);
            emitRM("ST", AC, offset, FP, "op: push left");
            offset--;
        } else if (exp.left instanceof VarExp) {
            VarExp varExp = (VarExp) exp.left;
            if (varExp.name instanceof SimpleVar) {
                codeGen(varExp, offset, false);
                emitRM("ST", AC, offset, FP, "op: push left");
                offset--;
            } else {
                codeGen(varExp, offset, true);
                offset--;
            }
        } else if (exp.left instanceof CallExp) {
            codeGen(exp.left, offset, false);
        } else if (exp.left instanceof OpExp) {
            codeGen(exp.left, offset, false);
            emitRM("ST", AC, offset, FP, "");
            offset--;
        }

        if (exp.right instanceof IntExp) {
            codeGen(exp.right, offset, false);
        } else if (exp.right instanceof VarExp) {
            VarExp varExp = (VarExp) exp.right;
            if (varExp.name instanceof SimpleVar) {
                codeGen(varExp, offset, false);
            } else {
                codeGen(varExp, offset, true);
            }
        } else if (exp.right instanceof CallExp) {
            codeGen(exp.right, offset, false);
        } else if (exp.right instanceof OpExp) {
            codeGen(exp.right, offset, false);
        }

        offset++;
        emitRM("LD", 1, offset, FP, "op: load left");

        switch (exp.op) {
        case OpExp.ADD:
            emitOp("ADD", AC, 1, AC, "op +");
            break;
        case OpExp.SUB:
            emitOp("SUB", AC, 1, AC, "op -");
            break;
        case OpExp.MUL:
            emitOp("MUL", AC, 1, AC, "op *");
            break;
        case OpExp.DIV:
            emitOp("DIV", AC, 1, AC, "op /");
            break;
        case OpExp.ASSN:
            emitOp("ASSN", AC, 1, AC, "op =");
            break;
        case OpExp.LT:
            emitOp("SUB", AC, 1, AC, "op <");
            emitRM("JLT", AC, 2, PC, "");
            emitRM("LDC", AC, 0, 0, "false case");
            emitRM("LDA", PC, 1, PC, "unconditional jump");
            emitRM("LDC", AC, 1, 0, "true case");
            break;
        case OpExp.LE:
            emitOp("SUB", AC, 1, AC, "op <=");
            emitRM("JLE", AC, 2, PC, "");
            emitRM("LDC", AC, 0, 0, "false case");
            emitRM("LDA", PC, 1, PC, "unconditional jump");
            emitRM("LDC", AC, 1, 0, "true case");
            break;
        case OpExp.GT:
            emitOp("SUB", AC, 1, AC, "op >");
            emitRM("JGT", AC, 2, PC, "");
            emitRM("LDC", AC, 0, 0, "false case");
            emitRM("LDA", PC, 1, PC, "unconditional jump");
            emitRM("LDC", AC, 1, 0, "true case");
            break;
        case OpExp.GE:
            emitOp("SUB", AC, 1, AC, "op >=");
            emitRM("JGE", AC, 2, PC, "");
            emitRM("LDC", AC, 0, 0, "false case");
            emitRM("LDA", PC, 1, PC, "unconditional jump");
            emitRM("LDC", AC, 1, 0, "true case");
            break;
        case OpExp.EQ:
            emitOp("SUB", AC, 1, AC, "op ==");
            emitRM("JEQ", AC, 2, PC, "");
            emitRM("LDC", AC, 0, 0, "false case");
            emitRM("LDA", PC, 1, PC, "unconditional jump");
            emitRM("LDC", AC, 1, 0, "true case");
            break;
        case OpExp.NE:
            emitOp("SUB", AC, 1, AC, "op !=");
            emitRM("JNE", AC, 2, PC, "");
            emitRM("LDC", AC, 0, 0, "false case");
            emitRM("LDA", PC, 1, PC, "unconditional jump");
            emitRM("LDC", AC, 1, 0, "true case");
            break;
        }
        generateCode("* <- op\n");
    }

    /*
     * AssignExp
     */
    public void codeGen(AssignExp exp, int offset) {
        generateCode("* -> op\n");

        if (exp.lhs instanceof SimpleVar) {
            codeGen((SimpleVar) exp.lhs, offset, true);
            emitRM("ST", AC, offset, FP, "op: push left");
            offset--;
        } else if (exp.lhs instanceof IndexVar) {
            codeGen((IndexVar) exp.lhs, offset, false);
            offset--;
        }

        if (exp.rhs instanceof IntExp) {
            codeGen(exp.rhs, offset, false);
        } else if (exp.rhs instanceof VarExp) {
            codeGen(exp.rhs, offset, false);
        } else if (exp.rhs instanceof CallExp) {
            codeGen(exp.rhs, offset, false);
        } else if (exp.rhs instanceof OpExp) {
            codeGen(exp.rhs, offset, false);
        }

        offset++;
        emitRM("LD", 1, offset, FP, "op: load left");
        emitRM("ST", AC, 0, 1, "assign: store value");
        generateCode("* <- op\n");
    }

    /*
     * IfExp
     */
    public void codeGen(IfExp exp, int offset) {
        analyzer.enterScope();
        generateCode("* -> if\n");
        codeGen(exp.test, offset, false);
        int savedLoc1 = emitSkip(1);
        codeGen(exp.thenpart, offset, false);
        int savedLoc2 = emitSkip(0);
        if (savedLoc1 > highLoc) {
            generateCode("* Bug in emmitBackup\n");
        }
        emitLoc = savedLoc1;
        emitRMAbs("JEQ", 0, savedLoc2, "if: jump to else part");
        emitLoc = highLoc;
        codeGen(exp.elsepart, offset, false);
        generateCode("* <- if\n");
        analyzer.exitScope();
    }

    /*
     * WhileExp
     */
    public void codeGen(WhileExp exp, int offset) {
        analyzer.enterScope();
        generateCode("* -> While\n");
        generateCode("* While: jump after body comes back here\n");
        int savedLoc3 = emitSkip(0);
        codeGen(exp.test, offset, false);
        int savedLoc1 = emitSkip(1);
        codeGen(exp.body, offset, false);
        emitRMAbs("LDA", PC, savedLoc3, "While: absolute jmp to test");
        int savedLoc2 = emitSkip(0);
        if (savedLoc1 > highLoc) {
            generateCode("* Bug in emmitBackup\n");
        }
        emitLoc = savedLoc1;
        emitRMAbs("JEQ", 0, savedLoc2, "While: jmp to end");
        emitLoc = highLoc;
        generateCode("* <- While\n");
        analyzer.exitScope();
    }

    /*
     * ReturnExp
     */
    public void codeGen(ReturnExp exp, int offset) {
        generateCode("* -> return\n");
        codeGen(exp.exp, offset, false);
        emitRM("LD", PC, -1, FP, "return to caller");
        generateCode("* <- return\n");
    }

    /*
     * CompoundExp
     */
    public int codeGen(CompoundExp exp, int offset) {
        generateCode("* -> compound statement\n");
        offset = codeGen(exp.decs, offset, false);
        codeGen(exp.exps, offset);
        generateCode("* <- compound statement\n");
        return offset;
    }

    /*
     * Exp
     */
    public int codeGen(Exp exp, int offset, boolean isAddr) {
        if (exp instanceof NilExp) {

        } else if (exp instanceof VarExp) {
            codeGen((VarExp) exp, offset, isAddr);
        } else if (exp instanceof CallExp) {
            codeGen((CallExp) exp, offset);
        } else if (exp instanceof OpExp) {
            codeGen((OpExp) exp, offset);
        } else if (exp instanceof AssignExp) {
            codeGen((AssignExp) exp, offset);
        } else if (exp instanceof IfExp) {
            codeGen((IfExp) exp, offset);
        } else if (exp instanceof WhileExp) {
            codeGen((WhileExp) exp, offset);
        } else if (exp instanceof ReturnExp) {
            codeGen((ReturnExp) exp, offset);
        } else if (exp instanceof CompoundExp) {
            offset = codeGen((CompoundExp) exp, offset);
        } else if (exp instanceof IntExp) {
            codeGen((IntExp) exp);
        }
        return offset;
    }

    /*
     * FunctionDec
     */
    public void codeGen(FunctionDec dec) {
        int offset = -2;

        generateCode("* -> fundecl\n");
        generateCode("* processing function: \n");
        generateCode("* jump around functions body here\n");

        int savedLoc1 = emitSkip(1);
        FunctionSymbol fun = new FunctionSymbol(NameTy.INT, dec.func, null, emitLoc);
        analyzer.addSymbol(dec.func, fun);

        analyzer.enterScope();
        emitRM("ST", 0, -1, FP, "store return");
        offset = codeGen(dec.params, offset, true);
        offset = codeGen(dec.body, offset, false);
        emitRM("LD", PC, -1, FP, "return caller");
        int savedLoc2 = emitSkip(0);
        if (savedLoc1 > highLoc) {
            generateCode("* Bug in emmitBackup\n");
        }
        emitLoc = savedLoc1;
        emitRMAbs("LDA", PC, savedLoc2, "Jump around function body");
        emitLoc = highLoc;
        generateCode("* <- fundecl\n");

        analyzer.exitScope();
    }

    /*
     * VarDec
     */
    public int codeGen(VarDec dec, int offset, boolean isParam) {
        if (isParam) {
            if (dec instanceof SimpleDec) {
                SimpleDec simpleDec = (SimpleDec) dec;
                VarSymbol varSymbol = new VarSymbol(NameTy.INT, simpleDec.name, offset);
                offset--;
                analyzer.addSymbol(simpleDec.name, varSymbol);
            } else if (dec instanceof ArrayDec) {
                ArrayDec arrayDec = (ArrayDec) dec;
                ArraySymbol arraySymbol = new ArraySymbol(NameTy.INT, arrayDec.name, 1, offset);
                offset--;
                analyzer.addSymbol(arrayDec.name, arraySymbol);
            }
        } else {
            if (dec instanceof SimpleDec) {
                SimpleDec simpleDec = (SimpleDec) dec;
                VarSymbol varSymbol = new VarSymbol(NameTy.INT, simpleDec.name, offset);
                offset--;
                analyzer.addSymbol(simpleDec.name, varSymbol);
                generateCode("* processing local var: " + simpleDec.name + "\n");
            } else if (dec instanceof ArrayDec) {
                ArrayDec arrayDec = (ArrayDec) dec;
                offset -= arrayDec.size.value - 1;
                ArraySymbol arraySymbol = new ArraySymbol(NameTy.INT, arrayDec.name, arrayDec.size.value, offset);
                offset--;
                analyzer.addSymbol(arrayDec.name, arraySymbol);
                generateCode("* processing local var: " + arrayDec.name + "\n");
            }
        }
        return offset;
    }

    /*
     * Dec
     */
    public void codeGen(Dec dec) {
        if (dec instanceof FunctionDec) {
            codeGen((FunctionDec) dec);
        } else if (dec instanceof VarDec) {
            VarDec varDec = (VarDec) dec;
            if (varDec instanceof SimpleDec) {
                SimpleDec simpleDec = (SimpleDec) varDec;
                VarSymbol varSymbol = new VarSymbol(NameTy.INT, simpleDec.name, this.offset);
                analyzer.addSymbol(simpleDec.name, varSymbol);
                generateCode("* Allocating global var: " + simpleDec.name + "\n");
                generateCode("* <- vardecl\n");
                this.offset--;
            } else if (varDec instanceof ArrayDec) {
                ArrayDec arrayDec = (ArrayDec) varDec;
                ArraySymbol arraySymbol = new ArraySymbol(NameTy.INT, arrayDec.name, arrayDec.size.value, this.offset - (arrayDec.size.value - 1));
                analyzer.addSymbol(arrayDec.name, arraySymbol);
                generateCode("* Allocating global var: " + arrayDec.name + "\n");
                generateCode("* <- vardecl\n");
                this.offset = this.offset - arrayDec.size.value;
            }
        }
    }

    /*
     * VarDecList
     */
    public int codeGen(VarDecList list, int offset, boolean isParam) {
        while (list != null) {
            if (list.head != null) {
                offset = codeGen(list.head, offset, isParam);
            }
            list = list.tail;
        }
        return offset;
    }

    /*
     * ExpList
     */
    public void codeGen(ExpList list, int offset) {
        while (list != null) {
            if (list.head != null) {
                codeGen(list.head, offset, false);
            }
            list = list.tail;
        }
    }

    public int emitSkip(int dist) {
        int loc = emitLoc;
        emitLoc += dist;
        if (highLoc < emitLoc) {
            highLoc = emitLoc;
        }
        return loc;
    }

    public void emitRM(String op, int p1, int p2, int p3, String comment) {
        generateCode(String.format("%d:  %s  %d,%d(%d)\t%s\n", emitLoc, op, p1, p2, p3, comment));
        emitLoc++;
        if (highLoc < emitLoc) {
            highLoc = emitLoc;
        }
    }

    public void emitRMAbs(String op, int p1, int p2, String comment) {
        generateCode(String.format("%d:  %s  %d,%d(%d)\t%s\n", emitLoc, op, p1, p2 - emitLoc - 1, PC, comment));
        emitLoc++;
        if (highLoc < emitLoc) {
            highLoc = emitLoc;
        }
    }

    public void emitOp(String op, int p1, int p2, int p3, String comment) {
        generateCode(String.format("%d:  %s %d,%d,%d\t%s\n", emitLoc, op, p1, p2, p3, comment));
        emitLoc++;
    }

    public void generateCode(String data) {
        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(this.filename, true));
            writer.print(data);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}