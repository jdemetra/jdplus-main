package jdplus.sql.base.api;

import _test.TableAsCubes.AllSeries;
import _test.TableAsCubes.AllSeriesWithData;
import _test.TableAsCubes.Series;
import _test.TableAsCubes.SeriesWithData;
import internal.sql.base.api.DefaultConnectionSource;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.tsp.cube.CubeId;
import jdplus.toolkit.base.tsp.cube.TableDataParams;
import org.assertj.core.util.DateUtil;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.Date;

import static _test.TableAsCubes.getChildren;
import static org.assertj.core.api.Assertions.*;

class SqlTableAsCubeResourceTest {

    @Test
    public void testCube0D() throws Exception {
        CubeId root = CubeId.root();

        SqlTableAsCubeResource x = SqlTableAsCubeResource
                .builder()
                .source(new DefaultConnectionSource(ignore -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""), "mydb"))
                .table("Table0")
                .root(root)
                .tdp(TableDataParams
                        .builder()
                        .periodColumn("Period")
                        .valueColumn("Rate")
                        .build())
                .gathering(ObsGathering.DEFAULT)
                .labelColumn("")
                .build();

        assertThat(x.getRoot()).isEqualTo(root);

        assertThat(x.getDisplayName()).isEqualTo("mydb ~ Table0 » Rate ");

        assertThat(x.getDisplayName(root)).isEqualTo("All");

        assertThat(x.getDisplayNodeName(root)).isEqualTo("All");

        assertThatCode(x::testConnection).doesNotThrowAnyException();

        assertThatIllegalArgumentException().isThrownBy(() -> getChildren(x, root));

        assertThatIllegalArgumentException().isThrownBy(() -> AllSeries.of(x, root));

        assertThatIllegalArgumentException().isThrownBy(() -> AllSeriesWithData.of(x, root));

        assertThat(Series.of(x, root)).containsOnly(
                Series.builder().build()
        );

        assertThat(SeriesWithData.of(x, root)).containsExactly(
                SeriesWithData.<Date>builder().value(1.2).period(JAN_2012).build(),
                SeriesWithData.<Date>builder().value(2.3).period(FEB_2012).build()
        );
    }

    @Test
    public void testCube2D() throws Exception {
        CubeId root = CubeId.root("Sector", "Region");
        CubeId node = root.child("Industry");
        CubeId leaf = root.child("Industry", "Belgium");

        SqlTableAsCubeResource x = SqlTableAsCubeResource
                .builder()
                .source(new DefaultConnectionSource(ignore -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""), "mydb"))
                .table("Table2")
                .root(root)
                .tdp(TableDataParams
                        .builder()
                        .periodColumn("Period")
                        .valueColumn("Rate")
                        .build())
                .gathering(ObsGathering.DEFAULT)
                .labelColumn("")
                .build();

        assertThat(x.getRoot()).isEqualTo(root);

        assertThat(x.getDisplayName()).isEqualTo("mydb ~ Table2 » Rate ");

        assertThat(x.getDisplayName(root)).isEqualTo("");
        assertThat(x.getDisplayName(node)).isEqualTo("Industry");
        assertThat(x.getDisplayName(leaf)).isEqualTo("Industry, Belgium");

        assertThat(x.getDisplayNodeName(root)).isEqualTo("");
        assertThat(x.getDisplayNodeName(node)).isEqualTo("Industry");
        assertThat(x.getDisplayNodeName(leaf)).isEqualTo("Belgium");

        assertThatCode(x::testConnection).doesNotThrowAnyException();

        assertThat(getChildren(x, root)).containsExactly("Industry", "Other");
        assertThat(getChildren(x, node)).containsExactly("Belgium", "Europe");
        assertThatIllegalArgumentException().isThrownBy(() -> getChildren(x, leaf));

        assertThat(AllSeries.of(x, root)).containsExactly(
                AllSeries.builder().dimensions(new String[]{"Industry", "Belgium"}).build(),
                AllSeries.builder().dimensions(new String[]{"Industry", "Europe"}).build(),
                AllSeries.builder().dimensions(new String[]{"Other", "Belgium"}).build(),
                AllSeries.builder().dimensions(new String[]{"Other", "Europe"}).build()
        );
        assertThat(AllSeries.of(x, node)).containsExactly(
                AllSeries.builder().dimensions(new String[]{"Belgium"}).build(),
                AllSeries.builder().dimensions(new String[]{"Europe"}).build()
        );
        assertThatIllegalArgumentException().isThrownBy(() -> AllSeries.of(x, leaf));

        assertThat(AllSeriesWithData.of(x, root)).containsExactly(
                AllSeriesWithData.<Date>builder().dimensions(new String[]{"Industry", "Belgium"}).value(1.2).period(JAN_2012).build(),
                AllSeriesWithData.<Date>builder().dimensions(new String[]{"Industry", "Belgium"}).value(2.3).period(FEB_2012).build(),
                AllSeriesWithData.<Date>builder().dimensions(new String[]{"Industry", "Europe"}).value(3.4).period(JAN_2012).build(),
                AllSeriesWithData.<Date>builder().dimensions(new String[]{"Industry", "Europe"}).value(4.5).period(FEB_2012).build(),
                AllSeriesWithData.<Date>builder().dimensions(new String[]{"Other", "Belgium"}).value(5.6).period(JAN_2012).build(),
                AllSeriesWithData.<Date>builder().dimensions(new String[]{"Other", "Belgium"}).value(6.7).period(FEB_2012).build(),
                AllSeriesWithData.<Date>builder().dimensions(new String[]{"Other", "Europe"}).value(7.8).period(JAN_2012).build(),
                AllSeriesWithData.<Date>builder().dimensions(new String[]{"Other", "Europe"}).value(8.9).period(FEB_2012).build()
        );
        assertThat(AllSeriesWithData.of(x, node)).containsExactly(
                AllSeriesWithData.<Date>builder().dimensions(new String[]{"Belgium"}).value(1.2).period(JAN_2012).build(),
                AllSeriesWithData.<Date>builder().dimensions(new String[]{"Belgium"}).value(2.3).period(FEB_2012).build(),
                AllSeriesWithData.<Date>builder().dimensions(new String[]{"Europe"}).value(3.4).period(JAN_2012).build(),
                AllSeriesWithData.<Date>builder().dimensions(new String[]{"Europe"}).value(4.5).period(FEB_2012).build()
        );
        assertThatIllegalArgumentException().isThrownBy(() -> AllSeriesWithData.of(x, leaf));

        assertThatIllegalArgumentException().isThrownBy(() -> Series.of(x, root));
        assertThatIllegalArgumentException().isThrownBy(() -> Series.of(x, node));
        assertThat(Series.of(x, leaf)).containsOnly(
                Series.builder().build()
        );

        assertThatIllegalArgumentException().isThrownBy(() -> SeriesWithData.of(x, root));
        assertThatIllegalArgumentException().isThrownBy(() -> SeriesWithData.of(x, node));
        assertThat(SeriesWithData.of(x, leaf)).containsExactly(
                SeriesWithData.<Date>builder().value(1.2).period(JAN_2012).build(),
                SeriesWithData.<Date>builder().value(2.3).period(FEB_2012).build()
        );
    }

    private static final Date JAN_2012 = DateUtil.parse("2012-01-01");
    private static final Date FEB_2012 = DateUtil.parse("2012-02-01");
}