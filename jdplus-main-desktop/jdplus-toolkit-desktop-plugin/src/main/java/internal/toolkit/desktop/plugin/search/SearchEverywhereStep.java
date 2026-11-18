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

import java.awt.AWTEvent;
import java.awt.Dialog;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;
import jdplus.toolkit.desktop.plugin.util.InstallerStep;

/** Opens the Search Everywhere dialog when Shift is pressed twice. */
public final class SearchEverywhereStep extends InstallerStep {

    private static final long DOUBLE_SHIFT_INTERVAL_MILLIS = 300;

    private final DoubleShiftDetector detector = new DoubleShiftDetector(DOUBLE_SHIFT_INTERVAL_MILLIS);
    private final AWTEventListener listener = this::onEvent;

    @Override
    public void restore() {
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.KEY_EVENT_MASK);
    }

    @Override
    public void close() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
    }

    private void onEvent(AWTEvent event) {
        if (event instanceof KeyEvent key
                && detector.onKeyEvent(key.getID(), key.getKeyCode(), key.getWhen())
                && isTriggerAllowed()) {
            SwingUtilities.invokeLater(SearchEverywhereDialog::open);
        }
    }

    private static boolean isTriggerAllowed() {
        Window activeWindow =
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        return activeWindow != null && !(activeWindow instanceof Dialog dialog && dialog.isModal());
    }
}
