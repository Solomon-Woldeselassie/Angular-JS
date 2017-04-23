package com.example.torton.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherActivity extends AppCompatActivity {

    // Let's store gps values here
    private double latitude = 15.00;
    private double longitude = 47.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
    }

    public void getWeather(View view) {
        // 1. Make HTTP request and parse JSON
        new GetWeatherTask().execute();
    }

    public void getGPS(View view) {
        // Get the current position of the device and update latitude and longitude
       /* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if( lastKnownLocation != null ) {
                latitude = lastKnownLocation.getLatitude();
                longitude = lastKnownLocation.getLongitude();
                ((TextView)findViewById(R.id.gpsTextView)).setText("Lat:"+latitude+" Lng:"+longitude);
            }
            return;
        }*/
        return;

    }

    class GetWeatherTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            // Make HTTP Request
            return doWebRequest();
        }

        protected void onPostExecute( String jsonString ) {
            // Parse JSON and update UI
            parseJsonAndUpdateUI(jsonString);
        }
    }
    private void parseJsonAndUpdateUI( String jsonToParse ){
        // Show the json response on the screen with a Toast notification
        // Toast.makeText(this, jsonToParse, Toast.LENGTH_LONG).show();
        // Let's parse the json and update ui
        JSONObject weatherObservation = null;
        try {
            weatherObservation = new JSONObject(jsonToParse).getJSONObject("weatherObservation");
            // Read required fields from the object
            String temperature = weatherObservation.getString("temperature");
            String windSpeed = weatherObservation.getString("windSpeed");
            // Update the UI
            ((TextView)findViewById(R.id.temperatureTextView)).setText("Temperature " + temperature);
            ((TextView)findViewById(R.id.windTextView)).setText("Wind speed " + windSpeed);
        }
        catch (JSONException e){
            Toast.makeText(this, "Could not update weather data", Toast.LENGTH_SHORT).show();
        }
    }

    private String doWebRequest() {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            String urlString = "http://api.geonames.org/findNearByWeatherJSON?formatted=true&lat="
            + latitude + "&lng=" + longitude + "&username=tonytorp&style=full";
            URL url = new URL(urlString);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                forecastJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                forecastJsonStr = null;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            forecastJsonStr = null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        return forecastJsonStr;
    }

}
