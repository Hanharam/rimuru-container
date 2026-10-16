package com.rimuru;

import com.rimuru.animation.WorldTransition;
import com.rimuru.cli.RimuruCli;

public class Main {

    public static void main(String[] args) {
        try {
            WorldTransition.welcome();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        RimuruCli cli = new RimuruCli();
        int exitCode = cli.run(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
