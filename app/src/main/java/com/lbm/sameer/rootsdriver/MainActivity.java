package com.lbm.sameer.rootsdriver;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    //Views
    Button logout;

    //Var
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_preference), MODE_PRIVATE);
        final String restoredPass = prefs.getString("user_password", "");
        final String restoredUserID = prefs.getString("user_id", "");

        logout = (Button)findViewById(R.id.main_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this).setIcon(android.R.drawable.alert_dark_frame).setTitle("Roots Foster")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = getSharedPreferences(
                                        getString(R.string.shared_preference), MODE_PRIVATE).edit();
                                editor.putString("user_id","");
                                editor.putString("user_password","");
                                editor.putInt("login", 0);
                                editor.apply();
                                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                                finish();
                            }
                        }).setNegativeButton("No", null).show();
            }
        });

        if(isServicesOK()){
            init();
        }

        DocumentReference mDocRef = FirebaseFirestore.getInstance().document("drivers/"+restoredUserID);
        mDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (!restoredPass.equals(documentSnapshot.getString("password"))){
                        SharedPreferences.Editor editor = getSharedPreferences(
                                getString(R.string.shared_preference), MODE_PRIVATE).edit();
                        editor.putString("user_id","");
                        editor.putString("user_password","");
                        editor.putInt("login", 0);
                        editor.apply();
                        Toast.makeText(MainActivity.this, "Password Changed! Login Again", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this,LoginActivity.class));
                        finish();
                    }
                }else{
                    SharedPreferences.Editor editor = getSharedPreferences(
                            getString(R.string.shared_preference), MODE_PRIVATE).edit();
                    editor.putString("user_id","");
                    editor.putString("user_password","");
                    editor.putInt("login", 0);
                    editor.apply();
                    Toast.makeText(MainActivity.this, "Username Not Found! Login Again", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e+"", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void init(){
        RelativeLayout btnMap = (RelativeLayout)findViewById(R.id.main_startTrack);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}

