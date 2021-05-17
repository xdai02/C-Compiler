package absyn;

import java.io.PrintWriter;
import java.io.FileNotFoundException;

abstract public class Absyn {
    public static final int SPACES = 4;
    public static String outFileName = "";
    public static boolean noErrors = true;
    public static PrintWriter writer;
    public int pos;

    private static void indent(int level) {
        for (int i = 0; i < level * SPACES; i++) {
            writer.print(" ");
		}
    }

	/*
		NameTy
	*/
    public static void showTree(NameTy nameTy, int level) {
        if (nameTy.type == NameTy.INT) {
            writer.println("(int)");
		} else {
            writer.println("(void)");
		}
    }

	/*
		SimpleVar
	*/
    public static void showTree(SimpleVar var, int level) {
        indent(level);
        writer.println("SimpleVar: " + var.name);
    }

	/*
		IndexVar
	*/
    public static void showTree(IndexVar var, int level) {
        indent(level);
        writer.println("IndexVar: " + var.name);
		level++;
        showTree(var.index, level);
    }

    /*
        Var
    */
    public static void showTree(Var var, int level) {
        if (var instanceof SimpleVar) {
            showTree((SimpleVar) var, level);
        } else if (var instanceof IndexVar) {
            showTree((IndexVar) var, level);
        }
    }
	
	/*
		NilExp
	*/
    public static void showTree(NilExp exp, int level) {

    }

	/*
		VarExp
	*/
    public static void showTree(VarExp exp, int level) {
        indent(level);
        writer.println("VarExp:");
        level++;
        showTree(exp.name, level);
    }

	/*
		IntExp
	*/
    public static void showTree(IntExp exp, int level) {
        if (exp != null) {
            indent(level);
            writer.println("IntExp: " + exp.value);
        }
    }

    /*
		CallExp
	*/
    public static void showTree(CallExp exp, int level) {
        indent(level);
        writer.println("CallExp: " + exp.func);
        level++;
        showTree(exp.args, level);
    }

	/*
		OpExp
	*/
    public static void showTree(OpExp exp, int level) {
        indent(level);

        writer.print("OpExp: ");
        switch (exp.op) {
        case OpExp.ADD:
            writer.println("+");
            break;
        case OpExp.SUB:
            writer.println("-");
            break;
        case OpExp.MUL:
            writer.println("*");
            break;
        case OpExp.DIV:
            writer.println("/");
            break;
        case OpExp.ASSN:
            writer.println("=");
            break;
        case OpExp.LT:
            writer.println("<");
            break;
        case OpExp.LE:
            writer.println("<=");
            break;
        case OpExp.GT:
            writer.println(">");
            break;
        case OpExp.GE:
            writer.println(">=");
            break;
		case OpExp.EQ:
            writer.println("==");
            break;
        case OpExp.NE:
            writer.println("!=");
            break;
        default:
            System.err.println(String.format("[Error at line %d] undeclared operator.", exp.pos));
            noErrors = false;
            break;
        }

        level++;
        showTree(exp.left, level);
        showTree(exp.right, level);
    }

	/*
		AssignExp
	*/
    public static void showTree(AssignExp exp, int level) {
        indent(level);
        writer.println("AssignExp:");
        level++;
        showTree(exp.lhs, level);
        showTree(exp.rhs, level);
    }

	/*
		IfExp
	*/
    public static void showTree(IfExp exp, int level) {
        indent(level);
        writer.println("IfExp:");
        level++;
        showTree(exp.test, level);
        showTree(exp.thenpart, level);
        showTree(exp.elsepart, level);
    }

	/*
		WhileExp
	*/
    public static void showTree(WhileExp exp, int level) {
        indent(level);
        writer.println("WhileExp:");
        level++;
        showTree(exp.test, level);
        showTree(exp.body, level);
    }

	/*
		ReturnExp
	*/
    public static void showTree(ReturnExp exp, int level) {
        indent(level);
        writer.println("ReturnExp:");
        level++;
        if (exp.exp != null) {
            showTree(exp.exp, level);
        }
    }

