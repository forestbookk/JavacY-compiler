package midend;

import backend.Register;
import backend.instruction.i_instr.BranchImi;
import frontend.parser.AST.Exp.Cond;
import frontend.symbol.symbols.Func.Func;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import llvm_ir.component.Constant;
import llvm_ir.component.Instruction;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.function.Function;
import llvm_ir.component.type.LLVMType;
import llvm_ir.instr.BrInstr;
import llvm_ir.instr.CondBrInstr;
import llvm_ir.instr.IcmpInstr;
import llvm_ir.instr.MoveInstr;
import llvm_ir.instr.PcopyInstr;
import llvm_ir.instr.PhiInstr;

import javax.management.ValueExp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class RemovePhi {
    private static Function function;

    public static void run() {
        for (Function function : Optimizer.irModule.getFunctionList()) {
            RemovePhi.function = function;
            phi2pcopy();
            pcopy2move();
        }
    }



    public static void phi2pcopy() {
        ArrayList<BasicBlock> basicBlocks = new ArrayList<>(function.getBBList());
        for (BasicBlock bb : basicBlocks) {
            // 如果bb中没有phi指令，continue
            if (!(bb.getInstrList().getFirst() instanceof PhiInstr)) {
                continue;
            }
            // 只处理bb中含有phi指令
            ArrayList<BasicBlock> preBBList = bb.getPre();
            ArrayList<PcopyInstr> pcopyInstrList = new ArrayList<>();

            for (int i = 0; i < preBBList.size(); i++) {
                // 若preOfBB的后继只有bb，则直接将pcopy插入preOfBB
                // create pcopyInstr
                PcopyInstr pcopyInstr = new PcopyInstr(IRBuilder.genLocalVarNameInSpecificFunc(function));
                pcopyInstrList.add(pcopyInstr);

                BasicBlock preBB = preBBList.get(i);
                if (preBB.getSuc().size() == 1) {
                    insertPcopyIntoPreBB(pcopyInstr, preBB);
                }
                // 若preOfbb有多个后继，则将pcopy插入中间基本块
                else {
                    insertPcopyIntoMidBB(pcopyInstr, preBB, bb);
                }
            }
            // 将phi的option（非UndefinedValue）放入pcopy，顺序已定不必操心
            Iterator<Instruction> it = bb.getInstrList().iterator();
            while (it.hasNext()) {
                Instruction instr = it.next();
                if (instr instanceof PhiInstr) {
                    for (int i = 0; i < instr.getOperands().size(); i++) {
                        Value option = instr.getOperands().get(i);
                        if (!(option instanceof UndefinedValue)) {
                            pcopyInstrList.get(i).addCopy(instr, option);
                        }
                    }
                    it.remove(); // delete phiInstr
                }
            }
        }
    }

    public static void insertPcopyIntoPreBB(PcopyInstr pcopyInstr, BasicBlock preBB) {
        LinkedList<Instruction> instrList = preBB.getInstrList();
        Instruction lastInstr = instrList.getLast();
        assert lastInstr instanceof BrInstr || lastInstr instanceof CondBrInstr;
        pcopyInstr.setParentBB(preBB);
        instrList.add(instrList.indexOf(lastInstr), pcopyInstr);
    }

    public static void insertPcopyIntoMidBB(PcopyInstr pcopyInstr, BasicBlock preBB, BasicBlock sucBB) {
        // 生成BB并insert
        Function function = preBB.getParentFunction();
        BasicBlock midBB = new BasicBlock(IRBuilder.genBBName());
        midBB.setParentFunction(function);
        function.getBBList().add(function.getBBList().indexOf(sucBB), midBB);
        // 插入pcopy
        pcopyInstr.setParentBB(midBB);
        midBB.addInstr(pcopyInstr);
        // modify jump
        CondBrInstr condBrInstr = (CondBrInstr) preBB.getLastInstr();
        // 当后继块就是跳转的tureLabel时，
        if (sucBB.equals(condBrInstr.getTrueLabel())) {
            // 修改pre的trueLabel为midBB
            condBrInstr.setTrueLabel(midBB);
        }
        // 当后继块就是跳转的falseLabel
        else {
            // 修改pre的falseLabel为midBB
            condBrInstr.setFalseLabel(midBB);
        }
        // create and insert jump: midBB -> sucBB
        BrInstr brInstr = new BrInstr(IRBuilder.genLocalVarNameInSpecificFunc(function), sucBB);
        midBB.addInstr(brInstr);
        brInstr.setParentBB(midBB);

        // modify and add relation of pre, mid and suc

        preBB.addSucAtIndex(preBB.getSuc().indexOf(sucBB), midBB);
        preBB.removeSuc(sucBB);

        midBB.addPre(preBB);
        midBB.addSuc(sucBB);

        sucBB.addPreAtIndex(sucBB.getPre().indexOf(preBB), midBB);
        sucBB.removePre(preBB);
    }

    public static void pcopy2move() {
        for (BasicBlock bb : function.getBBList()) {
            LinkedList<Instruction> instrList = bb.getInstrList();
            // 如果存在pcopy指令
            int pcopyIndex = instrList.size() - 2;
            if (pcopyIndex >= 0 && instrList.get(pcopyIndex) instanceof PcopyInstr) {
                // pcopy2move
                LinkedList<MoveInstr> moveInstrs = toMove((PcopyInstr) instrList.get(pcopyIndex));
                // delete pcopy
                instrList.remove(pcopyIndex);
                // insert move
                for (MoveInstr moveInstr : moveInstrs) {
                    moveInstr.setParentBB(bb);
                    instrList.add(instrList.size() - 1, moveInstr);
                }
            }
        }
    }

    public static LinkedList<MoveInstr> toMove(PcopyInstr pcopyInstr) {
        ArrayList<Value> dstList = pcopyInstr.getDstList();
        ArrayList<Value> srcList = pcopyInstr.getSrcList();
        LinkedList<MoveInstr> moveList = new LinkedList<>();
        Function function = pcopyInstr.getParentBB().getParentFunction();

        // init
        for (int i = 0; i < dstList.size(); i++) {
            moveList.add(new MoveInstr(
                    IRBuilder.genLocalVarNameInSpecificFunc(function),
                    dstList.get(i), srcList.get(i)));
        }

        // deal loop assign
        ArrayList<MoveInstr> newlyGenMoveList = new ArrayList<>();
        HashSet<Value> processedValueTracker = new HashSet<>();
        boolean isAssignLoop;
        int beginIndex = 0;
        for (int i = 0; i < moveList.size(); i++) {
            Value dstValue = moveList.get(i).getDstValue();
            if (!processedValueTracker.contains(dstValue) && !(dstValue instanceof Constant)) {
                isAssignLoop = false;
                for (int j = i + 1; j < moveList.size(); j++) {
                    if (moveList.get(j).getSrcValue().equals(dstValue)) {
                        isAssignLoop = true;
                        beginIndex = j;
                        break;
                    }
                }
                if (isAssignLoop) {
                    insertMidValue(dstValue, beginIndex, moveList, newlyGenMoveList, function);
                }
                processedValueTracker.add(dstValue);
            }
        }

        // 寄存器冲突
        // 前指令的dst和后指令的src
        processedValueTracker.clear();
        boolean isConflict;
        HashMap<Value, Register> val2reg = function.getVal2reg();
        for (int i = moveList.size() - 1; i >= 0; i--) {
            Value srcValue = moveList.get(i).getSrcValue();
            if (!(srcValue instanceof Constant) && !processedValueTracker.contains(srcValue)) {
                isConflict = false;
                for (int j = 0; j < i; j++) {
                    if (val2reg != null && val2reg.get(srcValue) != null &&
                            val2reg.get(srcValue) == val2reg.get(moveList.get(j).getDstValue())) {
                        isConflict = true;
                        beginIndex = j;
                        break;
                    }
                }
                if (isConflict) {
                    insertMidValue(srcValue, beginIndex, moveList, newlyGenMoveList, function);
                }
                processedValueTracker.add(srcValue);
            }
        }

        // insert newlyGenMoveInstr into moveList
        for (MoveInstr instr : newlyGenMoveList) {
            moveList.addFirst(instr);
        }

        return moveList;
    }

    public static void insertMidValue(Value targetValue, int beginIndex,
                                      LinkedList<MoveInstr> moveList,
                                      ArrayList<MoveInstr> newlyGenMoveList,
                                      Function function) {
        Value midValue = new Value(targetValue.getName(), targetValue.getIntegerTypeOfValue());
        for (int j = beginIndex; j < moveList.size(); j++) {
            MoveInstr moveInstr = moveList.get(j);
            if (moveInstr.getSrcValue().equals(targetValue)) {
                moveInstr.setSrcValue(midValue);
            }
        }
        newlyGenMoveList.add(new MoveInstr(IRBuilder.genLocalVarNameInSpecificFunc(function), midValue, targetValue));
    }

}
