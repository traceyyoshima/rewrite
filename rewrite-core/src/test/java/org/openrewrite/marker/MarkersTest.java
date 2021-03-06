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
package org.openrewrite.marker;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkersTest {
    @Test
    public void addPreventsDuplicates() {
        Markers markers = Markers.EMPTY;
        markers = markers.add(new TextMarker("test"));
        markers = markers.add(new TextMarker("test"));
        assertThat(markers.findAll(TextMarker.class)).hasSize(1);
    }

    @Test
    public void addAcceptsNonDuplicates() {
        Markers markers = Markers.EMPTY;
        markers = markers.add(new TextMarker("thing1"));
        markers = markers.add(new TextMarker("thing2"));
        assertThat(markers.findAll(TextMarker.class)).hasSize(2);
    }
    
    private static class TextMarker implements Marker {
        private final String text;

        private TextMarker(String text) {
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TextMarker that = (TextMarker) o;
            return text.equals(that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text);
        }
    }
}
