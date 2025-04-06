package llvm_ir.instr;

import backend.MipsBuilder;
import backend.instruction.j_instr.JumpJmi;
import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import llvm_ir.component.Instruction;
import llvm_ir.component.type.IntegerType;

public class BrInstr extends Instruction {

    public BrInstr(String name, BasicBlock targetBB) {
        super(name, IntegerType.VOID);
        addOperand(targetBB);
    }

    @Override
    public String toString() {
        return "br label %" + getTargetBB().getName();
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        MipsBuilder.genNInsJumpJmi(JumpJmi.Op.j, getTargetBB().getName());
    }

    public BasicBlock getTargetBB() {
        return (BasicBlock) operands.get(0);
    }
}
