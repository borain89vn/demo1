package com.knx.framework.main;

import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.knx.framework.R;

public class WebActivity extends Activity {

    boolean callback = false;
    int delay = 0;
    Timer timer;
    
    private ProgressBar loadingIndicator;
    private WebView mWebView;
    
    private ImageButton backwardButton;
    private ImageButton forwardButton;
    
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        
        setContentView(R.layout.web_activity_arise);
        
        loadingIndicator = (ProgressBar) findViewById(R.id.webviewLoadingIndicator);
        mWebView = (WebView) findViewById(R.id.webpage);
        
        backwardButton = (ImageButton) findViewById(R.id.backward_button);
        backwardButton.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		mWebView.goBack();
        	}
        });
        forwardButton = (ImageButton) findViewById(R.id.forward_button);
        forwardButton.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		mWebView.goForward();
        	}
        });

        Bundle extras = getIntent().getExtras();
        String urlLink = extras.getString("weblink");
        callback = extras.getBoolean("callback");
        delay = extras.getInt("delay");
        
        WebViewClient myWebViewClient = new WebViewClient() {
        	
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            	WebActivity.this.runOnUiThread(new Runnable() {
            		public void run() {
            			backwardButton.setClickable(false);
            			forwardButton.setClickable(false);
            			showProgressBar();
            		}
            	});
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
            	WebActivity.this.runOnUiThread(new Runnable() {
            		public void run() {
            			hideProgressBar();
            			
//            			if (mWebView.canGoBack()) {
//            				backwardButton.setVisibility(View.VISIBLE);
//            				backwardButton.setClickable(true);
//            			} else {
//            				backwardButton.setVisibility(View.GONE);
//            				backwardButton.setClickable(false);
//            			}
//            			
//            			if (mWebView.canGoForward()) {
//            				forwardButton.setVisibility(View.VISIBLE);
//            				forwardButton.setClickable(true);
//            			} else {
//            				forwardButton.setVisibility(View.GONE);
//            				forwardButton.setClickable(false);
//            			}
            		}
            	});
            	
                if (callback == true) {
                    timer = new Timer();
                    timer.schedule(new BackTask(), 1000 * delay);
                }
            }
            
            @Override
            public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
                error.getCertificate();
            }
        };

        WebChromeClient mWebChromeClient = new WebChromeClient() {
        	public void onProgressChanged(WebView view, int newProgress) {
        		if (callback == true && delay > 0) {
                	timer = new Timer();
                    timer.schedule(new BackTask(), 1000 * delay);
                }
            }
        };
        
//        mWebView = (WebView) findViewById(R.id.webpage);
//        //mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
//        //mWebView.setWebViewClient(myWebViewClient);
//        //mWebView.getSettings().setPluginsEnabled(true);
//        mWebView.setWebChromeClient(mWebChromeClient);
//        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.getSettings().setAppCacheEnabled(true);
//        mWebView.setInitialScale(1);
//        //mWebView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//        //mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
//
//        //urlLink = "http://fast.wistia.com/embed/iframe/li6ov9kl9g?autoPlay=true&videoFoam=true&smallPlayButton=false&playbar=false&fullscreenButton=false";
//        mWebView.loadUrl(urlLink);
//        mWebView.getSettings().setLoadWithOverviewMode(true);
//        mWebView.getSettings().setUseWideViewPort(true);
//        
//        //mWebView.setInitialScale(30);
//        
////        mWebView.getSettings().setSupportZoom(true);
//        mWebView.getSettings().setBuiltInZoomControls(true);
//        mWebView.getSettings().setDisplayZoomControls(false);
        
        mWebView.setWebViewClient(myWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);

        //mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        
      //  mWebView.getSettings().setPluginsEnabled(true);
        mWebView.getSettings().setPluginState(PluginState.ON);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        //mWebView.setInitialScale(1);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        
//        mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
//        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        mWebView.getSettings().setAllowFileAccess(true);
        
        mWebView.setBackgroundColor(0x00000000);
        String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.36 (KHTML, like Gecko) Chrome/13.0.766.0 Safari/534.36";
        mWebView.getSettings().setUserAgentString(userAgent);
        
        if (urlLink.endsWith(".pdf")) {
        	String urlPdf = "http://docs.google.com/gview?embedded=true&url="+urlLink;
        	mWebView.loadUrl(urlPdf);
        } else {
        	mWebView.loadUrl(urlLink);
        }
    }
    
    private void showProgressBar() {
    	if (loadingIndicator == null) {
    		loadingIndicator = (ProgressBar) findViewById(R.id.webviewLoadingIndicator);
    	}
    	loadingIndicator.setVisibility(View.VISIBLE);
    }
    
    private void hideProgressBar() {
    	if (loadingIndicator == null) {
    		loadingIndicator = (ProgressBar) findViewById(R.id.webviewLoadingIndicator);
    	}
    	loadingIndicator.setVisibility(View.GONE);
    }
    
    @Override
    public void onBackPressed(){
//    	super.onBackPressed();
    	
    	if (mWebView != null && mWebView.canGoBack()) {
    		mWebView.goBack();
    	} else {
    		super.onBackPressed();
    		if (mWebView != null) {
    			mWebView.removeAllViews();
        		mWebView.destroy();
    		}
    		finish();
    	}
    }
    
    @Override
    public void onPause() {
        super.onPause();

        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause", (Class[]) null)
                                .invoke(mWebView, (Object[]) null);
        } catch(ClassNotFoundException cnfe) {
        	cnfe.printStackTrace();
        } catch(NoSuchMethodException nsme) {
        	nsme.printStackTrace();
        } catch(InvocationTargetException ite) {
        	ite.printStackTrace();
        } catch (IllegalAccessException iae) {
        	iae.printStackTrace();
        }
    }

    /********************************************************************
     * A task for finishing the activity.
     * @author le_vu
     *
     */
    private class BackTask extends TimerTask {
        @Override
        public void run() {
            finish();
        }
    }
}
