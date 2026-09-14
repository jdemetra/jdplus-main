package jdplus.main.cli.design;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GAVTest {

    @Test
    public void test() {
        assertThat(GAV.haveSameVersion(List.of())).isFalse();
    }
}
