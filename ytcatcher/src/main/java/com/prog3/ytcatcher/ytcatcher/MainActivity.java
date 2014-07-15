package com.prog3.ytcatcher.ytcatcher;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity {

    private ClipboardManager cbm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }

    protected void onStart() {
        super.onStart();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnected() && ni.isAvailable()) {
            ClipData clip = cbm.getPrimaryClip();
            if (clip != null) {
                ClipData.Item it = clip.getItemAt(0);
                String url = it.coerceToText(this).toString();
                Pattern pt = Pattern.compile("(http://|https://)?(www\\.)?(((m\\.)?youtube\\.com/watch\\?v=)|youtu\\.be/)[\\w\\-]{11}");
                if (pt.matcher(url).matches()) {
                    String id = url.substring(url.length() - 11);
                    setContentView(R.layout.choice_layout);
                    YTAsyncGet t = new YTAsyncGet(this, (TextView) findViewById(R.id.titleView), (ListView) findViewById(R.id.listView), (ImageView) findViewById(R.id.thumbsView));
                    t.execute("http://www.youtube.com/get_video_info?video_id=".concat(id));
                }
            } else
                setContentView(R.layout.error_layout);
        } else {
            setContentView(R.layout.error_layout);
            TextView tv = (TextView) findViewById(R.id.errorView);
            tv.setText(getString(R.string.net_error));
        }
    }
}