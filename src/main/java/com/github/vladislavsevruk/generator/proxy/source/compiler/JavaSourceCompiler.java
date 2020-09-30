
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
package com.github.vladislavsevruk.generator.proxy.source.compiler;

import com.github.vladislavsevruk.generator.proxy.source.file.JavaByteFileManager;
import com.github.vladislavsevruk.generator.proxy.source.file.JavaByteFileObject;
import com.github.vladislavsevruk.generator.proxy.source.file.JavaSourceFileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

/**
 * Compiles source code and defines compiled classes on runtime for further interaction.
 */
public final class JavaSourceCompiler {

    private static final Logger logger = LogManager.getLogger(JavaSourceCompiler.class);

    private JavaSourceCompiler() {
    }

    /**
     * Compiles received class source code and defines compiled classes on runtime for further interaction.
     *
     * @param name    <code>String</code> with expected binary name of the class.
     * @param content <code>String</code> with class source code.
     * @return <code>JavaByteFileObject</code> with compiled byte code.
     */
    public static JavaByteFileObject compile(String name, String content) {
        logger.debug("Compiling '{}' class.", name);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            JavaFileObject javaFileObject = new JavaSourceFileObject(name, content);
            JavaByteFileObject javaByteFileObject = new JavaByteFileObject(name);
            JavaByteFileManager javaByteFileManager = new JavaByteFileManager(fileManager, javaByteFileObject);
            CompilationTask compilationTask = compiler.getTask(null, javaByteFileManager, diagnostics, null, null,
                    Collections.singletonList(javaFileObject));
            if (Boolean.TRUE.equals(compilationTask.call())) {
                logger.debug("Successfully compiled '{}' class.", name);
                return javaByteFileObject;
            } else {
                logger.debug("Failed to compile '{}' class.", name);
                logCompileErrors(diagnostics);
            }
        } catch (IOException ioEx) {
            logger.warn(ioEx);
        }
        return null;
    }

    private static void logCompileErrors(DiagnosticCollector<JavaFileObject> diagnostics) {
        diagnostics.getDiagnostics().forEach(diagnostic -> logger
                .warn("{} at {} ({}, {}): {}.", diagnostic.getKind(), diagnostic.getSource().getName(),
                        diagnostic.getLineNumber(), diagnostic.getColumnNumber(), diagnostic.getMessage(Locale.US)));
    }
}
