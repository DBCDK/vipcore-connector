package dk.dbc.vipcore.agencyinfo;

import dk.dbc.commons.useragent.UserAgent;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.vipcore.VipCoreConnector;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VipCoreAgencyInfoConnector factory
 * <p>
 * Synopsis:
 * </p>
 * <pre>
 *    // New instance
 *    VipCoreAgencyInfoConnector connector = VipCoreAgencyInfoConnector.create("http://vip-core");
 *
 *    // Singleton instance in CDI enabled environment
 *    {@literal @}Inject
 *    VipCoreAgencyInfoConnectorFactory factory;
 *    ...
 *    VipCoreAgencyInfoConnector connector = factory.getInstance();
 *
 *    // or simply
 *    {@literal @}Inject
 *    VipCoreAgencyInfoConnector connector;
 * </pre>
 * <p>
 * CDI case depends on the vipcore service baseurl being defined as
 * the value of either a system property or environment variable
 * named VIPCORE_ENDPOINT. VIPCORE_SERVICE_TIMING_LOG_LEVEL
 * should be one of TRACE, DEBUG, INFO(default), WARN or ERROR, for setting
 * log level
 * </p>
 */
@ApplicationScoped
public class VipCoreAgencyInfoConnectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(VipCoreAgencyInfoConnectorFactory.class);

    public static VipCoreAgencyInfoConnector create(String vipcoreServiceBaseUrl) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating VipCoreAgencyInfoConnector for: {}", vipcoreServiceBaseUrl);
        return new VipCoreAgencyInfoConnector(client, UserAgent.forInternalRequests(), vipcoreServiceBaseUrl);
    }

    public static VipCoreAgencyInfoConnector create(String vipcoreServiceBaseUrl, int cacheAge, VipCoreConnector.TimingLogLevel level) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating VipCoreAgencyInfoConnector for: {}", vipcoreServiceBaseUrl);
        return new VipCoreAgencyInfoConnector(client, UserAgent.forInternalRequests(), vipcoreServiceBaseUrl, cacheAge, level);
    }

    @Inject
    @ConfigProperty(name = "VIPCORE_ENDPOINT")
    private String vipcoreServiceBaseUrl;

    @Inject
    @ConfigProperty(name = "VIPCORE_SERVICE_TIMING_LOG_LEVEL", defaultValue = "INFO")
    private VipCoreConnector.TimingLogLevel level;

    @Inject
    @ConfigProperty(name = "VIPCORE_CACHE_AGE", defaultValue = "8")
    private int cacheAge;

    VipCoreAgencyInfoConnector vipCoreAgencyInfoConnector;

    @PostConstruct
    public void initializeConnector() {
        vipCoreAgencyInfoConnector = VipCoreAgencyInfoConnectorFactory.create(vipcoreServiceBaseUrl, cacheAge, level);
    }

    @Produces
    public VipCoreAgencyInfoConnector getInstance() {
        return vipCoreAgencyInfoConnector;
    }

    @PreDestroy
    public void tearDownConnector() {
        vipCoreAgencyInfoConnector.close();
    }
}
