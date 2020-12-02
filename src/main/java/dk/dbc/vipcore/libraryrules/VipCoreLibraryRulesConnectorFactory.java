/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.libraryrules;

import dk.dbc.httpclient.HttpClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.client.Client;

/**
 * VipCoreLibraryRulesConnector factory
 * <p>
 * Synopsis:
 * </p>
 * <pre>
 *    // New instance
 *    VipCoreLibraryRulesConnector connector = VipCoreLibraryRulesConnectorFactory.create("http://vip-core");
 *
 *    // Singleton instance in CDI enabled environment
 *    {@literal @}Inject
 *    VipCoreLibraryRulesConnectorFactory factory;
 *    ...
 *    VipCoreLibraryRulesConnector connector = factory.getInstance();
 *
 *    // or simply
 *    {@literal @}Inject
 *    VipCoreLibraryRulesConnector connector;
 * </pre>
 * <p>
 * CDI case depends on the vipcore service baseurl being defined as
 * the value of either a system property or environment variable
 * named VIPCORE_SERVICE_URL. VIPCORE_SERVICE_TIMING_LOG_LEVEL
 * should be one of TRACE, DEBUG, INFO(default), WARN or ERROR, for setting
 * log level
 * </p>
 */
@ApplicationScoped
public class VipCoreLibraryRulesConnectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(VipCoreLibraryRulesConnectorFactory.class);

    public static VipCoreLibraryRulesConnector create(String vipcoreServiceBaseUrl) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating VipCoreLibraryRulesConnector for: {}", vipcoreServiceBaseUrl);
        return new VipCoreLibraryRulesConnector(client, vipcoreServiceBaseUrl);
    }

    public static VipCoreLibraryRulesConnector create(String vipcoreServiceBaseUrl, VipCoreLibraryRulesConnector.TimingLogLevel level) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating VipCoreLibraryRulesConnector for: {}", vipcoreServiceBaseUrl);
        return new VipCoreLibraryRulesConnector(client, vipcoreServiceBaseUrl, level);
    }

    @Inject
    @ConfigProperty(name = "VIPCORE_SERVICE_URL")
    private String vipcoreServiceBaseUrl;

    @Inject
    @ConfigProperty(name = "VIPCORE_SERVICE_TIMING_LOG_LEVEL", defaultValue = "INFO")
    private VipCoreLibraryRulesConnector.TimingLogLevel level;

    VipCoreLibraryRulesConnector vipCoreLibraryRulesConnector;

    @PostConstruct
    public void initializeConnector() {
        vipCoreLibraryRulesConnector = VipCoreLibraryRulesConnectorFactory.create(vipcoreServiceBaseUrl, level);
    }

    @Produces
    public VipCoreLibraryRulesConnector getInstance() {
        return vipCoreLibraryRulesConnector;
    }

    @PreDestroy
    public void tearDownConnector() {
        vipCoreLibraryRulesConnector.close();
    }
}
