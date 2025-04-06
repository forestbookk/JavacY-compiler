package llvm_ir.component;

import llvm_ir.Value;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.LLVMType;

public class UndefinedValue extends Value {
    public UndefinedValue(LLVMType type) {
        super("0", type);
    }

    // 需要后期设定type
    public UndefinedValue() {
        super("0", IntegerType.INT_32);
    }

    @Override
    public String toString() {
        return "undefined";
    }
}
