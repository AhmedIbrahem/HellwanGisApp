package com.android.gis.huapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SearchMap extends FragmentActivity implements
        OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    ArrayList<String> PlacesNames = new ArrayList<String>();
    AutoCompleteTextView autoCompleteTextView;
    Button Search;
    String SearchData = "";
    String Location;
    static android.location.Location location;
    private UiSettings mUiSettings = null;
    private Toolbar mToolbar;

    private NavigationView mDrawer;
    private DrawerLayout mDrawerLayout;
    private int mSelectedId;
    private boolean mUserSawDrawer = false;
    private static final String SELECTED_ITEM_ID = "selected_item_id";
    private static final String FIRST_TIME = "first_time";
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_map);


        mDrawer = (NavigationView) findViewById(R.id.map_navigate);
        mDrawer.setNavigationItemSelectedListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_main);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                mToolbar,
                R.string.drawer_open,
                R.string.drawer_close);


        BackGroundPlaces backGroundPlaces = new BackGroundPlaces();
        backGroundPlaces.execute();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autocomplute);
        Search = (Button) findViewById(R.id.searchB);

    }


    private void navigate(int mSelectedId) {
        Intent intent = null;


        if (mSelectedId == R.id.map_navigation_item_1) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            intent = new Intent(this, News.class);
            startActivity(intent);
        }
        if (mSelectedId == R.id.map_navigation_item_2) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            intent = new Intent(this, SearchMap.class);
            startActivity(intent);
        }
        if (mSelectedId == R.id.map_navigation_item_3) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            intent = new Intent(this, NavigationMap.class);
            startActivity(intent);
        }
        if (mSelectedId == R.id.map_navigation_item_4) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            intent = new Intent(this, FollowMap.class);
            startActivity(intent);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        menuItem.setChecked(true);
        mSelectedId = menuItem.getItemId();

        navigate(mSelectedId);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_ID, mSelectedId);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        //noinspection SimplifiableIfStatement
        if (id == R.id.map_op_navigation_item_1) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            return true;
        }
        if (id == R.id.map_op_navigation_item_2) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            return true;
        }
        if (id == R.id.map_op_navigation_item_3) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            return true;
        }
        if (id == R.id.map_op_navigation_item_4) {
            intent = new Intent(this, SearchMap.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_optionals, menu);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapUP();

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLng helwanUNI = new LatLng(29.868728, 31.315409);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(helwanUNI));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(helwanUNI, 15));
        // Add a marker in Sydney and move the camera
    }

    public void MapUP() {
        location = null;
        UiSettings settings = mMap.getUiSettings();

        LocationManager service = (LocationManager)
                getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = service.getLastKnownLocation(provider);

        if(location!=null)
        {
            LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
            Log.d("Latitute",""+location.getLatitude());
            Log.d("Latnagggte",""+location.getLongitude());

        }
        else
        {

            new AlertDialog.Builder(SearchMap.this)
                    .setTitle("Google Location Service")
                    .setMessage("Google location Service Not enable Enaple it")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));



                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();



                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
        settings.setZoomControlsEnabled(true);
        settings.setIndoorLevelPickerEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            return;
        }
        mMap.setMyLocationEnabled(true);
        settings.setMyLocationButtonEnabled(true);

    }




    public class BackGroundPlaces extends AsyncTask<String, Void, String> {
        public BackGroundPlaces() {
            super();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviesJsonStr = null;

            try {

                String base_url = "http://helwangis.eu.pn/places.php";
                Log.d("TTTTt", "RRRR");

                URL url = new URL(base_url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {

                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                moviesJsonStr = buffer.toString();


            } catch (IOException e) {

                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                    }
                }
            }

            try {


                String y = datacuter(moviesJsonStr);
                Log.d("hhhh", moviesJsonStr);
                return moviesJsonStr;


            } catch (Exception E) {

                Log.v("exec", E.toString());


            }

            return moviesJsonStr;

        }


        public String datacuter(String x) throws JSONException


        {
            Log.d("YYYYY", x);


            JSONObject jsonObject = new JSONObject(x);
            JSONArray jsonArray = jsonObject.getJSONArray("Places");
            int array_length = jsonArray.length();

            for (int i = 0; i < array_length; i++) {

                PlacesNames.add(jsonArray.getJSONObject(i).get("Place_name").toString());


            }
            Log.d("GGGGGGGGGGG", "" + PlacesNames);


            return null;
        }


        protected void onPostExecute(String path) {
            autoCompleteTextView.setAdapter(new ArrayAdapter<String>(getApplication(), R.layout.list_detiald, PlacesNames));
            Search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    BackGroundShowMap backGroundShowMap=new BackGroundShowMap();
                    backGroundShowMap.execute(autoCompleteTextView.getText().toString());

                    Toast.makeText(getApplication(), autoCompleteTextView.getText().toString(), Toast.LENGTH_LONG).show();
                }
            });


        }


    }
    public class BackGroundShowMap extends AsyncTask<String, Void, String> {
        public BackGroundShowMap() {
            super();
        }

        @Override
        protected String doInBackground(String... params) {
            String ShowMap_url = "http://helwangis.eu.pn/ShowMapPlace.php";

            try {
                Location = params[0];

                URL url = new URL(ShowMap_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String data = URLEncoder.encode("locationName", "UTF-8") + "=" + URLEncoder.encode(Location, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String line = "";
                SearchData="";
                while ((line = bufferedReader.readLine()) != null) {

                    SearchData += line;


                }
                Log.d("GGGGGGg", "" + SearchData);
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return SearchData;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("Result", result);
            if (result.contains("something")) {
                Toast.makeText(getApplication(), "place not inDB", Toast.LENGTH_LONG).show();


            } else

            {

                mMap.clear();
                double lantitue;
                double langtute;
                String[] LatLgTute = result.split("_");
                lantitue = Double.parseDouble(LatLgTute[0]);
                langtute = Double.parseDouble(LatLgTute[1]);
                LatLng latLng = new LatLng(lantitue, langtute);
                mMap.addMarker(new MarkerOptions().position(latLng).title(Location));


            }

        }


    }












}
