package org.jabsorb.test;

import java.io.Serializable;

public class Hello implements Serializable {
    
    private final static long serialVersionUID = 2;

    public String sayHello(String who) {
        return "hello " + who;
    }
}
