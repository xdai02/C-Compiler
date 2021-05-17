package absyn;

public class NameTy extends Absyn{
    public static final int INT = 0;
    public static final int VOID = 1;
    public int type;
 
    public NameTy(int pos, int type) {
        this.pos = pos;
        this.type = type;
    }
}