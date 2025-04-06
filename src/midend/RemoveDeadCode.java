package midend;

import llvm_ir.component.BasicBlock;
import llvm_ir.component.Instruction;
import llvm_ir.component.function.Function;
import llvm_ir.instr.BrInstr;
import llvm_ir.instr.CondBrInstr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class RemoveDeadCode {
    public static void removeExtraBr() {
        for (Function function : Optimizer.irModule.getFunctionList()) {
            for (BasicBlock bb : function.getBBList()) {
                Iterator<Instruction> it = bb.getInstrList().iterator();
                boolean isRemove = false;
                while (it.hasNext()) {
                    Instruction instr = it.next();

                    // 如果删除标志位激活，则无条件最优先删除
                    if (isRemove) {
                        System.out.println(function.getName() + " " + bb.getName() + " " + instr.toString());
                        it.remove();
                        continue;
                    }

                    // 如果指令是跳转，则删除该指令之后的所有指令
                    if (instr instanceof BrInstr || instr instanceof CondBrInstr) {
                        isRemove = true;
                    }
                }
            }
        }
    }

    public static void removeUnreachableBB() {
        for (Function function : Optimizer.irModule.getFunctionList()) {
            BasicBlock entry = function.getBBList().get(0);
            HashSet<BasicBlock> visit = new HashSet<>();
            visitBB(entry, visit);
            Iterator<BasicBlock> it = function.getBBList().iterator();
            while (it.hasNext()) {
                BasicBlock bb = it.next();
                if (!visit.contains(bb)) {
                    it.remove();
                    bb.setDelete(true);
                }
            }
        }
    }

    public static void visitBB(BasicBlock entry, HashSet<BasicBlock> visit) {
        visit.add(entry);
        Instruction instr = entry.getLastInstr();
        if (instr instanceof BrInstr) {
            BasicBlock bb = ((BrInstr) instr).getTargetBB();
            System.out.println(bb.getName());
            if (!visit.contains(bb)) {
                visitBB(bb, visit);
            }
        } else if (instr instanceof CondBrInstr) {
            BasicBlock falseLabel = ((CondBrInstr) instr).getFalseLabel();
            if (!visit.contains(falseLabel)) {
                visitBB(falseLabel, visit);
            }
            BasicBlock trueLabel = ((CondBrInstr) instr).getTrueLabel();
            if (!visit.contains(trueLabel)) {
                visitBB(trueLabel, visit);
            }
        }
    }
}
