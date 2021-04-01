package org.malovitsinm.compiletest;

import com.google.common.base.Joiner;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.malovitsinm.processors.BuilderProcessor;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class AnnotationProcessorTest {

    private final JavaFileObject samplePojo = JavaFileObjects.forSourceString("org.malovitsinm.pojo.Sword",
            "package org.malovitsinm.pojo;" +
                    "import org.malovitsinm.annotations.GeneratedBuilder;" +
                    "@GeneratedBuilder(setterPrefix = \"set\")" +
                    "public class Sword {" +
                    "private Integer durability;" +
                    "private String title;" +
                    "private Integer damage;" +
                    "public void setDurability(Integer durability) {this.durability = durability;}" +
                    "public void setDamage(Integer damage) {this.damage = damage;}" +
                    "public void setTitle(String title) {this.title = title;}" +
                    "public String getTitle() {return title;}" +
                    "public Integer getDurability() {return durability;}" +
                    "public Integer getDamage() {return damage;}" +
                    "}"
    );


    JavaFileObject expectedBuilderSource = JavaFileObjects.forSourceString("org.malovitsinm.pojo.SwordBuilder",
            Joiner.on('\n').join(
                    "package org.malovitsinm.pojo;",
                    "",
                    "import java.lang.Integer;",
                    "import java.lang.String;",
                    "import javax.annotation.Generated;",
                    "",
                    "@Generated(\"org.malovitsinm.processors.BuilderProcessor\")",
                    "public final class SwordBuilder {",
                    "  private Sword instance;",
                    "",
                    "  private SwordBuilder(Sword pojo) {",
                    "    this.instance = pojo;",
                    "  }",
                    "",
                    "  public static final SwordBuilder aSword() {",
                    "    return new SwordBuilder(new Sword());",
                    "  }",
                    "",
                    "  public SwordBuilder withDurability(Integer durability) {",
                    "    this.instance.setDurability(durability);",
                    "    return this;",
                    "  }",
                    "",
                    "  public SwordBuilder withDamage(Integer damage) {",
                    "    this.instance.setDamage(damage);",
                    "    return this;",
                    "  }",
                    "",
                    "  public SwordBuilder withTitle(String title) {",
                    "    this.instance.setTitle(title);",
                    "    return this;",
                    "  }",
                    "",
                    "  public Sword build() {",
                    "    return instance;",
                    "  }",
                    "}"));


    @Test
    public void compilesWithProcessor() {
        Compilation compilation = javac()
                .withProcessors(new BuilderProcessor())
                .compile(samplePojo);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("org.malovitsinm.pojo.SwordBuilder")
                .hasSourceEquivalentTo(expectedBuilderSource);
    }

}