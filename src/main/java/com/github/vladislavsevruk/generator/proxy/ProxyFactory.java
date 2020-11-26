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
import com.github.vladislavsevruk.generator.proxy.source.loader.JavaByteClassLoader;
import com.github.vladislavsevruk.resolver.resolver.executable.ExecutableTypeMetaResolver;
import com.github.vladislavsevruk.resolver.resolver.executable.ExecutableTypeResolver;
import com.github.vladislavsevruk.resolver.type.TypeMeta;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Factory for dynamic generation of proxies with custom behavior.
 *
 * @param <T> type of target class.
 */
@Log4j2
public final class ProxyFactory<T> {

    private static final Map<String, Class<?>> RESOLVED_CLASSES = new ConcurrentHashMap<>();

    private final Class<T> clazz;
    private final ExecutableTypeResolver<TypeMeta<?>> executableTypeResolver = new ExecutableTypeMetaResolver();
    private final String proxyClassPrefix;
    private final ProxySourceCodeGenerator proxyContentGenerator;

    public ProxyFactory(Class<T> clazz, ProxySourceCodeGenerator proxyContentGenerator) {
        this(clazz, proxyContentGenerator, "");
    }

    public ProxyFactory(Class<T> clazz, ProxySourceCodeGenerator proxyContentGenerator, String proxyClassPrefix) {
        this.clazz = clazz;
        this.proxyContentGenerator = proxyContentGenerator;
        this.proxyClassPrefix = proxyClassPrefix;
    }

    /**
     * Returns constructor of proxy or initial class if proxy generation failed that matches received parameter types.
     *
     * @param args argument types to pick matching constructor for.
     * @return constructor of generated proxy or initial class if proxy generation failed.
     * @throws IllegalArgumentException if received argument types doesn't match any public constructor of initial
     *                                  class.
     */
    @SuppressWarnings("java:S1452")
    public Constructor<? extends T> getConstructor(Class<?>... args) {
        return getConstructor(getProxyClass(), args);
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
    private Class<? extends T> compileClass(String proxyClassName, String proxyClassContent) {
        return (Class<? extends T>) Optional.ofNullable(JavaSourceCompiler.compile(proxyClassName, proxyClassContent))
                .map(compiledByteFileObject -> JavaByteClassLoader.instance()
                        .defineClass(proxyClassName, compiledByteFileObject)).orElse(null);
    }

    private T createInstance(Class<? extends T> clazzToCreate, Class<?>[] receivedParameterTypes, Object[] args) {
        try {
            return getConstructor(clazzToCreate, receivedParameterTypes).newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            log.warn(String.format("Failed to create '%s' instance by constructor with %s args.",
                    clazzToCreate.getName(), Arrays.asList(receivedParameterTypes)), ex);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Constructor<? extends T> getConstructor(Class<? extends T> clazzToCreate,
            Class<?>[] receivedParameterTypes) {
        log.debug("Picking '{}' constructor for {} parameters.", clazzToCreate.getName(),
                Arrays.asList(receivedParameterTypes));
        Constructor<?> firstFoundCandidate = null;
        for (Constructor<?> constructor : clazzToCreate.getConstructors()) {
            List<TypeMeta<?>> typeMetas = executableTypeResolver.getParameterTypes(clazzToCreate, constructor);
            if (isExactMatchingParameters(typeMetas, receivedParameterTypes)) {
                logExactMatchingConstructor(typeMetas);
                return (Constructor<? extends T>) constructor;
            }
            if (firstFoundCandidate == null && isMatchingParameters(typeMetas, receivedParameterTypes)) {
                firstFoundCandidate = constructor;
            }
        }
        if (firstFoundCandidate != null) {
            logPickedMatchingConstructor(firstFoundCandidate);
            return (Constructor<? extends T>) firstFoundCandidate;
        }
        throw new IllegalArgumentException(String.format("There is no public constructor for args %s at %s class.",
                Arrays.asList(receivedParameterTypes), clazz.getName()));
    }

    @SuppressWarnings("unchecked")
    private Class<? extends T> getProxyClass() {
        if (Modifier.isFinal(clazz.getModifiers())) {
            log.warn("'{}' class is final.", clazz.getName());
            return clazz;
        }
        String proxyClassName = String
                .format("%s.%s%sProxy", clazz.getPackage().getName(), proxyClassPrefix, clazz.getSimpleName());
        if (!isAlreadyCompiled(proxyClassName)) {
            String proxyClassContent = proxyContentGenerator.generate(clazz);
            Class<? extends T> compiledClass = compileClass(proxyClassName, proxyClassContent);
            Class<? extends T> resultedClass = compiledClass != null ? compiledClass : clazz;
            RESOLVED_CLASSES.put(proxyClassName, resultedClass);
        }
        return (Class<? extends T>) RESOLVED_CLASSES.get(proxyClassName);
    }

    private boolean isAllMatchCondition(List<TypeMeta<?>> typeMetas, Class<?>[] receivedParameterTypes,
            BiPredicate<TypeMeta<?>, Class<?>> condition) {
        if (typeMetas.size() != receivedParameterTypes.length) {
            return false;
        }
        for (int i = 0; i < typeMetas.size(); ++i) {
            if (condition.test(typeMetas.get(i), receivedParameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isExactMatchingParameters(List<TypeMeta<?>> typeMetas, Class<?>[] receivedParameterTypes) {
        return isAllMatchCondition(typeMetas, receivedParameterTypes,
                (typeMeta, parameterType) -> !typeMeta.getType().equals(parameterType));
    }

    private boolean isMatchingParameters(List<TypeMeta<?>> typeMetas, Class<?>[] receivedParameterTypes) {
        return isAllMatchCondition(typeMetas, receivedParameterTypes,
                (typeMeta, parameterType) -> parameterType != null && !typeMeta.getType()
                        .isAssignableFrom(parameterType));
    }

    private void logExactMatchingConstructor(List<TypeMeta<?>> typeMetas) {
        log.debug(() -> {
            String parameterTypes = typeMetas.stream().map(TypeMeta::getType).map(Class::getName)
                    .collect(Collectors.joining(", "));
            return String.format("Found constructor with [%s] parameters.", parameterTypes);
        });
    }

    private void logPickedMatchingConstructor(Constructor<?> firstFoundCandidate) {
        log.debug(() -> {
            String parameterTypes = Arrays.stream(firstFoundCandidate.getAnnotatedParameterTypes())
                    .map(AnnotatedType::getType).map(Type::getTypeName).collect(Collectors.joining(", "));
            return String.format("Picked constructor with [%s] parameters.", parameterTypes);
        });
    }
}
