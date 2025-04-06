import backend.MipsBuilder;
import frontend.lexer.Lexer;
import frontend.parser.AST.Node;
import frontend.parser.Parser;
import llvm_ir.IRBuilder;
import midend.Optimizer;
import utils.ArgType;
import utils.Printer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

public class Compiler {
    public static void main(String[] args) throws IOException {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(new FileInputStream("testfile.txt"), 16);
        Lexer lexer = new Lexer(pushbackInputStream);
        ArgType arg = ArgType.OPTIMIZE;
        Lexer.TRICK = true;

        switch (arg) {
            case LEXER -> {
                lexer.solve();
                Printer.solve(arg);
            }
            case PARSER -> {
                lexer.solve();
                Parser parser = new Parser();
                parser.parseCompUnit();
                Printer.solve(arg);
            }
            case SYMBOL -> {
                lexer.solve();
                Parser parser = new Parser();
                Node compUnit = parser.parseCompUnit();
                compUnit.analyseSemantic();
                Printer.solve(arg);
            }
            case LLVM -> {
                lexer.solve();
                Parser parser = new Parser();
                Node compUnit = parser.parseCompUnit();
                compUnit.analyseSemantic();
                compUnit.genIR();
                Printer.solve(arg);
            }
            case MIPS -> {
                IRBuilder.OPTIMIZE_MODE = false;
                lexer.solve();
                Parser parser = new Parser();
                Node compUnit = parser.parseCompUnit();
                compUnit.analyseSemantic();
                compUnit.genIR();
                Printer.solve(ArgType.LLVM);
                MipsBuilder.init();
                IRBuilder.getModule().toAssembly();
                MipsBuilder.close();
                Printer.solve(arg);
            }
            case OPTIMIZE -> {
                IRBuilder.OPTIMIZE_MODE = true;
                lexer.solve();
                Parser parser = new Parser();
                Node compUnit = parser.parseCompUnit();
                compUnit.analyseSemantic();

                compUnit.genIR();
                Printer.solve(ArgType.LLVM);

                Optimizer.irModule = IRBuilder.getModule();
                Optimizer.run();

                MipsBuilder.init();
                IRBuilder.getModule().toAssembly();
                MipsBuilder.close();
                Printer.solve(arg);
            }
        }
        pushbackInputStream.close();
    }
}
