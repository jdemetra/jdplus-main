/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.tramoseats.base.api.tramoseats;

import jdplus.sa.base.api.SaDictionaries;
import jdplus.tramoseats.base.api.seats.SeatsDictionaries;
import jdplus.toolkit.base.api.dictionaries.ComplexDictionary;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.dictionaries.PrefixedDictionary;
import jdplus.toolkit.base.api.dictionaries.RegArimaDictionaries;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TramoSeatsDictionaries {

    // Decomposition
    public final String SEATS = SaDictionaries.DECOMPOSITION;

    // finals
    public final String FINAL = "";
     
    public final Dictionary TRAMOSEATSDICTIONARY=ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(null, RegArimaDictionaries.REGSARIMA))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.REGEFFECTS))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.SADECOMPOSITION))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.SADECOMPOSITION_F))
            .dictionary(new PrefixedDictionary(SEATS, SeatsDictionaries.LINDECOMPOSITION))
            .dictionary(new PrefixedDictionary(SEATS, SaDictionaries.CMPDECOMPOSITION))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, SaDictionaries.COMBINEDSEASONALITY))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, SaDictionaries.GENERICSEASONALITY))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, SaDictionaries.GENERICTRADINGDAYS))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, RegArimaDictionaries.REGSARIMA_DIAGNOSTICS))
            .dictionary(new PrefixedDictionary(SaDictionaries.VARIANCE, SaDictionaries.VAR_DECOMPOSITION))
            .dictionary(new PrefixedDictionary(SaDictionaries.QUALITY, SaDictionaries.SA_QUALITY))
            .dictionary(new PrefixedDictionary(SaDictionaries.BENCHMARKING, SaDictionaries.SABENCHMARKING))
            .build();

     
}
