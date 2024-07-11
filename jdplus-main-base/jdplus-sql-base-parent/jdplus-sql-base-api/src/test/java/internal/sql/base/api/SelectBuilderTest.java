package internal.sql.base.api;

import nbbrd.sql.jdbc.SqlIdentifierQuoter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SelectBuilderTest {

    @Test
    public void test() {
        assertThat(SelectBuilder.from("abc").build())
                .isEqualTo("SELECT * FROM abc");

        assertThat(SelectBuilder.from("abc").select("sector").build())
                .isEqualTo("SELECT sector FROM abc");

        assertThat(SelectBuilder.from("abc").distinct(true).select("sector").filter("method", "region").orderBy("sector").build())
                .isEqualTo("SELECT DISTINCT sector FROM abc WHERE method=? AND region=? ORDER BY sector");

        SqlIdentifierQuoter quoter = SqlIdentifierQuoter
                .builder()
                .sqlKeyword("ABC")
                .sqlKeyword("SECTOR")
                .sqlKeyword("REGION")
                .quoteString("'")
                .build();

        assertThat(SelectBuilder.from("abc").distinct(true).select("sector").filter("method", "region").orderBy("sector").withQuoter(quoter).build())
                .isEqualTo("SELECT DISTINCT 'sector' FROM 'abc' WHERE method=? AND 'region'=? ORDER BY 'sector'");
    }
}