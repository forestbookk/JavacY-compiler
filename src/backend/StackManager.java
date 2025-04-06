package backend;

import llvm_ir.Value;

import java.util.HashMap;

public class StackManager {
    private int curOffset;
    private HashMap<Value, Integer> value2Offset; // 变量到栈偏移量的映射

    public StackManager() {
        this.curOffset = 0;
        this.value2Offset = new HashMap<>();
    }

    public void init() {
        this.curOffset = 0; // 每个函数栈偏移从0开始
        this.value2Offset.clear(); // 清空栈偏移映射
    }

    // 为变量添加栈偏移量
    public void addOffsetForValue(Value value) {
        value2Offset.put(value, curOffset);
    }

    public void addOffsetForValue(Value value, int offset) {
        value2Offset.put(value, offset);
    }

    // 获取某个变量的栈偏移量
    public Integer getStackOffset(Value value) {
        if (!value2Offset.containsKey(value)) return null;
        return value2Offset.get(value);
    }

    public int getCurOffset() {
        return curOffset;
    }

    // 更新当前栈偏移
    public void decrementOffset(int offset) {
        curOffset -= offset;
        assert curOffset >= 0 : "栈偏移量不能小于 0";
    }
}
