package com.prog3.ytcatcher.ytcatcher;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.Iterator;
import java.util.Map;

public class YTAsyncGet extends AsyncTask<String, Void, Void> {

    final private Context context;
    final private TextView tv;
    final private ListView lv;
    final private Map<Integer, String> fmt = new HashMap<Integer, String>();
    private String title, obj;
    private ArrayList<String> choice;

    public YTAsyncGet(Context context, TextView tv, ListView lv) {
        this.context = context;
        this.tv = tv;
        this.lv = lv;
        fmt.put(5, "Low Quality, 240p, FLV, 400x240");
        fmt.put(17, "Low Quality, 144p, 3GP, 0x0");
        fmt.put(18, "Medium Quality, 360p, MP4, 480x360");
        fmt.put(22, "High Quality, 720p, MP4, 1280x720");
        fmt.put(34, "Medium Quality, 360p, FLV, 640x360");
        fmt.put(35, "Standard Definition, 480p, FLV, 854x480");
        fmt.put(36, "Low Quality, 240p, 3GP, 0x0");
        fmt.put(37, "Full High Quality, 1080p, MP4, 1920x1080");
        fmt.put(38, "Original Definition, MP4, 4096x3072");
        fmt.put(43, "Medium Quality, 360p, WebM, 640x360");
        fmt.put(44, "Standard Definition, 480p, WebM, 854x480");
        fmt.put(45, "High Quality, 720p, WebM, 1280x720");
        fmt.put(46, "Full High Quality, 1080p, WebM, 1280x720");
        fmt.put(82, "Medium Quality 3D, 360p, MP4, 640x360");
        fmt.put(84, "High Quality 3D, 720p, MP4, 1280x720");
        fmt.put(100, "Medium Quality 3D, 360p, WebM, 640x360");
        fmt.put(102, "High Quality 3D, 720p, WebM, 1280x720");
    }

    protected Void doInBackground(String... video) {
        BufferedReader br = null;
        try {
            URL url = new URL(video[0]);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36");
            c.connect();
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
            obj = URLDecoder.decode(line.substring(begin + 27, end), "UTF-8");
            obj = obj.replaceAll("\\\\u0026", "&");
            begin = 0;
            end = obj.indexOf(",");
            ArrayList<String> tags = new ArrayList<String>();
            while (end != -1) {
                tags.add(obj.substring(begin, end));
                begin = end + 1;
                end = obj.indexOf(",", begin);
            }
            tags.add(obj.substring(begin, obj.length()));
            Iterator<String> it = tags.iterator();
            choice = new ArrayList<String>();
            while (it.hasNext()) {
                String temp = it.next();
                begin = temp.indexOf("itag=");
                if (begin != -1) {
                    end = temp.indexOf("&", begin + 5);
                    if (end == -1)
                        end = temp.length();
                    int f = Integer.parseInt(temp.substring(begin + 5, end));
                    choice.add(fmt.get(new Integer(f)));
                }
            }
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (br != null)
                    br.close();
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
                TextView temp = (TextView) v;
                t.execute(obj, temp.getText().toString(), title);
            }
        });
        adapter.notifyDataSetChanged();
        tv.setText(title);
    }
}