package com.lifedawn.capstoneapp.account;

import android.accounts.Account;
import android.app.Dialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;
import com.lifedawn.capstoneapp.calendar.util.GoogleCalendarUtil;
import com.lifedawn.capstoneapp.common.interfaces.OnHttpApiCallback;
import com.lifedawn.capstoneapp.common.viewmodel.AccountCalendarViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentProfileBinding;

import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends DialogFragment {
    private FragmentProfileBinding binding;
    private AccountCalendarViewModel accountCalendarViewModel;
    private GoogleAccountUtil googleAccountUtil;
    private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountCalendarViewModel = new ViewModelProvider(requireActivity()).get(AccountCalendarViewModel.class);
        googleAccountUtil = GoogleAccountUtil.getInstance(getContext());
        googleAccountLifeCycleObserver = new GoogleAccountLifeCycleObserver(requireActivity().getActivityResultRegistry(),
                requireActivity());
        getLifecycle().addObserver(googleAccountLifeCycleObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //로그아웃(사인아웃)버튼의 기능 설정
        binding.signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account = googleAccountUtil.getConnectedGoogleAccount();
                accountCalendarViewModel.signOut(account, new GoogleAccountUtil.OnSignCallback() {
                    @Override
                    public void onSignInSuccessful(Account signInAccount, GoogleAccountCredential googleAccountCredential) {


                    }

                    @Override
                    public void onSignOutSuccessful(Account signOutAccount) {
                    }
                });
            }
        });

        //로그인(사인인)버튼의 기능 설정
        binding.signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountCalendarViewModel.signIn(googleAccountLifeCycleObserver, new GoogleAccountUtil.OnSignCallback() {
                    @Override
                    public void onSignInSuccessful(Account signInAccount, GoogleAccountCredential googleAccountCredential) {

                    }

                    @Override
                    public void onSignOutSuccessful(Account signOutAccount) {

                    }
                });
            }
        });

        //
        accountCalendarViewModel.getSignInLiveData().observe(getViewLifecycleOwner(), new Observer<Account>() {
            @Override
            public void onChanged(Account account) {
                if (account == null) {
                } else {
                    Toast.makeText(getContext(), R.string.signin_successful, Toast.LENGTH_SHORT).show();
                    onSignIn(account);
                }
            }
        });

        accountCalendarViewModel.getSignOutLiveData().observe(getViewLifecycleOwner(), new Observer<Account>() {
            @Override
            public void onChanged(Account account) {
                if (account == null) {

                } else {
                    Toast.makeText(getContext(), R.string.signout_successful, Toast.LENGTH_SHORT).show();
                    onSignOut();
                }
            }
        });


        final GoogleSignInAccount lastSignInAccount = accountCalendarViewModel.lastSignInAccount();
        if (lastSignInAccount == null) {
            //계정이 없거나 로그아웃된 상태
            onSignOut();
        } else {
            //로그인을 이전에 하였던 경우
            GoogleAccountUtil googleAccountUtil = GoogleAccountUtil.getInstance(getContext());
            final Account connectedAccount = googleAccountUtil.getConnectedGoogleAccount();

            if (connectedAccount.name.equals(lastSignInAccount.getAccount().name)) {
                //같은 계정 -> 로그인 성공
                onSignIn(connectedAccount);
            } else {
                //다른 계정 -> 로그인 실패
                onSignOut();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog dialog = getDialog();

        Rect rect = new Rect();
        dialog.getWindow().getWindowManager().getDefaultDisplay().getRectSize(rect);


        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.width = (int) (rect.width() * 0.9);
        layoutParams.height = (int) (rect.height() * 0.7);

        dialog.getWindow().setAttributes(layoutParams);
    }

    public void onSignIn(Account account) {
        binding.profileImg.setVisibility(View.VISIBLE);
        binding.profileName.setText(account.name);
        binding.signInBtn.setVisibility(View.GONE);
        binding.signOutBtn.setVisibility(View.VISIBLE);

    }

    public void onSignOut() {
        binding.profileImg.setVisibility(View.GONE);
        binding.profileName.setText(R.string.local);
        binding.signInBtn.setVisibility(View.VISIBLE);
        binding.signOutBtn.setVisibility(View.GONE);


    }

    @Override
    public void onDestroy() {
        getLifecycle().removeObserver(googleAccountLifeCycleObserver);
        super.onDestroy();
    }
}