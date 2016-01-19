package io.github.stevenrudenko.iot.sample.mqtt;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/** Certificates utilities. */
public final class CertUtils {
    /** Log tag. */
    private static final String TAG = CertUtils.class.getSimpleName();

    private CertUtils() {
        // hide
    }

    /**
     * Provides SSL socket factory with server certificate included to key store.
     * @param context to read assets.
     * @param certFilename server certificate file asset path.
     * @return SSL socket factory with server certificate included to key store.
     */
    @Nullable
    public static SSLSocketFactory getCertificate(Context context, String certFilename) {
        final CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            Log.e(TAG, "Fail to initialize SSL algorithm", e);
            return null;
        }

        // Load CAs from an assets
        InputStream caInput = null;
        Certificate ca;
        try {
            caInput = new BufferedInputStream(context.getAssets().open(certFilename));
            ca = cf.generateCertificate(caInput);
        } catch (CertificateException | IOException e) {
            Log.e(TAG, "Fail to read certificate", e);
            return null;
        } finally {
            try {
                if (caInput != null) {
                    caInput.close();
                }
            } catch (IOException ignore) {
            }
        }

        // Create a KeyStore containing our trusted CAs
        final String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
            Log.e(TAG, "Fail to set certificate entry", e);
            return null;
        }

        // Create a TrustManager that trusts the CAs in our KeyStore
        TrustManagerFactory tmf;
        try {
            final String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            Log.e(TAG, "Fail to initialise trust manager", e);
            return null;
        }

        // Create an SSLContext that uses our TrustManager
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            Log.e(TAG, "Fail to initialise SSL context", e);
            return null;
        }
        return sslContext.getSocketFactory();
    }

}
