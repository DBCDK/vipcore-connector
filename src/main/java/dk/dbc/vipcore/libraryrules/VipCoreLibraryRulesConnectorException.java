/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.libraryrules;

import dk.dbc.vipcore.exception.VipCoreException;

public class VipCoreLibraryRulesConnectorException extends VipCoreException {
    public VipCoreLibraryRulesConnectorException(String message) {
        super(message);
    }

    public VipCoreLibraryRulesConnectorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
