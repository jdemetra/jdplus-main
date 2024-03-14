package jdplus.main.cli.design;

import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class GAVTest {

    @Test
    public void test() {
        assertThat(GAV.haveSameVersion(Collections.emptyList())).isFalse();
    }
}
