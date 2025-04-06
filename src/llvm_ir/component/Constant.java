package llvm_ir.component;

import llvm_ir.Value;
import llvm_ir.component.type.ConstantType;

public class Constant extends Value {
    private int content;

    public Constant(int content) {
        super(String.valueOf(content), new ConstantType());
        this.content = content;
    }

    public int getContent() {
        return content;
    }
}
