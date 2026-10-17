# Rimuru Container Runtime

Rimuru는 Linux의 namespace, cgroup v2, OverlayFS를 직접 사용해 만든 학습용 컨테이너 런타임입니다.
Docker 이미지를 root filesystem으로 변환한 뒤 격리된 환경에서 사용자가 지정한 명령을 실행합니다.

> 실제 서비스용 런타임이 아니라 Linux 컨테이너가 어떻게 만들어지는지 학습하기 위한 프로젝트입니다.

## 동작 방식

`rimuru run`을 실행하면 다음 순서로 컨테이너를 준비합니다.

1. Docker 이미지를 export하여 rootfs를 생성합니다.
2. OverlayFS로 읽기 전용 rootfs와 컨테이너의 변경 영역을 결합합니다.
3. cgroup v2로 메모리와 CPU 사용량을 제한합니다.
4. UTS, PID, Mount namespace를 생성합니다.
5. `pivot_root`로 컨테이너의 루트 파일시스템을 전환합니다.
6. 사용자가 입력한 명령을 컨테이너의 PID 1로 실행합니다.
7. 명령이 종료되면 OverlayFS와 cgroup을 정리합니다.

현재 기본 자원 제한은 다음과 같습니다.

| 자원 | 제한 |
| --- | --- |
| 메모리 | 50 MiB |
| Swap | 사용 안 함 |
| CPU | 50% |

## 요구 환경

- Ubuntu 또는 유사한 Linux 환경
- Java 17 이상
- Docker
- cgroup v2
- OverlayFS
- `unshare`, `mount`, `pivot_root`를 제공하는 `util-linux`
- `sudo`, `bash`, `tar`

Ubuntu에서는 다음 패키지를 설치합니다.

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk docker.io util-linux bash tar sudo
sudo systemctl enable --now docker
```

커널 기능을 확인하려면 다음 명령을 사용할 수 있습니다.

```bash
java -version
docker --version
unshare --version
mount | grep cgroup2
grep overlay /proc/filesystems
```

## 빌드 및 설치

저장소를 내려받고 Gradle Wrapper로 빌드합니다.

```bash
git clone https://github.com/Hanharam/rimuru-container.git
cd rimuru-container
./gradlew clean test installDist
```

빌드 결과에는 실행 스크립트와 필요한 JAR가 함께 생성됩니다.

```text
build/install/rimuru/
├── bin/rimuru
└── lib/
```

어느 디렉터리에서든 `rimuru` 명령을 사용할 수 있도록 설치합니다.

```bash
sudo mkdir -p /opt/rimuru
sudo cp -r build/install/rimuru/. /opt/rimuru/
sudo ln -sfn /opt/rimuru/bin/rimuru /usr/local/bin/rimuru
```

설치 확인:

```bash
rimuru help
```

`/usr/local/bin/rimuru`는 `/opt/rimuru/bin/rimuru`를 가리키며, 실행 스크립트가 Java와 `lib` 디렉터리의 JAR를 자동으로 실행합니다.

## 사용법

```text
rimuru run <docker_image> <rootfs_name> <command> [args...]
```

BusyBox 컨테이너에서 셸을 실행하는 예시:

```bash
rimuru run busybox:latest tmproot /bin/sh
```

명령 하나만 실행하고 종료할 수도 있습니다.

```bash
rimuru run busybox:latest tmproot /bin/sh -c 'echo "Hello from Rimuru"'
```

사용자가 지정한 명령이 컨테이너의 메인 프로세스입니다. 해당 명령이 끝나면 컨테이너도 종료됩니다.

## 격리 확인

컨테이너 안에서 PID, hostname, namespace와 cgroup을 확인할 수 있습니다.

```sh
echo "PID=$$"
hostname
ps
readlink /proc/self/ns/pid
readlink /proc/self/ns/uts
readlink /proc/self/ns/mnt
cat /proc/self/cgroup
```

컨테이너의 메인 프로세스는 내부에서 PID 1로 보이고 hostname은 `rimuru`로 설정됩니다. 호스트에서는 동일한 프로세스가 실제 호스트 PID로 보이는 것이 정상입니다.

컨테이너 실행 중 호스트의 다른 터미널에서 자원 제한도 확인할 수 있습니다.

```bash
sudo cat /sys/fs/cgroup/rimuru/memory.max
sudo cat /sys/fs/cgroup/rimuru/memory.swap.max
sudo cat /sys/fs/cgroup/rimuru/cpu.max
sudo cat /sys/fs/cgroup/rimuru/cgroup.procs
```

## 생성되는 파일

Rimuru는 명령을 실행한 현재 디렉터리에 컨테이너 파일시스템을 만듭니다.

```text
runtime/container/<UUID>/
├── <rootfs_name>/  # Docker 이미지에서 추출한 rootfs
├── upper/          # 컨테이너에서 변경한 파일
├── work/           # OverlayFS 작업 영역
└── merged/         # 컨테이너가 사용하는 결합된 파일시스템
```

컨테이너 종료 시 `merged` 마운트와 cgroup은 정리하지만, `runtime/container/<UUID>` 아래의 파일은 현재 자동으로 삭제하지 않습니다.

## 현재 제한사항

- Linux에서만 실행할 수 있습니다.
- 실행 과정에서 Docker와 mount, cgroup 작업을 위해 `sudo`를 사용합니다.
- PID, UTS, Mount namespace만 격리합니다.
- Network, IPC, User namespace는 아직 호스트와 공유합니다.
- cgroup 경로가 `/sys/fs/cgroup/rimuru`로 고정되어 있어 여러 컨테이너의 동시 실행을 지원하지 않습니다.
- 컨테이너별 파일과 상태를 관리하는 `ps`, `rm`, 백그라운드 실행 기능은 아직 없습니다.

## 개발 중 실행

시스템에 설치하지 않고 Gradle을 통해 바로 실행할 수도 있습니다.

```bash
./gradlew run --args='run busybox:latest tmproot /bin/sh'
```
