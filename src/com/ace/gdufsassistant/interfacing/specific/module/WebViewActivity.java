/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module;

import com.ace.gdufsassistant.util.builder.DialogBuilder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * @author WIN8
 * 
 */
public class WebViewActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WebView webView = new WebView(this);
		setContentView(webView);
		WebSettings settings = webView.getSettings();
		settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);  
		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		try {
			webView.loadUrl(url);
		} catch (Exception e) {
			DialogBuilder.showErrorDialogWithoutLogin(this);
		}
	}
}
