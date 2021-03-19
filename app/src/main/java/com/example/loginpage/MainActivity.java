package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private SignInButton signInButton;
     private GoogleSignInClient mGoogleSignInClient;
     private String TAG = "MainActivity";
     private FirebaseAuth mAuth;
     private Button btnSignOut;
     private int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());

        textViewUser = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email","public_profile");
        mCallbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG1, "onSuccess" + loginResult);
                handleFacebookToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.d(TAG1, "onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG1, "onError" + error);

            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    updateUI1(user);
                }
                else{
                    updateUI1(null);
                }
            }
        };

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null){
                    mFirebaseAuth.signOut();
                }
            }
        };





        signInButton = findViewById(R.id.sign_in_button);
        mAuth = FirebaseAuth.getInstance();
        btnSignOut = findViewById(R.id.sign_out_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignInClient.signOut();
                Toast.makeText(MainActivity.this,"You are Logged out",Toast.LENGTH_SHORT).show();
                btnSignOut.setVisibility(View.INVISIBLE);
            }
        });
    }
    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
     if (requestCode == RC_SIGN_IN) {
         Task<GoogleSignInAccount> task =GoogleSignIn.getSignedInAccountFromIntent(data);
         handleSignInResult(task);
    }
}
   private void handleSignInResult( Task<GoogleSignInAccount> completedTask) {
    try {

        GoogleSignInAccount acc = completedTask.getResult(ApiException.class);
        Toast.makeText(MainActivity.this,"Signed In Successfully",Toast.LENGTH_SHORT).show();
        FirebaseGoogleAuth(acc);
    }
    catch (ApiException e) {
        Toast.makeText(MainActivity.this,"SignIn Failed",Toast.LENGTH_SHORT).show();
        FirebaseGoogleAuth(null);
    }

}

   private void FirebaseGoogleAuth(GoogleSignInAccount acct) {
       AuthCredential authCredential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
       mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
           @Override
           public void onComplete(@NonNull Task<AuthResult> task) {
               if (task.isSuccessful()) {
                   Toast.makeText(MainActivity.this, "Successfull", Toast.LENGTH_SHORT).show();
                   FirebaseUser user = mAuth.getCurrentUser();
                   updateUI(user);
               } else {
                   Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                   updateUI(null);
               }
           }
       });
   }

     private void updateUI( FirebaseUser fUser) {
        btnSignOut.setVisibility(View.VISIBLE);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null){
            String personName = account.getDisplayName();
            String personGivenName = account.getGivenName();
            String personFamilyName = account.getFamilyName();
            String personEmail = account.getEmail();
            String personId = account.getId();
            Uri personPhoto = account.getPhotoUrl();

            Toast.makeText(MainActivity.this, personName + personEmail,Toast.LENGTH_SHORT).show();

        }

     }

    private CallbackManager mCallbackManager;
    private FirebaseAuth mFirebaseAuth;
    private TextView textViewUser;
    private ImageView imageView;
    private LoginButton loginButton;
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;
    private static final String TAG1 = "FacebookAuthentication";

    private void handleFacebookToken(AccessToken token){
        Log.d(TAG1, "handleFacebookToken" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG1,"sign in with credential: successful");
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    updateUI1(user);
                }
                else {
                    Log.d(TAG1,"sign in with credential: failure", task.getException());
                    Toast.makeText(MainActivity.this, task.getException().toString(),Toast.LENGTH_SHORT).show();
                    updateUI1(null);
                }
            }
        });
    }
    private void updateUI1(FirebaseUser user) {
        if (user != null) {
            textViewUser.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
//                String photoUrl = user.getPhotoUrl().toString();
//                photoUrl = photoUrl + "?type=large";
//                Picasso.get().load(photoUrl).into(imageView);
                Bundle params = new Bundle();
                params.putString("fields", "id,picture.type(large)");
                new GraphRequest(AccessToken.getCurrentAccessToken(), "me", params, HttpMethod.GET,
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                if (response != null) {
                                    try {
                                        JSONObject data = response.getJSONObject();
                                        if (data.has("picture")) {
                                           String profilePicUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");

                                            Glide.with(MainActivity.this)
                                                    .load(profilePicUrl)
                                                    .fitCenter()
                                                    .into(imageView);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).executeAsync();
            }
        }
        else {
            textViewUser.setText("");
            imageView.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            mFirebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}