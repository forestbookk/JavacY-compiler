package frontend.symbol.symbols.Var;

import frontend.symbol.symbols.Symbol;

public class Char extends Symbol {
    public Char(String content) {
        super(content);
    }

    @Override
    public String getSymbolInfo() {
        return content+" Char";
    }
}
