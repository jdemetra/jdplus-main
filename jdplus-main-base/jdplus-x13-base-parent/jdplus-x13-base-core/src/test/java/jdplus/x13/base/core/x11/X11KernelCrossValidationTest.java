/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.x13.base.core.x11;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.x13.base.api.x11.BiasCorrection;
import jdplus.x13.base.api.x11.CalendarSigmaOption;
import jdplus.x13.base.api.x11.CrossValidationQualityCriteria;
import jdplus.x13.base.api.x11.CrossValidationSeasonalFilterOptions;
import jdplus.x13.base.api.x11.CrossValidationTable;
import jdplus.x13.base.api.x11.SeasonalFilterOption;
import jdplus.x13.base.api.x11.X11Spec;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Christiane Hofer
 */
public class X11KernelCrossValidationTest {

    private static final double[] WU5636 = {1.1608, 1.1208, 1.0883, 1.0704, 1.0628, 1.0378, 1.0353, 1.0604, 1.0501, 1.0706, 1.0338, 1.011, 1.0137, 0.9834, 0.9643, 0.947, 0.906, 0.9492, 0.9397, 0.9041, 0.8721, 0.8552, 0.8564, 0.8973, 0.9383, 0.9217, 0.9095, 0.892, 0.8742, 0.8532, 0.8607, 0.9005, 0.9111, 0.9059, 0.8883, 0.8924, 0.8833, 0.87, 0.8758, 0.8858, 0.917, 0.9554, 0.9922, 0.9778, 0.9808, 0.9811, 1.0014, 1.0183, 1.0622, 1.0773, 1.0807, 1.0848, 1.1582, 1.1663, 1.1372, 1.1139, 1.1222, 1.1692, 1.1702, 1.2286, 1.2613, 1.2646, 1.2262, 1.1985, 1.2007, 1.2138, 1.2266, 1.2176, 1.2218, 1.249, 1.2991, 1.3408, 1.3119, 1.3014, 1.3201, 1.2938, 1.2694, 1.2165, 1.2037, 1.2292, 1.2256, 1.2015, 1.1786, 1.1856, 1.2103, 1.1938, 1.202, 1.2271, 1.277, 1.265, 1.2684, 1.2811, 1.2727, 1.2611, 1.2881, 1.3213, 1.2999, 1.3074, 1.3242, 1.3516, 1.3511, 1.3419, 1.3716, 1.3622, 1.3896, 1.4227, 1.4684, 1.457, 1.4718, 1.4748, 1.5527, 1.5751, 1.5557, 1.5553, 1.577, 1.4975, 1.437, 1.3322, 1.2732, 1.3449, 1.3239, 1.2785, 1.305, 1.319, 1.365, 1.4016, 1.4088, 1.4268, 1.4562, 1.4816, 1.4914, 1.4614, 1.4272, 1.3686, 1.3569, 1.3406, 1.2565, 1.2209, 1.277, 1.2894, 1.3067, 1.3898, 1.3661, 1.322, 1.336, 1.3649, 1.3999, 1.4442, 1.4349, 1.4388, 1.4264, 1.4343, 1.377, 1.3706, 1.3556, 1.3179, 1.2905, 1.3224, 1.3201, 1.3162, 1.2789, 1.2526, 1.2288, 1.24, 1.2856, 1.2974, 1.2828, 1.3119, 1.3288, 1.3359, 1.2964, 1.3026, 1.2982, 1.3189, 1.308, 1.331, 1.3348, 1.3635, 1.3493, 1.3704};

