/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package jdplus.toolkit.desktop.plugin.ui.processing;

import jdplus.toolkit.desktop.plugin.components.JExceptionPanel;
import jdplus.main.desktop.design.SwingComponent;
import jdplus.toolkit.base.api.processing.ProcDocument;
import jdplus.toolkit.base.api.util.Id;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import lombok.NonNull;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import nbbrd.design.SkipProcessing;

/**
 * An implementation of {@link IProcDocumentItemFactory} that combines an
 * {@link InformationExtractor} an an {@link ItemUI}.
 *
 * @param <D> the type of the source of information
 * @param <I> the type of the information
 */
public abstract class ProcDocumentItemFactory<D extends ProcDocument, I> implements IProcDocumentItemFactory {

    protected final Class<D> documentType;
    protected final Id itemId;
    protected final Function<? super D, I> extractor;
    protected final ItemUI itemUI;
    protected boolean async;

    /**
     * The only constructor of this class.Note that all its parameters must be
     * non-null and are checked to find out NPE as early as possible.
     *
     * @param documentType
     * @param itemId
     * @param extractor
     * @param itemUI
     */
    public ProcDocumentItemFactory(@NonNull Class<D> documentType, @NonNull Id itemId, @NonNull Function<? super D, I> extractor, @NonNull ItemUI<I> itemUI) {
        this.documentType = Objects.requireNonNull(documentType, "documentType");
        this.itemId = Objects.requireNonNull(itemId, "itemId");
        this.extractor = extractor;
        this.itemUI = Objects.requireNonNull(itemUI, "itemUI");
        this.async = false;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    @Override
    public Class<D> getDocumentType() {
        return documentType;
    }

    @Override
    public Id getItemId() {
        return itemId;
    }

    @Override
    public JComponent getView(ProcDocument document) {
        if (!getDocumentType().isInstance(document)) {
            throw new IllegalArgumentException("Invalid document type");
        }
        D source = getDocumentType().cast(document);
        if (async) {
            return new JAsyncView(source);
        } else {
            I info = extractor.apply(source);
            if (info == null) {
                return TsViewToolkit.getMessageViewer("No information for this item");
            }
            return itemUI.getView(info);

        }
    }

    @NonNull
    public Function<? super D, ?> getInformationExtractor() {
        return extractor;
    }

    @NonNull
    public ItemUI getItemUI() {
        return itemUI;
    }

    @SwingComponent
    @SkipProcessing(target = SwingComponent.class, reason = "parameters in constructor")
    private final class JAsyncView extends JComponent {

        public JAsyncView(final D source) {
            setLayout(new BorderLayout());
            add(newLoadingComponent("Loading"), BorderLayout.CENTER);

            new SwingWorker<I, Void>() {
                @Override
                protected I doInBackground() throws Exception {
                    return extractor.apply(source);
                }

                @Override
                protected void done() {
                    try {
                        I rslt = get();
                        switchToComponent(rslt == null ? null : itemUI.getView(rslt));
                    } catch (InterruptedException | ExecutionException ex) {
                        Thread.currentThread().interrupt();
                        switchToComponent(JExceptionPanel.create(ex));
                    }
                }
            }.execute();
        }

        private JComponent newLoadingComponent(String msg) {
            JLabel result = new JLabel();
            result.setHorizontalAlignment(SwingConstants.CENTER);
            result.setFont(result.getFont().deriveFont(result.getFont().getSize2D() * 2));
            result.setText("<html><center>" + msg);
            return result;
        }

        private void switchToComponent(Component c) {
            removeAll();
            if (c != null) {
                add(c, BorderLayout.CENTER);
                validate();
                invalidate();
                repaint();
                c.setSize(getSize());
            } else {
                add(newLoadingComponent("..."), BorderLayout.CENTER);
                validate();
                invalidate();
                repaint();
            }
        }
    }
}
