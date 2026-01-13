package dk.dbc.vipcore;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import dk.dbc.commons.useragent.UserAgent;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.vipcore.exception.VipCoreException;
import dk.dbc.vipcore.libraryrules.VipCoreLibraryRulesConnector;
import dk.dbc.vipcore.marshallers.LibraryRule;
import dk.dbc.vipcore.marshallers.LibraryRulesRequest;
import jakarta.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

class VipCoreLibraryRulesConnectorCacheTest {

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
        final UserAgent userAgent = new UserAgent("VipCoreLibraryRulesConnectorCacheTest");
        connector = new VipCoreLibraryRulesConnector(CLIENT, userAgent, wireMockHost, 1, VipCoreConnector.TimingLogLevel.INFO);
    }

    @AfterAll
    static void stopWireMockServer() {
        wireMockServer.stop();
    }

    @Test
    void getLibraryRulesByAgencyIdCacheTest() throws VipCoreException {
        wireMockServer.resetRequests();

        connector.getLibraryRulesByAgencyId("710100");
        connector.getLibraryRulesByAgencyId("710100");
        connector.getLibraryRulesByAgencyId("710100");
        connector.getLibraryRulesByAgencyId("010100");

        verify(2, new RequestPatternBuilder().withUrl("/1.0/api/libraryrules"));
    }

    @Test
    void getLibrariesCacheTest() throws VipCoreException {
        wireMockServer.resetRequests();

        final LibraryRulesRequest libraryRulesRequest = new LibraryRulesRequest();

        final LibraryRule catalogingTemplateSetJulemanden = new LibraryRule();
        catalogingTemplateSetJulemanden.setName(VipCoreLibraryRulesConnector.Rule.CATALOGING_TEMPLATE_SET.getValue());
        catalogingTemplateSetJulemanden.setString("julemanden");
        libraryRulesRequest.setLibraryRule(Collections.singletonList(catalogingTemplateSetJulemanden));

        connector.getLibraries(libraryRulesRequest);
        connector.getLibraries(libraryRulesRequest);
        connector.getLibraries(libraryRulesRequest);

        verify(1, new RequestPatternBuilder().withUrl("/1.0/api/libraryrules"));
    }

    @Test
    void getLibrariesCombinedRulesCacheTest() throws VipCoreException {
        wireMockServer.resetRequests();

        final LibraryRulesRequest libraryRulesRequest = new LibraryRulesRequest();

        final LibraryRule libraryRuleIms = new LibraryRule();
        libraryRuleIms.setName(VipCoreLibraryRulesConnector.Rule.IMS_LIBRARY.getValue());
        libraryRuleIms.setBool(true);

        final LibraryRule libraryRuleCreateEnrichments = new LibraryRule();
        libraryRuleCreateEnrichments.setName(VipCoreLibraryRulesConnector.Rule.CREATE_ENRICHMENTS.getValue());
        libraryRuleCreateEnrichments.setBool(true);

        libraryRulesRequest.setLibraryRule(Arrays.asList(libraryRuleIms, libraryRuleCreateEnrichments));

        connector.getLibraries(libraryRulesRequest);
        connector.getLibraries(libraryRulesRequest);
        connector.getLibraries(libraryRulesRequest);

        verify(1, new RequestPatternBuilder().withUrl("/1.0/api/libraryrules"));
    }
}