    private static final double[] b3x1_s500_30y = {45.5456382377878, 45.7599055224076, 46.4427973717099, 48.3042351426612, 49.204533822578, 49.3938022088093, 49.0589621417206, 48.5170141365818, 46.9473142889712, 44.596551909103, 43.6362825697286, 41.8491630855428, 41.6791680542729, 43.3964814646522, 44.4144614059753, 45.7477207042256, 46.7097864453623, 48.9345884794239, 49.7303211561184, 49.0171330569198, 48.5520950290868, 47.8249413933696, 46.6293057823812, 44.5621466327587, 44.002150055594, 45.6370220942567, 47.2450664431815, 49.650115190332, 50.4408506459578, 51.5549552573402, 52.8347042450597, 53.8635521269663, 52.845351681361, 51.3251792965566, 51.1831715998264, 49.310421098473, 50.8363180136459, 52.1667023636694, 53.7207874274078, 57.1603245778366, 58.4029284428646, 59.3443734752612, 60.8524509010505, 61.5557732048137, 60.6239278513844, 58.9665473184069, 58.2916046527878, 55.3823094121724, 59.4040577675636, 60.1880199533328, 61.2364783482571, 65.2922738481899, 68.9930425174366, 71.6547315540323, 73.5083237843415, 73.8896543996427, 72.083936904452, 70.5244073967968, 70.8211019753995, 69.0238933103854, 78.1021692980324, 78.5736083068733, 79.7380751817594, 84.4471712252812, 89.9480365211447, 93.1882890797399, 94.5770472973265, 96.5785393558403, 97.5747867557415, 96.5285790105254, 95.4860983820336, 91.9952392203018, 108.301848279294, 106.511789555729, 105.62204749252, 109.96758105182, 114.164083062881, 115.911779541362, 117.85112526879, 123.333809548795, 124.962442216182, 122.781332925589, 122.955476616008, 120.579266386035, 150.084237150012, 147.072188722206, 140.428668784018, 140.079731231855, 142.421185754832, 142.857777327189, 141.675918054013, 142.755192725547, 142.940375656427, 140.015068752514, 136.613015513063, 128.61662544921, 163.950424938912, 157.599023244683, 152.595233283815, 150.550608058573, 147.647531637932, 143.69545933099, 140.211765127709, 137.88639385978, 136.274256927635, 133.462845415497, 131.8678259826, 125.878311412872, 165.226595008778, 159.360903478158, 155.822099862266, 155.243104832705, 152.06785752545, 148.530808251051, 144.626407541806, 142.53746469504, 138.663162271723, 133.6991993709, 131.133053474661, 124.314069005446, 158.374264306792, 156.477233816551, 153.423226582235, 152.622651176737, 150.920075751464, 145.825487422832, 140.575617653493, 139.409297911082, 137.913270031549, 133.716372678793, 130.39415904161, 124.440869723076, 149.876092585632, 148.859851632821, 147.852503506007, 146.959401789459, 145.821392132217, 144.247174420808, 140.049190268743, 136.428772076309, 133.788179185812, 130.959465903153, 129.808175652794, 127.416097638169, 151.78432566724, 152.28169158495, 152.915716837173, 152.389940429508, 151.484265368426, 150.8925245432, 147.683492174017, 146.822798403605, 146.005356878977, 141.713978193242, 137.396728753557, 133.712903258547, 153.445828761312, 151.583003918518, 149.373950996948, 147.37720916678, 147.241234633117, 147.356456861322, 146.045847982691, 143.583151021863, 139.347482261142, 135.333905116046, 133.619499481909, 131.294475201162, 151.057480275951, 151.403822801777, 149.558686330845, 147.648723401008, 145.657987879107, 141.838030400058, 137.646186104566, 136.512310169261, 135.93775112543, 135.147770487196, 132.792482878047, 129.042292475725, 143.375772892093, 141.511074771169, 143.179379901473, 146.098142878827, 144.290505732121, 141.238498245194, 137.796560193756, 134.356621133171, 131.715054040557, 127.912529292393, 125.778288872593, 126.468310200264, 142.858731839944, 139.70450018762, 137.459997132167, 136.756616804667, 136.002992465192, 133.546285190588, 132.048829889489, 132.748819149756, 131.974892301413, 129.868574134759, 127.688205341879, 124.703479357615, 139.36096581117, 138.296508788065, 137.41287364007, 136.245316682932, 133.938725832078, 132.621560872072, 131.980167794344, 130.975999382407, 130.118256816053, 130.161517986561, 128.98275356337, 124.900951139832, 138.925860114029, 140.837918019213, 142.304729423184, 141.67599866752, 140.527853438928, 141.335496965389, 142.032439242556, 140.74860080722, 138.405228465484, 137.454081417308, 135.51179157511, 130.970389086266, 146.549745626202, 144.017947743465, 142.681436825326, 141.167728438231, 141.615362374393, 141.504273254505, 138.411060604787, 134.623771462797, 132.897399630783, 133.295570596838, 131.152932996853, 127.391356072726, 144.579260973718, 145.465990273847, 147.008784670438, 147.015937009395, 145.658926760181, 142.987431010947, 140.641842350974, 137.581617195927, 136.146658263227, 136.739363287485, 136.636246701575, 133.657154652476, 153.410056103501, 153.313430191538, 153.011268885594, 151.026735502751, 148.439079460706, 144.616905979186, 140.923481255732, 139.353751227694, 136.395056070265, 133.246513157957, 131.826350818161, 130.395583659623, 148.654335301621, 146.135582715618, 144.420225205551, 141.620104157448, 139.346345025901, 139.500330304012, 139.569338944783, 137.546183351951, 134.708517608785, 133.407557788238, 130.913700866114, 128.531293382138, 147.792116745549, 144.86347793799, 142.126478278937, 139.951810239525, 139.39367734453, 138.679564647983, 137.654276020576, 135.083351612145, 130.121047105356, 127.866011270103, 128.076033056745, 124.536955261319, 140.757314393986, 138.036016542713, 135.755522547244, 133.62157381203, 131.707760368492, 129.81269057136, 128.72852376939, 125.703317193202, 121.523603415731, 119.520470319094, 117.984940697226, 116.870347680427, 136.095096456867, 135.210492368213, 132.249794940388, 129.976295949239, 127.484894496602, 126.872701563364, 125.929007435426, 125.036818089522, 124.870050134537, 124.258275971759, 121.34010758348, 119.522854375939, 138.606584415975, 138.75248107963, 138.27654769933, 136.795700512989, 134.166842889041, 131.935789755162, 130.462575544782, 130.045154166201, 128.479408647167, 125.678693234095, 121.888142778078, 117.597834176566, 134.373616631537, 134.650959194642, 132.411783479564, 130.336722026037, 130.166994024244, 128.681112754521, 126.830891949301, 126.955820734762, 124.330243157999, 120.731105249882, 119.357515930027, 119.023480386119, 135.024686724733, 132.058388807328, 129.927252918031, 127.480522601987, 125.405355883331, 124.274269141815, 121.451087205602, 119.020327674725, 119.042230108616, 118.952547857905, 117.35214821375, 114.643738759241, 130.20385794158, 128.560652157522, 127.620100282946, 126.956239962184, 125.299189680005, 122.468189455021, 119.542616678986, 119.985453388752, 118.833212352225, 114.630719328216, 110.979192867186, 108.723863437533, 123.812607525833, 122.270041945546, 121.393351580341, 120.789453170485, 119.219826911628, 116.499616240496, 113.714636469224, 114.181551840468, 113.066063118612, 109.003305427664, 105.498357123745, 103.35};

