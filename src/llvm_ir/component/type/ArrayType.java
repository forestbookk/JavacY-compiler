package llvm_ir.component.type;

public class ArrayType implements LLVMType {
    private final IntegerType elementType; // 数组的元素类型
    private final int numElements; // 数组长度
    private boolean isElementTypeInt32;

    // 初始化
    public ArrayType(IntegerType elementType, int numElements) {
        isElementTypeInt32 = elementType.bitWidth == 32;
        this.elementType = elementType;
        this.numElements = numElements;
    }

    public IntegerType getElementType() {
        return elementType;
    }

    public int getNumElements() {
        return numElements;
    }

    @Override
    public String toString() {
        // 数组长度可变，出现场景为函数参数
        if (numElements < 0) {
            return elementType.toString();
        }
        return "[" + numElements + " x " + elementType.toString() + "]";
    }

    @Override
    public boolean isInt32() {
        return this.isElementTypeInt32;
    }

    @Override
    public boolean isBaseTypeSame(IntegerType objType) {
        return (isInt32() && objType.bitWidth == 32) || (!isInt32() && objType.bitWidth == 8);
    }
}
