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

import com.github.vladislavsevruk.generator.proxy.data.FinalTestClass;
import com.github.vladislavsevruk.generator.proxy.data.TestClass;
import com.github.vladislavsevruk.generator.proxy.data.TestClassWithPrivateConstructor;
import com.github.vladislavsevruk.generator.proxy.source.generator.SimpleProxySourceTestGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

class ProxyFactoryTest {

    @Test
    void createProxyConstructorWithMatchingParameterTest() {
        ProxyFactory<TestClass> proxyFactory = newProxyFactory(TestClass.class);
        TestClass testClass = proxyFactory.newInstance(1);
        Assertions.assertNotEquals(TestClass.class, testClass.getClass());
    }

    @Test
    void createProxyConstructorWithNonMatchingParameterTest() {
        ProxyFactory<TestClass> proxyFactory = newProxyFactory(TestClass.class);
        Assertions.assertThrows(IllegalArgumentException.class, () -> proxyFactory.newInstance("test"));
    }

    @Test
    void createProxyConstructorWithNonMatchingParametersNumberTest() {
        ProxyFactory<TestClass> proxyFactory = newProxyFactory(TestClass.class);
        Assertions.assertThrows(IllegalArgumentException.class, () -> proxyFactory.newInstance(1, 1));
    }

    @Test
    void createProxyConstructorWithNullParameterTest() {
        ProxyFactory<TestClass> proxyFactory = newProxyFactory(TestClass.class);
        TestClass testClass = proxyFactory.newInstance((Object) null);
        Assertions.assertNotEquals(TestClass.class, testClass.getClass());
    }

    @Test
    void createProxyConstructorWithParameterTest() {
        ProxyFactory<TestClass> proxyFactory = newProxyFactory(TestClass.class);
        TestClass testClass = proxyFactory.newInstance(Boolean.TRUE);
        Assertions.assertNotEquals(TestClass.class, testClass.getClass());
    }

    @Test
    void createProxyConstructorWithPrimitiveParameterTest() {
        ProxyFactory<TestClass> proxyFactory = newProxyFactory(TestClass.class);
        TestClass testClass = proxyFactory.newInstance(true);
        Assertions.assertNotEquals(TestClass.class, testClass.getClass());
    }

    @Test
    void createProxyConstructorWithPrivateConstructorTest() {
        ProxyFactory<TestClassWithPrivateConstructor> proxyFactory = newProxyFactory(
                TestClassWithPrivateConstructor.class);
        Assertions.assertThrows(IllegalArgumentException.class, proxyFactory::newInstance);
    }

    @Test
    void createProxyForFinalClassTest() {
        ProxyFactory<FinalTestClass> proxyFactory = newProxyFactory(FinalTestClass.class);
        FinalTestClass testClass = proxyFactory.newInstance();
        Assertions.assertEquals(FinalTestClass.class, testClass.getClass());
    }

    @Test
    void createProxyTest() {
        ProxyFactory<TestClass> proxyFactory = newProxyFactory(TestClass.class);
        TestClass testClass = proxyFactory.newInstance();
        Assertions.assertNotEquals(TestClass.class, testClass.getClass());
    }

    @Test
    void pickConstructorsTest() {
        ProxyFactory<TestClass> proxyFactory = newProxyFactory(TestClass.class);
        Constructor<? extends TestClass> constructor1 = proxyFactory.getConstructor(Number.class);
        Constructor<? extends TestClass> constructor2 = proxyFactory.getConstructor(Integer.class);
        Constructor<? extends TestClass> constructor3 = proxyFactory.getConstructor(Boolean.class);
        Constructor<? extends TestClass> constructor4 = proxyFactory.getConstructor();
        Assertions.assertEquals(constructor1, constructor2);
        Assertions.assertNotEquals(constructor2, constructor3);
        Assertions.assertNotEquals(constructor2, constructor4);
        Assertions.assertNotEquals(constructor3, constructor4);
    }

    private <T> ProxyFactory<T> newProxyFactory(Class<T> clazz) {
        return new ProxyFactory<>(clazz, new SimpleProxySourceTestGenerator());
    }
}
