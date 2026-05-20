package dk.dbc.vipcore.agencyinfo;

import dk.dbc.vipcore.exception.VipCoreException;

import java.io.Serial;

public class VipCoreAgencyInfoConnectorException extends VipCoreException {

    @Serial
    private static final long serialVersionUID = 1844088107595420961L;

    public VipCoreAgencyInfoConnectorException(String message) {
        super(message);
    }

    public VipCoreAgencyInfoConnectorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
