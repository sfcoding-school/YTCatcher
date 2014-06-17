package com.prog3.ytcatcher.ytcatcher;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;

public class YTAsyncTask extends AsyncTask<String, Byte, Void> {

    final private Context context;

    public YTAsyncTask(Context context) {
        this.context = context;
    }

    protected Void doInBackground(String... video) {
        BufferedReader br = null;
        FileOutputStream fs = null;
        InputStream is = null;
        try {
            URL url = new URL(video[0]);
            br = new BufferedReader(new InputStreamReader(url.openStream()));
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
            String title = URLDecoder.decode(line.substring(begin + 6, end), "UTF-8");
            title = title.replaceAll("\\+", " ");
            begin = line.indexOf("url_encoded_fmt_stream_map=");
            end = line.indexOf("&", begin + 27);
            if (end == -1)
                end = line.indexOf("\"", begin + 27);
            String obj = URLDecoder.decode(line.substring(begin + 27, end), "UTF-8");
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
            String result = "";
            while (it.hasNext()) {
                String temp = it.next();
                begin = temp.indexOf("itag=");
                if (begin != -1) {
                    end = temp.indexOf("&", begin + 5);
                    if (end == -1)
                        end = temp.length();
                    int f = Integer.parseInt(temp.substring(begin + 5, end));
                    if (f == 18) {
                        begin = temp.indexOf("url=");
                        if (begin != -1) {
                            end = temp.indexOf("&", begin + 4);
                            if (end == -1)
                                end = temp.length();
                            result = URLDecoder.decode(temp.substring(begin + 4, end), "UTF-8");
                            break;
                        }
                    }
                }
            }
            url = new URL(result);
            Log.i("My", title);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.34 Safari/534.24");
            fs = new FileOutputStream(new File("/storage/sdcard0", title + ".mp4"));
            is = c.getInputStream();
            byte[] buffer = new byte[4096];
            int size = 0;
            while ((size = is.read(buffer)) > 0)
                fs.write(buffer, 0, size);
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (br != null)
                    br.close();
                if (is != null)
                    is.close();
                if (fs != null)
                    fs.close();
            } catch (IOException e) {
            }
            return null;
        }
    }

    protected void onProgressUpdate(Byte... progress) {
        super.onProgressUpdate(progress);
    }

    protected void onPostExecute(Void args) {
        Toast.makeText(context, "Video scaricato con successo!", Toast.LENGTH_LONG).show();
    }
}