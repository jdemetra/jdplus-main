package jdplus.toolkit.base.api.timeseries;

import _util.MockedTsProvider;
import jdplus.toolkit.base.api.data.DoubleSeq;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static jdplus.toolkit.base.api.timeseries.TsInformationType.BaseInformation;
import static jdplus.toolkit.base.api.timeseries.TsInformationType.Data;
import static org.assertj.core.api.Assertions.*;

public class TsCollectionTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException()
                .isThrownBy(() -> TsCollection.of((Ts) null));

        assertThatNullPointerException()
                .isThrownBy(() -> TsCollection.of((Iterable<Ts>) null));

        Ts ts = Ts.of(TsData.empty("abc"));
        TsCollection col = TsCollection.of(ts);

        assertThat(TsCollection.of(col)).isEqualTo(col);
    }

    @Test
    public void testToTsCollection() {
        assertThat(Stream.<Ts>empty().collect(TsCollection.toTsCollection()))
                .isEqualTo(TsCollection.EMPTY);

        List<Ts> list = IntStream.range(0, 100).mapToObj(i -> Ts.builder().name("ts" + i).build()).toList();

        assertThat(list.stream().collect(TsCollection.toTsCollection()))
                .isEqualTo(TsCollection.of(list));

        assertThat(list.parallelStream().collect(TsCollection.toTsCollection()))
                .isEqualTo(TsCollection.of(list));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testLoadOfProvided() {
        TsCollection provided = factory.makeTsCollection(colMoniker, BaseInformation);

        assertThatNullPointerException()
                .isThrownBy(() -> provided.load(null, factory));

        assertThatNullPointerException()
                .isThrownBy(() -> provided.load(Data, null));

        for (TsInformationType info : TsInformationType.values()) {
            if (provided.getType().encompass(info)) {
                assertThat(provided.load(info, factory))
                        .describedAs("Provided collection can be modified by 'load' if old type encompasses new one")
                        .isSameAs(provided);
            } else {
                assertThat(provided.load(info, factory))
                        .describedAs("Provided collection must not be modified by 'load' if old type doesn't not encompass new one")
                        .isNotSameAs(provided);
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testLoadOfAnonymous() {
        TsCollection anonymous = TsCollection.of(Ts.of(TsData.empty("abc")));

        assertThatNullPointerException()
                .isThrownBy(() -> anonymous.load(null, factory));

        for (TsInformationType info : TsInformationType.values()) {
            assertThatNullPointerException()
                    .isThrownBy(() -> anonymous.load(info, null));
            assertThat(anonymous.load(info, factory))
                    .describedAs("Anonymous collection must not be modified by 'load'")
                    .isEqualTo(anonymous);
        }
    }

    @Test
    public void testToList() {
        TsCollection col = Stream.of(
                Ts.builder().name("t1").build(),
                Ts.builder().name("t2").build()
        ).collect(TsCollection.toTsCollection());

        assertThat(col.toList()).containsExactlyElementsOf(col.getItems());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testReplaceAll() {
        assertThatNullPointerException()
                .isThrownBy(() -> TsCollection.of(List.of()).replaceAll(null));

        assertThat(TsCollection.of(List.of()).replaceAll(TsCollection.of(ts1b)))
                .isEmpty();

        assertThat(TsCollection.of(List.of(ts1a)).replaceAll(TsCollection.of(ts1b)))
                .containsExactly(ts1b);

        assertThat(TsCollection.of(List.of(ts1b)).replaceAll(TsCollection.of(ts1b)))
                .containsExactly(ts1b);

        assertThat(TsCollection.of(List.of(ts2)).replaceAll(TsCollection.of(ts1b)))
                .containsExactly(ts2);

        assertThat(TsCollection.of(List.of(ts1a, ts2)).replaceAll(TsCollection.of(ts1b)))
                .containsExactly(ts1b, ts2);

        assertThat(TsCollection.of(List.of(ts2, ts1a)).replaceAll(TsCollection.of(ts1b)))
                .containsExactly(ts2, ts1b);
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testOfName() {
        assertThatNullPointerException()
                .isThrownBy(() -> TsCollection.ofName(null));

        assertThatObject(TsCollection.ofName(""))
                .returns("", TsCollection::getName);
    }

    private final Ts ts1a = Ts.builder().name("ts1a").moniker(TsMoniker.of(MockedTsProvider.NAME, "0:1")).build();
    private final Ts ts1b = ts1a.toBuilder().name("ts1b").build();
    private final Ts ts2 = Ts.builder().name("ts2").moniker(TsMoniker.of(MockedTsProvider.NAME, "0:2")).build();

    private final TsMoniker colMoniker = TsMoniker.of(MockedTsProvider.NAME, "0");

    private final List<TsProvider> providers = Collections.singletonList(
            MockedTsProvider
                    .builder()
                    .tsCollection(TsCollection.builder().moniker(colMoniker).build())
                    .build()
    );

    @Test
    public void testGetDomain() {
        assertThat(TsCollection.EMPTY.getDomain())
                .describedAs("No time series")
                .isEqualTo(TsDomain.DEFAULT_EMPTY);

        assertThat(TsCollection.of(Ts.of(TsData.of(TsPeriod.monthly(2010, 2), DoubleSeq.empty()))).getDomain())
                .describedAs("Empty time series")
                .isEqualTo(TsDomain.DEFAULT_EMPTY);

        assertThat(TsCollection.of(Ts.of(TsData.of(TsPeriod.monthly(2010, 2), DoubleSeq.of(3.14)))).getDomain())
                .describedAs("Non empty time series")
                .hasToString("R1/2010-02-01T00:00:00/P1M");
    }

    private final TsFactory factory = TsFactory.of(providers);
}
