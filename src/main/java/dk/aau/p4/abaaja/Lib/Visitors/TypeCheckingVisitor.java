package dk.aau.p4.abaaja.Lib.Visitors;

import dk.aau.p4.abaaja.Lib.Nodes.*;
import dk.aau.p4.abaaja.Lib.ProblemHandling.ProblemCollection;
import dk.aau.p4.abaaja.Lib.ProblemHandling.ProblemType;
import dk.aau.p4.abaaja.Lib.Symbols.FuncSymbol;
import dk.aau.p4.abaaja.Lib.Symbols.Symbol;
import dk.aau.p4.abaaja.Lib.Symbols.SymbolTable;
import dk.aau.p4.abaaja.Lib.Symbols.TypeDescriptors.*;
import dk.aau.p4.abaaja.mctlParser;

import java.util.ArrayList;
import java.util.List;

public class TypeCheckingVisitor {
    private final ProblemCollection _problemCollection;
    private final SymbolTable _symbolTable;

    public TypeCheckingVisitor(ProblemCollection problemCollection, SymbolTable symbolTable) {
        this._problemCollection = problemCollection;
        this._symbolTable = symbolTable;
    }

    public MctlTypeDescriptor visit(DecNode node) {
        MctlTypeDescriptor typeDescriptor = null;

        if (node instanceof VarDecNode varDecNode) { typeDescriptor = visit(varDecNode); }
        else if (node instanceof FuncDecNode funcDecNode) { typeDescriptor = visit(funcDecNode); }
        else if (node instanceof StructDecNode structDecNode) { typeDescriptor = visit(structDecNode); }

        return typeDescriptor;
    }

    public MctlTypeDescriptor visit(StateNode node) {
        MctlTypeDescriptor typeDescriptor = null;

        if (node instanceof IfStateNode ifStateNode) { typeDescriptor = visit(ifStateNode); }
        else if (node instanceof AssStateNode assStateNode) { typeDescriptor = visit(assStateNode); }
        else if (node instanceof FuncInvokeNode funcInvokeNode) { typeDescriptor = visit(funcInvokeNode); }
        else if (node instanceof InvokeNode invokeNode) { typeDescriptor = visit(invokeNode); }
        else if (node instanceof RepeatStateNode repeatStateNode) { typeDescriptor = visit(repeatStateNode); }
        else if (node instanceof ReturnNode returnNode) { typeDescriptor = visit(returnNode); }
        else if (node instanceof StopNode stopNode) { typeDescriptor = visit(stopNode); }

        return typeDescriptor;
    }

    public MctlTypeDescriptor visit(IfStateNode node) {
        MctlTypeDescriptor typeDescriptor = _symbolTable.searchType("BOOLEAN");

        for (ExpNode expNode : node.get_expChildren()) {
            MctlTypeDescriptor tempTypeDescriptor = visit(expNode);
            if (!tempTypeDescriptor.get_type_literal().equals("BOOLEAN")) {
                _problemCollection.addFormattedProblem(
                        ProblemType.ERROR_TYPE_MISMATCH,
                        "Expected type BOOLEAN but got: " + tempTypeDescriptor.get_type_literal(),
                        expNode.get_lineNumber()
                );

                typeDescriptor = _symbolTable.searchType("NOTHING");
            }
        }

        return typeDescriptor;
    }

    public MctlTypeDescriptor visit(RepeatStateNode node) {
        String typeLiteral = visit(node.get_repeatExp()).get_type_literal();

        if (!(typeLiteral.equals("BOOLEAN") || typeLiteral.equals("NUMBER"))) {
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_TYPE_MISMATCH,
                    "Expected type BOOLEAN or NUMBER but got: " + typeLiteral,
                    node.get_lineNumber()
            );
        }

