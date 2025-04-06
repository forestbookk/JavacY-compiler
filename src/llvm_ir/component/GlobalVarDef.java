package llvm_ir.component;

import backend.MipsBuilder;
import llvm_ir.Value;
import llvm_ir.component.type.ArrayType;
import llvm_ir.component.type.LLVMType;
import llvm_ir.component.type.PointerType;

import java.util.ArrayList;

public class GlobalVarDef extends Value {
    private boolean needCut;
    private Initial initial;
    private boolean isConstant;

    /**
     * @param name
     * @param type：PointerType。指向的类型可能是IntegerType或ArrayType
     * @param initial
     * @param isConstant
     */

    public GlobalVarDef(String name, LLVMType type, Initial initial, boolean isConstant) {
        super(name, type);
        this.initial = initial;
        this.isConstant = isConstant;
        this.needCut = !((PointerType) type).getElementType().isInt32();
        if (needCut) this.initial.truncValue();
    }

    /**
     * 输出的actualType：是变量实际类型，而不是初始化存的指针类型
     *
     * @return
     */
    @Override
    public String toString() {
        String constStr = isConstant ? "constant " : "global ";
        StringBuilder res = new StringBuilder();
        res.append(name).append(" = dso_local ").append(constStr);
        LLVMType actualType = ((PointerType) type).getElementType();
        res.append(actualType).append(" ");

        ArrayList<Integer> valueList = initial.getValueList();
        if (initial.isGlobalEmpty()) {
            res.append("zeroinitializer");
        } else {
            if (!(initial.getType() instanceof ArrayType)) {
                res.append(needCut ? (valueList.get(0) & 0xff) : valueList.get(0));
            } else {
                res.append("[");
                for (int i = 0; i < valueList.size(); i++) {
                    res.append(needCut ? "i8 " : "i32 ").append(valueList.get(i));
                    res.append(i == valueList.size() - 1 ? "]" : ", ");
                }
            }
        }
        return res.toString();
    }

    @Override
    public void toAssembly() {
        MipsBuilder.genNInsUnit(!needCut, name.substring(1), initial.getValueList());
    }
}