    private static double[] sqEVB_B3_b3x1_s500_30y = {1.06087724, 1.0535346, 1.0233931, 0.9873905, 0.96537003, 0.91454194, 0.95084782, 0.95602605, 0.96468854, 1.00773859, 1.03268073, 1.04691957, 1.0577604, 1.05387214, 1.0199027, 0.97752253, 0.95934421, 0.91150994, 0.95090892, 0.95838825, 0.96576684, 1.00437157, 1.03084593, 1.05218498, 1.06223119, 1.05125712, 1.02137854, 0.98380984, 0.9613496, 0.91313317, 0.94421232, 0.95530642, 0.96484285, 1.00150096, 1.02754156, 1.05084998, 1.05677457, 1.04816316, 1.02141226, 0.98690943, 0.96314512, 0.9147604, 0.97383939, 0.97046444, 0.97139806, 1.00732491, 1.02647037, 1.03369462, 1.03662559, 1.04154009, 1.02034111, 0.98248696, 0.96086965, 0.91166574, 1.01484777, 0.99866207, 0.98084602, 1.00702982, 1.02613331, 1.03122547, 1.03067516, 1.03039521, 1.00719421, 0.97047238, 0.94900296, 0.89935806, 1.04763058, 1.02298357, 0.99809145, 1.01202727, 1.02963892, 1.03145332, 1.02074016, 1.01070075, 0.99088853, 0.95988529, 0.93750456, 0.88801303, 1.07470925, 1.03997434, 1.01591529, 1.02486944, 1.02942843, 1.01919833, 1.00241095, 0.99809257, 0.98203483, 0.95049841, 0.9310189, 0.88672655, 1.09988204, 1.06772091, 1.03362585, 1.03266582, 1.03058864, 1.01367633, 0.99317297, 0.99372198, 0.9812618, 0.95012593, 0.92967438, 0.88524553, 1.10214533, 1.07821504, 1.04906938, 1.04081036, 1.03380126, 1.01480737, 0.98873639, 0.97685904, 0.96540621, 0.94097228, 0.9227509, 0.88254813, 1.0973532, 1.07408905, 1.05787926, 1.05045395, 1.03617878, 1.01894563, 0.99300721, 0.9774747, 0.96163243, 0.93553887, 0.91846025, 0.88515913, 1.08763798, 1.07075657, 1.05544526, 1.04778955, 1.0370829, 1.02100889, 0.99842572, 0.98900525, 0.97254657, 0.94398563, 0.92503076, 0.89401307, 1.06851756, 1.06324156, 1.05149861, 1.04352617, 1.03591847, 1.01823723, 0.99321551, 0.98064318, 0.96643802, 0.94623089, 0.9313916, 0.90607052, 1.05447042, 1.05102484, 1.05022034, 1.04960375, 1.03932898, 1.02297883, 0.99634913, 0.98112892, 0.97062263, 0.95144841, 0.93522811, 0.92023585, 1.05354246, 1.04169466, 1.03740618, 1.03696161, 1.03069721, 1.01992639, 1.00353968, 0.99349536, 0.97941979, 0.95590283, 0.93970194, 0.92738928, 1.05631712, 1.04815688, 1.03719825, 1.02805211, 1.01893903, 1.00587625, 0.99367096, 0.9884647, 0.97772545, 0.9652559, 0.95133807, 0.92706988, 1.04058058, 1.03929153, 1.04024531, 1.03889515, 1.02569868, 1.01227062, 0.99854909, 0.9865345, 0.97578879, 0.96667606, 0.95337396, 0.93231013, 1.04227021, 1.03196012, 1.03112339, 1.03146454, 1.02592314, 1.01742782, 1.00402437, 0.98966823, 0.97606045, 0.96456888, 0.94992891, 0.93164481, 1.04946446, 1.03974127, 1.03505813, 1.02899986, 1.02116547, 1.00874116, 0.99471124, 0.98092794, 0.97036924, 0.96608184, 0.9538634, 0.92714357, 1.04407617, 1.04503945, 1.04652654, 1.03838363, 1.02378633, 1.00997665, 0.99785231, 0.98420193, 0.9699996, 0.96364331, 0.95514995, 0.93120748, 1.05121423, 1.04479591, 1.04105947, 1.02859551, 1.01900284, 1.01432706, 1.00381154, 0.98848704, 0.97154466, 0.96347231, 0.94976893, 0.92861117, 1.05555368, 1.04373923, 1.03729001, 1.02670663, 1.0206519, 1.01513293, 1.00398727, 0.98341118, 0.96333376, 0.95834889, 0.95016558, 0.9278671, 1.05969221, 1.05196732, 1.04629516, 1.03672295, 1.02719302, 1.01248887, 0.99921334, 0.98175599, 0.95831909, 0.94773128, 0.94486061, 0.93074864, 1.06859708, 1.05892511, 1.04648451, 1.03049089, 1.01482222, 1.00532592, 0.99640631, 0.98348529, 0.96577082, 0.95382466, 0.93908529, 0.92722454, 1.06993861, 1.05873352, 1.04377847, 1.02726188, 1.01210181, 1.0059747, 1.00068718, 0.99116304, 0.97482593, 0.96289271, 0.94671625, 0.92489734, 1.06099013, 1.05298416, 1.04032245, 1.02791847, 1.02049468, 1.01053122, 1.0022762, 0.99357397, 0.97074807, 0.95303594, 0.94341287, 0.92863605, 1.06474688, 1.05605094, 1.04023704, 1.02476408, 1.01344569, 1.0049645, 0.99285, 0.98147491, 0.96847439, 0.95628053, 0.94184415, 0.93049406, 1.0695241, 1.05954654, 1.04671952, 1.03303503, 1.01597655, 1.0036535, 0.98914583, 0.98453942, 0.98044026, 0.96613203, 0.94305994, 0.92176548, 1.0558564, 1.05096612, 1.04278608, 1.03505108, 1.02537097, 1.00877347, 0.99231516, 0.99235437, 0.98298439, 0.96196113, 0.93997853, 0.92409927, 1.05873596, 1.05237917, 1.04307626, 1.03199326, 1.02091043, 1.00731878, 0.99090477, 0.98469676, 0.97716572, 0.96392478, 0.94538765, 0.92792045, 1.06161745, 1.05419428, 1.04370586, 1.03190586, 1.02072237, 1.0072685};

