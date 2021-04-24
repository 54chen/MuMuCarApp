package com.chen.mumucarapp.control;

public class Util {
    public static int get4Random() {
        while (true) {
            int i = (int)(Math.random() * 10000.0D);
            if (i >= 1000)
                return i;
        }
    }
}