        return _symbolTable.searchType(typeLiteral);
    }

    public MctlTypeDescriptor visit(InvokeExpNode node) { return visit(node.getInvokeNode()); }

    public MctlTypeDescriptor visit(InvokeNode node) {
        MctlTypeDescriptor typeDescriptor = null;

        if (node instanceof StringMethodInvokeNode stringMethodInvokeNode) { typeDescriptor = visit(stringMethodInvokeNode); }
        else if (node instanceof VarMethodInvokeNode varMethodInvokeNode) { typeDescriptor = visit(varMethodInvokeNode); }
        else if (node instanceof FuncInvokeNode funcInvokeNode) { typeDescriptor = visit(funcInvokeNode); }


        return typeDescriptor;
    }

    public MctlTypeDescriptor visit(TypeNode node) {
        MctlTypeDescriptor typeDescriptor = null;

        if (node instanceof BoolTypeNode boolTypeNode) { typeDescriptor = visit(boolTypeNode); }
        else if (node instanceof NumTypeNode numTypeNode) { typeDescriptor = visit(numTypeNode); }
        else if (node instanceof StringTypeNode stringTypeNode) { typeDescriptor = visit(stringTypeNode); }
        else if (node instanceof NothingTypeNode nothingTypeNode) { typeDescriptor = visit(nothingTypeNode); }
        else if (node instanceof IDTypeNode idTypeNode) { typeDescriptor = visit(idTypeNode); }
        return typeDescriptor;
    }

    public MctlTypeDescriptor visit(IDTypeNode node) {
        MctlTypeDescriptor typeDescriptor = _symbolTable.searchType(node.get_type());

        if (typeDescriptor == null) {
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_UNKNOWN_TYPE,
                    "The type: " + node.get_type() + " has not been declared",
                    node.get_lineNumber()
            );
        } else if (node.get_arrayDegree() != 0) {
            typeDescriptor = new MctlArrayTypeDescriptor(typeDescriptor, node.get_arrayDegree());
        }

        return typeDescriptor;
    }

    public MctlTypeDescriptor visit(BinaryExpNode node) {
        MctlTypeDescriptor typeDescriptor = null;

        if (node instanceof AddExpNode addExpNode) { typeDescriptor = visit(addExpNode); }
        else if (node instanceof AndExpNode andExpNode) { typeDescriptor = visit(andExpNode); }
        else if (node instanceof CompExpNode compExpNode) { typeDescriptor = visit(compExpNode); }
        else if (node instanceof EqualExpNode equalExpNode) { typeDescriptor = visit(equalExpNode); }
        else if (node instanceof MulExpNode mulExpNode) { typeDescriptor = visit(mulExpNode); }
        else if (node instanceof OrExpNode orExpNode) { typeDescriptor = visit(orExpNode); }

        return typeDescriptor;
    }

    public MctlTypeDescriptor visit(EqualExpNode node) {
        MctlTypeDescriptor type1 = visit((ExpNode) node.get_children().get(0));
        MctlTypeDescriptor type2 = visit((ExpNode) node.get_children().get(1));

        if (type1.get_type_literal().equals("NOTHING") || type2.get_type_literal().equals("NOTHING")) {
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_VARIABLE_NOT_INSTANTIATED,
                    "Expression contains a variable that has not been instantiated.",
                    node.get_lineNumber()
            );

            return _symbolTable.searchType("NOTHING");
        }
        else if (!type1.get_type_literal().equals(type2.get_type_literal())) {
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_TYPE_MISMATCH,
                    "Expected both expressions to be of the same type, but got type: \"" + type1 + "\" and type: \"" + type2 + "\"",
                    node.get_lineNumber()
            );

            return _symbolTable.searchType("NOTHING");
        }

        return _symbolTable.searchType("BOOLEAN");
    }

    public MctlTypeDescriptor visit(IDExpNode node) {
        MctlTypeDescriptor typeDescriptor = _symbolTable.searchType("NOTHING");

        if (node instanceof IDArrayExpNode idArrayExpNode) { typeDescriptor = visit(idArrayExpNode); }
        else if (node instanceof IDStructNode idStructNode) { typeDescriptor = visit(idStructNode); }
        else if (node instanceof ActualIDExpNode actualIDExpNode) { typeDescriptor = visit(actualIDExpNode); }

        return typeDescriptor;
    }

    public MctlTypeDescriptor visit(IDArrayExpNode node) {
        Symbol symbol = _symbolTable.searchSymbol(node.get_contained_id());
        MctlTypeDescriptor accessorArrayTypeDescriptor = _symbolTable.searchType("NOTHING");

        if ((symbol != null && !symbol.get_isInstantiated()) || symbol == null) {
            return accessorArrayTypeDescriptor;
        }

        // Counting array nodes
        int arrayDegree = 0;
        IDExpNode tempIdNode = node;
        while (tempIdNode instanceof IDArrayExpNode) {
            arrayDegree++;
            tempIdNode = tempIdNode.get_idNode();
        }

        if (symbol.get_type() instanceof MctlArrayTypeDescriptor arrayTypeDescriptor) {
            int accessorDegree = arrayTypeDescriptor.getDegree() - arrayDegree;

            // The contained type is a struct type
            if (arrayTypeDescriptor.getType() instanceof MctlStructDescriptor && tempIdNode instanceof  IDStructNode idStructNode) {
                // Struct Type
                MctlTypeDescriptor descriptor = getStructDerivedType(arrayTypeDescriptor, idStructNode);

                if (descriptor instanceof MctlArrayTypeDescriptor arrayDescriptor) {
                    accessorDegree = arrayDescriptor.getDegree() - arrayDegree;

                    // The type referred to is a primitive type
                    accessorArrayTypeDescriptor = getArrayType(arrayDescriptor, accessorDegree, node.get_lineNumber());
                }
            }
            else if (arrayTypeDescriptor.getType() instanceof MctlStructDescriptor && tempIdNode instanceof  ActualIDExpNode actualIDExpNode) {
                accessorDegree = arrayTypeDescriptor.getDegree() - arrayDegree;

                // The type referred to is a primitive type
                accessorArrayTypeDescriptor = getArrayType(arrayTypeDescriptor, accessorDegree, node.get_lineNumber());
            }
            else {
                // The type referred to is a primitive type
                accessorArrayTypeDescriptor = getArrayType(arrayTypeDescriptor, accessorDegree, node.get_lineNumber());
            }
        }
        else if (symbol.get_type() instanceof MctlStructDescriptor structTypeDescriptor) {
            MctlTypeDescriptor derivedType = getStructDerivedType(structTypeDescriptor, tempIdNode);

            if (arrayDegree > 0 && !(derivedType instanceof MctlArrayTypeDescriptor)) {
                _problemCollection.addFormattedProblem(
                        ProblemType.ERROR_TYPE_MISMATCH,
                        "The type \"" + derivedType.get_type_literal() + "\" cannot be accessed in array degree: " + arrayDegree,
                        node.get_lineNumber()
                );
                return _symbolTable.searchType("NOTHING");
            } else if (arrayDegree > 0 && derivedType instanceof MctlArrayTypeDescriptor derivedArrayType) {
                // Calculating the accessory array degree
                int accessorDegree = derivedArrayType.getDegree() - arrayDegree;

                // The type referred to is not an array
                accessorArrayTypeDescriptor = getArrayType(derivedArrayType, accessorDegree, node.get_lineNumber());
            }
            else {
                accessorArrayTypeDescriptor = derivedType;
            }
        }
        else {
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_TYPE_MISMATCH,
                    "The variable \"" + symbol.get_name() + "\" is not of the correct type",
                    node.get_lineNumber()
            );
        }

        return accessorArrayTypeDescriptor;
    }

    private MctlTypeDescriptor getArrayType(MctlArrayTypeDescriptor descriptor, int degree, int lineNumber) {
        MctlTypeDescriptor accessorType = _symbolTable.searchType("NOTHING");

        // The type referred to is not an array
        if (degree == 0) {
            accessorType = descriptor.getType();
        } else if (degree < 0) {
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_TYPE_MISMATCH,
                    "It is not possible to access the degree \"" + degree + "\" on the type \"" + descriptor.get_type_literal() + "\"",
                    lineNumber
            );
        } else {
            accessorType = new MctlArrayTypeDescriptor(descriptor.getType(), degree);
        }

        return accessorType;
    }

    private MctlTypeDescriptor getStructDerivedType(MctlTypeDescriptor parsedStructDescriptor, IDExpNode idNode) {
        IDExpNode idExpNode = idNode;
        MctlTypeDescriptor accessorType = parsedStructDescriptor;

        List<IDExpNode> nodeList = new ArrayList<>();

        // Add all IdExpNodes to a list in bottom up order
        while(!(idExpNode instanceof ActualIDExpNode)) {
            nodeList.add(0, idExpNode);
            idExpNode = idExpNode.get_idNode();
        }

        // Iterate over each element in the IdExpNodeElement
        for (IDExpNode idExp : nodeList) {
            if (idExp instanceof IDStructNode tempIDStructNode) {
                if (accessorType instanceof MctlStructDescriptor accessorDescriptor) {
                    accessorType = accessorDescriptor.get_structsymbol(tempIDStructNode.get_accessor().get_contained_id());

                    // Trying to access a field that is not declared on the specific struct type
                    if (accessorType == null) {
                        _problemCollection.addFormattedProblem(
                                ProblemType.ERROR_UNDEFINED_IDENTIFIER,
                                "The type \"" + accessorDescriptor.get_type_literal() + "\" does not contain a field \"" + tempIDStructNode.get_accessor().get_contained_id() + "\"",
                                tempIDStructNode.get_lineNumber()
                        );
                        return _symbolTable.searchType("NOTHING");
                    }
                } else {
                    // Happens if user is trying to access a struct on a type that is not a struct and so on
                    _problemCollection.addFormattedProblem(
                            ProblemType.ERROR_TYPE_MISMATCH,
                            "The type \"" + accessorType.get_type_literal() + "\" cannot be accessed as a struct",
                            idExp.get_lineNumber()
                    );

                    return _symbolTable.searchType("NOTHING");
                }
            } else if (idExp instanceof IDArrayExpNode) {
                if (accessorType instanceof MctlArrayTypeDescriptor accessorDescriptor) {
                    int accessorDegree = accessorDescriptor.getDegree() - 1;

                    accessorType = getArrayType(accessorDescriptor, accessorDegree, idExp.get_lineNumber());
                } else {
                    // Happens if user is trying to access an array type on a type that is not an array
                    _problemCollection.addFormattedProblem(
                            ProblemType.ERROR_TYPE_MISMATCH,
                            "The type \"" + accessorType.get_type_literal() + "\" cannot be accessed using []",
                            idExp.get_lineNumber()
                    );

                    return _symbolTable.searchType("NOTHING");
                }
            }
        }

        return accessorType;
    }

    public MctlTypeDescriptor visit(ActualIDExpNode node) {
        Symbol symbol = _symbolTable.searchSymbol(node.get_id());

        if ((symbol != null && !symbol.get_isInstantiated()) || symbol == null) {
            return _symbolTable.searchType("NOTHING");
        }

        return symbol.get_type();
    }

    public MctlTypeDescriptor visit(IDStructNode node) {
        Symbol symbol = _symbolTable.searchSymbol(node.get_contained_id());
        MctlTypeDescriptor type;

        // Return if the variable has not yet been instantiated
        if ((symbol != null && !symbol.get_isInstantiated()) || symbol == null) {
            return _symbolTable.searchType("NOTHING");
        }

        type = symbol.get_type();

        // Check if the root type is a Struct or an Array
        if (symbol.get_type() instanceof MctlArrayTypeDescriptor arrayTypeDescriptor &&
                arrayTypeDescriptor.getType() instanceof MctlStructDescriptor) {
            // The contained type is a struct type
            type = getStructDerivedType(arrayTypeDescriptor, node);
        }
        else if (type instanceof MctlStructDescriptor structTypeDescriptor) {
            type = getStructDerivedType(structTypeDescriptor, node);
        }
        else {
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_TYPE_MISMATCH,
                    "The variable \"" + symbol.get_name() + "\" is not of the correct type",
                    node.get_lineNumber()
            );
        }

        return type;
    }

    public MctlTypeDescriptor visit(ExpNode node) {
        MctlTypeDescriptor typeDescriptor = null;

        if (node instanceof IDExpNode idExpNode) { typeDescriptor = visit(idExpNode); }
        else if (node instanceof BinaryExpNode binaryExpNode) { typeDescriptor = visit(binaryExpNode); }
        else if (node instanceof BoolExpNode boolExpNode) { typeDescriptor = visit(boolExpNode); }
        else if (node instanceof InvokeExpNode invokeExpNode) { typeDescriptor = visit(invokeExpNode); }
        else if (node instanceof NumExpNode numExpNode) { typeDescriptor = visit(numExpNode); }
        else if (node instanceof StringExpNode stringExpNode) { typeDescriptor = visit(stringExpNode); }
        else if (node instanceof TypecastExpNode typecastExpNode) { typeDescriptor = visit(typecastExpNode); }
        else if (node instanceof UnaryExpNode unaryExpNode) { typeDescriptor = visit(unaryExpNode); }

        return typeDescriptor;
    }

    private MctlTypeDescriptor getTypecastResult(MctlTypeDescriptor newType, MctlTypeDescriptor previousType, String correctType, int lineNumber) {
        MctlTypeDescriptor returnType = _symbolTable.searchType("NOTHING");

        if (newType.get_type_literal().equals(correctType)) {
            returnType = newType;
        } else if (previousType.get_type_literal().equals(newType.get_type_literal())) {
            _problemCollection.addFormattedProblem(
                    ProblemType.WARNING_REDUNDANT_TYPECAST,
                    "Typecasting the type \"" + previousType.get_type_literal() + "\" to the type \"" + newType.get_type_literal() + "\" is redundant",
                    lineNumber
            );
            returnType = newType;
        } else {
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_TYPE_CANNOT_BE_CAST,
                    "The type \"" + previousType.get_type_literal() + "\" cannot be cast to" + newType.get_type_literal(),
                    lineNumber
            );
        }

        return returnType;
    }

    public MctlTypeDescriptor visit(TypecastExpNode node) {
        MctlTypeDescriptor newType = visit(node.get_typeNode());
        MctlTypeDescriptor previousType = visit(node.get_expression_node());
        MctlTypeDescriptor returnType = _symbolTable.searchType("NOTHING");

        // Switch over the previous type and check whether the typecast is legal
        switch (previousType.get_type_literal()) {
            case "STRING" ->
                    returnType = getTypecastResult(newType, previousType, "NUMBER", node.get_lineNumber());
            case "NUMBER", "BOOLEAN" ->
                    returnType = getTypecastResult(newType, previousType, "STRING", node.get_lineNumber());
            default ->
                    _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_TYPE_CANNOT_BE_CAST,
                    "The struct type \"" + previousType.get_type_literal() + "\" cannot be cast",
                    node.get_lineNumber()
            );
        }

        return returnType;
    }

    public MctlTypeDescriptor visit(CompExpNode node) {
        MctlTypeDescriptor typeDescriptor = expectsType(node, "NUMBER");
        return (typeDescriptor.get_type_literal().equals("NUMBER") ? _symbolTable.searchType("BOOLEAN") : typeDescriptor);
    }

    public MctlTypeDescriptor visit(MulExpNode node) { return expectsType(node, "NUMBER"); }
    public MctlTypeDescriptor visit(AddExpNode node) { return expectsType(node, "NUMBER"); }
    public MctlTypeDescriptor visit(AndExpNode node) { return expectsType(node, "BOOLEAN"); }
    public MctlTypeDescriptor visit(OrExpNode node) { return expectsType(node, "BOOLEAN"); }

    public MctlTypeDescriptor getPrimitiveType(TypeNode node, MctlTypeDescriptor type) {
        MctlTypeDescriptor typeDescriptor;

        // Check if the type is an array
        if (node.get_arrayDegree() > 0) {
            typeDescriptor = new MctlArrayTypeDescriptor(type, node.get_arrayDegree());
        } else {
            typeDescriptor = type;
        }

        return typeDescriptor;
    }

    public MctlTypeDescriptor visit(BoolTypeNode node) {
        MctlTypeDescriptor typeDescriptor = _symbolTable.searchType("BOOLEAN");
        return getPrimitiveType(node, typeDescriptor);
    }
    public MctlTypeDescriptor visit(NumTypeNode node) {
        MctlTypeDescriptor typeDescriptor = _symbolTable.searchType("NUMBER");
        return getPrimitiveType(node, typeDescriptor);
    }
    public MctlTypeDescriptor visit(StringTypeNode node) {
        MctlTypeDescriptor typeDescriptor = _symbolTable.searchType("STRING");
        return getPrimitiveType(node, typeDescriptor);
    }

    public MctlTypeDescriptor visit(NothingTypeNode node) { return _symbolTable.searchType("NOTHING"); }
    public MctlTypeDescriptor visit(BoolExpNode node) { return _symbolTable.searchType("BOOLEAN"); }
    public MctlTypeDescriptor visit(NumExpNode node) { return _symbolTable.searchType("NUMBER"); }
    public MctlTypeDescriptor visit(StringExpNode node) { return _symbolTable.searchType("STRING"); }
    public MctlTypeDescriptor visit(UnaryExpNode node) {
        MctlTypeDescriptor descriptor = visit(node.get_unaryExp());

        if (node.get_operator() == mctlParser.NOT && !(descriptor instanceof MctlBooleanDescriptor)) {
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_TYPE_MISMATCH,
                    "The unary operator \"!\" can only be used on expressions of type \"BOOLEAN\" but got \"" + descriptor.get_type_literal() + "\"",
                    node.get_lineNumber()
            );
            descriptor = _symbolTable.searchType("NOTHING");
        } else if ((node.get_operator() == mctlParser.PLUS || node.get_operator() == mctlParser.MINUS) &&
                !(descriptor instanceof  MctlNumberDescriptor)) {
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_TYPE_MISMATCH,
                    "The unary operator \"" + (node.get_operator() == mctlParser.PLUS ? "+" : "-") + "\" can only be used on expressions of type \"NUMBER\" but got \"" + descriptor.get_type_literal() + "\"",
                    node.get_lineNumber()
            );
            descriptor = _symbolTable.searchType("NOTHING");
        }

        return descriptor;
    }
    public MctlTypeDescriptor visit(ReturnNode node) { return visit(node.get_returnExp()); }

    public MctlTypeDescriptor visit(MctlNode node) { return null; }
    public MctlTypeDescriptor visit(LineNode node) { return null; }
    public MctlTypeDescriptor visit(BlockNode node) { return null; }

    public MctlTypeDescriptor visit(FuncInvokeNode node) {
        Symbol symbol = _symbolTable.searchSymbol(node.get_id().get_id());

        if (symbol == null) {
            // Function has not been declared
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_UNDEFINED_IDENTIFIER,
                    "The function \"" + node.get_id().get_id() + "\" has not yet been declared",
                    node.get_lineNumber()
            );
        } else if (!(symbol instanceof FuncSymbol)) {
            // ID refers to variable
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_ID_NOT_FUNCTION,
                    "The ID \"" + node.get_id().get_id() + "\" refers to a variable and can therefore not be invoked",
                    node.get_lineNumber()
            );
        } else {
            // Is valid function symbol
            FuncSymbol funcSymbol = (FuncSymbol) symbol;
            if (funcSymbol.getIsStringFunction() || funcSymbol.getIsVarFunction()) {
                // Function should be called on string or var
                _problemCollection.addFormattedProblem(
                        ProblemType.ERROR_UTILITY_FUNCTION_INVOKED_IN_WRONG_CONTEXT,
                        "The function \"" + node.get_id().get_id() + "\" is called in the wrong context",
                        node.get_lineNumber()
                );
            } else if ((funcSymbol.get_types().size() == 0 && node.get_paramExps().size() != 0) || funcSymbol.get_types().size() != node.get_paramExps().size()) {
                // Number of parameters does not match
                _problemCollection.addFormattedProblem(
                        ProblemType.ERROR_PARAMETERS_DOES_NOT_MATCH,
                        "The provided number of parameters: " + node.get_paramExps().size() + " does not match the expected: " + funcSymbol.get_types().size() + " parameters",
                        node.get_lineNumber()
                );
            } else {
                // Check function parameters
                _checkFunctionParams(node.get_paramExps(), funcSymbol, node.get_lineNumber());
            }

            return funcSymbol.get_type();
        }

        return _symbolTable.searchType("NOTHING");
    }

    public MctlTypeDescriptor visit(VarMethodInvokeNode node) {
        Symbol symbol = _symbolTable.searchSymbol(node.get_id().get_id());

        if (symbol == null) {
            // Function does not exist
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_UNDEFINED_IDENTIFIER,
                    "The function \"" + node.get_id().get_id() + "\" doesnt exist",
                    node.get_lineNumber()
            );
        } else if (!(symbol instanceof FuncSymbol)) {
            // ID refers to variable
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_ID_NOT_FUNCTION,
                    "The ID \"" + node.get_id().get_id() + "\" refers to a variable and can therefore not be invoked",
                    node.get_lineNumber()
            );
        } else {
            // Is valid function symbol
            FuncSymbol funcSymbol = (FuncSymbol) symbol;
            MctlTypeDescriptor expectedVarType = funcSymbol.getExpectedVarType();

            // Special case for the add function as it potentially instantiates variables;
            if (funcSymbol.get_name().equals("add")) {
                Symbol varSymbol = _symbolTable.searchSymbol(node.get_varId().get_contained_id());
                varSymbol.set_isInstantiated(true);
            }
            MctlTypeDescriptor varType = visit(node.get_varId());

            // Check if the var types match
            boolean notDeclared = (expectedVarType == null || varType == null);
            boolean typesMatch = false;

            // Check if types match (If they are not null)
            if (!notDeclared) {
                if (expectedVarType.get_type_literal().equals(varType.get_type_literal())) {
                    typesMatch = true;
                } else if (funcSymbol.getIsStringFunction() && varType.get_type_literal().equals("STRING")) {
                    typesMatch = true;
                } else if (varType instanceof MctlArrayTypeDescriptor varArrayType &&
                        expectedVarType instanceof MctlArrayTypeDescriptor expectedVarArrayType) {
                    if (expectedVarArrayType.get_contained_type_literal().equals("ANY") || varArrayType.get_contained_type_literal().equals(expectedVarArrayType.get_contained_type_literal())) {
                        typesMatch = true;
                    }
                }
            }

            if (!funcSymbol.getIsVarFunction() ) {
                // Function should be called on var
                _problemCollection.addFormattedProblem(
                        ProblemType.ERROR_UTILITY_FUNCTION_INVOKED_IN_WRONG_CONTEXT,
                        "The function \"" + node.get_id().get_id() + "\" cannot be called on variables",
                        node.get_lineNumber()
                );
            } else if (notDeclared) {
                // Variable has not been declared
                _problemCollection.addFormattedProblem(
                        ProblemType.ERROR_UNDEFINED_IDENTIFIER,
                        "The function \"" + node.get_id().get_id() + "\" cannot be called on undeclared variables",
                        node.get_lineNumber()
                );
            } else if (!typesMatch) {
                // Type is not the expected
                _problemCollection.addFormattedProblem(
                        ProblemType.ERROR_TYPE_MISMATCH,
                        "Unsupported variable type \"" + varType.get_type_literal() + "\" for function " + node.get_id().get_id(),
                        node.get_lineNumber()
                );
            } else if ((funcSymbol.get_types().size() == 0 && node.get_paramExps().size() != 0) || funcSymbol.get_types().size() != node.get_paramExps().size()) {
                // Number of parameters does not match
                _problemCollection.addFormattedProblem(
                        ProblemType.ERROR_PARAMETERS_DOES_NOT_MATCH,
                        "The provided number of parameters: " + node.get_paramExps().size() + " does not match the expected: " + funcSymbol.get_types().size() + " parameters",
                        node.get_lineNumber()
                );
            } else {
                // Check function parameters
                _checkFunctionParams(node.get_paramExps(), funcSymbol, node.get_lineNumber());

                // Special case in-which the add function has been called on a string literal
                if (funcSymbol.get_name().equals("add")) {
                    MctlTypeDescriptor firstParamType = visit(node.get_paramExps().get(0));

                    if (!firstParamType.get_type_literal().equals(varType.get_type_literal().replaceFirst("\\[\\]", ""))) {
                        // Trying to add a non-string type to a string.
                        _problemCollection.addFormattedProblem(
                                ProblemType.ERROR_PARAMETERS_DOES_NOT_MATCH,
                                "The first parameter resolved to type: \"" + firstParamType.get_type_literal() + "\" and does not match the expected type: \"" + varType.get_type_literal().replaceFirst("\\[\\]", "") + "\"",
                                node.get_lineNumber()
                        );
                    }
                }

                // Return the variable type if the function returns ANY type
                if (funcSymbol.get_type().get_type_literal().equals("ANY")) return varType;
                else return funcSymbol.get_type();
            }
        }
        return _symbolTable.searchType("NOTHING");
    }

    public MctlTypeDescriptor visit(StringMethodInvokeNode node) {
        Symbol symbol = _symbolTable.searchSymbol(node.get_id().get_id());

        if (symbol == null) {
            // Function does not exist
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_UNDEFINED_IDENTIFIER,
                    "The function \"" + node.get_id().get_id() + "\" doesnt exist",
                    node.get_lineNumber()
            );
        } else if (!(symbol instanceof FuncSymbol)) {
            // ID refers to variable
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_ID_NOT_FUNCTION,
                    "The ID \"" + node.get_id().get_id() + "\" refers to a variable and can therefore not be invoked",
                    node.get_lineNumber()
            );
        } else {
            // Is valid function symbol
            FuncSymbol funcSymbol = (FuncSymbol) symbol;
            if (!funcSymbol.getIsStringFunction()) {
                // Function should not be called on string
                _problemCollection.addFormattedProblem(
                        ProblemType.ERROR_UTILITY_FUNCTION_INVOKED_IN_WRONG_CONTEXT,
                        "The function \"" + node.get_id().get_id() + "\" cannot be called on type STRING",
                        node.get_lineNumber()
                );
            } else if ((funcSymbol.get_types().size() == 0 && node.get_paramExps().size() != 0) || funcSymbol.get_types().size() != node.get_paramExps().size()) {
                // Number of parameters does not match
                _problemCollection.addFormattedProblem(
                        ProblemType.ERROR_PARAMETERS_DOES_NOT_MATCH,
                        "The provided number of parameters: " + node.get_paramExps().size() + " does not match the expected: " + funcSymbol.get_types().size() + " parameters",
                        node.get_lineNumber()
                );
            } else {
                // Check function parameters
                _checkFunctionParams(node.get_paramExps(), funcSymbol, node.get_lineNumber());

                // Special case in-which the add function has been called on a string literal
                if (funcSymbol.get_name().equals("add")) {
                    MctlTypeDescriptor firstParamType = visit(node.get_paramExps().get(0));
                    if (!firstParamType.get_type_literal().equals("STRING")) {
                        // Trying to add a non-string type to a string.
                        _problemCollection.addFormattedProblem(
                                ProblemType.ERROR_PARAMETERS_DOES_NOT_MATCH,
                                "The first parameter resolved to type: " + firstParamType.get_type_literal() + " and does not match the expected type: \"STRING\"",
                                node.get_lineNumber()
                        );
                    }
                }

                // Return the variable type if the function returns ANY type
                if (funcSymbol.get_type().get_type_literal().equals("ANY")) {
                    return _symbolTable.searchType("STRING");
                }
                else return funcSymbol.get_type();
            }
        }
        return _symbolTable.searchType("NOTHING");
    }

    public MctlTypeDescriptor visit(FormalParamNode node) { return null; }
    public MctlTypeDescriptor visit(StopNode node) { return null; }
    public MctlTypeDescriptor visit(CommentNode node) { return null; }
    public MctlTypeDescriptor visit(VarDecNode node) { return null; }
    public MctlTypeDescriptor visit(FuncDecNode node) { return null; }
    public MctlTypeDescriptor visit(StructDecNode node) { return null; }
    public MctlTypeDescriptor visit(AssStateNode node) { return null; }

    public MctlTypeDescriptor expectsType(BaseNode node, String typeLiteral) {
        String typeChildOne = visit((ExpNode) node.get_children().get(0)).get_type_literal();
        String typeChildTwo = visit((ExpNode) node.get_children().get(1)).get_type_literal();

        if (!typeChildOne.equals(typeLiteral)) {
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_TYPE_MISMATCH,
                    "Expected type \"" + typeLiteral + "\" but got \"" + typeChildOne + "\"",
                    node.get_lineNumber()
            );
        }

        if (!typeChildTwo.equals(typeLiteral)) {
            _problemCollection.addFormattedProblem(
                    ProblemType.ERROR_TYPE_MISMATCH,
                    "Expected type \"" + typeLiteral + "\" but got \"" + typeChildTwo + "\"",
                    node.get_lineNumber()
            );
        }

        if (!(typeChildOne.equals(typeLiteral) && typeChildTwo.equals(typeLiteral))) {
            return _symbolTable.searchType("NOTHING");
        }

        return _symbolTable.searchType(typeLiteral);
    }

    // Function for checking the type of parameters
    public void _checkFunctionParams(List<ExpNode> expressionNodes, FuncSymbol funcSymbol, int lineNumber) {
        int counter = 0;

        // Check if the parameter types match
        for (ExpNode expressionNode : expressionNodes) {
            boolean typeMatched = false;

            MctlTypeDescriptor expressionType = visit(expressionNode);

            // Check if type of parameter is ANY or one of the expected types
            if (!((List<MctlTypeDescriptor>) funcSymbol.get_types().get(counter)).get(0).get_type_literal().equals("ANY")){
                for (MctlTypeDescriptor typeDescriptor : (List<MctlTypeDescriptor>) funcSymbol.get_types().get(counter)) {
                    if(typeDescriptor.get_type_literal().equals(expressionType.get_type_literal())) {
                        // Parameter type matched
                        typeMatched = true;
                        break;
                    }
                }
            }
            else {
                typeMatched = true;
            }

            // Add problem
            if (!typeMatched) {
                StringBuilder typeLiterals = new StringBuilder();
                for (MctlTypeDescriptor typeDescriptor : (List<MctlTypeDescriptor>) funcSymbol.get_types().get(counter)) {
                    typeLiterals.append("\"").append(typeDescriptor.get_type_literal()).append("\", ");
                }

                _problemCollection.addFormattedProblem(
                        ProblemType.ERROR_TYPE_MISMATCH,
                        "Expected one of the following types " + typeLiterals + " for parameter " + counter + " but got \"" + expressionType.get_type_literal() + "\"",
                        lineNumber
                );
            }

            counter++;
        }
    }
}
