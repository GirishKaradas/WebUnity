package com.genius.unitysample;

import com.example.glass.ui.GlassGestureDetector;
import com.unity3d.player.*;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UnityPlayerActivity extends BaseActivity
{
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code

    // Override this in your custom UnityPlayerActivity to tweak the command line arguments passed to the Unity Android Player
    // The command line arguments are passed as a string, separated by spaces
    // UnityPlayerActivity calls this from 'onCreate'
    // Supported: -force-gles20, -force-gles30, -force-gles31, -force-gles31aep, -force-gles32, -force-gles, -force-vulkan
    // See https://docs.unity3d.com/Manual/CommandLineArguments.html
    // @param cmdLine the current command line arguments, may be null
    // @return the modified command line string or null
    protected String updateUnityCommandLineArguments(String cmdLine)
    {
        return cmdLine;
    }
    File imgFile;

    // Setup activity layout
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        String cmdLine = updateUnityCommandLineArguments(getIntent().getStringExtra("unity"));
        getIntent().putExtra("unity", cmdLine);

        mUnityPlayer = new UnityPlayer(this);
        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();
    }

    @Override protected void onNewIntent(Intent intent) {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        super.onNewIntent(intent);
        setIntent(intent);
    }

    // Quit Unity
    @Override protected void onDestroy ()
    {
        mUnityPlayer.destroy();
        super.onDestroy();
    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();
        mUnityPlayer.resume();
    }

    @Override protected void onStart()
    {
        super.onStart();
        mUnityPlayer.start();
    }

    @Override protected void onStop()
    {
        super.onStop();
        mUnityPlayer.stop();
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }

    @Override
    public boolean onGesture(GlassGestureDetector.Gesture gesture) {

        switch (gesture){

            case SWIPE_UP:
                UnityPlayer.UnitySendMessage("ModelLoader", "TopView", "");
                break;

            case SWIPE_FORWARD:
                UnityPlayer.UnitySendMessage("ModelLoader", "SideView", "");
                break;

            case TAP:
                Toast.makeText(this, "Loading Object", Toast.LENGTH_SHORT).show();
                String gltfUrl = "https://raw.githubusercontent.com/GirishKaradas/GlassTest/master/CesiumMilkTruck.gltf";
         //       MyAsyncTasks asyncTasks = new MyAsyncTasks();
          //      asyncTasks.execute(gltfUrl);
                UnityPlayer.UnitySendMessage("ModelLoader", "DownloadFile", gltfUrl);
                break;
        }
        return super.onGesture(gesture);
    }

   /* class MyAsyncTasks extends AsyncTask<String, String, String> {

        File sdCardRoot;

        @Override
        protected String doInBackground(String... strings) {

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
              //  urlConnection.setRequestMethod("GET");
             //   urlConnection.setDoOutput(true);
                urlConnection.connect();


             //   sdCardRoot = new File(Environment.getExternalStorageDirectory(), "WebSample");
                sdCardRoot = new File("/storage/emulated/0/Android/data/com.genius.unitysample/files/Files");
                Log.e("Progress: path", sdCardRoot.getAbsolutePath());
                if (!sdCardRoot.exists()) {
                    Log.e("Progress:", "not created");
                    sdCardRoot.mkdirs();
                    Log.e("Progress:", "Created");
                }

                Log.e("check_path", "" + sdCardRoot.getAbsolutePath());

                String fileName =
                        strings[0].substring(strings[0].lastIndexOf('/') + 1, strings[0].length());
                Log.e("Progress:", "" + fileName);
                imgFile =
                        new File(sdCardRoot, fileName);
                if (sdCardRoot.exists()) {
                    imgFile.createNewFile();
                    Log.e("Progress:", "Created");
                }
                InputStream inputStream = urlConnection.getInputStream();
                int totalSize = urlConnection.getContentLength();
                FileOutputStream outPut = new FileOutputStream(imgFile);
                int downloadedSize = 0;
                byte[] buffer = new byte[2024];
                int bufferLength = 0;
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    outPut.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                    Log.e("Progress:", "downloadedSize:" + Math.abs(downloadedSize * 100 / totalSize));
                }
                Log.e("Progress:", "imgFile.getAbsolutePath():" + imgFile.getAbsolutePath());

                Log.e("Progress:", "check image path 2" + imgFile.getAbsolutePath());

                outPut.close();
                Log.e("Progress:", "Sent to unity");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("checkException:-", "" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("Progress:", "Unity Started");
            UnityPlayer.UnitySendMessage("ModelLoader", "LoadModel", imgFile.getAbsolutePath());

        }

    }


    */
}
