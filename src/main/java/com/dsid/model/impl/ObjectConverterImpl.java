package com.dsid.model.impl;

import com.dsid.model.ObjectConverter;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;

public class ObjectConverterImpl implements ObjectConverter {

    @Override
    public <T extends Serializable> T fromFile(Class<T> clazz, String file) {
        if (!new File(file).exists()) {
            return null;
        }
        try (InputStream inputStream = new FileInputStream(file)) {
            return SerializationUtils.deserialize(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toFile(Serializable serializable, String file) {
        try (final OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(SerializationUtils.serialize(serializable));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
