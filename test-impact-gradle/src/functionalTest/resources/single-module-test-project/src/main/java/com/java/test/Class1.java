package com.java.test;

public class Class1 {

    public int covered(boolean arg) {
        for (int i = 0; i < 100; i++) {
            System.out.println(i);
        }
        if (arg) {
            return 1;
        }
        return 0;
    }

    public int partialCovered(boolean arg) {
        int result;
        if (arg) {
            result = 1;
        } else {
            result = 0;
        }
        return result;
    }

    public int notCovered(boolean arg) {
        return arg ? 1 : 0;
    }
}
