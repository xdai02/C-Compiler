package absyn;

public class ArrayDec extends VarDec {
	public IntExp size;

	public ArrayDec(int pos, NameTy type, String name, IntExp size) {
		this.pos = pos;
		this.type = type;
		this.name = name;
		this.size = size;
	}
}