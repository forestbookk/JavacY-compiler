package frontend.symbol;

import frontend.symbol.symbols.Func.Func;
import frontend.symbol.symbols.Symbol;
import utils.ErrorType;
import utils.Printer;
import utils.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class SymbolManager {
    private static boolean isGlobal = true;
    public static Func lastFunc;
    public static TokenType fType;
    public static TokenType bType;
    public static boolean inLoopForNextBlock;
    public static int curIndex;
    private static HashMap<Integer, SymbolTable> symbolTableHashMap; // for output
    private static Stack<SymbolTable> symbolTables;
    private static HashMap<String, Stack<Symbol>> globalNameStack;

    static {
        symbolTableHashMap = new HashMap<>();
        symbolTables = new Stack<>();
        curIndex = 1;
        SymbolTable firstSymbolTable = new SymbolTable(curIndex);
        symbolTableHashMap.put(curIndex, firstSymbolTable);
        symbolTables.push(firstSymbolTable);
        globalNameStack = new HashMap<>();
    }

    public static boolean isGlobal() {
        return isGlobal;
    }

    public static void setGlobal(boolean isGlobal) {
        SymbolManager.isGlobal = isGlobal;
    }

    public static void setLastFunc(Func func) {
        SymbolManager.lastFunc = func;
    }

    public static void setFType(TokenType fType) {
        SymbolManager.fType = fType;
    }

    public static void setBType(TokenType bType) {
        SymbolManager.bType = bType;
    }

    public static boolean tryAddSymbol(Symbol symbol) {
        SymbolTable symbolTable = symbolTables.peek();
        boolean res = symbolTable.tryAddSymbol(symbol);
        if (res) {
            if (globalNameStack.containsKey(symbol.content)) {
                globalNameStack.get(symbol.content).push(symbol);
            } else {
                Stack<Symbol> s = new Stack<>();
                s.push(symbol);
                globalNameStack.put(symbol.content, s);
            }
        }
        return res;
    }

    public static void dealFuncFParams() {
        ArrayList<Integer> paramLineNo = lastFunc.getLineNoOfParam();
        ArrayList<Symbol> paramSymbol = lastFunc.getSymbolOfParam();
        for (int i = 0; i < paramLineNo.size(); i++) {
            /* check error */
            // b: 名字重复定义
            if (!tryAddSymbol(paramSymbol.get(i))) {
                Printer.addError(paramLineNo.get(i), ErrorType.b);
            }
        }
    }

    public static void enterNewSymbolTable() {
        SymbolTable newSymbolTable = new SymbolTable(++curIndex);
        symbolTables.push(newSymbolTable);
        symbolTableHashMap.put(curIndex, newSymbolTable);
        if (inLoopForNextBlock) {
            newSymbolTable.setInLoop(true);
            inLoopForNextBlock = false;
        }
    }

    public static void exitSymbolTable() {
        SymbolTable symbolTable = symbolTables.pop();
        for (String name : symbolTable.getSymbolNameHashSet()) {
            if (globalNameStack.containsKey(name)) {
                globalNameStack.get(name).pop();
                if (globalNameStack.get(name).isEmpty()) {
                    globalNameStack.remove(name);
                }
            }
        }
    }

    /* probably null */
    public static Symbol getSymbolByNameInGlobalStack(String name) {
        if (globalNameStack.containsKey(name) &&
                !globalNameStack.get(name).isEmpty()) {
            return globalNameStack.get(name).peek();
        }
        return null;
    }

    public static ArrayList<String> getAllSymbols() {
        ArrayList<String> res = new ArrayList<>();
        int i = 1;
        while (true) {
            if (symbolTableHashMap.containsKey(i)) {
                res.addAll(symbolTableHashMap.get(i).getAllSymbols());
            } else {
                break;
            }
            i++;
        }
        return res;
    }

    public static boolean isLastSymbolTableInLoop() {
        return symbolTables.peek().isInLoop();
    }

    public static void setLoopForNextBlock(boolean inLoop) {
        inLoopForNextBlock = inLoop;
    }
}
