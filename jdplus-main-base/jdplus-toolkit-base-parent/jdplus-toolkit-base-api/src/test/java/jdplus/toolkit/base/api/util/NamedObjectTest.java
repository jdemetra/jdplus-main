package jdplus.toolkit.base.api.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class NamedObjectTest {

    @Test
    public void test() {
        assertThat(new NamedObject<>("a", "b"))
                .isEqualTo(new NamedObject<>("a", "b"))
                .isNotEqualTo(new NamedObject<>("a", "c"))
                .isNotEqualTo(new NamedObject<>("c", "b"));

        assertThat(new NamedObject<>("a", null))
                .isEqualTo(new NamedObject<>("a", null))
                .isNotEqualTo(new NamedObject<>("a", "c"))
                .isNotEqualTo(new NamedObject<>("c", null));

        assertThatNullPointerException()
                .isThrownBy(() -> new NamedObject<>(null, "b").compareTo(new NamedObject<>("a", "b")));
    }
}
