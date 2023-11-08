package jdplus.toolkit.base.api.design;

import java.lang.annotation.*;

/**
 * Specifies that a class is an extension point for JDemetra+.<br>
 * An extension point allows to change behavior and add functionality.
 * Example: time series providers.
 *
 * @author Philippe Charles
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface ExtensionPoint {
}
