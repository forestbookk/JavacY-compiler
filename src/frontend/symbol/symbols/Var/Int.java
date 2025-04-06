package frontend.symbol.symbols.Var;

import frontend.symbol.symbols.Symbol;

public class Int extends Symbol {
    public Int(String content) {
        super(content);
    }

    @Override
    public String getSymbolInfo() {
        return content + " Int";
    }
}
