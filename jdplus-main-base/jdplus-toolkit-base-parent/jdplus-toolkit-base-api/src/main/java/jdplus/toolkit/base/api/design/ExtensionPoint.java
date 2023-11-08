package jdplus.toolkit.base.api.design;

import java.lang.annotation.*;

/**
 * Specifies that a class is an extension point for JDemetra+.
 *
 * @author Philippe Charles
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface ExtensionPoint {
}
