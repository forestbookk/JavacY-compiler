package llvm_ir.instr;

import backend.MipsBuilder;
import backend.Register;
import backend.instruction.i_instr.BranchImi;
import backend.instruction.i_instr.MemImi;
import backend.instruction.j_instr.JumpJmi;
import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import llvm_ir.component.Instruction;
import llvm_ir.component.type.IntegerType;

public class CondBrInstr extends Instruction {

    public CondBrInstr(String name, Value cond, BasicBlock trueLabel, BasicBlock falseLabel) {
        super(name, IntegerType.BOOLEAN);
        addOperand(cond);
        addOperand(trueLabel);
        addOperand(falseLabel);
    }

    public Value getCond() {
        return operands.get(0);
    }

    public void setTrueLabel(BasicBlock trueLabel) {
        operands.set(1, trueLabel);
    }

    public void setFalseLabel(BasicBlock falseLabel) {
        operands.set(2, falseLabel);
    }

    @Override
    public String toString() {
        return "br i1 " + getCond().getName() +
                ", label %" + getTrueLabel().getName() +
                ", label %" + getFalseLabel().getName();
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        Register condReg = MipsBuilder.getRegisterForValue(getCond());
        if (condReg == null) {
            condReg = Register.K0;
            MipsBuilder.genNInsMemImi(MemImi.Op.lw,
                    Register.SP, condReg, MipsBuilder.getStackManager().getStackOffset(getCond()));
        }
        MipsBuilder.genNInsBranchImi(BranchImi.Op.bne, condReg, Register.ZERO, getTrueLabel().getName());
        MipsBuilder.genNInsJumpJmi(JumpJmi.Op.j, getFalseLabel().getName());
    }

    public BasicBlock getFalseLabel() {
        return (BasicBlock) operands.get(2);
    }

    public BasicBlock getTrueLabel() {
        return (BasicBlock) operands.get(1);
    }
}
