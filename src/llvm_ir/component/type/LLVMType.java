package llvm_ir.component.type;

public interface LLVMType {
    public boolean isInt32();

    public boolean isBaseTypeSame(IntegerType objType);

}
