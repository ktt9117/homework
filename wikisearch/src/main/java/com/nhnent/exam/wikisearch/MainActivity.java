package com.nhnent.exam.wikisearch;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.nhnent.exam.netlibrary.ImageLoader;

public class MainActivity extends AppCompatActivity implements WikiFragment.OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageLoader = new ImageLoader();
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
                WikiFragment fragment = WikiFragment.newInstance(query);
                fragment.setImageLoader(mImageLoader);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment_container, fragment)
                        .commit();

                search.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.requestFocus();
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}