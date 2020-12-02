/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.exception;

public class AuthenticationErrorException extends VipCoreException {
    public AuthenticationErrorException() {
        super("authentication_error");
    }
}
