/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

interface ChangeNonFluentSetterToFluentSetterTest : JavaRecipeTest {

    @Test
    fun doNotChangeEmptySetter(jp: JavaParser) = assertUnchanged(
        jp,
        recipe = ChangeNonFluentSetterToFluentSetter("org.A"),
        before = """
            package org;

            public class A {
                private String foo;

                public void setFoo(String value) {
                }
            }
        """
    )

    @Test
    fun doNotChangeSetterWithReturnType(jp: JavaParser) = assertUnchanged(
        jp,
        recipe = ChangeNonFluentSetterToFluentSetter("org.A"),
        before = """
            package org;

            public class A {
                private String foo;

                public String setFoo(String value) {
                    foo = value;
                    return foo;
                }
            }
        """
    )

    @Test
    fun doNotChangeFluentSetter(jp: JavaParser) = assertUnchanged(
        jp,
        recipe = ChangeNonFluentSetterToFluentSetter("org.A"),
        before = """
            package org;

            public class A {
                private String foo;

                public A setFoo(String value) {
                    foo = value;
                    return this;
                }
            }
        """
    )

    @Test
    fun changeNonFluentSetterToFluentSetter(jp: JavaParser) = assertChanged(
        jp,
        recipe = ChangeNonFluentSetterToFluentSetter("org.A"),
        before = """
            package org;

            public class A {
                private String foo;
                private String bar;

                public void setFoo(String value) {
                    foo = value;
                }
            }
        """,
        after = """
            package org;

            public class A {
                private String foo;
                private String bar;

                public A setFoo(String value) {
                    foo = value;
                    return this;
                }
            }
        """
    )

    @Test
    fun changeOfFluentSetterRetainsWhitespace(jp: JavaParser) = assertChanged(
        jp,
        recipe = ChangeNonFluentSetterToFluentSetter("org.A"),
        before = """
            package org;

            public class A {
                private String foo;
                private String bar;

                public void setFoo(String value) {

                    foo = value;

                }
            }
        """,
        after = """
            package org;

            public class A {
                private String foo;
                private String bar;

                public A setFoo(String value) {

                    foo = value;
                    return this;

                }
            }
        """
    )

    @Test
    fun fluentSetterWorksWithFieldAccess(jp: JavaParser) = assertChanged(
        jp,
        recipe = ChangeNonFluentSetterToFluentSetter("org.A"),
        before = """
            package org;

            public class A {
                private String foo;
                private String bar;

                public void setFoo(String foo) {
                    this.foo = foo;
                }
            }
        """,
        after = """
            package org;

            public class A {
                private String foo;
                private String bar;

                public A setFoo(String foo) {
                    this.foo = foo;
                    return this;
                }
            }
        """
    )


    @Test
    fun doNotChangeChainOfSettersFromDifferentClass(jp: JavaParser) = assertUnchanged(
        jp,
        dependsOn = arrayOf(
            """
                package org;

                public class C {
                    private String foo;
                    private String bar;

                    public C setFoo(String value) {
                        foo = value;
                        return this;
                    }

                    public C setBar(String value) {
                        bar = value;
                        return this;
                    }
                }
            """),
        recipe = ChangeNonFluentSetterToFluentSetter("org.A"),
        before = """
            package org.packageB;

            import org.C;

            public class B {

                public void testC1() {
                    C c = new C();
                    c.setFoo("foo");
                    c.setBar("bar");
                }
            }
        """
    )

    @Test
    fun onlyChangeChainsOfFluentSetters(jp: JavaParser) = assertChanged(
        jp,
        dependsOn = arrayOf(
            """
                package org;

                public class A {
                    private String foo;
                    private String bar;
                    private String baz;
                    private String qux;
                    private String quuz;

                    public A setFoo(String value) {
                        foo = value;
                        return this;
                    }

                    public A setBar(String value) {
                        bar = value;
                        return this;
                    }

                    public void setBaz(String value) {
                        baz = value;
                    }

                    public A setQux(String value) {
                        qux = value;
                        return this;
                    }

                    public A setQuuz(String value) {
                        quuz = value;
                        return this;
                    }
                }
            """),
        recipe = ChangeNonFluentSetterToFluentSetter("org.A"),
        before = """
            package org.packageB;

            import org.A;

            public class B {

                public A testA1() {
                    A a = new A();
                    a.setFoo("foo");
                    String bar = "bar";
                    a.setBar(bar);

                    a.setBaz("baz");
                    a.setQux("qux");
                    a.setQux("quuz");
                    return a;
                }
            }
        """,
        after = """
            package org.packageB;

            import org.A;

            public class B {

                public A testA1() {
                    A a = new A();
                    a.setFoo("foo");
                    String bar = "bar";

                    a.setBar(bar);

                    a.setBaz("baz");
                    a.setQux("qux")
                            .setQux("quuz");
                    return a;
                }
            }
        """
    )

    @Test
    fun changeAllNonFluentSettersToFluentSetters(jp: JavaParser) = assertChanged(
        jp,
        dependsOn = arrayOf(
            """
                package org;

                public class A {
                    private String foo;
                    private String bar;

                    public A setFoo(String value) {
                        foo = value;
                        return this;
                    }

                    public A setBar(String value) {
                        bar = value;
                        return this;
                    }

                    public A setBuz(String value) {
                        buz = value;
                        return this;
                    }
                }
            """),
        recipe = ChangeNonFluentSetterToFluentSetter("org.A"),
        before = """
            package org.packageB;

            import org.A;

            public class B {

                public void testA1() {
                    A a = new A();
                    a.setFoo("foo");
                    a.setBar("bar");
                    a.setBuz("buz");
                }
            }
        """,
        after = """
            package org.packageB;

            import org.A;

            public class B {

                public void testA1() {
                    A a = new A();
                    a.setFoo("foo")
                            .setBar("bar")
                            .setBuz("buz");
                }
            }
        """
    )

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Test
    fun checkValidation() {
        val arg1 = "fullyQualifiedTargetTypeName"
        val recipe = ChangeNonFluentSetterToFluentSetter(null)
        val valid = recipe.validate()
        Assertions.assertThat(valid.isValid).isFalse
        Assertions.assertThat(valid.failures()).hasSize(1)
        Assertions.assertThat(valid.failures()[0].property).isEqualTo(arg1)
    }
}
