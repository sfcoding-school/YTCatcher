package com.prog3.ytcatcher.ytcatcher;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class YTAsyncDown extends AsyncTask<String, String, Void> {

    final private Context context;
    final private ProgressDialog pd;
    final private Map<String, Integer> fmt = new HashMap<String, Integer>();
    boolean copyrighted, complete;

    public YTAsyncDown(Context context) {
        this.context = context;
        pd = new ProgressDialog(context);
        fmt.put("Low Quality, 240p, FLV, 400x240", 5);
        fmt.put("Low Quality, 144p, 3GP, 176x144", 17);
        fmt.put("Medium Quality, 360p, MP4, 480x360", 18);
        fmt.put("High Quality, 720p, MP4, 1280x720", 22);
        fmt.put("Low Quality, 240p, 3GP, 400x240", 36);
        fmt.put("Medium Quality, 360p, WebM, 640x360", 43);
        fmt.put("Medium Quality 3D, 360p, MP4, 640x360", 82);
        fmt.put("Low Quality 3D, 240p, MP4, 400x240", 83);
        fmt.put("High Quality 3D, 720p, MP4, 1280x720", 84);
        fmt.put("High Quality 3D, 1080p, MP4, 1920x1080", 85);
        fmt.put("Medium Quality 3D, 360p, WebM, 640x360", 100);
    }

    protected void onPreExecute() {
        super.onPreExecute();
        pd.setTitle(context.getString(R.string.pd_desc));
        pd.setCancelable(false);
        pd.setIndeterminate(false);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.show();
    }

    protected Void doInBackground(String... video) {
        FileOutputStream fs = null;
        InputStream is = null;
        HttpURLConnection c = null;
        try {
            int l = fmt.get(video[1]);
            String obj = video[0];
            int begin = 0;
            int end = obj.indexOf(",");
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
                    if (f == l) {
                        begin = temp.indexOf("url=");
                        end = temp.indexOf("&", begin + 4);
                        if (end == -1)
                            end = temp.length();
                        result = URLDecoder.decode(temp.substring(begin + 4, end), "UTF-8");
                        begin = temp.indexOf("s=");
                        if (begin != -1) {
                            copyrighted = true;
                            throw new Exception();
                        } else
                            copyrighted = false;
                        break;
                    }
                }
            }
            URL url = new URL(result);
            c = (HttpURLConnection) url.openConnection();
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0");
            pd.setMax(c.getContentLength() / 1024);
            String ext = "";
            switch (l) {
                case 5:
                case 34:
                case 35:
                    ext = ".flv";
                    break;
                case 17:
                case 36:
                    ext = ".3gp";
                    break;
                case 18:
                case 22:
                case 37:
                case 38:
                case 82:
                case 84:
                    ext = ".mp4";
                    break;
                default:
                    ext = ".webm";
                    break;
            }
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "YTCatcher");
            if (!dir.exists())
                dir.mkdirs();
            String title = video[2].replaceAll("\"", "");
            title = title.replaceAll("<", "");
            title = title.replaceAll(">", "");
            title = title.replaceAll(":", "");
            title = title.replaceAll("/", "");
            title = title.replaceAll("\\\\", "");
            title = title.replaceAll("|", "");
            title = title.replaceAll("\\?", "");
            title = title.replaceAll("\\*", "");
            title = title.concat(ext);
            complete = false;
            fs = new FileOutputStream(new File(dir, title));
            is = c.getInputStream();
            byte[] buffer = new byte[4096];
            int size = 0;
            int downloaded = 0;
            while ((size = is.read(buffer)) > 0) {
                fs.write(buffer, 0, size);
                downloaded += size;
                publishProgress("" + (downloaded / 1024));
            }
            fs.flush();
            complete = true;
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(dir, title))));
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } catch (Exception e) {
        } finally {
            try {
                if (is != null)
                    is.close();
                if (fs != null)
                    fs.close();
                if (c != null)
                    c.disconnect();
            } catch (IOException e) {
            }
            return null;
        }
    }

    protected void onProgressUpdate(String... progress) {
        super.onProgressUpdate(progress[0]);
        pd.setProgress(Integer.parseInt(progress[0]));
    }

    protected void onPostExecute(Void args) {
        pd.dismiss();
        if (copyrighted)
            Toast.makeText(context, context.getString(R.string.copyright), Toast.LENGTH_LONG).show();
        else if (complete)
            Toast.makeText(context, context.getString(R.string.success), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, context.getString(R.string.unsuccess), Toast.LENGTH_LONG).show();
    }
}