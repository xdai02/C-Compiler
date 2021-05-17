package symbol;

import java.util.ArrayList;

public class FunctionSymbol extends SymbolNode {
	public ArrayList<SymbolNode> params;
	public int addr;

	public FunctionSymbol(int type, String id, ArrayList<SymbolNode> params) {
		this.type = type;
		this.id = id;
		this.params = params;
	}

	public FunctionSymbol(int type, String id, ArrayList<SymbolNode> params, int addr) {
		this.type = type;
		this.id = id;
		this.params = params;
		this.addr = addr;
	}
}