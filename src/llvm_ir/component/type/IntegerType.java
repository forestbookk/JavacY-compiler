package llvm_ir.component.type;

public class IntegerType implements LLVMType {
    public static IntegerType VOID = new IntegerType(0);
    public static IntegerType BOOLEAN = new IntegerType(1);
    public static IntegerType CHAR = new IntegerType(8);
    public static IntegerType INT_32 = new IntegerType(32);
    public int bitWidth;

    public IntegerType(int bitWidth) {
        this.bitWidth = bitWidth;
    }

    @Override
    public String toString() {
        return (bitWidth == 0) ? "void" : (
                (bitWidth == 1) ? "i1" : (
                        (bitWidth == 8) ? "i8" : (
                                (bitWidth == 32) ? "i32" : "ERROR"
                        )
                )
        );
    }

    @Override
    public boolean isInt32() {
        return bitWidth == 32;
    }

    @Override
    public boolean isBaseTypeSame(IntegerType objType) {
        return (this.bitWidth == 32 && objType.bitWidth == 32) ||
                (this.bitWidth == 8 && objType.bitWidth == 8);
    }
}
