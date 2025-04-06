package frontend.symbol.symbols.Func;

import frontend.lexer.Token;
import frontend.symbol.symbols.Array.CharArray;
import frontend.symbol.symbols.Array.IntArray;
import frontend.symbol.symbols.Symbol;
import utils.ErrorType;
import utils.ParamType;
import utils.TokenType;

import java.util.ArrayList;

public class IntFunc extends Symbol implements Func {
    ArrayList<Symbol> params;
    ArrayList<Integer> paramLineNos;
    ArrayList<ParamType> paramTypes;
    private boolean isParamsAlreadySet = false;
    private TokenType returnType;

    public IntFunc(String content) {
        super(content);
        this.params = new ArrayList<>();
        this.paramLineNos = new ArrayList<>();
        this.paramTypes = new ArrayList<>();
    }

    @Override
    public String getSymbolInfo() {
        return content + " IntFunc";
    }

    @Override
    public boolean isParamAlreadyAddedIntoSymbolTable() {
        return isParamsAlreadySet;
    }

    @Override
    public void addParamAndLineNo(Symbol param, int lineNo) {
        params.add(param);
        paramLineNos.add(lineNo);
        if (param instanceof CharArray) {
            paramTypes.add(ParamType.CHAR_ARRAY);
        } else if (param instanceof IntArray) {
            paramTypes.add(ParamType.INT_ARRAY);
        } else {
            paramTypes.add(ParamType.VAR);
        }
    }

    @Override
    public ErrorType checkParams(ArrayList<ParamType> paramTypes) {
        if (this.paramTypes.size() != paramTypes.size()) {
            return ErrorType.d;
        }
        for (int i = 0; i < paramTypes.size(); i++) {
            if (paramTypes.get(i) != this.paramTypes.get(i)) {
                return ErrorType.e;
            }
        }
        return null;
    }

    @Override
    public ArrayList<Symbol> getSymbolOfParam() {
        isParamsAlreadySet = true;
        return params;
    }

    @Override
    public ArrayList<Integer> getLineNoOfParam() {
        return paramLineNos;
    }

    @Override
    public void setReturnType(TokenType returnType) {
        this.returnType = returnType;
    }

    @Override
    public TokenType getReturnType() {
        return returnType;
    }
}
