/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.vipcore.exception;

public class NoUserIdSelectedException extends VipCoreException {

    /**
     *
     */
    private static final long serialVersionUID = 903152243614742548L;

    public NoUserIdSelectedException() {
        super("no_userid_selected");
    }
}
