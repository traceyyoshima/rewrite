package org.openrewrite.java.dsl.type;

import org.openrewrite.java.tree.JavaType;

public class JavaTypes {

    public static VariableTypeBuilder variableType() {
        return new VariableTypeBuilder();
    }

    public static ClassTypeBuilder classType() {
        return new ClassTypeBuilder();
    }

    public static MethodTypeBuilder methodType() {
        return new MethodTypeBuilder();
    }

    public static JavaType.Array arrayType(JavaType elementType) {
       assert(elementType != null);
       return new JavaType.Array(elementType);
    }

    public static JavaType.Array arrayType(String elementType) {
        assert(elementType != null);
        return new JavaType.Array(JavaType.buildType(elementType));
    }

    public static JavaType.Primitive booleanPrimitive() {
        return JavaType.Primitive.Boolean;
    }

    public static JavaType.Class booleanClass() {
        return JavaType.Class.build("java.lang.Boolean");
    }

    public static JavaType.Primitive bytePrimitive() {
        return JavaType.Primitive.Byte;
    }
    public static JavaType.Class byteClass() {
        return JavaType.Class.build("java.lang.Byte");
    }

    public static JavaType.Primitive charPrimitive() {
        return JavaType.Primitive.Char;
    }
    public static JavaType.Class charClass() {
        return JavaType.Class.build("java.lang.Char");
    }

    public static JavaType.Primitive doublePrimitive() {
        return JavaType.Primitive.Double;
    }
    public static JavaType.Class doubleClass() {
        return JavaType.Class.build("java.lang.Double");
    }
    public static JavaType.Primitive floatPrimitive() {
        return JavaType.Primitive.Float;
    }
    public static JavaType.Class floatClass() {
        return JavaType.Class.build("java.lang.Float");
    }
    public static JavaType.Primitive integerPrimitive() {
        return JavaType.Primitive.Int;
    }
    public static JavaType.Class integerClass() {
        return JavaType.Class.build("java.lang.Integer");
    }
    public static JavaType.Primitive longPrimitive() {
        return JavaType.Primitive.Long;
    }
    public static JavaType.Class longClass() {
        return JavaType.Class.build("java.lang.Long");
    }
    public static JavaType.Primitive shortPrimitive() {
        return JavaType.Primitive.Short;
    }
    public static JavaType.Class shortClass() {
        return JavaType.Class.build("java.lang.Short");
    }
    public static JavaType.Primitive voidPrimitive() {
        return JavaType.Primitive.Void;
    }
    public static JavaType.Primitive stringPrimitive() {
        return JavaType.Primitive.String;
    }
    public static JavaType.Class stringClass() {
        return JavaType.Class.build("java.lang.String");
    }
    public static JavaType.Primitive wildcardPrimitive() {
        return JavaType.Primitive.Wildcard;
    }
    public static JavaType.Primitive nullPrimitive() {
        return JavaType.Primitive.Null;
    }
    public static JavaType.Class dateClass() {
        return JavaType.Class.build("java.util.Date");
    }
}