    @Test
    public void testProcess_CrossValidation_Default() {

        int period = 12;

        jdplus.toolkit.base.api.timeseries.TsData tsData = jdplus.toolkit.base.api.timeseries.TsData.ofInternal(TsPeriod.of(TsUnit.ofAnnualFrequency(period), 0), b3x1_s500_30y);
        SeasonalFilterOption[] seasonalFilterOptions = new SeasonalFilterOption[period];
        for (int i = 0; i < period; i++) {
            seasonalFilterOptions[i] = SeasonalFilterOption.valueOf(SeasonalFilterOption.CrossValidation.name());
        }

        X11Spec spec = X11Spec.builder()
                .filters(seasonalFilterOptions)
                .build();

        X11Kernel instanceKernel = new X11Kernel();
        X11Results x11Results = instanceKernel.process(tsData, spec);
        org.junit.Assert.assertEquals("First CV", "{B3={CV Criteria=RMSE, CV Filter Options=[S3X3, S3X5, S3X9, S3X15], CrossValidation=S3X3, S3X15=0.021269569851035734, S3X3=0.01393256881405985, S3X5=0.014507053687038416, S3X9=0.01710222148606237}}", x11Results.getResultCV().toString());
    }

    @Test
    public void testProcess_CrossValidation_fromTable() {
        String modeName = DecompositionMode.Multiplicative.name();
        String seasonalFilterOptionName = SeasonalFilterOption.CrossValidation.name();
        int filterLength = 23;
        int period = 12;

        CrossValidationSeasonalFilterOptions cvsf = CrossValidationSeasonalFilterOptions.All;

        CrossValidationTable cvt = CrossValidationTable.C9;
        CrossValidationTable[] cvts = {
            CrossValidationTable.B3,
            CrossValidationTable.B4,
            CrossValidationTable.B8,
            CrossValidationTable.C4,
            CrossValidationTable.C9,
            CrossValidationTable.D4,
            CrossValidationTable.D8,
            CrossValidationTable.D9
        };
        CrossValidationQualityCriteria cvqc = CrossValidationQualityCriteria.RMSE;

        X11Results res = X11KernelProcess(modeName, seasonalFilterOptionName, filterLength, period, b3x1_s500_30y, CalendarSigmaOption.None.name(), 0, cvts, cvqc, cvsf, cvt);

    }

