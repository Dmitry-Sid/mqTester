package com.dsid.model;

import java.net.URL;
import java.util.Map;

public interface ObjectManager {
    <T> T create(Class<T> clazz, String className, Map<String, String> properties);

    void register(URL url);
}
