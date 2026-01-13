package dk.dbc.vipcore.service;

import dk.dbc.commons.jsonb.JSONBContext;
import dk.dbc.commons.jsonb.JSONBException;
import dk.dbc.commons.useragent.UserAgent;
import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.vipcore.VipCoreConnector;
import dk.dbc.vipcore.exception.VipCoreException;
import dk.dbc.vipcore.marshallers.Information;
import dk.dbc.vipcore.marshallers.ServiceRequest;
import dk.dbc.vipcore.marshallers.ServiceResponse;
import org.apache.commons.collections4.map.PassiveExpiringMap;

import jakarta.ws.rs.client.Client;
import java.util.concurrent.TimeUnit;

public class VipCoreServiceConnector extends VipCoreConnector {
    private static final JSONBContext jsonbContext = new JSONBContext();

    private static final int MAX_CACHE_AGE = 8;
    private final PassiveExpiringMap<String, Information> serviceCache;

    private static final String SERVICE_PATH = "1.0/api/service";

    private static final String SERVICE_TYPE_INFORMATION = "information";

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for vipcore service endpoint
     */
    public VipCoreServiceConnector(Client httpClient, UserAgent userAgent, String baseUrl) {
        super(httpClient, userAgent, baseUrl, VipCoreConnector.TimingLogLevel.INFO);

        serviceCache = new PassiveExpiringMap<>(MAX_CACHE_AGE, TimeUnit.HOURS);
    }

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for vipcore service endpoint
     * @param level      timings log level
     */
    public VipCoreServiceConnector(Client httpClient, UserAgent userAgent, String baseUrl, int cacheAge, VipCoreConnector.TimingLogLevel level) {
        super(httpClient, userAgent, baseUrl, level);

        serviceCache = new PassiveExpiringMap<>(cacheAge, TimeUnit.HOURS);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for vipcore service endpoint
     */
    public VipCoreServiceConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl) {
        super(failSafeHttpClient, baseUrl, VipCoreConnector.TimingLogLevel.INFO);

        serviceCache = new PassiveExpiringMap<>(MAX_CACHE_AGE, TimeUnit.HOURS);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for vipcore service endpoint
     * @param level              timings log level
     */
    public VipCoreServiceConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl, int cacheAge, VipCoreConnector.TimingLogLevel level) {
        super(failSafeHttpClient, baseUrl, level);

        serviceCache = new PassiveExpiringMap<>(cacheAge, TimeUnit.HOURS);
    }

    public Information getInformation(String agencyId) throws VipCoreException {
        return getInformation(agencyId, null);
    }

    public Information getInformation(String agencyId, String trackingId) throws VipCoreException {
        try {
            final String cacheKey = generateCacheKey(SERVICE_TYPE_INFORMATION, agencyId);
            final Information cacheValue = serviceCache.get(cacheKey);
            if (cacheValue != null) {
                return cacheValue;
            } else {
                final ServiceRequest serviceRequest = new ServiceRequest();
                serviceRequest.setService(SERVICE_TYPE_INFORMATION);
                serviceRequest.setAgencyId(agencyId);
                if (trackingId != null) {
                    serviceRequest.setTrackingId(trackingId);
                }

                final ServiceResponse serviceResponse = postRequest(SERVICE_PATH, jsonbContext.marshall(serviceRequest), ServiceResponse.class);
                serviceCache.put(cacheKey, serviceResponse.getInformation());

                return serviceResponse.getInformation();
            }
        } catch (JSONBException e) {
            throw new VipCoreServiceConnectorException("Caught unexpected JSONBException", e);
        }
    }

    private String generateCacheKey(String serviceType, String agencyId) {
        return String.format("%s_%s", serviceType, agencyId);
    }

}
