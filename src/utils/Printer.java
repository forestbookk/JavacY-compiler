package utils;

import backend.MipsBuilder;
import frontend.lexer.Lexer;
import frontend.parser.Parser;
import frontend.symbol.SymbolManager;
import llvm_ir.IRBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Printer {
    private static PrintWriter writer;

    private static String lex_corr_path = "lexer.txt";
    private static String parse_corr_path = "parser.txt";
    private static String symbol_corr_path = "symbol.txt";
    private static String llvm_corr_path = "llvm_ir.txt";
    private static String mips_corr_path = "mips.txt";
    private static String err_path = "error.txt";

    private static HashMap<Integer, String> ERRORS = new HashMap<>();

    public static void addError(int lineNo, ErrorType type) {
        if (ERRORS.containsKey(lineNo)) {
            return;
        }
        ERRORS.put(lineNo, type.toString());
    }

    public static void printError() throws IOException {
        List<Integer> sortedLineNoList = new ArrayList<>(ERRORS.keySet());
        Collections.sort(sortedLineNoList);
        writer = new PrintWriter(new FileWriter(err_path));
        for (Integer lineNo : sortedLineNoList) {
            writer.println(lineNo + " " + ERRORS.get(lineNo));
        }

        // DEBUG
        for (Integer lineNo : sortedLineNoList) {
            System.out.println(lineNo + " " + ERRORS.get(lineNo));
        }
    }

    public static void solve(ArgType type) throws IOException {
        switch (type) {
            case LEXER -> {
                if (ERRORS.isEmpty()) {
                    writer = new PrintWriter(new FileWriter(lex_corr_path));
                    Lexer.getTokenList().forEach(writer::println);
                } else {
                    printError();
                }
            }
            case PARSER -> {
                if (ERRORS.isEmpty()) {
                    writer = new PrintWriter(new FileWriter(parse_corr_path));
                    printParserBuffer(Parser.getOutputBuffer());
                } else {
                    printError();
                }
                // DEBUG
                Parser.printBuffer();
            }
            case SYMBOL -> {
                if (ERRORS.isEmpty()) {
                    writer = new PrintWriter(new FileWriter(symbol_corr_path));
                    SymbolManager.getAllSymbols().forEach(writer::println);
                    // DEBUG
                    SymbolManager.getAllSymbols().forEach(System.out::println);
                } else {
                    printError();
                }
            }
            case LLVM -> {
                if (ERRORS.isEmpty()) {
                    writer = new PrintWriter(new FileWriter(llvm_corr_path));
                    writer.println(IRBuilder.getModule().toString());
                    // DEBUG
                    System.out.println(IRBuilder.getModule().toString());
                } else {
                    printError();
                }
            }
            case MIPS, OPTIMIZE -> {
                if (ERRORS.isEmpty()) {
                    writer = new PrintWriter(new FileWriter(mips_corr_path));
                    writer.println(MipsBuilder.getModule().toString());
                    // DEBUG
                    System.out.println(MipsBuilder.getModule().toString());
                    //writer = new PrintWriter(new FileWriter(llvm_corr_path));
                    //writer.println(IRBuilder.getModule().toString());
                } else {
                    printError();
                }
            }
        }
        close();
    }

    public static void close() {
        if (writer != null) {
            writer.close();
        }
    }

    public static void printParserBuffer(ArrayList<String> buffers) {
        buffers.forEach(writer::println);
    }
}
