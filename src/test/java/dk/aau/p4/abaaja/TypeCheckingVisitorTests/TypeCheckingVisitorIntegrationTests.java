package dk.aau.p4.abaaja.TypeCheckingVisitorTests;

import dk.aau.p4.abaaja.Lib.Nodes.*;
import dk.aau.p4.abaaja.Lib.ProblemHandling.ProblemCollection;
import dk.aau.p4.abaaja.Lib.Symbols.TypeDescriptors.MctlTypeDescriptor;
import dk.aau.p4.abaaja.Lib.Visitors.AstBuilder;
import dk.aau.p4.abaaja.Lib.Visitors.SymbolTableVisitor;
import dk.aau.p4.abaaja.Lib.Visitors.TypeCheckingVisitor;
import dk.aau.p4.abaaja.mctlLexer;
import dk.aau.p4.abaaja.mctlParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;


public class TypeCheckingVisitorIntegrationTests {
    private ProblemCollection problemCollection = new ProblemCollection();
    private final AstBuilder astBuilder = new AstBuilder(problemCollection);
    private SymbolTableVisitor symbolTableVisitor;
    private TypeCheckingVisitor typeCheckingVisitor;
    private SoftAssert softAssert;

    @BeforeMethod
    public void BeforeMethod() {
        softAssert = new SoftAssert();
        symbolTableVisitor = new SymbolTableVisitor(problemCollection);
        typeCheckingVisitor = new TypeCheckingVisitor(problemCollection, symbolTableVisitor.getSymbolTable());
    }

    @AfterMethod
    public void AfterMethod() {
        symbolTableVisitor = null;
        typeCheckingVisitor = null;


        problemCollection = new ProblemCollection();
    }

    private ParseTree createParseTree(String code) {
        mctlLexer lexer = new mctlLexer(CharStreams.fromString(code));
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);

        mctlParser parser = new mctlParser(commonTokenStream);
        parser.setBuildParseTree(true);

