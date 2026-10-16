package com.rimuru.cli;

import com.rimuru.animation.WorldTransition;
import com.rimuru.runtime.ContainerConfig;
import com.rimuru.runtime.ContainerRuntime;

import java.util.Arrays;
import java.util.List;

public class RimuruCli {

    public int run(String[] args) {
        if (args.length == 0 || args[0].equals("--help")) {
            printHelp();
            return 0;
        }

        return switch (args[0].toLowerCase()) {
            case "help" -> {
                printHelp();
                yield 0;
            }
            case "run" -> runContainer(args);
            case "preview" -> previewAnimation();
            default -> {
                printHelp();
                yield 1;
            }
        };
    }

    private int previewAnimation() {
        try {
            WorldTransition.preview();
            return 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 130;
        }
    }

    private int runContainer(String[] args) {
        if (args.length < 4) {
            printHelp();
            return 1;
        }

        String imageName = args[1];
        String rootfsName = args[2];
        List<String> command = Arrays.asList(args).subList(3, args.length);

        ContainerConfig config = new ContainerConfig(
                imageName,
                rootfsName,
                command
        );

        try {
            new ContainerRuntime().run(config);
            return 0;
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return 1;
        }
    }

    private void printHelp() {
        System.out.println("""
                크하하하! 나는 폭풍룡 베루도라!
                이 세계의 길잡이가 되어 주마. 잘 듣거라, 리무루!

                [명령어]
                rimuru run <docker_image> <rootfs_name> <command> [args...]
                    Docker 이미지를 바탕으로 격리된 세계를 열고 명령을 실행한다.
                rimuru help
                    이 폭풍룡의 안내를 다시 듣는다.

                [예시]
                    rimuru run busybox:latest tmproot /bin/sh
                """);
    }
}
