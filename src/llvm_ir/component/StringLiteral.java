package llvm_ir.component;

import backend.MipsBuilder;
import llvm_ir.Value;
import llvm_ir.component.type.ArrayType;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.PointerType;

public class StringLiteral extends Value {
    private String str;

    public StringLiteral(String name, String str) {
        super(name, new PointerType(new ArrayType(IntegerType.CHAR, str.length() + 1)));
        this.str = str;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name + " = constant " + ((PointerType) type).getElementType() + " c\"");
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\n') {
                sb.append("\\0A");
            } else {
                sb.append(str.charAt(i));
            }
        }
        sb.append("\\00\"");
        return sb.toString();
    }

    @Override
    public void toAssembly() {
        MipsBuilder.genNInsAsciiz(name.substring(1), str);
    }
}
