/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.exception;

public class ErrorInRequestException extends VipCoreException {

    public ErrorInRequestException() {
        super("error_in_request");
    }
}
