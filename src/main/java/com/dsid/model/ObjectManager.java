package com.dsid.model;

import java.net.URL;
import java.util.Map;
import java.util.Set;

public interface ObjectManager extends Savable {
    <T> T create(Class<T> clazz, String className, Map<String, String> properties);

    <T> Set<Class<? extends T>> getSuccessors(Class<T> clazz);

    Set<String> getProperties(Class<?> clazz);

    void register(URL url);
}