    @Test
    public void testProcess_CrossValidationb_3x1() {
        String modeName = DecompositionMode.Multiplicative.name();
        String seasonalFilterOptionName = SeasonalFilterOption.CrossValidation.name();
        int filterLength = 23;
        int period = 12;

        CrossValidationSeasonalFilterOptions cvsf = CrossValidationSeasonalFilterOptions.All;
        CrossValidationTable[] cvt = {CrossValidationTable.B3};
        CrossValidationQualityCriteria cvqc = CrossValidationQualityCriteria.RMSE;
        X11Results res = X11KernelProcess(modeName, seasonalFilterOptionName, filterLength, period, b3x1_s500_30y, CalendarSigmaOption.None.name(), 0, cvt, cvqc, cvsf);
        org.junit.Assert.assertEquals("First CV", "{B3={CV Criteria=RMSE, CV Filter Options=[S3X1, S3X3, S3X5, S3X9, S3X15], CrossValidation=S3X1, S3X1=0.013740494466518895, S3X15=0.021269569851035734, S3X3=0.01393256881405985, S3X5=0.014507053687038416, S3X9=0.01710222148606237}}", res.getResultCV().toString());
    }

    private List<List<String>> readCSV(String csvFile) {

        List<List<String>> data = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Path.of(csvFile))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] values = line.split("\t");
                List<String> lineData = Arrays.asList(values);
                data.add(lineData);
            }

        } catch (IOException e) {
            System.err.println("Error reading the CSV file: " + e.getMessage());
        }
        return data;
    }

    @Test
    /*
   Test to run lot of simmulated Data
     */
    public void testProcess_CrossValidation_SimData() throws IOException {

        List<List<String>> data = readCSV("src\\test\\java\\jdplus\\x13\\base\\core\\x11\\data\\ZR.csv");

        double[] values;
        String name;
        String resact = "";
        for (int i = 0; i < data.size(); i++) {
            List<String> row = data.get(i);
            name = row.get(0);

            values = new double[row.size() - 1];
            for (int j = 1; j < row.size(); j++) {
                values[j - 1] = Double.parseDouble(row.get(j));
            }

            String modeName = DecompositionMode.Additive.name();
            String seasonalFilterOptionName = SeasonalFilterOption.CrossValidation.name();
            int filterLength = 23;
            int frequency = 12;

            CrossValidationSeasonalFilterOptions cvsf = CrossValidationSeasonalFilterOptions.All;
            CrossValidationTable[] cvt = {
                CrossValidationTable.C4,
                CrossValidationTable.C9,
                CrossValidationTable.B3,
                CrossValidationTable.B4,
                CrossValidationTable.B8,
                CrossValidationTable.D8,
                CrossValidationTable.D9,
                CrossValidationTable.D4};
            CrossValidationQualityCriteria cvqc = CrossValidationQualityCriteria.RMSE;// nothing else is implemented

            X11Results res = X11KernelProcess(modeName, seasonalFilterOptionName, filterLength, frequency, values, CalendarSigmaOption.All.name(), 0, cvt, cvqc, cvsf, CrossValidationTable.B3);

            resact = res.getResultCV().toString();

        }
        String expected_last = "{B3={CV Criteria=RMSE, CV Filter Options=[S3X1, S3X3, S3X5, S3X9, S3X15], CrossValidation=S3X9, S3X1=1.2112671885894304, S3X15=1.193992752954624, S3X3=1.22078538708091, S3X5=1.2056287706161182, S3X9=1.192331640009903}}";
        org.junit.Assert.assertEquals("Sim_Data", expected_last, resact);
    }

    @Test
    public void testProcess_CrossValidation_allTables() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.CrossValidation.name();
        int filterLength = 13;
        int frequency = 12;

        CrossValidationSeasonalFilterOptions cvsf = CrossValidationSeasonalFilterOptions.All;
        CrossValidationTable[] cvt = {CrossValidationTable.C4, CrossValidationTable.C9, CrossValidationTable.B3, CrossValidationTable.B4, CrossValidationTable.B8, CrossValidationTable.D9, CrossValidationTable.D4}; //allowed for selection, B3 is used
        CrossValidationQualityCriteria cvqc = CrossValidationQualityCriteria.RMSE;

        X11Results res = X11KernelProcess(modeName, seasonalFilterOptionName, filterLength, frequency, WU5636, CalendarSigmaOption.None.name(), 0, cvt, cvqc, cvsf);
        String expected = "{B3={CV Criteria=RMSE, CV Filter Options=[S3X1, S3X3, S3X5, S3X9, S3X15], CrossValidation=S3X15, S3X1=0.047558228512589645, S3X15=0.038787930262633094, S3X3=0.04574271227553908, S3X5=0.04309471451448417, S3X9=0.04086832572959272}}";
        org.junit.Assert.assertEquals("allTables", expected, res.getResultCV().toString());
    }

    private void writeOutput(String nameTest, Map<CrossValidationTable, Map<String, String>> expectedResultsAll, Map<CrossValidationTable, Map<String, String>> resultsAll) {

        System.out.println(nameTest);
        System.out.println("Expected:");
        writeResultSet(expectedResultsAll);

        System.out.println("Result:");
        writeResultSet(resultsAll);
    }

    private void writeResultSetWithName(String name, Map<CrossValidationTable, Map<String, String>> resultSet) {
        for (Map.Entry<CrossValidationTable, Map<String, String>> entry : resultSet.entrySet()) {
            CrossValidationTable key = entry.getKey();
            Map<String, String> value = entry.getValue();
            System.out.println(name + ": " + key + " => " + value);
        }
    }

    private void writeResultSet(Map<CrossValidationTable, Map<String, String>> resultSet) {
        for (Map.Entry<CrossValidationTable, Map<String, String>> entry : resultSet.entrySet()) {
            CrossValidationTable key = entry.getKey();
            Map<String, String> value = entry.getValue();
            System.out.println(key + " => " + value);
        }
    }


    @Test
    public void testProcess_CrossValidation_default() {
        int frequency = 12;
        X11Kernel instanceKernel = new X11Kernel();
        jdplus.toolkit.base.api.timeseries.TsData tsData = jdplus.toolkit.base.api.timeseries.TsData.ofInternal(TsPeriod.of(TsUnit.ofAnnualFrequency(frequency), 0), WU5636);
        SeasonalFilterOption[] seasonalFilterOptions;
        seasonalFilterOptions = new SeasonalFilterOption[frequency];
        for (int i = 0; i < frequency; i++) {
            seasonalFilterOptions[i] = SeasonalFilterOption.valueOf(SeasonalFilterOption.CrossValidation.name());
        }

        X11Spec spec = X11Spec.builder()
                .filters(seasonalFilterOptions)
                .build();

        X11Results res = instanceKernel.process(tsData, spec);
        System.out.println(res.getResultCV().toString());
        String expected = "{B3={CV Criteria=RMSE, CV Filter Options=[S3X3, S3X5, S3X9, S3X15], CrossValidation=S3X15, S3X15=0.030906020004861257, S3X3=0.03659245114355146, S3X5=0.03450798788153551, S3X9=0.0326667940367354}}";

        org.junit.Assert.assertEquals("First CV", expected, res.getResultCV().toString());

    }

    @Test
    public void testProcess_CrossValidation_1() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.CrossValidation.name();
        int filterLength = 13;
        int frequency = 12;

        CrossValidationSeasonalFilterOptions cvsf = CrossValidationSeasonalFilterOptions.All;
        CrossValidationTable[] cvt = {CrossValidationTable.D9};
        CrossValidationQualityCriteria cvqc = CrossValidationQualityCriteria.RMSE;

        X11Results res = X11KernelProcess(modeName, seasonalFilterOptionName, filterLength, frequency, WU5636, CalendarSigmaOption.None.name(), 0, cvt, cvqc, cvsf);

        String expectedResultsAll = "{B3={CV Criteria=RMSE, CV Filter Options=[S3X1, S3X3, S3X5, S3X9, S3X15], CrossValidation=S3X15, S3X1=0.047558228512589645, S3X15=0.038787930262633094, S3X3=0.04574271227553908, S3X5=0.04309471451448417, S3X9=0.04086832572959272}}";
        org.junit.Assert.assertEquals("First CV", expectedResultsAll, res.getResultCV().toString());

    }

    private X11Results X11KernelProcess(String modeName, String seasonalFilterOptionName, int filterLength, int frequency, double[] values, String calendarSigma, int forecastHorizon, CrossValidationTable[] cvts, CrossValidationQualityCriteria cvqc, CrossValidationSeasonalFilterOptions cvfs) {
        return X11KernelProcess(modeName, seasonalFilterOptionName, filterLength, frequency, values, calendarSigma, forecastHorizon, cvts, cvqc, cvfs, CrossValidationTable.B3);
    }

    private X11Results X11KernelProcess(String modeName, String seasonalFilterOptionName, int filterLength, int frequency, double[] values, String calendarSigma, int forecastHorizon, CrossValidationTable[] cvts, CrossValidationQualityCriteria cvqc, CrossValidationSeasonalFilterOptions cvfs, CrossValidationTable cvt) {
        X11Kernel instanceKernel = new X11Kernel();
        jdplus.toolkit.base.api.timeseries.TsData tsData = jdplus.toolkit.base.api.timeseries.TsData.ofInternal(TsPeriod.of(TsUnit.ofAnnualFrequency(frequency), 0), values);
        SeasonalFilterOption[] seasonalFilterOptions = new SeasonalFilterOption[frequency];

        for (int i = 0; i < frequency; i++) {
            seasonalFilterOptions[i] = SeasonalFilterOption.valueOf(seasonalFilterOptionName);
        }

        X11Spec spec = X11Spec.builder()
                .mode(DecompositionMode.valueOf(modeName))
                .hendersonFilterLength(filterLength)
                .calendarSigma(CalendarSigmaOption.valueOf(calendarSigma))
                .filters(seasonalFilterOptions)
                .forecastHorizon(forecastHorizon)
                .bias(BiasCorrection.Legacy)
                .crossValidationQualityCriteria(cvqc)
                .crossValidationTables(cvts)
                .crossValidationSeasonalFilterOptions(cvfs)
                .crossValidationSelectionTable(cvt)
                .build();

        X11Results x11Results = instanceKernel.process(tsData, spec);

        return x11Results;
    }

}
