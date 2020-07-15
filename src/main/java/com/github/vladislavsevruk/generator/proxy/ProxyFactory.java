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
package com.github.vladislavsevruk.generator.proxy;

import com.github.vladislavsevruk.generator.proxy.source.compiler.JavaSourceCompiler;
import com.github.vladislavsevruk.generator.proxy.source.generator.ProxySourceCodeGenerator;
import com.github.vladislavsevruk.generator.proxy.util.ExecutableUtil;
import com.github.vladislavsevruk.resolver.resolver.ExecutableTypeResolver;
import com.github.vladislavsevruk.resolver.resolver.ExecutableTypeResolverImpl;
import com.github.vladislavsevruk.resolver.type.TypeMeta;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Factory for dynamic generation of proxies with custom behavior.
 *
 * @param <T> type of target class.
 */
public final class ProxyFactory<T> {

    private static final Map<String, Class<?>> RESOLVED_CLASSES = new ConcurrentHashMap<>();
    private static final Logger logger = LogManager.getLogger(ProxyFactory.class);

    private final Class<T> clazz;
    private final ExecutableTypeResolver executableTypeResolver = new ExecutableTypeResolverImpl();
    private final ProxySourceCodeGenerator proxyContentGenerator;

    public ProxyFactory(Class<T> clazz, ProxySourceCodeGenerator proxyContentGenerator) {
        this.clazz = clazz;
        this.proxyContentGenerator = proxyContentGenerator;
    }

    /**
     * Creates new instance of proxy or initial class if proxy generation failed.
     *
     * @param args arguments to be passed as arguments to the constructor call.
     * @return instance of generated proxy or initial class if proxy generation failed.
     * @throws IllegalArgumentException if received arguments doesn't match any public constructor of initial class.
     */
    public T newInstance(Object... args) {
        Class<?>[] receivedParameterTypes = Arrays.stream(args).map(arg -> arg == null ? null : arg.getClass())
                .toArray(Class<?>[]::new);
        return createInstance(getProxyClass(), receivedParameterTypes, args);
    }

    private static boolean isAlreadyCompiled(String name) {
        return RESOLVED_CLASSES.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    private T createInstance(Class<?> clazzToCreate, Class<?>[] receivedParameterTypes, Object[] args) {
        try {
            return (T) getConstructor(clazzToCreate, receivedParameterTypes).newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            logger.warn(String.format("Failed to create '%s' instance by constructor with %s args.",
                    clazzToCreate.getName(), Arrays.asList(receivedParameterTypes)), ex);
            return null;
        }
    }

    private Constructor<?> getConstructor(Class<?> clazzToCreate, Class<?>[] receivedParameterTypes) {
        logger.debug("Picking '{}' constructor for {} parameters.", clazzToCreate.getName(),
                Arrays.asList(receivedParameterTypes));
        Constructor<?> firstFoundCandidate = null;
        for (Constructor<?> constructor : clazzToCreate.getDeclaredConstructors()) {
            if (!ExecutableUtil.isNonPrivate(constructor)) {
                continue;
            }
            List<TypeMeta<?>> typeMetas = executableTypeResolver.getParameterTypes(clazzToCreate, constructor);
            if (isExactMatchingParameters(typeMetas, receivedParameterTypes)) {
                logExactMatchingConstructor(typeMetas);
                return constructor;
            }
            if (firstFoundCandidate == null && isMatchingParameters(typeMetas, receivedParameterTypes)) {
                firstFoundCandidate = constructor;
            }
        }
        if (firstFoundCandidate != null) {
            logPickedMatchingConstructor(firstFoundCandidate);
            return firstFoundCandidate;
        }
        throw new IllegalArgumentException(String.format("There is no public constructor for args %s at %s class.",
                Arrays.asList(receivedParameterTypes), clazz.getName()));
    }

    private Class<?> getProxyClass() {
        String proxyClassName = String.format("%s.%sProxy", clazz.getPackage().getName(), clazz.getSimpleName());
        if (!isAlreadyCompiled(proxyClassName)) {
            String proxyClassContent = proxyContentGenerator.generate(clazz);
            Class<? extends T> compiledClass = JavaSourceCompiler.compile(proxyClassName, proxyClassContent);
            Class<? extends T> resultedClass = compiledClass != null ? compiledClass : clazz;
            RESOLVED_CLASSES.put(proxyClassName, resultedClass);
        }
        return RESOLVED_CLASSES.get(proxyClassName);
    }

    private boolean isExactMatchingParameters(List<TypeMeta<?>> typeMetas, Class<?>[] receivedParameterTypes) {
        if (typeMetas.size() != receivedParameterTypes.length) {
            return false;
        }
        for (int i = 0; i < typeMetas.size(); ++i) {
            if (!typeMetas.get(i).getType().equals(receivedParameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isMatchingParameters(List<TypeMeta<?>> typeMetas, Class<?>[] receivedParameterTypes) {
        if (typeMetas.size() != receivedParameterTypes.length) {
            return false;
        }
        for (int i = 0; i < typeMetas.size(); ++i) {
            Class<?> parameterType = receivedParameterTypes[i];
            if (parameterType != null && !typeMetas.get(i).getType().isAssignableFrom(parameterType)) {
                return false;
            }
        }
        return true;
    }

    private void logExactMatchingConstructor(List<TypeMeta<?>> typeMetas) {
        logger.debug(() -> {
            String parameterTypes = typeMetas.stream().map(TypeMeta::getType).map(Class::getName)
                    .collect(Collectors.joining(", "));
            return String.format("Found constructor with [%s] parameters.", parameterTypes);
        });
    }

    private void logPickedMatchingConstructor(Constructor<?> firstFoundCandidate) {
        logger.debug(() -> {
            String parameterTypes = Arrays.stream(firstFoundCandidate.getAnnotatedParameterTypes())
                    .map(AnnotatedType::getType).map(Type::getTypeName).collect(Collectors.joining(", "));
            return String.format("Picked constructor with [%s] parameters.", parameterTypes);
        });
    }
}
