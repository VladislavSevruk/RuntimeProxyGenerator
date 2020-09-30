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
package com.github.vladislavsevruk.generator.proxy.source.generator.constructor;

import com.github.vladislavsevruk.generator.java.config.JavaClassGeneratorConfig;
import com.github.vladislavsevruk.generator.java.generator.method.BaseMethodGenerator;
import com.github.vladislavsevruk.generator.java.type.SchemaObject;
import com.github.vladislavsevruk.generator.proxy.util.ClassMemberUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates proxy class constructors that simply delegate call to superclass.
 */
public class ProxyClassConstructorGenerator extends BaseMethodGenerator {

    private static final Logger logger = LogManager.getLogger(ProxyClassConstructorGenerator.class);

    private Class<?> delegatedClass;

    public ProxyClassConstructorGenerator(Class<?> delegatedClass) {
        this.delegatedClass = delegatedClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generate(JavaClassGeneratorConfig config, SchemaObject schemaObject) {
        logger.debug("Generating proxy constructors for {} class.", schemaObject.getName());
        List<Constructor<?>> nonPrivateConstructors = Arrays.stream(delegatedClass.getConstructors())
                .filter(ClassMemberUtil::isNonPrivate).collect(Collectors.toList());
        if (nonPrivateConstructors.isEmpty()) {
            logger.info("There is no any non-private constructor for {}.", delegatedClass.getName());
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        nonPrivateConstructors
                .forEach(constructor -> appendMatchingConstructor(stringBuilder, config, schemaObject, constructor));
        return stringBuilder.toString();
    }

    private void appendMatchingConstructor(StringBuilder stringBuilder, JavaClassGeneratorConfig config,
            SchemaObject schemaObject, Constructor<?> constructor) {
        String parameters = Arrays.stream(constructor.getParameters()).map(Parameter::toString)
                .collect(Collectors.joining(", "));
        String parameterNames = Arrays.stream(constructor.getParameters()).map(Parameter::getName)
                .collect(Collectors.joining(", "));
        String indent = config.getIndent().value();
        stringBuilder.append(indent).append("public ").append(schemaObject.getName()).append("(").append(parameters)
                .append(") {\n");
        doubleIndents(stringBuilder, config).append("super(").append(parameterNames).append(");\n");
        closeMethod(stringBuilder, config);
    }
}
