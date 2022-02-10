package com.lifedawn.capstoneapp.account.util;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.main.MyApplication;

import java.util.Arrays;

public class GoogleAccountUtil {
    private static GoogleAccountUtil instance;
    private final String[] CREDENTIAL_SCOPES = {CalendarScopes.CALENDAR};
    private GoogleAccountCredential googleAccountCredential;

    private Context context;
    private static final String TAG = "GoogleAccountUtil";

    public static GoogleAccountUtil getInstance(Context context) {
        if (instance == null) {
            instance = new GoogleAccountUtil(context);
        }
        return instance;
    }

    public GoogleAccountCredential getGoogleAccountCredential() {
        return googleAccountCredential;
    }

    public GoogleAccountUtil(Context context) {
        this.context = context;
    }

    public Account getConnectedGoogleAccount() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String name = sharedPreferences.getString("connectedAccountName", "");
        String type = sharedPreferences.getString("connectedAccountType", "");
        return name.isEmpty() ? null : new Account(name, type);
    }

    public void connectNewGoogleAccount(Account account) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("connectedAccountName", account.name);
        editor.putString("connectedAccountType", account.type);
        editor.commit();
    }

    public void disconnectNewGoogleAccount() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("connectedAccountName", "");
        editor.putString("connectedAccountType", "");
        editor.commit();
    }

    public void signOut(Account signInAccount, OnSignCallback onSignCallback) {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).setAccountName(
                signInAccount.name).build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions);
        googleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void unused) {
                disconnectNewGoogleAccount();
                onSignCallback.onSignOutSuccessful(signInAccount);
            }
        });
    }

    public GoogleSignInAccount lastSignInAccount() {
        return GoogleSignIn.getLastSignedInAccount(context);
    }

    public void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, OnSignCallback onSignCallback) {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context);

        if (googleSignInAccount != null && googleSignInAccount.getId() != null) {

        }
        Intent intent = googleSignInClient.getSignInIntent();

        googleAccountLifeCycleObserver.launchGoogleSignInIntent(intent, new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Account signInAccount = account.getAccount();

                    googleAccountCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(CREDENTIAL_SCOPES)).setBackOff(
                            new ExponentialBackOff());
                    googleAccountCredential.setSelectedAccount(signInAccount);

                    connectNewGoogleAccount(signInAccount);
                    onSignCallback.onSignInSuccessful(signInAccount, googleAccountCredential);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }


    public interface OnSignCallback {
        void onSignInSuccessful(Account signInAccount, GoogleAccountCredential googleAccountCredential);

        void onSignOutSuccessful(Account signOutAccount);
    }


}
