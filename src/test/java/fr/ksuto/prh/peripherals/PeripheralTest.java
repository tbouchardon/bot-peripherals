package fr.ksuto.prh.peripherals;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeripheralTest {

    @Test
    void delayIsPreciseWithoutKeepingTheProcessorBusy() {

        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        long         cpu     = threads.getCurrentThreadCpuTime();
        long         start   = System.nanoTime();

        Peripheral.delay(100);

        double elapsed = (System.nanoTime() - start) / 1e6;
        double cpuMs   = (threads.getCurrentThreadCpuTime() - cpu) / 1e6;
        assertTrue(elapsed >= 100 && elapsed < 110, "100 ms attendues, " + elapsed + " ms");
        assertTrue(cpuMs < 20, "le fil est suspendu, pas en boucle active : " + cpuMs + " ms de processeur");
    }

    @Test
    void aboutAcceptsSmallNumbers() {

        for (int i = 0; i < 200; i++) {
            assertEquals(3, Peripheral.about(3), "écart nul (3 × 20 % arrondi à 0) : plantait (nextInt(0))");
            int around = Peripheral.about(100);
            assertTrue(around >= 80 && around < 120, "100 ± 20 : " + around);
        }
    }
}
