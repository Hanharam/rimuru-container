package com.rimuru.runtime;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContainerConfigTest {

    @Test
    void acceptsAContainerCommand() {
        ContainerConfig config = assertDoesNotThrow(() -> new ContainerConfig(
                "busybox:latest",
                "tmproot",
                List.of("/bin/sh")
        ));

        assertEquals(List.of("/bin/sh"), config.command());
    }

    @Test
    void rejectsMissingContainerCommand() {
        assertThrows(IllegalArgumentException.class, () -> new ContainerConfig(
                "busybox:latest",
                "tmproot",
                null
        ));
        assertThrows(IllegalArgumentException.class, () -> new ContainerConfig(
                "busybox:latest",
                "tmproot",
                List.of()
        ));
    }
}
