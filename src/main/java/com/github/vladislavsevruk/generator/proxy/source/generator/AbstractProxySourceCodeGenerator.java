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

import com.github.vladislavsevruk.generator.proxy.util.ExecutableUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains base logic for dynamic proxy source code generators.
 */
public abstract class AbstractProxySourceCodeGenerator implements ProxySourceCodeGenerator {

    private static final Logger logger = LogManager.getLogger(AbstractProxySourceCodeGenerator.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String generate(Class<?> clazz) {
        String proxyClassName = getProxyClassName(clazz);
        logger.debug("Generating source code for '{}' class.", proxyClassName);
        StringBuilder stringBuilder = new StringBuilder("package ").append(clazz.getPackage().getName()).append(";\n");
        stringBuilder.append(getImportsDeclaration());
        String typeVariablesDeclaration = generateTypeVariablesDeclaration(clazz);
        stringBuilder.append("public class ").append(proxyClassName).append(typeVariablesDeclaration)
                .append(" extends ").append(clazz.getSimpleName()).append(typeVariablesDeclaration).append(" {\n");
        stringBuilder.append(getConstructorsDeclaration(clazz));
        Arrays.stream(clazz.getMethods()).filter(ExecutableUtil::isNonObjectMethod).filter(ExecutableUtil::isNonStatic)
                .filter(ExecutableUtil::isNonFinal).map(this::generateMethod).forEach(stringBuilder::append);
        return stringBuilder.append("}").toString();
    }

    protected String generateTypeVariablesDeclaration(GenericDeclaration genericDeclaration) {
        String typeVariablesDeclaration = Arrays.stream(genericDeclaration.getTypeParameters())
                .map(this::generateTypeVariableDeclaration).collect(Collectors.joining(", "));
        return typeVariablesDeclaration.isEmpty() ? typeVariablesDeclaration
                : String.format("<%s>", typeVariablesDeclaration);
    }

    @SuppressWarnings("java:S3400")
    protected String getImportsDeclaration() {
        // no imports by default
        return "";
    }

    protected String getProxyClassName(Class<?> clazz) {
        return clazz.getSimpleName() + "Proxy";
    }

    protected abstract String getProxyMethodBodyContent(Method method, String delegateCall);

    private String generateMatchingConstructor(Constructor<?> constructor) {
        String parameters = Arrays.stream(constructor.getParameters()).map(Parameter::toString)
                .collect(Collectors.joining(", "));
        String parameterNames = Arrays.stream(constructor.getParameters()).map(Parameter::getName)
                .collect(Collectors.joining(", "));
        StringBuilder stringBuilder = new StringBuilder("public ")
                .append(getProxyClassName(constructor.getDeclaringClass())).append("(").append(parameters)
                .append(") { ");
        return stringBuilder.append("super(").append(parameterNames).append("); }\n").toString();
    }

    private String generateMethod(Method method) {
        String parameters = Arrays.stream(method.getParameters()).map(Parameter::toString)
                .collect(Collectors.joining(", "));
        String parameterNames = Arrays.stream(method.getParameters()).map(Parameter::getName)
                .collect(Collectors.joining(", "));
        String typeVariablesDeclaration = generateTypeVariablesDeclaration(method);
        String throwsClause = Arrays.stream(method.getAnnotatedExceptionTypes()).map(AnnotatedType::getType)
                .map(Type::getTypeName).collect(Collectors.joining(", "));
        if (!throwsClause.isEmpty()) {
            throwsClause = String.format("throws %s ", throwsClause);
        }
        StringBuilder stringBuilder = new StringBuilder("@Override public ").append(typeVariablesDeclaration)
                .append(method.getAnnotatedReturnType().getType().getTypeName()).append(" ").append(method.getName())
                .append("(").append(parameters).append(") ").append(throwsClause).append("{ ");
        String delegateCall = String.format("super.%s(%s)", method.getName(), parameterNames);
        return stringBuilder.append(getProxyMethodBodyContent(method, delegateCall)).append(" }\n").toString();
    }

    private String generateTypeVariableDeclaration(TypeVariable<? extends GenericDeclaration> typeVariable) {
        Type bound = typeVariable.getBounds()[0];
        if (Class.class.isAssignableFrom(bound.getClass()) && Object.class.equals(bound)) {
            return typeVariable.getName();
        }
        return String.format("%s extends %s", typeVariable.getName(), bound.getTypeName());
    }

    private String getConstructorsDeclaration(Class<?> clazz) {
        List<Constructor<?>> nonPrivateConstructors = Arrays.stream(clazz.getConstructors())
                .filter(ExecutableUtil::isNonPrivate).collect(Collectors.toList());
        if (nonPrivateConstructors.isEmpty()) {
            logger.info("There is no any non-private constructor for {}.", clazz.getName());
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        nonPrivateConstructors.forEach(constructor -> stringBuilder.append(generateMatchingConstructor(constructor)));
        return stringBuilder.toString();
    }
}
