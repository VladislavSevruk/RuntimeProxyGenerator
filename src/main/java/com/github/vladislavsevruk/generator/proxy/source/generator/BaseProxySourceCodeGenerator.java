/*
 * MIT License
 *
 * Copyright (c) 2020 Uladzislau Seuruk
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.vladislavsevruk.generator.proxy.source.generator;

import com.github.vladislavsevruk.generator.java.JavaClassContentGenerator;
import com.github.vladislavsevruk.generator.java.config.JavaClassGeneratorConfig;
import com.github.vladislavsevruk.generator.java.context.ClassGenerationContextManager;
import com.github.vladislavsevruk.generator.java.generator.ClassElementCollectionGenerator;
import com.github.vladislavsevruk.generator.java.generator.ClassElementGenerator;
import com.github.vladislavsevruk.generator.java.provider.JavaClassContentGeneratorProvider;
import com.github.vladislavsevruk.generator.proxy.source.generator.constructor.ProxyClassConstructorGenerator;
import com.github.vladislavsevruk.generator.proxy.source.generator.provider.ClonedJavaClassContentGeneratorProvider;
import com.github.vladislavsevruk.generator.proxy.source.schema.ProxyClassSchema;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Collections;

/**
 * Contains base logic for dynamic proxy source code generators.
 */
@Log4j2
public abstract class BaseProxySourceCodeGenerator implements ProxySourceCodeGenerator {

    /**
     * {@inheritDoc}
     */
    @Override
    public String generate(ProxyClassSchema proxyClassSchemaObject) {
        log.debug("Generating source code for '{}' class.", proxyClassSchemaObject.getName());
        JavaClassContentGeneratorProvider classContentGeneratorProvider = ClassGenerationContextManager.getContext()
                .getClassContentGeneratorPicker().pickClassContentGeneratorProvider(proxyClassSchemaObject);
        JavaClassContentGeneratorProvider localContentGeneratorProvider = getLocalContentGeneratorProvider(
                classContentGeneratorProvider, proxyClassSchemaObject.delegatedClass());
        return new JavaClassContentGenerator(localContentGeneratorProvider)
                .generate(setupJavaClassGeneratorConfig(), proxyClassSchemaObject);
    }

    protected Collection<ClassElementGenerator> getConstructorsDeclaration(Class<?> clazz) {
        return Collections.singletonList(new ProxyClassConstructorGenerator(clazz));
    }

    @SuppressWarnings("unused")
    protected Collection<ClassElementCollectionGenerator> getFieldsDeclaration(Class<?> clazz) {
        // no fields by default
        return Collections.emptyList();
    }

    @SuppressWarnings("unused")
    protected Collection<ClassElementCollectionGenerator> getImportsDeclaration(Class<?> clazz) {
        // no imports by default
        return Collections.emptyList();
    }

    protected JavaClassContentGeneratorProvider getLocalContentGeneratorProvider(
            JavaClassContentGeneratorProvider classContentGeneratorProvider, Class<?> clazz) {
        JavaClassContentGeneratorProvider localContentGeneratorProvider = new ClonedJavaClassContentGeneratorProvider(
                classContentGeneratorProvider);
        localContentGeneratorProvider.getImportGenerators().addAll(getImportsDeclaration(clazz));
        localContentGeneratorProvider.getFieldGenerators().addAll(getFieldsDeclaration(clazz));
        localContentGeneratorProvider.getConstructorGenerators().addAll(getConstructorsDeclaration(clazz));
        localContentGeneratorProvider.getMethodGenerators().addAll(getMethodsDeclaration(clazz));
        return localContentGeneratorProvider;
    }

    @SuppressWarnings("unused")
    protected Collection<ClassElementGenerator> getMethodsDeclaration(Class<?> clazz) {
        // no overridden methods by default
        return Collections.emptyList();
    }

    protected JavaClassGeneratorConfig setupJavaClassGeneratorConfig() {
        return JavaClassGeneratorConfig.builder().useLombokAnnotations(false).build();
    }
}
