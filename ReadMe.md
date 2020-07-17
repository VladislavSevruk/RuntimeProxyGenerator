[![Build Status](https://travis-ci.org/VladislavSevruk/RuntimeProxyGenerator.svg?branch=develop)](https://travis-ci.com/VladislavSevruk/RuntimeProxyGenerator)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=VladislavSevruk_RuntimeProxyGenerator&metric=alert_status)](https://sonarcloud.io/dashboard?id=VladislavSevruk_RuntimeProxyGenerator)
[![Code Coverage](https://sonarcloud.io/api/project_badges/measure?project=VladislavSevruk_RuntimeProxyGenerator&metric=coverage)](https://sonarcloud.io/component_measures?id=VladislavSevruk_RuntimeProxyGenerator&metric=coverage)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.vladislavsevruk/runtime-proxy-generator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.vladislavsevruk/runtime-proxy-generator)

# Runtime Proxy Generator
This utility library helps to generate runtime proxies for non-final classes to inject additional actions or override 
method logic using inheritance mechanism.

## Table of contents
* [Getting started](#getting-started)
  * [Maven](#maven)
  * [Gradle](#gradle)
* [Usage](#usage)
  * [Implement ProxySourceCodeGenerator](#implement-proxysourcecodegenerator)
  * [Generate proxy class instance](#generate-proxy-class-instance)
* [License](#license)

## Getting started
To add library to your project perform next steps:

### Maven
Add the following dependency to your pom.xml:
```xml
<dependency>
      <groupId>com.github.vladislavsevruk</groupId>
      <artifactId>runtime-proxy-generator</artifactId>
      <version>1.0.0</version>
</dependency>
```
### Gradle
Add the following dependency to your build.gradle:
```groovy
implementation 'com.github.vladislavsevruk:runtime-proxy-generator:1.0.0'
```

## Usage
### Implement ProxySourceCodeGenerator
First of all you need to implement 
[ProxySourceCodeGenerator](/src/main/java/com/github/vladislavsevruk/generator/proxy/source/generator/ProxySourceCodeGenerator.java) 
interface to provide source code of proxy class to generate. Library has 
[BaseProxySourceCodeGenerator](/src/main/java/com/github/vladislavsevruk/generator/proxy/source/generator/BaseProxySourceCodeGenerator.java) 
as base implementation with common logic of this interface (like generating constructors that match target type) so you 
can extend this class and override necessary methods for generating certain class members:
```kotlin
public class SimpleProxySourceGenerator extends BaseProxySourceCodeGenerator {

    // overriding generators for proxy methods
    @Override
    protected Collection<ClassElementGenerator> getMethodsDeclaration(Class<?> clazz) {
        return Collections.singletonList(new DelegateProxyMethodGenerator(clazz));
    }
}

public class DelegateProxyMethodGenerator extends AbstractProxyMethodGenerator {

    public DelegateProxyMethodGenerator(Class<?> delegatedClass) {
        super(delegatedClass);
    }

    // simply delegate call to superclass
    @Override
    protected String getProxyMethodBodyContent(JavaClassGeneratorConfig config, Method originalMethod,
            String delegateCall) {
        return String.format("%s%s;", getReturnKeyWordIfRequired(originalMethod), delegateCall);
    }
}
```

You may also find 
[AbstractProxyMethodGenerator](/src/main/java/com/github/vladislavsevruk/generator/proxy/source/generator/method/AbstractProxyMethodGenerator.java)
useful if all overridden methods should have common logic. You can simply implement its __getProxyMethodBodyContent__ 
method to generate all methods of proxy class with similar behavior:
```kotlin
public class LoggingProxyMethodGenerator extends AbstractProxyMethodGenerator {

    public LoggingProxyMethodGenerator(Class<?> delegatedClass) {
        super(delegatedClass);
    }

    // add debug logging before method call
    @Override
    protected String getProxyMethodBodyContent(JavaClassGeneratorConfig config, Method originalMethod,
            String delegateCall) {
        StringBuilder stringBuilder = new StringBuilder("logger.debug(\"Calling '")
                .append(originalMethod.getName()).append("' method.\");\n");
        return doubleIndents(stringBuilder, config).append(getReturnKeyWordIfRequired(originalMethod))
                .append(delegateCall).append(";").toString();
    }
}

public class LoggingProxySourceGenerator extends BaseProxySourceCodeGenerator {

    // overriding generators for imports
    @Override
    protected Collection<ClassImportGenerator> getImportsDeclaration(Class<?> clazz) {
        return Collections.singletonList((config, schemaObject) -> Arrays
                .asList("import org.apache.logging.log4j.LogManager;\n",
                        "import org.apache.logging.log4j.Logger;\n"));
    }

    // overriding generators for fields
    protected Collection<ClassElementGenerator> getFieldsDeclaration(Class<?> clazz) {
        return Collections.singletonList((config, schemaObject) -> String.format(
                        "%sprivate static final Logger logger = LogManager.getLogger(%s.class);%n%n",
                        config.getIndent().value(), schemaObject.getName()));
    }

    // overriding generators for proxy methods
    @Override
    protected Collection<ClassElementGenerator> getMethodsDeclaration(Class<?> clazz) {
        return Collections.singletonList(new LoggingProxyMethodGenerator(clazz));
    }
}
```
__NOTE:__ [AbstractProxyMethodGenerator](/src/main/java/com/github/vladislavsevruk/generator/proxy/source/generator/method/AbstractProxyMethodGenerator.java) 
generates source code that doesn't override static, final or methods from `java.lang.Object` class.

### Generate proxy class instance
To generate new proxy instance you need to use 
[ProxyFactory](/src/main/java/com/github/vladislavsevruk/generator/proxy/ProxyFactory.java) class:
```kotlin
class Cake {

    Cake() {
    }

    ...
}

ProxyFactory<Cake> proxyFactory = new ProxyFactory<>(Cake.class, new LoggingProxySourceGenerator());
// result is proxy class instance that extends 'Cake' class
Cake cake = proxyFactory.newInstance();
```

Please note that proxy class cannot be generated for __final__ classes:
```kotlin
final class Cake {

    Cake() {
    }

    ...
}

ProxyFactory<Cake> proxyFactory = new ProxyFactory<>(Cake.class, new LoggingProxySourceGenerator());
// result is 'Cake' class instance
Cake cake = proxyFactory.newInstance();
```

and for classes without any __non-private__ constructor:
```kotlin
class Cake {

    // the only constructor of class
    private Cake() {
    }

    ...
}

ProxyFactory<Cake> proxyFactory = new ProxyFactory<>(Cake.class, new LoggingProxySourceGenerator());
// 'IllegalArgumentException' is thrown as there is no any public constructor matching received parameters
Cake cake = proxyFactory.newInstance();
```

## License
This project is licensed under the MIT License, you can read the full text [here](LICENSE).
