package com.lifedawn.capstoneapp.appsettings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.appsettings.notification.DefaultNotificationSettingsFragment;
import com.lifedawn.capstoneapp.databinding.FragmentAppSettingsBinding;


public class AppSettingsFragment extends Fragment {
    private FragmentAppSettingsBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppSettingsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.notificationDefaultSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DefaultNotificationSettingsFragment defaultNotificationSettingsFragment = new DefaultNotificationSettingsFragment();
                getParentFragmentManager().beginTransaction().hide(AppSettingsFragment.this).add(R.id.fragmentContainerView, defaultNotificationSettingsFragment,
                        DefaultNotificationSettingsFragment.class.getName()).addToBackStack(DefaultNotificationSettingsFragment.class.getName())
                        .commit();
            }
        });
    }
}