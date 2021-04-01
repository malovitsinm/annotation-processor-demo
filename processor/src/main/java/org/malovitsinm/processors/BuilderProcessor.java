package org.malovitsinm.processors;


import com.google.auto.service.AutoService;
import org.malovitsinm.annotations.GeneratedBuilder;
import org.malovitsinm.processors.BuilderClassTemplate.TemplateBuilder;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("org.malovitsinm.annotations.GeneratedBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) return false;
        Map<TypeElement, List<ExecutableElement>> collect = roundEnv.getElementsAnnotatedWith(GeneratedBuilder.class)
                .stream()
                .collect(Collectors.toMap(
                        element -> (TypeElement) element,
                        element -> ((TypeElement) element).getEnclosedElements().stream()
                                .filter(enclosed -> enclosed.getKind().equals(ElementKind.METHOD))
                                .map(enclosed -> (ExecutableElement) enclosed)
                                .filter(enclosed -> enclosed.getSimpleName().toString()
                                        .startsWith((element).getAnnotation(GeneratedBuilder.class).setterPrefix())
                                )
                                .collect(Collectors.toList())
                ));

        for (Map.Entry<TypeElement, List<ExecutableElement>> entry : collect.entrySet()) {
            TypeElement typeElement = entry.getKey();
            List<ExecutableElement> methodList = entry.getValue();
            try {
                TemplateBuilder
                        .aBuilderClassTemplate()
                        .withTargetPackage(elementUtils.getPackageOf(typeElement).getQualifiedName().toString())
                        .withTargetPojo(typeElement)
                        .withSetterMethods(methodList)
                        .withSetterPrefix(typeElement.getAnnotation(GeneratedBuilder.class).setterPrefix())
                        .build()
                        .writeToFiler(filer);
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }

        }

        return true;
    }
}