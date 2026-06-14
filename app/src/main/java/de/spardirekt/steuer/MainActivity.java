package de.spardirekt.steuer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.*;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private Uri cameraImageUri;
    private static final int FILE_CHOOSER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        webView = new WebView(this);
        webView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView wv, ValueCallback<Uri[]> cb,
                    FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = cb;

                String[] accept = params.getAcceptTypes();
                boolean capture = params.isCaptureEnabled();

                if (capture) {
                    try {
                        File imgFile = createImageFile();
                        cameraImageUri = FileProvider.getUriForFile(MainActivity.this,
                                BuildConfig.APPLICATION_ID + ".fileprovider", imgFile);
                        Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cam.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                        startActivityForResult(cam, FILE_CHOOSER_REQUEST);
                    } catch (IOException e) {
                        filePathCallback = null;
                    }
                } else {
                    Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                    gallery.setType("image/*");
                    startActivityForResult(Intent.createChooser(gallery, "Bild wählen"),
                            FILE_CHOOSER_REQUEST);
                }
                return true;
            }
        });

        webView.loadUrl("https://gangster2490.github.io/steuer-app/");
    }

    private File createImageFile() throws IOException {
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("BELEG_" + stamp, ".jpg", dir);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (filePathCallback == null) return;
        Uri[] results = null;
        if (res == RESULT_OK) {
            if (data != null && data.getData() != null) {
                results = new Uri[]{data.getData()};
            } else if (cameraImageUri != null) {
                results = new Uri[]{cameraImageUri};
            }
        }
        filePathCallback.onReceiveValue(results);
        filePathCallback = null;
        cameraImageUri = null;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
