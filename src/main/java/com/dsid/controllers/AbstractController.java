package com.dsid.controllers;

import com.dsid.model.ControllerCommunicator;

public abstract class AbstractController {
    protected final ControllerCommunicator controllerCommunicator;

    public AbstractController(ControllerCommunicator controllerCommunicator) {
        this.controllerCommunicator = controllerCommunicator;
    }
}