        return parser.mctl();
    }

    @DataProvider
    public Object[][] createParseTreeTestData() {
        return new Object[][] {
                {"a = \"a\";", 2},
                {"a = \"a\"; a = \"a\";", 3},
                {"a = \"a\"; a = \"a\"; a = \"a\";", 4}
        };
    }

    @Test(dataProvider = "createParseTreeTestData")
    public void createParseTree_ValidInput_CreatesParseTree(String code, int expectedNodes) {
        ParseTree parseTree = createParseTree(code);

        softAssert.assertEquals(parseTree.getChildCount(), expectedNodes, "Create Parse Tree: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] expectsTypeTestData() {
        return new Object[][] {
                {"variable test: NUMBER; test = 1 * 2;", "NUMBER", 1},
                {"variable test: NUMBER; test = 2 / 1;", "NUMBER", 1},
                {"variable test: NUMBER; test = 10 % 3;", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 + 2;", "NUMBER", 1},
                {"variable test: NUMBER; test = 2 - 1;", "NUMBER", 1},
                {"variable test: BOOLEAN; test = true or false;", "BOOLEAN", 1},
                {"variable test: BOOLEAN; test = false and false;", "BOOLEAN", 1},
                {"variable test: NUMBER; test = 1 > 2;", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 < 2;", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 >= 2;", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 <= 2;", "NUMBER", 1}
        };
    }

    @Test(dataProvider = "expectsTypeTestData")
    public void expectsType_ValidInput_ReturnsCorrectType(String code, String type, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        ExpNode expNode = assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.expectsType(expNode, type);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals(type), "ExpectsType: Expected type " + type + " for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] expectsTypeInvalidTestData() {
        return new Object[][] {
                {"variable test: NUMBER; test = 1 * false;", "NUMBER", 1},
                {"variable test: NUMBER; test = 2 / true;", "NUMBER", 1},
                {"variable test: NUMBER; test = 10 % false;", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 + true;", "NUMBER", 1},
                {"variable test: NUMBER; test = 2 - false;", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 * \"hello\";", "NUMBER", 1},
                {"variable test: NUMBER; test = 2 / \"hello\";", "NUMBER", 1},
                {"variable test: NUMBER; test = 10 % \"hello\";", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 + \"hello\";", "NUMBER", 1},
                {"variable test: NUMBER; test = 2 - \"hello\";", "NUMBER", 1},
                {"variable test: BOOLEAN; test = true or 1;", "BOOLEAN", 1},
                {"variable test: BOOLEAN; test = false and 1;", "BOOLEAN", 1},
                {"variable test: BOOLEAN; test = true or \"hello\";", "BOOLEAN", 1},
                {"variable test: BOOLEAN; test = false and \"hello\";", "BOOLEAN", 1},
                {"variable test: NUMBER; test = 1 > true;", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 < false;", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 >= true;", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 <= false;", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 > \"hello\";", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 < \"hello\";", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 >= \"hello\";", "NUMBER", 1},
                {"variable test: NUMBER; test = 1 <= \"hello\";", "NUMBER", 1},
                {"variable test: NUMBER; test = false * 1 ;", "NUMBER", 1},
                {"variable test: NUMBER; test = true / 2;", "NUMBER", 1},
                {"variable test: NUMBER; test = false % 10;", "NUMBER", 1},
                {"variable test: NUMBER; test = true + 1;", "NUMBER", 1},
                {"variable test: NUMBER; test = false - 2;", "NUMBER", 1},
                {"variable test: NUMBER; test = \"hello\" * 1;", "NUMBER", 1},
                {"variable test: NUMBER; test = \"hello\" / 2;", "NUMBER", 1},
                {"variable test: NUMBER; test = \"hello\" % 10;", "NUMBER", 1},
                {"variable test: NUMBER; test = \"hello\" + 1;", "NUMBER", 1},
                {"variable test: NUMBER; test = \"hello\" - 2;", "NUMBER", 1},
                {"variable test: BOOLEAN; test = 1 or true;", "BOOLEAN", 1},
                {"variable test: BOOLEAN; test = 1 and false;", "BOOLEAN", 1},
                {"variable test: BOOLEAN; test = \"hello\" or true;", "BOOLEAN", 1},
                {"variable test: BOOLEAN; test = \"hello\" and false;", "BOOLEAN", 1},
                {"variable test: NUMBER; test = true > 1;", "NUMBER", 1},
                {"variable test: NUMBER; test = false < 1;", "NUMBER", 1},
                {"variable test: NUMBER; test = true >= 1;", "NUMBER", 1},
                {"variable test: NUMBER; test = false <= 1;", "NUMBER", 1},
                {"variable test: NUMBER; test = \"hello\" > 1;", "NUMBER", 1},
                {"variable test: NUMBER; test = \"hello\" < 1;", "NUMBER", 1},
                {"variable test: NUMBER; test = \"hello\" >= 1;", "NUMBER", 1},
                {"variable test: NUMBER; test = \"hello\" <= 1;", "NUMBER", 1}
        };
    }

    @Test(dataProvider = "expectsTypeInvalidTestData")
    public void expectsType_InvalidInput_ReturnsCorrectType(String code, String type, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        ExpNode expNode = assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.expectsType(expNode, type);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "ExpectsType: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitStructDecTestData() {
        return new Object[][] {
                {"struct STRUCT1 {variable varID: NUMBER,}"},
                {"struct struct2 {variable variable_ID: STRING,}"},
                {"struct coordinate {variable x: NUMBER}"},
                {"struct x_y_z {variable hasCoordinates: BOOLEAN}"}
        };
    }

    @Test(dataProvider = "visitStructDecTestData")
    public void visitStructDec_ValidInput_ReturnsNull(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        StructDecNode structDecNode = (StructDecNode) mctlNode.get_children().get(0);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(structDecNode);

        softAssert.assertTrue(typeDescriptor == null, "Visit StructDec: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitAssStateTestData() {
        return new Object[][] {
                {"variable a: NUMBER; a = 1; a = a + 1;", 2},
                {"variable a: NUMBER; a = 1; a = a + a;", 2},
                {"variable a: NUMBER; a = 1; a = a + 123456789;", 2},
                {"variable a: BOOLEAN; a = true; a = false;", 2},
                {"variable a: STRING; a = \"hello\";", 1}
        };
    }

    @Test(dataProvider = "visitAssStateTestData")
    public void visitAssState_ValidInput_ReturnsNull(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(assStateNode);

        softAssert.assertTrue(typeDescriptor == null, "Visit AssState: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitFunctionDecTestData() {
        return new Object[][] {
                {"to _functionId(): BOOLEAN { return false; }"},
                {"to function1(id1: NUMBER): NUMBER { return 2; }"},
                {"to test_func(id1:NUMBER, id2:NUMBER): STRING { return \"false\"; }"}
        };
    }

    @Test(dataProvider = "visitFunctionDecTestData")
    public void visitFunctionDec_ValidInput_ReturnsNull(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        FuncDecNode funcDecNode = (FuncDecNode) mctlNode.get_children().get(0);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(funcDecNode);

        softAssert.assertTrue(typeDescriptor == null, "Visit FuncDec: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitVarDecTestData() {
        return new Object[][] {
                {"variable identifier1: NUMBER;"},
                {"variable identifier2: STRING[];"},
                {"variable identifier3: BOOLEAN[][];"},
                {"variable identifier4: STRUCTURE[][][];"}
        };
    }

    @Test(dataProvider = "visitVarDecTestData")
    public void visitVarDec_ValidInput_ReturnsNull(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        VarDecNode varDecNode = (VarDecNode) mctlNode.get_children().get(0);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(varDecNode);

        softAssert.assertTrue(typeDescriptor == null, "Visit VarDec: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitCommentTestData() {
        return new Object[][] {
                {"#{ comment }"},
                {"#{comment}"}
        };
    }

    @Test(dataProvider = "visitCommentTestData")
    public void visitComment_ValidInput_ReturnsNull(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        CommentNode commentNode = (CommentNode) mctlNode.get_children().get(0);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(commentNode);

        softAssert.assertTrue(typeDescriptor == null, "Visit Comment: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitStopTestData() {
        return new Object[][] {
                {"stop;"}
        };
    }

    @Test(dataProvider = "visitStopTestData")
    public void visitStop_ValidInput_ReturnsNull(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        StopNode stopNode = (StopNode) mctlNode.get_children().get(0);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(stopNode);

        softAssert.assertTrue(typeDescriptor == null, "Visit Stop: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitFormalParamTestData() {
        return new Object[][] {
                {"to func(id0: NUMBER): NUMBER {}"},
                {"to func(id1: STRING): NUMBER {}"},
                {"to func(id2: BOOLEAN): NUMBER {}"}
        };
    }

    @Test(dataProvider = "visitFormalParamTestData")
    public void visitFormalParam_ValidInput_ReturnsNull(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        FuncDecNode funcDecNode = (FuncDecNode) mctlNode.get_children().get(0);
        FormalParamNode formalParamNode = funcDecNode.get_paramList().get(0);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(formalParamNode);

        softAssert.assertTrue(typeDescriptor == null, "Visit FormalParam: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitStringMethodInvokeTestData() {
        return new Object[][] {
                {"\"var\".add(\"test\");", "STRING"},
                {"\"vroom vroom\".length();", "NUMBER"},
                {"\"1\".indexesOf(\"1\");", "NUMBER[]"}
        };
    }

    @Test(dataProvider = "visitStringMethodInvokeTestData")
    public void visitStringMethodInvoke_ValidInput_ReturnsNull(String code, String expectedType) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        StringMethodInvokeNode stringMethodInvokeNode = (StringMethodInvokeNode) mctlNode.get_children().get(0);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(stringMethodInvokeNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals(expectedType), "Visit StringMethodInvoke: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitVarMethodInvokeTestData() {
        return new Object[][] {
                {"variable var: NUMBER[]; var.add(1);", "NUMBER[]", 1},
                {"variable var: NUMBER[]; var.add(1); var.length();", "NUMBER", 2},
                {"variable var: NUMBER[]; var.add(1); var.indexesOf(\"1\");", "NUMBER[]", 2}
        };
    }

    @Test(dataProvider = "visitVarMethodInvokeTestData")
    public void visitVarMethodInvoke_ValidInput_ReturnsType(String code, String expectedType, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        VarMethodInvokeNode varMethodInvokeNode = (VarMethodInvokeNode) mctlNode.get_children().get(index);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(varMethodInvokeNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals(expectedType), "Visit VarMethodInvoke: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitFuncInvokeTestData() {
        return new Object[][] {
                {"to var(a: NUMBER, b: NUMBER): NOTHING {}; variable bing: NUMBER; bing = 2; variable bong: NUMBER; bong = 3; var(bing, bong);", 5},
                {"to vroom(): NOTHING {}; vroom();", 1},
                {"to _1(num:NUMBER): NOTHING {} _1(2);", 1}
        };

    }

    @Test(dataProvider = "visitFuncInvokeTestData")
    public void visitFuncInvoke_ValidInput_ReturnsNull(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        FuncInvokeNode funcInvokeNode = (FuncInvokeNode) mctlNode.get_children().get(index);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(funcInvokeNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit FuncInvoke: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitBlockTestData() {
        return new Object[][] {
                {"if(true){}"},
                {"if(true){id=val;id=val;}"},
                {"if(true){id=val;id=val;id=val;}"},
                {"if(true){id=val;id=val;id=val;id=val;}"}
        };
    }

    @Test(dataProvider = "visitBlockTestData")
    public void visitBlock_ValidInput_ReturnsNull(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        IfStateNode ifStateNode = (IfStateNode) mctlNode.get_children().get(0);
        BlockNode blockNode = ifStateNode.get_blockChildrenNode().get(0);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(blockNode);

        softAssert.assertTrue(typeDescriptor == null, "Visit Block: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitLineTestData() {
        return new Object[][] {
                {"if(true){id=val;id=val;}"},
                {"if(true){id=val;id=val;id=val;}"},
                {"if(true){id=val;id=val;id=val;id=val;}"}
        };
    }

    @Test(dataProvider = "visitLineTestData")
    public void visitLine_ValidInput_ReturnsNull(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        IfStateNode ifStateNode = (IfStateNode) mctlNode.get_children().get(0);
        BlockNode blockNode = ifStateNode.get_blockChildrenNode().get(0);
        LineNode lineNode = (LineNode) blockNode.get_children().get(0);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(lineNode);

        softAssert.assertTrue(typeDescriptor == null, "Visit Line: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitMctlTestData() {
        return new Object[][] {
                {"variable test: NUMBER; test = 0.0;"},
                {"variable var1: NUMBER; var1 = 0; variable var2: NUMBER; var2 = 0; variable test: NUMBER; test = var1 + var2;"},
                {"struct x_y_z {variable hasCoordinates : BOOLEAN}", },
                {"if (true) { var = a; } else if (true) { var = b; } else { var = c; }"},
                {"variable a: STRING; variable b: STRING; variable var: NUMBER;"},
                {"variable var1: NUMBER; var1 = 1; variable var2: NUMBER; var2 = 2; variable var3: NUMBER; var3 = 3; variable var4: NUMBER; var4 = 4; variable var5: NUMBER; var5 = 5;"}
        };
    }

    @Test(dataProvider = "visitMctlTestData")
    public void visitMctl_ValidInput_ReturnsNull(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);
        symbolTableVisitor.visit(mctlNode);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(mctlNode);

        softAssert.assertTrue(typeDescriptor == null, "Visit Mctl: " + code);
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitReturnTestData() {
        return new Object[][] {
                {"return 4;", "NUMBER", 0},
                {"return \"yo\";", "STRING", 0},
                {"return true;", "BOOLEAN", 0},
                {"variable var: NUMBER; var = 1; return var;", "NUMBER", 2},
                {"variable var: STRING; var = \"1\"; return var;", "STRING", 2},
                {"variable var: BOOLEAN; var = true; return var;", "BOOLEAN", 2},
                {"variable var: NUMBER[]; var[0] = 1; return var[0];", "NUMBER", 2},
                {"variable var: STRING[]; var[0] = \"1\"; return var[0];", "STRING", 2},
                {"variable var: BOOLEAN[]; var[0] = true; return var[0];", "BOOLEAN", 2},
                {"variable var: NUMBER[][]; var[0][0] = 1; return var[0][0];", "NUMBER", 2},
                {"variable var: STRING[][]; var[0][0] = \"1\"; return var[0][0];", "STRING", 2},
                {"variable var: BOOLEAN[][]; var[0][0] = true; return var[0][0];", "BOOLEAN", 2},
                {"variable var: NUMBER[][][]; var[0][0][0] = 1; return var[0];", "NUMBER[][]", 2},
                {"variable var: STRING[][][]; var[0][0][0] = \"1\"; return var[0];", "STRING[][]", 2},
                {"variable var: BOOLEAN[][][]; var[0][0][0] = true; return var[0];", "BOOLEAN[][]", 2}
        };
    }

    @Test(dataProvider = "visitReturnTestData")
    public void visitReturn_ValidInput_ReturnsCorrectType(String code, String type, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        ReturnNode returnNode = (ReturnNode) mctlNode.get_children().get(index);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(returnNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals(type), "Visit Return: Expected type " + type + " for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitReturnInvalidTestData() {
        return new Object[][] {
                {"variable hello: NUMBER; return hello;", 1},
                {"variable var: NUMBER; var = \"hello\"; return var;", 2},
                {"variable var: NUMBER; var = 1; return var[0];", 2},
                {"variable var: NUMBER[]; var = 1; return var[0];", 2},
                {"variable var: NUMBER[]; var[0][0] = 1; return var[0];", 2},
                {"variable var: NUMBER[]; var[0] = 1; return var[0][0];", 2},
                {"variable var: STRING[]; var = \"hello\"; return var[0];", 2},
                {"variable var: STRING[]; var[0][0] = \"hello\"; return var[0];", 2},
                {"variable var: STRING[]; var[0] = \"hello\"; return var[0][0];", 2},
                {"variable var: BOOLEAN[]; var = true; return var[0];", 2},
                {"variable var: BOOLEAN[]; var[0][0] = true; return var[0];", 2},
                {"variable var: BOOLEAN[]; var[0] = true; return var[0][0];", 2},
                {"variable var: NUMBER[][]; var[0][0] = 1; return var[0][0][0];", 2},
                {"variable var: STRING[][]; var[0][0] = \"1\"; return var[0][0][0];", 2},
                {"variable var: BOOLEAN[][]; var[0][0] = true; return var[0][0][0];", 2},
                {"variable var: NUMBER[][]; var[0][0][0] = 1; return var[0][0][0];", 2},
                {"variable var: STRING[][]; var[0][0][0] = \"1\"; return var[0][0][0];", 2},
                {"variable var: BOOLEAN[][]; var[0][0][0] = true; return var[0][0][0];", 2}
        };
    }

    @Test(dataProvider = "visitReturnInvalidTestData")
    public void visitReturn_InvalidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        ReturnNode returnNode = (ReturnNode) mctlNode.get_children().get(index);

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(returnNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit Return: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitTypecastTestData() {
        return new Object[][] {
                {"variable var: STRING; variable test: STRING; var = \"hi\"; test = (STRING) var;", "STRING", 3}, // STRING TO STRING (REDUNDANT)
                {"variable var: STRING; variable test: NUMBER; var = \"1\"; test = (NUMBER) var;", "NUMBER", 3}, // STRING TO NUMBER
                {"variable var: NUMBER; variable test: NUMBER; var = 1; test = (NUMBER) var;", "NUMBER", 3}, // NUMBER TO NUMBER (REDUNDANT)
                {"variable var: NUMBER; variable test: STRING; var = 1; test = (STRING) var;", "STRING", 3}, // NUMBER TO STRING
                {"variable var: BOOLEAN; variable test: BOOLEAN; var = true; test = (BOOLEAN) var;", "BOOLEAN", 3}, // BOOLEAN TO BOOLEAN (REDUNDANT)
                {"variable var: BOOLEAN; variable test: STRING; var = true; test = (STRING) var;", "STRING", 3} // BOOLEAN TO STRING
        };
    }

    @Test(dataProvider = "visitTypecastTestData")
    public void visitTypecast_ValidInput_ReturnsCorrectType(String code, String type, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        TypecastExpNode typecastExpNode = (TypecastExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(typecastExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals(type), "Visit Typecast: Expected type " + type + " for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitTypecastInvalidTestData() {
        return new Object[][] {
                {"variable var: NUMBER; variable test: BOOLEAN; var = 1; test = (BOOLEAN) var;", 3}, // NUMBER TO BOOLEAN
                {"struct STRUCTURE { variable x: NUMBER }; variable var: NUMBER; variable test: STRUCTURE; var = 1; test = (STRUCTURE) var;", 4}, // NUMBER TO STRUCT
                {"variable var: STRING; variable test: BOOLEAN; var = \"true\"; test = (BOOLEAN) var;", 3}, // STRING TO BOOLEAN
                {"struct STRUCTURE { variable x: NUMBER }; variable var: STRING; variable test: STRUCTURE; var = \"hi\"; test = (STRUCTURE) var;", 4}, // STRING TO STRUCT
                {"variable var: BOOLEAN; variable test: NUMBER; var = true; test = (NUMBER) var;", 3}, // BOOLEAN TO NUMBER
                {"struct STRUCTURE { variable x: NUMBER }; variable var: STRING; variable test: STRUCTURE; var = \"hi\"; test = (STRUCTURE) var;", 4}, // BOOLEAN TO STRUCT
                {"struct STRUCTURE { variable x: NUMBER }; variable var: STRUCTURE; variable test: NUMBER; var.x = 1; test = (NUMBER) var;", 4}, // STRUCT TO NUMBER
                {"struct STRUCTURE { variable x: NUMBER }; variable var: STRUCTURE; variable test: STRING; var.x = 1; test = (STRING) var;", 4}, // STRUCT TO STRING
                {"struct STRUCTURE { variable x: NUMBER }; variable var: STRUCTURE; variable test: BOOLEAN; var.x = 1; test = (BOOLEAN) var;", 4}, // STRUCT TO BOOLEAN
                {"struct STRUCTURE { variable x: NUMBER }; variable var: STRUCTURE; variable test: STRUCTURE; var.x = 1; test = (STRUCTURE) var;", 4}, // STRUCT TO STRUCT
                {"variable test: NUMBER[]; variable var: NUMBER[]; var = (NUMBER[]) test;", 2}, // NUMBER[] TO NUMBER[]
                {"variable test: NUMBER[]; variable var: STRING; var = (STRING) test;", 2}, // NUMBER[] TO STRING
                {"variable test: NUMBER[]; variable var: NUMBER; var = (NUMBER) test;", 2}, // NUMBER[] TO NUMBER
                {"variable test: NUMBER[]; variable var: BOOLEAN; var = (BOOLEAN) test;", 2}, // NUMBER[] TO BOOLEAN
                {"variable test: NUMBER[]; variable var: STRING; var = (STRING) test;", 2}, // NUMBER[] TO STRING

        };
    }

    @Test(dataProvider = "visitTypecastInvalidTestData")
    public void visitTypecast_InvalidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        TypecastExpNode typecastExpNode = (TypecastExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(typecastExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit Typecast: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitUnaryExpTestData() {
        return new Object[][] {
                {"variable test: NUMBER; variable var: NUMBER; var = 2; test = +var;", "NUMBER", 3},
                {"variable test: NUMBER; variable var: NUMBER; var = -2; test = +var;", "NUMBER", 3},
                {"variable test: NUMBER; test = -8;", "NUMBER", 1},
                {"variable test: BOOLEAN; test = false; test = !test;", "BOOLEAN", 2}
        };
    }

    @Test(dataProvider = "visitUnaryExpTestData")
    public void visitUnaryExp_ValidInput_ReturnsCorrectType(String code, String type, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        UnaryExpNode unaryExpNode = (UnaryExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(unaryExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals(type), "Visit UnaryExp: Expected type " + type + " for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitUnaryExpInvalidTestData() {
        return new Object[][] {
                {"variable test: NUMBER; test = +true;", 1},
                {"variable test: NUMBER; test = +\"hi\";", 1},
                {"variable test: NUMBER; test = -true;", 1},
                {"variable test: NUMBER; test = -\"hi\";", 1},
                {"variable test: BOOLEAN; test = !1;", 1},
                {"variable test: BOOLEAN; test = !\"hi\";", 1},
                {"variable test: BOOLEAN; struct STRUCTURE { variable x: NUMBER }; variable var: STRUCTURE; test = +var;", 3},
                {"variable test: BOOLEAN; struct STRUCTURE { variable x: NUMBER }; variable var: STRUCTURE; test = -var;", 3},
                {"variable test: BOOLEAN; struct STRUCTURE { variable x: NUMBER }; variable var: STRUCTURE; test = !var;", 3}
        };
    }

    @Test(dataProvider = "visitUnaryExpInvalidTestData")
    public void visitUnaryExp_InvalidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        UnaryExpNode unaryExpNode = (UnaryExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(unaryExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit UnaryExp: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitStringExpTestData() {
        return new Object[][] {
                {"variable test: STRING; test = \"This is a String\";", 1},
                {"variable test: STRING; test = \"1234 string 8078.,/()&\";", 1},
                {"variable test: STRING; test = \"'Double Quoted String'\";", 1},
                {"variable test: STRING; test = 'Single Quoted String';", 1},
                {"variable test: STRING; test = \"Newline: \n Carriage return: \r Tab: \t Backslash: \\ \";", 1},
                {"variable test: STRING; test = \"Unicode: Ωαφω\";", 1}
        };
    }

    @Test(dataProvider = "visitStringExpTestData")
    public void visitStringExp_ValidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        StringExpNode stringExpNode = (StringExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(stringExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("STRING"), "Visit StringExp: Expected type STRING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitNumExpTestData() {
        return new Object[][]{
                {"variable test: NUMBER; test = 0.0;", 1},
                {"variable test: NUMBER; test = 1.0;", 1},
                {"variable test: NUMBER; test = 1.0;", 1},
                {"variable test: NUMBER; test = 1234.56789;", 1},
                {"variable test: NUMBER; test = 9876.54321;", 1},
                {"variable test: NUMBER; test = 0;", 1},
                {"variable test: NUMBER; test = 1;", 1},
                {"variable test: NUMBER; test = 12345;", 1},
                {"variable test: NUMBER; test = 67890;", 1},
                {"variable test: NUMBER; test = 2147483647;", 1}
        };
    }

    @Test(dataProvider = "visitNumExpTestData")
    public void visitNumExp_ValidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        NumExpNode numExpNode = (NumExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(numExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NUMBER"), "Visit NumExp: Expected type NUMBER for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitNothingTypeTestData() {
        return new Object[][] {
                {"to test(): NOTHING {}"},
                {"to test(): NOTHING { variable x: NUMBER; x = 5; }"},
                {"to test(): NOTHING { test(); }"}
        };
    }

    @Test(dataProvider = "visitNothingTypeTestData")
    public void visitNothingType_ValidInput_ReturnsCorrectType(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        FuncDecNode funcDecNode = (FuncDecNode) mctlNode.get_children().get(0);
        NothingTypeNode nothingTypeNode = (NothingTypeNode) funcDecNode.get_returnType();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(nothingTypeNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit Nothing: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitTypeTestData() {
        return new Object[][] {
                {"variable test: STRING;", "STRING", 0},
                {"variable test: STRING[];", "STRING[]", 0},
                {"variable test: STRING[][];", "STRING[][]", 0},
                {"variable test: NUMBER;", "NUMBER", 0},
                {"variable test: NUMBER[];", "NUMBER[]", 0},
                {"variable test: NUMBER[][];", "NUMBER[][]", 0},
                {"variable test: BOOLEAN;", "BOOLEAN", 0},
                {"variable test: BOOLEAN[];", "BOOLEAN[]", 0},
                {"variable test: BOOLEAN[][];", "BOOLEAN[][]", 0},
                {"struct STRUCTURE { variable x: NUMBER }; variable test: STRUCTURE;", "STRUCTURE", 1},
                {"struct testStructId { variable x: NUMBER }; variable test: testStructId[];", "testStructId[]", 1},
                {"struct struct_test_id { variable x: NUMBER }; variable test: struct_test_id[][];", "struct_test_id[][]", 1}
        };
    }

    @Test(dataProvider = "visitTypeTestData")
    public void visitType_ValidInput_ReturnsCorrectType(String code, String type, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        VarDecNode varDecNode = (VarDecNode) mctlNode.get_children().get(index);
        TypeNode typeNode = varDecNode.get_varDecType();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(typeNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals(type), "Visit Type: Expected type " + type + " for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitCompExpTestData() {
        return new Object[][]{
                {"variable test: BOOLEAN; test = -1 > -2;", 1},
                {"variable test: BOOLEAN; test = 1 > 2;", 1},
                {"variable var1: NUMBER; variable var2: NUMBER; variable test: BOOLEAN; var1 = 1; var2 = 2; test = var1 > var2;", 5},
                {"variable test: BOOLEAN; test = 1 < 2;", 1},
                {"variable var1: NUMBER; variable var2: NUMBER; variable test: BOOLEAN; var1 = 1; var2 = 2; test = var1 < var2;", 5},
                {"variable test: BOOLEAN; test = 1 >= 2;", 1},
                {"variable var1: NUMBER; variable var2: NUMBER; variable test: BOOLEAN; var1 = 1; var2 = 2; test = var1 >= var2;", 5},
                {"variable test: BOOLEAN; test = 1 <= 2;", 1},
                {"variable var1: NUMBER; variable var2: NUMBER; variable test: BOOLEAN; var1 = 1; var2 = 2; test = var1 <= var2;", 5}
        };
    }

    @Test(dataProvider = "visitCompExpTestData")
    public void visitCompExp_ValidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        CompExpNode compExpNode = (CompExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(compExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("BOOLEAN"), "Visit CompExp: Expected type BOOLEAN for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitCompExpInvalidTestData() {
        return new Object[][]{
                {"variable test: BOOLEAN; test = true > true;", 1},
                {"variable test: BOOLEAN; test = true > \"hi\";", 1},
                {"variable test: BOOLEAN; test = true < true;", 1},
                {"variable test: BOOLEAN; test = true < \"hi\";", 1},
                {"variable test: BOOLEAN; test = true >= true;", 1},
                {"variable test: BOOLEAN; test = true >= \"hi\";", 1},
                {"variable test: BOOLEAN; test = true <= true;", 1},
                {"variable test: BOOLEAN; test = true <= \"hi\";", 1},
                {"variable test: BOOLEAN; test = true > 1;", 1},
                {"variable test: BOOLEAN; test = true < 1;", 1},
                {"variable test: BOOLEAN; test = true >= 1;", 1},
                {"variable test: BOOLEAN; test = true <= 1;", 1},
                {"variable test: BOOLEAN; test = \"hi\" > \"hi\";", 1},
                {"variable test: BOOLEAN; test = \"hi\" > true;", 1},
                {"variable test: BOOLEAN; test = \"hi\" < \"hi\";", 1},
                {"variable test: BOOLEAN; test = \"hi\" < true;", 1},
                {"variable test: BOOLEAN; test = \"hi\" >= \"hi\";", 1},
                {"variable test: BOOLEAN; test = \"hi\" >= true;", 1},
                {"variable test: BOOLEAN; test = \"hi\" <= \"hi\";", 1},
                {"variable test: BOOLEAN; test = \"hi\" <= true;", 1},
                {"variable test: BOOLEAN; test = \"hi\" > 1;", 1},
                {"variable test: BOOLEAN; test = \"hi\" < 1;", 1},
                {"variable test: BOOLEAN; test = \"hi\" >= 1;", 1},
                {"variable test: BOOLEAN; test = \"hi\" <= 1;", 1},
                {"variable test: BOOLEAN; test = 1 > true;", 1},
                {"variable test: BOOLEAN; test = 1 > \"hi\";", 1},
                {"variable test: BOOLEAN; test = 1 < true;", 1},
                {"variable test: BOOLEAN; test = 1 < \"hi\";", 1},
                {"variable test: BOOLEAN; test = 1 >= true;", 1},
                {"variable test: BOOLEAN; test = 1 >= \"hi\";", 1},
                {"variable test: BOOLEAN; test = 1 <= true;", 1},
                {"variable test: BOOLEAN; test = 1 <= \"hi\";", 1},
                {"variable test: BOOLEAN; struct STRUCTURE { variable x: NUMBER }; variable var1: STRUCTURE; variable var2: STRUCTURE; var1 = 1; var2 = 1; test = var1 > var2;", 6},
                {"variable test: BOOLEAN; struct STRUCTURE { variable x: NUMBER }; variable var1: STRUCTURE; variable var2: STRUCTURE; var1 = 1; var2 = 1; test = var1 < var2;", 6},
                {"variable test: BOOLEAN; struct STRUCTURE { variable x: NUMBER }; variable var1: STRUCTURE; variable var2: STRUCTURE; var1 = 1; var2 = 1; test = var1 >= var2;", 6},
                {"variable test: BOOLEAN; struct STRUCTURE { variable x: NUMBER }; variable var1: STRUCTURE; variable var2: STRUCTURE; var1 = 1; var2 = 1; test = var1 <= var2;", 6}
        };
    }

    @Test(dataProvider = "visitCompExpInvalidTestData")
    public void visitCompExp_InvalidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        CompExpNode compExpNode = (CompExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(compExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit CompExp: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitOrExpTestData() {
        return new Object[][] {
                {"variable var1: BOOLEAN;" +
                        "variable var2: BOOLEAN;" +
                        "variable test: BOOLEAN;" +
                        "var1 = true;" +
                        "var2 = true;" +
                        "test = var1 or var2;"}
        };
    }

    @Test(dataProvider = "visitOrExpTestData")
    public void visitOrExp_ValidInput_ReturnsCorrectType(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(5);
        OrExpNode orExpNode = (OrExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(orExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("BOOLEAN"), "Visit OrExp: Expected type BOOLEAN for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitOrExpInvalidTestData() {
        return new Object[][] {
                {"variable test: BOOLEAN; test = true or \"hi\";"},
                {"variable test: BOOLEAN; test = true or 1;"},
                {"variable test: BOOLEAN; test = 1 or 1;"},
                {"variable test: BOOLEAN; test = 1 or \"hi\";"},
                {"variable test: BOOLEAN; test = 1 or true;"},
                {"variable test: BOOLEAN; test = \"hi\" or \"hi\";"},
                {"variable test: BOOLEAN; test = \"hi\" or true;"},
                {"variable test: BOOLEAN; test = \"hi\" or 1;"}
        };
    }

    @Test(dataProvider = "visitOrExpInvalidTestData")
    public void visitOrExp_InvalidInput_ReturnsCorrectType(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(1);
        OrExpNode orExpNode = (OrExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(orExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit OrExp: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitAndExpTestData() {
        return new Object[][] {
                {"variable var1: BOOLEAN; var1 = true; " +
                        "variable var2: BOOLEAN; var2 = true;" +
                        "variable var3: BOOLEAN; var3 = true;" +
                        "variable var4: BOOLEAN; var4 = true;" +
                        "variable var5: BOOLEAN; var5 = true;" +
                        "variable test: BOOLEAN; test = var1 and var2;"},
                {"variable var1: BOOLEAN; var1 = true; " +
                        "variable var2: BOOLEAN; var2 = true;" +
                        "variable var3: BOOLEAN; var3 = true;" +
                        "variable var4: BOOLEAN; var4 = true;" +
                        "variable var5: BOOLEAN; var5 = true;" +
                        "variable test: BOOLEAN; test = var1 and var2 and var3 and var4 and var5;"}
        };
    }

    @Test(dataProvider = "visitAndExpTestData")
    public void visitAndExp_ValidInput_ReturnsCorrectType(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(11);
        AndExpNode andExpNode = (AndExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(andExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("BOOLEAN"), "Visit AndExp: Expected type BOOLEAN for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitAndExpInvalidTestData() {
        return new Object[][] {
                {"variable test: BOOLEAN; test = true and \"hi\";"},
                {"variable test: BOOLEAN; test = true and 1;"},
                {"variable test: BOOLEAN; test = 1 and 1;"},
                {"variable test: BOOLEAN; test = 1 and \"hi\";"},
                {"variable test: BOOLEAN; test = 1 and true;"},
                {"variable test: BOOLEAN; test = \"hi\" and \"hi\";"},
                {"variable test: BOOLEAN; test = \"hi\" and true;"},
                {"variable test: BOOLEAN; test = \"hi\" and 1;"}
        };
    }

    @Test(dataProvider = "visitAndExpInvalidTestData")
    public void visitAndExp_InvalidInput_ReturnsCorrectType(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(1);
        AndExpNode andExpNode = (AndExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(andExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit AndExp: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitAddExpTestData() {
        return new Object[][] {
                {"variable var1: NUMBER; variable var2: NUMBER; variable test: NUMBER; var1 = 1; var2 = 2; test = var1 + var2;"},
                {"variable var1: NUMBER; variable var2: NUMBER; variable test: NUMBER; var1 = 1; var2 = 2; test = var1 - var2;"}
        };
    }

    @Test(dataProvider = "visitAddExpTestData")
    public void visitAddExp_ValidInput_ReturnsCorrectType(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(5);
        AddExpNode addExpNode = (AddExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(addExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NUMBER"), "Visit AddExp: Expected type NUMBER for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitAddExpInvalidTestData() {
        return new Object[][] {
                {"variable test: NUMBER; test = true + false;", 1},
                {"variable test: NUMBER; test = true + \"hi\";", 1},
                {"variable test: NUMBER; test = true + 1;", 1},
                {"variable test: NUMBER; test = 1 + \"hi\";", 1},
                {"variable test: NUMBER; test = 1 + true;", 1},
                {"variable test: NUMBER; test = \"hi\" + \"hi\";", 1},
                {"variable test: NUMBER; test = \"hi\" + true;", 1},
                {"variable test: NUMBER; test = \"hi\" + 1;", 1},
                {"variable test: NUMBER; test = true - false;", 1},
                {"variable test: NUMBER; test = true - \"hi\";", 1},
                {"variable test: NUMBER; test = true - 1;", 1},
                {"variable test: NUMBER; test = 1 - \"hi\";", 1},
                {"variable test: NUMBER; test = 1 - true;", 1},
                {"variable test: NUMBER; test = \"hi\" - \"hi\";", 1},
                {"variable test: NUMBER; test = \"hi\" - true;", 1},
                {"variable test: NUMBER; test = \"hi\" - 1;", 1},
                {"variable test: NUMBER; struct STRUCTURE { variable x: NUMBER }; variable var1: STRUCTURE; variable var2: STRUCTURE; var1 = 1; var2 = 1; test = var1 + var2;", 6},
                {"variable test: NUMBER; struct STRUCTURE { variable x: NUMBER }; variable var1: STRUCTURE; variable var2: STRUCTURE; var1 = 1; var2 = 1; test = var1 - var2;", 6}
        };
    }

    @Test(dataProvider = "visitAddExpInvalidTestData")
    public void visitAddExp_InvalidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        AddExpNode addExpNode = (AddExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(addExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit AddExp: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitMulExpTestData() {
        return new Object[][] {
                {"variable var1: NUMBER; variable var2: NUMBER; variable test: NUMBER; var1 = 1; var2 = 2; test = var1 * var2;"},
                {"variable var1: NUMBER; variable var2: NUMBER; variable test: NUMBER; var1 = 1; var2 = 2; test = var1 / var2;"},
                {"variable var1: NUMBER; variable var2: NUMBER; variable test: NUMBER; var1 = 1; var2 = 2; test = var1 % var2;"}
        };
    }

    @Test(dataProvider = "visitMulExpTestData")
    public void visitMulExp_ValidInput_ReturnsCorrectType(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(5);
        MulExpNode mulExpNode = (MulExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(mulExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NUMBER"), "Visit MulExp: Expected type NUMBER for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitMulExpInvalidTestData() {
        return new Object[][] {
                {"variable test: NUMBER; test = true * false;", 1},
                {"variable test: NUMBER; test = true * \"hi\";", 1},
                {"variable test: NUMBER; test = true * 1;", 1},
                {"variable test: NUMBER; test = 1 * \"hi\";", 1},
                {"variable test: NUMBER; test = 1 * true;", 1},
                {"variable test: NUMBER; test = \"hi\" * \"hi\";", 1},
                {"variable test: NUMBER; test = \"hi\" * true;", 1},
                {"variable test: NUMBER; test = \"hi\" * 1;", 1},
                {"variable test: NUMBER; test = true / false;", 1},
                {"variable test: NUMBER; test = true / \"hi\";", 1},
                {"variable test: NUMBER; test = true / 1;", 1},
                {"variable test: NUMBER; test = 1 / \"hi\";", 1},
                {"variable test: NUMBER; test = 1 / true;", 1},
                {"variable test: NUMBER; test = \"hi\" / \"hi\";", 1},
                {"variable test: NUMBER; test = \"hi\" / true;", 1},
                {"variable test: NUMBER; test = \"hi\" / 1;", 1},
                {"variable test: NUMBER; test = true % false;", 1},
                {"variable test: NUMBER; test = true % \"hi\";", 1},
                {"variable test: NUMBER; test = true % 1;", 1},
                {"variable test: NUMBER; test = 1 % \"hi\";", 1},
                {"variable test: NUMBER; test = 1 % true;", 1},
                {"variable test: NUMBER; test = \"hi\" % \"hi\";", 1},
                {"variable test: NUMBER; test = \"hi\" % true;", 1},
                {"variable test: NUMBER; test = \"hi\" % 1;", 1},
                {"variable test: NUMBER; struct STRUCTURE { variable x: NUMBER }; variable var1: STRUCTURE; variable var2: STRUCTURE; var1 = 1; var2 = 1; test = var1 * var2;", 6},
                {"variable test: NUMBER; struct STRUCTURE { variable x: NUMBER }; variable var1: STRUCTURE; variable var2: STRUCTURE; var1 = 1; var2 = 1; test = var1 / var2;", 6},
                {"variable test: NUMBER; struct STRUCTURE { variable x: NUMBER }; variable var1: STRUCTURE; variable var2: STRUCTURE; var1 = 1; var2 = 1; test = var1 % var2;", 6}
        };
    }

    @Test(dataProvider = "visitMulExpInvalidTestData")
    public void visitMulExp_InvalidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        MulExpNode mulExpNode = (MulExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(mulExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit MulExp: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitIDStructTestData() {
        return new Object[][] {
                {"struct STRUCTURE { variable num: NUMBER }; variable var: STRUCTURE; var.num = 1;", "NUMBER", 2},
                {"struct STRUCTURE { variable num: NUMBER }; variable var: STRUCTURE[]; var[0].num = 3;", "NUMBER", 2},
                {"struct STRUCTURE { variable str: STRING }; variable var: STRUCTURE; var.str = \"1\";", "STRING", 2},
                {"struct STRUCTURE { variable str: STRING }; variable var: STRUCTURE[]; var[0].str = \"3\";", "STRING", 2},
                {"struct STRUCTURE { variable bool: BOOLEAN }; variable var: STRUCTURE; var.bool = true;", "BOOLEAN", 2},
                {"struct STRUCTURE { variable bool: BOOLEAN }; variable var: STRUCTURE[]; var[0].bool = true;", "BOOLEAN", 2},
                {"struct STRUCTURE { variable bool: BOOLEAN }; variable var: STRUCTURE[][]; var[0][0].bool = true;", "BOOLEAN", 2},
                {"struct STRUCTURE1 { variable bool: BOOLEAN }; struct STRUCTURE2 { variable inner: STRUCTURE1 }; variable var: STRUCTURE2; var.inner.bool = true;", "BOOLEAN", 3},
                {"struct STRUCTURE1 { variable bool: BOOLEAN }; struct STRUCTURE2 { variable inner: STRUCTURE1[][] }; variable var: STRUCTURE2[][][][][]; var[0][0][0][0][0].inner[0][0].bool = true;", "BOOLEAN", 3},
                {"struct STRUCTURE1 { variable bool: BOOLEAN }; struct STRUCTURE2 { variable inner: STRUCTURE1[] }; variable var: STRUCTURE2[][]; var[0][0].inner[0].bool = true;", "BOOLEAN", 3},
                {"struct STRUCTURE1 { variable bool: STRUCTURE2 }; struct STRUCTURE2 { variable inner: STRUCTURE1, variable final: NUMBER }; variable var: STRUCTURE2; var.inner.bool.final = 1;", "NUMBER", 3},
                {"struct STRUCTURE1 { variable bool: STRUCTURE2 }; struct STRUCTURE2 { variable inner: STRUCTURE1, variable final: NUMBER }; variable var: STRUCTURE2; var.inner.bool.inner.bool.inner.bool.final = 1;", "NUMBER", 3}
        };
    }

    @Test(dataProvider = "visitIDStructTestData")
    public void visitIDStruct_ValidInput_ReturnsCorrectType(String code, String type, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        IDStructNode idStructNode = (IDStructNode) assStateNode.get_assignId();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(idStructNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals(type), "Visit IDStruct: Expected type " + type + " for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitIDStructInvalidTestData() {
        return new Object[][] {
                {"struct STRUCTURE { variable num: NUMBER[] }; variable var: STRUCTURE; var.num = 1;", 2},
                {"struct STRUCTURE { variable num: NUMBER[] }; variable var: STRUCTURE[]; var[0].num = 2;", 2},
                {"struct STRUCTURE { variable num: NUMBER }; variable var: STRUCTURE[]; var[0][0].num = 1;", 2},
                {"struct STRUCTURE { variable num: NUMBER }; variable var: STRUCTURE; var.num = true;", 2},
                {"struct STRUCTURE { variable num: NUMBER }; variable var: STRUCTURE; var.num = \"hi\";", 2},
                {"struct STRUCTURE { variable num: NUMBER }; variable var: STRUCTURE; var.inner = 1;", 2},
                {"struct STRUCTURE { variable num: NUMBER }; variable var: STRUCTURE; var.num.inner = 1;", 2},
                {"struct STRUCTURE { variable num: NUMBER }; variable var: STRUCTURE; var.num.inner = true;", 2},
                {"struct STRUCTURE { variable num: NUMBER }; variable var: STRUCTURE; var.bool.inner = true;", 2},
                {"struct STRUCTURE1 { variable bool: STRUCTURE2 }; struct STRUCTURE2 { variable inner: STRUCTURE1, variable final: NUMBER[] }; variable var: STRUCTURE2; var.inner.bool.final = true;", 3}
        };
    }

    @Test(dataProvider = "visitIDStructInvalidTestData")
    public void visitIDStruct_InvalidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        IDStructNode idStructNode = (IDStructNode) assStateNode.get_assignId();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(idStructNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit IDStruct: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitActualIDExpTestData() {
        return new Object[][] {
                {"variable var: NUMBER; variable test: NUMBER; var = 1; test = var;", "NUMBER", 3},
                {"variable var: STRING; variable test: STRING; var = \"1\"; test = var;", "STRING", 3},
                {"variable var: BOOLEAN; variable test: BOOLEAN; var = true; test = var;", "BOOLEAN", 3},
                {"variable var: NUMBER; variable test: NUMBER; test = var;", "NOTHING", 2}
        };
    }

    @Test(dataProvider = "visitActualIDExpTestData")
    public void visitActualIDExp_ValidInput_ReturnsCorrectType(String code, String type, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        ActualIDExpNode actualIDExpNode = (ActualIDExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(actualIDExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals(type), "Visit ActualIDExp: Expected type " + type + " for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitIDArrayExpTestData() {
        return new Object[][] {
                {"variable a: NUMBER[]; a[0] = 1;", "NUMBER", 1},
                {"variable a: NUMBER[][]; a[0][0] = 2;", "NUMBER", 1},
                {"variable a: NUMBER[][][]; a[0][0][0] = 5;", "NUMBER", 1},
                {"variable a: STRING[]; a[0] = \"1\";", "STRING", 1},
                {"variable a: STRING[][]; a[0][0] = \"2\";", "STRING", 1},
                {"variable a: STRING[][][]; a[0][0][0] = \"5\";", "STRING", 1},
                {"variable a: BOOLEAN[]; a[0] = true;", "BOOLEAN", 1},
                {"variable a: BOOLEAN[][]; a[0][0] = true;", "BOOLEAN", 1},
                {"variable a: BOOLEAN[][][]; a[0][0][0] = false;", "BOOLEAN", 1},
                {"struct test { variable b: NUMBER[] }; variable a: test; a.b[0] = 1;", "NUMBER", 2},
                {"struct test { variable b: NUMBER[] }; variable a: test[][]; a[0][0].b[0] = 1;", "NUMBER", 2},
                {"struct test { variable b: NUMBER[][] }; variable a: test[]; a[0].b[0][0] = 1;", "NUMBER", 2},
                {"struct test { variable b: NUMBER[][] }; variable a: test[][]; a[0][0].b[0][0] = 1;", "NUMBER", 2},
                {"struct test { variable b: NUMBER[][][] }; variable a: test[][][]; a[0][0][0].b[0][0][0] = 1;", "NUMBER", 2},
                {"struct test { variable b: test2[] }; struct test2 { variable c: NUMBER[] }; variable a: test[]; a[0].b[0].c[0] = 1;", "NUMBER", 3,},
                {"struct test { variable b: test2[] }; struct test2 { variable c: test3[] }; struct test3 { variable d: NUMBER[] }; variable a: test[]; a[0].b[0].c[0].d[0] = 1;", "NUMBER", 4,},
                {"struct test { variable b: test2[][][][] }; struct test2 { variable c: test3[] }; struct test3 { variable d: NUMBER[][][] }; variable a: test[][]; a[0][0].b[0][0][0][0].c[0].d[0][0][0] = 1;", "NUMBER", 4,},
                {"struct test { variable b: test2[][][][][][][][][][][][] }; struct test2 { variable c: test3 }; struct test3 { variable d: NUMBER[][][][][][] }; variable a: test[][]; a[0][0].b[0][0][0][0][0][0][0][0][0][0][0][0].c.d[0][0][0][0][0][0] = 1;", "NUMBER", 4,},
                {"struct STRUCTURE1 { variable bool: BOOLEAN[] }; struct STRUCTURE2 { variable inner: STRUCTURE1[] }; variable var: STRUCTURE2; var.inner[0].bool[0] = true;", "BOOLEAN", 3},
                {"struct STRUCTURE1 { variable bool: STRUCTURE2[] }; struct STRUCTURE2 { variable inner: STRUCTURE1[], variable final: NUMBER[] }; variable var: STRUCTURE2[]; var[0].inner[0].bool[0].inner[0].bool[0].final[0] = 1;", "NUMBER", 3},
                {"struct STRUCTURE1 { variable bool: STRUCTURE2[][] }; struct STRUCTURE2 { variable inner: STRUCTURE1[][], variable final: NUMBER[][] }; variable var: STRUCTURE2[]; var[0].inner[0][0].bool[0][0].inner[0][0].bool[0][0].final[0][0] = 1;", "NUMBER", 3}
        };
    }

    @Test(dataProvider = "visitIDArrayExpTestData")
    public void visitIDArrayExp_ValidInput_ReturnsCorrectType(String code, String type, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        IDArrayExpNode idArrayExpNode = (IDArrayExpNode) assStateNode.get_assignId();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(idArrayExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals(type), "Visit IDArrayExp: Expected type " + type + " for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitIDArrayExpInvalidTestData() {
        return new Object[][] {
                {"variable a: NUMBER; a[0] = 1;", 1},
                {"variable a: NUMBER[][]; a[0][0][0] = 2;", 1},
                {"variable a: NUMBER[][]; a[100][5][8] = 2;", 1},
                {"struct test { variable b: NUMBER }; variable a: test; a.b[0] = 1;", 2},
                {"struct test { variable b: NUMBER }; variable a: test; a.b[0][0] = 1;", 2},
                {"struct test { variable b: NUMBER }; variable a: test; a.b.c[0] = 1;", 2},
                {"struct test { variable b: NUMBER }; variable a: test; a.b.c.d[0] = 1;", 2},
                {"struct test { variable b: NUMBER[] }; variable a: test; a.b[0].c.d[0] = 1;", 2},
                {"struct test { variable b: NUMBER[][] }; variable a: test[][]; a[0][0][0].b[0] = 1;", 2},
                {"struct test { variable b: test2[] }; struct test2 { variable c: NUMBER[] }; variable a: test[][]; a[0].b[0].c[0] = 1;", 3,},
                {"struct test { variable b: test2[] }; struct test2 { variable c: NUMBER }; variable a: test[]; a[0] = 1;", 3,},
                {"struct test { variable b: test2[] }; struct test2 { variable c: NUMBER }; variable a: test[]; a[0].b[0].c[0] = 1;", 3,},
                {"struct test { variable b: test2[] }; struct test2 { variable c: test3[][] }; struct test3 { variable d: NUMBER[][] }; variable a: test[]; a[0].b[0].c[0].d[0] = 1;", 4,},
                {"struct test { variable b: test2[][][][][][][][][][][][][] }; struct test2 { variable c: test3 }; struct test3 { variable d: NUMBER[][][][][][] }; variable a: test[][]; a[0][0].b[0][0][0][0][0][0][0][0][0][0][0][0].c.d[0][0][0][0][0][0] = 1;", 4,},
                {"struct STRUCTURE1 { variable bool: STRUCTURE2[][] }; struct STRUCTURE2 { variable inner: STRUCTURE1[][], variable final: NUMBER[][] }; variable var: STRUCTURE2[]; var[0].inner[0][0][0].bool[0][0].inner[0][0].bool[0][0].final[0][0] = 1;", 3},
                {"struct STRUCTURE1 { variable bool: STRUCTURE2[][] }; struct STRUCTURE2 { variable inner: STRUCTURE1[][], variable final: NUMBER[][] }; variable var: STRUCTURE2[]; var[0].inner[0][0][0].bool[0][0].inner[0][0].bool[0][0].final[0][0].oof[0] = 1;", 3}
        };
    }

    @Test(dataProvider = "visitIDArrayExpInvalidTestData")
    public void visitIDArrayExp_InvalidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        IDArrayExpNode idArrayExpNode = (IDArrayExpNode) assStateNode.get_assignId();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(idArrayExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit IDArrayExp: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitEqualExpTestData() {
        return new Object[][] {
                {"variable var: BOOLEAN; variable test: NUMBER; test = 3; var = test == 3;", 3},
                {"variable var: BOOLEAN; variable test: NUMBER; test = 3; var = test != 8;", 3},
                {"variable var: BOOLEAN; variable test: STRING; test = \"hi\"; var = test == \"hi\";", 3},
                {"variable var: BOOLEAN; variable test: STRING; test = \"hi\"; var = test != \"hello\";", 3},
                {"variable var: BOOLEAN; variable test: BOOLEAN; test = true; var = test == true;", 3},
                {"variable var: BOOLEAN; variable test: BOOLEAN; test = true; var = test != false;", 3},
                {"variable var: BOOLEAN; variable test: NUMBER; variable test2: NUMBER; test = 3; test2 = 3; var = test == test2;", 5},
                {"variable var: BOOLEAN; variable test: NUMBER; variable test2: NUMBER; test = 3; test2 = 8; var = test != test2;", 5},
                {"variable var: BOOLEAN; variable test: STRING; variable test2: STRING; test = \"hi\"; test2 = \"hi\"; var = test == test2;", 5},
                {"variable var: BOOLEAN; variable test: STRING; variable test2: STRING; test = \"hi\"; test2 = \"hello\"; var = test != test2;", 5},
                {"variable var: BOOLEAN; variable test: BOOLEAN; variable test2: BOOLEAN; test = true; test2 = true; var = test == test2;", 5},
                {"variable var: BOOLEAN; variable test: BOOLEAN; variable test2: BOOLEAN; test = true; test2 = false; var = test != test2;", 5}
        };
    }

    @Test(dataProvider = "visitEqualExpTestData")
    public void visitEqualExp_ValidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        EqualExpNode equalExpNode = (EqualExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(equalExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("BOOLEAN"), "Visit EqualExp: Expected type BOOLEAN for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitEqualExpInvalidTestData() {
        return new Object[][]{
                {"variable test: BOOLEAN; test = true == 1;", 1},
                {"variable test: BOOLEAN; test = true == \"hi\";", 1},
                {"variable test: BOOLEAN; test = true != 1;", 1},
                {"variable test: BOOLEAN; test = true != \"hi\";", 1},
                {"variable test: BOOLEAN; test = 1 == true;", 1},
                {"variable test: BOOLEAN; test = 1 == \"hi\";", 1},
                {"variable test: BOOLEAN; test = 1 != true;", 1},
                {"variable test: BOOLEAN; test = 1 != \"hi\";", 1},
                {"variable test: BOOLEAN; test = \"hi\" == true;", 1},
                {"variable test: BOOLEAN; test = \"hi\" == 1;", 1},
                {"variable test: BOOLEAN; test = \"hi\" != true;", 1},
                {"variable test: BOOLEAN; test = \"hi\" != 1;", 1},
                {"variable test: BOOLEAN; test = var1 == var2;", 1},
                {"variable test: BOOLEAN; test = var1 != var2;", 1},
                {"variable test: BOOLEAN; variable var1: NUMBER; variable var2: NUMBER; test = var1 == var2;", 3},
                {"variable test: BOOLEAN; variable var1: NUMBER; variable var2: STRING; test = var1 == var2;", 3},
                {"variable test: BOOLEAN; variable var1: NUMBER; variable var2: NUMBER; test = var1 != var2;", 3},
                {"variable test: BOOLEAN; variable var1: NUMBER; variable var2: STRING; test = var1 != var2;", 3},
                {"variable test: BOOLEAN; struct STRUCTURE { variable x: NUMBER }; variable var1: STRUCTURE; variable var2: STRUCTURE; var1 = 1; var2 = 1; test = var1 == var2;", 6},
                {"variable test: BOOLEAN; struct STRUCTURE { variable x: NUMBER }; variable var1: STRUCTURE; variable var2: STRUCTURE; var1 = 1; var2 = 1; test = var1 != var2;", 6}
        };
    }

    @Test(dataProvider = "visitEqualExpInvalidTestData")
    public void visitEqualExp_InvalidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        AssStateNode assStateNode = (AssStateNode) mctlNode.get_children().get(index);
        EqualExpNode equalExpNode = (EqualExpNode) assStateNode.get_assignExp();

        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(equalExpNode);

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit EqualExp: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitRepeatStateTestData() {
        return new Object[][] {
                {"repeat (true and true) {}", "BOOLEAN", 0},
                {"repeat (1 + 1 == 2 and 3 > 2) {}", "BOOLEAN", 0},
                {"variable test1: BOOLEAN; variable test2: BOOLEAN; test1 = true; test2 = true; repeat (test1 and test2) {}", "BOOLEAN", 4},
                {"repeat (10) {}", "NUMBER", 0}
        };
    }

    @Test(dataProvider = "visitRepeatStateTestData")
    public void visitRepeatState_ValidInput_ReturnsCorrectType(String code, String type, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        RepeatStateNode repeatStateNode = (RepeatStateNode) mctlNode.get_children().get(index);
        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(repeatStateNode.get_repeatExp());

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals(type), "Visit RepeatState: Expected type " + type + " for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitRepeatStateInvalidTestData() {
        return new Object[][] {
                {"repeat (!2) {}", 0},
                {"repeat (!\"per\") {}", 0},
                {"repeat (!\"per\" or \"bob\") {}", 0},
                {"repeat (var) {}", 0},
                {"variable var1: BOOLEAN; variable var2: BOOLEAN; repeat (var1 and var2) {}", 2}
        };
    }

    @Test(dataProvider = "visitRepeatStateInvalidTestData")
    public void visitRepeatState_InvalidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        RepeatStateNode repeatStateNode = (RepeatStateNode) mctlNode.get_children().get(index);
        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(repeatStateNode.get_repeatExp());

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit RepeatState: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitIfStateTestData() {
        return new Object[][] {
                {"if (true) { var = a; } else if (true) { var = b; } else { var = c; }"},
                {"if (true) { var = a; } else { var = b; }"},
                {"if (true) { var = a; }"},
                {"if (true) {}"},
        };
    }

    @Test(dataProvider = "visitIfStateTestData")
    public void visitIfState_ValidInput_ReturnsCorrectType(String code) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        IfStateNode ifStateNode = (IfStateNode) mctlNode.get_children().get(0);
        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(ifStateNode.get_expChildren().get(0));

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("BOOLEAN"), "Visit IfState: Expected type BOOLEAN for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] visitIfStateInvalidTestData() {
        return new Object[][] {
                {"if (!2) {}", 0},
                {"if (!\"per\") {}", 0},
                {"if (!\"per\" or \"bob\") {}", 0},
                {"if (var) {}", 0},
                {"variable var1: BOOLEAN; variable var2: BOOLEAN; if (var1 and var2) {}", 2}
        };
    }

    @Test(dataProvider = "visitIfStateInvalidTestData")
    public void visitIfState_InvalidInput_ReturnsCorrectType(String code, int index) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        IfStateNode ifStateNode = (IfStateNode) mctlNode.get_children().get(index);
        MctlTypeDescriptor typeDescriptor = typeCheckingVisitor.visit(ifStateNode.get_expChildren().get(0));

        softAssert.assertTrue(typeDescriptor.get_type_literal().equals("NOTHING"), "Visit IfState: Expected type NOTHING for '" + code + "' but got: " + typeDescriptor.get_type_literal());
        softAssert.assertAll();
    }

    @DataProvider
    public Object[][] variableScopeTestData() {
        return new Object[][] {
                {"variable x: NUMBER; x = 10; to f(): NUMBER { return x; } to g(x: BOOLEAN): NUMBER { return f(); } g(true);", 0}, // Will fail if the variable scope rules are dynamic
                {"variable x: BOOLEAN; to f(): BOOLEAN { return x; } to g(x: BOOLEAN): BOOLEAN { return f(); } g(true);", 1} // Will fail if the correct scope rules applies for variables as x in global scope is not instantiated
        };
    }
    @Test(dataProvider = "variableScopeTestData")
    public void variableScopeTest_usesStaticScoping_returnsCorrectValue(String code, int expectedProblems) {
        ParseTree parseTree = createParseTree(code);
        MctlNode mctlNode = (MctlNode) parseTree.accept(astBuilder);

        symbolTableVisitor.visit(mctlNode);

        Assert.assertNotNull(mctlNode, "Should create a mctl abstract syntax tree");
        Assert.assertEquals(problemCollection.getProblems().size(), expectedProblems, "Should add: \"" + expectedProblems + "\" to the problem collection but added: \"" + problemCollection.getProblems().size() + "\"");
    }
}
