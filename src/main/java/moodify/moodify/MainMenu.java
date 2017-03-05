package moodify.moodify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainMenu extends AppCompatActivity {

    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);

//        InputStream stream = null;
//        try {
//            stream = getAssets().open("colourCircle.gif");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        WebView webviewActionView;
//
//        webviewActionView = (WebView)findViewById(R.id.webviewActionView);
//        webviewActionView.setWebViewClient(new MainMenu.MyWebViewClient());
//        webviewActionView.getSettings().setJavaScriptEnabled(true);
//
//        GifWebView view = new GifWebView(this, stream);
//        webviewActionView.addView(view);
//        WebView webviewActionView=(WebView)findViewById(R.id.webviewActionView);
//        webviewActionView.loadUrl("colourCircle.html");
    }

    private class MyWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
