package jdplus.sa.base.csv;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvOutputConfigurationTest {

    @Test
    public void defaultValuesAreCorrectlySet() {
        var config = new CsvOutputConfiguration();

        assertThat(config.getPresentation()).isEqualTo(CsvLayout.List);
        assertThat(config.getFolder()).isNull();
        assertThat(config.getFilePrefix()).isEqualTo("series");
        assertThat(config.getSeries()).containsExactly("y", "t", "sa", "s", "i", "ycal");
        assertThat(config.isFullName()).isTrue();
        assertThat(config.getCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
    }

    @Test
    public void cloneCreatesIndependentCopy() {
        var original = new CsvOutputConfiguration();
        original.setPresentation(CsvLayout.HTable);
        original.setFolder(Path.of("C:\\original").toFile());
        original.setFilePrefix("originalPrefix");
        original.setSeries(List.of("a", "b", "c"));
        original.setFullName(false);
        original.setCharset(StandardCharsets.UTF_8);

        var cloned = original.clone();

        assertThat(cloned).isNotNull();
        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getPresentation()).isEqualTo(original.getPresentation());
        assertThat(cloned.getFolder()).isEqualTo(original.getFolder());
        assertThat(cloned.getFilePrefix()).isEqualTo(original.getFilePrefix());
        assertThat(cloned.getSeries()).isEqualTo(original.getSeries());
        assertThat(cloned.isFullName()).isEqualTo(original.isFullName());
        assertThat(cloned.getCharset()).isEqualTo(original.getCharset());
    }

    @Test
    public void cloneWithDefaultValues() {
        var original = new CsvOutputConfiguration();

        var cloned = original.clone();

        assertThat(cloned).isNotNull();
        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getPresentation()).isEqualTo(CsvLayout.List);
        assertThat(cloned.getFolder()).isNull();
        assertThat(cloned.getFilePrefix()).isEqualTo("series");
        assertThat(cloned.getSeries()).containsExactly("y", "t", "sa", "s", "i", "ycal");
        assertThat(cloned.isFullName()).isTrue();
        assertThat(cloned.getCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
    }

    @Test
    public void modifyingClonedObjectDoesNotAffectOriginal() {
        var original = new CsvOutputConfiguration();
        original.setPresentation(CsvLayout.List);
        original.setFilePrefix("original");

        var cloned = original.clone();
        assertThat(cloned).isNotNull();
        cloned.setPresentation(CsvLayout.HTable);
        cloned.setFilePrefix("cloned");

        assertThat(original.getPresentation()).isEqualTo(CsvLayout.List);
        assertThat(original.getFilePrefix()).isEqualTo("original");
        assertThat(cloned.getPresentation()).isEqualTo(CsvLayout.HTable);
        assertThat(cloned.getFilePrefix()).isEqualTo("cloned");
    }
}










