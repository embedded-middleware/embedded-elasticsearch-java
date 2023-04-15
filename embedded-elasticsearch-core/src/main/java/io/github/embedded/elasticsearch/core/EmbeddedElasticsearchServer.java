package io.github.embedded.elasticsearch.core;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Files;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.http.HttpServerTransport;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EmbeddedElasticsearchServer implements AutoCloseable {
    private final File dataDir;

    private final EmbeddedNode node;

    public EmbeddedElasticsearchServer() {
        this(new EmbeddedElasticsearchConfig());
    }

    public EmbeddedElasticsearchServer(EmbeddedElasticsearchConfig embeddedElasticsearchConfig) {
        try {
            this.dataDir = Files.newTemporaryFolder();
            this.dataDir.deleteOnExit();

            Settings settings = Settings.builder()
                    .put("cluster.name", "embedded-elasticsearch-cluster")
                    .put("http.port", embeddedElasticsearchConfig.getPort())
                    .put("node.name", "embedded-elasticsearch-node")
                    .put("path.home", dataDir.getAbsolutePath())
                    .put("transport.type", "netty4")
                    .build();
            HashMap<String, String> properties = new HashMap<>();
            Environment environment = InternalSettingsPreparer.prepareEnvironment(settings, properties,
                    null, () -> "embedded-elasticsearch-node");
            List<Class<? extends Plugin>> plugins = new ArrayList<>();
            plugins.add(Netty4Plugin.class);
            this.node = new EmbeddedNode(environment, plugins);
        } catch (Throwable e) {
            log.error("exception is ", e);
            throw new IllegalStateException("start elasticsearch standalone failed");
        }
    }

    public void start() throws Exception {
        node.start();

        long start = System.nanoTime();
        HttpClient httpClient = HttpClient.newHttpClient();

        while (true) {
            try {
                int webPort = getPort();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + webPort))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    log.info("Elasticsearch server started at port {}", webPort);
                    break;
                }
            } catch (Exception e) {
                if (System.nanoTime() - start > TimeUnit.MINUTES.toNanos(3)) {
                    log.error("Starting Elasticsearch server timed out, stopping server");
                    try {
                        this.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
                System.out.println("Starting Elasticsearch server... Exception: " + e);
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public Client client() {
        return node.client();
    }

    public int getPort() {
        HttpServerTransport httpServerTransport = node.injector().getInstance(HttpServerTransport.class);
        return httpServerTransport.boundAddress().publishAddress().getPort();
    }

    @Override
    public void close() throws IOException {
        node.close();
    }
}
