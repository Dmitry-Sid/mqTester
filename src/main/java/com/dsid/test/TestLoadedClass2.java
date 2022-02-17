package com.dsid.test;

public class TestLoadedClass2 extends TestLoadedClass {
    private String value2;

    public void setValue2(String value2) {
        this.value2 = value2;
        settersCount++;
    }

    public String getValue2() {
        return value2;
    }
}
