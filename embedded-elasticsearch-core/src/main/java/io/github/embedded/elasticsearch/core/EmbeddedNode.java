package io.github.embedded.elasticsearch.core;

import org.elasticsearch.env.Environment;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;

import java.util.Collection;

public class EmbeddedNode extends Node {
    protected EmbeddedNode(final Environment initialEnvironment,
                           Collection<Class<? extends Plugin>> classpathPlugins) {
        super(initialEnvironment, classpathPlugins, false);
    }
}
