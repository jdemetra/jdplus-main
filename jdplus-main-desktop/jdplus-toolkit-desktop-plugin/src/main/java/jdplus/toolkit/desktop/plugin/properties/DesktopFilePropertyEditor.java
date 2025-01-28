/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
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
package jdplus.toolkit.desktop.plugin.properties;

import jdplus.toolkit.desktop.plugin.completion.DesktopFileAutoCompletionSource;
import jdplus.toolkit.desktop.plugin.concurrent.ThreadPriority;
import jdplus.toolkit.desktop.plugin.concurrent.UIExecutors;
import jdplus.main.desktop.design.SwingEditorAttribute;
import jdplus.toolkit.desktop.plugin.util.Collections2;
import ec.util.completion.swing.FileListCellRenderer;
import ec.util.completion.swing.JAutoCompletion;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

import javax.swing.*;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * @author Philippe Charles
 */
public final class DesktopFilePropertyEditor extends ForwardingPropertyEditor implements ExPropertyEditor, InplaceEditor.Factory {

    @SwingEditorAttribute
    public static final String FILTER_ATTRIBUTE = "filter";

    @SwingEditorAttribute
    public static final String PATHS_ATTRIBUTE = "paths";

    private final Supplier<InplaceEditor> inplaceEditor;

    // we cannot extend FileEditor (not a "friend" dependency), so we delegate to the default
    // => cannot register this editor
    public DesktopFilePropertyEditor() {
        super(PropertyEditorManager.findEditor(File.class));
        this.inplaceEditor = Collections2.memoize(DesktopFileInplaceEditor::new);
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        env.registerInplaceEditorFactory(this);
        if (getDelegate() instanceof ExPropertyEditor) {
            ((ExPropertyEditor) getDelegate()).attachEnv(env);
        }
    }

    @Override
    public InplaceEditor getInplaceEditor() {
        return inplaceEditor.get();
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final class DesktopFileInplaceEditor extends AbstractInplaceEditor {

        private static final ExecutorService ICON_EXECUTOR = UIExecutors.newSingleThreadExecutor(ThreadPriority.MIN);

        private final JTextField component = new JTextField();
        private final JAutoCompletion autoCompletion = new JAutoCompletion(component);

        @Override
        public void connect(PropertyEditor propertyEditor, PropertyEnv env) {
            Object tmp = AbstractExPropertyEditor.attr(env, FILTER_ATTRIBUTE, Object.class).orElse(null);
            File[] paths = AbstractExPropertyEditor.attr(env, PATHS_ATTRIBUTE, File[].class).orElse(new File[0]);
            java.io.FileFilter filter = null;
            if (tmp instanceof java.io.FileFilter) {
                filter = (java.io.FileFilter) tmp;
            } else if (tmp instanceof javax.swing.filechooser.FileFilter) {
                filter = toFileFilter((javax.swing.filechooser.FileFilter) tmp);
            } else if (tmp instanceof java.io.FilenameFilter) {
                filter = toFileFilter((java.io.FilenameFilter) tmp);
            }
            autoCompletion.setSource(new DesktopFileAutoCompletionSource(filter, paths));
            autoCompletion.getList().setCellRenderer(new FileListCellRenderer(null, ICON_EXECUTOR, paths));
            super.connect(propertyEditor, env);
        }

        @Override
        public JComponent getComponent() {
            return component;
        }

        @Override
        public Object getValue() {
            return Path.of(component.getText()).toFile();
        }

        @Override
        public void setValue(Object o) {
            component.setText(((File) o).getPath());
        }
    }

    private static java.io.FileFilter toFileFilter(javax.swing.filechooser.FileFilter filter) {
        return filter::accept;
    }

    private static java.io.FileFilter toFileFilter(java.io.FilenameFilter filter) {
        return o -> filter.accept(o.getParentFile(), o.getName());
    }
    //</editor-fold>
}
