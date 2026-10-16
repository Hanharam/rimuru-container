package com.rimuru.isolation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Namespace {

    public int run(
            Path merged,
            Path cgroupPath,
            List<String> containerCommand
    ) throws IOException, InterruptedException {

        String script = """
                merged=$1
                cgroup_path=$2
                shift 2

                # 현재 namespace의 프로세스를 cgroup에 넣기
                printf '0\\n' > "$cgroup_path/cgroup.procs"

                mount --make-rprivate /

                # old 디렉터리 생성
                mkdir -p "$merged/put_old"
                cd "$merged"

                # pivot_root 실행
                pivot_root . put_old
                cd /

                # 캐시된 실행 파일 경로 제거
                hash -r

                # 기존 호스트 루트 제거
                umount -l /put_old
                rmdir /put_old

                # PID namespace에 맞는 proc 마운트
                mount -t proc proc /proc

                hostname rimuru

                exec "$@"
                """;

        ProcessBuilder processBuilder = new ProcessBuilder(
                "sudo",
                "unshare",
                "--uts",
                "--pid",
                "--fork",
                "--mount",
                "--kill-child",
                "bash",
                "-c",
                script,
                "container-init",
                merged.toString(),
                cgroupPath.toString()
        );

        processBuilder.command().addAll(containerCommand);

        return processBuilder
                .inheritIO()
                .start()
                .waitFor();
    }
}
