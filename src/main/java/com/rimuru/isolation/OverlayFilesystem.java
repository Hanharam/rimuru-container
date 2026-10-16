package com.rimuru.isolation;

import com.rimuru.runtime.ContainerConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class OverlayFilesystem {

    private Path merged;

    public void mount(ContainerConfig config)
            throws IOException, InterruptedException {

        // 1. 컨테이너 디렉터리 생성
        Path containerDir = Path.of(
                "runtime",
                "container",
                UUID.randomUUID().toString()
        ).toAbsolutePath();

        Path rootfs = containerDir.resolve(config.rootfsName());
        Path upper = containerDir.resolve("upper");
        Path work = containerDir.resolve("work");
        merged = containerDir.resolve("merged");

        Files.createDirectories(rootfs);
        Files.createDirectories(upper);
        Files.createDirectories(work);
        Files.createDirectories(merged);

        // 2. 도커 이미지 rootfs 추출 후 OverlayFS 생성
        String script = String.format(
                "cid=$(sudo docker create %s) && " +
                        "sudo docker export $cid | sudo tar -C %s -xf - && " +
                        "sudo docker rm -v $cid && " +
                        "sudo mount -t overlay overlay -o lowerdir=%s,upperdir=%s,workdir=%s %s",
                config.imageName(),
                rootfs,
                rootfs,
                upper,
                work,
                merged
        );

        new ProcessBuilder("sh", "-c", script)
                .inheritIO()
                .start()
                .waitFor();
    }

    public void unmount() throws IOException, InterruptedException {
        new ProcessBuilder(
                "sudo", "umount", merged.toString()
        ).inheritIO().start().waitFor();
    }

    public Path getMerged() {
        return merged;
    }
}