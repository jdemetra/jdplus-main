package jdplus.toolkit.base.api.util;

import org.assertj.core.api.Condition;
import org.assertj.core.condition.AllOf;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultNameValidatorTest {

    @Test
    public void test() {
        var samples = new String[]{"", ".?/"};

        for (var invalidChars : samples) {
            DefaultNameValidator x = new DefaultNameValidator(invalidChars);

            assertThat(x)
                    .is(invalidating(null, "The name can't be empty"))
                    .is(invalidating("", "The name can't be empty"))
                    .is(invalidating(" abc", "The name can't contain leading or trailing ws"))
                    .is(invalidating("abc ", "The name can't contain leading or trailing ws"))
                    .is(validating("abc"))
                    .is(validating("a bc"))
                    .hasToString("DefaultNameValidator(invalidChars=" + Arrays.toString(invalidChars.toCharArray()) + ")");

            for (char c : invalidChars.toCharArray()) {
                assertThat(x)
                        .is(invalidating(String.valueOf(c), "The name can't contain '" + c + "'"));
            }
        }
    }

    private static Condition<INameValidator> validating(String name) {
        return AllOf.allOf(
                new Condition<>(x -> x.accept(name), "expecting valid name"),
                new Condition<>(x -> x.getLastError() == null, "expecting no error message")
        );
    }

    private static Condition<INameValidator> invalidating(String name, String errorMessage) {
        return AllOf.allOf(
                new Condition<>(x -> !x.accept(name), "expecting invalid name"),
                new Condition<>(x -> x.getLastError().equals(errorMessage), "expecting an error message")
        );
    }
}