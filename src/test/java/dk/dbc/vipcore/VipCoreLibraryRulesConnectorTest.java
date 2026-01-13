package dk.dbc.vipcore;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.commons.useragent.UserAgent;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.vipcore.exception.AgencyNotFoundException;
import dk.dbc.vipcore.exception.ErrorInRequestException;
import dk.dbc.vipcore.exception.VipCoreException;
import dk.dbc.vipcore.libraryrules.VipCoreLibraryRulesConnector;
import dk.dbc.vipcore.marshallers.LibraryRule;
import dk.dbc.vipcore.marshallers.LibraryRules;
import dk.dbc.vipcore.marshallers.LibraryRulesRequest;
import jakarta.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class VipCoreLibraryRulesConnectorTest {

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
        final UserAgent userAgent = new UserAgent("VipCoreLibraryRulesConnectorTest");
        connector = new VipCoreLibraryRulesConnector(CLIENT, userAgent, wireMockHost, 0, VipCoreConnector.TimingLogLevel.INFO);
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
        assertThat(connector.hasFeature("710100", VipCoreLibraryRulesConnector.Rule.AUTH_VERA), is(false));
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
                                "view_metacompass",
                                "create_enrichments",
                                "auth_common_subjects",
                                "auth_root",
                                "auth_ret_record",
                                "auth_create_common_record",
                                "auth_add_dk5_to_phd")
                )
        ));

        assertThat(connector.getAllowedLibraryRules("710100"), is(new HashSet<>(
                Arrays.asList(
                        "ims_library",
                        "auth_common_notes",
                        "auth_agency_common_record",
                        "use_localdata_stream",
                        "part_of_requestorder",
                        "create_enrichments",
                        "use_enrichments",
                        "use_central_faust",
                        "auth_public_lib_common_record",
                        "use_holdings_item",
                        "auth_common_subjects",
                        "auth_export_holdings",
                        "auth_ret_record",
                        "auth_create_common_record",
                        "auth_add_dk5_to_phd"
                )
        )));
    }

    @Test
    void getLibraryRulesByAgencyIdTestDBC() throws VipCoreException {
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
        expectedLibraryRuleList010100.add(createLibraryRule("view_metacompass", true, null));
        expectedLibraryRuleList010100.add(createLibraryRule("use_central_faust", true, null));
        expectedLibraryRuleList010100.add(createLibraryRule("part_of_requestorder", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("regional_obligations", false, null));
        expectedLibraryRuleList010100.add(createLibraryRule("auth_vera", false, null));

        final LibraryRules expectedLibraryRules010100 = new LibraryRules();
        expectedLibraryRules010100.setLibraryRule(expectedLibraryRuleList010100);
        expectedLibraryRules010100.setAgencyId("010100");
        expectedLibraryRules010100.setAgencyType("Other");

        assertThat(connector.getLibraryRulesByAgencyId("010100"), is(expectedLibraryRules010100));
    }

    @Test
    void getLibraryRulesByAgencyIdTestOther() throws VipCoreException {
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
        expectedLibraryRuleList710100.add(createLibraryRule("ims_library", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("worldcat_synchronize", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("worldcat_resource_sharing", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("cataloging_template_set", null, "fbs"));
        expectedLibraryRuleList710100.add(createLibraryRule("part_of_danbib", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_add_dk5_to_phd", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_metacompass", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("view_metacompass", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("use_central_faust", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("part_of_requestorder", true, null));
        expectedLibraryRuleList710100.add(createLibraryRule("regional_obligations", false, null));
        expectedLibraryRuleList710100.add(createLibraryRule("auth_vera", false, null));

        final LibraryRules expectedLibraryRules710100 = new LibraryRules();
        expectedLibraryRules710100.setLibraryRule(expectedLibraryRuleList710100);
        expectedLibraryRules710100.setAgencyId("710100");
        expectedLibraryRules710100.setAgencyType("Folkebibliotek");

        assertThat(connector.getLibraryRulesByAgencyId("710100"), is(expectedLibraryRules710100));
    }

    @Test
    void getLibrariesTest() throws VipCoreException {
        final LibraryRulesRequest libraryRulesRequest = new LibraryRulesRequest();

        final LibraryRule libraryRuleIms = new LibraryRule();
        libraryRuleIms.setName(VipCoreLibraryRulesConnector.Rule.IMS_LIBRARY.getValue());
        libraryRuleIms.setBool(true);

        libraryRulesRequest.setLibraryRule(Collections.singletonList(libraryRuleIms));

        assertThat(connector.getLibraries(libraryRulesRequest), is(new HashSet<>(
                List.of("785100", "775100", "737000", "754000", "710100")
        )));

        final LibraryRule libraryRuleAuthRoot = new LibraryRule();
        libraryRuleAuthRoot.setName(VipCoreLibraryRulesConnector.Rule.AUTH_ROOT.getValue());
        libraryRuleAuthRoot.setBool(true);

        libraryRulesRequest.setLibraryRule(Collections.singletonList(libraryRuleAuthRoot));

        assertThat(connector.getLibraries(libraryRulesRequest), is(new HashSet<>(
                List.of("010100", "790900")
        )));
    }

    @Test
    void getLibrariesCatalogingTemplateSetTest() throws VipCoreException {
        final LibraryRulesRequest libraryRulesRequest = new LibraryRulesRequest();

        final LibraryRule catalogingTemplateSetDBS = new LibraryRule();
        catalogingTemplateSetDBS.setName(VipCoreLibraryRulesConnector.Rule.CATALOGING_TEMPLATE_SET.getValue());
        catalogingTemplateSetDBS.setString("dbc");
        libraryRulesRequest.setLibraryRule(Collections.singletonList(catalogingTemplateSetDBS));

        assertThat(connector.getLibraries(libraryRulesRequest), is(new HashSet<>(
                List.of("000002", "150094", "000007", "870978", "870977", "000004", "190008", "870979", "870974", "870973", "870976", "000008", "870975", "870970", "870971", "010100", "190007", "150077", "190004", "190002")
        )));

        final LibraryRule catalogingTemplateSetFBS = new LibraryRule();
        catalogingTemplateSetFBS.setName(VipCoreLibraryRulesConnector.Rule.CATALOGING_TEMPLATE_SET.getValue());
        catalogingTemplateSetFBS.setString("fbs");
        libraryRulesRequest.setLibraryRule(Collections.singletonList(catalogingTemplateSetFBS));

        assertThat(connector.getLibraries(libraryRulesRequest), is(new HashSet<>(
                Arrays.asList(
                        "731600", "131120", "773000", "779100", "781300", "133300", "748200", "756100",
                        "721000", "758000", "717300", "744000", "133030", "774600", "782000", "131090",
                        "131130", "772700", "133150", "726000", "131010", "753000", "718500", "721900",
                        "762100", "710100", "714700", "733000", "737600", "131020", "131140", "133160",
                        "546120", "133040", "727000", "543910", "133200", "131300", "777300", "725900",
                        "760700", "767100", "715700", "732000", "776600", "131030", "133170", "300860",
                        "715300", "131150", "133050", "792615", "300741", "724000", "757300", "755000",
                        "761500", "784600", "765700", "747900", "741000", "716100", "546180", "735000",
                        "716900", "133180", "133060", "820012", "131040", "131160", "716500", "131200",
                        "133100", "748000", "725000", "775100", "754000", "746100", "740000", "133190",
                        "786000", "872760", "719000", "763000", "131170", "734000", "133110", "133070",
                        "717500", "770600", "131050", "732900", "774000", "720100", "784900", "546160",
                        "718300", "766500", "133080", "721700", "743000", "766100", "737000", "131060",
                        "131180", "777900", "133120", "775600", "545110", "300970", "718700", "733600",
                        "790900", "131100", "300773", "300411", "781000", "771000", "700400", "725300",
                        "723000", "133090", "715100", "736000", "742000", "784000", "131070", "133130",
                        "131190", "133010", "715900", "300540", "730600", "546153", "715500", "770700",
                        "732600", "776000", "751000", "131110", "757500", "782500", "774100", "778700",
                        "749200", "745000", "726900", "722300", "726500", "739000", "716300", "785100",
                        "133140", "133020", "131080", "543310", "716700", "791615"
                )
        )));

        final LibraryRule catalogingTemplateSetJulemanden = new LibraryRule();
        catalogingTemplateSetJulemanden.setName(VipCoreLibraryRulesConnector.Rule.CATALOGING_TEMPLATE_SET.getValue());
        catalogingTemplateSetJulemanden.setString("julemanden");
        libraryRulesRequest.setLibraryRule(Collections.singletonList(catalogingTemplateSetJulemanden));

        assertThat(connector.getLibraries(libraryRulesRequest), is(new HashSet<>(
                Collections.emptyList()
        )));
    }

    @Test
    void getLibrariesCombinesRulesTest() throws VipCoreException {
        final LibraryRulesRequest libraryRulesRequest = new LibraryRulesRequest();

        final LibraryRule libraryRuleIms = new LibraryRule();
        libraryRuleIms.setName(VipCoreLibraryRulesConnector.Rule.IMS_LIBRARY.getValue());
        libraryRuleIms.setBool(true);

        final LibraryRule libraryRuleCreateEnrichments = new LibraryRule();
        libraryRuleCreateEnrichments.setName(VipCoreLibraryRulesConnector.Rule.CREATE_ENRICHMENTS.getValue());
        libraryRuleCreateEnrichments.setBool(true);

        libraryRulesRequest.setLibraryRule(Arrays.asList(libraryRuleIms, libraryRuleCreateEnrichments));

        assertThat(connector.getLibraries(libraryRulesRequest), is(new HashSet<>(
                List.of("785100", "775100", "737000", "754000", "710100")
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
