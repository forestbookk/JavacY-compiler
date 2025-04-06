package llvm_ir.instr;

import backend.MipsBuilder;
import backend.Register;
import backend.instruction.i_instr.AluImi;
import backend.instruction.i_instr.MemImi;
import backend.instruction.j_instr.JumpJmi;
import frontend.symbol.symbols.Func.Func;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.Instruction;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.function.Param;
import llvm_ir.component.type.ArrayType;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.LLVMType;
import llvm_ir.component.type.PointerType;
import llvm_ir.component.function.Function;

import java.util.ArrayList;
import java.util.List;

public class CallInstr extends Instruction {

    private ArrayList<LLVMType> paramTypeList;

    public CallInstr(String name, Function function, ArrayList<Value> paramList, ArrayList<LLVMType> paramTypeList) {
        super(name, function.getResultType());
        addOperand(function);
        for (Value param : paramList) {
            addOperand(param);
        }
        this.paramTypeList = paramTypeList;
    }

    public List<Value> getParamList() {
        return operands.subList(1, operands.size());
    }

    public Function getFunction() {
        return (Function) operands.get(0);
    }


    @Override
    public String toString() {
        Function function = getFunction();
        List<Value> paramList = getParamList();
        StringBuilder sb = new StringBuilder();
        if (name != null) sb.append(name).append(" = ");
        sb.append("call ").append(type.toString()).append(" ").append(function.getName()).append("(");
        for (int i = 0; i < paramList.size(); i++) {
            Value param = paramList.get(i);
            // type：作为参数，若为数组，那么type特殊
            LLVMType paramType = param.getResultType();
            if (paramType instanceof PointerType && ((PointerType) paramType).getElementType() instanceof ArrayType) {
                sb.append(((PointerType) paramType).toStringIfArrayVariable());
            } else {
                sb.append(paramType);
            }
            sb.append(" ").append(param.getName());
            sb.append(i == paramList.size() - 1 ? "" : ", ");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        Function function = getFunction();
        int oriOffset = MipsBuilder.getStackManager().getCurOffset();

        ArrayList<Register> allocatedRegs = MipsBuilder.registerManager.getAllocatedRegs();
        int regIndex = 0;
        for (Register reg : allocatedRegs) {
            // 无论i8，还是i32，offset统一以4为单位移动
            // 也统一采用sw存储，应该没问题吧？
            regIndex++;
            MipsBuilder.genNInsMemImi(MemImi.Op.sw, Register.SP, reg, oriOffset - regIndex * 4);
        }

        // 存sp和ra
        MipsBuilder.genNInsMemImi(MemImi.Op.sw, Register.SP, Register.SP, oriOffset - regIndex * 4 - 4);
        MipsBuilder.genNInsMemImi(MemImi.Op.sw, Register.SP, Register.RA, oriOffset - regIndex * 4 - 8);

        // 将参数压入
        List<Value> paramList = getParamList();
        boolean isParamWord;
        for (int i = 0; i < paramList.size(); i++) {
            Value param = paramList.get(i);
            isParamWord = paramTypeList.get(i).isInt32() || paramTypeList.get(i) instanceof PointerType; // 这里不能直接用param判断，因为param可能是常数。而常数的类型由上下文决定;

            // 如果参数可以放入a1-a3寄存器中
            int paramNum = i + 1;
            if (paramNum <= 3 && MipsBuilder.registerManager.isOn()) {
                Register reg = Register.getRegByIndex(Register.A0.ordinal() + paramNum);
                if (param instanceof Constant) {
                    // TODO: 0XFF
                    MipsBuilder.genNInsLiImi(reg, isParamWord ? ((Constant) param).getContent() :
                            ((Constant) param).getContent() & 0xff);
                } else if (param instanceof UndefinedValue) {
                    MipsBuilder.genNInsLiImi(reg, 0);
                }
                // 假设参数之前已经被分配了寄存器
                else if (MipsBuilder.getRegisterForValue(param) != null) {
                    Register oriReg = MipsBuilder.getRegisterForValue(param);
                    // 如果param本就是形参,则需从栈中取值
                    if (param instanceof Param) {
                        // 取值->lw或lb
                        MipsBuilder.genNInsMemImi(MemImi.Op.lw,
                                Register.SP, reg, oriOffset - (1 + allocatedRegs.indexOf(oriReg)) * 4);
                        if (!isParamWord) {
                            MipsBuilder.truncToI8(reg, reg);
                        }
                    } else {
                        MipsBuilder.genNInsMoveRmi(oriReg, reg);
                        if (!isParamWord) {
                            MipsBuilder.truncToI8(reg, reg);
                        }
                    }
                }
                // 参数之前存在堆上
                else {
                    MipsBuilder.genNInsMemImi(MemImi.Op.lw,
                            Register.SP, reg, MipsBuilder.getStackManager().getStackOffset(param));
                    if (!isParamWord) {
                        MipsBuilder.truncToI8(reg, reg);
                    }
                    //MipsBuilder.loadFromStack(reg, param, isParamWord);
                }
            }
            // 超出a0-a3范围，剩下的存在堆上
            else {
                Register temp = Register.K0;
                if (param instanceof Constant) {
                    MipsBuilder.genNInsLiImi(temp, isParamWord ? ((Constant) param).getContent() :
                            ((Constant) param).getContent() & 0xff);
                } else if (param instanceof UndefinedValue) {
                    MipsBuilder.genNInsLiImi(temp, 0);
                } else if (MipsBuilder.getRegisterForValue(param) != null) {
                    // 参数之前存的寄存器
                    Register oriReg = MipsBuilder.getRegisterForValue(param);
                    if (param instanceof Param) {
                        MipsBuilder.genNInsMemImi(MemImi.Op.lw,
                                Register.SP, temp,
                                oriOffset - 4 * (1 + allocatedRegs.indexOf(oriReg)));
                        if (!isParamWord) {
                            MipsBuilder.truncToI8(temp, temp);
                        }
                    } else {
                        temp = oriReg;
                        if (!isParamWord) {
                            MipsBuilder.truncToI8(temp, temp);
                        }
                    }
                }
                // 无优化
                else {
                    MipsBuilder.genNInsMemImi(MemImi.Op.lw,
                            Register.SP, temp, MipsBuilder.getStackManager().getStackOffset(param));
                    if (!isParamWord) {
                        MipsBuilder.truncToI8(temp, temp);
                    }
                }
//                MipsBuilder.genNInsMemImi(MemImi.Op.sw,
//                        Register.SP, temp, oriOffset - regIndex * 4 - 8 - paramNum * 4);
                MipsBuilder.genNInsMemImi(MemImi.Op.sw,
                        Register.SP, temp, oriOffset - regIndex * 4 - 8 - paramNum * 4 - 4);
            }
        }

        // 设置sp为函数的栈底地址
        MipsBuilder.genNInsAluImi(AluImi.Op.addiu, Register.SP, Register.SP, oriOffset - regIndex * 4 - 8 - 4);
        // 调用函数
        MipsBuilder.genNInsJumpJmi(JumpJmi.Op.jal, function.getName().substring(1));
        // 恢复sp
        MipsBuilder.genNInsMemImi(MemImi.Op.lw, Register.SP, Register.RA, 4);
        // 恢复RA
        MipsBuilder.genNInsMemImi(MemImi.Op.lw, Register.SP, Register.SP, 8);

//        // 设置sp为函数的栈底地址
//        MipsBuilder.genNInsAluImi(AluImi.Op.addiu, Register.SP, Register.SP, oriOffset - regIndex * 4 - 8);
//        // 调用函数
//        MipsBuilder.genNInsJumpJmi(JumpJmi.Op.jal, function.getName().substring(1));
//        // 恢复sp
//        MipsBuilder.genNInsMemImi(MemImi.Op.lw, Register.SP, Register.RA, 0);
//        // 恢复RA
//        MipsBuilder.genNInsMemImi(MemImi.Op.lw, Register.SP, Register.SP, 4);


        // TO/DO: 恢复寄存器
        for (int i = 0; i < allocatedRegs.size(); i++) {
            // 同上存寄存器的时候，上面用sw，所以下面统一用lw
            MipsBuilder.genNInsMemImi(MemImi.Op.lw, Register.SP, allocatedRegs.get(i), oriOffset - (i + 1) * 4);
        }

        // 若函数不为void：获取返回值
        if (!function.isTypeVoid()) {
            if (MipsBuilder.getRegisterForValue(this) != null) {
                // (rs, rd)，将v0的值移到寄存器，v0->寄存器
                MipsBuilder.genNInsMoveRmi(Register.V0, MipsBuilder.getRegisterForValue(this));
            } else {
                // 无优化
                MipsBuilder.storeRegValueToStack(this, Register.V0, function.getResultType().isInt32());
//                MipsBuilder.getStackManager().decrementOffset(4);
//                MipsBuilder.getStackManager().addOffsetForValue(this);
//                MipsBuilder.genNInsMemImi(function.getResultType().isInt32() ? MemImi.Op.sw : MemImi.Op.sb,
//                        Register.SP, Register.V0, MipsBuilder.getStackManager().getCurOffset());
            }
        }
    }

    @Override
    public boolean canBeUsed() {
        return !getFunction().isTypeVoid();
    }
}
