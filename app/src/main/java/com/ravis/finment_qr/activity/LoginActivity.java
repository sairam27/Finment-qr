package com.ravis.finment_qr.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.ravis.finment_qr.MainActivity;
import com.ravis.finment_qr.R;
import com.ravis.finment_qr.app.AppConfig;
import com.ravis.finment_qr.app.AppController;
import com.ravis.finment_qr.helper.SQLiteHandler;
import com.ravis.finment_qr.helper.SessionManager;

import static android.content.ContentValues.TAG;

public class LoginActivity extends Activity {

    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private EditText fid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        final Spinner myspinner = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<String> myadapter = new ArrayAdapter<String>(LoginActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.category));
        myadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        myspinner.setAdapter(myadapter);
        fid= (EditText) findViewById(R.id.fid);
        fid.setVisibility(View.GONE);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
           myspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
               @Override
               public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                   if(myspinner.getSelectedItem().toString().trim().equals(("Client"))){
                       fid.setVisibility(View.VISIBLE);
                   }else if(myspinner.getSelectedItem().toString().trim().equals(("Investor"))){
                       fid.setVisibility(View.VISIBLE);
                   }else{
                       fid.setVisibility(View.GONE);
                   }
               }

               @Override
               public void onNothingSelected(AdapterView<?> parent) {
                   fid.setVisibility(View.GONE);
               }
           });


        if(myspinner.getSelectedItem().toString().trim().equals(("Client"))){
            fid.setVisibility(View.VISIBLE);
        }else if(myspinner.getSelectedItem().toString().trim().equals(("Investor"))){
            fid.setVisibility(View.VISIBLE);
        }else{
            fid.setVisibility(View.GONE);
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String category = myspinner.getSelectedItem().toString().trim();
                String fid1 = fid.getText().toString().trim();
                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password, category, fid1);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });
    }

    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String email, final String password, final  String category, final String fid1) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean success = jObj.getBoolean("success");

                    // Check for error node in json
                    if (success) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        String uid = jObj.getString("uid");
                        String fid = jObj.getString("fid");
                        String fname =jObj.getString("fname");
                        String email =jObj.getString("email");
                        String created_at = jObj.getString("created_at");
                        String mobile = jObj.getString("mobile");

                        // Inserting row in users table
                        db.addUser(fname, email, uid, created_at, password, category, fid , mobile);

                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("lemail", email);
                params.put("lpassword", password);
                params.put("category",category);
                params.put("fid",fid1);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}