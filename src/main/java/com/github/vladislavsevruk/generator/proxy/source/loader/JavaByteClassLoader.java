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
package com.github.vladislavsevruk.generator.proxy.source.loader;

import com.github.vladislavsevruk.generator.proxy.source.file.JavaByteFileObject;

/**
 * Class loader that is able to load and define dynamically compiled classes on runtime.
 */
public class JavaByteClassLoader extends ClassLoader {

    private static final JavaByteClassLoader INSTANCE = new JavaByteClassLoader();

    private JavaByteClassLoader() {
    }

    /**
     * Returns instance of <code>JavaByteClassLoader</code>.
     */
    public static JavaByteClassLoader instance() {
        return INSTANCE;
    }

    /**
     * Defines and resolves dynamically compiled class on runtime so class will be available at class path of this class
     * loader for further interaction.
     *
     * @param name               <code>String</code> with expected binary name of the class.
     * @param javaByteFileObject <code>JavaByteFileObject</code> with compiled byte code.
     * @return defined <code>Class</code> from received byte code.
     */
    public Class<?> defineClass(String name, JavaByteFileObject javaByteFileObject) {
        return defineClass(name, javaByteFileObject.getBytes());
    }

    /**
     * Defines and resolves dynamically compiled class on runtime so class will be available at class path of this class
     * loader for further interaction.
     *
     * @param name  <code>String</code> with expected binary name of the class.
     * @param bytes <code>byte[]</code> with compiled byte code.
     * @return defined <code>Class</code> from received byte code.
     */
    public Class<?> defineClass(String name, byte[] bytes) {
        Class<?> definedClass = defineClass(name, bytes, 0, bytes.length);
        resolveClass(definedClass);
        return definedClass;
    }
}
