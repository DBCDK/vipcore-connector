/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore;

import dk.dbc.commons.useragent.UserAgent;
import dk.dbc.vipcore.agencyinfo.VipCoreAgencyInfoConnector;
import dk.dbc.vipcore.exception.VipCoreException;
import dk.dbc.vipcore.libraryrules.VipCoreLibraryRulesConnector;
import dk.dbc.vipcore.service.VipCoreServiceConnector;

public class VipCoreConnectorWireMockRecorder {
        /*
        Steps to reproduce wiremock recording:

        * Start standalone runner
            java -jar wiremock-standalone-{WIRE_MOCK_VERSION}.jar --proxy-all="{RECORD_SERVICE_HOST}" --record-mappings --verbose

        * Run the main method of this class

        * Replace content of src/test/resources/{__files|mappings} with that produced by the standalone runner
     */

    public static void main(String[] args) throws Exception {
        final UserAgent userAgent = new UserAgent("VipCoreConnectorWireMockRecorder");
        VipCoreLibraryRulesConnectorTest.connector = new VipCoreLibraryRulesConnector(
                VipCoreLibraryRulesConnectorTest.CLIENT, userAgent,"http://localhost:8080");
        VipCoreServiceConnectorTest.connector = new VipCoreServiceConnector(
                VipCoreServiceConnectorTest.CLIENT, userAgent,"http://localhost:8080");
        VipCoreAgencyInfoConnectorTest.connector = new VipCoreAgencyInfoConnector(
                VipCoreAgencyInfoConnectorTest.CLIENT, userAgent,"http://localhost:8080");

        final VipCoreLibraryRulesConnectorTest vipCoreLibraryRulesConnectorTest = new VipCoreLibraryRulesConnectorTest();
        final VipCoreServiceConnectorTest vipCoreServiceConnectorTest = new VipCoreServiceConnectorTest();
        final VipCoreAgencyInfoConnectorTest vipCoreAgencyInfoConnectorTest = new VipCoreAgencyInfoConnectorTest();

        libraryRulesConnectorTests(vipCoreLibraryRulesConnectorTest);
        serviceConnectorTests(vipCoreServiceConnectorTest);
        agencyInfoConnectorTests(vipCoreAgencyInfoConnectorTest);
    }

    private static void libraryRulesConnectorTests(VipCoreLibraryRulesConnectorTest connectorTest)
            throws VipCoreException {
        connectorTest.isAllowedTest();
        connectorTest.getAllowedLibraryRulesTest();
        connectorTest.getLibraryRulesByAgencyIdTestDBC();
        connectorTest.getLibraryRulesByAgencyIdTestOther();
        connectorTest.getLibrariesTest();
        connectorTest.getLibrariesCatalogingTemplateSetTest();
        connectorTest.getLibrariesCombinesRulesTest();
        connectorTest.getAllowedLibraryRules_testForErrors();
        connectorTest.getLibraryRulesByAgencyId_testForErrors();
    }

    private static void serviceConnectorTests(VipCoreServiceConnectorTest connectorTest) throws VipCoreException {
        connectorTest.getInformationTest_010100();
        connectorTest.getInformationTest_716700();
    }

    private static void agencyInfoConnectorTests(VipCoreAgencyInfoConnectorTest connectorTest) throws VipCoreException {
        connectorTest.getNameTest();
        connectorTest.getNameErrorTest();
    }
}
