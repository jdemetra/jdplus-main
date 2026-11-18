/*
 * Copyright 2026 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the Licence;
 * you may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.toolkit.desktop.plugin.search;

import java.awt.event.KeyEvent;

/** Detects two consecutive taps on the Shift key, without any other key in between. */
final class DoubleShiftDetector {

    private static final long NONE = Long.MIN_VALUE;

    private final long maxIntervalMillis;
    private boolean shiftDown;
    private boolean shiftUsedAsModifier;
    private long lastTapReleaseTime = NONE;

    DoubleShiftDetector(long maxIntervalMillis) {
        this.maxIntervalMillis = maxIntervalMillis;
    }

    /** Returns true when the given key event completes a double tap on Shift. */
    boolean onKeyEvent(int id, int keyCode, long when) {
        if (keyCode != KeyEvent.VK_SHIFT) {
            if (id == KeyEvent.KEY_PRESSED) {
                lastTapReleaseTime = NONE;
                shiftUsedAsModifier = shiftDown;
            }
            return false;
        }
        switch (id) {
            case KeyEvent.KEY_PRESSED -> {
                if (shiftDown) {
                    return false;
                }
                shiftDown = true;
                shiftUsedAsModifier = false;
                if (lastTapReleaseTime != NONE && when - lastTapReleaseTime <= maxIntervalMillis) {
                    lastTapReleaseTime = NONE;
                    shiftUsedAsModifier = true;
                    return true;
                }
                return false;
            }
            case KeyEvent.KEY_RELEASED -> {
                lastTapReleaseTime = shiftDown && !shiftUsedAsModifier ? when : NONE;
                shiftDown = false;
                return false;
            }
            default -> {
                return false;
            }
        }
    }
}
