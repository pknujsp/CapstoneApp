package com.lifedawn.capstoneapp.intro;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.CalendarScopes;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;
import com.lifedawn.capstoneapp.databinding.FragmentIntroBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class IntroFragment extends Fragment {
	private FragmentIntroBinding binding;
	private GoogleAccountCredential googleAccountCredential;
	private final String[] CREDENTIAL_SCOPES = {CalendarScopes.CALENDAR};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		googleAccountCredential = GoogleAccountCredential.usingOAuth2(getContext(), Arrays.asList(CREDENTIAL_SCOPES))
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentIntroBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
				GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
		GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getContext(), googleSignInOptions);
		
		binding.loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = googleSignInClient.getSignInIntent();
				googleSignIntentActivityResultLauncher.launch(intent);
			}
		});
		
		binding.startWithoutGoogleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
			
			}
		});
		
	}
	
	private final ActivityResultLauncher<Intent> googleSignIntentActivityResultLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
					try {
						GoogleSignInAccount account = task.getResult(ApiException.class);
						Account loginAccount = account.getAccount();
						GoogleAccountUtil.connectNewGoogleAccount(getContext(), loginAccount);
					} catch (ApiException e) {
						e.printStackTrace();
					}
				}
			});
}