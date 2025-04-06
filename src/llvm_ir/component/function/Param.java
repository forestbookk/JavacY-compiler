package llvm_ir.component.function;

import llvm_ir.Value;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.LLVMType;
import llvm_ir.component.type.PointerType;

/**
 * Represents a parameter in an LLVM function.
 * A parameter can either be an array (represented as a pointer to an array)
 * or an integer (represented by an integer type such as i32).
 */
public class Param extends Value {

    /**
     * Constructs a parameter with the given name and type.
     *
     * @param name the name of the parameter.
     * @param type the type of the parameter, which can be:
     *             - PointerType if the parameter is an array.(由于数组长度不可确定，ArrayType的elementNum为 -1)
     *             - IntegerType if the parameter is an integer.
     */
    public Param(String name, LLVMType type) {
        super(name, type);
    }

    /**
     * Returns the string representation of the parameter in LLVM IR format.
     * The format is: `<type> <name>`.
     * For example:
     * - "i32 %param1" for an integer parameter.
     * - "[10 x i32]* %param2" for a pointer to an array.
     *
     * @return the LLVM IR string representation of the parameter.
     */
    @Override
    public String toString() {
        // Check if the type is a pointer type or an integer type
        if (type instanceof PointerType) {
            // PointerType represents an array (e.g., [10 x i32]*)
            return type + " " + name;
        } else if (type instanceof IntegerType) {
            // IntegerType represents an integer (e.g., i32)
            return type + " " + name;
        } else {
            throw new IllegalStateException("Unsupported parameter type: " + type);
        }
    }


}
