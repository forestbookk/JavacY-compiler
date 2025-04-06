package llvm_ir.component.type;

public class ConstantType implements LLVMType {
    private IntegerType assignType = null;

    public void setAssignType(IntegerType assignType) {
        this.assignType = assignType;
    }

    public IntegerType getAssignType() {
        return assignType == null ? IntegerType.INT_32 : assignType;
    }

    @Override
    public boolean isInt32() {
        return true;
    }

    @Override
    public boolean isBaseTypeSame(IntegerType objType) {
        return true;
    }

    @Override
    public String toString() {
        if (assignType == null) {
            return IntegerType.INT_32.toString();
        }
        return assignType.toString();
    }
}
