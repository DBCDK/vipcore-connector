package dk.dbc.vipcore;

import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpPost;
import dk.dbc.invariant.InvariantUtil;
import dk.dbc.util.Stopwatch;
import dk.dbc.vipcore.dto.ErrorMessageDTO;
import dk.dbc.vipcore.exception.AgencyNotFoundException;
import dk.dbc.vipcore.exception.AuthenticationErrorException;
import dk.dbc.vipcore.exception.ErrorInRequestException;
import dk.dbc.vipcore.exception.NoAgenciesFoundException;
import dk.dbc.vipcore.exception.NoUserIdSelectedException;
import dk.dbc.vipcore.exception.ProfileNotFoundException;
import dk.dbc.vipcore.exception.ServiceUnavailableException;
import dk.dbc.vipcore.exception.VipCoreException;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public abstract class VipCoreConnector {
    public enum TimingLogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(VipCoreConnector.class);

    protected static final RetryPolicy RETRY_POLICY = new RetryPolicy()
            .retryOn(Collections.singletonList(ProcessingException.class))
            .retryIf((Response response) -> response.getStatus() == 500
                    || response.getStatus() == 502)
            .withDelay(10, TimeUnit.SECONDS)
            .withMaxRetries(3);

    protected final FailSafeHttpClient failSafeHttpClient;
    private final String baseUrl;
    private final LogLevelMethod logger;

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for vipcore service endpoint
     */
    protected VipCoreConnector(Client httpClient, String baseUrl) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for vipcore service endpoint
     * @param level      timings log level
     */
    protected VipCoreConnector(Client httpClient, String baseUrl, TimingLogLevel level) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, level);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for vipcore service endpoint
     */
    protected VipCoreConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl) {
        this(failSafeHttpClient, baseUrl, TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for vipcore service endpoint
     * @param level              timings log level
     */
    protected VipCoreConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl, TimingLogLevel level) {
        this.failSafeHttpClient = InvariantUtil.checkNotNullOrThrow(
                failSafeHttpClient, "failSafeHttpClient");
        this.baseUrl = InvariantUtil.checkNotNullNotEmptyOrThrow(
                baseUrl, "baseUrl");
        switch (level) {
            case TRACE:
                logger = LOGGER::trace;
                break;
            case DEBUG:
                logger = LOGGER::debug;
                break;
            case INFO:
                logger = LOGGER::info;
                break;
            case WARN:
                logger = LOGGER::warn;
                break;
            case ERROR:
                logger = LOGGER::error;
                break;
            default:
                logger = LOGGER::info;
                break;
        }
    }

    public void close() {
        failSafeHttpClient.getClient().close();
    }

    protected <S, T> T postRequest(String basePath,
                                   String data,
                                   Class<T> type) throws VipCoreException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final HttpPost httpPost = new HttpPost(failSafeHttpClient)
                    .withBaseUrl(baseUrl)
                    .withPathElements(basePath)
                    .withData(data, "application/json")
                    .withHeader("Accept", "application/json");
            final Response response = httpPost.execute();
            assertResponseStatus(response, Response.Status.OK);
            return readResponseEntity(response, type);
        } finally {
            logger.log("POST /{} took {} milliseconds",
                    basePath,
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    private <T> T readResponseEntity(Response response, Class<T> type)
            throws VipCoreException {
        final T entity = response.readEntity(type);
        if (entity == null) {
            throw new VipCoreException(
                    String.format("VipCore service returned with null-valued %s entity",
                            type.getName()));
        }
        return entity;
    }

    protected void assertResponseStatus(Response response, Response.Status expectedStatus)
            throws VipCoreException {
        final Response.Status actualStatus =
                Response.Status.fromStatusCode(response.getStatus());
        if (actualStatus != expectedStatus) {
            final ErrorMessageDTO errorMessage = readResponseEntity(response, ErrorMessageDTO.class);

            switch (errorMessage.getError()) {
                case "authentication_error":
                    throw new AuthenticationErrorException();
                case "service_unavailable":
                    throw new ServiceUnavailableException();
                case "agency_not_found":
                    throw new AgencyNotFoundException();
                case "error_in_request":
                    throw new ErrorInRequestException();
                case "no_agencies_found":
                    throw new NoAgenciesFoundException();
                case "no_userid_selected":
                    throw new NoUserIdSelectedException();
                case "profile_not_found":
                    throw new ProfileNotFoundException();
                default:
                    throw new VipCoreException(errorMessage.getError());
            }
        }
    }

    @FunctionalInterface
    interface LogLevelMethod {
        void log(String format, Object... objs);
    }
}
