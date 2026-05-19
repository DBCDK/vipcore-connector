package dk.dbc.vipcore.agencyinfo;

import dk.dbc.commons.jsonb.JSONBContext;
import dk.dbc.commons.jsonb.JSONBException;
import dk.dbc.commons.useragent.UserAgent;
import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.vipcore.VipCoreConnector;
import dk.dbc.vipcore.exception.VipCoreException;
import dk.dbc.vipcore.marshallers.AgencyInfoRequest;
import dk.dbc.vipcore.marshallers.AgencyInfoResponse;
import dk.dbc.vipcore.marshallers.AgencyInfoSingle;
import dk.dbc.vipcore.marshallers.PickupAgency;
import jakarta.ws.rs.client.Client;
import org.apache.commons.collections4.map.PassiveExpiringMap;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class VipCoreAgencyInfoConnector extends VipCoreConnector {
    private static final JSONBContext jsonbContext = new JSONBContext();

    private static final int MAX_CACHE_AGE = 8;
    private final PassiveExpiringMap<String, Set<AgencyInfoSingle>> agencyInfoCache;

    private static final String AGENCY_INFO_PATH = "1.0/api/agencyinfo";

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for vipcore service endpoint
     */
    public VipCoreAgencyInfoConnector(Client httpClient, UserAgent userAgent, String baseUrl) {
        super(httpClient, userAgent, baseUrl, TimingLogLevel.INFO);

        agencyInfoCache = new PassiveExpiringMap<>(MAX_CACHE_AGE, TimeUnit.HOURS);
    }

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for vipcore service endpoint
     * @param level      timings log level
     */
    public VipCoreAgencyInfoConnector(Client httpClient, UserAgent userAgent, String baseUrl, int cacheAge, TimingLogLevel level) {
        super(httpClient, userAgent, baseUrl, level);

        agencyInfoCache = new PassiveExpiringMap<>(MAX_CACHE_AGE, TimeUnit.HOURS);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for vipcore service endpoint
     */
    public VipCoreAgencyInfoConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl) {
        super(failSafeHttpClient, baseUrl, TimingLogLevel.INFO);

        agencyInfoCache = new PassiveExpiringMap<>(MAX_CACHE_AGE, TimeUnit.HOURS);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for vipcore service endpoint
     * @param level              timings log level
     */
    public VipCoreAgencyInfoConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl, int cacheAge, TimingLogLevel level) {
        super(failSafeHttpClient, baseUrl, level);

        agencyInfoCache = new PassiveExpiringMap<>(MAX_CACHE_AGE, TimeUnit.HOURS);
    }

    public String getAgencyName(String agencyId) throws VipCoreException {
        Set<AgencyInfoSingle> agencyInfos = getAgencyInfo(agencyId);

        return agencyInfos.stream()
                .findFirst()
                .map(AgencyInfoSingle::getPickupAgency)
                .map(PickupAgency::getAgencyName)
                .orElseThrow(() -> new VipCoreAgencyInfoConnectorException("No agency info found for agency id: " + agencyId));
    }

    private Set<AgencyInfoSingle> getAgencyInfo(String agencyId) throws VipCoreException {
        Set<AgencyInfoSingle> result;
        try {
            result = agencyInfoCache.get(agencyId);
            if (result != null) {
                return result;
            }

            final AgencyInfoRequest agencyInfoRequest = new AgencyInfoRequest();
            agencyInfoRequest.setAgencyId(agencyId);

            final AgencyInfoResponse AgencyInfoResponse = postRequest(AGENCY_INFO_PATH, jsonbContext.marshall(agencyInfoRequest), AgencyInfoResponse.class);

            if (AgencyInfoResponse.getAgencyInfo() != null) {
                result = new HashSet<>(AgencyInfoResponse.getAgencyInfo());
            } else {
                result = new HashSet<>();
            }

            agencyInfoCache.put(agencyId, result);

            return result;
        } catch (JSONBException e) {
            throw new VipCoreAgencyInfoConnectorException("Caught unexpected JSONBException", e);
        }
    }

}
