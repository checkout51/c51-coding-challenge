package com.c51.sedwards.c51challenge.viewmodel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.c51.sedwards.c51challenge.R;
import com.c51.sedwards.c51challenge.model.OfferList;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OfferViewModel extends ViewModel {
    public static final String DEFAULT_FILE_NAME = "c51.json";
    public static final String JSON_URL = "https://api.myjson.com/bins/wavu4";
    private MutableLiveData<OfferList> mOfferList;
    private Application mApplication;

    /**
     * Application context survives alongside lifecycle of the application.
     * So it may be passed to this ViewModel without concern
     * @param application
     */
    public void setApplication(Application application) {
        mApplication = application;
    }

    /**
     * Pass back the LiveData object which can be observed by the UI
     * And then asynchronously fetch the offers.
     * @return offer list as live data
     */
    public LiveData<OfferList> getOffers() {
        if (null == mOfferList) {
            mOfferList = new MutableLiveData<>();
        }
        fetchOffers();
        return mOfferList;
    }

    private void fetchOffers() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mApplication.getApplicationContext());
        final String url = sharedPref.getString(mApplication.getResources().getString(R.string.pref_host_url), mApplication.getResources().getString(R.string.pref_default_host_url));
        //live data updates will be posted to observables here
        new FetchOfferTask(mApplication, mOfferList).execute(url);
    }



    /**
     * Static class so we don't leak the app context (which we'll hold weakly)
     * This will asynchronously fetch the offer list in order of priority
     * 1. From OkHttpCache
     * 2. From my json host (change in settings)
     * 3. If no network, then from file
     */
    static class FetchOfferTask extends AsyncTask<String, Void, Void> {
        private WeakReference<Application> mApp;
        private OkHttpClient mHttpClient;
        private WeakReference<MutableLiveData<OfferList>> mOfferListLiveData;

        public FetchOfferTask(@NonNull Application app,@NonNull MutableLiveData<OfferList> offerListLiveData) {
            mApp = new WeakReference<>(app);
            mOfferListLiveData = new WeakReference<>(offerListLiveData);
            if (null != app.getCacheDir()) {
                //4kb cache should be enough
                mHttpClient = new OkHttpClient.Builder()
                        .cache(new Cache(app.getCacheDir(), 4*1024)).build();
            } else {
                //no cache! (helps for testing)
                mHttpClient = new OkHttpClient.Builder().build();
            }

        }
        @Override
        protected Void doInBackground(String... requests) {
            if (null == requests || requests.length == 0) {
                return null;
            }
            final Request dataRequest = new Request.Builder()
                    .url(requests[0])
                    .build();
            InputStream dataStream = null;
            try {
                final Response response = mHttpClient.newCall(dataRequest).execute();
                if (response.isSuccessful()) {
                    final ResponseBody body = response.body();
                    if (null != body) {
                        dataStream = body.byteStream();
                    }
                }
            } catch (IOException e) {
                Logger.getGlobal().warning("Network problem: " + e.toString());
            }
            if (null == dataStream) {
                dataStream = fetchFromFile();
            }
            final MutableLiveData<OfferList> liveData = mOfferListLiveData.get();
            if (null != dataStream) {
                liveData.postValue(new Gson().fromJson(new InputStreamReader(dataStream), OfferList.class));
            }
            return null;
        }

        private InputStream fetchFromFile() {
            final Application app = mApp.get();
            if (null != app) {
                try {
                    return app.getAssets().open(DEFAULT_FILE_NAME);
                } catch (IOException e) {
                    Logger.getGlobal().warning("Network problem: " + e.toString());
                }
            }
            return null;
        }
    }
}
