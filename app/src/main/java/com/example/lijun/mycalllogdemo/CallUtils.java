package com.example.lijun.mycalllogdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lijun on 17-11-21.
 */

public class CallUtils {
    private static final boolean DEBUG = true;
    private static final String TAG = "CallUtils";

    private static final int DISPLAY_CALL_LOG_COUNT = 4;

    public static String[] getImageCallLog(Context context, ContentResolver cr){
        Cursor cs;
        String[] imageId = new String[DISPLAY_CALL_LOG_COUNT];
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            if(DEBUG){
                Log.d(TAG, "getCallHistoryList()  the permission is ok!");
            }
            cs = cr.query(CallLog.Calls.CONTENT_URI, //系统方式获取通讯录存储地址
                    new String[]{
                    CallLog.Calls.CACHED_PHOTO_ID
                    }, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
            int i = 0;
            if(DEBUG){
                Log.e(TAG, "getCallHistoryList() cs.getCount()== " + cs.getCount());
            }
            if (cs != null && cs.getCount() > 0) {
                for (cs.moveToFirst(); !cs.isAfterLast() & i < DISPLAY_CALL_LOG_COUNT; cs.moveToNext()) {
                    imageId[i] = cs.getString(0);
                    if(DEBUG){
                        Log.d(TAG, "getCallHistoryList() 图片缓存ID== " + imageId[i]);//content://com.android.contacts/display_photo/1
                    }
                    i++;
                }
            }
        }else{
            if(DEBUG){
                Log.e(TAG, "getCallHistoryList()  the permission is no have!");
            }
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CALL_LOG}, 0);
        }
        return imageId;

    }


    /**
     * 利用系统CallLog获取通话记录的电话号码或者姓名
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getCallHistoryList(Context context, ContentResolver cr) {
        Cursor cs;
        String callHistoryListStr = "";
        if (PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            if(DEBUG){
                Log.e(TAG, "getCallHistoryList()  the permission is ok!");
            }
            cs = cr.query(CallLog.Calls.CONTENT_URI, //系统方式获取通讯录存储地址
                    new String[]{
                            CallLog.Calls.CACHED_NAME,  //姓名
                            CallLog.Calls.NUMBER,    //号码
                            CallLog.Calls.TYPE,  //呼入/呼出(2)/未接
                            CallLog.Calls.DATE,  //拨打时间
                            CallLog.Calls.DURATION   //通话时长
                    }, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
            int i = 0;
            if (cs != null && cs.getCount() > 0) {
                for (cs.moveToFirst(); !cs.isAfterLast() & i < DISPLAY_CALL_LOG_COUNT; cs.moveToNext()) {
                    String callName = cs.getString(0);
                    String callNumber = cs.getString(1);
                    //通话类型
                    int callType = Integer.parseInt(cs.getString(2));
                    String callTypeStr = "";
                    switch (callType) {
                        case CallLog.Calls.INCOMING_TYPE:
                            callTypeStr = "呼入";
                            break;
                        case CallLog.Calls.OUTGOING_TYPE:
                            callTypeStr = "呼出";
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            callTypeStr = "未接";
                            break;
                        case CallLog.Calls.REJECTED_TYPE:
                            break;
                    }
                    //拨打时间
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date callDate = new Date(Long.parseLong(cs.getString(3)));
                    String callDateStr = sdf.format(callDate);
                    //通话时长
                    int callDuration = Integer.parseInt(cs.getString(4));
                    int min = callDuration / 60;
                    int sec = callDuration % 60;
                    String callDurationStr = min + "分" + sec + "秒";
                    if(DEBUG){
                        Log.e(TAG, "getCallHistoryList() 时间== " + callDurationStr);
                    }
                    String callOne = "类型：" + callTypeStr + ", 称呼：" + callName + ", 号码："
                            + callNumber + ", 通话时长：" + callDurationStr + ", 时间:" + callDateStr
                            + "\n---------------------\n";
                    if(DEBUG){
                        Log.e(TAG, "getCallHistoryList() " + callOne);
                    }
                    callHistoryListStr += callOne;
                    i++;
                }
            }
        }else{
            if(DEBUG){
                Log.e(TAG, "getCallHistoryList()  the permission is no have!");
            }
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CALL_LOG}, 0);
        }
        return callHistoryListStr;
    }
}
