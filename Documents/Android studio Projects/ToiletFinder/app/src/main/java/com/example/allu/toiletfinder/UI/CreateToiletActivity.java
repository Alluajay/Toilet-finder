package com.example.allu.toiletfinder.UI;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.example.allu.toiletfinder.ApplicationActivity;
import com.example.allu.toiletfinder.R;
import com.example.allu.toiletfinder.pojo.Toilet;
import com.example.allu.toiletfinder.utils.DefaultCode;
import com.example.allu.toiletfinder.utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class CreateToiletActivity extends AppCompatActivity {

    String TAG = CreateToiletActivity.class.toString();
    Utils utils;
    Context context;

    LatLng latLng;

    LinearLayout layout_timingInput;
    TextInputLayout textInputLayout_Amount;
    EditText edit_Title,edit_Address,edit_Description,edit_PhoneNo,edit_Amount,edit_OpenTime,edit_CloseTime;
    Switch switchPaid;
    Button btnCreateToilet;
    RadioGroup radioGroup_ToiletType;
    CheckBox checkBox_AllTime;


    Toilet toilet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_toilet);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        context = this;
        utils = new Utils(this);
        toilet = new Toilet();

        if(!utils.isLoggedIn()){
            utils.Toast("You must login to create a toilet");
            utils.Goto(LoginActivity.class);
            return;
        }

        if(getIntent().hasExtra(DefaultCode.Intent_MapLocation)){
            latLng = getIntent().getParcelableExtra(DefaultCode.Intent_MapLocation);
            toilet.setLatitude(latLng.latitude);
            toilet.setLongitude(latLng.longitude);
        }

        initView();


    }

    void initView(){
        textInputLayout_Amount = (TextInputLayout)findViewById(R.id.txtInputAmount);
        textInputLayout_Amount.setVisibility(View.GONE);

        edit_Title = (EditText)findViewById(R.id.editToiletTitle);
        edit_Address = (EditText)findViewById(R.id.editAddress);
        edit_Description = (EditText)findViewById(R.id.editDescription);
        edit_PhoneNo = (EditText)findViewById(R.id.editPhoneNo);
        edit_Amount = (EditText)findViewById(R.id.editamount);

        edit_OpenTime = (EditText)findViewById(R.id.edit_openTime);
        edit_CloseTime = (EditText)findViewById(R.id.edit_closeTime);

        layout_timingInput = (LinearLayout)findViewById(R.id.layout_TimingEntry);
        layout_timingInput.setVisibility(View.GONE);

        radioGroup_ToiletType = (RadioGroup)findViewById(R.id.radioGroup_type);

        checkBox_AllTime = (CheckBox)findViewById(R.id.check_openall);
        checkBox_AllTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    layout_timingInput.setVisibility(View.GONE);
                }else{
                    layout_timingInput.setVisibility(View.VISIBLE);
                }
            }
        });

        radioGroup_ToiletType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                Log.e(TAG,"radio checked "+i);
            }
        });
        radioGroup_ToiletType.check(R.id.radio_typeIndian);

        edit_Amount.setText("0");

        btnCreateToilet = (Button)findViewById(R.id.btnCreate);
        btnCreateToilet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createToilet();
            }
        });

        switchPaid = (Switch)findViewById(R.id.switchPaid);

        switchPaid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    textInputLayout_Amount.setVisibility(View.VISIBLE);
                }else{
                    textInputLayout_Amount.setVisibility(View.GONE);
                }
            }
        });
        switchPaid.setChecked(false);
        toilet.setPaid(false);
    }

    void createToilet(){
        String Title = edit_Title.getText().toString();
        if(utils.isEmptyString(Title)){
            utils.Toast("Title cannot be empty");
            return;
        }
        String Address = edit_Address.getText().toString();
        if(utils.isEmptyString(Address)){
            utils.Toast("Address cannot be empty");
            return;
        }
        String Desc = edit_Description.getText().toString();
        if(utils.isEmptyString(Desc)){
            utils.Toast("Description cannot be empty");
            return;
        }
        String PhoneNo = edit_PhoneNo.getText().toString();
        if(utils.isEmptyString(PhoneNo)){
            utils.Toast("PhoneNo cannot be empty");
            return;
        }
        double amount = 0;
        if(toilet.isPaid()){
            amount = Double.parseDouble(edit_Amount.getText().toString());
            if(amount == 0){
                utils.Toast("Amount cannot be zero");
                return;
            }
        }

        if(toilet.getLatitude() == 0 && toilet.getLongitude() == 0){
            utils.Toast("Please select the location");
            return;
        }
        toilet.setTitle(Title);
        toilet.setAddress(Address);
        toilet.setDescription(Desc);
        toilet.setPhoneNo(PhoneNo);
        toilet.setUserUid(utils.getUserUid());

        ApplicationActivity.databaseReference.push().setValue(toilet).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                utils.Toast("Toilet successfully added");
                startActivity(getIntent());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                utils.Toast("Error in creating the toilet");
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onToiletTypeButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_typeIndian:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.radio_typeWestern:
                if (checked)

                    break;
            case R.id.radio_typeBoth:
                if (checked)

                    break;

        }
    }

}
