package com.rimuru.animation;

public final class WorldTransition {

    private enum Expression { CALM, CURIOUS, FOCUSED, HAPPY, SURPRISED }

    private enum Effect { NONE, ABSORB, SCAN, HALO }

    public enum IsolationStage {
        OVERLAY_FS("OverlayFS", "포식자: rootfs를 흡수해 새 몸을 만듭니다."),
        CGROUP("cgroup v2", "대현자: 메모리 50MiB · CPU 50%로 힘을 제한합니다."),
        NAMESPACE("Namespace", "결계: UTS · PID · Mount를 격리합니다.");

        private final String displayName;
        private final String narration;

        IsolationStage(String displayName, String narration) {
            this.displayName = displayName;
            this.narration = narration;
        }
    }

    private static final String CLEAR_SCREEN = "\u001B[H\u001B[2J";
    private static final String PALE_BLUE = "\u001B[38;2;167;225;247m";
    private static final String PURPLE = "\u001B[95m";
    private static final String GREEN = "\u001B[92m";
    private static final String RESET = "\u001B[0m";
    private static final int FRAME_WIDTH = 49;
    private static final double ANIMATION_SLOWDOWN = 1.3;
    private static final String[] SLIME_COLORS = {
            "", "35;112;166", "167;225;247", "96;153;213",
            "250;253;255", "40;97;158", "198;247;255"
    };

    private static final String HUMAN = """
                                  .-.
                                 (   )
                                  \u2571|\u2572
                                  \u2571 \u2572
            """;

    private static final String SLIME = drawSlime(36, 24, Expression.CALM, Effect.NONE, 0);
    private static final String SMALL_SLIME = drawSlime(24, 16, Expression.SURPRISED, Effect.NONE, 0);

    private WorldTransition() {
    }

    public static void welcome() throws InterruptedException {
        play(new String[]{
                frame("RIMURU CONTAINER RUNTIME", "현실 세계", HUMAN),
                frame("고유 능력 《대현자》 발동", "육체 분석 중 ·", """
                                 . * .
                                * ( ) *
                               .  \u2571|\u2572  .
                                  \u2571 \u2572
                                 . * .
                        분석 결과: 재구성 가능
                """),
                frame("고유 능력 《대현자》 발동", "육체 재구성 중 · ·", """
                              .  :  *  :  .
                            *    .   .    *
                              .  :  *  :  .
                             *   .  .   *
                                .  *  .
                """),
                frame("고유 능력 《대현자》 완료", "종족: 슬라임", SMALL_SLIME),
                frame("RIMURU CONTAINER RUNTIME", "새로운 몸으로 세계에 진입합니다.", SLIME)
        }, 850, PALE_BLUE);
    }

    public static void enterWorld() throws InterruptedException {
        play(new String[]{
                frame("DIMENSIONAL RIFT", "세계의 경계를 감지했습니다.", """
                              .  :  *  :  .
                           .       . .       .
                             .    :   :    .
                               .  : * :  .
                """),
                frame("DIMENSIONAL RIFT", "격리된 세계를 여는 중 ·", """
                           .   *   .   *   .
                         *      .---.      *
                             .-'     '-.
                            /    . .    \\
                           |   .  *  .   |
                            \\    ' '    /
                             '-._____.-'
                         *   .   *   .   *
                """),
                frame("DIMENSIONAL RIFT", "격리된 세계를 여는 중 · ·", """
                         .  :  *  :  *  :  .
                       :      .-------.      :
                          .-'           '-.
                         /    .  :  .     \\
                        |   :   ***   :    |
                         \\    .  :  .     /
                          '-.           .-'
                             '-------'
                       :  .  *  :  *  .  :
                """),
                frame("CONTAINER WORLD", "대현자: 격리 준비를 시작합니다.",
                        drawSlime(36, 24, Expression.CURIOUS, Effect.HALO, 0))
        }, 750, PURPLE);
    }

