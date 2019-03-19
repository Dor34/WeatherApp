package com.example.android.weatherapp;

import android.net.Uri;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.weatherapp.data.SunshinePrefernces;
import com.example.android.weatherapp.utilities.NetworkUtils;
import com.example.android.weatherapp.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapter.ForecastAdapterOnClickHandler, LoaderManager.LoaderCallbacks<String[]> {

    private static final String TAG = MainActivity.class.getSimpleName ();

    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private static final int FORECAST_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_forcast);

        mRecyclerView = (RecyclerView)findViewById (R.id.recycler_forecast);
        mErrorMessageDisplay = (TextView)findViewById (R.id.tv_error_message_display);

        LinearLayoutManager layoutManager = new LinearLayoutManager
                (this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager (layoutManager);
        mRecyclerView.setHasFixedSize (true);
        mForecastAdapter = new ForecastAdapter (this);
        mRecyclerView.setAdapter (mForecastAdapter);

        mLoadingIndicator = (ProgressBar)findViewById (R.id.pb_loading_indicator);

        int loaderId = FORECAST_LOADER_ID;
        LoaderManager.LoaderCallbacks<String[]> callback = MainActivity.this;
        Bundle bundleForLoader = null;

        android.support.v4.app.LoaderManager.getInstance(this).initLoader
                (loaderId, bundleForLoader, callback).forceLoad ();
    }
//TODO fix this 
    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int id, final Bundle loaderArgs){
        return new AsyncTaskLoader<String[]> (this) {
            String[] mWeatherData = null;

            @Override
            protected void onStartLoading(){
                if (mWeatherData != null){
                    deliverResult (mWeatherData);
                }else{
                    mLoadingIndicator.setVisibility (View.VISIBLE);
                    forceLoad ();
                }
            }

            @Nullable
            @Override
            public String[] loadInBackground() {

                String locationQuery = SunshinePreferences
                        .getPreferredWeatherLocation(MainActivity.this);
                URL weatherRequestUrl = NetworkUtils.buildUrl (locationQuery);

                try{
                    String jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl (weatherRequestUrl);

                    String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                            .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);
                    return simpleJsonWeatherData;
                }catch (Exception e){
                    e.printStackTrace ();
                    return null;
                }
            }

            public void deliverResult(String[] data){
                mWeatherData = data;
                super.deliverResult (data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data){
        mLoadingIndicator.setVisibility (View.INVISIBLE);
        if (null == data){
            showErrorMessage ();
        }else{
            showWeatherDataView ();
            mForecastAdapter.setWeatherData (data);
        }
    }

   @Override
   public void onLoaderReset(Loader<String[]> loader){

   }

   private void invalidateData(){
        mForecastAdapter.setWeatherData (null);
   }

    private void openLocationInMap(){
        String addressString = "1600 Amphitheater Parkway, CA";
        Uri geoLocation = Uri.parse ("geo:0,0?q=" + addressString);

        Intent intent = new Intent (Intent.ACTION_VIEW);
        intent.setData (geoLocation);

        if (intent.resolveActivity (getPackageManager ()) != null){
            startActivity (intent);
        }else{
            Log.d (TAG, "Couldn't call " + geoLocation.toString () + ", no receiving apps installed");
        }
    }

    @Override
    public void onClick(String weatherForDay) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent (context, destinationClass);
        intentToStartDetailActivity.putExtra (Intent.EXTRA_TEXT, weatherForDay);
        startActivity(intentToStartDetailActivity);
    }

    private void showWeatherDataView(){
        mErrorMessageDisplay.setVisibility (View.INVISIBLE);
        mRecyclerView.setVisibility (View.VISIBLE);
    }

    private void showErrorMessage(){
        mRecyclerView.setVisibility (View.INVISIBLE);
        mErrorMessageDisplay.setVisibility (View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater ();
        inflater.inflate (R.menu.forecast, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    int id = item.getItemId ();
    if (id == R.id.action_refresh){
        invalidateData ();
        android.support.v4.app.LoaderManager.getInstance(this).initLoader
                (FORECAST_LOADER_ID, null, this).forceLoad ();
        return true;
    }

    if (id == R.id.action_map){
        openLocationInMap ();
        return true;
    }
    return super.onOptionsItemSelected (item);
    }
}





