import absyn.*;
import symbol.*;
import java.util.ArrayList;

public class TypeCheck {
    public static boolean noErrors = true;
    private static boolean hasMain = false;
    private SemanticAnalyzer analyzer;
    private DecList program;
    private int returnType;

    public TypeCheck(DecList program, boolean genSYM, String filename) {
        this.program = program;
        filename += CM.EXT_SYM;
        analyzer = new SemanticAnalyzer(genSYM, filename);
    }

    public void start() {
        checkTypes(program);
    }

    public void close() {
        if(analyzer.writer != null) {
            analyzer.writer.close();
        }
    }

    /*
        SimpleVar
    */
    public void checkTypes(SimpleVar var) {
        if (analyzer.symbolExists(var.name) != SemanticAnalyzer.NOT_FOUND) {
            if (analyzer.getSymbol(var.name) instanceof VarSymbol) {
                // all variables should be int
                if (analyzer.getSymbol(var.name).type != NameTy.INT) {
                    System.err.println(String.format("[Error at line %d] invalid variable type.", var.pos + 1));
                    this.noErrors = false;
                }
            } else {
                System.err.println(String.format("[Error at line %d] invalid array type.", var.pos + 1));
                this.noErrors = false;
            }
        } else {
            System.err.println(String.format("[Error at line %d] \'%s\' undefined.", var.pos + 1, var.name));
            this.noErrors = false;
        }
    }

    /*
        IndexVar
    */
    public void checkTypes(IndexVar var) {
        if (analyzer.symbolExists(var.name) != SemanticAnalyzer.NOT_FOUND) {
            if (!(analyzer.getSymbol(var.name) instanceof ArraySymbol)) {
                System.err.println(String.format("[Error at line %d] invalid array type.", var.pos + 1));
                this.noErrors = false;
            }
        } else {
            System.err.println(String.format("[Error at line %d] \'%s\' undefined.", var.pos + 1, var.name));
            this.noErrors = false;
        }
        // index of array must be integer
        checkTypes(var.index, true);
    }

    /*
        Var
    */
    public void checkTypes(Var var) {
        if (var instanceof SimpleVar) {
            checkTypes((SimpleVar) var);
        } else if (var instanceof IndexVar) {
            checkTypes((IndexVar) var);
        }
    }

    /*
        VarExp
    */
    public void checkTypes(VarExp exp) {
        checkTypes(exp.name);
    }

    /*
        IntExp
    */
    public void checkTypes(IntExp exp) {

    }

    /*
        CallExp
    */
    public void checkTypes(CallExp exp, boolean isInt) {
        String func = exp.func;
        if (analyzer.getFunction(func) == null
            || !(analyzer.getFunction(func) instanceof FunctionSymbol)) {
            System.err.println(String.format("[Error at line %d] function \'%s\' undecalred.", exp.pos + 1, func));
            this.noErrors = false;
            return;
        } else if (analyzer.getFunction(func).type != NameTy.INT && isInt) {
            System.err.println(String.format("[Error at line %d] function \'%s\' return type mismatch.", exp.pos + 1, func));
            this.noErrors = false;
        }

        FunctionSymbol funcSymbol = (FunctionSymbol) analyzer.getFunction(func);
        if (funcSymbol.params.size() < exp.argsCount()) {
            System.err.println(String.format("[Error at line %d] too many arguments to function \'%s\'.", exp.pos + 1, func));
            this.noErrors = false;
            return;
        } else if (funcSymbol.params.size() > exp.argsCount()) {
            System.err.println(String.format("[Error at line %d] too few arguments to function \'%s\'.", exp.pos + 1, func));
            this.noErrors = false;
            return;
        }

        ExpList funcArgs = exp.args;
        for (int i = 0; i < funcSymbol.params.size(); i++) {
            SymbolNode symbol = funcSymbol.params.get(i);
            Exp e = funcArgs.head;

            if (symbol instanceof VarSymbol) {
                checkTypes(e, true);
            } else if (symbol instanceof ArraySymbol) {
                if ((e instanceof VarExp) && (((VarExp) e)).name instanceof SimpleVar) {
                    String varName = ((SimpleVar) ((VarExp) e).name).name;
                    if (analyzer.symbolExists(varName) != SemanticAnalyzer.NOT_FOUND) {
                        if (!(analyzer.getSymbol(varName) instanceof ArraySymbol)) {
                            System.err.println(String.format("[Error at line %d] invalid array type.", exp.pos + 1));
                            this.noErrors = false;
                        }
                    } else {
                        System.err.println(String.format("[Error at line %d] variable \'%s\' undeclared.invalid array type.", exp.pos + 1, varName));
                        this.noErrors = false;
                    }
                } else {
                    System.err.println(String.format("[Error at line %d] invalid array type.", exp.pos + 1));
                    this.noErrors = false;
                }
            }
            funcArgs = funcArgs.tail;
        }
    }

