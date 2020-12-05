/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.exception;

public class AuthenticationErrorException extends VipCoreException {
    /**
     *
     */
    private static final long serialVersionUID = 4475500850148307809L;

    public AuthenticationErrorException() {
        super("authentication_error");
    }
}
