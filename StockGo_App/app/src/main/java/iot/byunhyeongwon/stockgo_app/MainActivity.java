package iot.byunhyeongwon.stockgo_app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import vo.Product;
import vo.Product_Store;
import vo.Store;

public class MainActivity extends Activity {

    EditText editText;
    TextView textView;

    Product p = new Product();
    ArrayList<Product_Store> psList = new ArrayList<Product_Store>();
    ArrayList<Store> sList = new ArrayList<Store>();

    private LocationManager locationManager;
    private LocationListener locationListener;

    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        //textView = (TextView) findViewById(R.id.textView);

        ImageView imageView = (ImageView) findViewById(R.id.nfcImg);
        GlideDrawableImageViewTarget glideDrawableImageViewTarget =
                new GlideDrawableImageViewTarget(imageView);
        Glide.with(this).load(R.raw.nfc).into(glideDrawableImageViewTarget);

        settingGPS();

        // 사용자의 현재 위치 //
        Location userLocation = getMyLocation();

        if( userLocation != null ) {
            // TODO 위치를 처음 얻어왔을 때 하고 싶은 것
            latitude = userLocation.getLatitude();
            longitude = userLocation.getLongitude();

            Log.e("latitude", " " + latitude);
            Log.e("longitude", " " + longitude);
        }

        Log.e("latitude", " " + latitude);
        Log.e("longitude", " " + longitude);

    }

    public void reqBtn(View view) {

        String url = editText.getText().toString();

        String query = null;

        try {

            query = URLEncoder.encode("빵", "utf-8");

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
        url = url + query;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {

                            println("OnResponse() call...!!! : " + response);

                            JSONArray jarray = new JSONArray(response);
                            psList.clear();
                            sList.clear();

                            for (int i = 0; i < jarray.length(); i++) {

                                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                                p.setId(jObject.getInt("id"));
                                p.setProduct_name(jObject.getString("product_name"));
                                p.setPrice(jObject.getInt("price"));

                                Product_Store ps = new Product_Store();
                                ps.setProduct_id(jObject.getInt("product_id"));
                                ps.setStore_id(jObject.getInt("store_id"));
                                ps.setStock(jObject.getInt("stock"));
                                psList.add(ps);

                                Store s = new Store();
                                s.setId(jObject.getInt("store_id"));
                                s.setName(jObject.getString("name"));
                                s.setAddr(jObject.getString("addr"));
                                s.setLatitude(jObject.getDouble("latitude"));
                                s.setLogitude(jObject.getDouble("longitude"));

                                sList.add(s);

                                //latitude = jObject.getDouble("latitude");
                            }

                            Log.e("latitude", " " + latitude);
                            Log.e("longitude", " " + longitude);

                            Intent i = new Intent();
                            i.setClass(MainActivity.this, ProductDetailActivity.class);
                            i.putExtra("product", p);
                            i.putExtra("psList", psList);
                            i.putExtra("sList", sList);
                            i.putExtra("cur_longitude", longitude);
                            i.putExtra("cur_latitude", latitude);

                            startActivity(i);

                            //i.putExtra()
//                            i.putExtra("longitude", longitude);
//                            i.putExtra("latitude", latitude);
//
//                            i.setClass(MainActivity.this, MapsActivity.class);
//                            startActivity(i);
                        } catch (Exception e) {

                            e.printStackTrace();
                        }

                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();

                return params;
            }
        };

        request.setShouldCache(false);
        Volley.newRequestQueue(this).add(request);
        println("request to web server" + url);
    }

    public void println(final String data) {

        //textView.append(data + '\n');
    }

    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MInteger.REQUEST_CODE_LOCATION);
        }
        else {

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
                Log.d("Main", "longtitude=" + lng + ", latitude=" + lat);
            }
        }
        return currentLocation;
    }

    private void settingGPS() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // TODO 위도, 경도로 하고 싶은 것
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    public void search(final View view) {

        AlertDialog.Builder alertdialog = new AlertDialog.Builder(MainActivity.this);
        EditText et = new EditText(MainActivity.this);
        //다이얼로그의 내용을 설정합니다.
        alertdialog.setTitle("아이템 검색");
        alertdialog.setMessage("직접 입력하세요.");

        alertdialog.setView(et);

        //확인 버튼
        alertdialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //확인 버튼이 눌렸을 때 토스트를 띄워줍니다.
                reqBtn(view);
            }
        });

        //취소 버튼
        alertdialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //취소 버튼이 눌렸을 때 토스트를 띄워줍니다.
                Toast.makeText(MainActivity.this, "취소", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alert = alertdialog.create();
        alert.show();

        //reqBtn(view);
    }

    public void history(View view) {

    }
}