    /*
        OpExp
    */
    public void checkTypes(OpExp exp) {
        checkTypes(exp.left, true);
        checkTypes(exp.right, true);
    }

    /*
        AssignExp
    */
    public void checkTypes(AssignExp exp) {
        checkTypes(exp.lhs);
        checkTypes(exp.rhs, true);
    }

    /*
        IfExp
    */
    public void checkTypes(IfExp exp) {
        checkTypes(exp.test, true);
        checkTypes(exp.thenpart, false);
        if (exp.elsepart != null) {
            checkTypes(exp.elsepart, false);
        }
    }

    /*
        WhileExp
    */
    public void checkTypes(WhileExp exp) {
        checkTypes(exp.test, true);
        checkTypes(exp.body, false);
    }

    /*
        ReturnExp
    */
    public void checkTypes(ReturnExp exp) {
        if (returnType != NameTy.INT) {
            if (exp.exp != null) {
                System.err.println(String.format("[Error at line %d] function return type mismatch.", exp.pos + 1));
                this.noErrors = false;
                return;
            }
        } else {
            if (exp.exp == null) {
                System.err.println(String.format("[Error at line %d] function return type mismatch.", exp.pos + 1));
                this.noErrors = false;
            } else {
                checkTypes(exp.exp, true);
            }
        }
    }

    /*
        CompoundExp
    */
    public void checkTypes(CompoundExp exp, FunctionSymbol func) {
        boolean hasReturn = false;
        if (func.type == NameTy.INT) {
            hasReturn = true;
        }

        // local declarations
        if (exp.decs != null) {
            checkTypes(exp.decs);
        }
        // statement list
        if (exp.exps != null) {
            checkTypes(exp.exps);
        }
        analyzer.exitScope();
    }

    /*
        CompoundExp
    */
    public void checkTypes(CompoundExp exp) {
        analyzer.enterScope();
        checkTypes(exp.decs);
        checkTypes(exp.exps);
        analyzer.exitScope();
    }

    /*
        Exp
    */
    public void checkTypes(Exp exp, boolean isInt) {
        if (exp instanceof VarExp) {
            checkTypes((VarExp) exp);
        } else if (exp instanceof CallExp) {
            checkTypes((CallExp) exp, isInt);
        } else if (exp instanceof OpExp) {
            checkTypes((OpExp) exp);
        } else if (exp instanceof AssignExp) {
            checkTypes((AssignExp) exp);
        } else if (exp instanceof IfExp) {
            checkTypes((IfExp) exp);
        } else if (exp instanceof WhileExp) {
            checkTypes((WhileExp) exp);
        } else if (exp instanceof ReturnExp) {
            checkTypes((ReturnExp) exp);
        } else if (exp instanceof CompoundExp) {
            checkTypes((CompoundExp) exp);
        } else if (exp instanceof IntExp) {
            checkTypes((IntExp) exp);
        }
    }

