package absyn;

public class OpExp extends Exp {
    public static final int ADD = 0;
    public static final int SUB = 1;
    public static final int MUL = 2;
    public static final int DIV = 3;
    public static final int ASSN = 4;
    public static final int LT = 5;
    public static final int LE = 6;
    public static final int GT = 7;
    public static final int GE = 8;
    public static final int EQ = 9;
    public static final int NE = 10;

    public Exp left;
    public int op;
    public Exp right;

    public OpExp(int pos, Exp left, int op, Exp right) {
        this.pos = pos;
        this.left = left;
        this.op = op;
        this.right = right;
    }
}
