package dk.dbc.vipcore;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.commons.useragent.UserAgent;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.vipcore.agencyinfo.VipCoreAgencyInfoConnector;
import dk.dbc.vipcore.exception.AgencyNotFoundException;
import dk.dbc.vipcore.exception.ErrorInRequestException;
import dk.dbc.vipcore.exception.VipCoreException;
import dk.dbc.vipcore.service.VipCoreServiceConnector;
import jakarta.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class VipCoreAgencyInfoConnectorTest {


    private static WireMockServer wireMockServer;
    private static String wireMockHost;

    final static Client CLIENT = HttpClient.newClient(new ClientConfig()
            .register(new JacksonFeature()));
    static VipCoreAgencyInfoConnector connector;

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
        final UserAgent userAgent = new UserAgent("VipCoreServiceConnectorTest");
        connector = new VipCoreAgencyInfoConnector(CLIENT, userAgent, wireMockHost, 0, VipCoreConnector.TimingLogLevel.INFO);
    }

    @AfterAll
    static void stopWireMockServer() {
        wireMockServer.stop();
    }

    @Test
    void getNameTest() throws VipCoreException {
        assertThat(connector.getAgencyName("790900"), is("DBC-Testbiblioteksvæsen"));
        assertThat(connector.getAgencyName("726000"), is("Halsnæs Bibliotekerne"));
    }

    @Test
    void getNameErrorTest() throws VipCoreException {
        Assertions.assertThrows(AgencyNotFoundException.class, () -> connector.getAgencyName("000000"), "agency_not_found");
        Assertions.assertThrows(ErrorInRequestException.class, () -> connector.getAgencyName("sdfsdf"), "error_in_request");
    }

}
