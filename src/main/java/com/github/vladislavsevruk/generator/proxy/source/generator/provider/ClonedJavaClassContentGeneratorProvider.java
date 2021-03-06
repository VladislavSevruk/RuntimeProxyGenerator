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
package com.github.vladislavsevruk.generator.proxy.source.generator.provider;

import com.github.vladislavsevruk.generator.java.generator.ClassElementCollectionGenerator;
import com.github.vladislavsevruk.generator.java.generator.ClassElementGenerator;
import com.github.vladislavsevruk.generator.java.provider.JavaClassContentGeneratorProvider;
import com.github.vladislavsevruk.generator.java.type.SchemaObject;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * Clones generators from received provider to create local copy of provider so following modifications not affect
 * initial provider.
 *
 * @see JavaClassContentGeneratorProvider
 */
@Log4j2
public class ClonedJavaClassContentGeneratorProvider implements JavaClassContentGeneratorProvider {

    @Getter
    private ClassElementGenerator classDeclarationGenerator;
    @Getter
    private Collection<ClassElementGenerator> constructorGenerators;
    @Getter
    private Collection<ClassElementCollectionGenerator> fieldGenerators;
    @Getter
    private Collection<ClassElementCollectionGenerator> importGenerators;
    private Predicate<SchemaObject> matchesFunction;
    @Getter
    private Collection<ClassElementGenerator> methodGenerators;
    @Getter
    private ClassElementGenerator packageGenerator;

    public ClonedJavaClassContentGeneratorProvider(JavaClassContentGeneratorProvider provider) {
        log.debug("Cloning generators from '{}' provider.", provider.getClass().getName());
        this.classDeclarationGenerator = provider.getClassDeclarationGenerator();
        this.constructorGenerators = new ArrayList<>(provider.getConstructorGenerators());
        this.fieldGenerators = new ArrayList<>(provider.getFieldGenerators());
        this.importGenerators = new ArrayList<>(provider.getImportGenerators());
        this.methodGenerators = new ArrayList<>(provider.getMethodGenerators());
        this.packageGenerator = provider.getPackageGenerator();
        this.matchesFunction = provider::matches;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(SchemaObject schemaObject) {
        return matchesFunction.test(schemaObject);
    }
}
