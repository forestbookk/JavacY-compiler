package frontend.symbol.symbols.Array;

import frontend.symbol.symbols.Symbol;
import utils.ParamType;

public class IntArray extends Symbol implements Array {
    public IntArray(String content) {
        super(content);
    }
    @Override
    public String getSymbolInfo() {
        return content + " IntArray";
    }

    @Override
    public ParamType getParamType() {
        return ParamType.INT_ARRAY;
    }
}
