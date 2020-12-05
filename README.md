# VipCore Connector
Jar library containing helper functions for calling the vipcore service

### Usage
In pom.xml add this dependency:

    <groupId>dk.dbc</groupId>
    <artifactId>vipcore-connector</artifactId>
    <version>1.0-SNAPSHOT</version>

In your EJB add the following inject:

    @Inject
    private VipCoreLibraryRulesConnector vipCoreLibraryRulesConnector;

You must have the following environment variables in your deployment:

    VIPCORE_ENDPOINT

By default the connector caches responses for 8 hours. To use a different value set:    
    
    VIPCORE_CACHE_AGE

The value is the amount of hours to keep the cache. To disable set the value to 0.

### Examples
        Set<String> allowedLibraryRules = vipCoreLibraryRulesConnector.getAllowedLibraryRules("710100")

        Set<String> libraries = vipCoreLibraryRulesConnector.getLibrariesByLibraryRule(VipCoreLibraryRulesConnector.Rule.USE_ENRICHMENTS, true)

        List<LibraryRule> libraryRuleList = vipCoreLibraryRulesConnector.getLibraryRulesByAgencyId("010100")
