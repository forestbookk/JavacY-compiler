package llvm_ir;

import llvm_ir.component.type.LLVMType;
import llvm_ir.instr.io.PutInt;

import java.util.ArrayList;

public class User extends Value {
    protected ArrayList<Value> operands;

    public User(String name, LLVMType type) {
        super(name, type);
        this.operands = new ArrayList<>();
    }

    public void addOperand(Value v) {
        operands.add(v);
        if (v != null) {
            v.addUse(new Use(this));
        }
    }

    public boolean modifyAllOperands(Value oldValue, Value newValue) {
        for (int i = 0; i < operands.size(); i++) {
            if (operands.get(i) == oldValue) {
                oldValue.delUse(this);
                operands.set(i, newValue);
                newValue.addUse(new Use(this));
                return true;
            }
        }
        return false;
    }

    public Value getOp1() {
        return operands.get(0);
    }

    public Value getOp2() {
        return operands.get(1);
    }

    public ArrayList<Value> getOperands() {
        return operands;
    }
}
