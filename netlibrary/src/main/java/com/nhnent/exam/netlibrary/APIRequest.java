package com.nhnent.exam.netlibrary;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by gradler on 17/08/2017.
 */

public class APIRequest extends Thread {
    private static final String DEFAULT_METHOD = HttpMethod.GET;

    private String requestUrl;
    private String method;
    private String body;
    private int connectTimeout;
    private OnResultListener listener;
    private Map<String, String> headerMap;
    private Context context;

    private APIRequest(Context context, String requestUrl, String method, String body,
                       int connectTimeout, Map<String, String> headerMap) {
        this.context = context;
        this.requestUrl = requestUrl;
        this.method = method;
        this.body = body;
        this.connectTimeout = connectTimeout;
        this.headerMap = headerMap;
    }

    @Override
    public void run() {
        super.run();

        URL url;
        try {
            url = new URL(this.requestUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            sendResult(ErrorCode.INVALID_URL, e.getMessage());
            return;
        }

        boolean redirect = true;

        while (redirect) {
            HttpURLConnection conn;
            try {
                if (url.getProtocol().equals(Protocol.HTTPS)) {
                    System.out.println("try to https connection");
                    HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
                    conn = httpsConn;

                } else {
                    System.out.println("try to http connection");
                    conn = (HttpURLConnection) url.openConnection();
                }

            } catch (IOException e) {
                e.printStackTrace();
                sendResult(ErrorCode.OPEN_CONNECTION_FAILED, e.getMessage());
                return;
            }

            try {
                conn.setRequestMethod(method);
            } catch (ProtocolException e) {
                e.printStackTrace();
                sendResult(ErrorCode.INVALID_METHOD, e.getMessage());
                return;
            }

            if (connectTimeout > 0) {
                conn.setConnectTimeout(connectTimeout);
            }

            if (headerMap != null) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            conn.setDoInput(true);
            conn.setUseCaches(false);

            if (method.equalsIgnoreCase(HttpMethod.POST)
                    || method.equalsIgnoreCase(HttpMethod.PUT)) {
                System.out.println("[send] method equals POST or PUT");
                conn.setDoOutput(true);
                if (body != null && body.length() > 0) {
                    System.out.println("[send] write request body: " + body);
                    try {
                        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                        dos.writeBytes(body);
                        dos.flush();
                        dos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("[send] request body is empty");
                }
            }

            try {
                int resCode;
                resCode = conn.getResponseCode();
                System.out.println("[send] responseCode: " + resCode);
                if (resCode == HttpURLConnection.HTTP_OK
                        || resCode == HttpURLConnection.HTTP_ACCEPTED
                        || resCode == HttpURLConnection.HTTP_CREATED) {
                    int errorCode = ErrorCode.NO_ERROR;
                    BufferedReader in = null;
                    StringBuffer response = new StringBuffer();
                    try {
                        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorCode = ErrorCode.IO_EXCEPTION;
                    } finally {
                        in.close();
                    }

                    redirect = false;
                    sendResult(errorCode, response.toString());

                } else if (resCode == HttpURLConnection.HTTP_MOVED_PERM || resCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    url = new URL(conn.getHeaderField("Location"));

                } else {
                    redirect = false;
                    sendResult(resCode, conn.getResponseMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
                sendResult(ErrorCode.IO_EXCEPTION, e.getMessage());
            }
        }
    }

    public void send(OnResultListener listener) {
        this.listener = listener;

        if (TextUtils.isEmpty(this.requestUrl)) {
            System.out.println("[send] invalid parameter: url is null");
            sendResult(ErrorCode.INVALID_URL, null);
            return;
        }

        if (method == null) {
            System.out.println("[send] there is no assigned method. set default method to GET");
            method = DEFAULT_METHOD;
        }

        if (!this.requestUrl.toLowerCase().startsWith("http")) {
            this.requestUrl = "http://" + requestUrl;
        }

        System.out.println("[send] requestUrl: " + requestUrl + ", method: " + method +
                ", contentType: " + ", connectTimeout : " + connectTimeout);

        start();
    }

    public interface OnResultListener {
        void onResult(int errorCode, String result);
    }

    interface Protocol {
        String HTTP = "http";
        String HTTPS = "https";
        String TLS = "TLS";
    }

    private void sendResult(final int errorCode, final String result) {
        if (context != null) {
            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onResult(errorCode, result);
                }
            });
        } else {
            listener.onResult(errorCode, result);
        }
    }

    public static class APIRequestBuilder {
        private Context context;
        private String requestUrl;
        private String method;
        private int timeout;
        private String body;
        private Map<String, String> headerMap;

        public APIRequestBuilder(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        public APIRequestBuilder context(Context context) {
            this.context = context;
            return this;
        }

        public APIRequestBuilder method(String method) {
            this.method = method;
            return this;
        }

        public APIRequestBuilder timeout(int timeInMillis) {
            this.timeout = timeInMillis;
            return this;
        }

        public APIRequestBuilder header(Map<String, String> headerMap) {
            this.headerMap = headerMap;
            return this;
        }

        public APIRequestBuilder body(String body) {
            this.body = body;
            return this;
        }

        public APIRequest create() {
            return new APIRequest(context, requestUrl, method, body, timeout, headerMap);
        }
    }

    private void trustAll() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[] {};
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws java.security.cert.CertificateException {}

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws java.security.cert.CertificateException {}
            }
        };

        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2"); //Protocol.TLS
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    };
}