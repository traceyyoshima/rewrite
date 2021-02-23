package org.openrewrite.java;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.format.AutoFormatVisitor;
import org.openrewrite.java.tree.*;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.openrewrite.Tree.randomId;

/**
 * Initial goal:
 * A recipe that converts non-fluent setters to fluent setters on a target class.
 * Non-fluent setters are qualified using the following conditions:
 *  - method exists in the target class.
 *  - method is public.
 *  - method has 1 argument.
 *  - method contains 1 assignment to a member variable in the class.
 *  - method returns a void.
 *
 * Stretch goal:
 * Also, convert chains of setters to fluent setters.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChangeNonFluentSetterToFluentSetter extends Recipe {

    /**
     * A fully-qualified class name in which the non-fluent setter exists.
     */
    private final String fullyQualifiedTargetTypeName;

    @Override
    protected JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new ChangeNonFluentSetterToFluentSetterVisitor();
    }

    private class ChangeNonFluentSetterToFluentSetterVisitor extends JavaIsoVisitor<ExecutionContext> {
        private final JavaTemplate addFluentReturnTemplate = template("\nreturn this;").build();
        private final JavaType.FullyQualified classType = JavaType.Class.build(fullyQualifiedTargetTypeName);

        /**
         * Qualifies method declarations as a non-fluent setters on the target class and updates qualified
         * method declarations to fluent setters.
         */
        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            J.MethodDeclaration m = super.visitMethodDeclaration(method, ctx);

            final J.ClassDeclaration classDecl = getCursor().firstEnclosingOrThrow(J.ClassDeclaration.class);
            if (isChangeableToFluentSetter(method, classDecl)) {
                assert method.getReturnTypeExpression() != null;
                m = m.withReturnTypeExpression(
                        J.Identifier.build(randomId(),
                                method.getReturnTypeExpression().getPrefix(),
                                Markers.EMPTY,
                                classType.getClassName(),
                                JavaType.Class.build(classType.getFullyQualifiedName())));

                assert method.getBody() != null;
                m = m.withTemplate(addFluentReturnTemplate, method.getBody().getCoordinates().lastStatement());
            }

            return m;
        }

        /**
         * Returns whether the method is a non-fluent setter and qualifies to change to fluent setter.
         * @implNote The assertions are pretty strict and could be relaxed to allow for further refactoring.
         *           i.e. a setter will not be qualified to be fluent if business logic exists in the setter.
         */
        private boolean isChangeableToFluentSetter(J.MethodDeclaration method, J.ClassDeclaration classDecl) {
            // Assert the class matches the target.
            if (classDecl.getType() == null
                    || !classDecl.getType().getFullyQualifiedName().equals(classType.getFullyQualifiedName())) {
                return false;
            }

            // Assert the method is public.
            if (method.getModifiers().stream().noneMatch(
                    id -> Flag.Public.toString().equals(id.getType().toString()))) {
                return false;
            }

            // Assert the return type is void.
            if (method.getReturnTypeExpression() == null
                    || method.getReturnTypeExpression().getType() == null
                    || method.getReturnTypeExpression().getType() != JavaType.Primitive.Void) {
                return false;
            }

            // Assert the method has one setter.
            if (method.getParameters().size() != 1) {
                return false;
            }

            // Assert the method has one assignment statement.
            if (method.getBody() == null
                    || method.getBody().getStatements().size() != 1
                    || !(method.getBody().getStatements().get(0) instanceof J.Assignment)) {
                return false;
            }

            // Asserts the assignment is set to a member within the class.
            J.Assignment assignment = (J.Assignment) method.getBody().getStatements().get(0);
            if (assignment.getVariable() instanceof J.FieldAccess) {
                // this.foo = foo is a field access.
                return ((JavaType.Class) classType).getMembers().stream().anyMatch(
                        id -> id.getName().equals(
                                ((J.FieldAccess) assignment.getVariable()).getSimpleName()));
            } else if (assignment.getVariable() instanceof J.Identifier) {
                // foo = value is an identifier.
                return ((JavaType.Class) classType).getMembers().stream().anyMatch(
                        id -> id.getName().equals(
                                ((J.Identifier) assignment.getVariable()).getSimpleName()));
            } else {
                return false;
            }
        }

        /**
         * Qualifies sequences of fluent setters and converts them to chained fluent setters.
         */
        @Override
        public J.Block visitBlock(J.Block block, ExecutionContext ctx) {
            J.Block b = super.visitBlock(block, ctx);

            // Prevent overriding blocks that shouldn't be change.
            if (block.getStatements().stream().noneMatch(s -> s instanceof J.MethodInvocation)) {
                return b;
            }

            List<Statement> statements = new ArrayList<>();
            String cachedCode = "";
            boolean isCacheStored = false;

            for (int i = 0; i < block.getStatements().size(); i++) {
                if (block.getStatements().get(i) instanceof J.MethodInvocation) {
                    J.MethodInvocation method = ((J.MethodInvocation) block.getStatements().get(i));
                    if (isFluentSetter(method)) {
                        if (!isCacheStored) {
                            cachedCode = StringUtils.trimIndent(method.print());
                            isCacheStored = true;
                        } else {
                            String postFix = "\n." + StringUtils.trimIndent(method.withSelect(null).print());
                            cachedCode = cachedCode.replace(";", "") + postFix;
                        }

                        if (!StringUtils.isNullOrEmpty(cachedCode) && i == block.getStatements().size()-1) {
                            statements.add(
                                    generateStatementFromCachedCode(method, cachedCode, ctx));
                        }
                        continue;
                    }
                }

                Statement statement = block.getStatements().get(i);
                if (isCacheStored && !StringUtils.isNullOrEmpty(cachedCode)) {
                    statements.add(
                            generateStatementFromCachedCode(statement, cachedCode, ctx));

                    cachedCode = "";
                    isCacheStored = false;
                }

                statements.add(statement);
            }

            return b.withStatements(statements);
        }

        /**
         * Uses an explicit AutoFormatVisitor, since the base statement.withTemplate
         * generated excessive whitespace.
         */
        private Statement generateStatementFromCachedCode(Statement statement, String cachedCode, ExecutionContext ctx) {
            assert !StringUtils.isNullOrEmpty(cachedCode);

            return (Statement) Objects.requireNonNull(
                    new AutoFormatVisitor<ExecutionContext>()
                            .visit(statement.withTemplate(
                                    template(cachedCode).build(),
                                    statement.getCoordinates().replace()),
                                    ctx,
                                    getCursor()));
        }

        /**
         * Returns if a method invocation qualifies as a fluent setter.
         */
        private boolean isFluentSetter(@Nullable J.MethodInvocation mi) {
            return (mi != null
                    // Assert the class matches the target;
                    && mi.getSelect() != null
                    && TypeUtils.isOfClassType(
                            mi.getSelect().getType(), classType.getFullyQualifiedName())
                    // Assert the flag is public;
                    && mi.getType() != null
                    && mi.getType().getFlags().stream().anyMatch(id -> Flag.Public.toString().equals(id.toString()))
                    // Assert the setter returns the declaring type.
                    && mi.getReturnType() != null
                    && TypeUtils.isOfClassType(mi.getReturnType(), classType.getFullyQualifiedName())
                    // Assert the method has one setter.
                    && mi.getArguments().size() == 1);
        }

    }
}
