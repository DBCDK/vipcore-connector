/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.service;

import dk.dbc.commons.jsonb.JSONBContext;
import dk.dbc.commons.jsonb.JSONBException;
import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.vipcore.VipCoreConnector;
import dk.dbc.vipcore.exception.VipCoreException;
import dk.dbc.vipcore.marshallers.Information;
import dk.dbc.vipcore.marshallers.ServiceRequest;
import dk.dbc.vipcore.marshallers.ServiceResponse;
import org.apache.commons.collections4.map.PassiveExpiringMap;

import javax.ws.rs.client.Client;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class VipCoreServiceConnector extends VipCoreConnector {
    private static final JSONBContext jsonbContext = new JSONBContext();

    private static final int MAX_CACHE_AGE = 8;
    private final PassiveExpiringMap<String, Set<String>> serviceCache;

    private static final String SERVICE_PATH = "1.0/api/service";

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for vipcore service endpoint
     */
    public VipCoreServiceConnector(Client httpClient, String baseUrl) {
        super(httpClient, baseUrl, VipCoreConnector.TimingLogLevel.INFO);

        serviceCache = new PassiveExpiringMap<>(MAX_CACHE_AGE, TimeUnit.HOURS);
    }

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for vipcore service endpoint
     * @param level      timings log level
     */
    public VipCoreServiceConnector(Client httpClient, String baseUrl, int cacheAge, VipCoreConnector.TimingLogLevel level) {
        super(httpClient, baseUrl, level);

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
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setService("information");
        serviceRequest.setAgencyId(agencyId);
        if (trackingId != null) {
            serviceRequest.setTrackingId(trackingId);
        }

        try {
            ServiceResponse serviceResponse = postRequest(SERVICE_PATH, jsonbContext.marshall(serviceRequest), ServiceResponse.class);

            return serviceResponse.getInformation();
        } catch (JSONBException e) {
            throw new VipCoreServiceConnectorException("Caught unexpected JSONBException", e);
        }
    }


}
