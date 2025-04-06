package frontend.symbol.symbols.Array;

import frontend.symbol.symbols.Symbol;
import utils.ParamType;

public class ConstCharArray extends Symbol implements Array{
    public ConstCharArray(String content) {
        super(content);
    }

    @Override
    public String getSymbolInfo() {
        return content+" ConstCharArray";
    }
    @Override
    public ParamType getParamType() {
        return ParamType.CHAR_ARRAY;
    }
}
