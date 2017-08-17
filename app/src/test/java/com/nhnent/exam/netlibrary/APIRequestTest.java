package com.nhnent.exam.netlibrary;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by gradler on 17/08/2017.
 */
public class APIRequestTest {

    @Test
    public void httpsTest() {
        APIRequest APIRequest = new APIRequest.APIRequestBuilder("https://en.wikipedia.org/api/rest_v1/page/summary/google")
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
        APIRequest APIRequest = new APIRequest.APIRequestBuilder("http://en.wikipedia.org/api/rest_v1/page/related/google")
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