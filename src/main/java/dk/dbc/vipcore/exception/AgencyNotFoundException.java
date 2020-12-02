/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.exception;

public class AgencyNotFoundException extends VipCoreException {

    public AgencyNotFoundException() {
        super("agency_not_found");
    }
}
