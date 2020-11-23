package org.openrewrite.maven.tree;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openrewrite.Formatting;
import org.openrewrite.Metadata;
import org.openrewrite.SourceVisitor;
import org.openrewrite.maven.MavenSourceVisitor;
import org.openrewrite.xml.XmlSourceVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.Collection;

import static java.util.Collections.emptyList;

public class Maven extends Xml.Document {
    private final transient Pom model;
    private final transient Collection<Pom> modules;

    public Maven(Xml.Document document) {
        super(
                document.getId(),
                document.getSourcePath(),
                document.getMetadata(),
                document.getProlog(),
                document.getRoot(),
                document.getFormatting()
        );

        model = getMetadata(Pom.class);
        assert model != null;

        Modules modulesContainer = getMetadata(Modules.class);
        modules = modulesContainer == null ? emptyList() :
                modulesContainer.getModules();
    }

    @JsonIgnore
    public Pom getModel() {
        return model;
    }

    @JsonIgnore
    public Collection<Pom> getModules() {
        return modules;
    }

    @Override
    public <R> R accept(SourceVisitor<R> v) {
        if (v instanceof MavenSourceVisitor) {
            return ((MavenSourceVisitor<R>) v).visitMaven(this);
        } else if (v instanceof XmlSourceVisitor) {
            return super.accept(v);
        }
        return v.defaultTo(null);
    }

    @Override
    public Maven withRoot(Tag root) {
        return new Maven(super.withRoot(root));
    }

    @Override
    public Maven withMetadata(Collection<Metadata> metadata) {
        return new Maven(super.withMetadata(metadata));
    }

    @Override
    public Maven withFormatting(Formatting formatting) {
        return new Maven(super.withFormatting(formatting));
    }

    @Override
    public Maven withProlog(Prolog prolog) {
        return new Maven(super.withProlog(prolog));
    }
}