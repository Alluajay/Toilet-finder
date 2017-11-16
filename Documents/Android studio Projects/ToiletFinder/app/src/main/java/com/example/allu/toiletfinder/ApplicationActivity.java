package com.example.allu.toiletfinder;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.allu.toiletfinder.pojo.Toilet;
import com.example.allu.toiletfinder.utils.Utils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by allu on 9/11/17.
 */

public class ApplicationActivity extends Application {
    String TAG = ApplicationActivity.class.getSimpleName();
    Utils utils;
    Context context;

    public static FirebaseDatabase firebaseDatabase;
    public static DatabaseReference databaseReference;

    public static ArrayList<Toilet> toiletArrayList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;
        utils = new Utils(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        databaseReference = firebaseDatabase.getReference("ToiletList");

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getValue() != null){
                    Toilet toilet = dataSnapshot.getValue(Toilet.class);
                    toilet.setKey(dataSnapshot.getKey());
                    Log.e(TAG,"Toilet added");
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
}
