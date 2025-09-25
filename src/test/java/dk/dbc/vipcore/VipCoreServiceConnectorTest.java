package dk.dbc.vipcore;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.vipcore.exception.AgencyNotFoundException;
import dk.dbc.vipcore.exception.ErrorInRequestException;
import dk.dbc.vipcore.exception.VipCoreException;
import dk.dbc.vipcore.marshallers.BranchName;
import dk.dbc.vipcore.marshallers.Information;
import dk.dbc.vipcore.service.VipCoreServiceConnector;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.client.Client;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class VipCoreServiceConnectorTest {

    private static WireMockServer wireMockServer;
    private static String wireMockHost;

    final static Client CLIENT = HttpClient.newClient(new ClientConfig()
            .register(new JacksonFeature()));
    static VipCoreServiceConnector connector;

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
        connector = new VipCoreServiceConnector(CLIENT, wireMockHost, 0, VipCoreConnector.TimingLogLevel.INFO);
    }

    @AfterAll
    static void stopWireMockServer() {
        wireMockServer.stop();
    }

    @Test
    void getInformationTest_010100() throws VipCoreException {
        final Information expected = new Information();
        expected.setAgencyId("010100");
        expected.setBranchId("010100");
        expected.setAgencyName("Dansk BiblioteksCenter");
        expected.setAgencyType("Other");
        final BranchName branchName = new BranchName();
        branchName.setValue("Dansk BiblioteksCenter");
        expected.setBranchName(branchName);
        expected.setBranchPhone("44867777");
        expected.setBranchEmail("checkmail@dbc.dk");
        expected.setBranchType("x");
        expected.setPostalAddress("Tempovej 7-11");
        expected.setPostalCode("2750");
        expected.setCity("Ballerup");
        expected.setIsil("10100");
        expected.setKvik("NO");
        expected.setNorfri("NO");
        expected.setRequestOrder("10100");
        expected.setSender("010100");
        expected.setReplyToEmail("returors@dbc.dk");

        final Information actual = connector.getInformation("010100");

        assertThat(actual, is(expected));
    }

    @Test
    void getInformationTest_716700() {
        Assertions.assertThrows(AgencyNotFoundException.class, () -> connector.getInformation("000000"), "agency_not_found");
        Assertions.assertThrows(ErrorInRequestException.class, () -> connector.getInformation("sdfsdf"), "error_in_request");
    }

}
