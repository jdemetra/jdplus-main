/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.x13.base.api.x11;

import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.dictionaries.AtomicDictionary;
import jdplus.toolkit.base.api.dictionaries.Dictionary;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X11Dictionaries {


    public static final String B_TABLES="b-tables", C_TABLES="c-tables", D_TABLES="d-tables";

    public final String B1="b1", B2="b2", B3="b3", B4="b4", B5="b5",
        B6="b6", B7="b7", B8="b8", B9="b9", B10="b10", B11="b11", B13="b13", B17="b17", B20="b20";
    
    public final String[] B_TABLE = new String[]{B1, B2, B3, B4, B5,
        B6, B7, B8, B9, B10, B11, B13, B17, B20};
   
    public final Dictionary BTABLES = AtomicDictionary.builder()
            .name(B_TABLES)
            .item(AtomicDictionary.Item.builder().name(B1).description("Linearized Series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B2).description("Unmodified Trend preliminary estimation using composite moving average").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B3).description("Unmodified Seasonal-Irregular preliminary estimation").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B4).description("Replacement Values for Extreme Seasonal-Irregular Values").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B5).description("Seasonal Component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B6).description("Seasonally Adjusted Series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B7).description("Trend estimation using Henderson moving average").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B8).description("Unmodified Seasonal-Irregular Component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B9).description("Replacement Values for Extreme Seasonal-Irregular Values").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B10).description("Seasonal Component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B11).description("Seasonally Adjusted Series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B13).description("Irregular Component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B17).description("Preliminary Weights for the Irregular").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(B20).description("Adjustment Values for the original series B1, allows to compute C1").outputClass(TsData.class).build())
            .build();
    
    public final String C1="c1", C2="c2", C4="c4", C5="c5",
        C6="c6", C7="c7", C9="c9", C10="c10", C11="c11", C13="c13", C17="c17", C20="c20";
    
    public final String[] C_TABLE = new String[]{C1, C2, C4, C5,
        C6, C7, C9, C10, C11, C13, C17, C20};
   
    public final Dictionary CTABLES = AtomicDictionary.builder()
            .name(C_TABLES)
            .item(AtomicDictionary.Item.builder().name(C1).description("Modified Linearized Series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(C2).description("Trend preliminary estimation using composite moving average").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(C4).description("Modified Seasonal-Irregular Component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(C5).description("Seasonal Component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(C6).description("Seasonally Adjusted Series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(C7).description("Trend estimation using Henderson moving average").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(C9).description("Seasonal-Irregular Component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(C10).description("Seasonal Component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(C11).description("Seasonally Adjusted Series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(C13).description("Irregular Component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(C17).description("Final Weights for the Irregular").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(C20).description("Adjustment Values for the original series B1, allows to compute D1").outputClass(TsData.class).build())
            .build();

    public final String D1="d1", D2="d2", D4="d4",
        D5="d5", D6="d6", D7="d7", D8="d8", D9="d9", D10="d10",
            D11="d11", D12="d12", D13="d13";

    public final String[] D_TABLE = new String[]{D1, D2, D4,
        D5, D6, D7, D8, D9, D10, D11, D12, D13};
    
    public final Dictionary DTABLES = AtomicDictionary.builder()
            .name(D_TABLES)
            .item(AtomicDictionary.Item.builder().name(D1).description("Modified Linearized Series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(D2).description("Trend preliminary estimation using composite moving average").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(D4).description("Modified Seasonal-Irregular Component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(D5).description("Seasonal Component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(D6).description("Seasonally Adjusted Series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(D7).description("Trend estimation using Henderson moving average").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(D8).description("Unmodified Seasonal-Irregular Component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(D9).description("Replacement Values for Extreme Seasonal-Irregular Values").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(D10).description("Final Seasonal Component from the decomposition of the linearized series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(D11).description("Final Seasonally Adjusted Series obtained with D10 factors").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(D12).description("Final Trend Component from the decomposition of the linearized series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(D13).description("Final Irregular Component from the decomposition of the linearized series").outputClass(TsData.class).build())
            .build();
    
    public final String X11_ALL="x11-all", ICRATIO = "icratio", TRENDFILTER="trend-filter", D7_TRENDFILTER="d7-trend-filter", SEASONALFILTERS="seasonal-filters",
            D9_GLOBALMSR="d9-global-msr", D9_MSR = "d9-msr", D9_MSRTABLE ="d9-msr-table" ;

    public final Dictionary ADVANCED = AtomicDictionary.builder()
            .name("x11-advanced")
            .item(AtomicDictionary.Item.builder().name(X11_ALL).description("all X11 tables").outputClass(Matrix.class).build())
            .item(AtomicDictionary.Item.builder().name(ICRATIO).description("ic-ratio").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(TRENDFILTER).description("final henderson filter length").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(D7_TRENDFILTER).description("henderson filter length used to compute D7").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(SEASONALFILTERS).description("final seasonal filters").outputClass(String[].class).build())
            .item(AtomicDictionary.Item.builder().name(D9_GLOBALMSR).description("global moving seasonality ratio on D9").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(D9_MSR).description("moving seasonality ratio on D9").outputClass(double[].class).build())
            .item(AtomicDictionary.Item.builder().name(D9_MSRTABLE).description("moving seasonality on D9 (details)").outputClass(Matrix.class).build())
          .build();
    
}
