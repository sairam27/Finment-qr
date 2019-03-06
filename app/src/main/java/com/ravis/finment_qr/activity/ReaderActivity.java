package com.ravis.finment_qr.activity;


import com.ravis.finment_qr.MainActivity;
import com.ravis.finment_qr.app.Utils;
import  com.ravis.finment_qr.app.WsConfig;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;


import java.net.URI;
import java.net.URLEncoder;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


import android.support.v7.app.AppCompatActivity;


import com.ravis.finment_qr.R;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ravis.finment_qr.helper.SQLiteHandler;


import java.util.HashMap;


public class ReaderActivity extends AppCompatActivity {
    private Button scan_btn;
    private static final String TAG = MainActivity.class.getSimpleName();
    private WebSocketClient client;
    private Utils utils;
    private String name = null;
    private SQLiteHandler db;
    private String email,password,category,fid;
    private WebSocketClient mWebSocketClient;

    private static final String TAG_SELF = "self", TAG_NEW = "new",
            TAG_MESSAGE = "message", TAG_EXIT = "exit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        scan_btn = (Button) findViewById(R.id.scan_btn);

        db = new SQLiteHandler(this.getApplicationContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        email = user.get("email");
        password = user.get("password");
        category = user.get("category");
        fid = user.get("fid");

        connectWebSocket();


        final Activity activity = this;
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();


            }
        });
    }
    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://52.66.108.224:9000/Finment/server.php");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                Toast.makeText(getApplicationContext(), "connected! and open", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onMessage(final String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       try{
                           JSONObject jsonObj = new JSONObject(s);
                           Toast.makeText(getApplicationContext(),jsonObj.getString("message") , Toast.LENGTH_LONG).show();
                       }catch(final JSONException e){
                           Toast.makeText(getApplicationContext(),e.getMessage() , Toast.LENGTH_LONG).show();
                       }
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }


    public void sendMessage(String json) {
        mWebSocketClient.send(json.toString());

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null){

                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }
            else {
                    String qrcode = result.getContents();
                    Toast.makeText(this, qrcode, Toast.LENGTH_LONG).show();
                    String json = getSendMessageJSON(qrcode, email, password, category, fid);
                    Toast.makeText(this, json, Toast.LENGTH_LONG).show();
                    sendMessage(json);

            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
