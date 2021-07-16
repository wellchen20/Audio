package com.mtkj.cnpc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.mtkj.cnpc.utils.Global;

import org.json.JSONException;
import org.json.JSONObject;

public class service extends Service {
    private static final String TAG = "service";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(TAG, "rokid service");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, TAG);
        Notification noti = builder.setContentTitle("rokid service")
                .setContentText("rokid service")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        startForeground(1, noti);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getStringExtra("action");
            String nlp = intent.getStringExtra("nlp");
            if (nlp != null)
                parseNlp(nlp,action);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void parseNlp(String nlp,String action) {
        Log.e(TAG, "parseNlp: "+nlp );
        Log.e(TAG, "action: "+action );
        if(TextUtils.isEmpty(nlp)){
            return;
        }
        Intent broadcastIntent = null;
        try {
            JSONObject nlpObj = new JSONObject(nlp);
            String intent = nlpObj.getString("intent");
        if(Global.OPEN_MEETTING.equals(intent)){
            Intent intent1 = new Intent(this, LoginActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity(intent1);
        }else if (Global.SELECT_ITEM.equals(intent)){
            broadcastIntent = new Intent(Global.SELECT_ITEM);
            String num = null;
            if (nlpObj.has("slots")){
                JSONObject slots = nlpObj.getJSONObject("slots");
                Log.e(TAG, "slots: "+slots );
                if (slots.has("num")){
                    JSONObject number = slots.getJSONObject("num");
                    if (number.has("value")){
                        num = number.getString("value");
                        JSONObject object = new JSONObject(num);
                        num = object.getString("number");
                        broadcastIntent.putExtra("num",num);
                        sendBroadcast(broadcastIntent);
                        Log.e(TAG, "num:"+num );
                    }
                }
            }

        }else if (Global.HUNG_UP_MEETTING.equals(intent)){
            sendBroadcast(new Intent(Global.HUNG_UP_MEETTING));
        }else if (Global.EXIT_APP.equals(intent)){
            sendBroadcast(new Intent(Global.EXIT_APP));
        }else if (Global.CALL_NUMBER.equals(intent)){
            broadcastIntent = new Intent(Global.CALL_NUMBER);
            String num = null;
            if (nlpObj.has("slots")){
                JSONObject slots = nlpObj.getJSONObject("slots");
                Log.e(TAG, "slots: "+slots );
                if (slots.has("num")){
                    JSONObject number = slots.getJSONObject("num");
                    if (number.has("value")){
                        num = number.getString("value");
                        JSONObject object = new JSONObject(num);
                        num = object.getString("number");
                        broadcastIntent.putExtra("num",num);
                        sendBroadcast(broadcastIntent);
                        Log.e(TAG, "num:"+num );
                    }
                }
            }
            sendBroadcast(new Intent(Global.CALL_NUMBER));
        }else if (Global.DELETE_ITEM.equals(intent)){
            broadcastIntent = new Intent(Global.DELETE_ITEM);
            String num = null;
            if (nlpObj.has("slots")){
                JSONObject slots = nlpObj.getJSONObject("slots");
                Log.e(TAG, "slots: "+slots );
                if (slots.has("num")){
                    JSONObject number = slots.getJSONObject("num");
                    if (number.has("value")){
                        num = number.getString("value");
                        JSONObject object = new JSONObject(num);
                        num = object.getString("number");
                        broadcastIntent.putExtra("num",num);
                        sendBroadcast(broadcastIntent);
                        Log.e(TAG, "num:"+num );
                    }
                }
            }
            sendBroadcast(new Intent(Global.CALL_NUMBER));
        }else if (Global.BEGIN_MEETTING.equals(intent)){
            sendBroadcast(new Intent(Global.BEGIN_MEETTING));
        }else if (Global.OPEN_VIDEO.equals(intent)){
            sendBroadcast(new Intent(Global.OPEN_VIDEO));
        }else if (Global.CLOSE_VIDEO.equals(intent)){
            sendBroadcast(new Intent(Global.CLOSE_VIDEO));
        }else if (Global.OPEN_MIKE.equals(intent)){
            sendBroadcast(new Intent(Global.OPEN_MIKE));
        }else if (Global.CLOSE_MIKE.equals(intent)){
            sendBroadcast(new Intent(Global.CLOSE_MIKE));
        }else if (Global.START_RECORDING.equals(intent)){
            sendBroadcast(new Intent(Global.START_RECORDING));
        }else if (Global.STOP_RECORDING.equals(intent)){
            sendBroadcast(new Intent(Global.STOP_RECORDING));
        }else if (Global.SWITCH_LAYOUT.equals(intent)){
            sendBroadcast(new Intent(Global.SWITCH_LAYOUT));
        }else if (Global.NEXT_PAGE.equals(intent)){
            sendBroadcast(new Intent(Global.NEXT_PAGE));
        }else if (Global.LAST_PAGE.equals(intent)){
            sendBroadcast(new Intent(Global.LAST_PAGE));
        }else if (Global.HOLD_ON.equals(intent)){
            sendBroadcast(new Intent(Global.HOLD_ON));
        }else if (Global.HANG_UP.equals(intent)){
            sendBroadcast(new Intent(Global.HANG_UP));
        }else if (Global.LOGIN_XY.equals(intent)){
            sendBroadcast(new Intent(Global.LOGIN_XY));
        }else if (Global.SET_PROXY.equals(intent)){
            sendBroadcast(new Intent(Global.SET_PROXY));
        }else if (Global.DELETE_PROXY.equals(intent)){
            sendBroadcast(new Intent(Global.DELETE_PROXY));
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
