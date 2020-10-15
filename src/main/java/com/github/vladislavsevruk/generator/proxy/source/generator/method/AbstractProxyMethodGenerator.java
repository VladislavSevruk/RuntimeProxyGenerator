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
import com.github.vladislavsevruk.resolver.resolver.executable.BaseExecutableTypeResolver;
import com.github.vladislavsevruk.resolver.resolver.executable.ExecutableStringRepresentationResolver;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains base logic for generating proxy methods with ability to delegate call to initial class.
 */
@Log4j2
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractProxyMethodGenerator extends BaseMethodGenerator {

    private Class<?> delegatedClass;
    private BaseExecutableTypeResolver<String> executableResolver;

    protected AbstractProxyMethodGenerator(Class<?> delegatedClass) {
        this(delegatedClass, new ExecutableStringRepresentationResolver());
    }

    protected AbstractProxyMethodGenerator(Class<?> delegatedClass,
            BaseExecutableTypeResolver<String> executableResolver) {
        this.delegatedClass = delegatedClass;
        this.executableResolver = executableResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generate(JavaClassGeneratorConfig config, SchemaObject schemaObject) {
        log.debug("Generating proxy methods for {} class.", schemaObject.getName());
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
        String parameterNames = Arrays.stream(originalMethod.getParameters()).map(Parameter::getName)
                .collect(Collectors.joining(", "));
        String parameters = generateParameters(originalMethod, originalMethod.getParameters());
        String typeVariablesDeclaration = getTypeVariables(originalMethod);
        String throwsClause = generateThrowClause(originalMethod);
        String indent = config.getIndent().value();
        addOverrideAnnotation(stringBuilder, config);
        String returnTypeDeclaration = executableResolver.getReturnType(delegatedClass, originalMethod);
        stringBuilder.append(indent).append("public ").append(typeVariablesDeclaration).append(returnTypeDeclaration)
                .append(" ").append(originalMethod.getName()).append("(").append(parameters).append(") ")
                .append(throwsClause).append("{\n");
        String delegateCall = String.format("super.%s(%s)", originalMethod.getName(), parameterNames);
        doubleIndents(stringBuilder, config).append(getProxyMethodBodyContent(config, originalMethod, delegateCall))
                .append("\n");
        closeMethod(stringBuilder, config);
    }

    private String generateParameters(Method originalMethod, Parameter[] parameters) {
        List<String> parameterTypes = executableResolver.getParameterTypes(delegatedClass, originalMethod);
        List<String> stringRepresentations = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; ++i) {
            Parameter parameter = parameters[i];
            String parameterType = parameter.isVarArgs() ? parameterTypes.get(i).replaceFirst("\\[]$", "...")
                    : parameterTypes.get(i);
            String stringRepresentation = parameterType + " " + parameter.getName();
            stringRepresentations.add(stringRepresentation);
        }
        return String.join(", ", stringRepresentations);
    }

    private String generateThrowClause(Method originalMethod) {
        List<String> exceptionTypes = executableResolver.getExceptionTypes(delegatedClass, originalMethod);
        String exceptions = String.join(", ", exceptionTypes);
        return exceptions.isEmpty() ? exceptions : String.format("throws %s ", exceptions);
    }

    private String getTypeVariables(Method method) {
        String typeVariablesDeclaration = ClassMemberUtil.generateBoundedTypeVariablesDeclaration(method);
        return typeVariablesDeclaration.isEmpty() ? typeVariablesDeclaration : typeVariablesDeclaration + " ";
    }
}
