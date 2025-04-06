package llvm_ir.instr.io;

import backend.MipsBuilder;
import backend.Register;
import llvm_ir.component.StringLiteral;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.PointerType;

public class PutStr extends IOInstr {
    private StringLiteral stringLiteral;

    public PutStr(String name, StringLiteral stringLiteral) {
        super(name, IntegerType.VOID);
        this.stringLiteral = stringLiteral;
    }

    @Override
    public String toString() {
        PointerType pointerType = (PointerType) stringLiteral.getResultType();
        return "call void @putstr(i8* getelementptr inbounds (" +
                pointerType.getElementType() + ", " +
                pointerType + " " +
                stringLiteral.getName() + ", i64 0, i64 0))";
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        MipsBuilder.genNInsLaImi(Register.A0, stringLiteral.getName().substring(1));
        MipsBuilder.genNInsLiImi(Register.V0, 4);
        MipsBuilder.genNInsSyscallMi();
    }
}
