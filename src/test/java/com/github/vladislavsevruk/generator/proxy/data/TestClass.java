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
package com.github.vladislavsevruk.generator.proxy.data;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class TestClass<T, V extends Exception> {

    public TestClass() {
    }

    public TestClass(Boolean value) {
    }

    public TestClass(Number number) {
    }

    public static void staticMethod() {
    }

    public final void finalMethod() {
    }

    public void method() {
    }

    public <U> void methodWithClassTypeVariableAndMethodTypeVariableArgs(T one, U two) {
    }

    public void methodWithClassTypeVariableArg(T arg) {
    }

    public T methodWithClassTypeVariableReturnType() {
        return null;
    }

    public void methodWithClassTypeVariableThrowClause() throws V {
    }

    public <U> void methodWithMethodTypeVariableArg(U arg) {
    }

    public <U> U methodWithMethodTypeVariableReturnType() {
        return null;
    }

    public <U extends Exception> void methodWithMethodTypeVariableThrowClause() throws U {
    }

    public void methodWithOneArg(String one) {
    }

    public void methodWithParameterizedClassTypeVariableArg(List<T> arg) {
    }

    public List<T> methodWithParameterizedClassTypeVariableReturnType() {
        return null;
    }

    public <U> void methodWithParameterizedMethodTypeVariableArg(List<U> arg) {
    }

    public <U> List<U> methodWithParameterizedMethodTypeVariableReturnType() {
        return null;
    }

    public String methodWithReturnType() {
        return null;
    }

    public <T> void methodWithThatCoversClassTypeVariable(T arg) {
    }

    public void methodWithThrowClause() throws IOException {
        throw new IOException();
    }

    public void methodWithThrowClauseSeveralExceptions(boolean flag) throws IOException, ParseException {
        if (flag) {
            throw new ParseException("", 0);
        }
        throw new IOException();
    }

    public void methodWithTwoArgs(String one, String two) {
    }

    public void methodWithVararg(String... args) {
    }
}
