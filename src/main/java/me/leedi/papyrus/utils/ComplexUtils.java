package me.leedi.papyrus.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class ComplexUtils {
    static String statuscode;
    static boolean NeedRefresh = false;

    /**
     * 애플리케이션 버전정보 가져오는 메소드
     *
     * @param context (Context)
     * @return 버전정보
     */

    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * API 에서 가져온 데이터(JSON)을 가공하는 메소드
     *
     * @param json (JSON)
     * @return 파싱된 값
     */

    public static JSONObject JSONParse(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject status = jsonObject.getJSONObject("status");
        String code = status.getString("code");
        String msg = status.getString("message");
        JSONObject data = jsonObject.getJSONObject("data");
        
        statuscode = code;

        Log.d("API Status Code : ", code);
        Log.d("API Status Message : ", msg);
        return data;
    }
    
    public static String getStatusCode() {
        return statuscode;
    }
    
    public static boolean isNeedRefresh() {
        return NeedRefresh;
    }
    
    public static void setNeedRefresh(Boolean set) {
        NeedRefresh = set;
    }
}