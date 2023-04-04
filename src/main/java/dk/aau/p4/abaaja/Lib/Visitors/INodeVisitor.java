package dk.aau.p4.abaaja.Lib.Visitors;

import dk.aau.p4.abaaja.Lib.Nodes.*;

public interface INodeVisitor {
    void visit(MctlNode node);
    void visit(LineNode node);

    void visit(BlockNode node);

    void visit(DecNode node);
    void visit(StateNode node);

    void visit(VarDecNode node);
    void visit(FuncDecNode node);
    void visit(StructDecNode node);

    void visit(IfStateNode node);
    void visit(RepeatStateNode node);
    void visit(AssStateNode node);
    void visit(InvokeNode node);
    void visit(ReturnNode node);

    void visit(ParamNode node);
    void visit(StopNode node);
    void visit(TypeNode node);

    void visit(ExpNode node);
    void visit(MulExpNode node);
    void visit(AddExpNode node);
    void visit(AndExpNode node);
    void visit(OrExpNode node);
    void visit(CompExpNode node);
    void visit(EqualExpNode node);
    void visit(ParenExpNode node);
    void visit(TypecastExpNode node);
    void visit(UnaryExpNode node);

    void visit(IDExpNode node);
    void visit(IDTypeNode node);
    void visit(BoolExpNode node);
    void visit(BoolTypeNode node);
    void visit(NumExpNode node);
    void visit(NumTypeNode node);
    void visit(StringExpNode node);
    void visit(StringTypeNode node);
    
    void visit(NothingTypeNode node);

}