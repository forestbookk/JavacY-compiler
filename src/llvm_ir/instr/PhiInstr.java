package llvm_ir.instr;

import llvm_ir.Use;
import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import llvm_ir.component.Instruction;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.LLVMType;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PhiInstr extends Instruction {
    private ArrayList<BasicBlock> preBBList;

    public PhiInstr(String name, ArrayList<BasicBlock> pre) {
        super(name, IntegerType.INT_32); // 缺省值为i32，后期随时修改,但其实还是不准确 TODO
        this.preBBList = pre;
        for (int i = 0; i < pre.size(); i++) {
            addOperand(null);
        }
    }

    @Override
    public void setType(LLVMType type) {
        super.setType(type);
        for (Value op : operands) {
            if (op instanceof UndefinedValue) {
                op.setType(type);
            }
        }
    }

    public void addOption(Value value, BasicBlock preBB) {
        // TODO: 如果value是UndefinedValue，那么在设定this的type的时候，需要顺手设置value的type
        operands.set(preBBList.indexOf(preBB), value);
        value.addUse(new Use(this));
    }

    @Override
    public boolean canBeUsed() {
        return true;
    }

    @Override
    public String toString() {
        return name + " = phi " + type + " " +
                preBBList.stream()
                        .map(bb -> "[ " +
                                operands.get(preBBList.indexOf(bb)).getName() + ", %" +
                                bb.getName() + " ]")
                        .collect(Collectors.joining(", "));
    }
}
