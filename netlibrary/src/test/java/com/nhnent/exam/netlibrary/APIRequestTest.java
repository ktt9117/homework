package com.nhnent.exam.netlibrary;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;

/**
 * Created by gradler on 17/08/2017.
 */
public class APIRequestTest {

    private static final String TEST_ROOT = "http://www.mocky.io/v2/5185415ba171ea3a00704eed";
    private static final String SUMMARY_URL = "https://en.wikipedia.org/api/rest_v1/page/summary/";
    private static final String RELATED_URL = "http://en.wikipedia.org/api/rest_v1/page/related/";

    @Test
    public void httpsTest() {

        final CountDownLatch signal = new CountDownLatch(1);

        APIRequest request = new APIRequest.APIRequestBuilder("https://en.wikipedia.org/api/rest_v1/page/summary/google")
                .method(HttpMethod.GET)
                .create();

        request.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                System.out.println("errorCode: " + errorCode + ", result: " + result);
                assertEquals(errorCode, ErrorCode.NO_ERROR);
                signal.countDown();
            }
        });

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void httpTest() {
        final CountDownLatch signal = new CountDownLatch(1);

        APIRequest request = new APIRequest.APIRequestBuilder("http://en.wikipedia.org/api/rest_v1/page/related/google")
                .method(HttpMethod.GET)
                .create();

        request.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                System.out.println("errorCode: " + errorCode + ", result: " + result);
                assertEquals(errorCode, ErrorCode.NO_ERROR);
                signal.countDown();
            }
        });

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}