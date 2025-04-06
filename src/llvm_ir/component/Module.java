package llvm_ir.component;

import llvm_ir.component.function.Function;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Module {
    private final ArrayList<Function> functionList;
    private final ArrayList<GlobalVarDef> globalVarDefList;
    private final ArrayList<StringLiteral> stringLiteralList;

    public Module() {
        functionList = new ArrayList<>();
        this.globalVarDefList = new ArrayList<>();
        this.stringLiteralList = new ArrayList<>();
    }

    public void addFunction(Function f) {
        functionList.add(f);
    }

    public void addGlobalVarDef(GlobalVarDef g) {
        this.globalVarDefList.add(g);
    }

    public void addStringLiteral(StringLiteral s) {
        this.stringLiteralList.add(s);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("declare i32 @getint()\n" +
                "declare i32 @getchar()    \n" +
                "declare void @putint(i32)  \n" +
                "declare void @putch(i32)     \n" +
                "declare void @putstr(i8*)   ");
        sb.append("\n\n");
        sb.append(stringLiteralList.stream().map(StringLiteral::toString).collect(Collectors.joining("\n")));
        sb.append("\n\n");
        sb.append(globalVarDefList.stream().map(GlobalVarDef::toString).collect(Collectors.joining("\n")));
        sb.append("\n\n");
        sb.append(functionList.stream().map(Object::toString).collect(Collectors.joining("\n\n")));
        return sb.toString();
    }

    public void toAssembly() {
        stringLiteralList.forEach(StringLiteral::toAssembly);
        globalVarDefList.forEach(GlobalVarDef::toAssembly);
        functionList.forEach(Function::toAssembly);
    }

    public ArrayList<Function> getFunctionList() {
        return functionList;
    }
}
