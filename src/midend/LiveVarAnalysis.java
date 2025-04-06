package midend;

import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import llvm_ir.component.Module;
import llvm_ir.component.function.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class LiveVarAnalysis {
    private static HashMap<BasicBlock, HashSet<Value>> inMap;
    private static HashMap<BasicBlock, HashSet<Value>> outMap;
    private static ArrayList<BasicBlock> bbListOfCurFunc;

    public static void run() {
        for (Function function : Optimizer.irModule.getFunctionList()) {
            bbListOfCurFunc = function.getBBList();
            initInOutMap();
            for (BasicBlock bb : bbListOfCurFunc) {
                bb.computeDefUse();
            }
            computeInOut();
        }
    }

    public static void initInOutMap() {
        inMap = new HashMap<>();
        outMap = new HashMap<>();
        for (BasicBlock bb : bbListOfCurFunc) {
            inMap.put(bb, new HashSet<>());
            outMap.put(bb, new HashSet<>());
        }
    }

    public static void computeInOut() {
        boolean isInChanged = true;
        while (isInChanged) {
            isInChanged = false;
            for (int i = bbListOfCurFunc.size() - 1; i >= 0; i--) {
                BasicBlock bb = bbListOfCurFunc.get(i);

                // out[bb] = 并_{B的后继基本块sucBB}^in[sucBB]
                HashSet<Value> out = new HashSet<>();
                for (BasicBlock sucBB : bb.getSuc()) {
                    out.addAll(inMap.get(sucBB));
                }
                outMap.put(bb, out);

                // in[bb]=use[bb]并(out[bb]-def[bb])
                HashSet<Value> in = new HashSet<>(bb.getUse());
                in.addAll(outMap.get(bb));
                in.removeAll(bb.getDef());

                // 比较计算得到的bb的in[B]与此前计算得出的该基本块in[B]不同，则循环继续
                if (!in.equals(inMap.get(bb))) {
                    inMap.put(bb, in);
                    isInChanged = true;
                }
            }
        }

        for (BasicBlock bb : bbListOfCurFunc) {
            bb.setIn(inMap.get(bb));
            bb.setOut(outMap.get(bb));
        }
    }

}
