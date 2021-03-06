/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.exception;

public class ServiceUnavailableException extends VipCoreException {

    /**
     *
     */
    private static final long serialVersionUID = 6212049466598679047L;

    public ServiceUnavailableException() {
        super("service_unavailable");
    }
}