    public static void showIsolationStage(IsolationStage stage)
            throws InterruptedException {
        String title = "[" + (stage.ordinal() + 1) + "/3] "
                + stage.displayName + " 적용 중";
        Effect effect = switch (stage) {
            case OVERLAY_FS -> Effect.ABSORB;
            case CGROUP -> Effect.SCAN;
            case NAMESPACE -> Effect.HALO;
        };

        play(new String[]{
                frame(title, stage.narration,
                        drawSlime(30, 20, Expression.CURIOUS, effect, 0)),
                frame(title, stage.narration,
                        drawSlime(36, 24, Expression.FOCUSED, effect, 1)),
                frame(title, stage.narration,
                        drawSlime(36, 24, Expression.HAPPY, effect, 2))
        }, 750, PALE_BLUE);
    }

    public static void completeStage(IsolationStage stage) {
        System.out.printf("%s%s%s%n%n", GREEN,
                center(stage.displayName + " 적용 완료"), RESET);
        System.out.flush();
    }

    public static void leaveWorld() throws InterruptedException {
        play(new String[]{
                frame("RETURNING HOME", "격리된 세계를 닫습니다.",
                        drawSlime(36, 24, Expression.SURPRISED, Effect.HALO, 1)),
                frame("RETURNING HOME", "육체 복원 중 ·", SMALL_SLIME),
                frame("RETURNING HOME", "육체 복원 중 · ·", """
                              .  :  *  :  .
                            *    .   .    *
                              .  :  *  :  .
                """),
                frame("RETURN COMPLETE", "현실 세계", HUMAN)
        }, 750, PURPLE);
    }

    public static void preview() throws InterruptedException {
        enterWorld();

        for (IsolationStage stage : IsolationStage.values()) {
            showIsolationStage(stage);
            completeStage(stage);
            pause(700);
        }

        leaveWorld();
    }

    private static String frame(String title, String subtitle, String scene) {
        String border = "=".repeat(FRAME_WIDTH);
        return border + "\n" + center(title) + "\n" + border + "\n\n"
                + center(subtitle) + "\n\n"
                + (scene.contains("\u001B[") ? scene : centerScene(scene));
    }

    private static String centerScene(String scene) {
        StringBuilder centered = new StringBuilder();
        for (String line : scene.split("\n")) {
            centered.append(center(line.strip())).append('\n');
        }
        return centered.toString();
    }

    private static String center(String text) {
        int width = text.codePoints().map(WorldTransition::cellWidth).sum();
        return " ".repeat(Math.max(0, (FRAME_WIDTH - width) / 2)) + text;
    }

    private static int cellWidth(int codePoint) {
        if (codePoint >= 0x1100 && (codePoint <= 0x115F
                || codePoint >= 0x2E80 && codePoint <= 0xA4CF
                || codePoint >= 0xAC00 && codePoint <= 0xD7A3
                || codePoint >= 0xF900 && codePoint <= 0xFAFF
                || codePoint >= 0xFE10 && codePoint <= 0xFE6F
                || codePoint >= 0xFF01 && codePoint <= 0xFF60)) return 2;
        return 1;
    }

    private static void play(
            String[] frames,
            long delayMillis,
            String color
    ) throws InterruptedException {
        for (String frame : frames) {
            System.out.print(CLEAR_SCREEN);
            System.out.print(color);
            System.out.println(frame);
            System.out.print(RESET);
            System.out.flush();
            pause(delayMillis);
        }
    }

    private static void pause(long millis) throws InterruptedException {
        Thread.sleep(Math.round(millis * ANIMATION_SLOWDOWN));
    }

    // Each terminal cell draws two vertical pixels for a filled, rounded silhouette.
    private static String drawSlime(int width, int height, Expression expression,
                                    Effect effect, int phase) {
        StringBuilder art = new StringBuilder();

        for (int row = 0; row < height; row += 2) {
            for (int col = 0; col < FRAME_WIDTH; col++) {
                int top = slimePixel(col, row, width, height, expression, effect, phase);
                int bottom = slimePixel(col, row + 1, width, height, expression, effect, phase);
                if (top == 0 && bottom == 0) {
                    art.append(' ');
                } else if (top == bottom) {
                    art.append(foreground(top)).append('█');
                } else if (top == 0) {
                    art.append(foreground(bottom)).append('▄');
                } else if (bottom == 0) {
                    art.append(foreground(top)).append('▀');
                } else {
                    art.append(foreground(top)).append(background(bottom)).append('▀');
                }
                if (top != 0 || bottom != 0) art.append(RESET);
            }
            art.append('\n');
        }
        return art.toString();
    }

