package com.example.afbs_summit;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.annotation.RequiresApi;

public class MainActivity extends Activity {

    private final int STORAGE_PERMISSION_CODE = 1;
    private WebView mWebView;
    private TextView offlineTextView;

    private void requestStoragePermission() {
        // ... (Your existing code for storage permission)
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        requestStoragePermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        offlineTextView = findViewById(R.id.offlineText);

        mWebView = findViewById(R.id.activity_main_webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Set up WebViewClient to handle loading progress and URL overrides
        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Show the loading animation when the page starts loading
                findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
            }

            public void onPageFinished(WebView view, String url) {
                // Hide the loading animation when the page finishes loading
                findViewById(R.id.loading_progress).setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                view.loadUrl(url);
                return true;
            }
        });

        if (isNetworkAvailable()) {
            // Load the URL when the internet is available
            mWebView.setVisibility(View.VISIBLE);
            offlineTextView.setVisibility(View.GONE);
            mWebView.loadUrl("https://afbs.footballfoundation.africa/2023/");
        } else {
            // Show offline layout when there's no internet
            mWebView.setVisibility(View.GONE);
            offlineTextView.setVisibility(View.VISIBLE);
        }

        mWebView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            // ... (Your existing code for download)
        });

        registerNetworkChangeReceiver();
    }

    private void registerNetworkChangeReceiver() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void unregisterNetworkChangeReceiver() {
        try {
            unregisterReceiver(networkChangeReceiver);
        } catch (IllegalArgumentException e) {
            // Ignore the exception if the receiver is not registered.
        }
    }

    private final BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkAvailable()) {
                // Internet connection is available
                if (mWebView.getVisibility() == View.GONE) {
                    // Reload the content if WebView is hidden
                    mWebView.setVisibility(View.VISIBLE);
                    offlineTextView.setVisibility(View.GONE);
                    mWebView.loadUrl("https://afbs.footballfoundation.africa/2023/");
                }
            } else {
                // No internet connection
                mWebView.setVisibility(View.GONE);
                offlineTextView.setVisibility(View.VISIBLE);
            }
        }
    };

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkChangeReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNetworkChangeReceiver();
    }

    @Override
    public void onBackPressed() {
        // If the WebView can go back, go back in its history
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            // Otherwise, let the default back button behavior take place (e.g., close the activity)
            super.onBackPressed();
        }
    }
}

