package grails.plugin.lightweightdeploy.connector;

import com.google.common.base.Objects;

import java.io.IOException;
import java.util.Map;

public class HttpConfiguration {

    private int port = 8080;

    private int adminPort = 8081;

    private String contextPath = "/";

    private int minThreads = 8;

    private int maxThreads = 128;

    private int maxIdleTime = 200 * 1000;

    private int acceptorThreads = 1;

    private int acceptorThreadPriorityOffset = 0;

    private int acceptQueueSize = -1;

    private int maxBufferCount = 1024;

    private int requestBufferSize = 16 * 1024;

    private int requestHeaderBufferSize = 6 * 1024;

    private int responseBufferSize = 32 * 1024;

    private int responseHeaderBufferSize = 6 * 1024;

    private boolean reuseAddress = true;

    private Integer soLingerTime = null;

    private int lowResourcesMaxIdleTime = 0;

    private SessionsConfiguration sessionsConfiguration = new SessionsConfiguration();

    private SslConfiguration sslConfiguration;

    private GzipConfiguration gzipConfiguration = new GzipConfiguration();

    public HttpConfiguration(final Map<String, ?> httpConfig) throws IOException {
        this.port = Objects.firstNonNull((Integer) httpConfig.get("port"), port);
        this.adminPort = Objects.firstNonNull((Integer) httpConfig.get("adminPort"), adminPort);
        this.contextPath = Objects.firstNonNull((String) httpConfig.get("contextPath"), contextPath);
        this.minThreads = Objects.firstNonNull((Integer) httpConfig.get("minThreads"), minThreads);
        this.maxThreads = Objects.firstNonNull((Integer) httpConfig.get("maxThreads"), maxThreads);
        this.maxIdleTime = Objects.firstNonNull((Integer) httpConfig.get("maxIdleTime"), maxIdleTime);
        this.acceptQueueSize = Objects.firstNonNull((Integer) httpConfig.get("acceptQueueSize"), acceptQueueSize);
        this.acceptorThreads = Objects.firstNonNull((Integer) httpConfig.get("acceptorThreads"), acceptorThreads);
        this.acceptorThreadPriorityOffset = Objects.firstNonNull((Integer) httpConfig.get("acceptorThreadPriorityOffset"), acceptorThreadPriorityOffset);
        this.maxBufferCount = Objects.firstNonNull((Integer) httpConfig.get("maxBufferCount"), maxBufferCount);
        this.requestBufferSize = Objects.firstNonNull((Integer) httpConfig.get("requestBufferSize"), requestBufferSize);
        this.requestHeaderBufferSize = Objects.firstNonNull((Integer) httpConfig.get("requestHeaderBufferSize"), requestHeaderBufferSize);
        this.responseBufferSize = Objects.firstNonNull((Integer) httpConfig.get("responseBufferSize"), responseBufferSize);
        this.responseHeaderBufferSize = Objects.firstNonNull((Integer) httpConfig.get("responseHeaderBufferSize"), responseHeaderBufferSize);
        this.reuseAddress = Objects.firstNonNull((Boolean) httpConfig.get("reuseAddress"), reuseAddress);
        this.soLingerTime = (Integer) httpConfig.get("soLingerTime");
        this.lowResourcesMaxIdleTime = Objects.firstNonNull((Integer) httpConfig.get("lowResourcesMaxIdleTime"), lowResourcesMaxIdleTime);

        if (httpConfig.containsKey("sessions")) {
            this.sessionsConfiguration = new SessionsConfiguration((Map<String, ?>) httpConfig.get("sessions"));
        }

        if (httpConfig.containsKey("ssl")) {
            Map<String, ?> sslConfig = (Map<String, ?>) httpConfig.get("ssl");
            this.sslConfiguration = new SslConfiguration(sslConfig);
        }

        if (httpConfig.containsKey("gzip")) {
            this.gzipConfiguration = new GzipConfiguration((Map<String, ?>) httpConfig.get("gzip"));
        }
    }

    public int getPort() {
        return port;
    }

    public Integer getAdminPort() {
        return adminPort;
    }

    public String getContextPath() {
        return contextPath;
    }

    public int getMinThreads() {
        return minThreads;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public boolean hasAdminPort() {
        return getAdminPort() != null;
    }

    public SslConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public int getAcceptorThreads() {
        return acceptorThreads;
    }

    public int getAcceptorThreadPriorityOffset() {
        return acceptorThreadPriorityOffset;
    }

    public int getAcceptQueueSize() {
        return acceptQueueSize;
    }

    public int getMaxBufferCount() {
        return maxBufferCount;
    }

    public int getRequestBufferSize() {
        return requestBufferSize;
    }

    public int getRequestHeaderBufferSize() {
        return requestHeaderBufferSize;
    }

    public int getResponseBufferSize() {
        return responseBufferSize;
    }

    public int getResponseHeaderBufferSize() {
        return responseHeaderBufferSize;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public Integer getSoLingerTime() {
        return soLingerTime;
    }

    public int getLowResourcesMaxIdleTime() {
        return lowResourcesMaxIdleTime;
    }

    public boolean isMixedMode() {
        return isSsl() && sslConfiguration.getPort() != null && sslConfiguration.getPort() != port;
    }

    public boolean isSsl() {
        return (getSslConfiguration() != null);
    }

    public SessionsConfiguration getSessionsConfiguration() {
        return sessionsConfiguration;
    }

    public GzipConfiguration getGzipConfiguration() {
        return gzipConfiguration;
    }

}
