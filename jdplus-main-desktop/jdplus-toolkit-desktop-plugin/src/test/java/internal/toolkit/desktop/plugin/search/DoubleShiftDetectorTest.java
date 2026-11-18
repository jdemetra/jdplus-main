package internal.toolkit.desktop.plugin.search;

import static java.awt.event.KeyEvent.KEY_PRESSED;
import static java.awt.event.KeyEvent.KEY_RELEASED;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DoubleShiftDetectorTest {

    @Test
    void detectsTwoQuickTaps() {
        DoubleShiftDetector x = new DoubleShiftDetector(300);

        assertThat(x.onKeyEvent(KEY_PRESSED, VK_SHIFT, 0)).isFalse();
        assertThat(x.onKeyEvent(KEY_RELEASED, VK_SHIFT, 50)).isFalse();
        assertThat(x.onKeyEvent(KEY_PRESSED, VK_SHIFT, 200)).isTrue();
    }

    @Test
    void ignoresSlowTaps() {
        DoubleShiftDetector x = new DoubleShiftDetector(300);

        tap(x, 0);
        assertThat(x.onKeyEvent(KEY_PRESSED, VK_SHIFT, 500)).isFalse();
    }

    @Test
    void ignoresTapsSeparatedByAnotherKey() {
        DoubleShiftDetector x = new DoubleShiftDetector(300);

        tap(x, 0);
        x.onKeyEvent(KEY_PRESSED, VK_A, 60);
        assertThat(x.onKeyEvent(KEY_PRESSED, VK_SHIFT, 100)).isFalse();
    }

    @Test
    void ignoresShiftUsedAsModifier() {
        DoubleShiftDetector x = new DoubleShiftDetector(300);

        x.onKeyEvent(KEY_PRESSED, VK_SHIFT, 0);
        x.onKeyEvent(KEY_PRESSED, VK_A, 20);
        x.onKeyEvent(KEY_RELEASED, VK_SHIFT, 50);
        assertThat(x.onKeyEvent(KEY_PRESSED, VK_SHIFT, 100)).isFalse();
    }

    @Test
    void ignoresAutoRepeatAndThirdTap() {
        DoubleShiftDetector x = new DoubleShiftDetector(300);

        x.onKeyEvent(KEY_PRESSED, VK_SHIFT, 0);
        assertThat(x.onKeyEvent(KEY_PRESSED, VK_SHIFT, 30)).isFalse();
        x.onKeyEvent(KEY_RELEASED, VK_SHIFT, 50);
        assertThat(x.onKeyEvent(KEY_PRESSED, VK_SHIFT, 100)).isTrue();
        x.onKeyEvent(KEY_RELEASED, VK_SHIFT, 150);
        assertThat(x.onKeyEvent(KEY_PRESSED, VK_SHIFT, 200)).isFalse();
    }

    private static void tap(DoubleShiftDetector x, long when) {
        x.onKeyEvent(KEY_PRESSED, VK_SHIFT, when);
        x.onKeyEvent(KEY_RELEASED, VK_SHIFT, when + 50);
    }
}
