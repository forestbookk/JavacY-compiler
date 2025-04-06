package llvm_ir.component.type;

public class PointerType implements LLVMType {

    private final LLVMType elementType; // 指向的类型，可以是ArrayType、IntegerType等
    private boolean isActualTypeInt32;

    public PointerType(LLVMType elementType) {
        this.isActualTypeInt32 = elementType.isInt32();
        this.elementType = elementType;
    }

    public LLVMType getElementType() {
        return elementType;
    }

    public String toStringIfArrayVariable() {
        return ((ArrayType) elementType).getElementType() + "*";
    }

    @Override
    public String toString() {
        return elementType.toString() + "*";
    }

    @Override
    public boolean isInt32() {
        return this.isActualTypeInt32;
    }

    @Override
    public boolean isBaseTypeSame(IntegerType objType) {
        return (this.isActualTypeInt32 && objType.bitWidth == 32) ||
                (!this.isActualTypeInt32 && objType.bitWidth == 8);
    }
}

