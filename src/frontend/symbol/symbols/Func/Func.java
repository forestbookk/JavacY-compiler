package frontend.symbol.symbols.Func;

import frontend.symbol.symbols.Symbol;
import utils.ErrorType;
import utils.ParamType;
import utils.TokenType;

import java.util.ArrayList;

public interface Func {
    /* probably null */
    public ErrorType checkParams(ArrayList<ParamType> paramTypes);

    public void addParamAndLineNo(Symbol param, int lineNo);

    public boolean isParamAlreadyAddedIntoSymbolTable();

    public ArrayList<Symbol> getSymbolOfParam();

    public ArrayList<Integer> getLineNoOfParam();

    public void setReturnType(TokenType returnType);

    public TokenType getReturnType();
}
