package utils;

public enum SyntaxType {
    CompUnit,

    Decl,
    ConstDecl,
    VarDecl,

    FuncDef,
    MainFuncDef,
    ConstDef,
    VarDef,

    BType,
    FuncType,
    FuncFParams,
    FuncFParam,
    FuncRParams,

    Block,
    BlockItem,

    InitVal,
    ConstInitVal,

    Stmt,
    ForStmt,

    Exp,
    Cond,
    LVal,
    PrimaryExp,
    Number,
    Character,
    UnaryExp,
    UnaryOp,
    MulExp,
    AddExp,
    RelExp,
    EqExp,
    LAndExp,
    LOrExp,
    ConstExp,

    Leaf
}
