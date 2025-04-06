package llvm_ir.instr;

import backend.MipsBuilder;
import backend.Register;
import backend.instruction.i_instr.MemImi;
import llvm_ir.component.Constant;
import llvm_ir.component.GlobalVarDef;
import llvm_ir.component.Instruction;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.type.IntegerType;
import llvm_ir.Value;
import llvm_ir.component.type.PointerType;

/* store type %value, type* %pointer_variable, align alignment */
public class StoreInstr extends Instruction {
    // 要存储的值 TODO 尽量避免判断from的类型，因为from有可能是Constant，而Constant是i32也是i8
    // 存储目标，即目标地址（指针）

    public StoreInstr(Value source, Value destination) {
        super(null, IntegerType.VOID);
        addOperand(source);
        addOperand(destination);
    }


    @Override
    public String toString() {
        Value source = getSource();
        Value destination = getDestination();
        return "store " +
                source.getResultType() + " " + source.getName() + ", " +
                destination.getResultType() + " " + destination.getName();
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        Value source = getSource();
        Value destination = getDestination();
        boolean isObjectInt = destination.getResultType().isInt32();
        Register rs = Register.K0;
        Register rt = Register.K1;

        if (destination instanceof GlobalVarDef) {
            MipsBuilder.genNInsLaImi(rt, destination.getName().substring(1));
        } else if (MipsBuilder.getRegisterForValue(destination) != null) {
            rt = MipsBuilder.getRegisterForValue(destination);
        } else {
            // 加载指针地址：按字加载
            MipsBuilder.genNInsMemImi(MemImi.Op.lw, Register.SP, rt, MipsBuilder.getStackManager().getStackOffset(destination));
        }

        // 若source为常数：
        if (source instanceof Constant) {
            MipsBuilder.genNInsLiImi(rs, ((Constant) source).getContent());
        } else if (source instanceof UndefinedValue) {
            MipsBuilder.genNInsLiImi(rs, 0);
        } else {
            rs = MipsBuilder.allocateOrLoadRegForValue(source, rs,
                    isObjectInt || source.getResultType() instanceof PointerType);
        }

        if (!isObjectInt) {
            MipsBuilder.truncToI8(rs, rs);
        }
        // 此处不能判断source的类型，因为source可能是常数。而常数的类型取决于上下文
        MipsBuilder.genNInsMemImi(MemImi.Op.sw,
                rt, rs, 0);
    }

    public Value getSource() {
        return operands.get(0);
    }

    public Value getDestination() {
        return operands.get(1);
    }
}
