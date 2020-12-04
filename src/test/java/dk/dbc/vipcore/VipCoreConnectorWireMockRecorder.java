/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore;

import dk.dbc.vipcore.exception.VipCoreException;
import dk.dbc.vipcore.libraryrules.VipCoreLibraryRulesConnector;

public class VipCoreConnectorWireMockRecorder {
        /*
        Steps to reproduce wiremock recording:

        * Start standalone runner
            java -jar wiremock-standalone-{WIRE_MOCK_VERSION}.jar --proxy-all="{RECORD_SERVICE_HOST}" --record-mappings --verbose

        * Run the main method of this class

        * Replace content of src/test/resources/{__files|mappings} with that produced by the standalone runner
     */

    public static void main(String[] args) throws Exception {
        VipCoreLibraryRulesConnectorTest.connector = new VipCoreLibraryRulesConnector(
                VipCoreLibraryRulesConnectorTest.CLIENT, "http://localhost:8080");

        final VipCoreLibraryRulesConnectorTest vipCoreLibraryRulesConnectorTest = new VipCoreLibraryRulesConnectorTest();

        libraryRulesConnectorTests(vipCoreLibraryRulesConnectorTest);
    }

    private static void libraryRulesConnectorTests(VipCoreLibraryRulesConnectorTest connectorTest)
            throws VipCoreException {
        connectorTest.isAllowedTest();
        connectorTest.getAllowedLibraryRulesTest();
        connectorTest.getLibraryRulesByAgencyIdTest();
        connectorTest.getLibrariesByLibraryRuleBooleanTest();
        connectorTest.getLibrariesByLibraryRuleStringTest();
        connectorTest.testForErrors();
    }
}
