package com.lifedawn.capstoneapp.account.util;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class GoogleAccountUtil {
	
	private GoogleAccountUtil() {}
	
	public static Account getConnectedGoogleAccount(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String name = sharedPreferences.getString("connectedAccountName", "");
		String type = sharedPreferences.getString("connectedAccountType", "");
		Account account = new Account(name, type);
		return account;
	}
	
	public static void connectNewGoogleAccount(Context context,Account account) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("connectedAccountName",account.name);
		editor.putString("connectedAccountType",account.type);
		editor.commit();
	}
}