    /*
        FunctionDec
    */
    public void checkTypes(FunctionDec dec) {
        String func = dec.func;
        int type = dec.result.type;
        ArrayList<SymbolNode> params = new ArrayList<SymbolNode>();

        if (func.equals("main")) {
            this.hasMain = true;
        }

        // add params to symbol table
        VarDecList list = dec.params;
        while (list != null) {
            if (list.head instanceof ArrayDec) {
                params.add(new ArraySymbol(list.head.type.type, list.head.name, -1));
            } else if (list.head instanceof SimpleDec) {
                params.add(new VarSymbol(list.head.type.type, list.head.name));
            }
            list = list.tail;
        }

        SymbolNode symbol = new FunctionSymbol(type, func, params);
        analyzer.addSymbol(func, symbol);
        this.returnType = type;
        analyzer.enterScope();
        checkTypes(dec.params);
        checkTypes((CompoundExp) dec.body, (FunctionSymbol) symbol);
    }

    /*
        SimpleDec
    */
    public void checkTypes(SimpleDec dec) {
        String name = dec.name;
        int type = dec.type.type;

        if (analyzer.inCurrentScope(name)) {
            System.err.println(String.format("[Error at line %d] redeclaration of variable \'%s\'.", dec.pos + 1, name));
            this.noErrors = false;
            return;
        }

        if (type == NameTy.VOID) {
            System.err.println(String.format("[Errpr at line %d] invalid variable type.", dec.pos + 1));
            this.noErrors = false;
        }

        SymbolNode symbol = new VarSymbol(type, name);
        analyzer.addSymbol(name, symbol);
    }

    /*
        ArrayDec
    */
    public void checkTypes(ArrayDec dec) {
        String name = dec.name;
        int type = dec.type.type;
        int arraySize = -1;
        if (dec.size != null) {
            arraySize = dec.size.value;
        }

        if (analyzer.inCurrentScope(name)) {
            System.err.println(String.format("[Error at line %d] redeclaration of variable \'%s\'.", dec.pos + 1, name));
            this.noErrors = false;
            return;
        }

        if (type == NameTy.VOID) {
            System.err.println(String.format("[Errpr at line %d] invalid variable type.", dec.pos + 1));
            this.noErrors = false;
            return;
        }

        SymbolNode symbol = new ArraySymbol(type, name, arraySize);
        analyzer.addSymbol(name, symbol);
    }

    /*
        VarDec
    */
    public void checkTypes(VarDec dec) {
        if (dec instanceof SimpleDec) {
            checkTypes((SimpleDec) dec);
        } else if (dec instanceof ArrayDec) {
            checkTypes((ArrayDec) dec);
        }
    }

    /*
        Dec
    */
    public void checkTypes(Dec dec) {
        if (dec instanceof VarDec) {
            checkTypes((VarDec) dec);
        } else if (dec instanceof FunctionDec) {
            checkTypes((FunctionDec) dec);
        }
    }

    /*
        DecList
    */
    private void checkTypes(DecList list) {
        analyzer.enterScope();

        SymbolNode input = new FunctionSymbol(NameTy.INT, "input", new ArrayList<SymbolNode>());
        analyzer.addSymbol("input", input);
        ArrayList<SymbolNode> params = new ArrayList<SymbolNode>();
        params.add(new VarSymbol(NameTy.INT, "value"));
        SymbolNode output = new FunctionSymbol(NameTy.VOID, "output", params);
        analyzer.addSymbol("output", output);

        while (list != null) {
            if (list.head != null) {
                checkTypes(list.head);
            }
            list = list.tail;
        }

        if (!this.hasMain) {
            System.err.println("[Error] main() not found.");
            this.noErrors = false;
        }
        analyzer.exitScope();
    }
    
    /*
        VarDecList
    */
    private void checkTypes(VarDecList list) {
        while (list != null) {
            if (list.head != null) {
                checkTypes(list.head);
            }
            list = list.tail;
        }
    }

    /*
        ExpList
    */
    private void checkTypes(ExpList list) {
        while (list != null) {
            if (list.head != null) {
                checkTypes(list.head, false);
            }
            list = list.tail;
        }
    }
}