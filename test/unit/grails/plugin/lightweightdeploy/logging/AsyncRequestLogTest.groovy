
package grails.plugin.lightweightdeploy.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.codahale.metrics.Clock;
import org.eclipse.jetty.http.HttpHeaders;
import javax.servlet.http.Cookie;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.*;
import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.junit.Test;

import java.security.Principal;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AsyncRequestLogTest {

    private final Clock clock = mock(Clock.class);
    private final List<String> cookies = ["wgid", "s_fid"];
    @SuppressWarnings("unchecked")
    private final Appender<ILoggingEvent> appender = mock(Appender.class);
    private final AppenderAttachableImpl<ILoggingEvent> appenders = new AppenderAttachableImpl<ILoggingEvent>();
    private final AsyncRequestLog asyncRequestLog = new AsyncRequestLog(clock, appenders, TimeZone.getTimeZone("UTC"), cookies);

    private final Request request = mock(Request.class);
    private final Response response = mock(Response.class);
    private final AsyncContinuation continuation = mock(AsyncContinuation.class);

    @Before
    public void setUp() throws Exception {
        when(continuation.isInitial()).thenReturn(true);

        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getTimeStamp()).thenReturn(TimeUnit.SECONDS.toMillis(1353042047));
        when(request.getMethod()).thenReturn("GET");
        when(request.getUri()).thenReturn(new HttpURI("/test/things?yay"));
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getAsyncContinuation()).thenReturn(continuation);
        when(request.getDispatchTime()).thenReturn(TimeUnit.SECONDS.toMillis(1353042048));
        when(request.getCookies()).thenReturn(null);

        when(response.getStatus()).thenReturn(200);
        when(response.getContentCount()).thenReturn(8290L);

        when(clock.getTime()).thenReturn(TimeUnit.SECONDS.toMillis(1353042049));

        appenders.addAppender(appender);

        asyncRequestLog.start();
    }

    @After
    public void tearDown() throws Exception {
        if (asyncRequestLog.isRunning()) {
            asyncRequestLog.stop();
        }
    }

    @Test
    public void startsAndStops() throws Exception {
        asyncRequestLog.stop();

        verify(appender, timeout(1000)).stop();
    }

    @Test
    public void logsRequests() throws Exception {
        final ILoggingEvent event = logAndCapture();

        assertThat(event.getFormattedMessage())
                .isEqualTo("10.0.0.1 - - [16/Nov/2012:05:00:47 +0000] \"GET /test/things?yay HTTP/1.1\" 200 8290 \"-\" \"-\" 1000 2000 null \"\"");

        assertThat(event.getLevel())
                .isEqualTo(Level.INFO);
    }

    @Test
    public void logsForwardedFor() throws Exception {
        when(request.getHeader(HttpHeaders.X_FORWARDED_FOR)).thenReturn("123.123.123.123");

        final ILoggingEvent event = logAndCapture();
        assertThat(event.getFormattedMessage())
                .isEqualTo("123.123.123.123 - - [16/Nov/2012:05:00:47 +0000] \"GET /test/things?yay HTTP/1.1\" 200 8290 \"-\" \"-\" 1000 2000 null \"\"");
    }

    @Test
    public void logsPrincipal() throws Exception {
        final Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("coda");

        final UserIdentity identity = mock(UserIdentity.class);
        when(identity.getUserPrincipal()).thenReturn(principal);

        final Authentication.User user = mock(Authentication.User.class);
        when(user.getUserIdentity()).thenReturn(identity);

        when(request.getAuthentication()).thenReturn(user);

        final ILoggingEvent event = logAndCapture();
        assertThat(event.getFormattedMessage())
                .isEqualTo("10.0.0.1 - coda [16/Nov/2012:05:00:47 +0000] \"GET /test/things?yay HTTP/1.1\" 200 8290 \"-\" \"-\" 1000 2000 null \"\"");
    }

    @Test
    public void logsAsyncContinuations() throws Exception {
        when(continuation.isInitial()).thenReturn(false);

        final ILoggingEvent event = logAndCapture();

        assertThat(event.getFormattedMessage())
                .isEqualTo("10.0.0.1 - - [16/Nov/2012:05:00:47 +0000] \"GET /test/things?yay HTTP/1.1\" Async 8290 \"-\" \"-\" 1000 2000 null \"\"");
    }

    @Test
    public void logsReferer() throws Exception {
        when(request.getHeader(HttpHeaders.REFERER)).thenReturn("referer");

        final ILoggingEvent event = logAndCapture();

        assertThat(event.getFormattedMessage())
                .isEqualTo("10.0.0.1 - - [16/Nov/2012:05:00:47 +0000] \"GET /test/things?yay HTTP/1.1\" 200 8290 \"referer\" \"-\" 1000 2000 null \"\"");
    }

    @Test
    public void logsUserAgent() throws Exception {
        when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn("UA/1.0");

        final ILoggingEvent event = logAndCapture();

        assertThat(event.getFormattedMessage())
                .isEqualTo("10.0.0.1 - - [16/Nov/2012:05:00:47 +0000] \"GET /test/things?yay HTTP/1.1\" 200 8290 \"-\" \"UA/1.0\" 1000 2000 null \"\"");
    }

    @Test
    public void logsRequestId() throws Exception {
        when(request.getAttribute("requestId")).thenReturn("requestId");

        final ILoggingEvent event = logAndCapture();

        assertThat(event.getFormattedMessage())
                .isEqualTo("10.0.0.1 - - [16/Nov/2012:05:00:47 +0000] \"GET /test/things?yay HTTP/1.1\" 200 8290 \"-\" \"-\" 1000 2000 requestId \"\"");
    }

    @Test
    public void logsTrackingCookie() throws Exception {
        Cookie[] cookies = new Cookie[2];
        cookies[0] = new Cookie("wgid", "test");
        cookies[1] = new Cookie("s_fid", "omniture");
        when(request.getCookies()).thenReturn(cookies);

        final ILoggingEvent event = logAndCapture();

        assertThat(event.getFormattedMessage())
                .isEqualTo("10.0.0.1 - - [16/Nov/2012:05:00:47 +0000] \"GET /test/things?yay HTTP/1.1\" 200 8290 \"-\" \"-\" 1000 2000 null \"wgid=test; s_fid=omniture\"");
    }

    private ILoggingEvent logAndCapture() {
        asyncRequestLog.log(request, response);

        final ArgumentCaptor<ILoggingEvent> captor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, timeout(1000)).doAppend(captor.capture());

        return captor.getValue();
    }

}
