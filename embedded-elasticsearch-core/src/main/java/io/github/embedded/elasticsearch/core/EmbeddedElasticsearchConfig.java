package io.github.embedded.elasticsearch.core;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmbeddedElasticsearchConfig {
    private int port;

    public EmbeddedElasticsearchConfig() {
    }

    public EmbeddedElasticsearchConfig port(int port) {
        this.port = port;
        return this;
    }
}
