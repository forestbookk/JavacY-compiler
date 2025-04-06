package frontend.symbol.symbols.Var;

import frontend.symbol.symbols.Symbol;

public class ConstChar extends Symbol {
    public ConstChar(String content) {
        super(content);
    }
    @Override
    public String getSymbolInfo() {
        return content+" ConstChar";
    }
}
