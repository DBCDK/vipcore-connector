/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.service;

import dk.dbc.vipcore.exception.VipCoreException;

public class VipCoreServiceConnectorException extends VipCoreException {

    public VipCoreServiceConnectorException(String message) {
        super(message);
    }

    public VipCoreServiceConnectorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
