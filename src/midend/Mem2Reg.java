package midend;

import llvm_ir.IRBuilder;
import llvm_ir.Use;
import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import llvm_ir.component.Instruction;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.LLVMType;
import llvm_ir.component.type.PointerType;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.function.Function;
import llvm_ir.instr.AllocaInstr;
import llvm_ir.instr.LoadInstr;
import llvm_ir.instr.PhiInstr;
import llvm_ir.instr.StoreInstr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

/**
 * 将内存分配（Alloca）指令优化为寄存器分配
 * 形式：将变量提升到SSA形式
 * 总体流程：
 * (1)初始化：为每个Alloca指令构建使用与定义集合
 * (2)插入PHI指令：在必要的基本块中插入PHI指令
 * (3)重命名变量：通过深度优先遍历和栈操作，将Load和Store替换为直接的寄存器操作
 */

public class Mem2Reg {
    private static Instruction curAllocaInstr;
    private static ArrayList<BasicBlock> defBBList;
    private static ArrayList<BasicBlock> useBBList;
    private static ArrayList<Instruction> defInstrList;
    private static ArrayList<Instruction> useInstrList;
    private static Stack<Value> instrStack;

    public static void run() {
        for (Function function : Optimizer.irModule.getFunctionList()) {
            for (BasicBlock bb : function.getBBList()) {
                // 需要根据原list增加list，所以原list需要另作保存
                ArrayList<Instruction> instrNewList = new ArrayList<>(bb.getInstrList());
                for (Instruction instr : instrNewList) {
                    if (instr instanceof AllocaInstr && ((PointerType) instr.getResultType()).
                            getElementType() instanceof IntegerType) {
                        initDataStructure(instr);
                        placePhiForSSA();
                        renameInstr(function.getBBList().get(0));
                    }
                }
            }
        }
    }

    public static void initDataStructure(Instruction instr) {
        curAllocaInstr = instr;
        defBBList = new ArrayList<>();
        useBBList = new ArrayList<>();
        defInstrList = new ArrayList<>();
        useInstrList = new ArrayList<>();
        instrStack = new Stack<>();

        for (Use use : curAllocaInstr.getUseList()) {
            assert use.getUser() instanceof Instruction;
            Instruction user = (Instruction) use.getUser();
            // TO/DO: DELETE
            if (!user.getParentBB().isDelete() && user instanceof LoadInstr) {
                useInstrList.add(user);
                if (!defBBList.contains(user.getParentBB())) {
                    useBBList.add(user.getParentBB());
                }
            } else if (!user.getParentBB().isDelete() && user instanceof StoreInstr) {
                defInstrList.add(user);
                if (!defBBList.contains(user.getParentBB())) {
                    defBBList.add(user.getParentBB());
                }
            }
        }
    }

    public static void placePhiForSSA() {
        HashSet<BasicBlock> phiInsertedBlocks = new HashSet<>(); // 需要插入PHI的基本块集合
        Stack<BasicBlock> defBBStack = new Stack<>(); // 定义变量的基本块的集合
        for (BasicBlock bb : defBBList) {
            // defBBList: 存储了当前变量的所有定义（store指令）所在的基本块
            // 将这些基本块压入栈作为起点
            defBBStack.push(bb);
        }
        // 逐步分析这些块的支配边界
        while (!defBBStack.isEmpty()) {
            BasicBlock bb = defBBStack.pop();
            // 遍历该基本块的支配边界
            for (BasicBlock df : bb.getDf()) {
                if (df.getName().equals("bb_8_end") || df.getName().equals("bb_10_cond")) {
                    df = df;
                }
                // 如果df不在集合中，说明df尚未插入phi指令
                if (!phiInsertedBlocks.contains(df)) {
                    // 调用createAndInsertPhi，插入phi到Y中
                    createAndInsertPhi(df);
                    // 将df加入集合
                    phiInsertedBlocks.add(df);
                    // 如果df不在defBBList中，将df压入栈，继续分析支配边界
                    if (!defBBList.contains(df)) {
                        defBBStack.push(df);
                    }
                }
            }
        }
    }

    public static void createAndInsertPhi(BasicBlock bb) {
        Instruction phiInstr = new PhiInstr(IRBuilder.genLocalVarNameInSpecificFunc
                (bb.getParentFunction()), bb.getPre());
        bb.getInstrList().addFirst(phiInstr);
        phiInstr.setParentBB(bb);
        defInstrList.add(phiInstr);
        useInstrList.add(phiInstr);
    }

    public static void renameInstr(BasicBlock entry) {
        int pushCntOfStack = 0;
        Iterator<Instruction> it = entry.getInstrList().iterator();
        while (it.hasNext()) {
            Instruction instr = it.next();

            if (instr instanceof StoreInstr && ((StoreInstr) instr).getSource().getName().equals("%v0")) {
                pushCntOfStack = pushCntOfStack;
            }

            if (instr.equals(curAllocaInstr)) {
                it.remove();
            } else if (instr instanceof LoadInstr && useInstrList.contains(instr)) {
                Value value = instrStack.isEmpty() ?
                        new UndefinedValue(instr.getResultType().isInt32() ? IntegerType.INT_32 : IntegerType.CHAR) :
                        instrStack.peek();
                instr.modifyAllUse(value);
                it.remove();
            } else if (instr instanceof StoreInstr && defInstrList.contains(instr)) {
                Value value = ((StoreInstr) instr).getSource();
                instrStack.push(value);
                pushCntOfStack++;
                it.remove();
            } else if (instr instanceof PhiInstr && defInstrList.contains(instr)) {
                instrStack.push(instr);
                pushCntOfStack++;
            }
        }
        // bb's suc
        for (BasicBlock bb : entry.getSuc()) {
            Instruction firstInstr = bb.getInstrList().getFirst();
            if (firstInstr instanceof PhiInstr phiInstr && useInstrList.contains(firstInstr)) {
                LLVMType type = IntegerType.INT_32; // type初始化为i32，后期需要给phi和其操作数设置
                Value option = instrStack.isEmpty() ? new UndefinedValue() : instrStack.peek();
                // 如果操作数是i8，则type改为i8.若操作数为i32，则保持原值
                type = !(option.getIntegerTypeOfValue().isInt32()) ? IntegerType.CHAR : type;
                phiInstr.addOption(option, entry);
                // 给phi和其操作数设置类型
                phiInstr.setType(type);
            }
        }

        // 对entry支配的基本块进行重命名
        for (BasicBlock subOfIDom : entry.getSubOfIDom()) {
            renameInstr(subOfIDom);
        }

        for (int i = 0; i < pushCntOfStack; i++) {
            instrStack.pop();
        }
    }
}
