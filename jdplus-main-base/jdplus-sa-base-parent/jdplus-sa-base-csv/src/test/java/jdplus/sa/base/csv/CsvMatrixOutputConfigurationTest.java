package jdplus.sa.base.csv;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvMatrixOutputConfigurationTest {

    @Test
    public void defaultValuesAreCorrectlySet() {
        var config = new CsvMatrixOutputConfiguration();

        assertThat(config.getItems()).isEmpty();
        assertThat(config.getFolder()).isNull();
        assertThat(config.getFileName()).isEqualTo("demetra_m");
        assertThat(config.isFullName()).isTrue();
        assertThat(config.isShortColumnName()).isFalse();
        assertThat(config.getCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
    }

    @Test
    public void cloneCreatesIndependentCopy() {
        var original = new CsvMatrixOutputConfiguration();
        original.setItems(List.of("a", "b", "c"));
        original.setFolder(Path.of("C:\\original").toFile());
        original.setFileName("originalFile");
        original.setFullName(false);
        original.setShortColumnName(false);
        original.setCharset(StandardCharsets.UTF_8);

        var cloned = original.clone();

        assertThat(cloned).isNotNull();
        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getItems()).isEqualTo(original.getItems());
        assertThat(cloned.getFolder()).isEqualTo(original.getFolder());
        assertThat(cloned.getFileName()).isEqualTo(original.getFileName());
        assertThat(cloned.isFullName()).isEqualTo(original.isFullName());
        assertThat(cloned.isShortColumnName()).isEqualTo(original.isShortColumnName());
        assertThat(cloned.getCharset()).isEqualTo(original.getCharset());
    }

    @Test
    public void cloneWithDefaultValues() {
        var original = new CsvMatrixOutputConfiguration();

        var cloned = original.clone();

        assertThat(cloned).isNotNull();
        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getItems()).isEmpty();
        assertThat(cloned.getFolder()).isNull();
        assertThat(cloned.getFileName()).isEqualTo("demetra_m");
        assertThat(cloned.isFullName()).isTrue();
        assertThat(cloned.isShortColumnName()).isFalse();
        assertThat(cloned.getCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
    }

    @Test
    public void modifyingClonedObjectDoesNotAffectOriginal() {
        var original = new CsvMatrixOutputConfiguration();
        original.setFileName("original");
        original.setFullName(true);

        var cloned = original.clone();
        assertThat(cloned).isNotNull();
        cloned.setFileName("cloned");
        cloned.setFullName(false);

        assertThat(original.getFileName()).isEqualTo("original");
        assertThat(original.isFullName()).isTrue();
        assertThat(cloned.getFileName()).isEqualTo("cloned");
        assertThat(cloned.isFullName()).isFalse();
    }
}

