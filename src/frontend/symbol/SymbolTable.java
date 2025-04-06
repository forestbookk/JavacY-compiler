package frontend.symbol;

import frontend.symbol.symbols.Symbol;

import java.util.ArrayList;
import java.util.HashSet;

public class SymbolTable {
    private int index;
    private HashSet<String> symbolNameHashSet; // for search
    private ArrayList<Symbol> symbols; // in order
    private static boolean inLoop;

    public SymbolTable(int index) {
        this.index = index;
        this.symbols = new ArrayList<>();
        this.symbolNameHashSet = new HashSet<>();
    }


    public boolean tryAddSymbol(Symbol symbol) {
        if (symbolNameHashSet.contains(symbol.content)) {
            return false;
        }
        symbols.add(symbol);
        symbolNameHashSet.add(symbol.content);
        return true;
    }

    public ArrayList<String> getAllSymbols() {
        ArrayList<String> res = new ArrayList<>();
        for (Symbol symbol : symbols) {
            res.add(index + " " + symbol.getSymbolInfo());
        }
        return res;
    }

    public void setInLoop(boolean inLoop) {
        SymbolTable.inLoop = inLoop;
    }

    public boolean isInLoop() {
        return inLoop;
    }

    public HashSet<String> getSymbolNameHashSet() {
        return symbolNameHashSet;
    }
}
