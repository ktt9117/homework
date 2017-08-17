package com.nhnent.exam.netlibrary;

import org.junit.Test;

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
        APIRequest APIRequest = new APIRequest.APIRequestBuilder(SUMMARY_URL + "google")
                .method(HttpMethod.GET)
                .create();

        APIRequest.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                System.out.println("errorCode: " + errorCode + ", result: " + result);
                assertEquals(errorCode, ErrorCode.NO_ERROR);
            }
        });
    }

    @Test
    public void httpTest() {
        APIRequest APIRequest = new APIRequest.APIRequestBuilder(RELATED_URL + "google")
                .method(HttpMethod.GET)
                .create();

        APIRequest.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                System.out.println("errorCode: " + errorCode + ", result: " + result);
                assertEquals(errorCode, ErrorCode.NO_ERROR);
            }
        });
    }

    @Test
    public void methodTest() {
        APIRequest APIRequest = new APIRequest.APIRequestBuilder(TEST_ROOT)
                .method(HttpMethod.GET)
                .create();

        APIRequest.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                System.out.println("errorCode: " + errorCode + ", result: " + result);
                assertEquals(errorCode, ErrorCode.NO_ERROR);
            }
        });
    }
}