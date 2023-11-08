package jdplus.toolkit.base.api.design;

import java.lang.annotation.*;

/**
 * Specifies that a class is an interchangeable processor.<br>
 * An interchangeable processor doesn't change behaviour or add functionality.
 * Instead, it allows a more powerful implementation to be plugged in at runtime.
 * Example: matrix computation on CPU vs GPU
 *
 * @author Philippe Charles
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface InterchangeableProcessor {
}
