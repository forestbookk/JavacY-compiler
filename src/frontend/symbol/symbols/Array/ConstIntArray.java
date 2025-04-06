package frontend.symbol.symbols.Array;

import frontend.symbol.symbols.Symbol;
import utils.ParamType;

public class ConstIntArray extends Symbol implements Array{
    public ConstIntArray(String content) {
        super(content);
    }

    @Override
    public String getSymbolInfo() {
        return content+" ConstIntArray";
    }
    @Override
    public ParamType getParamType() {
        return ParamType.INT_ARRAY;
    }
}
