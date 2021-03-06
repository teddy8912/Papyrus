package me.leedi.papyrus.utils;

import android.content.Context;
import android.content.SharedPreferences;
import me.leedi.papyrus.R;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static me.leedi.papyrus.utils.ComplexUtils.*;

public class ServerUtils {
    public static String StatusMsg;
    /**
     * Papyrus 로그인 메소드
     *
     * @param userId (소셜 네트워크 ID)
     * @param userName (소셜 네트워크 계정이름)
     * @param context (Context)
     */

    public static boolean login(String userId, String userName, Context context) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("userId", userId));
        params.add(new BasicNameValuePair("userName", userName));
        String res = doPost("/user/login", params, null, context);
        if (!isTimeout(res, context)) {
            try {
                JSONObject json = JSONParse(res);
                String userToken = json.getString("userToken");
                String returnName = json.getString("userName");
                context.getSharedPreferences("common", Context.MODE_PRIVATE)
                        .edit().putString("userToken", userToken).putString("userName", returnName).putString("userId", userId).putBoolean("isLogin", true).apply();
                SecurityUtils.setKeyHash(context);
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    
    /**
     * Papyrus 프로필(유저이름만) 업데이트 메소드
     *
     * @param userName (소셜 네트워크 계정이름)
     * @param context (Context)
     * @return 프로필 업데이트 성공여부
     */

    public boolean update(String userName, Context context) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("userName", userName));
        String userToken = context.getSharedPreferences("common", Context.MODE_PRIVATE).getString("userToken", null);
        String res = doPost("/user/update", params, userToken, context);
        if (!isTimeout(res, context)) {
            try {
                JSONParse(res);
                if (!isError(context)) {
                    context.getSharedPreferences("common", Context.MODE_PRIVATE)
                            .edit().putString("userName", userName).apply();
                    return true;
                }
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }  
        }
        return false;
    }


    /**
     * Papyrus 계정삭제 메소드
     *
     * @param context (Context)
     * @return 프로필 업데이트 성공여부
     */

    public boolean unregister(Context context) {
        SharedPreferences pref = context.getSharedPreferences("common", Context.MODE_PRIVATE);
        String userId = pref.getString("userId", null);
        String userToken = pref.getString("userToken", null);
        String res = doDelete("/user/" + userId, userToken, context);
        if (!isTimeout(res, context)) {
            try {
                JSONParse(res);
                if (!isError(context)) {
                    pref.edit().clear().apply();
                    return true;
                }
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }  
        }
        return false;
    }

    /**
     * Papyrus 목록 가져오는 메소드
     *
     * @param userId (소셜 네트워크 ID)
     * @param date (날짜)
     * @param context (Context)
     */

    public static JSONArray papyrusGet(String userId, String date, Context context) {
        String res = doGet("/papyrus/" + userId + "?date=" + date, null, context);
        if (!isTimeout(res, context)) {
            try {
                JSONObject jsonObject = new JSONObject(res);
                return jsonObject.getJSONArray("data");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * Papyrus 작성 메소드
     *
     * @param userId (소셜 네트워크 ID)
     * @param title (당연히 제목)
     * @param content (당연히 내용)
     * @param date (날짜)
     * @param context (Context)
     * @return contentId (콘텐츠 ID, 차기 SQLite 이용시 사용을 위함)                
     */
    
    public static String papyrusNew(String userId, String title, String content, String date, Context context) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("userId", userId));
        params.add(new BasicNameValuePair("title", title));
        params.add(new BasicNameValuePair("content", content));
        params.add(new BasicNameValuePair("date", date));
        params.add(new BasicNameValuePair("attachId", "")); // TODO : 차후 첨부파일 기능 구현 시 수정바람 (현재는 DB 입력오류 방지용)
        String userToken = context.getSharedPreferences("common", Context.MODE_PRIVATE).getString("userToken", null);
        String res = doPost("/papyrus", params, userToken, context);
        if (!isTimeout(res, context)) {
            try {
                JSONObject json = JSONParse(res);
                return json.getString("contentId");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * Papyrus 목록 가져오는 메소드
     *
     * @param userId (소셜 네트워크 ID)
     * @param contentId (컨텐츠 ID)
     * @param context (Context)
     */

    public static JSONObject papyrusDetail(String userId, String contentId, Context context) {
        String res = doGet("/papyrus/" + userId + "/diary/" + contentId, null, context);
        if (!isTimeout(res, context)) {
            try {
                return JSONParse(res);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


    /**
     * API 타임아웃 여부 체크 메소드
     * 
     * @param response (응답값)
     * @param context (Context)                 
     * @return 타임아웃 여부                 
     */
    
    public static boolean isTimeout(String response, Context context) {
        if(response.equals("Timeout")) {
            StatusMsg = context.getString(R.string.alert_timeout);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * API 자체오류 체크 메소드
     *
     * @param context (Context)
     * @return 오류여부                
     */
    
    public boolean isError(Context context) {
        String statusCode = getStatusCode();
        if(!statusCode.equals("200")) {
            if (statusCode.equals("404")) {
                StatusMsg = context.getString(R.string.alert_forbidden);
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * API 에 GET 명령어로 접근하는 메소드
     *
     * @param url (URL + 질의문)
     * @param token (토큰)
     * @param context (Context)
     * @return 결과값
     */

    public static String doGet(String url, String token, Context context) {
        String res = null;
        HttpClient http = new DefaultHttpClient();
        try {
            HttpParams param = http.getParams();
            HttpConnectionParams.setConnectionTimeout(param, 3000);
            HttpConnectionParams.setSoTimeout(param, 3000);

            HttpGet request = new HttpGet("http://api.leedi.me/papyrus" + url);
            request.setHeader("User-Agent", "Papyrus App/" + getVersionName(context) + " Android/" + android.os.Build.VERSION.RELEASE);
            request.setHeader("token", token);

            HttpResponse response = http.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                res = EntityUtils.toString(entity, "UTF-8");
                res = res.replace("\\\\n", "");
                res = res.replace("\\", "");
                res = res.substring(1, res.length() -1);
            }
            return res;
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            return "Timeout";
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * API 에 POST 명령어로 접근하는 메소드
     *
     * @param url (URL + 질의문)
     * @param token (토큰)
     * @param context (Context)
     * @return 결과값
     */

    public static String doPost(String url, ArrayList<NameValuePair> params, String token, Context context) {
        String res = null;
        HttpClient http = new DefaultHttpClient();
        try {
            HttpParams param = http.getParams();
            HttpConnectionParams.setConnectionTimeout(param, 3000);
            HttpConnectionParams.setSoTimeout(param, 3000);

            HttpPost request = new HttpPost("http://api.leedi.me/papyrus" + url);
            request.setHeader("User-Agent", "Papyrus App/" + getVersionName(context) + " Android/" + android.os.Build.VERSION.RELEASE);
            request.setHeader("token", token);

            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            request.setEntity(formEntity);
            
            HttpResponse response = http.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                res = EntityUtils.toString(entity, "UTF-8");
                res = res.replace("\\\\n", "");
                res = res.replace("\\", "");
                res = res.substring(1, res.length() -1);
            }
            return res;
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            return "Timeout";
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * API 에 POST 명령어로 접근하는 메소드
     *
     * @param url (URL + 질의문)
     * @param token (토큰)
     * @param context (Context)
     * @return 결과값
     */

    public static String doDelete(String url, String token, Context context) {
        String res = null;
        HttpClient http = new DefaultHttpClient();
        try {
            HttpParams param = http.getParams();
            HttpConnectionParams.setConnectionTimeout(param, 3000);
            HttpConnectionParams.setSoTimeout(param, 3000);

            HttpDelete request = new HttpDelete("http://api.leedi.me/papyrus" + url);
            request.setHeader("User-Agent", "Papyrus App/" + getVersionName(context) + " Android/" + android.os.Build.VERSION.RELEASE);
            request.setHeader("token", token);

            HttpResponse response = http.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                res = EntityUtils.toString(entity, "UTF-8");
                res = res.replace("\\\\n", "");
                res = res.replace("\\", "");
                res = res.substring(1, res.length() -1);
            }
            return res;
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            return "Timeout";
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}