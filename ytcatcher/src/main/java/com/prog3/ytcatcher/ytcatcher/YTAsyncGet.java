package com.prog3.ytcatcher.ytcatcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class YTAsyncGet extends AsyncTask<String, Void, Void> {

    final private Context context;
    final private TextView tv;
    final private ListView lv;
    final private ImageView iv;
    final private Map<Integer, String> fmt = new HashMap<Integer, String>();
    private String title;
    private ArrayList<String> tags;
    private ArrayList<String> choice;
    private Bitmap bmap;

    public YTAsyncGet(Context context, TextView tv, ListView lv, ImageView iv) {
        this.context = context;
        this.tv = tv;
        this.lv = lv;
        this.iv = iv;
        fmt.put(5, "Low Quality, 240p, FLV, 400x240");
        fmt.put(17, "Low Quality, 144p, 3GP, 176x144");
        fmt.put(18, "Medium Quality, 360p, MP4, 480x360");
        fmt.put(22, "High Quality, 720p, MP4, 1280x720");
        fmt.put(36, "Low Quality, 240p, 3GP, 400x240");
        fmt.put(43, "Medium Quality, 360p, WebM, 640x360");
        fmt.put(82, "Medium Quality 3D, 360p, MP4, 640x360");
        fmt.put(83, "Low Quality 3D, 240p, MP4, 400x240");
        fmt.put(84, "High Quality 3D, 720p, MP4, 1280x720");
        fmt.put(85, "High Quality 3D, 1080p, MP4, 1920x1080");
        fmt.put(100, "Medium Quality 3D, 360p, WebM, 640x360");

    }

    protected Void doInBackground(String... video) {
        BufferedReader br = null;
        HttpURLConnection c = null;
        try {
            URL url = new URL(video[0]);
            c = (HttpURLConnection) url.openConnection();
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0");
            br = new BufferedReader(new InputStreamReader(c.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            line = sb.toString();
            int begin = line.indexOf("title=");
            int end = line.indexOf("&", begin + 6);
            if (end == -1)
                end = line.indexOf("\"", begin + 6);
            title = URLDecoder.decode(line.substring(begin + 6, end), "UTF-8");
            title = title.replaceAll("\\+", " ");
            begin = line.indexOf("url_encoded_fmt_stream_map=");
            end = line.indexOf("&", begin + 27);
            if (end == -1)
                end = line.indexOf("\"", begin + 27);
            String obj = URLDecoder.decode(line.substring(begin + 27, end), "UTF-8");
            obj = obj.replaceAll("\\\\u0026", "&");
            begin = 0;
            end = obj.indexOf(",");
            tags = new ArrayList<String>();
            while (end != -1) {
                tags.add(obj.substring(begin, end));
                begin = end + 1;
                end = obj.indexOf(",", begin);
            }
            tags.add(obj.substring(begin));
            choice = new ArrayList<String>();
            for (String temp : tags) {
                begin = temp.indexOf("itag=");
                if (begin != -1) {
                    end = temp.indexOf("&", begin + 5);
                    if (end == -1)
                        end = temp.length();
                    int f = Integer.parseInt(temp.substring(begin + 5, end));
                    choice.add(fmt.get(new Integer(f)));
                }
            }
            String id = video[0].substring(video[0].length() - 11);
            url = new URL("http://img.youtube.com/vi/".concat(id.concat("/3.jpg")));
            bmap = BitmapFactory.decodeStream(url.openStream());
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (br != null)
                    br.close();
                if (c != null)
                    c.disconnect();
            } catch (IOException e) {
            }
            return null;
        }
    }

    protected void onProgressUpdate(Void... progress) {
        super.onProgressUpdate(progress);
    }

    protected void onPostExecute(Void args) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.adapter_entry, choice);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int pos, long id) {
                YTAsyncDown t = new YTAsyncDown(context);
                t.execute(tags.get(pos), title);
            }
        });
        adapter.notifyDataSetChanged();
        tv.setText(title);
        iv.setImageBitmap(bmap);
    }
}