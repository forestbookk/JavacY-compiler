package frontend.symbol.symbols.Var;

import frontend.symbol.symbols.Symbol;

public class ConstInt extends Symbol {
    public ConstInt(String content) {
        super(content);
    }
    @Override
    public String getSymbolInfo() {
        return content+" ConstInt";
    }
}
