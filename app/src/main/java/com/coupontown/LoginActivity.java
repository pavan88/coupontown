package com.coupontown;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.coupontown.model.UserProfile;
import com.coupontown.utility.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //Email
    private Button loginbutton;
    private EditText email;
    private EditText password;


    //LOGGER Tags
    private static final String SKIPLOGIN_TAG = "SKIP";
    private static final String G_TAG = "GoogleLogin";
    private static final String E_TAG = "EmailLogin";
    private static final String FIREBASE = "Firebase";


    //Firebase Auth
    FirebaseAuth.AuthStateListener authStateListener;

    private ProgressBar spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);


        setupFirebaseAuth();


        //Email
        loginbutton = findViewById(R.id.buttonLogin);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginbutton.setOnClickListener(this);


        spinner = findViewById(R.id.progressBar);


    }

    @Override
    public void onClick(View view) {

        if (view == loginbutton) {
            Log.d(E_TAG, "Sign Method using Normal Options");
            spinner.setVisibility(View.VISIBLE);
            loginExistingUser();
            hidekeypad();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }


    //----------- END OF OVERRIDE --------------//


    private void loginExistingUser() {
        final String emailStr = email.getText().toString().trim();
        final String passwordStr = password.getText().toString().trim();


        if (TextUtils.isEmpty(emailStr)) {
            email.setError("Enter email address!");
            Toast.makeText(LoginActivity.this, "Enter email address!", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(passwordStr)) {
            password.setError("Enter password!");
            Toast.makeText(LoginActivity.this, "Enter password!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidEmail(emailStr)) {
            Toast.makeText(LoginActivity.this, "Invalid Email", Toast.LENGTH_LONG).show();
            return;

        }
        //1. Check email exists
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(emailStr).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                boolean emailexists;
                emailexists = task.getResult().getSignInMethods().isEmpty();
                if (emailexists) {
                    registeruser(emailStr, passwordStr);
                    spinner.setVisibility(View.GONE);
                } else {
                    //User Exists , So sign in
                    signin(emailStr, passwordStr);
                    spinner.setVisibility(View.GONE);
                }
            }
        });
    }

    private void redirecttoHome() {
        Intent loginIntent = new Intent(this, MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
        finish();
    }

    private final static boolean isValidEmail(String emailID) {

        return android.util.Patterns.EMAIL_ADDRESS.matcher(emailID).matches();

    }

    //User Existing. So login with valid credentails.
    private void signin(final String emailStr, final String passwordStr) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Please verify the email/password", Toast.LENGTH_LONG).show();
                            task.getException();
                        } else {
                            if (task.getResult().getUser().isEmailVerified()) {
                                Toast.makeText(LoginActivity.this, "Login Success via Email & Password", Toast.LENGTH_LONG).show();
                                redirecttoHome();
                            } else {
                                Toast.makeText(LoginActivity.this, "Please verify the Email, before login", Toast.LENGTH_LONG).show();
                                FirebaseUtil.sendVerificationEmail(LoginActivity.this);
                            }
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();
                Log.getStackTraceString(e);

            }
        });
    }

    //Check User exists, if not register
    private void registeruser(final String emailStr, final String passwordStr) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i("register", "onComplete: Successful registration via email and password =>" + task.isSuccessful());
                            Toast.makeText(LoginActivity.this, "User Email Created, Please check email to verify", Toast.LENGTH_LONG).show();
                            FirebaseUtil.sendVerificationEmail(LoginActivity.this);
                            FirebaseUtil.signout();

                        } else {
                            Toast.makeText(LoginActivity.this, "Registration Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }


    private void hidekeypad() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void setupFirebaseAuth() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = FirebaseUtil.firebaseUser();
                if (firebaseUser != null) {
                    if (firebaseUser.isEmailVerified()) {
                        Log.i("1.Firebase", "onAuthStateChanged: User Signed in =>" + firebaseUser.getUid());
                        Log.i("1.Firebase", "onAuthStateChanged: User Signed in Email =>" + firebaseUser.getEmail());
                        redirecttoHome();
                    }
                }
            }
        };
    }

}
