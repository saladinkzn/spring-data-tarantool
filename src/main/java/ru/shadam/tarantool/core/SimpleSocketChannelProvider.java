package ru.shadam.tarantool.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tarantool.SocketChannelProvider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author sala
 */
public class SimpleSocketChannelProvider implements SocketChannelProvider {
    private static final Logger logger = LoggerFactory.getLogger(SimpleSocketChannelProvider.class);

    private final String host;
    private final int port;

    public SimpleSocketChannelProvider(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public SocketChannel get(int retryNumber, Throwable lastError) {
        if(lastError != null) {
            logger.error(lastError.getMessage(), lastError);
        }
        try {
            return SocketChannel.open(new InetSocketAddress(host, port));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
