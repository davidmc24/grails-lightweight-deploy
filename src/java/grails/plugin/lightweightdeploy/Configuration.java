package grails.plugin.lightweightdeploy;

import grails.plugin.lightweightdeploy.connector.SslConfiguration;
import grails.plugin.lightweightdeploy.jmx.JmxConfiguration;
import grails.plugin.lightweightdeploy.logging.LoggingConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Stores the configuration for the jetty server
 */
public class Configuration {

    private Integer port;
    private SslConfiguration sslConfiguration;
    private Integer adminPort;
    private LoggingConfiguration serverLogConfiguration;
    private LoggingConfiguration requestLogConfiguration;
    private File workDir;
    private JmxConfiguration jmxConfiguration;
    private int minThreads = 8;
    private int maxThreads = 128;

    public Configuration(Map<String, ?> config) throws IOException {
        init(config);
    }

    public Configuration(String ymlFilePath) throws IOException {
        Map<String, ?> config = (Map<String, ?>) new Yaml().load(new FileReader(new File(ymlFilePath)));
        init(config);
    }

    protected void init(Map<String, ?> config) throws IOException {
        initHttp(config);
        initLogging(config);
        initJmx(config);
    }

    protected void initHttp(Map<String, ?> config) throws IOException {
        Map<String, ?> httpConfig = (Map<String, ?>) config.get("http");

        this.port = (Integer) httpConfig.get("port");
        if (httpConfig.containsKey("ssl")) {
            Map<String, ?> sslConfig = (Map<String, ?>) httpConfig.get("ssl");
            this.sslConfiguration = new SslConfiguration(sslConfig);
        }

        this.adminPort = null;
        if (httpConfig.containsKey("adminPort")) {
            this.adminPort = (Integer) httpConfig.get("adminPort");
        }

        if (httpConfig.containsKey("minThreads")) {
            this.minThreads = (Integer) httpConfig.get("minThreads");
        }
        if (httpConfig.containsKey("maxThreads")) {
            this.maxThreads = (Integer) httpConfig.get("maxThreads");
        }
    }

    protected void initJmx(Map<String, ?> config) {
        if (config.containsKey("jmx")) {
            Map<String, ?> jmxConfig = (Map<String, ?>) config.get("jmx");
            Integer registryPort = (Integer) jmxConfig.get("registryPort");
            Integer serverPort = (Integer) jmxConfig.get("serverPort");
            if (registryPort == null || serverPort == null) {
                throw new IllegalArgumentException("Both server and registry port must be present for jmx");
            }
            this.jmxConfiguration = new JmxConfiguration(registryPort, serverPort);
        }
    }

    protected void initLogging(Map<String, ?> config) {
        initRequestLogging(config);
        initServerLogging(config);
        initWorkDir(config);
    }

    protected void initRequestLogging(Map<String, ?> config) {
        Map<String, ?> httpConfig = (Map<String, ?>) config.get("http");
        if (httpConfig.containsKey("requestLog")) {
            requestLogConfiguration = new LoggingConfiguration((Map<String, ?>) httpConfig.get("requestLog"));
        }
    }

    protected void initServerLogging(Map<String, ?> config) {
        if (config.containsKey("logging")) {
            serverLogConfiguration = new LoggingConfiguration((Map<String, ?>) config.get("logging"));
        }
    }

    protected void initWorkDir(Map<String, ?> config) {
        if (config.containsKey("workDir")) {
            this.workDir = new File((String) config.get("workDir"));
        } else {
            this.workDir = new File(System.getProperty("java.io.tmpdir"));
        }
    }

    public Integer getPort() {
        return port;
    }

    public Integer getAdminPort() {
        return adminPort;
    }

    public JmxConfiguration getJmxConfiguration() {
        return jmxConfiguration;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getMinThreads() {
        return minThreads;
    }

    public boolean hasAdminPort() {
        return getAdminPort() != null;
    }

    public boolean isJmxEnabled() {
        return (this.jmxConfiguration != null);
    }

    public boolean isRequestLoggingEnabled() {
        return (this.requestLogConfiguration != null);
    }

    public boolean isServerLoggingEnabled() {
        return (this.serverLogConfiguration != null);
    }

    public boolean isMixedMode() {
        return isSsl() && sslConfiguration.getPort() != null && sslConfiguration.getPort() != port;
    }

    public boolean isSsl() {
        return (this.sslConfiguration != null);
    }

    public SslConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

    public LoggingConfiguration getServerLogConfiguration() {
        return serverLogConfiguration;
    }

    public LoggingConfiguration getRequestLogConfiguration() {
        return requestLogConfiguration;
    }

    public File getWorkDir() {
        return workDir;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "port=" + port +
                ", adminPort=" + adminPort +
                ", ssl=" + isSsl() +
                '}';
    }

}
