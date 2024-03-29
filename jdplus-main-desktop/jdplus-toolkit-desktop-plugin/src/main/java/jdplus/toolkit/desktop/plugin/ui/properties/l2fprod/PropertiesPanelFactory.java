package jdplus.toolkit.desktop.plugin.ui.properties.l2fprod;

import com.l2fprod.common.propertysheet.*;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;

import javax.swing.*;
import java.awt.*;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * @author Demortier Jeremy
 */
@lombok.extern.java.Log
public enum PropertiesPanelFactory {

    INSTANCE;

    public PropertySheetPanel createPanel(final Object o) {
        return createPanel(o, null);
    }

    public void update(PropertySheetPanel psp, final Object o, PropertyChangeListener listener) {
        final PropertySheetTableModel model = new PropertySheetTableModel();

        psp.setTable(new PropertySheetTable(model));
        if (o != null) {
            model.setProperties(createProperties(o));
            if (listener != null) {
                model.addPropertyChangeListener(listener);
            }
            model.addPropertyChangeListener(evt -> {
                try {
                    model.setProperties(createProperties(o));
                } catch (RuntimeException err) {
                    String msg = err.getMessage();
                } finally {
                    model.fireTableStructureChanged();
                }
            });
        }
        psp.setToolBarVisible(false);
        psp.setEditorFactory(CustomPropertyEditorRegistry.INSTANCE.getRegistry());
        psp.setRendererFactory(CustomPropertyRendererFactory.INSTANCE.getRegistry());
        psp.setDescriptionVisible(true);
        psp.setMode(PropertySheet.VIEW_AS_CATEGORIES);
        psp.setSorting(false);
        psp.setPreferredSize(new Dimension(300, 400));
        psp.setRestoreToggleStates(true);

    }

    public PropertySheetPanel createPanel(final Object o, PropertyChangeListener listener) {
        final PropertySheetTableModel model = new PropertySheetTableModel();
        final PropertySheetPanel psp = new PropertySheetPanel();

        psp.setTable(new PropertySheetTable(model));
        if (o != null) {
            model.setProperties(createProperties(o));
            if (listener != null) {
                model.addPropertyChangeListener(listener);
            }
            model.addPropertyChangeListener(evt -> {
                try {
                    model.setProperties(createProperties(o));
                } catch (RuntimeException err) {
                    String msg = err.getMessage();
                }
//                    finally {
//                        model.fireTableStructureChanged();
//                    }
            });
        }
        psp.setToolBarVisible(false);
        psp.setEditorFactory(CustomPropertyEditorRegistry.INSTANCE.getRegistry());
        psp.setRendererFactory(CustomPropertyRendererFactory.INSTANCE.getRegistry());
        psp.setDescriptionVisible(true);
        psp.setMode(PropertySheet.VIEW_AS_CATEGORIES);
        psp.setSorting(false);
        psp.setPreferredSize(new Dimension(300, 400));
        psp.setRestoreToggleStates(true);

        return psp;
    }

    public Property[] createProperties(final Object o) {
        List<Property> result = new ArrayList<>();

        // try first propertyDescriptors
        if (o instanceof IPropertyDescriptors) {
            createRoots((IPropertyDescriptors) o, result);

            //BAYENSK: Added because one-level IPropertyDescriptors were never taken into account.
            if (result.isEmpty()) {
                createRootProperties((IPropertyDescriptors) o, result, ((IPropertyDescriptors) o).getDisplayName());
            }
        } else {
            try {
                // use bean info
                BeanInfo info = Introspector.getBeanInfo(o.getClass());
                if (info != null) {
                    createRootProperties(o, info.getPropertyDescriptors(), result);
                }
            } catch (IntrospectionException ex) {
                log.log(Level.SEVERE, "", ex);
            }
        }
        return result.toArray(new Property[0]);
    }

