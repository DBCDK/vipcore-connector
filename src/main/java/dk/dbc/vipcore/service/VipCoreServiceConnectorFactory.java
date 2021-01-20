/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.service;

import dk.dbc.httpclient.HttpClient;
import dk.dbc.vipcore.VipCoreConnector;
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
 * VipCoreServiceConnector factory
 * <p>
 * Synopsis:
 * </p>
 * <pre>
 *    // New instance
 *    VipCoreServiceConnector connector = VipCoreServiceConnectorFactory.create("http://vip-core");
 *
 *    // Singleton instance in CDI enabled environment
 *    {@literal @}Inject
 *    VipCoreServiceConnectorFactory factory;
 *    ...
 *    VipCoreServiceConnector connector = factory.getInstance();
 *
 *    // or simply
 *    {@literal @}Inject
 *    VipCoreServiceConnector connector;
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
public class VipCoreServiceConnectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(VipCoreServiceConnectorFactory.class);

    public static VipCoreServiceConnector create(String vipcoreServiceBaseUrl) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating VipCoreServiceConnector for: {}", vipcoreServiceBaseUrl);
        return new VipCoreServiceConnector(client, vipcoreServiceBaseUrl);
    }

    public static VipCoreServiceConnector create(String vipcoreServiceBaseUrl, int cacheAge, VipCoreConnector.TimingLogLevel level) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating VipCoreServiceConnector for: {}", vipcoreServiceBaseUrl);
        return new VipCoreServiceConnector(client, vipcoreServiceBaseUrl, cacheAge, level);
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

    VipCoreServiceConnector vipCoreServiceConnector;

    @PostConstruct
    public void initializeConnector() {
        vipCoreServiceConnector = VipCoreServiceConnectorFactory.create(vipcoreServiceBaseUrl, cacheAge, level);
    }

    @Produces
    public VipCoreServiceConnector getInstance() {
        return vipCoreServiceConnector;
    }

    @PreDestroy
    public void tearDownConnector() {
        vipCoreServiceConnector.close();
    }
}
