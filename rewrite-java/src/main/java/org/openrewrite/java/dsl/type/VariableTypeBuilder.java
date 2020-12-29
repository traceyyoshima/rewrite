package org.openrewrite.java.dsl.type;

import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.tree.Flag;
import org.openrewrite.java.tree.JavaType;

import java.util.*;

public class VariableTypeBuilder {

    private Set<Flag> flags = Collections.emptySet();
    private JavaType type;
    private String name;

    VariableTypeBuilder() {
    }

    /**
     * @param flags a list of qualifiers (public, static, etc) for the variable
     */
    public VariableTypeBuilder flags(Flag ... flags) {
        assert(flags != null);
        if (this.flags == Collections.<Flag>emptySet()) {
            this.flags = new HashSet<>();
        }
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    public VariableTypeBuilder type(JavaType variableType) {
        assert(variableType != null);
        this.type = variableType;
        return this;
    }

    public VariableTypeBuilder type(String variableType) {
        assert(variableType != null);
        this.type = JavaType.Class.build(variableType);
        return this;
    }

    public VariableTypeBuilder name(String variableName) {
        assert(variableName != null);
        this.name = variableName;
        return this;
    }

    public JavaType.Var build() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("The variable name is required.");
        }
        if (type == null) {
            throw new IllegalStateException("The variable type is required.");
        }
        return new JavaType.Var(name, type, flags);
    }
}
