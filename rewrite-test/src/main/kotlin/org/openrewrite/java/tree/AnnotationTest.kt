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
package org.openrewrite.java.tree

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openrewrite.Issue
import org.openrewrite.java.JavaParser
import org.openrewrite.java.JavaTreeTest
import org.openrewrite.java.JavaTreeTest.NestingLevel.CompilationUnit

interface AnnotationTest : JavaTreeTest {

    @Test
    fun annotationWithDefaultArgument(jp: JavaParser) = assertParsePrintAndProcess(
        jp, CompilationUnit, """
           @SuppressWarnings("ALL")
           public class A {}
        """
    )

    @Test
    fun annotationWithArgument(jp: JavaParser) = assertParsePrintAndProcess(
        jp, CompilationUnit, """
           @SuppressWarnings(value = "ALL")
           public class A {}
        """
    )

    @Test
    fun preserveOptionalEmptyParentheses(jp: JavaParser) = assertParsePrintAndProcess(
        jp, CompilationUnit, """
           @Deprecated ( )
           public class A {}
        """
    )

    @Test
    fun newArrayArgument(jp: JavaParser) = assertParsePrintAndProcess(
        jp, CompilationUnit, """
            import java.lang.annotation.Target;
            import static java.lang.annotation.ElementType.*;

            @Target({ FIELD, PARAMETER })
            public @interface Annotation {}
        """
    )

    @Test
    @Issue("https://github.com/openrewrite/rewrite/issues/254")
    fun annotationPositionsOnMethods(jp: JavaParser) = assertParsePrintAndProcess(
        jp, CompilationUnit, """
            import java.lang.annotation.*;
            
            public class Test {
                @Ho
                public @Ho <T> @Ho T merryChristmas() {
                    return null;
                }
                
                @Ho
                <T> @Ho T merryChristmas2() {
                    return null;
                }
                
                @Ho Test merryChristmas3() {
                    return null;
                }
            }
            
            @Target({ElementType.TYPE_USE})
            @interface Hos {
                Ho[] value();
            }
            
            @Target({ElementType.TYPE_USE})
            @Repeatable(Hos.class)
            @interface Ho {}
        """
    )
}
