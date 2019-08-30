package yzw.ahaqth.emaildemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class NetWorkTest {
    static boolean isNetworkConnected(Context context){
        if(context!=null){
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getActiveNetworkInfo();
            if(info !=null) {
                boolean b = info.isConnected();
                if (b) {
                    return info.getState() == NetworkInfo.State.CONNECTED;
                }
            }
        }
        return false;
    }
}
