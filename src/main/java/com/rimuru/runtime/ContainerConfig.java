package com.rimuru.runtime;

import java.util.List;

public record ContainerConfig(
        String imageName,
        String rootfsName,
        List<String> command
) {
    public ContainerConfig {
        if (imageName == null || imageName.isBlank()) {
            throw new IllegalArgumentException(
                    "도커 이미지 이름이 필요합니다."
            );
        }

        if (rootfsName == null || rootfsName.isBlank()) {
            throw new IllegalArgumentException(
                    "루트파일 시스템 이름이 필요합니다."
            );
        }

        if (command == null || command().isEmpty()) {
            throw new IllegalArgumentException(
                    "컨테이너 명령어를 입력해주세요."
            );
        }

        command = List.copyOf(command);
    }
}
