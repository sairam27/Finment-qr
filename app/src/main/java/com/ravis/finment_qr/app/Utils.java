package com.ravis.finment_qr.app;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Utils {

    private Context context;
    private SharedPreferences sharedPref;

    private static final String KEY_SHARED_PREF = "ANDROID_WEB_CHAT";
    private static final int KEY_MODE_PRIVATE = 0;
    private static final String KEY_SESSION_ID = "sessionId",
            FLAG_MESSAGE = "message";

    public Utils(Context context) {
        this.context = context;
        sharedPref = this.context.getSharedPreferences(KEY_SHARED_PREF,
                KEY_MODE_PRIVATE);
    }

    public void storeSessionId(String sessionId) {
        Editor editor = sharedPref.edit();
        editor.putString(KEY_SESSION_ID, sessionId);
        editor.commit();
    }

    public String getSessionId() {
        return sharedPref.getString(KEY_SESSION_ID, null);
    }

    public String getSendMessageJSON(String qrcode,String email,String password,String category,String fid ) {
        String json = null;

        try {
            JSONObject jObj = new JSONObject();

            jObj.put("message", qrcode);
            jObj.put("email", email);
            jObj.put("password", password);
            jObj.put("category", category);
            jObj.put("fid", fid);

            json = jObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

}