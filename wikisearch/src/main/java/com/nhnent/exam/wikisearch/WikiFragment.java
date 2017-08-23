package com.nhnent.exam.wikisearch;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by gradler on 19/08/2017.
 */
public class WikiFragment extends Fragment implements WikiAdapter.OnItemSelectedListener {

    private static final String TAG = WikiFragment.class.getSimpleName();
    private String mQuery;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<WikiModel> mContentList;
    private ImageLoader mImageLoader;
    private WikiAdapter mAdapter;

    public WikiFragment() {
        // Required empty public constructor
    }

    public static WikiFragment newInstance(String query) {
        WikiFragment fragment = new WikiFragment();
        Bundle args = new Bundle();
        args.putString(Const.INTENT_KEY_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        if (getArguments() != null) {
            mQuery = getArguments().getString(Const.INTENT_KEY_QUERY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_wiki, container, false);
        initSwipeRefreshLayout(view);
        initRecyclerView(view);
        updateActionBar();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onActivtyCreated");
        super.onActivityCreated(savedInstanceState);
        requestSummary(mQuery);
        requestRelated(mQuery);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            menu.clear();
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(WikiModel item) {
        if (item.isHeader()) {
            Intent intent = new Intent(getActivity(), WikiDetailActivity.class);
            intent.putExtra(Const.INTENT_KEY_QUERY, item.getDisplayTitle());
            startActivity(intent);

        } else {
            WikiFragment fragment = WikiFragment.newInstance(item.getDisplayTitle());
            fragment.setImageLoader(mImageLoader);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.left_in, R.anim.left_out)
                    .replace(R.id.main_fragment_container, fragment)
                    .addToBackStack(item.getDisplayTitle())
                    .commit();
        }
    }

    public void setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }

    private void updateActionBar() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            setHasOptionsMenu(true);
            if (getActivity() instanceof MainActivity) {
                ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }

        } else {
            getActivity().invalidateOptionsMenu();
            if (getActivity() instanceof MainActivity) {
                ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }
            }
        }

        getActivity().setTitle(mQuery);
    }

    private void initSwipeRefreshLayout(View view) {
        mSwipeRefreshLayout = view.findViewById(R.id.wiki_swipe_refresh_layout);
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mQuery != null) {
                    int itemCount = mContentList.size();
                    mContentList.clear();
                    mAdapter.notifyItemRangeRemoved(0, itemCount);
                    requestSummary(mQuery);
                    requestRelated(mQuery);
                }
            }
        });
    }

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.wiki_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        mContentList = new ArrayList<>();
        mAdapter = new WikiAdapter(getContext(), mContentList, mImageLoader);
        mAdapter.setOnItemSelectedListener(this);
        recyclerView.setAdapter(mAdapter);
    }

    private void requestSummary(String query) {
        String requestUrl = Const.SUMMARY_URL + query;
        APIRequest request = new APIRequest.APIRequestBuilder(requestUrl)
                .context(getContext())
                .create();
        request.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                Log.d(TAG, "errorCode: " + errorCode + ", result: " + result);
                if (errorCode != ErrorCode.NO_ERROR) {
                    Toast.makeText(getActivity(), "Request fail(" + result + ")", Toast.LENGTH_LONG).show();
                    return;
                }

                WikiModel header = null;
                try {
                    header = convertModel(new JSONObject(result));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Failed to convert result to json", Toast.LENGTH_SHORT).show();
                }

                if (header != null) {
                    header.setHeader(true);
                    mContentList.add(0, header);
                    getActivity().setTitle(header.getDisplayTitle());
                }
            }
        });
    }

    private void requestRelated(String query) {
        String requestUrl = Const.RELATED_URL + query;
        APIRequest request = new APIRequest.APIRequestBuilder(requestUrl)
                .context(getContext())
                .create();
        request.send(new APIRequest.OnResultListener() {
            @Override
            public void onResult(int errorCode, String result) {
                Log.d(TAG, "errorCode: " + errorCode + ", result: " + result);
                if (errorCode != ErrorCode.NO_ERROR) {
                    Toast.makeText(getActivity(), "Request fail(" + result + ")", Toast.LENGTH_LONG).show();
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
                            model.setHeader(false);
                            mContentList.add(model);
                        }
                    }

                    mAdapter.notifyItemRangeInserted(0, mContentList.size());
                    mSwipeRefreshLayout.setRefreshing(false);
                    mSwipeRefreshLayout.setEnabled(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private WikiModel convertModel(JSONObject json) {
        try {
            WikiModel model = new WikiModel();
            String displayTitle = json.getString(WikiModel.Key.DISPLAY_TITLE);
            if (!TextUtils.isEmpty(displayTitle)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    model.setDisplayTitle(Html.fromHtml(displayTitle,
                            Html.FROM_HTML_MODE_COMPACT).toString());
                } else {
                    model.setDisplayTitle(Html.fromHtml(displayTitle).toString());
                }
            }

            model.setExtractText(json.getString(WikiModel.Key.EXTRACT));
            if (json.has(WikiModel.Key.THUMBNAIL)) {
                JSONObject jsonThumbnail = json.getJSONObject(WikiModel.Key.THUMBNAIL);
                model.setThumbnailUrl(jsonThumbnail.getString(WikiModel.Key.SOURCE));
            } else {
                Log.i(TAG, "It has no thumbnailUrl");
            }

            return model;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
