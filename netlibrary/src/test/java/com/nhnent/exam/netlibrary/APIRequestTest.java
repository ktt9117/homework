package com.nhnent.exam.netlibrary;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;

/**
 * Created by gradler on 17/08/2017.
 */
public class APIRequestTest {

    private static final String TEST_ROOT = "https://jsonplaceholder.typicode.com/";
    private static final String SUMMARY_URL = "https://en.wikipedia.org/api/rest_v1/page/summary/";
    private static final String RELATED_URL = "http://en.wikipedia.org/api/rest_v1/page/related/";

    @Test
    public void httpsTest() {

        final CountDownLatch signal = new CountDownLatch(1);

        APIRequest request = new APIRequest.APIRequestBuilder(SUMMARY_URL + "google")
                .method(HttpMethod.GET)
                .create();

        request.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                System.out.println(String.format("errorCode: %d, result: %s", errorCode, result));
                assertEquals(ErrorCode.NO_ERROR, errorCode);
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
        APIRequest request = new APIRequest.APIRequestBuilder(RELATED_URL + "google")
                .method(HttpMethod.GET)
                .create();

        request.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                System.out.println(String.format("errorCode: %d, result: %s", errorCode, result));
                assertEquals(ErrorCode.NO_ERROR, errorCode);
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
    public void postMethodTest() {
        final CountDownLatch signal = new CountDownLatch(1);

        APIRequest APIRequest = new APIRequest.APIRequestBuilder(TEST_ROOT + "posts")
                .method(HttpMethod.POST)
                .body("data: {\n" +
                        "    title: 'foo',\n" +
                        "    body: 'bar',\n" +
                        "    userId: 1\n" +
                        "  }")
                .create();

        APIRequest.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                System.out.println(String.format("errorCode: %d, result: %s", errorCode, result));
                assertEquals(ErrorCode.NO_ERROR, errorCode);
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
    public void putMethodTest() {
        final CountDownLatch signal = new CountDownLatch(1);

        APIRequest APIRequest = new APIRequest.APIRequestBuilder(TEST_ROOT + "posts")
                .method(HttpMethod.PUT)
                .body("data: {\n" +
                        "    id: 4,\n" +
                        "    title: 'foo_fix',\n" +
                        "    body: 'bar_fix',\n" +
                        "    userId: 1\n" +
                        "  }")
                .create();

        APIRequest.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                System.out.println(String.format("errorCode: %d, result: %s", errorCode, result));
                assertEquals(ErrorCode.NO_ERROR, errorCode);
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
    public void deleteMethodTest() {
        final CountDownLatch signal = new CountDownLatch(1);

        APIRequest APIRequest = new APIRequest.APIRequestBuilder(TEST_ROOT + "posts/1")
                .method(HttpMethod.DELETE)
                .create();

        APIRequest.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                System.out.println(String.format("errorCode: %d, result: %s", errorCode, result));
                assertEquals(ErrorCode.NO_ERROR, errorCode);
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