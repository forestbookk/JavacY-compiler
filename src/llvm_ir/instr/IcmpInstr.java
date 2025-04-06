package llvm_ir.instr;

import backend.MipsBuilder;
import backend.Register;
import backend.instruction.r_instr.CmpRmi;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.Instruction;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.LLVMType;

import java.util.Objects;

public class IcmpInstr extends Instruction {
    public enum Op {
        sgt, sge, slt, sle, eq, ne
    }

    private Op predicate;
    private LLVMType printType;

    public IcmpInstr(String name, Op predicate, Value op1, Value op2) {
        super(name, IntegerType.BOOLEAN);
        this.predicate = predicate;
        addOperand(op1);
        addOperand(op2);
        this.printType = op1.getResultType();
        if (!op2.getResultType().isInt32()) {
            printType = op2.getResultType();
        }
    }

    @Override
    public String toString() {
        Value op1 = getOp1();
        Value op2 = getOp2();
        LLVMType printType = op1.getResultType();
        if (!op2.getResultType().isInt32()) {
            printType = op2.getResultType();
        }
        return name + " = icmp " + predicate + " " + printType + " " + op1.getName() + ", " + op2.getName();
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        Value op1 = getOp1();
        Value op2 = getOp2();
        boolean isOp2Imm = op2.isImm();
        Register reg1 = Register.K0;
        Register reg2 = Register.K1;
        Register rd = Objects.requireNonNullElse(MipsBuilder.getRegisterForValue(this), Register.K0);

        // calc_i
        if (predicate == Op.slt && isOp2Imm) {
            if (op1 instanceof Constant) {
                MipsBuilder.genNInsLiImi(reg1, ((Constant) op1).getContent());
            } else if (op1 instanceof UndefinedValue) {
                MipsBuilder.genNInsLiImi(reg1, 0);
            } else {
                reg1 = MipsBuilder.allocateOrLoadRegForValue(op1, reg1, printType.isInt32());
            }
            MipsBuilder.genNInsSltiImi(reg1, rd, ((Constant) op2).getContent());
        }
        // calc_r
        else {
            // 处理op1
            if (op1 instanceof Constant) {
                MipsBuilder.genNInsLiImi(reg1, ((Constant) op1).getContent());
            } else if (op1 instanceof UndefinedValue) {
                MipsBuilder.genNInsLiImi(reg1, 0);
            } else {
                reg1 = MipsBuilder.allocateOrLoadRegForValue(op1, reg1, printType.isInt32());
            }
            // 处理op2
            if (op2 instanceof Constant) {
                MipsBuilder.genNInsLiImi(reg2, ((Constant) op2).getContent());
            } else if (op2 instanceof UndefinedValue) {
                MipsBuilder.genNInsLiImi(reg2, 0);
            } else {
                reg2 = MipsBuilder.allocateOrLoadRegForValue(op2, reg2, printType.isInt32());
            }
            // 处理cmp逻辑
            switch (predicate) {
                case eq -> {
                    MipsBuilder.genNInsCmpRmi(CmpRmi.Op.seq, reg1, reg2, rd);
                }
                case ne -> {
                    MipsBuilder.genNInsCmpRmi(CmpRmi.Op.sne, reg1, reg2, rd);
                }
                case sge -> {
                    MipsBuilder.genNInsCmpRmi(CmpRmi.Op.sge, reg1, reg2, rd);
                }
                case sgt -> {
                    MipsBuilder.genNInsCmpRmi(CmpRmi.Op.sgt, reg1, reg2, rd);
                }
                case sle -> {
                    MipsBuilder.genNInsCmpRmi(CmpRmi.Op.sle, reg1, reg2, rd);
                }
                case slt -> {
                    MipsBuilder.genNInsCmpRmi(CmpRmi.Op.slt, reg1, reg2, rd);
                }
            }
        }

        // 无优化：把rd存入栈
        if (MipsBuilder.getRegisterForValue(this) == null) {
            MipsBuilder.storeRegValueToStack(this, rd, true);
        }
    }

    @Override
    public boolean canBeUsed() {
        return true;
    }
}
