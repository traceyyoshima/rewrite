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
package org.openrewrite.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.properties.tree.Properties;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChangePropertyKey extends Recipe {

    private final String oldPropertyKey;
    private final String newPropertyKey;

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ChangePropertyKeyVisitor<>();
    }

    public class ChangePropertyKeyVisitor<P> extends PropertiesVisitor<P> {

        public ChangePropertyKeyVisitor() {
        }

        @Override
        public Properties visitEntry(Properties.Entry entry, P p) {
            if (entry.getKey().equals(oldPropertyKey)) {
                entry = entry.withKey(newPropertyKey);
            }
            return super.visitEntry(entry, p);
        }
    }

}
