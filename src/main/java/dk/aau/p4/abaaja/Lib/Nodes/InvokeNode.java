package dk.aau.p4.abaaja.Lib.Nodes;

import dk.aau.p4.abaaja.Lib.Visitors.INodeVisitor;

public class InvokeNode extends StateNode {
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
