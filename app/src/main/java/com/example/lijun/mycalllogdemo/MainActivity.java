package com.example.lijun.mycalllogdemo;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;


import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final boolean DEBUG = true;
    private static final String TAG = "MainActivity";

    private TextView textView;
    private ImageView callIamge;
    private Button getData, getApplicationData;
    private String pasteData = "";
    UsageStatsManager usageStatsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if(DEBUG){
                    Log.e("MainActivity", "onCreate() onPrimaryClipChanged ");
                }
            }
        });

        textView = findViewById(R.id.call_log_text);
        callIamge = findViewById(R.id.call_image);
        getData = findViewById(R.id.get_data_button);
        getApplicationData = findViewById(R.id.get_application_data_button);
        ContentResolver cr;
        cr=getContentResolver();
        final String callHistoryListStr=CallUtils.getCallHistoryList(this, cr);
        textView.setTextSize(12.0f);
        textView.setText(callHistoryListStr);

//        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//        final long time =System.currentTimeMillis()-24*60*60*1000;

//        getApplicationData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
////                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
//                    List<UsageStats> list=usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time, System.currentTimeMillis());
//                    if(list.size() == 0){
//                        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
//                    }else{
//                        String result = "";
//                        for(int i = 0; i<list.size(); i++){
//                            result = list.get(i).getPackageName() + result;
//                        }
//                        textView.setText(result);
//                    }
//                }
//            }
//        });



        getData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                pasteData = item.coerceToText(MainActivity.this).toString();
                if(DEBUG){
                    Log.e("MainActivity", "onCreate() 剪贴板的纯文本为== " + pasteData);
                    Log.e("MainActivity", "onCreate() 剪贴板的URI为== " + item.getUri());
                    Log.e("MainActivity", "onCreate() 剪贴板的Intent为== " + item.getIntent());
                }
                textView.setText(pasteData);
            }
        });
        get_people_image(cr, "15555215554");  //获取用户头像

        //监听数据库的变化
        Uri uriCallLog = CallLog.Calls.CONTENT_URI;
        getContentResolver().registerContentObserver(uriCallLog, false, mContentObserver);
    }
    private Handler mHandler = new Handler(Looper.myLooper());

    private ContentObserver mContentObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            ContentResolver cr;
            cr=getContentResolver();
            final String callHistoryListStr=CallUtils.getCallHistoryList(MainActivity.this, cr);
            Log.e(TAG, "通话记录发生变化==  " + callHistoryListStr);
//            Log.e(TAG, "通话记录发生变化==  " + uri);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults[0] == PERMISSION_GRANTED){
            if(DEBUG){
                Log.e("MainActivity", "onRequestPermissionsResult() 允许权限 grantResults== " + grantResults[0]);
            }
        }else{
            if(DEBUG){
                Log.e("MainActivity", "onRequestPermissionsResult() 拒绝权限 grantResults== " + grantResults[0]);
            }
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])){
                if(DEBUG){
                    Log.e("MainActivity", "onRequestPermissionsResult() 暂时拒绝");
                }
            }else{
                if(DEBUG){
                    Log.e("MainActivity", "onRequestPermissionsResult() 永久拒绝");
                }
            }
        }

    }

    public void get_people_image(ContentResolver cr, String x_number) {

        // 获得Uri
        Uri uriNumber2Contacts = Uri.parse("content://com.android.contacts/"
                + "data/phones/filter/" + x_number);

        Cursor cursorCantacts =cr.query(
                uriNumber2Contacts,
                null,
                null,
                null,
                null);
        // 如果该联系人存在
        if (cursorCantacts.getCount() > 0) {
            // 移动到第一条数据
            cursorCantacts.moveToFirst();
            // 获得该联系人的contact_id
            Long contactID = cursorCantacts.getLong(cursorCantacts.getColumnIndex("contact_id"));
            // 获得contact_id的Uri
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID);
            Uri displayPhotoUri = Uri.withAppendedPath(uri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
            try {
                AssetFileDescriptor fd = getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
                Bitmap bmp_head = BitmapFactory.decodeStream(fd.createInputStream());
                callIamge.setImageBitmap(bmp_head);
            } catch (IOException e) {
                if(DEBUG){
                    Log.e("MainActivity", "异常==  " + e.getMessage());
                }
            }
        }

    }
}

