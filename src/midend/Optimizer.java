package midend;

import llvm_ir.component.Module;
import utils.ArgType;
import utils.Printer;

import java.io.IOException;

public class Optimizer {
    public static Module irModule;
    public static boolean isOn = true;

    public static void run() throws IOException {
        RemoveDeadCode.removeExtraBr();
        RemoveDeadCode.removeUnreachableBB();
        if (isOn) {
            Printer.solve(ArgType.LLVM);
            CFGBuilder.run();

            Mem2Reg.run();
            LiveVarAnalysis.run();
            new RegAllocator().performRegAllocation();

            Printer.solve(ArgType.LLVM);
            RemovePhi.run();

            Printer.solve(ArgType.LLVM);
        }
    }
}
