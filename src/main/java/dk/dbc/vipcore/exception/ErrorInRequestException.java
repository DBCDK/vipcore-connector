/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.exception;

public class ErrorInRequestException extends VipCoreException {

    /**
     *
     */
    private static final long serialVersionUID = 2559697170711315421L;

    public ErrorInRequestException() {
        super("error_in_request");
    }
}