	/*
		CompoundExp
	*/
    public static void showTree(CompoundExp exp, int level) {
        indent(level);
        writer.println("CompoundExp:");
        level++;
        showTree(exp.decs, level);
        showTree(exp.exps, level);
    }

    /*
        Exp
    */
    public static void showTree(Exp exp, int level) {
        if (exp instanceof NilExp) {
            showTree((NilExp) exp, level);
        } else if (exp instanceof VarExp) {
            showTree((VarExp) exp, level);
        } else if (exp instanceof VarExp) {
            showTree((VarExp) exp, level);
        } else if (exp instanceof CallExp) {
            showTree((CallExp) exp, level);
        } else if (exp instanceof OpExp) {
            showTree((OpExp) exp, level);
        } else if (exp instanceof AssignExp) {
            showTree((AssignExp) exp, level);
        } else if (exp instanceof IfExp) {
            showTree((IfExp) exp, level);
        } else if (exp instanceof WhileExp) {
            showTree((WhileExp) exp, level);
        } else if (exp instanceof ReturnExp) {
            showTree((ReturnExp) exp, level);
        } else if (exp instanceof CompoundExp) {
            showTree((CompoundExp) exp, level);
        } else if (exp instanceof IntExp) {
            showTree((IntExp) exp, level);
        } else {
            indent(level);
            System.err.println(String.format("[Error at line %d] illegal expression.", ((ErrorExp) exp).pos));
            noErrors = false;
        }
    }

	/*
		FunctionDec
	*/
    public static void showTree(FunctionDec dec, int level) {
        indent(level);
        writer.print("FunctionDec: " + dec.func + " ");
        showTree(dec.result, level);
        level++;
        indent(level);
        writer.println("Params:");
        level++;
        showTree(dec.params, level);
        level--;
        showTree(dec.body, level);
    }

	/*
		SimpleDec
	*/
    public static void showTree(SimpleDec dec, int level) {
        indent(level);
        writer.print("SimpleDec: " + dec.name + " ");
        showTree(dec.type, level);
    }

	/*
		ArrayDec
	*/
    public static void showTree(ArrayDec dec, int level) {
        indent(level);
        if (dec.size != null) {
            writer.print(String.format("ArrayDec: %s[%d] ", dec.name, dec.size.value));
            showTree(dec.type, level);
		} else {
            writer.print("ArrayDec: " + dec.name + "[] ");
            showTree(dec.type, level);
		}
    }

    /*
        VarDec
    */
    public static void showTree(VarDec dec, int level) {
        if (dec instanceof SimpleDec) {
            showTree((SimpleDec) dec, level);
        } else if (dec instanceof ArrayDec) {
            showTree((ArrayDec) dec, level);
        } else {
            indent(level);
            System.err.println(String.format("[Error at line %d] illegal expression.", ((ErrorVarDec) dec).pos));
            noErrors = false;
        }
    }

    /*
        Dec
    */
    public static void showTree(Dec dec, int level) {
        if (dec instanceof FunctionDec) {
            showTree((FunctionDec) dec, level);
        } else if (dec instanceof VarDec) {
            showTree((VarDec) dec, level);
        } else {
            indent(level);
            System.err.println(String.format("[Error at line %d] illegal expression.", ((ErrorDec) dec).pos));
            noErrors = false;
        }
    }

	/*
		DecList
	*/
	public static void showTree(DecList list, int level, String filename) {
        try {
            writer = new PrintWriter(filename + ".ast");
        } catch (FileNotFoundException e) {
            System.err.println("[Error] failed to create .ast file.");
            e.printStackTrace();
        }

        while (list != null) {
            if (list.head != null) {
                showTree(list.head, level);
            }
            list = list.tail;
        }
        writer.close();
    }

	/*
		VarDecList
	*/
    public static void showTree(VarDecList list, int level) {
        while (list != null) {
            if (list.head != null) {
                showTree(list.head, level);
            }
            list = list.tail;
        }
    }

	/*
		ExpList
	*/
    public static void showTree(ExpList list, int level) {
        while (list != null) {
            if (list.head != null) {
                showTree(list.head, level);
            }
            list = list.tail;
        }
    }
}