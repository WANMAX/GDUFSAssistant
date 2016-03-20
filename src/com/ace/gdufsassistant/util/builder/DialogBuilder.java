/**
 * 
 */
package com.ace.gdufsassistant.util.builder;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.interfacing.specific.module.living.ModifyPasswordActivity;
import com.ace.gdufsassistant.util.TestNetworkState;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.KeyEvent;
import main.API;

/**
 * @author wan
 *
 */
public class DialogBuilder {
	public static Dialog baseDialog(Activity activity, String text) {
		return dialog = new AlertDialog.Builder(activity).setTitle(R.string.warning).setMessage(text)
				.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						dialog.dismiss();
						dialog = null;
						new Thread() {
							@Override
							public void run() {
								new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
							}
						}.start();
					}
				}).create();
	}

	public static Dialog baseDialogNoReturn(Activity activity, String text) {
		return new AlertDialog.Builder(activity).setTitle(R.string.warning).setMessage(text)
				.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				}).create();
	}
	private static Dialog dialog;
	public static Dialog baseDialog(Activity activity, int resource) {
		return dialog = new AlertDialog.Builder(activity).setTitle(R.string.warning).setMessage(resource)
				.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						dialog.dismiss();
						dialog = null;
						new Thread() {
							@Override
							public void run() {
								new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
							}
						}.start();
					}
				}).create();
	}

	public static Dialog baseDialogNoReturn(Activity activity, int resource) {
		return new AlertDialog.Builder(activity).setTitle(R.string.warning).setMessage(resource)
				.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				}).create();
	}
	
	public static void showNotFinishedDialog(Activity activity) {
		baseDialog(activity, R.string.not_finished).show();
	}
	public static void showNotFinishedDialogNoReturn(Activity activity) {
		baseDialogNoReturn(activity, R.string.not_finished).show();
	}
	public static void showErrorDialog(Activity activity) {
		if(!TestNetworkState.testNetwork(activity))
			baseDialog(activity, R.string.no_internet).show();
		else if(!API.getInstance().isLogged())
			baseDialog(activity, R.string.not_logged).show();
		else
			baseDialog(activity, R.string.loading_error).show();
	}
	public static void showErrorDialogNoReturn(Activity activity) {
		if(!TestNetworkState.testNetwork(activity))
			baseDialogNoReturn(activity, R.string.no_internet).show();
		else if(!API.getInstance().isLogged())
			baseDialogNoReturn(activity, R.string.not_logged).show();
		else
			baseDialogNoReturn(activity, R.string.loading_error).show();
	}
	public static void showErrorDialogWithoutLogin(Activity activity) {
		ConnectivityManager cwjManager=(ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo info = cwjManager.getActiveNetworkInfo(); 
		boolean internetError;
		if (info != null && info.isAvailable()) 
			internetError = false;
		else
			internetError = true;
		if(internetError)
			baseDialog(activity, R.string.no_internet).show();
		else
			baseDialog(activity, R.string.loading_error).show();
	}
	public static void showErrorNoReturnDialogWithoutLogin(Activity activity) {
		ConnectivityManager cwjManager=(ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo info = cwjManager.getActiveNetworkInfo(); 
		boolean internetError;
		if (info != null && info.isAvailable()) 
			internetError = false;
		else
			internetError = true;
		if(internetError)
			baseDialogNoReturn(activity, R.string.no_internet).show();
		else
			baseDialogNoReturn(activity, R.string.loading_error).show();
	}
}
