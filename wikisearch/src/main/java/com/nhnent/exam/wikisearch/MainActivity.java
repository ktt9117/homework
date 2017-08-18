package com.nhnent.exam.wikisearch;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nhnent.exam.netlibrary.APIRequest;
import com.nhnent.exam.netlibrary.ErrorCode;
import com.nhnent.exam.netlibrary.ImageLoader;
import com.nhnent.exam.wikisearch.adapters.WikiAdapter;
import com.nhnent.exam.wikisearch.models.WikiModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WikiAdapter.OnItemSelectedListener, WikiFragment.OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private List<WikiModel> mContentList;
    private ImageLoader mImageLoader;
    private WikiAdapter mAdapter;
    private String mLastQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageLoader = new ImageLoader();

        initSwipeRefreshLayout();
        initRecyclerView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImageLoader != null) {
            mImageLoader.clear();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        final MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mLastQuery = query;
                mContentList.clear();
                requestSummary(query);
                requestRelated(query);
                search.collapseActionView();
                mSwipeRefreshLayout.setEnabled(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.main_swipe_refresh_layout);
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mLastQuery != null) {
                    int itemCount = mContentList.size();
                    mContentList.clear();
                    mAdapter.notifyItemRangeRemoved(0, itemCount);
                    requestSummary(mLastQuery);
                    requestRelated(mLastQuery);
                }
            }
        });
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mContentList = new ArrayList<>();
        mAdapter = new WikiAdapter(this, mContentList, mImageLoader);
        mAdapter.setOnItemSelectedListener(this);
        recyclerView.setAdapter(mAdapter);
    }

    private void requestSummary(String query) {
        String requestUrl = Const.SUMMARY_URL + query;
        APIRequest request = new APIRequest.APIRequestBuilder(requestUrl)
                .context(this)
                .create();
        request.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                Log.d(TAG, "errorCode: " + errorCode + ", result: " + result);
                if (errorCode != ErrorCode.NO_ERROR) {
                    Toast.makeText(getApplicationContext(), "Request fail(" + result + ")", Toast.LENGTH_LONG).show();
                    return;
                }

                WikiModel header = null;
                try {
                    header = convertModel(new JSONObject(result));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Failed to convert result to json", Toast.LENGTH_SHORT).show();
                }

                if (header != null) {
                    header.setHeader(true);
                    mContentList.add(0, header);
                }
            }
        });
    }

    private void requestRelated(String query) {
        String requestUrl = Const.RELATED_URL + query;
        APIRequest request = new APIRequest.APIRequestBuilder(requestUrl)
                .context(this)
                .create();
        request.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                Log.d(TAG, "errorCode: " + errorCode + ", result: " + result);
                if (errorCode != ErrorCode.NO_ERROR) {
                    Toast.makeText(getApplicationContext(), "Request fail(" + result + ")", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    JSONObject json = new JSONObject(result);
                    JSONArray pages = json.getJSONArray("pages");
                    int loopCnt = pages.length();
                    Log.d(TAG, "pages count : " + loopCnt);
                    for (int i = 0; i < loopCnt; i++) {
                        WikiModel model = convertModel(pages.getJSONObject(i));
                        if (model != null) {
                            mContentList.add(model);
                        }
                    }

                    mAdapter.notifyItemRangeInserted(0, mContentList.size());
                    mSwipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private WikiModel convertModel(JSONObject json) {
        try {
            WikiModel model = new WikiModel();
            model.setDisplayTitle(json.getString("displaytitle"));
            Log.i(TAG, "[convertModel] displayTitle: " + model.getDisplayTitle());
            model.setExtractText(json.getString("extract"));
            Log.i(TAG, "[convertModel] extract: " + model.getExtractText());
            if (json.has("thumbnail")) {
                JSONObject jsonThumbnail = json.getJSONObject("thumbnail");
                model.setThumbnailUrl((jsonThumbnail.getString("source")));
                Log.i(TAG, "[convertModel] thumbnailUrl: " + model.getThumbnailUrl());
            } else {
                Log.i(TAG, "[convertModel] it has no thumbnailUrl");
            }

            return model;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onItemSelect(WikiModel item) {
        if (item.isHeader()) {
            // TODO: Create Detail WebView
        } else {
            // TODO: Create new WikiFragment. You need to move RecyclerView that you have implemented MainActivity to the WikiFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_fragment_container, WikiFragment.newInstance(item.getDisplayTitle()))
                    .addToBackStack("detail").commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}