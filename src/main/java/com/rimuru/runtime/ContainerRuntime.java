package com.rimuru.runtime;

import com.rimuru.animation.WorldTransition;
import com.rimuru.animation.WorldTransition.IsolationStage;
import com.rimuru.isolation.Cgroup;
import com.rimuru.isolation.Namespace;
import com.rimuru.isolation.OverlayFilesystem;

import java.io.IOException;

public class ContainerRuntime {

    public void run(ContainerConfig config) throws IOException, InterruptedException{

        WorldTransition.enterWorld();

        OverlayFilesystem filesystem = new OverlayFilesystem();
        Cgroup cgroup = new Cgroup();
        Namespace namespace = new Namespace();

        boolean filesystemMounted = false;
        boolean cgroupCreated = false;

        try {
            WorldTransition.showIsolationStage(IsolationStage.OVERLAY_FS);

            // 1. Docker 이미지 추출 및 OverlayFS 마운트
            filesystem.mount(config);
            filesystemMounted = true;
            WorldTransition.completeStage(IsolationStage.OVERLAY_FS);

            WorldTransition.showIsolationStage(IsolationStage.CGROUP);

            // 2. Cgroup 생성 및 자원 제한 설정
            cgroup.create();
            cgroupCreated = true;
            WorldTransition.completeStage(IsolationStage.CGROUP);

            WorldTransition.showIsolationStage(IsolationStage.NAMESPACE);

            // 3. namespace 생성 후 컨테이너 프로세스 실행
            int exitCode = namespace.run(
                    filesystem.getMerged(),
                    cgroup.getPath(),
                    config.command()
            );

            if (exitCode != 0) {
                throw new IOException(
                        "컨테이너 프로세스 비정상 종료되었습니다. exitCode=" + exitCode
                );
            }
        } finally {
            try {
                if (filesystemMounted) {
                    filesystem.unmount();
                }
            } finally {
                try {
                    if (cgroupCreated) {
                        cgroup.delete();
                    }
                } finally {
                    WorldTransition.leaveWorld();
                }
            }
        }
    }
}
