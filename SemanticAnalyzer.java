import absyn.*;
import symbol.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

public class SemanticAnalyzer {
    public static final int SPACES = 4;
    public static final int NOT_FOUND = -1;
    public static boolean showSYM;
    public String filename;
    public PrintWriter writer;
    private ArrayList<HashMap<String, SymbolNode>> symbolTable;

    public SemanticAnalyzer(boolean showSYM, String filename) {
        this.showSYM = showSYM;
        this.filename = filename;
        this.writer = null;
        symbolTable = new ArrayList<HashMap<String, SymbolNode>>();

        if (this.showSYM) {
            try {
                writer = new PrintWriter(filename);
            } catch (FileNotFoundException e) {
                System.err.println("[Error] failed to create .sym file.");
                e.printStackTrace();
            }
        }
    }

    public void indent(int level) {
        for (int i = 0; i < level * SPACES; i++) {
            writer.print(" ");
        }
    }

    /*
        Add a symbol to the symbol table
    */
    public void addSymbol(String id, SymbolNode symbol) {
        symbolTable.get(symbolTable.size() - 1).put(id, symbol);
    }

    /*
        Print all symbols in a scope
    */
    public void printScope() {
        Set<String> keys = symbolTable.get(symbolTable.size() - 1).keySet();
        for (String key : keys) {
            SymbolNode symbol = symbolTable.get(symbolTable.size() - 1).get(key);
            if (symbol instanceof VarSymbol) {
                indent(symbolTable.size() - 1);
                writer.print(key);
                if(symbol.type == NameTy.INT) {
                    writer.println(" (int)");
                } else {
                    writer.println(" (void)");
                }
            } else if (symbol instanceof ArraySymbol) {
                ArraySymbol arrSymbol = (ArraySymbol) symbol;
                indent(symbolTable.size() - 1);
                writer.print(key + "[" + arrSymbol.size + "]");
                if(symbol.type == NameTy.INT) {
                    writer.println(" (int)");
                } else {
                    writer.println(" (void)");
                }
            } else if (symbol instanceof FunctionSymbol) {
                indent(symbolTable.size() - 1);
                if(symbol.type == NameTy.INT) {
                    writer.print("int ");
                } else {
                    writer.print("void ");
                }
                writer.print(key + " ( ");
                FunctionSymbol funcSymbol = (FunctionSymbol) symbol;
                for (SymbolNode sym : funcSymbol.params) {
                    if (sym instanceof VarSymbol) {
                        if (sym.type == NameTy.INT) {
                            writer.print("int");
                        } else {
                            writer.print("void");
                        }
                    } else if (sym instanceof ArraySymbol) {
                        if (sym.type == NameTy.INT) {
                            writer.print("int[]");
                        } else {
                            writer.print("void[]");
                        }
                    }
                    writer.print(" ");
                }
                writer.println(")");
            }
        }
    }

    /*
        Enter a new scope
    */
    public void enterScope() {
        // create a new hashmap for the new scope
        symbolTable.add(new HashMap<String, SymbolNode>());
        if (showSYM) {
            indent(symbolTable.size() - 1);
            writer.println("Entering a new block:");
        }
    }

    /*
        Leaving a scope
    */
    public void exitScope() {
        if (showSYM) {
            // print all symbols in current scope
            printScope();
            indent(symbolTable.size() - 1);
            writer.println("Leaving the block");
        }
        // remove current scope from the symbol table
        symbolTable.remove(symbolTable.size() - 1);
    }

    /*
        Determine whether a symbol exists
    */
    public int symbolExists(String symbol) {
        // find from inner to outer scope
        int size = symbolTable.size() - 1;
        for (int i = size; i >= 0; i--) {
            if (symbolTable.get(i).containsKey(symbol)) {
                return i;
            }
        }
        return NOT_FOUND;
    }

    /*
        Get a symbol in symbol table
    */
    public SymbolNode getSymbol(String symbol) {
        int size = symbolTable.size() - 1;
        for (int i = size; i >= 0; i--) {
            if (symbolTable.get(i).containsKey(symbol)) {
                return symbolTable.get(i).get(symbol);
            }
        }
        return null;
    }

    /*
        Get current function symbol
    */
    public SymbolNode getFunction(String symbol) {
        if (symbolTable.get(0).containsKey(symbol)) {
            return symbolTable.get(0).get(symbol);
        }
        return null;
    }

    /*
        Determine whether a symbol exists in current scope
    */
    public boolean inCurrentScope(String symbol) {
        return symbolTable.get(symbolTable.size() - 1).containsKey(symbol);
    }
}