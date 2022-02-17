package com.dsid.model.impl;

import com.dsid.Main;
import com.dsid.model.ObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class ObjectManagerImpl implements ObjectManager {
    private static final Logger log = LoggerFactory.getLogger(ObjectManagerImpl.class);

    private final ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();

    @Override
    public <T> T create(Class<T> clazz, String className, Map<String, String> properties) {
        try {
            final Class<? extends T> loadedClass = (Class<? extends T>) Class.forName(className, true, classLoaderWrapper);
            final T object = loadedClass.newInstance();
            properties.forEach((fieldName, value) -> {
                try {
                    final PropertyDescriptor propertyDescriptor = new PropertyDescriptor(fieldName, loadedClass);
                    propertyDescriptor.getWriteMethod().invoke(object, value);
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
    public void register(URL url) {
        classLoaderWrapper.register(url);
        log.info(url + " registered");
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
