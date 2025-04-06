package llvm_ir.instr;

import backend.MipsBuilder;
import backend.Register;
import backend.instruction.i_instr.MemImi;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.Instruction;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.type.ConstantType;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.PointerType;

public class MoveInstr extends Instruction {
    public MoveInstr(String name, Value dstValue, Value srcValue) {
        super(name, IntegerType.VOID);
        addOperand(dstValue);
        addOperand(srcValue);
    }

    public Value getDstValue() {
        return operands.get(0);
    }

    public Value getSrcValue() {
        return operands.get(1);
    }

    public void setSrcValue(Value srcValue) {
        operands.set(1, srcValue);
    }

    @Override
    public String toString() {
        return "move " +
                getDstValue().getResultType() + " " + getDstValue().getName() + ", " +
                " " + getSrcValue().getName();
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        Value dstValue = getDstValue();
        Value srcValue = getSrcValue();
        Register dstRegister = MipsBuilder.getRegisterForValue(dstValue);
        Register srcRegister = MipsBuilder.getRegisterForValue(srcValue);

        if (dstRegister != null && srcRegister == dstRegister) {
            return;
        }

        if (dstRegister == null) {
            // dstRegister可能被赋值，所以不能作为MipsBuilder查看的结果
            dstRegister = Register.K0;
        }

        if (srcValue instanceof Constant) {
            // 直接修改，无需真正的move
            MipsBuilder.genNInsLiImi(dstRegister, ((Constant) srcValue).getContent());
        } else if (srcValue instanceof UndefinedValue) {
            // 直接修改，无需真正的move
            MipsBuilder.genNInsLiImi(dstRegister, 0);
        }
        // 注意：这里为了方便move，和MipsBuilder的通用方法略有不同
        // 此前srcValue存在寄存器（srcRegister存的仍然是MipsBuilder的查询结果）
        else if (MipsBuilder.getRegisterForValue(srcValue) != null) {
            MipsBuilder.genNInsMoveRmi(MipsBuilder.getRegisterForValue(srcValue), dstRegister);
        }
        // 此前srcValue存在栈上
        else {
            Integer stackOffsetOfSrc = MipsBuilder.getStackManager().getStackOffset(srcValue);
            if (stackOffsetOfSrc == null) {
                MipsBuilder.getStackManager().decrementOffset(4);
                stackOffsetOfSrc = MipsBuilder.getStackManager().getCurOffset();
                MipsBuilder.getStackManager().addOffsetForValue(srcValue);
            }

            MipsBuilder.genNInsMemImi(MemImi.Op.lw,
                    Register.SP, dstRegister, stackOffsetOfSrc);
        }

        // 若此前dst并不在寄存器中，则需写回栈
        if (MipsBuilder.getRegisterForValue(dstValue) == null) {
            // 把srcValue的值写进去，而不是把dstValue的写进去（？TODO
            Integer offsetDst = MipsBuilder.getStackManager().getStackOffset(dstValue);
            if (offsetDst == null) {
                MipsBuilder.getStackManager().decrementOffset(4);
                offsetDst = MipsBuilder.getStackManager().getCurOffset();
                MipsBuilder.getStackManager().addOffsetForValue(dstValue);
            }

            if (!srcValue.isWord()) {
                MipsBuilder.truncToI8(dstRegister, dstRegister);
            }
            MipsBuilder.genNInsMemImi(MemImi.Op.sw, Register.SP, dstRegister, offsetDst);
        }
    }
}
