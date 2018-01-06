//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloar;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import cn.easyar.engine.EasyAR;
import rx.Observable;
import rx.functions.Action1;


public class MainActivity extends AppCompatActivity {
    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloAR
    *      Package Name: cn.easyar.samples.helloar
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key = "2CbY5AB96H52OIib2ecdEgYTTjsm3W4Jv69h9QrwObN9A6TnnniXSe8CYWStmGXlpGGeyHclUbSGjmb5s2GkjkoCNiNGbUG2V9UGL2XpTyD71Go0W7tHWYYc0w7Yln8oHOovgIYOtZulKw0sS9TqGafS4pHVV6DMYSvg14vO1hcaK15xYfowAmVl6FaQ5wvyzUBoqva9";
    private GLView glView;
    private TextView textView;
    private static final String TAG = "MainActivity";
    private LinearLayout linearLayout;
    private VideoView videoView;
    private  FragmentManager fragmentManage;
//    private Fragment mFragment;//当前显示的Fragment
//    private Fragment videoFragment;

    private VideoFragment videoFragment;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        textView = (TextView) findViewById(R.id.text);
        linearLayout = (LinearLayout) findViewById(R.id.linearlayout);
        videoFragment=new VideoFragment();
//        videoView.setVisibility(View.GONE);
        linearLayout.setVisibility(View.GONE);
//        linearLayout.setBackground(getResources().getColor("#17585757"));
//        linearLayout.setBackground(getResources().getColor(("#ff55ff")));
        Observable.interval(5, TimeUnit.SECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        EventBus.getDefault().post(new MessageEvent("0"));
                    }
                });
        EventBus.getDefault().register(this);
        if (!EasyAR.initialize(this, key)) {
            Log.e("HelloAR", "Initialization Failed.");
        }
        glView = new GLView(this, this);

        requestCameraPermission(new PermissionCallback() {
            @Override
            public void onSuccess() {
                ((ViewGroup) findViewById(R.id.preview)).addView(glView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private interface PermissionCallback {
        void onSuccess();

        void onFailure();
    }

    private HashMap<Integer, PermissionCallback> permissionCallbacks = new HashMap<Integer, PermissionCallback>();
    private int permissionRequestCodeSerial = 0;

    @TargetApi(23)
    public void requestCameraPermission(PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                int requestCode = permissionRequestCodeSerial;
                permissionRequestCodeSerial += 1;
                permissionCallbacks.put(requestCode, callback);
                requestPermissions(new String[]{Manifest.permission.CAMERA}, requestCode);
            } else {
                callback.onSuccess();
            }
        } else {
            callback.onSuccess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissionCallbacks.containsKey(requestCode)) {
            PermissionCallback callback = permissionCallbacks.get(requestCode);
            permissionCallbacks.remove(requestCode);
            boolean executed = false;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    executed = true;
                    callback.onFailure();
                }
            }
            if (!executed) {
                callback.onSuccess();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        Log.e("123", "66");
        super.onResume();
        if (glView != null) {
            Log.e("123", "31");
            glView.onResume();
        }
    }

    @Override
    protected void onPause() {
        Log.e("123", "77");
        if (glView != null) {
            Log.e("123", "30");
            glView.onPause();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.e("123", "55");

        if (id == R.id.action_settings) {
            Log.e("123", "56");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(MessageEvent messageEvent) {
        String msg = messageEvent.getMessage().toString();
        Log.e(TAG, msg);
        if (msg.contains("0")) {
            linearLayout.setVisibility(View.GONE);
        }
        if (msg.contains("1")) {
            linearLayout.setVisibility(View.VISIBLE);
        }

        if (msg.contains("3")) {
            linearLayout.setVisibility(View.GONE);
//            if (videoFragment!=null){

            if(!videoFragment.isAdded()) {
               fragmentManage = getSupportFragmentManager();
                fragmentManage.beginTransaction()
                        .add(R.id.fragmentre, videoFragment)
                        .addToBackStack(null)
                        .commit();
//                }else {
//                fragmentManage.beginTransaction().remove(videoFragment).commit();
//            }
            }
        }
    }
}



