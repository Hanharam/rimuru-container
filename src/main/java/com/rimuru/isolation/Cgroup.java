package com.rimuru.isolation;

import java.io.IOException;
import java.nio.file.Path;

public class Cgroup {

    private final Path path = Path.of("/sys/fs/cgroup/rimuru");

    public void create() throws IOException, InterruptedException {
        String script = """
                cgroup_path=$1

                mkdir -p "$cgroup_path"

                printf '52428800\\n' > "$cgroup_path/memory.max"

                printf '0\\n' > "$cgroup_path/memory.swap.max"

                printf '50000 100000\\n' > "$cgroup_path/cpu.max"
                """;

        Process process = new ProcessBuilder(
                "sudo",
                "bash",
                "-c",
                script,
                "cgroup-init",
                path.toString()
        )
                .inheritIO()
                .start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                    "cgroup 생성에 실패했습니다. exitCode=" + exitCode
            );
        }
    }

    public void delete() throws IOException, InterruptedException {
        Process process = new ProcessBuilder(
                "sudo",
                "rmdir",
                path.toString()
        )
                .inheritIO()
                .start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                    "cgroup 삭제에 실패했습니다. exitCode=" + exitCode
            );
        }
    }

    public Path getPath() {
        return path;
    }
}
