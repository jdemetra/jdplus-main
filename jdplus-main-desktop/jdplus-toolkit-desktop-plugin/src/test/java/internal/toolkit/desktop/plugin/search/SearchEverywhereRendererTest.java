package internal.toolkit.desktop.plugin.search;

import static internal.toolkit.desktop.plugin.search.SearchEverywhereRenderer.highlight;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SearchEverywhereRendererTest {

    @Test
    void highlightsFirstMatchIgnoringCaseAndOuterSpaces() {
        assertThat(highlight("Quarterly series", " SERIES ")).isEqualTo("<html>Quarterly <b>series</b>");
        assertThat(highlight("abcabc", "b")).isEqualTo("<html>a<b>b</b>cabc");
    }

    @Test
    void escapesHtmlAndIgnoresMissingOrBlankQuery() {
        assertThat(highlight("<a & b>", "&")).isEqualTo("<html>&lt;a <b>&amp;</b> b&gt;");
        assertThat(highlight("<a>", "z")).isEqualTo("<html>&lt;a&gt;");
        assertThat(highlight("<a>", " ")).isEqualTo("<html>&lt;a&gt;");
    }
}
