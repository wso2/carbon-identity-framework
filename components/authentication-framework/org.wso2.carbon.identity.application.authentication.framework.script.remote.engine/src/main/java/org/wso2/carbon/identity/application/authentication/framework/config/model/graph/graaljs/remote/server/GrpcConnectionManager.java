package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.server;

import io.grpc.ChannelCredentials;
import io.grpc.ConnectivityState;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.JsGraalGraphEngineModeRouter;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.utils.security.KeystoreUtils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cleaner version of GrpcConnectionManager using immutable pool holder.
 */
public class GrpcConnectionManager {

    private static final Log log = LogFactory.getLog(GrpcConnectionManager.class);

    // Singleton
    private static volatile GrpcConnectionManager instance;
    private static final Object lock = new Object();

    // Immutable holder for pool state
    private static final class ChannelPool {
        final ManagedChannel[] channels;
        final String target;

        ChannelPool(ManagedChannel[] channels, String target) {
            this.channels = channels;
            this.target = target;
        }
    }

    private volatile ChannelPool pool;
    private final AtomicInteger channelIndex = new AtomicInteger(0);

    private int channelPoolSize = 4;
    private int channelIdleTimeout = 180;

    private GrpcConnectionManager() {
        loadConfiguration();
    }

    public static GrpcConnectionManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new GrpcConnectionManager();
                }
            }
        }
        return instance;
    }

    /**
     * lock-free channel selection
     */
    public ManagedChannel getClientChannel(String target) {
        ChannelPool current = pool;

        if (current != null && current.target.equals(target)) {
            int index = (channelIndex.getAndIncrement() & Integer.MAX_VALUE)
                    % current.channels.length;
            return current.channels[index];
        }

        return createOrReplacePool(target);
    }

    /**
     * synchronized pool creation/replacement
     */
    private synchronized ManagedChannel createOrReplacePool(String target) {
        ChannelPool current = pool;

        // Double check
        if (current != null && current.target.equals(target)) {
            return pick(current);
        }

        log.info("[GrpcConnectionManager] Creating channel pool of size " +
                channelPoolSize + " for target: " + target);

        // Build the new pool BEFORE touching the old one. If createChannel fails,
        // the old pool keeps serving traffic instead of leaving the singleton
        // pointing to a pool of shut-down channels.
        ManagedChannel[] channels = new ManagedChannel[channelPoolSize];

        try {
            for (int i = 0; i < channels.length; i++) {
                channels[i] = createChannel(target);
            }
        } catch (RuntimeException e) {
            for (ManagedChannel ch : channels) {
                if (ch != null) {
                    ch.shutdownNow();
                }
            }
            throw e;
        }

        ChannelPool newPool = new ChannelPool(channels, target);
        pool = newPool;
        channelIndex.set(0);

        if (current != null) {
            log.info("[GrpcConnectionManager] Target changed, shutting down old pool");
            shutdown(current);
        }

        return newPool.channels[0];
    }

    private ManagedChannel pick(ChannelPool pool) {
        int index = (channelIndex.getAndIncrement() & Integer.MAX_VALUE)
                % pool.channels.length;
        return pool.channels[index];
    }

    /**
     * health check
     */
    public boolean isClientChannelConnected() {
        ChannelPool current = pool;
        if (current == null) {
            return false;
        }

        boolean tracing = JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled();
        boolean anyAlive = false;

        for (int i = 0; i < current.channels.length; i++) {
            ManagedChannel ch = current.channels[i];
            if (ch == null) {
                continue;
            }
            boolean alive = !ch.isShutdown() && !ch.isTerminated();
            if (alive) {
                anyAlive = true;
            }
            if (tracing) {
                // Passive probe: getState(false) does NOT force a connection attempt.
                ConnectivityState state = alive ? ch.getState(false) : null;
                log.debug("channel[" + i + "] target=" + current.target +
                        " shutdown=" + ch.isShutdown() + " terminated=" + ch.isTerminated() +
                        " state=" + state);
            }
        }

        return anyAlive;
    }

    /**
     * Shutdown all channels
     */
    public synchronized void shutdown() {
        if (pool != null) {
            log.info("[GrpcConnectionManager] Shutting down all channels");
            shutdown(pool);
            pool = null;
            channelIndex.set(0);
        }
    }

    private void shutdown(ChannelPool pool) {
        for (int i = 0; i < pool.channels.length; i++) {
            ManagedChannel ch = pool.channels[i];
            if (ch != null) {
                try {
                    ch.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    ch.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Configuration loading
     */
    private void loadConfiguration() {

        String idleTimeoutStr = IdentityUtil.getProperty("AdaptiveAuth.GraalJS.GrpcChannelIdleTimeout");
        if (idleTimeoutStr != null) {
            try {
                channelIdleTimeout = Integer.parseInt(idleTimeoutStr);
            } catch (NumberFormatException e) {
                log.warn("Invalid idle timeout: " + idleTimeoutStr);
            }
        }

        String poolSizeStr = IdentityUtil.getProperty("AdaptiveAuth.GraalJS.GrpcChannelPoolSize");
        if (poolSizeStr != null) {
            try {
                int size = Integer.parseInt(poolSizeStr);
                if (size > 0) {
                    channelPoolSize = size;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid pool size: " + poolSizeStr);
            }
        }

        log.info("Config loaded - PoolSize: " + channelPoolSize +
                ", IdleTimeout: " + channelIdleTimeout);
    }

    /**
     * Channel creation. mTLS is mandatory.
     * <p>
     * The channel carries the full {@code JsAuthenticationContext} (username, tenant,
     * userstore domain, claims, session id) plus host-function payloads such as bulk
     * user-attribute results. None of that may travel over a plaintext gRPC channel,
     * so there is no plaintext branch — if credentials cannot be built from the Carbon
     * primary keystore + truststore the framework refuses to operate the channel and
     * every subsequent request fails fast with a {@link FrameworkRuntimeException}
     * until the operator fixes the configuration.
     */
    private ManagedChannel createChannel(String target) {
        try {
            ChannelCredentials credentials = buildCredentialsFromCarbonKeystore();
            return Grpc.newChannelBuilder(target, credentials)
                    .idleTimeout(channelIdleTimeout, TimeUnit.SECONDS)
                    .build();
        } catch (Exception e) {
            throw new FrameworkRuntimeException(
                    "Failed to init mTLS channel from Carbon primary keystore: " + e.getMessage(), e);
        }
    }

    /**
     * Build TLS credentials from the Carbon primary keystore + client truststore.
     * <p>
     * Reads {@code Security.KeyStore.*} and {@code Security.TrustStore.*} from
     * {@link ServerConfiguration} (carbon.xml / deployment.toml). The same pattern is used by
     * {@code ClientAuthX509TrustManager} and {@code ThriftAuthenticationServiceComponent}.
     */
    private ChannelCredentials buildCredentialsFromCarbonKeystore() throws Exception {

        ServerConfiguration sc = ServerConfiguration.getInstance();

        String ksLocation = sc.getFirstProperty("Security.KeyStore.Location");
        String ksType     = sc.getFirstProperty("Security.KeyStore.Type");
        String ksPassword = sc.getFirstProperty("Security.KeyStore.Password");
        String ksKeyPassword = sc.getFirstProperty("Security.KeyStore.KeyPassword");

        String tsLocation = sc.getFirstProperty("Security.TrustStore.Location");
        String tsType     = sc.getFirstProperty("Security.TrustStore.Type");
        String tsPassword = sc.getFirstProperty("Security.TrustStore.Password");

        if (ksLocation == null || ksType == null || ksPassword == null) {
            throw new IllegalStateException("Security.KeyStore.* is incomplete in server configuration");
        }
        if (tsLocation == null || tsType == null || tsPassword == null) {
            throw new IllegalStateException("Security.TrustStore.* is incomplete in server configuration");
        }
        // KeyPassword is optional — falls back to the keystore password if not set.
        if (ksKeyPassword == null) {
            ksKeyPassword = ksPassword;
        }

        if (JsGraalGraphEngineModeRouter.isTracingEnabled()) {
            log.debug("[GrpcConnectionManager] Loading mTLS material from Carbon keystore: " + ksLocation +
                    ", truststore: " + tsLocation);
        }

        KeyManager[] keyManagers = loadKeyManagers(ksLocation, ksType, ksPassword, ksKeyPassword);
        TrustManager[] trustManagers = loadTrustManagers(tsLocation, tsType, tsPassword);

        return TlsChannelCredentials.newBuilder()
                .keyManager(keyManagers)
                .trustManager(trustManagers)
                .build();
    }

    private static KeyManager[] loadKeyManagers(String location, String type,
                                                String storePassword, String keyPassword) throws Exception {

        KeyStore ks = KeystoreUtils.getKeystoreInstance(type);
        try (InputStream in = new FileInputStream(location)) {
            ks.load(in, storePassword.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyPassword.toCharArray());
        return kmf.getKeyManagers();
    }

    private static TrustManager[] loadTrustManagers(String location, String type, String password) throws Exception {

        KeyStore ts = KeystoreUtils.getKeystoreInstance(type);
        try (InputStream in = new FileInputStream(location)) {
            ts.load(in, password.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);
        return tmf.getTrustManagers();
    }

    // Optional setters

    public void setChannelPoolSize(int size) {
        if (size > 0) {
            this.channelPoolSize = size;
        }
    }

    public void setChannelIdleTimeout(int seconds) {
        this.channelIdleTimeout = seconds;
    }

    public int getChannelPoolSize() {
        return channelPoolSize;
    }
}
