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
package com.github.vladislavsevruk.generator.proxy.util;

import java.lang.reflect.Executable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility methods for getting characteristics of class members.
 */
public final class ClassMemberUtil {

    private static final List<Method> OBJECT_METHODS = Arrays.asList(Object.class.getMethods());

    private ClassMemberUtil() {
    }

    /**
     * Generates string with type variables declaration for received class member.
     *
     * @param genericDeclaration <code>GenericDeclaration</code> with type variables.
     * @return <code>String</code> with type variables declaration.
     */
    public static String generateBoundedTypeVariablesDeclaration(GenericDeclaration genericDeclaration) {
        String typeVariablesDeclaration = Arrays.stream(genericDeclaration.getTypeParameters())
                .map(ClassMemberUtil::generateTypeVariableDeclaration).collect(Collectors.joining(", "));
        return typeVariablesDeclaration.isEmpty() ? typeVariablesDeclaration
                : String.format("<%s>", typeVariablesDeclaration);
    }

    /**
     * Generates string with superclass type variables declaration for received class member.
     *
     * @param genericDeclaration <code>GenericDeclaration</code> with type variables.
     * @return <code>String</code> with type variables declaration.
     */
    public static String generateUnboundedTypeVariablesDeclaration(Class<?> genericDeclaration) {
        String typeVariablesDeclaration = Arrays.stream(genericDeclaration.getTypeParameters())
                .map(TypeVariable::toString).collect(Collectors.joining(", "));
        return typeVariablesDeclaration.isEmpty() ? typeVariablesDeclaration
                : String.format("<%s>", typeVariablesDeclaration);
    }

    /**
     * Checks if received executable has no <code>final</code> modifier.
     *
     * @param executable <code>Executable</code> to check.
     * @return <code>false</code> if received executable has <code>final</code> modifier, <code>true</code> otherwise.
     */
    public static boolean isNonFinal(Executable executable) {
        return !Modifier.isFinal(executable.getModifiers());
    }

    /**
     * Checks if received method isn't one of <code>Object</code> class methods.
     *
     * @param method <code>Method</code> to check.
     * @return <code>false</code> if received method is one of <code>Object</code> class methods, <code>true</code>
     * otherwise.
     */
    public static boolean isNonObjectMethod(Method method) {
        return !OBJECT_METHODS.contains(method);
    }

    /**
     * Checks if received executable has no <code>private</code> modifier.
     *
     * @param executable <code>Executable</code> to check.
     * @return <code>false</code> if received executable has <code>private</code> modifier, <code>true</code> otherwise.
     */
    public static boolean isNonPrivate(Executable executable) {
        return !Modifier.isPrivate(executable.getModifiers());
    }

    /**
     * Checks if received executable has no <code>static</code> modifier.
     *
     * @param executable <code>Executable</code> to check.
     * @return <code>false</code> if received executable has <code>static</code> modifier, <code>true</code> otherwise.
     */
    public static boolean isNonStatic(Executable executable) {
        return !Modifier.isStatic(executable.getModifiers());
    }

    private static String generateTypeVariableDeclaration(TypeVariable<? extends GenericDeclaration> typeVariable) {
        Type bound = typeVariable.getBounds()[0];
        if (Class.class.isAssignableFrom(bound.getClass()) && Object.class.equals(bound)) {
            return typeVariable.getName();
        }
        return String.format("%s extends %s", typeVariable.getName(), bound.getTypeName());
    }
}