    private static int slimePixel(int col, int row, int width, int height,
                                  Expression expression, Effect effect, int phase) {
        double x = (col + 0.5 - FRAME_WIDTH / 2.0) * 2.0 / width;
        double y = (row + 0.5) / height;
        x += (phase - 1) * 0.045 * Math.sin(y * 8);
        double edge = x * x / (0.98 * 0.98)
                + (y - 0.53) * (y - 0.53) / (0.49 * 0.49);
        if (edge > 1.0) {
            if (effect == Effect.HALO) {
                for (int spark = 0; spark < 6; spark++) {
                    double angle = phase * 0.55 + spark * Math.PI / 3;
                    if (ellipse(x, y, 1.12 * 0.98 * Math.cos(angle),
                            0.53 + 1.12 * 0.49 * Math.sin(angle), 0.08, 0.05)) return 6;
                }
            }
            if (effect == Effect.ABSORB) {
                double distance = 1.40 - phase * 0.12;
                if (ellipse(x, y, -distance, 0.35, 0.07, 0.05)
                        || ellipse(x, y, distance, 0.65, 0.07, 0.05)
                        || ellipse(x, y, -distance + 0.16, 0.73, 0.05, 0.04)) return 6;
            }
            return 0;
        }
        if (edge > 0.88) return 1;

        if (effect == Effect.SCAN && Math.abs(y - (0.34 + phase * 0.12)) < 0.027) return 6;

        // Pale highlights stay in place while the eyes react to each stage.
        if (ellipse(x, y, -0.52, 0.29, 0.20, 0.09)
                || ellipse(x, y, 0.10, 0.42, 0.045, 0.035)) return 4;
        if (facePixel(x, y, expression)) return 5;

        if (ellipse(x, y, -0.05, 0.70, 0.77, 0.23)
                || ellipse(x, y, -0.09, 0.48, 0.46, 0.18)) return 3;
        return 2;
    }

    private static boolean facePixel(double x, double y, Expression expression) {
        return switch (expression) {
            case CALM -> nearLine(x, y, -0.45, 0.72, -0.12, 0.80)
                    || nearLine(x, y, 0.27, 0.81, 0.54, 0.70);
            case CURIOUS -> ellipse(x, y, -0.34, 0.73, 0.06, 0.045)
                    || ellipse(x, y, 0.34, 0.73, 0.06, 0.045)
                    || ellipse(x, y, 0, 0.84, 0.035, 0.035);
            case FOCUSED -> nearLine(x, y, -0.46, 0.69, -0.18, 0.77)
                    || nearLine(x, y, 0.18, 0.77, 0.46, 0.69);
            case HAPPY -> nearLine(x, y, -0.47, 0.76, -0.34, 0.70)
                    || nearLine(x, y, -0.34, 0.70, -0.21, 0.76)
                    || nearLine(x, y, 0.21, 0.76, 0.34, 0.70)
                    || nearLine(x, y, 0.34, 0.70, 0.47, 0.76)
                    || nearLine(x, y, -0.13, 0.85, 0, 0.89)
                    || nearLine(x, y, 0, 0.89, 0.13, 0.85);
            case SURPRISED -> ellipse(x, y, -0.34, 0.72, 0.07, 0.07)
                    || ellipse(x, y, 0.34, 0.72, 0.07, 0.07)
                    || ellipse(x, y, 0, 0.84, 0.06, 0.055);
        };
    }

    private static boolean ellipse(double x, double y, double cx, double cy,
                                   double rx, double ry) {
        return Math.pow((x - cx) / rx, 2) + Math.pow((y - cy) / ry, 2) <= 1;
    }

    private static boolean nearLine(double x, double y, double x1, double y1,
                                    double x2, double y2) {
        double t = Math.max(0, Math.min(1,
                ((x - x1) * (x2 - x1) + (y - y1) * (y2 - y1))
                        / (Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))));
        return Math.hypot(x - x1 - t * (x2 - x1), y - y1 - t * (y2 - y1)) < 0.025;
    }

    private static String foreground(int color) {
        return "\u001B[38;2;" + SLIME_COLORS[color] + "m";
    }

    private static String background(int color) {
        return "\u001B[48;2;" + SLIME_COLORS[color] + "m";
    }
}
