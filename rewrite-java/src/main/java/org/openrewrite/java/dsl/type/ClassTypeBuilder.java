package org.openrewrite.java.dsl.type;

import org.openrewrite.java.tree.JavaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClassTypeBuilder {

    private boolean relaxedTypeMatch = true;
    private String fullyQualifiedName;
    private List<JavaType.Var> members = Collections.emptyList();
    private List<JavaType> typeParameters = Collections.emptyList();
    private List<JavaType> interfaces = Collections.emptyList();
    private List<JavaType.Method> constructors = Collections.emptyList();
    private JavaType.Class superType;

    ClassTypeBuilder() {
    }

    public ClassTypeBuilder relaxedTypeMatch(boolean relaxedTypeMatch) {
        this.relaxedTypeMatch = relaxedTypeMatch;
        return this;
    }

    public ClassTypeBuilder fullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
        return this;
    }

    public ClassTypeBuilder memberVariables(JavaType.Var... memberVariables) {
        assert(memberVariables != null);
        if (this.members == Collections.<JavaType.Var>emptyList()) {
            this.members = new ArrayList<>();
        }
        this.members.addAll(Arrays.asList(memberVariables));
        return this;
    }

    public ClassTypeBuilder typeParameters(JavaType... typeParameters) {
        assert(typeParameters != null);
        if (this.typeParameters == Collections.<JavaType>emptyList()) {
            this.typeParameters = new ArrayList<>();
        }
        this.typeParameters.addAll(Arrays.asList(typeParameters));
        return this;
    }

    public ClassTypeBuilder typeParameters(String... typeParameters) {
        assert(typeParameters != null);
        if (this.typeParameters == Collections.<JavaType>emptyList()) {
            this.typeParameters = new ArrayList<>();
        }
        for (String typeParameter : typeParameters) {
            this.typeParameters.add(JavaType.buildType(typeParameter));
        }
        return this;
    }

    public ClassTypeBuilder interfaces(JavaType... interfaces) {
        assert(interfaces != null);
        if (this.interfaces == Collections.<JavaType>emptyList()) {
            this.interfaces = new ArrayList<>();
        }
        this.interfaces.addAll(Arrays.asList(interfaces));
        return this;
    }

    public ClassTypeBuilder interfaces(String... interfaces) {
        assert(interfaces != null);
        if (this.interfaces == Collections.<JavaType>emptyList()) {
            this.interfaces = new ArrayList<>();
        }
        for (String interfaceName : interfaces) {
            this.interfaces.add(JavaType.buildType(interfaceName));
        }
        return this;
    }

    public ClassTypeBuilder constructors(JavaType.Method... constructors) {
        assert(constructors != null);
        if (this.constructors == Collections.<JavaType.Method>emptyList()) {
            this.constructors = new ArrayList<>();
        }
        this.constructors.addAll(Arrays.asList(constructors));
        return this;
    }

    public ClassTypeBuilder superType(JavaType.Class superType) {
        this.superType = superType;
        return this;
    }

    public ClassTypeBuilder superType(String superType) {
        this.superType = JavaType.Class.build(superType);
        return this;
    }

    public JavaType.Class resolve() {
        if (fullyQualifiedName == null) {
            throw new IllegalStateException("A fully-qualified class name is required.");
        }
        return JavaType.Class.build(fullyQualifiedName, members, typeParameters, interfaces, constructors, superType,
                relaxedTypeMatch);
    }
}