    private void createRoots(final IPropertyDescriptors iprops, List<Property> props) {
        List<EnhancedPropertyDescriptor> eprops = iprops.getProperties();
        for (EnhancedPropertyDescriptor epd : eprops) {
            try {
                Object inner = epd.getDescriptor().getReadMethod().invoke(iprops);
                if (inner instanceof IPropertyDescriptors) {
                    createRootProperties((IPropertyDescriptors) inner, props, epd.getDescriptor().getDisplayName());
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                log.log(Level.SEVERE, "", ex);
            }
        }
    }

    private void createRootProperties(final IPropertyDescriptors iprops, List<Property> props, String category) {
        List<EnhancedPropertyDescriptor> eprops = iprops.getProperties();
        for (EnhancedPropertyDescriptor epd : eprops) {
            try {
                Object inner = epd.getDescriptor().getReadMethod().invoke(iprops);
                DefaultProperty root = createProperty(iprops, inner, epd);
                if (inner instanceof IPropertyDescriptors) {
                    createProperties((IPropertyDescriptors) inner, root);
                }
                root.setCategory(category);//epd.getCategory());
                props.add(root);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                log.log(Level.SEVERE, "", ex);
            }
        }

    }

    private void createProperties(final IPropertyDescriptors desc, final DefaultProperty parent) {
        List<EnhancedPropertyDescriptor> props = desc.getProperties();
        for (EnhancedPropertyDescriptor epd : props) {
            try {
                Object inner = epd.getDescriptor().getReadMethod().invoke(desc);
                DefaultProperty subProp = createProperty(desc, inner, epd);
                parent.addSubProperty(subProp);
                if (inner instanceof IPropertyDescriptors) {
                    createProperties((IPropertyDescriptors) inner, subProp);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                log.log(Level.SEVERE, "", ex);
            }
        }
    }

    private DefaultProperty createProperty(final Object owner, final Object value, final EnhancedPropertyDescriptor prop) {
        final DefaultProperty p = new DefaultProperty();

        try {
            PropertyDescriptor propDesc = prop.getDescriptor();
            p.setName(propDesc.getName());
            p.setDisplayName(propDesc.getDisplayName());
            p.setShortDescription(propDesc.getShortDescription());
            p.setEditable(!prop.isReadOnly());
            p.setCategory("");
            p.setType(propDesc.getPropertyType());

            if (value != null) {
                p.setValue(value);
                if (CustomPropertyEditorRegistry.INSTANCE.getRegistry().getEditor(value.getClass()) != null) {
                    // There is an editor available, so the property is a leaf
                    if (p.isEditable()) {
                        p.addPropertyChangeListener(evt -> {
                            try {
                                if (evt.getNewValue() == null) {
                                    return;
                                }
                                prop.getDescriptor().getWriteMethod().invoke(owner, evt.getNewValue());
                                if (prop.getRefreshMode() == EnhancedPropertyDescriptor.Refresh.All) {
                                }
                            } catch (IllegalAccessException | IllegalArgumentException ex) {
                            } catch (InvocationTargetException ex) {
                                JOptionPane.showMessageDialog(null, ex.getCause().getMessage());
                            } catch (RuntimeException err) {
                                JOptionPane.showMessageDialog(null, err.getMessage());
                            }
                        });
                    }
                    if (propDesc.getPropertyType().isArray()) {

                        Object[] array = (Object[]) value;
                        p.clearSubProperties();
                        if (array.length > 0) {
                            Property[] sp = new Property[array.length];

                            for (int i = 0; i < array.length; i++) {
                                Object element = array[i];
                                ArrayProperty subProp = new ArrayProperty();
                                subProp.setDisplayName((i + 1) + "");
                                subProp.setValue(element);
                                subProp.setEditable(false);
                                sp[i] = subProp;
                            }
                            p.addSubProperties(sp);
                        }
                    }

                    if (propDesc.getPropertyType().equals(Coefficients.class)) {
                        Coefficients coeff = (Coefficients) value;
                        p.clearSubProperties();
                        if (coeff.getFixedCoefficients() != null) {
                            Map<String, double[]> map = coeff.getFixedCoefficients();
                            if (map.size() > 0) {
                                Property[] sp = new Property[map.size()];
                                int i = 0;
                                for (Entry<String, double[]> entry : map.entrySet()) {
                                    ArrayProperty subProp = new ArrayProperty();
                                    subProp.setDisplayName(entry.getKey());
                                    if (entry.getValue().getClass().isArray()) {
                                        subProp.setValue(Arrays.toString(entry.getValue()));
                                    }

                                    subProp.setEditable(false);
                                    sp[i] = subProp;
                                    i++;
                                }
                                p.addSubProperties(sp);
                            }
                        }
                    }
                }
            }
            return p;
        } catch (Exception err) {
            return null;
        }
    }

    private void createRootProperties(Object o, PropertyDescriptor[] eprops, List<Property> props) {
        for (PropertyDescriptor pd : eprops) {
            try {
                Object inner = pd.getReadMethod().invoke(o);
                DefaultProperty root = createProperty(o, inner, new EnhancedPropertyDescriptor(pd, 0));
                if (inner instanceof IPropertyDescriptors) {
                    createProperties((IPropertyDescriptors) inner, root);
                }
                props.add(root);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                log.log(Level.SEVERE, "", ex);
            }
        }

    }
}

class ArrayProperty extends DefaultProperty {

    @Override
    public void readFromObject(Object object) {
    }
}
