package jdplus.sa.base.csv;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvArrayOutputConfigurationTest {

    @Test
    public void defaultValuesAreCorrectlySet() {
        var config = new CsvArrayOutputConfiguration();

        assertThat(config.getPresentation()).isEqualTo(CsvLayout.List);
        assertThat(config.getFolder()).isNull();
        assertThat(config.getFilePrefix()).isEqualTo("v");
        assertThat(config.getArrays()).isEmpty();
        assertThat(config.isFullName()).isTrue();
        assertThat(config.getCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
    }

    @Test
    public void cloneCreatesIndependentCopy() {
        var original = new CsvArrayOutputConfiguration();
        original.setPresentation(CsvLayout.HTable);
        original.setFolder(Path.of("C:\\original").toFile());
        original.setFilePrefix("originalPrefix");
        original.setArrays(List.of("a", "b", "c"));
        original.setFullName(false);
        original.setCharset(StandardCharsets.UTF_8);

        var cloned = original.clone();

        assertThat(cloned).isNotNull();
        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getPresentation()).isEqualTo(original.getPresentation());
        assertThat(cloned.getFolder()).isEqualTo(original.getFolder());
        assertThat(cloned.getFilePrefix()).isEqualTo(original.getFilePrefix());
        assertThat(cloned.getArrays()).isEqualTo(original.getArrays());
        assertThat(cloned.isFullName()).isEqualTo(original.isFullName());
        assertThat(cloned.getCharset()).isEqualTo(original.getCharset());
    }

    @Test
    public void cloneWithDefaultValues() {
        var original = new CsvArrayOutputConfiguration();

        var cloned = original.clone();

        assertThat(cloned).isNotNull();
        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getPresentation()).isEqualTo(CsvLayout.List);
        assertThat(cloned.getFolder()).isNull();
        assertThat(cloned.getFilePrefix()).isEqualTo("v");
        assertThat(cloned.getArrays()).isEmpty();
        assertThat(cloned.isFullName()).isTrue();
        assertThat(cloned.getCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
    }

    @Test
    public void modifyingClonedObjectDoesNotAffectOriginal() {
        var original = new CsvArrayOutputConfiguration();
        original.setPresentation(CsvLayout.List);
        original.setFilePrefix("original");

        var cloned = original.clone();
        assertThat(cloned).isNotNull();
        cloned.setPresentation(CsvLayout.VTable);
        cloned.setFilePrefix("cloned");

        assertThat(original.getPresentation()).isEqualTo(CsvLayout.List);
        assertThat(original.getFilePrefix()).isEqualTo("original");
        assertThat(cloned.getPresentation()).isEqualTo(CsvLayout.VTable);
        assertThat(cloned.getFilePrefix()).isEqualTo("cloned");
    }
}

