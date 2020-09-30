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
package com.github.vladislavsevruk.generator.proxy.source.schema;

import com.github.vladislavsevruk.generator.java.type.SchemaEntity;
import com.github.vladislavsevruk.generator.java.type.SchemaField;
import com.github.vladislavsevruk.generator.java.type.SchemaObject;
import com.github.vladislavsevruk.generator.proxy.util.ClassMemberUtil;

import java.util.Collections;
import java.util.List;

/**
 * Schema object for generating proxy class.
 *
 * @see SchemaObject
 * @see DelegatedClassSchema
 */
public class ProxyClassSchema extends DelegatedClassSchema implements SchemaObject {

    private final DelegatedClassSchema delegatedClassSchema;
    private final String proxyClassName;

    public ProxyClassSchema(Class<?> delegatedClass) {
        super(delegatedClass);
        delegatedClassSchema = new DelegatedClassSchema(delegatedClass);
        proxyClassName = getProxyClassName(delegatedClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SchemaField> getFields() {
        // no fields by default
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SchemaEntity> getInterfaces() {
        // no interfaces by default
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return proxyClassName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParameterizedDeclaration() {
        return getName() + ClassMemberUtil.generateTypeVariablesDeclaration(delegatedClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SchemaEntity getSuperclass() {
        return delegatedClassSchema;
    }

    protected String getProxyClassName(Class<?> clazz) {
        return clazz.getSimpleName() + "Proxy";
    }
}
