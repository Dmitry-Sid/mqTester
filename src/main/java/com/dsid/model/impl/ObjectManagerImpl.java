package com.dsid.model.impl;

import com.dsid.model.ObjectConverter;
import com.dsid.model.ObjectManager;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.beans.Statement;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObjectManagerImpl implements ObjectManager {
    private static final Logger log = LoggerFactory.getLogger(ObjectManagerImpl.class);

    private final ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();
    private final Map<Class<?>, Set<Class<?>>> originSystemCache = new HashMap<>();
    private final List<Class<?>> loadedClasses = new ArrayList<>();

    private final ObjectConverter objectConverter;
    private final String fileName;
    private final Set<URL> urls;

    @Inject
    public ObjectManagerImpl(ObjectConverter objectConverter, String fileName) {
        this.objectConverter = objectConverter;
        this.fileName = fileName;
        if (new File(fileName).exists()) {
            this.urls = (Set<URL>) objectConverter.fromFile(HashSet.class, fileName);
        } else {
            this.urls = new HashSet<>();
        }
        this.urls.forEach(url -> register(url, false, false));
        save();
    }

    private static <T> boolean classFilter(Class<? extends T> superClazz, Class<? extends T> clazz) {
        return superClazz.isAssignableFrom(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())
                && checkHasPublicConstructor(clazz);
    }

    private static <T> boolean checkHasPublicConstructor(Class<? extends T> clazz) {
        for (Constructor constructor : clazz.getConstructors()) {
            if (Modifier.isPublic(constructor.getModifiers())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> T create(Class<T> clazz, String className, Map<String, String> properties) {
        try {
            final Class<? extends T> loadedClass = (Class<? extends T>) Class.forName(className, true, classLoaderWrapper);
            final T object = loadedClass.newInstance();
            properties.forEach((fieldName, value) -> {
                try {
                    new Statement(object, "set" + StringUtils.capitalize(fieldName), new Object[]{value}).execute();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return object;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> Set<Class<? extends T>> getSuccessors(Class<T> clazz) {
        return Stream.concat(loadedClasses.stream()
                .filter(c -> ObjectManagerImpl.classFilter(clazz, c)), getSystemSuccessorsStream(clazz))
                .map(c -> (Class<? extends T>) c)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getProperties(Class<?> clazz) {
        final Set<String> set = new HashSet<>();
        for (Method method : clazz.getMethods()) {
            final String name = method.getName();
            if (name.startsWith("set")) {
                set.add(StringUtils.uncapitalize(name.substring(3)));
            }
        }
        return set;
    }

    private <T> Stream<Class<?>> getSystemSuccessorsStream(Class<T> clazz) {
        return originSystemCache.computeIfAbsent(clazz, k -> new Reflections().getSubTypesOf(clazz).stream()
                .filter(c -> ObjectManagerImpl.classFilter(clazz, c)).collect(Collectors.toSet())).stream();
    }

    @Override
    public void register(URL url) {
        register(url, true, true);
    }

    private void register(URL url, boolean checkInSystem, boolean throwExceptionIfNoAvailable) {
        if (checkInSystem && urls.contains(url)) {
            log.warn(url + " already in system");
            return;
        }
        try {
            url.openStream();
        } catch (Exception e) {
            if (throwExceptionIfNoAvailable) {
                throw new RuntimeException(e);
            } else {
                log.warn(url + " is not available");
                urls.remove(url);
                return;
            }
        }
        classLoaderWrapper.register(url);
        loadClasses(url);
        urls.add(url);
        log.info(url + " registered");
    }

    private void loadClasses(URL url) {
        try (JarInputStream jarInputStream = new JarInputStream(url.openStream())) {
            JarEntry jar;
            while ((jar = jarInputStream.getNextJarEntry()) != null) {
                if (jar.isDirectory() || !jar.getName().endsWith(".class")) {
                    continue;
                }
                final String className = jar.getName().substring(0, jar.getName().length() - 6).replace('/', '.');
                try {
                    loadedClasses.add(Class.forName(className, true, classLoaderWrapper));
                } catch (Throwable e) {
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save() {
        objectConverter.toFile((Serializable) urls, fileName);
    }

    private static class ClassLoaderWrapper extends URLClassLoader {

        private ClassLoaderWrapper() {
            super(((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs(), ClassLoader.getSystemClassLoader());
        }

        private void register(URL url) {
            addURL(url);
        }
    }
}
