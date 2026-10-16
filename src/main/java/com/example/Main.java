package com.example;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class Main {

    interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load("c", CLibrary.class);

        int getpid();
    }

    public static void main(String[] args) {
        int pid = CLibrary.INSTANCE.getpid();
        System.out.println("Current PID: " + pid);
    }
}