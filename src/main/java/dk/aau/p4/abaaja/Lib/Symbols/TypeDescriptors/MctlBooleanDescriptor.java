package dk.aau.p4.abaaja.Lib.Symbols.TypeDescriptors;

public class MctlBooleanDescriptor extends MctlTypeDescriptor {

    @Override
    public String get_type_literal() {
        return "BOOLEAN";
    }

    @Override
    public MctlBooleanDescriptor clone() {
        return new MctlBooleanDescriptor();
    }
}
