package com.example.allu.toiletfinder.UI.Fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.allu.toiletfinder.ApplicationActivity;
import com.example.allu.toiletfinder.R;
import com.example.allu.toiletfinder.UI.CreateToiletActivity;
import com.example.allu.toiletfinder.UI.DialogBox.MarkerDetailsDialog;
import com.example.allu.toiletfinder.UI.HomeActivity;
import com.example.allu.toiletfinder.UI.LoginActivity;
import com.example.allu.toiletfinder.interfaces.FragmentSetListener;
import com.example.allu.toiletfinder.interfaces.MarkerDialogInterface;
import com.example.allu.toiletfinder.pojo.Toilet;
import com.example.allu.toiletfinder.utils.DefaultCode;
import com.example.allu.toiletfinder.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;


public class HomeFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener,MarkerDialogInterface {
    String TAG = HomeFragment.class.getSimpleName();
    Context context;
    Utils utils;

    MapView mapView;
    GoogleMap map;

    StateChangeInterface changeInterface;

    FloatingActionButton fab_locate;

    static LatLng knownPos = new LatLng(10.416595, 77.901348);

    FragmentSetListener fragmentSetListener;

    Marker currentPos;
    MarkerDetailsDialog markerDetailsDialog;

    HashMap<Marker,Toilet> integerHashMap;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        fab_locate = (FloatingActionButton) view.findViewById(R.id.fab_locate);
        context = view.getContext();
        utils = new Utils(context);

        mapView = (MapView)view.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);
        return view;
    }

    public void focusMap(Location location){
        currentPos.remove();

        knownPos = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions mOp = new MarkerOptions();
        mOp.position(knownPos);
        currentPos = map.addMarker(mOp);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(knownPos,50);
        map.animateCamera(cameraUpdate);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof HomeActivity){
            changeInterface = (StateChangeInterface)context;
        }else{
            throw new RuntimeExecutionException(new Throwable("Interface not found"));
        }
        if(context instanceof FragmentSetListener){
            fragmentSetListener = (FragmentSetListener)context;
            setTitle();
        }else{
            throw new RuntimeExecutionException(new Throwable("FragmentSetListener interface not implemented"));
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        setTitle();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setTitle();
        mapView.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        changeInterface.onStateChanged(true);
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(knownPos, 50);
        map.animateCamera(cameraUpdate);
        MarkerOptions mOp = new MarkerOptions();
        mOp.position(knownPos);
        currentPos = map.addMarker(mOp);
        loadToilets();
        map.setOnMarkerClickListener(this);

        integerHashMap = new HashMap<>();

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                Log.e(TAG,"Long Pressed");
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Do you want to create a toilet here?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(!utils.isLoggedIn()){
                            dialogInterface.dismiss();
                            utils.Toast("Login to continue");
                            utils.Goto(LoginActivity.class);
                            return;
                        }
                        Intent intent = new Intent(context, CreateToiletActivity.class);
                        intent.putExtra(DefaultCode.Intent_MapLocation,latLng);
                        startActivity(intent);
                        return;
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        ApplicationActivity.databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getValue() != null){
                    Toilet toilet = dataSnapshot.getValue(Toilet.class);
                    Log.e(TAG,"Toilet added "+toilet.getTitle());
                    addPointer(toilet);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    void loadToilets(){
        ArrayList<Toilet> toilets = ApplicationActivity.toiletArrayList;
        Log.e(TAG,"toilers count "+toilets.size());
        for(int i = 0;i<toilets.size();i++){
            Toilet toilet = toilets.get(i);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(toilet.getLatitude(),toilet.getLongitude()));
            Marker marker = map.addMarker(markerOptions);
            Log.e(TAG,"marker "+marker.getId());
            integerHashMap.put(marker,toilet);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(TAG,"marker clicked "+marker.getId());
        if(marker != null && integerHashMap.containsKey(marker)){
            Toilet toilet = (Toilet) integerHashMap.get(marker);
            markerDetailsDialog = new MarkerDetailsDialog();
            markerDetailsDialog.setDate(toilet);
            markerDetailsDialog.show(getFragmentManager(),"");
        }
        return false;
    }

    @Override
    public void onLoaded() {
    }

    public interface StateChangeInterface{
        void onStateChanged(boolean state);
    }

    void addPointer(Toilet toilet){
        if(map != null){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(toilet.getLatitude(),toilet.getLongitude()));
            Marker marker = map.addMarker(markerOptions);
            integerHashMap.put(marker,toilet);
        }
    }

    void setTitle(){
        fragmentSetListener.setTitleOfActivity("Home");
    }
}
