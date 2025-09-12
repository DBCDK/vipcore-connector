package dk.dbc.vipcore.libraryrules;

import dk.dbc.commons.jsonb.JSONBContext;
import dk.dbc.commons.jsonb.JSONBException;
import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.vipcore.VipCoreConnector;
import dk.dbc.vipcore.exception.VipCoreException;
import dk.dbc.vipcore.marshallers.LibraryRule;
import dk.dbc.vipcore.marshallers.LibraryRules;
import dk.dbc.vipcore.marshallers.LibraryRulesRequest;
import dk.dbc.vipcore.marshallers.LibraryRulesResponse;
import org.apache.commons.collections4.map.PassiveExpiringMap;

import jakarta.ws.rs.client.Client;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VipCoreLibraryRulesConnector extends VipCoreConnector {
    private static final JSONBContext jsonbContext = new JSONBContext();

    private static final int MAX_CACHE_AGE = 8;
    private final PassiveExpiringMap<String, Set<String>> libraryRuleCache;
    private final PassiveExpiringMap<String, LibraryRules> libraryRulesByAgencyIdCache;

    public enum Rule {
        AUTH_ADD_DK5_TO_PHD_ALLOWED("auth_add_dk5_to_phd"),
        AUTH_AGENCY_COMMON_RECORD("auth_agency_common_record"),
        AUTH_COMMON_NOTES("auth_common_notes"),
        AUTH_COMMON_SUBJECTS("auth_common_subjects"),
        AUTH_CREATE_COMMON_RECORD("auth_create_common_record"),
        AUTH_DBC_RECORDS("auth_dbc_records"),
        AUTH_EXPORT_HOLDINGS("auth_export_holdings"),
        AUTH_METACOMPASS("auth_metacompass"),
        AUTH_PUBLIC_LIB_COMMON_RECORD("auth_public_lib_common_record"),
        AUTH_RET_RECORD("auth_ret_record"),
        AUTH_ROOT("auth_root"),
        AUTH_VERA("auth_vera"),
        CATALOGING_TEMPLATE_SET("cataloging_template_set"),
        CREATE_ENRICHMENTS("create_enrichments"),
        IMS_LIBRARY("ims_library"),
        PART_OF_BIBLIOTEK_DK("part_of_bibliotek_dk"),
        PART_OF_DANBIB("part_of_danbib"),
        PART_OF_REQUEST_ORDER("part_of_requestorder"),
        REGIONAL_OBLIGATIONS("regional_obligations"),
        USE_CENTRAL_FAUST("use_central_faust"),
        USE_ENRICHMENTS("use_enrichments"),
        USE_HOLDINGS_ITEM("use_holdings_item"),
        USE_LOCALDATA_STREAM("use_localdata_stream"),
        VIEW_METACOMPASS("view_metacompass"),
        WORLDCAT_RESOURCE_SHARING("worldcat_resource_sharing"),
        WORLDCAT_SYNCHRONIZE("worldcat_synchronize");

        private final String value;

        Rule(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }

    private static final String LIBRARY_RULES_PATH = "1.0/api/libraryrules";

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for vipcore service endpoint
     */
    public VipCoreLibraryRulesConnector(Client httpClient, String baseUrl) {
        super(httpClient, baseUrl, TimingLogLevel.INFO);

        libraryRuleCache = new PassiveExpiringMap<>(MAX_CACHE_AGE, TimeUnit.HOURS);
        libraryRulesByAgencyIdCache = new PassiveExpiringMap<>(MAX_CACHE_AGE, TimeUnit.HOURS);
    }

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for vipcore service endpoint
     * @param level      timings log level
     */
    public VipCoreLibraryRulesConnector(Client httpClient, String baseUrl, int cacheAge, TimingLogLevel level) {
        super(httpClient, baseUrl, level);

        libraryRuleCache = new PassiveExpiringMap<>(cacheAge, TimeUnit.HOURS);
        libraryRulesByAgencyIdCache = new PassiveExpiringMap<>(cacheAge, TimeUnit.HOURS);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for vipcore service endpoint
     */
    public VipCoreLibraryRulesConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl) {
        super(failSafeHttpClient, baseUrl, TimingLogLevel.INFO);

        libraryRuleCache = new PassiveExpiringMap<>(MAX_CACHE_AGE, TimeUnit.HOURS);
        libraryRulesByAgencyIdCache = new PassiveExpiringMap<>(MAX_CACHE_AGE, TimeUnit.HOURS);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for vipcore service endpoint
     * @param level              timings log level
     */
    public VipCoreLibraryRulesConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl, int cacheAge, TimingLogLevel level) {
        super(failSafeHttpClient, baseUrl, level);

        libraryRuleCache = new PassiveExpiringMap<>(cacheAge, TimeUnit.HOURS);
        libraryRulesByAgencyIdCache = new PassiveExpiringMap<>(cacheAge, TimeUnit.HOURS);
    }

    public boolean hasFeature(int agencyId, Rule feature) throws VipCoreException {
        return hasFeature(Integer.toString(agencyId), feature);
    }

    public boolean hasFeature(String agencyId, Rule feature) throws VipCoreException {
        final List<LibraryRule> libraryRules = postLibraryRulesRequest(agencyId, null).getLibraryRule();

        return libraryRules.stream()
                .anyMatch(libraryRule ->
                        feature.getValue().equals(libraryRule.getName()) &&
                                libraryRule.getBool() != null &&
                                libraryRule.getBool()
                );
    }

    public Set<String> getAllowedLibraryRules(String agencyId) throws VipCoreException {
        return getAllowedLibraryRules(agencyId, null);
    }

    public Set<String> getAllowedLibraryRules(String agencyId, String trackingId) throws VipCoreException {
        final List<LibraryRule> libraryRules = postLibraryRulesRequest(agencyId, trackingId).getLibraryRule();

        return libraryRules.stream()
                .filter(libraryRule -> libraryRule.getBool() != null && libraryRule.getBool())
                .map(LibraryRule::getName)
                .collect(Collectors.toSet());
    }

    public LibraryRules getLibraryRulesByAgencyId(String agencyId) throws VipCoreException {
        return postLibraryRulesRequest(agencyId, null);
    }

    public LibraryRules getLibraryRulesByAgencyId(String agencyId, String trackingId) throws VipCoreException {
        return postLibraryRulesRequest(agencyId, trackingId);
    }

    private LibraryRules postLibraryRulesRequest(String agencyId, String trackingId) throws VipCoreException {
        try {
            final LibraryRules cacheValue = libraryRulesByAgencyIdCache.get(agencyId);
            if (cacheValue != null) {
                return cacheValue;
            } else {
                final LibraryRulesRequest libraryRulesRequest = new LibraryRulesRequest();
                libraryRulesRequest.setAgencyId(agencyId);
                if (trackingId != null) {
                    libraryRulesRequest.setTrackingId(trackingId);
                }

                final LibraryRulesResponse libraryRulesResponse = postRequest(LIBRARY_RULES_PATH, jsonbContext.marshall(libraryRulesRequest), LibraryRulesResponse.class);
                for (LibraryRules libraryRules : libraryRulesResponse.getLibraryRules()) {
                    if (agencyId.equals(libraryRules.getAgencyId())) {
                        libraryRulesByAgencyIdCache.put(agencyId, libraryRules);
                        return libraryRules;
                    }
                }

                throw new VipCoreLibraryRulesConnectorException(String.format("Could not find LibraryRules for agencyId %s", agencyId));
            }
        } catch (JSONBException e) {
            throw new VipCoreLibraryRulesConnectorException("Caught unexpected JSONBException", e);
        }
    }

    public Set<String> getLibraries(LibraryRulesRequest libraryRulesRequest) throws VipCoreException {
        Set<String> result;
        try {
            final String libraryRuleCacheKey = createLibraryRuleCacheKey(libraryRulesRequest);
            result = libraryRuleCache.get(libraryRuleCacheKey);
            if (result != null) {
                return result;
            }

            final LibraryRulesResponse libraryRulesResponse = postRequest(LIBRARY_RULES_PATH, jsonbContext.marshall(libraryRulesRequest), LibraryRulesResponse.class);

            if (libraryRulesResponse.getLibraryRules() != null) {
                result = libraryRulesResponse.getLibraryRules().stream()
                        .map(LibraryRules::getAgencyId)
                        .collect(Collectors.toSet());
            } else {
                // If libraryRulesResponse.getLibraryRules() is null it is because no libraries with that rule was found
                result = new HashSet<>();
            }

            libraryRuleCache.put(libraryRuleCacheKey, result);

            return result;
        } catch (JSONBException e) {
            throw new VipCoreLibraryRulesConnectorException("Caught unexpected JSONBException", e);
        }
    }

    private String createLibraryRuleCacheKey(LibraryRulesRequest libraryRulesRequest) {
        StringBuilder stringBuilder = new StringBuilder();

        for (LibraryRule libraryRule : libraryRulesRequest.getLibraryRule()) {
            stringBuilder.append(libraryRule.getName().toUpperCase());
            stringBuilder.append("_");
            if (libraryRule.getBool() != null) {
                stringBuilder.append(libraryRule.getBool().toString().toUpperCase());
            } else {
                stringBuilder.append(libraryRule.getString().toUpperCase());
            }
        }

        return stringBuilder.toString();
    }

}
