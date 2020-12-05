/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

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

import javax.ws.rs.client.Client;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VipCoreLibraryRulesConnector extends VipCoreConnector {
    private static final JSONBContext jsonbContext = new JSONBContext();

    private static final int MAX_CACHE_AGE = 8;
    private PassiveExpiringMap<String, Set<String>> libraryRuleCache;
    private PassiveExpiringMap<String, List<LibraryRule>> libraryRulesByAgencyIdCache;

    public enum Rule {
        CREATE_ENRICHMENTS("create_enrichments"),
        PART_OF_BIBLIOTEK_DK("part_of_bibliotek_dk"),
        PART_OF_DANBIB("part_of_danbib"),
        USE_ENRICHMENTS("use_enrichments"),
        USE_LOCALDATA_STREAM("use_localdata_stream"),
        USE_HOLDINGS_ITEM("use_holdings_item"),
        AUTH_ROOT("auth_root"),
        AUTH_COMMON_SUBJECTS("auth_common_subjects"),
        AUTH_COMMON_NOTES("auth_common_notes"),
        AUTH_DBC_RECORDS("auth_dbc_records"),
        AUTH_PUBLIC_LIB_COMMON_RECORD("auth_public_lib_common_record"),
        AUTH_RET_RECORD("auth_ret_record"),
        AUTH_AGENCY_COMMON_RECORD("auth_agency_common_record"),
        AUTH_EXPORT_HOLDINGS("auth_export_holdings"),
        AUTH_CREATE_COMMON_RECORD("auth_create_common_record"),
        AUTH_ADD_DK5_TO_PHD_ALLOWED("auth_create_common_record"),
        AUTH_METACOMPASS("auth_metacompass"),
        CATALOGING_TEMPLATE_SET("cataloging_template_set");

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

    private static final String LIBRARY_RULES_PATH = "libraryrules";

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
        final List<LibraryRule> libraryRules = getLibraryRulesByAgencyId(agencyId);

        return libraryRules.stream()
                .anyMatch(libraryRule ->
                        feature.getValue().equals(libraryRule.getName()) &&
                                libraryRule.getBool() != null &&
                                libraryRule.getBool()
                );
    }

    public Set<String> getAllowedLibraryRules(String agencyId) throws VipCoreException {
        final List<LibraryRule> libraryRules = getLibraryRulesByAgencyId(agencyId);

        return libraryRules.stream()
                .filter(libraryRule -> libraryRule.getBool() != null && libraryRule.getBool())
                .map(LibraryRule::getName)
                .collect(Collectors.toSet());
    }

    public List<LibraryRule> getLibraryRulesByAgencyId(String agencyId) throws VipCoreException {
        try {
            List<LibraryRule> cacheValue = libraryRulesByAgencyIdCache.get(agencyId);
            if (cacheValue != null) {
                return cacheValue;
            } else {
                final LibraryRulesRequest libraryRulesRequest = new LibraryRulesRequest();
                libraryRulesRequest.setAgencyId(agencyId);

                final LibraryRulesResponse libraryRulesResponse = postRequest(LIBRARY_RULES_PATH, jsonbContext.marshall(libraryRulesRequest), LibraryRulesResponse.class);

                for (LibraryRules libraryRules : libraryRulesResponse.getLibraryRules()) {
                    if (agencyId.equals(libraryRules.getAgencyId())) {
                        libraryRulesByAgencyIdCache.put(agencyId, libraryRules.getLibraryRule());
                        return libraryRules.getLibraryRule();
                    }
                }

                throw new VipCoreLibraryRulesConnectorException(String.format("Could not find LibraryRules for agencyId %s", agencyId));
            }
        } catch (JSONBException e) {
            throw new VipCoreLibraryRulesConnectorException("Caught unexpected JSONBException", e);
        }
    }

    public Set<String> getLibrariesByLibraryRule(Rule rule, String value) throws VipCoreException {
        final LibraryRule libraryRule = new LibraryRule();
        libraryRule.setName(rule.getValue());
        libraryRule.setString(value);

        return postLibraryRulesRequest(libraryRule);
    }

    public Set<String> getLibrariesByLibraryRule(Rule rule, boolean value) throws VipCoreException {
        final LibraryRule libraryRule = new LibraryRule();
        libraryRule.setName(rule.getValue());
        libraryRule.setBool(value);

        return postLibraryRulesRequest(libraryRule);
    }

    private Set<String> postLibraryRulesRequest(LibraryRule libraryRule) throws VipCoreException {
        Set<String> result;
        try {
            final String libraryRuleCacheKey = createLibraryRuleCacheKey(libraryRule);
            result = libraryRuleCache.get(libraryRuleCacheKey);
            if (result != null) {
                return result;
            }

            final List<LibraryRule> libraryRuleList = Collections.singletonList(libraryRule);
            final LibraryRulesRequest libraryRulesRequest = new LibraryRulesRequest();
            libraryRulesRequest.setLibraryRule(libraryRuleList);

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

    private String createLibraryRuleCacheKey(LibraryRule libraryRule) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(libraryRule.getName().toUpperCase());
        stringBuilder.append("_");
        if (libraryRule.getBool() != null) {
            stringBuilder.append(libraryRule.getBool().toString().toUpperCase());
        } else {
            stringBuilder.append(libraryRule.getString().toUpperCase());
        }

        return stringBuilder.toString();
    }

}
