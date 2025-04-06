package llvm_ir.component;

import llvm_ir.component.type.ArrayType;
import llvm_ir.component.type.LLVMType;

import java.util.ArrayList;

public class Initial {
    private LLVMType type;
    private ArrayList<Integer> valueList;
    private boolean isGlobalEmpty = false;

    public Initial(ArrayList<Integer> valueList, LLVMType type) {
        this.valueList = valueList;
        this.type = type;
        if (type instanceof ArrayType) {
            if (valueList.isEmpty()) {
                isGlobalEmpty = true;
            }
            int cnt = ((ArrayType) type).getNumElements() - valueList.size();
            while (cnt > 0) {
                valueList.add(0);
                cnt--;
            }
        }
    }

    public LLVMType getType() {
        return type;
    }

    public void addValue(Integer integer) {
        valueList.add(integer);
    }

    public void truncValue() {
        valueList.forEach(value -> value = value & 0xff);
    }

    public void setValueList(ArrayList<Integer> valueList) {
        this.valueList = valueList;
    }

    public int getValue(int pos) {
        return valueList.get(pos);
    }

    public ArrayList<Integer> getValueList() {
        return valueList;
    }

    public boolean isGlobalEmpty() {
        return isGlobalEmpty;
    }
}
