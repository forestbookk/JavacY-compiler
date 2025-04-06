package frontend.symbol.symbols.Array;

import frontend.symbol.symbols.Symbol;
import utils.ParamType;

public class CharArray extends Symbol implements Array {
    public CharArray(String content) {
        super(content);
    }

    @Override
    public String getSymbolInfo() {
        return content + " CharArray";
    }

    @Override
    public ParamType getParamType() {
        return ParamType.CHAR_ARRAY;
    }
}
