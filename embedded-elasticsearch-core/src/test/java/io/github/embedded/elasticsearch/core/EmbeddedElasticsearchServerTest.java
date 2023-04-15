package io.github.embedded.elasticsearch.core;

import org.junit.jupiter.api.Test;

public class EmbeddedElasticsearchServerTest {

    @Test
    public void testBookkeeperServerBoot() throws Exception {
        EmbeddedElasticsearchServer server = new EmbeddedElasticsearchServer();
        server.start();
        server.close();
    }
}
