package com.dsid.model;

import java.io.Serializable;

public interface ObjectConverter {
    <T extends Serializable> T fromFile(Class<T> clazz, String file);

    void toFile(Serializable serializable, String file);
}
