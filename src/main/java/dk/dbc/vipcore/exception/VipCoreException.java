/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.exception;

public class VipCoreException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 868126125555959437L;

    public VipCoreException(String message) {
        super(message);
    }

    public VipCoreException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
