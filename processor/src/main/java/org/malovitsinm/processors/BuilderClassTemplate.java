package org.malovitsinm.processors;

import com.squareup.javapoet.*;

import javax.annotation.Generated;
import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BuilderClassTemplate {

    private TypeElement targetPojo;
    private List<ExecutableElement> setterMethods;
    private String setterPrefix;

    private ClassName builderClassName;
    private String targetPackage;

    private BuilderClassTemplate() {
    }

    public void writeToFiler(Filer filer) throws IOException {
        AnnotationSpec generated = AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", "org.malovitsinm.processors.BuilderProcessor")
                .build();

        TypeSpec typeSpec = TypeSpec.classBuilder(builderClassName)
                .addAnnotation(generated)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(ClassName.get(targetPojo), "instance", Modifier.PRIVATE)
                .addMethod(builderConstructor())
                .addMethod(staticBuilderMethod())
                .addMethods(
                        setterMethods.stream().map(this::builderStage).collect(Collectors.toList())
                )
                .addMethod(buildMethod())
                .build();

        JavaFile javaFile = JavaFile.builder(targetPackage, typeSpec).build();
        javaFile.writeTo(filer);
    }

    private MethodSpec builderStage(ExecutableElement setterMethod) {
        VariableElement setterParameter = setterMethod.getParameters().get(0);
        CodeBlock methodBody = CodeBlock.builder()
                .addStatement("this.instance.$L($L)", setterMethod.getSimpleName(), setterParameter)
                .addStatement("return this")
                .build();

        return MethodSpec.methodBuilder("with" + setterMethod.getSimpleName().toString().replace(setterPrefix, ""))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(setterParameter.asType()), setterParameter.getSimpleName().toString())
                .returns(builderClassName)
                .addCode(methodBody)
                .build();
    }

    private MethodSpec buildMethod() {
        return MethodSpec.methodBuilder("build")
                .returns(ClassName.get(targetPojo))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return instance")
                .build();
    }

    private MethodSpec builderConstructor() {
        ParameterSpec parameterSpec = ParameterSpec
                .builder(TypeName.get(targetPojo.asType()), "pojo")
                .build();
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(parameterSpec)
                .addStatement("this.instance = $N", parameterSpec)
                .build();
    }

    private MethodSpec staticBuilderMethod() {
        String prefix = isVowel(targetPojo.getSimpleName().toString().charAt(0)) ? "an" : "a";
        return MethodSpec.methodBuilder(prefix + targetPojo.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .returns(builderClassName)
                .addStatement("return new $T(new $T())", builderClassName, TypeName.get(targetPojo.asType()))
                .build();
    }

    private boolean isVowel(char character) {
        return "aeiou".indexOf(character) >= 0;
    }


    public static final class TemplateBuilder {
        private TypeElement targetPojo;
        private List<ExecutableElement> setterMethods;
        private String setterPrefix;
        private String targetPackage;

        private TemplateBuilder() {
        }

        public static TemplateBuilder aBuilderClassTemplate() {
            return new TemplateBuilder();
        }

        public TemplateBuilder withTargetPojo(TypeElement targetPojo) {
            this.targetPojo = targetPojo;
            return this;
        }

        public TemplateBuilder withSetterMethods(List<ExecutableElement> setterMethods) {
            this.setterMethods = setterMethods;
            return this;
        }

        public TemplateBuilder withSetterPrefix(String setterPrefix) {
            this.setterPrefix = setterPrefix;
            return this;
        }

        public TemplateBuilder withTargetPackage(String targetPackage) {
            this.targetPackage = targetPackage;
            return this;
        }

        public BuilderClassTemplate build() {
            BuilderClassTemplate builderClassTemplate = new BuilderClassTemplate();

            if (targetPojo == null) throw new IllegalStateException("Target pojo could not be null");
            builderClassTemplate.targetPojo = targetPojo;
            if (targetPackage == null) throw new IllegalStateException("Target package could not be null");
            builderClassTemplate.targetPackage = targetPackage;

            builderClassTemplate.builderClassName = ClassName.get(targetPackage, targetPojo.getSimpleName() + "Builder");

            if (setterPrefix == null) throw new IllegalStateException("Setter prefix could not be null");
            builderClassTemplate.setterPrefix = setterPrefix;
            if (setterMethods == null) throw new IllegalStateException("Setter methods could not be null");
            builderClassTemplate.setterMethods = setterMethods;

            return builderClassTemplate;
        }
    }
}