package com.dsid.test;

public class TestLoadedClass {
    public int settersCount = 0;
    private String value1;

    public void setValue1(String value1) {
        this.value1 = value1;
        settersCount++;
    }

    public String getValue1() {
        return value1;
    }
}
