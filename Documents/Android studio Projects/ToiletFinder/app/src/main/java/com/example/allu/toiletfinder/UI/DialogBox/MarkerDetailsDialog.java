package com.example.allu.toiletfinder.UI.DialogBox;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.allu.toiletfinder.R;
import com.example.allu.toiletfinder.interfaces.MarkerDialogInterface;
import com.example.allu.toiletfinder.pojo.Toilet;
import com.example.allu.toiletfinder.utils.Utils;
import com.google.android.gms.tasks.RuntimeExecutionException;

import java.util.Locale;

/**
 * Created by allu on 9/12/17.
 */

public class MarkerDetailsDialog extends DialogFragment {
    String TAG = MarkerDetailsDialog.class.getSimpleName();
    Context context;
    Utils utils;
    MarkerDialogInterface dialogInterface;

    Toilet toilet;

    TextView Txt_Cost,Txt_Address,Txt_Description,Txt_PhoneNo;
    Button Btn_Navigate;

    AlertDialog alertDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof MarkerDialogInterface){
            dialogInterface = (MarkerDialogInterface) context;
        }else{
            throw new RuntimeExecutionException(new Throwable("MarkerDialogInterface Interface not implemented"));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        context = getActivity();
        utils = new Utils(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        dialogInterface.onLoaded();

        View view = (View) LayoutInflater.from(context).inflate(R.layout.single_marker_layout,null,false);
        Txt_Cost = (TextView)view.findViewById(R.id.txt_cost);
        Txt_Address = (TextView)view.findViewById(R.id.txt_Address);
        Txt_Description = (TextView)view.findViewById(R.id.txt_Description);
        Txt_PhoneNo = (TextView)view.findViewById(R.id.txt_phoneNo);
        Btn_Navigate = (Button)view.findViewById(R.id.btn_navigate);
        builder.setView(view);
        builder.setTitle(toilet.getTitle());
        alertDialog = builder.create();
        loadUI();
        return alertDialog;
    }

    public void setAlertDialog(AlertDialog alertDialog){
        this.alertDialog = alertDialog;

    }

    void loadUI(){
        String cost = "";
        if(toilet.isPaid()){
            cost = toilet.getAmount()+" Rs per usage";
        }else{
            cost = "Free";
        }
        Txt_Cost.setText(cost);
        Txt_Address.setText(toilet.getAddress());
        Txt_Description.setText(toilet.getDescription());
        Txt_PhoneNo.setText(toilet.getPhoneNo());

        Btn_Navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG,"Clicked");
                String strUri = "http://maps.google.com/maps?q=loc:" + toilet.getLatitude() + "," + toilet.getLongitude() + " (" +toilet.getTitle() + ")";
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));

                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

                startActivity(intent);

            }
        });
    }



    public void setDate(Toilet toilet){
        this.toilet = toilet;


    }
}
