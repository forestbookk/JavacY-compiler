package midend;

import llvm_ir.component.BasicBlock;
import llvm_ir.component.Instruction;
import llvm_ir.component.Module;
import llvm_ir.component.function.Function;
import llvm_ir.instr.BrInstr;
import llvm_ir.instr.CondBrInstr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CFGBuilder {
    private static Function curFunction;
    private static ArrayList<BasicBlock> bbListOfCurFunc;
    private static HashMap<BasicBlock, ArrayList<BasicBlock>> preMap; // 前驱
    private static HashMap<BasicBlock, ArrayList<BasicBlock>> sucMap; // 后继

    private static HashMap<BasicBlock, ArrayList<BasicBlock>> domMap; // key支配value
    private static HashMap<BasicBlock, BasicBlock> sub2domOfIDom; // 一个sub只能有一个dom
    private static HashMap<BasicBlock, ArrayList<BasicBlock>> dom2subOfIDom; // 一个dom可以有多个sub

    private static HashMap<BasicBlock, ArrayList<BasicBlock>> dfMap;


    public static void run() {
        for (Function function : Optimizer.irModule.getFunctionList()) {
            curFunction = function;
            bbListOfCurFunc = function.getBBList();

            initDataStructure();
            getCFG();
            getDom();
            getDomTree();
            getDomFrontier();
        }
    }

    public static void initDataStructure() {
        preMap = new HashMap<>();
        sucMap = new HashMap<>();
        domMap = new HashMap<>();
        sub2domOfIDom = new HashMap<>();
        dom2subOfIDom = new HashMap<>();
        dfMap = new HashMap<>();
        for (BasicBlock bb : bbListOfCurFunc) {
            preMap.put(bb, new ArrayList<>());
            sucMap.put(bb, new ArrayList<>());
            domMap.put(bb, new ArrayList<>());
            sub2domOfIDom.put(bb, null);
            dom2subOfIDom.put(bb, new ArrayList<>());
            dfMap.put(bb, new ArrayList<>());
        }
    }

    public static void getCFG() {
        for (BasicBlock bb : bbListOfCurFunc) {
            Instruction lastInstr = bb.getLastInstr();
            if (lastInstr instanceof BrInstr) {
                BasicBlock targetBB = ((BasicBlock) ((BrInstr) lastInstr).getTargetBB());
                sucMap.get(bb).add(targetBB);
                preMap.get(targetBB).add(bb);
            } else if (lastInstr instanceof CondBrInstr) {
                BasicBlock falseBB = ((CondBrInstr) lastInstr).getFalseLabel();
                BasicBlock trueBB = ((CondBrInstr) lastInstr).getTrueLabel();

                sucMap.get(bb).add(trueBB);
                sucMap.get(bb).add(falseBB);

                preMap.get(trueBB).add(bb);
                preMap.get(falseBB).add(bb);
            }
        }
        for (BasicBlock bb : bbListOfCurFunc) {
            bb.setPre(preMap.get(bb));
            bb.setSuc(sucMap.get(bb));
        }
        curFunction.setPre(preMap);
        curFunction.setSuc(sucMap);
    }

    public static void getDom() {
        BasicBlock entry = bbListOfCurFunc.get(0);
        for (BasicBlock target : bbListOfCurFunc) {
            // 找到所有不被target支配的基本块，放入reachedSet中
            if ((target.getName().equals("bb_42") || target.getName().equals("bb_33"))) {
                System.out.println(1);
                target = target;
            }
            HashSet<BasicBlock> nonDomSet = new HashSet<>();
            getNonDomSet(entry, target, nonDomSet);
            // 取补集得到target支配的基本块集合
            ArrayList<BasicBlock> domList = new ArrayList<>();
            for (BasicBlock bb : bbListOfCurFunc) {
                if (!nonDomSet.contains(bb)) {
                    domList.add(bb);
                }
            }
            if ((target.getName().equals("bb_42") || target.getName().equals("bb_33"))) {
                System.out.println(1);
                target = target;
            }
            domMap.put(target, domList);
            target.setDom(domList);
        }
    }

    public static void getNonDomSet(BasicBlock entry, BasicBlock target, HashSet<BasicBlock> nonDomSet) {
        if (entry.equals(target)) {
            return;
        }
        nonDomSet.add(entry);
        for (BasicBlock sucBBOfEntry : entry.getSuc()) {
            if (!nonDomSet.contains(sucBBOfEntry)) {
                //nonDomSet.add(sucBBOfEntry);
                getNonDomSet(sucBBOfEntry, target, nonDomSet);
            }
        }
    }

    /**
     * 支配树由直接支配关系构建
     * 树的根是入口块，它支配所有其他基本块
     * 每个基本块的父节点是其直接支配者IDOM
     * 子节点是由该父节点直接支配的基本块
     */
    public static void getDomTree() {
        for (BasicBlock dom : bbListOfCurFunc) {
            for (BasicBlock sub : dom.getDom()) {
                if ((dom.getName().equals("bb_42") && sub.getName().equals("bb_33")) ||
                        (sub.getName().equals("bb_42") && dom.getName().equals("bb_33"))) {
                    System.out.println(1);
                }
                if (isIDom(dom, sub)) {
                    assert sub2domOfIDom.get(sub) == null;
                    sub2domOfIDom.put(sub, dom);
                    dom2subOfIDom.get(dom).add(sub);
                }
            }
        }
        for (BasicBlock bb : bbListOfCurFunc) {
            bb.setDomOfIDom(getDomOfIDom(bb));
            bb.setSubOfIDom(getSubListOfIDom(bb));
        }
        curFunction.setDom2subOfIDom(dom2subOfIDom);
        curFunction.setSub2domOfIDom(sub2domOfIDom);
    }

    public static BasicBlock getDomOfIDom(BasicBlock bb) {
        return sub2domOfIDom.get(bb);
    }

    public static ArrayList<BasicBlock> getSubListOfIDom(BasicBlock bb) {
        return dom2subOfIDom.get(bb);
    }

    /**
     * 特别地，dom和sub不能是同一个
     *
     * @param dom
     * @param sub
     * @return dom是否直接支配sub
     */
    public static boolean isIDom(BasicBlock dom, BasicBlock sub) {
        if (!dom.getDom().contains(sub) || dom.equals(sub)) {
            return false;
        }
        for (BasicBlock bb : dom.getDom()) {
            if (!bb.equals(dom) && !bb.equals(sub) && bb.getDom().contains(sub)) {
                return false; // 并非直接支配关系
            }
        }
        return true;
    }

    /**
     * 求A的支配边界集合，集合中有B：
     * A支配B的某个直接前驱P
     * A不支配B
     */
    public static void getDomFrontier() {
        for (Map.Entry<BasicBlock, ArrayList<BasicBlock>> entry : sucMap.entrySet()) {
            BasicBlock nodeA = entry.getKey();
            for (BasicBlock nodeB : entry.getValue()) {
                // 有边AB
                BasicBlock temp = nodeA;
                while (!temp.getDom().contains(nodeB) || temp.equals(nodeB)) {
                    // temp的支配边界包括B
                    // 42 33System.out.println(temp.getName());
                    dfMap.get(temp).add(nodeB); // 加入
                    temp = temp.getDomOfIDom(); // 沿支配树向上寻找
                }
            }
        }
        for (BasicBlock bb : bbListOfCurFunc) {
            bb.setDf(dfMap.get(bb));
        }
    }
}
