/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.vipcore.exception.AgencyNotFoundException;
import dk.dbc.vipcore.exception.ErrorInRequestException;
import dk.dbc.vipcore.exception.VipCoreException;
import dk.dbc.vipcore.libraryrules.VipCoreLibraryRulesConnector;
import dk.dbc.vipcore.marshallers.LibraryRule;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class VipCoreLibraryRulesConnectorTest {

    private static WireMockServer wireMockServer;
    private static String wireMockHost;

    final static Client CLIENT = HttpClient.newClient(new ClientConfig()
            .register(new JacksonFeature()));
    static VipCoreLibraryRulesConnector connector;

    @BeforeAll
    static void startWireMockServer() {
        wireMockServer = new WireMockServer(options().dynamicPort()
                .dynamicHttpsPort());
        wireMockServer.start();
        wireMockHost = "http://localhost:" + wireMockServer.port();
        configureFor("localhost", wireMockServer.port());
    }

    @BeforeAll
    static void setConnector() {
        connector = new VipCoreLibraryRulesConnector(CLIENT, wireMockHost, 0, VipCoreConnector.TimingLogLevel.INFO);
    }

    @AfterAll
    static void stopWireMockServer() {
        wireMockServer.stop();
    }

    @Test
    void isAllowedTest() throws VipCoreException {
        assertThat(connector.hasFeature("010100", VipCoreLibraryRulesConnector.Rule.AUTH_ROOT), is(true));
        assertThat(connector.hasFeature("710100", VipCoreLibraryRulesConnector.Rule.AUTH_ROOT), is(false));
        assertThat(connector.hasFeature("710100", VipCoreLibraryRulesConnector.Rule.USE_ENRICHMENTS), is(true));
    }

    @Test
    void getAllowedLibraryRulesTest() throws VipCoreException {
        assertThat(connector.getAllowedLibraryRules("010100"), is(new HashSet<>(
                        Arrays.asList(
                                "use_central_faust",
                                "auth_dbc_records",
                                "auth_public_lib_common_record",
                                "auth_common_notes",
                                "auth_agency_common_record",
                                "create_enrichments",
                                "auth_common_subjects",
                                "auth_root",
                                "auth_ret_record",
                                "auth_create_common_record",
                                "auth_add_dk5_to_phd")
                )
        ));

        assertThat(connector.getAllowedLibraryRules("710100"), is(new HashSet<String>() {
            /**
            *
            */
            private static final long serialVersionUID = 177297076452796083L;

            {
            add("auth_common_notes");
            add("auth_agency_common_record");
            add("use_localdata_stream");
            add("create_enrichments");
            add("use_enrichments");
            add("use_central_faust");
            add("auth_public_lib_common_record");
            add("use_holdings_item");
            add("auth_common_subjects");
            add("auth_export_holdings");
            add("auth_ret_record");
            add("auth_create_common_record");
            add("auth_add_dk5_to_phd");
        }}));
    }

    @Test
    void getLibraryRulesByAgencyIdTest() throws VipCoreException {
        final List<LibraryRule> expectedLibraryRuleList010100 = new ArrayList<>();
        expectedLibraryRuleList010100.add(createLibraryRule("create_enrichments", true, null));
        expectedLibraryRuleList010100.add(createLibraryRule("use_enrichments", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("auth_root", true, null));
        expectedLibraryRuleList010100.add(createLibraryRule("auth_common_subjects", true, null));
        expectedLibraryRuleList010100.add(createLibraryRule("auth_common_notes", true, null));
        expectedLibraryRuleList010100.add(createLibraryRule("auth_dbc_records", true, null));
        expectedLibraryRuleList010100.add(createLibraryRule("auth_public_lib_common_record", true, null));
        expectedLibraryRuleList010100.add(createLibraryRule("auth_ret_record", true, null));
        expectedLibraryRuleList010100.add(createLibraryRule("auth_agency_common_record", true, null));
        expectedLibraryRuleList010100.add(createLibraryRule("auth_export_holdings", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("use_localdata_stream", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("use_holdings_item", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("part_of_bibliotek_dk", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("auth_create_common_record", true, null));
        expectedLibraryRuleList010100.add(createLibraryRule("ims_library", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("worldcat_synchronize", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("worldcat_resource_sharing", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("cataloging_template_set", null, "dbc"));
        expectedLibraryRuleList010100.add(createLibraryRule("part_of_danbib", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("auth_add_dk5_to_phd", true, null));
        expectedLibraryRuleList010100.add(createLibraryRule("auth_metacompass", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("view_metacompass", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("use_central_faust", true, null));

        assertThat(connector.getLibraryRulesByAgencyId("010100"), is(expectedLibraryRuleList010100));

        final List<LibraryRule> expectedLibraryRuleList710100 = new ArrayList<>();
        expectedLibraryRuleList710100.add(createLibraryRule("create_enrichments", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("use_enrichments", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_root", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_common_subjects", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_common_notes", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_dbc_records", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_public_lib_common_record", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_ret_record", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_agency_common_record", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_export_holdings", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("use_localdata_stream", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("use_holdings_item", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("part_of_bibliotek_dk", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_create_common_record", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("ims_library", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("worldcat_synchronize", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("worldcat_resource_sharing", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("cataloging_template_set", null, "fbs"));
        expectedLibraryRuleList710100.add(createLibraryRule("part_of_danbib", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_add_dk5_to_phd", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_metacompass", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("view_metacompass", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("use_central_faust", true, null));

        assertThat(connector.getLibraryRulesByAgencyId("710100"), is(expectedLibraryRuleList710100));
    }

    @Test
    void getLibrariesByLibraryRuleBooleanTest() throws VipCoreException {
        assertThat(connector.getLibrariesByLibraryRule(VipCoreLibraryRulesConnector.Rule.USE_ENRICHMENTS, true), is(new HashSet<>(
                Arrays.asList(
                        "561884", "516950", "562739", "563949", "133030", "131090", "581000", "860960", "133150",
                        "556160", "562980", "510164", "560683", "510165", "753000", "510167", "563715", "948200",
                        "500002", "500243", "861700", "133160", "546120", "133040", "562992", "510073", "515766",
                        "562994", "563962", "546107", "777300", "725900", "503302", "871890", "503301", "500031",
                        "503304", "503303", "500278", "503300", "133170", "133050", "792615", "562882", "861970",
                        "582130", "900006", "831260", "900004", "562764", "900003", "562525", "561437", "747900",
                        "872960", "741000", "575712", "133180", "515988", "133060", "547310", "861720", "133100",
                        "775100", "571520", "740000", "500296", "550540", "526566", "763000", "131170", "133110",
                        "131050", "774000", "503309", "562944", "871930", "503306", "501007", "503305", "503308",
                        "510083", "784900", "503307", "546160", "718300", "766500", "721700", "503311", "503310",
                        "131060", "131180", "133120", "860810", "733600", "131070", "133130", "131190", "133010",
                        "546153", "732600", "751000", "782500", "774100", "778700", "546140", "739000", "502005",
                        "574700", "500068", "133140", "133020", "131080", "779100", "756100", "721000", "682105",
                        "523530", "744000", "500650", "772700", "861430", "562903", "533100", "522330", "710100",
                        "714700", "733000", "737600", "861560", "861680", "514710", "533310", "760700", "874400",
                        "500790", "767100", "500794", "500430", "500793", "300185", "715700", "514702", "732000",
                        "300183", "300187", "561950", "757300", "536710", "755000", "618799", "572720", "533320",
                        "874500", "874510", "526910", "300190", "581310", "716900", "568110", "300159", "515940",
                        "300157", "754000", "872630", "133190", "786000", "500455", "719000", "300163", "300161",
                        "133070", "300167", "510129", "770600", "300165", "538951", "300169", "575180", "720100",
                        "133080", "743000", "766100", "300173", "545110", "300175", "300259", "632904", "617314",
                        "133090", "742000", "559977", "300260", "715900", "300265", "770700", "000300", "300269",
                        "776000", "300147", "757500", "510144", "944500", "726900", "500698", "533120", "722300",
                        "870110", "300390", "556699", "300151", "300270", "300155", "300153", "791615", "300479",
                        "748200", "758000", "500804", "521990", "300360", "774600", "782000", "300240", "300482",
                        "300480", "517355", "874450", "300250", "300492", "300370", "300376", "300253", "300336",
                        "300575", "300219", "131300", "300217", "517380", "532610", "874240", "300580", "300101",
                        "715300", "300340", "300461", "300223", "724000", "861170", "761500", "550164", "575158",
                        "735000", "300350", "300230", "716500", "861340", "861580", "300316", "536920", "860490",
                        "530022", "746100", "530023", "530020", "530021", "530019", "530015", "734000", "547940",
                        "300320", "300563", "300440", "300561", "561176", "300201", "300329", "300326", "861590",
                        "534510", "575101", "873200", "530028", "874530", "530026", "521320", "530027", "530024",
                        "530025", "300210", "300573", "300450", "300330", "300410", "300773", "300411", "300779",
                        "547913", "563006", "300657", "547910", "563129", "725300", "723000", "575112", "552148",
                        "552147", "874320", "575105", "556623", "874560", "784000", "521530", "561190", "300540",
                        "300661", "561073", "300420", "715500", "300665", "300787", "300306", "745000", "726500",
                        "873220", "785100", "500981", "507015", "543310", "300430", "716700", "300791", "300550",
                        "300671", "731600", "300630", "300751", "131120", "300510", "773000", "781300", "133300",
                        "300756", "717300", "503000", "131130", "300760", "726000", "131010", "300400", "300766",
                        "521911", "718500", "573120", "721900", "551540", "762100", "131020", "131140", "300530",
                        "727000", "543910", "300730", "300851", "133200", "300615", "530710", "573130", "585107",
                        "776600", "875170", "562482", "131030", "300860", "585104", "131150", "585101", "562120",
                        "300621", "300740", "300741", "300746", "784600", "537360", "765700", "503015", "716100",
                        "546180", "615718", "502041", "131040", "563221", "131160", "565760", "131200", "300710",
                        "748000", "725000", "551000", "550022", "616916", "717500", "300840", "562663", "732900",
                        "679308", "300846", "562786", "643111", "300607", "300849", "300727", "531510", "737000",
                        "777900", "775600", "300970", "586620", "718700", "566910", "131100", "562672", "300813",
                        "300810", "565701", "781000", "771000", "520950", "520710", "715100", "505004", "736000",
                        "505002", "554600", "875370", "730600", "541523", "300820", "562562", "131110", "749200",
                        "300706", "300707", "300825", "521930", "716300")
        )));

        assertThat(connector.getLibrariesByLibraryRule(VipCoreLibraryRulesConnector.Rule.AUTH_ROOT, true), is(new HashSet<>(
                Collections.singletonList("010100")
        )));
    }

    @Test
    void getLibrariesByLibraryRuleStringTest() throws VipCoreException {
        assertThat(connector.getLibrariesByLibraryRule(VipCoreLibraryRulesConnector.Rule.CATALOGING_TEMPLATE_SET, "dbc"), is(new HashSet<>(
                Collections.singletonList("010100")
        )));

        assertThat(connector.getLibrariesByLibraryRule(VipCoreLibraryRulesConnector.Rule.CATALOGING_TEMPLATE_SET, "fbs"), is(new HashSet<>(
                Arrays.asList(
                        "561884", "516950", "871740", "562739", "563949", "133030", "131090", "581000", "860960",
                        "133150", "556160", "562980", "510164", "560683", "510165", "753000", "510167", "563715",
                        "948200", "500002", "500243", "861700", "133160", "546120", "133040", "562992", "510073",
                        "515766", "562994", "563962", "546107", "777300", "725900", "503302", "871890", "503301",
                        "500031", "503304", "503303", "500278", "503300", "133170", "133050", "792615", "562882",
                        "861970", "582130", "900006", "831260", "900004", "562764", "900003", "562525", "561437",
                        "747900", "872960", "741000", "575712", "133180", "515988", "133060", "547310", "861720",
                        "133100", "775100", "571520", "740000", "500296", "550540", "526566", "763000", "131170",
                        "133110", "875190", "131050", "774000", "503309", "562944", "871930", "503306", "501007",
                        "503305", "503308", "510083", "784900", "503307", "546160", "718300", "766500", "721700",
                        "503311", "503310", "131060", "131180", "133120", "860810", "733600", "131070", "133130",
                        "131190", "133010", "546153", "732600", "751000", "782500", "774100", "778700", "546140",
                        "739000", "502005", "574700", "500068", "133140", "133020", "131080", "779100", "756100",
                        "721000", "682105", "523530", "744000", "500650", "772700", "861430", "861790", "562903",
                        "533100", "872300", "872310", "522330", "874970", "710100", "714700", "733000", "737600",
                        "861560", "861680", "514710", "533310", "760700", "852730", "874400", "500790", "767100",
                        "500794", "500430", "500793", "300185", "715700", "514702", "732000", "300183", "300187",
                        "561950", "757300", "536710", "755000", "618799", "572720", "533320", "874500", "873300",
                        "874510", "526910", "300190", "581310", "716900", "568110", "300159", "515940", "300157",
                        "754000", "872510", "872630", "133190", "786000", "872760", "500455", "719000", "300163",
                        "300161", "133070", "300167", "510129", "770600", "300165", "538951", "300169", "575180",
                        "720100", "133080", "743000", "766100", "300173", "545110", "300175", "300259", "632904",
                        "700400", "617314", "874710", "872780", "873630", "874840", "133090", "742000", "559977",
                        "300260", "715900", "300265", "770700", "000300", "300269", "776000", "300147", "757500",
                        "510144", "944500", "726900", "500698", "533120", "722300", "872770", "872530", "870110",
                        "300390", "556699", "300151", "300270", "300155", "300153", "791615", "300479", "748200",
                        "758000", "500804", "875310", "521990", "874340", "872160", "874460", "300360", "774600",
                        "782000", "300240", "300482", "872280", "300480", "517355", "872150", "874450", "874330",
                        "874570", "300250", "300492", "300370", "300376", "300253", "300336", "300575", "300219",
                        "131300", "300217", "517380", "532610", "874240", "300580", "300101", "715300", "300340",
                        "300461", "300223", "724000", "861170", "761500", "550164", "575158", "875320", "735000",
                        "300350", "300230", "716500", "861340", "861580", "300316", "536920", "860490", "530022",
                        "746100", "530023", "530020", "530021", "852750", "530019", "530015", "734000", "547940",
                        "300320", "300563", "300440", "300561", "561176", "300201", "861110", "300329", "861470",
                        "300326", "861590", "534510", "852760", "575101", "873200", "530028", "874650", "874530",
                        "530026", "521320", "530027", "530024", "530025", "300210", "300573", "300450", "300330",
                        "790900", "300410", "300773", "300411", "300779", "547913", "563006", "300657", "547910",
                        "563129", "725300", "723000", "575112", "552148", "552147", "874320", "575105", "556623",
                        "872260", "873350", "874560", "784000", "521530", "561190", "300540", "300661", "561073",
                        "300420", "715500", "300665", "300787", "300306", "745000", "726500", "875400", "873220",
                        "785100", "873340", "874550", "500981", "507015", "872250", "543310", "300430", "716700",
                        "300791", "300550", "300671", "731600", "300630", "300751", "131120", "300510", "773000",
                        "781300", "133300", "300756", "862190", "717300", "503000", "875030", "875150", "874180",
                        "131130", "300760", "726000", "131010", "300400", "300766", "521911", "718500", "573120",
                        "721900", "551540", "762100", "131020", "131140", "300530", "727000", "543910", "300730",
                        "300851", "133200", "300615", "530710", "573130", "585107", "776600", "875170", "562482",
                        "131030", "300860", "585104", "874080", "131150", "585101", "562120", "300621", "300740",
                        "300741", "300746", "784600", "537360", "765700", "503015", "716100", "546180", "615718",
                        "502041", "131040", "563221", "131160", "565760", "131200", "300710", "748000", "725000",
                        "551000", "550022", "616916", "717500", "300840", "562663", "732900", "679308", "300846",
                        "562786", "643111", "300607", "300849", "300727", "875340", "531510", "737000", "777900",
                        "874370", "775600", "300970", "586620", "718700", "566910", "131100", "562672", "300813",
                        "300810", "565701", "781000", "771000", "520950", "520710", "715100", "505004", "736000",
                        "505002", "554600", "875370", "730600", "541523", "300820", "562562", "131110", "749200",
                        "300706", "300707", "300825", "521930", "716300"
                )
        )));

        assertThat(connector.getLibrariesByLibraryRule(VipCoreLibraryRulesConnector.Rule.CATALOGING_TEMPLATE_SET, "julemanden"), is(new HashSet<>(
                Collections.emptyList()
        )));
    }

    @Test
    void getAllowedLibraryRules_testForErrors() {
        Assertions.assertThrows(AgencyNotFoundException.class, () -> connector.getAllowedLibraryRules("000000"), "agency_not_found");
        Assertions.assertThrows(ErrorInRequestException.class, () -> connector.getAllowedLibraryRules("sdfsdf"), "error_in_request");
    }

    @Test
    void getLibraryRulesByAgencyId_testForErrors() {
        Assertions.assertThrows(AgencyNotFoundException.class, () -> connector.getLibraryRulesByAgencyId("000000"), "agency_not_found");
        Assertions.assertThrows(ErrorInRequestException.class, () -> connector.getLibraryRulesByAgencyId("sdfsdf"), "error_in_request");
    }

    private LibraryRule createLibraryRule(String name, Boolean booleanValue, String stringValue) {
        LibraryRule libraryRule = new LibraryRule();
        libraryRule.setName(name);
        libraryRule.setBool(booleanValue);
        libraryRule.setString(stringValue);

        return libraryRule;
    }
}
