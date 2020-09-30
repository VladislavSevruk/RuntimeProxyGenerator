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
package com.github.vladislavsevruk.generator.proxy.source.generator.method;

import com.github.vladislavsevruk.generator.java.config.JavaClassGeneratorConfig;
import com.github.vladislavsevruk.generator.java.generator.method.BaseMethodGenerator;
import com.github.vladislavsevruk.generator.java.type.SchemaObject;
import com.github.vladislavsevruk.generator.proxy.util.ClassMemberUtil;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Contains base logic for generating proxy methods with ability to delegate call to initial class.
 */
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractProxyMethodGenerator extends BaseMethodGenerator {

    private static final Logger logger = LogManager.getLogger(AbstractProxyMethodGenerator.class);

    private Class<?> delegatedClass;

    public AbstractProxyMethodGenerator(Class<?> delegatedClass) {
        this.delegatedClass = delegatedClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generate(JavaClassGeneratorConfig config, SchemaObject schemaObject) {
        logger.debug("Generating proxy methods for {} class.", schemaObject.getName());
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(delegatedClass.getMethods()).filter(ClassMemberUtil::isNonObjectMethod)
                .filter(ClassMemberUtil::isNonStatic).filter(ClassMemberUtil::isNonFinal)
                .forEach(method -> appendMethod(config, stringBuilder, method));
        return stringBuilder.toString();
    }

    protected abstract String getProxyMethodBodyContent(JavaClassGeneratorConfig config, Method originalMethod,
            String delegateCall);

    protected String getReturnKeyWordIfRequired(Method originalMethod) {
        boolean hasReturnType = void.class.equals(originalMethod.getAnnotatedReturnType().getType());
        return hasReturnType ? "" : "return ";
    }

    private void appendMethod(JavaClassGeneratorConfig config, StringBuilder stringBuilder, Method originalMethod) {
        String parameters = Arrays.stream(originalMethod.getParameters()).map(Parameter::toString)
                .collect(Collectors.joining(", "));
        String parameterNames = Arrays.stream(originalMethod.getParameters()).map(Parameter::getName)
                .collect(Collectors.joining(", "));
        String typeVariablesDeclaration = getTypeVariables(originalMethod);
        String throwsClause = Arrays.stream(originalMethod.getAnnotatedExceptionTypes()).map(AnnotatedType::getType)
                .map(Type::getTypeName).collect(Collectors.joining(", "));
        if (!throwsClause.isEmpty()) {
            throwsClause = String.format("throws %s ", throwsClause);
        }
        String indent = config.getIndent().value();
        addOverrideAnnotation(stringBuilder, config);
        stringBuilder.append(indent).append("public ").append(typeVariablesDeclaration)
                .append(originalMethod.getAnnotatedReturnType().getType().getTypeName()).append(" ")
                .append(originalMethod.getName()).append("(").append(parameters).append(") ").append(throwsClause)
                .append("{\n");
        String delegateCall = String.format("super.%s(%s)", originalMethod.getName(), parameterNames);
        doubleIndents(stringBuilder, config).append(getProxyMethodBodyContent(config, originalMethod, delegateCall))
                .append("\n");
        closeMethod(stringBuilder, config);
    }

    private String getTypeVariables(Method method) {
        String typeVariablesDeclaration = ClassMemberUtil.generateTypeVariablesDeclaration(method);
        return typeVariablesDeclaration.isEmpty() ? typeVariablesDeclaration : typeVariablesDeclaration + " ";
    }
}
