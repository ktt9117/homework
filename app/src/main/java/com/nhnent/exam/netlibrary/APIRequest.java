package com.nhnent.exam.netlibrary;

import android.text.TextUtils;

import java.io.BufferedReader;
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


public class APIRequest {

    private static final String DEFAULT_METHOD = HttpMethod.GET;

    private String requestUrl;
    private String method;
    private String contentType;
    private int connectTimeout;
    private Map<String, String> headerMap;

    private APIRequest(String requestUrl, String method, String contentType,
                       int connectTimeout, Map<String, String> headerMap) {
        this.requestUrl = requestUrl;
        this.method = method;
        this.contentType = contentType;
        this.connectTimeout = connectTimeout;
        this.headerMap = headerMap;
    }

    public void send(OnResultListener listener) {
        if (TextUtils.isEmpty(this.requestUrl)) {
            System.out.println("[send] invalid parameter: url is null");
            listener.onResult(ErrorCode.INVALID_URL, null);
            return;
        }

        if (method == null) {
            System.out.println("[send] there is no assigned method. set default method to GET");
            method = DEFAULT_METHOD;
        }

        if (!this.requestUrl.toLowerCase().startsWith("http")) {
            this.requestUrl = "http://" + requestUrl;
        }

        System.out.println("[send] requestUrl: " + requestUrl + ", method: " + method + ", contentType: " + contentType + ", connectTimeout : " + connectTimeout);

        URL url;
        try {
            url = new URL(this.requestUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            listener.onResult(ErrorCode.INVALID_URL, e.getMessage());
            return;
        }

        boolean redirect = true;

        while (redirect) {
            HttpURLConnection conn;
            try {
                if (url.getProtocol().equals("https")) {
                    System.out.println("try to https connection");
                    // TODO: This is dangerous way. You need to refactoring it later!
                    trustAll();
                    HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
                    httpsConn.setHostnameVerifier(HOSTNAME_VERIFIER);
                    conn = httpsConn;

                } else {
                    System.out.println("try to http connection");
                    conn = (HttpURLConnection) url.openConnection();
                }

            } catch (IOException e) {
                e.printStackTrace();
                listener.onResult(ErrorCode.OPEN_CONNECTION_FAILED, e.getMessage());
                return;
            }

            try {
                conn.setRequestMethod(method);
            } catch (ProtocolException e) {
                e.printStackTrace();
                listener.onResult(ErrorCode.INVALID_METHOD, e.getMessage());
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

            if (method.equalsIgnoreCase(HttpMethod.POST)) {
                conn.setDoOutput(true);
                // TODO: if you need to pass request body, you should implement here.
            }

            try {
                int resCode;
                resCode = conn.getResponseCode();
                System.out.println("[send] responseCode: " + resCode);
                if (resCode == HttpURLConnection.HTTP_OK || resCode == HttpURLConnection.HTTP_ACCEPTED) {
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
                    listener.onResult(errorCode, response.toString());

                } else if (resCode == HttpURLConnection.HTTP_MOVED_PERM || resCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    url = new URL(conn.getHeaderField("Location"));

                } else {
                    redirect = false;
                    listener.onResult(resCode, conn.getResponseMessage());
                }

            } catch (IOException e) {
                e.printStackTrace();
                listener.onResult(ErrorCode.IO_EXCEPTION, e.getMessage());
            }
        }
    }

    interface OnResultListener {
        void onResult(int errorCode, String result);
    }

    public static class APIRequestBuilder {

        private String requestUrl;
        private String method;
        private String contentType;
        private int timeout;
        private Map<String, String> headerMap;

        public APIRequestBuilder(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        public APIRequestBuilder method(String method) {
            this.method = method;
            return this;
        }

        public APIRequestBuilder contentType(String contentType) {
            this.contentType = contentType;
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

        public APIRequest create() {
            return new APIRequest(requestUrl, method, contentType, timeout, headerMap);
        }
    }

    private void trustAll() {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[] {};
                    }

                    @Override
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] chain,
                            String authType)
                            throws java.security.cert.CertificateException {}

                    @Override
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] chain,
                            String authType)
                            throws java.security.cert.CertificateException {}
                }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
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