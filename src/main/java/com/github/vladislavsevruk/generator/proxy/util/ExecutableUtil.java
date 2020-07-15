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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Utility methods for getting characteristics of methods and constructors.
 */
public final class ExecutableUtil {

    private static final List<Method> OBJECT_METHODS = Arrays.asList(Object.class.getMethods());

    private ExecutableUtil() {
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
}
