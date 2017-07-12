package com.texi.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.techintegrity.appu.R;
import com.texi.user.gpsLocation.LocationAddress;
import com.texi.user.utils.Common;

import java.io.IOException;
import java.util.List;

public class TraceDriver extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    RelativeLayout back;
    RelativeLayout call;
    TextView ok;

    MarkerOptions marker;
    private LocationManager locationManager;
    private LatLng latLng;
    private LatLng latlngcenter;
    private Marker PickupMarker;
    private LatLng pickupLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_driver);

        back = (RelativeLayout) findViewById(R.id.layout_back_arrow);
        call = (RelativeLayout) findViewById(R.id.call_button);
        ok = (TextView) findViewById(R.id.ok_button);


        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + "+91 123456789"));
                startActivity(callIntent);

            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.navigateUpFromSameTask(TraceDriver.this);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (ActivityCompat.checkSelfPermission(TraceDriver.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(TraceDriver.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    latLng = new LatLng(latitude, longitude);


                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        String str = addressList.get(0).getLocality() + ",";
                        str += addressList.get(0).getCountryName();
                        MarkerAdd(str);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });

        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    latLng = new LatLng(latitude, longitude);

                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        String str = addressList.get(0).getLocality() + ",";
                        str += addressList.get(0).getCountryName();
                        MarkerAdd(str);
                        // CameraPosition cp=CameraPosition.builder().target(new LatLng(28.63320831,77.22294813)).zoom(16).bearing(0).tilt(45).build();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });

        }


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TraceDriver.this, TrackingActivity.class));
                finish();
            }
        });

        MarkerAdd("DropPoint");


    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(TraceDriver.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        CameraPosition position = Common.mgr.getSavedCameraPosition();
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

            mMap.moveCamera(update);


            mMap.setMapType(Common.mgr.getSavedMapType());
            pickupLatLng = new LatLng(getIntent().getDoubleExtra("pickuplat", 0), getIntent().getDoubleExtra("pickuplon", 0));

            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)).position(pickupLatLng).title("Drop Point"));

        }
    }

    public void MarkerAdd(String title) {


        if (checkReady()) {


            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            if (latLng != null&&PickupMarker!=null) {
                PickupMarker.remove();

                marker = new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_location_icon));
                PickupMarker = mMap.addMarker(marker);
                PickupMarker.setDraggable(true);
                builder.include(marker.getPosition());

            }


            // .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_icon))

            if (latLng != null && title != "DropPoint") {
                LatLngBounds bounds = builder.build();

                //CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                Log.d("areBoundsTooSmall", "areBoundsTooSmall = " + areBoundsTooSmall(bounds, 300));
                if (areBoundsTooSmall(bounds, 300)) {
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 10));
                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 20);
                    mMap.animateCamera(cu, new GoogleMap.CancelableCallback() {

                        @Override
                        public void onFinish() {
                            CameraUpdate zout = CameraUpdateFactory.zoomBy((float) -2.5);
                            mMap.animateCamera(zout);
                            if (PickupMarker != null)
                                BounceAnimationMarker(PickupMarker, latLng);

                        }

                        @Override
                        public void onCancel() {

                        }
                    });

                } else {

                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                    mMap.animateCamera(cu, new GoogleMap.CancelableCallback() {

                        @Override
                        public void onFinish() {
                            CameraUpdate zout = CameraUpdateFactory.zoomBy((float) -1.0);
                            mMap.animateCamera(zout);
                            BounceAnimationMarker(PickupMarker, latLng);

                        }

                        @Override
                        public void onCancel() {
//                            CameraUpdate zout = CameraUpdateFactory.zoomBy((float) -1.0);
//                            mMap.animateCamera(zout);
                        }
                    });

                }
            }


//            CameraUpdate zoom=CameraUpdateFactory.zoomTo(5);
//            mMap.animateCamera(zoom);
            //mMap.moveCamera(cu);


        }
    }

    public void BounceAnimationMarker(final Marker animationMarker, final LatLng animationLatLng) {
        if (animationLatLng != null) {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            Projection proj = mMap.getProjection();
            Point startPoint = proj.toScreenLocation(animationLatLng);
            startPoint.offset(0, -100);
            final LatLng startLatLng = proj.fromScreenLocation(startPoint);
            final long duration = 1500;
            final Interpolator interpolator = new BounceInterpolator();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);
                    double lng = t * animationLatLng.longitude + (1 - t) * startLatLng.longitude;
                    double lat = t * animationLatLng.latitude + (1 - t) * startLatLng.latitude;
                    animationMarker.setPosition(new LatLng(lat, lng));
                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }
                }
            });
        }
    }

    private boolean areBoundsTooSmall(LatLngBounds bounds, int minDistanceInMeter) {
        float[] result = new float[1];
        Location.distanceBetween(bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude, result);
        return result[0] < minDistanceInMeter;
    }

    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, "Google Map not ready", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
