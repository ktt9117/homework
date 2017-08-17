package com.nhnent.exam.wikisearch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.nhnent.exam.netlibrary.APIRequest;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private APIRequest.OnResultListener mListener = new APIRequest.OnResultListener() {
        @Override
        public void onResult(int errorCode, String result) {
            Log.d(TAG, "errorCode: " + errorCode + ", result: " + result);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String requestUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/" + query;
                APIRequest request = new APIRequest.APIRequestBuilder(requestUrl)
                        .create();
                request.send(mListener);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }
}
