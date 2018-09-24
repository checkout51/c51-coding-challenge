package com.c51.sedwards.c51challenge;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.c51.sedwards.c51challenge.adapter.OfferAdapter;
import com.c51.sedwards.c51challenge.model.OfferList;
import com.c51.sedwards.c51challenge.viewmodel.OfferViewModel;

import java.lang.ref.WeakReference;

import static com.c51.sedwards.c51challenge.OfferListActivity.UiUpdateHandler.REFRESH_COMPLETE_MSG;

public class OfferListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final long REFRESH_DELAY_MS = 2000;

    //Views
    private RecyclerView mOfferRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout mSortByCashButton;
    private ImageView mSortByCashIcon;
    private LinearLayout mSortByNameButton;
    private ImageView mSortByNameIcon;

    //Model
    private OfferViewModel mOfferModel;

    //Listeners
    private OfferAdapter mOfferAdapter;
    private Observer<OfferList> mOfferObserver;

    //UI Handler
    private Handler mUiHandler;

    //Preferences Handling
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (s.equals(getString(R.string.pref_sort_type))) {
                final String sort = sharedPreferences.getString(getString(R.string.pref_sort_type), SortType.NONE.name());
                final SortType type = SortType.valueOf(sort);
                updateSortUi(type);
                if (type == SortType.NONE) {
                    if (null != mOfferModel) {
                        //Get the 'server' order
                        mOfferModel.getOffers();
                    }
                } else {
                    mOfferAdapter.sort(type);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_list);

        //UI Thread handler
        mUiHandler = new UiUpdateHandler(this);

        //Get Shared Preferences
        mSharedPreferences = getSharedPreferences(getString(R.string.c51_shared_prefs), Context.MODE_PRIVATE);

        initializeApplicationViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePreferences();
    }

    @Override
    protected void onStop() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mPreferenceChangedListener);
        super.onStop();
    }

    private void initializePreferences() {
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mPreferenceChangedListener);
        if (!mSharedPreferences.contains(getString(R.string.pref_sort_type))) {
            mSharedPreferences.edit()
                    .putString(getString(R.string.pref_sort_type), SortType.NONE.name())
                    .apply();
        }
        if (!mSharedPreferences.contains(getString(R.string.pref_host_url))) {
            mSharedPreferences.edit()
                    .putString(getString(R.string.pref_host_url), OfferViewModel.JSON_URL)
                    .apply();
        }
        final String sort = mSharedPreferences.getString(getString(R.string.pref_sort_type), SortType.NONE.name());
        final SortType type = SortType.valueOf(sort);
        updateSortUi(type);
    }

    private void updateSortUi(SortType type) {
        switch (type) {
            case CASH_BACK:
                mSortByCashButton.setBackground(getResources().getDrawable(R.drawable.selected_background));
                mSortByCashIcon.setImageResource(R.drawable.icons8_checkmark_24);
                mSortByNameButton.setBackground(getResources().getDrawable(R.drawable.button_background));
                mSortByNameIcon.setImageResource(R.drawable.icons8_ascending_sorting_24);
                break;
            case NAME:
                mSortByNameButton.setBackground(getResources().getDrawable(R.drawable.selected_background));
                mSortByNameIcon.setImageResource(R.drawable.icons8_checkmark_24);
                mSortByCashButton.setBackground(getResources().getDrawable(R.drawable.button_background));
                mSortByCashIcon.setImageResource(R.drawable.icons8_ascending_sorting_24);
                break;
            case NONE:
            default:
                mSortByNameButton.setBackground(getResources().getDrawable(R.drawable.button_background));
                mSortByNameIcon.setImageResource(R.drawable.icons8_descending_sorting_24);
                mSortByCashButton.setBackground(getResources().getDrawable(R.drawable.button_background));
                mSortByCashIcon.setImageResource(R.drawable.icons8_ascending_sorting_24);
        }
    }

    private void initializeApplicationViews() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "All your offers are now redeemed!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initializeContentViews();
    }

    private void initializeContentViews() {
        mSwipeRefreshLayout = findViewById(R.id.offer_swipe_refresh_root);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (null != mOfferModel) {
                    //observer is already initialized
                    mOfferModel.getOffers();
                }
                mUiHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE_MSG, REFRESH_DELAY_MS);
            }
        });

        mSortByCashButton = findViewById(R.id.sort_cash_back);
        mSortByCashIcon = mSortByCashButton.findViewById(R.id.sort_cash_icon);
        mSortByCashButton.setOnClickListener(new SortButtonClickListener(SortType.CASH_BACK));

        mSortByNameButton = findViewById(R.id.sort_name);
        mSortByNameIcon = mSortByNameButton.findViewById(R.id.sort_name_icon);
        mSortByNameButton.setOnClickListener(new SortButtonClickListener(SortType.NAME));

        mOfferRecyclerView = findViewById(R.id.offer_list_view);
        //initial offers are empty, we will observe and post to it
        mOfferAdapter = new OfferAdapter(null);
        mOfferRecyclerView.setAdapter(mOfferAdapter);
        mOfferRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mOfferModel = ViewModelProviders.of(this).get(OfferViewModel.class);
        mOfferModel.setApplication(getApplication());
        mOfferObserver = new OfferObserver();
        mOfferModel.getOffers().observe(this, mOfferObserver);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_setting) {
            final Intent startSettings = new Intent(this, SettingsActivity.class);
            startActivity(startSettings);
        }
        // Handle navigation view item clicks here.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public SwipeRefreshLayout getRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    class OfferObserver implements Observer<OfferList> {

        /**
         * Called when the data is changed.
         *
         * @param offerList The new data
         */
        @Override
        public void onChanged(@Nullable OfferList offerList) {
            final String sort = mSharedPreferences.getString(getString(R.string.pref_sort_type), SortType.NONE.name());
            final SortType type = SortType.valueOf(sort);
            if (null != offerList) {
                mOfferAdapter.sort(type, offerList.getOffers());
            } else {
                mOfferAdapter.sort(type, null);
            }
        }
    }

    static class UiUpdateHandler extends Handler {
        private WeakReference<OfferListActivity> mController;

        UiUpdateHandler(OfferListActivity uiController) {
            mController = new WeakReference<>(uiController);
        }

        static final int REFRESH_COMPLETE_MSG = 1;

        @Override
        public void handleMessage(Message msg) {
            if (null != msg) {
                switch (msg.what) {
                    case REFRESH_COMPLETE_MSG:
                        final OfferListActivity controller = mController.get();
                        if (null != controller) {
                            final SwipeRefreshLayout refresher = controller.getRefreshLayout();
                            if (null != refresher) {
                                refresher.setRefreshing(false);
                            }
                        }
                }
            }
        }
    }

    public enum SortType {
        CASH_BACK,
        NAME,
        NONE
    }

    class SortButtonClickListener implements View.OnClickListener {

        private final SortType mSortType;

        public SortButtonClickListener(SortType sortType) {
            mSortType = sortType;
        }

        @Override
        public void onClick(View view) {
            changeSortType(mSortType);
        }
    }

    private void changeSortType(SortType sortType) {
        final String currentName = mSharedPreferences.getString(getString(R.string.pref_sort_type), SortType.NONE.name());
        final SortType currentType = SortType.valueOf(currentName);
        if (sortType != currentType) {
            mSharedPreferences.edit()
                    .putString(getString(R.string.pref_sort_type), sortType.name())
                    .apply();
        } else {
            //if this is already set then toggle off
            mSharedPreferences.edit()
                    .putString(getString(R.string.pref_sort_type), SortType.NONE.name())
                    .apply();
        }
    }
}
