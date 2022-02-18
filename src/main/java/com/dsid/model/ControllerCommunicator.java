package com.dsid.model;

import java.util.Map;

public interface ControllerCommunicator {

    void show(String name, Map<String, Object> parameters);

    Map<String, Object> getParameters();

    void information(String message);

    void error(Throwable e);

}
