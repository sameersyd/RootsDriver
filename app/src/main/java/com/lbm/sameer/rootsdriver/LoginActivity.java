package com.lbm.sameer.rootsdriver;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    //Views
    EditText username,password;
    RelativeLayout loginBtn;

    private DocumentReference mDocRef;
    Dialog loadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_preference), MODE_PRIVATE);
        int restoredText = prefs.getInt("login", 0);
        if (restoredText == 1) {
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
        }

        username = (EditText)findViewById(R.id.login_usernameEdit);
        password = (EditText)findViewById(R.id.login_passwordEdit);
        loginBtn = (RelativeLayout)findViewById(R.id.login_loginBtn);

        loadDialog = new Dialog(LoginActivity.this);
        loadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadDialog.setContentView(R.layout.loading_one);
        loadDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    loadDialog.dismiss();
                }
                return true;
            }
        });
        LottieAnimationView animSelect;
        animSelect = (LottieAnimationView)loadDialog.findViewById(R.id.loading_one);
        animSelect.setAnimation("blueline.json");
        animSelect.playAnimation();
        animSelect.loop(true);

        Window window = loadDialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDialog.show();
                if (username.getText().toString().equals("")||username.getText().toString().isEmpty()){
                    loadDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Enter Username", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginUser();
            }
        });
    }

    public void loginUser(){
        mDocRef = FirebaseFirestore.getInstance().document("drivers/"+username.getText().toString());
        mDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (password.getText().toString().equals(documentSnapshot.getString("password"))){
                        SharedPreferences.Editor editor = getSharedPreferences(
                                getString(R.string.shared_preference), MODE_PRIVATE).edit();
                        editor.putString("user_id", username.getText().toString());
                        editor.putString("user_password", password.getText().toString());
                        editor.putInt("login", 1);                      //stored 1 when user is logged in
                        editor.apply();
                        loadDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        finish();
                    }else{
                        loadDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    loadDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Username doesn't exists", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadDialog.dismiss();
                Toast.makeText(LoginActivity.this, e+"", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
















