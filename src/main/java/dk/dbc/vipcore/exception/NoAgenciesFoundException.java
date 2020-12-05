/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.exception;

public class NoAgenciesFoundException extends VipCoreException {

    /**
     *
     */
    private static final long serialVersionUID = 2586680270239903460L;

    public NoAgenciesFoundException() {
        super("no_agencies_found");
    }
}
