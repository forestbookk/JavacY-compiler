package backend.component;

import backend.MipsNode;
import frontend.symbol.symbols.Var.Char;
import llvm_ir.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Global implements MipsNode {
    public static class Asciiz extends Global {
        private String name;
        private String content;

        public Asciiz(String name, String content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String toString() {
            return name + ": .asciiz \"" + content.replace("\n", "\\n") + "\"";
        }
    }

    public static class Word extends Global {
        private String name;
        private ArrayList<Integer> values;


        public Word(String name, ArrayList<Integer> values) {
            this.name = name;
            this.values = values;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append(": .").append("word").append(" ");
            for (int i = 0; i < values.size(); i++) {
                sb.append(values.get(i).toString());
                if (i != values.size() - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
    }
}
