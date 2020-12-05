/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.exception;

public class ProfileNotFoundException extends VipCoreException {

    /**
     *
     */
    private static final long serialVersionUID = -2210120792555756098L;

    public ProfileNotFoundException() {
        super("profile_not_found");
    }
}
