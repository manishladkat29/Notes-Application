package com.example.group03_inclass09;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private EditText et_email_id, et_password_id;
    private Button button_login_id, button_signup_id;
    private JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Login");

        et_email_id = findViewById(R.id.et_email_id);
        et_password_id = findViewById(R.id.et_password_id);
        button_login_id = findViewById(R.id.button_login_id);
        button_signup_id = findViewById(R.id.button_signup_id);

        findViewById(R.id.button_signup_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.button_login_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!et_email_id.getText().toString().equals("") && !et_password_id.getText().toString().equals("")){
                    final OkHttpClient client = new OkHttpClient();
                    Log.d("Email", et_email_id.getText().toString());
                    Log.d("Password", et_password_id.getText().toString());
                    RequestBody formBody = new FormBody.Builder()
                            .add("email", et_email_id.getText().toString())
                            .add("password", et_password_id.getText().toString())
                            .build();
                    Request request = new Request.Builder()
                            .url("http://ec2-18-191-172-10.us-east-2.compute.amazonaws.com:3000/api/auth/login")
                            .post(formBody)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override public void onResponse(Call call, Response response) throws IOException {
                            try (ResponseBody responseBody = response.body()) {
                                String responseLoggedUser = responseBody.string();
                                Log.d("Response", responseLoggedUser);
                                if(responseLoggedUser.equals("No user found.")){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "No User found", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }else{
                                    jsonObject = new JSONObject(responseLoggedUser);
                                    if(jsonObject.getString("auth").equals("true")){
                                        final String token = jsonObject.getString("token");

                                        Request request = new Request.Builder()
                                                .url("http://ec2-18-191-172-10.us-east-2.compute.amazonaws.com:3000/api/auth/me")
                                                .header("x-access-token", token)
                                                .build();
                                        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                                e.printStackTrace();
                                            }

                                            @Override
                                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                                try (ResponseBody responseBody = response.body()) {
                                                    String responseLoggedUserDetails = responseBody.string();
                                                    jsonObject = new JSONObject(responseLoggedUserDetails);

                                                    User user1 = new User(token, jsonObject.getString("_id"), jsonObject.getString("name"), jsonObject.getString("email"));
                                                    // Create Gson object.
                                                    Gson gson = new Gson();

                                                    // Get java object list json format string.
                                                    String userInfoListJsonString = gson.toJson(user1);

                                                    // Create SharedPreferences object.
                                                    Context ctx = getApplicationContext();
                                                    SharedPreferences sharedPreferences = ctx.getSharedPreferences(getResources().getString(R.string.SHARED_PREFERENCES_LOGGED_USER_INFO), MODE_PRIVATE);

                                                    // Put the json format string to SharedPreferences object.
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putString(getResources().getString(R.string.SHARED_PREFERENCES_LOGGED_USER_INFO), userInfoListJsonString);
                                                    editor.commit();
                                                    // Popup a toast message in screen bottom.
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Intent intent = new Intent(MainActivity.this, Inbox.class);
                                                            startActivity(intent);
                                                            finish();
                                                            Toast.makeText(MainActivity.this, "Logged In!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                    else{
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else if(et_email_id.getText().toString().equals("") && et_password_id.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "Email Id and Password field blank", Toast.LENGTH_SHORT).show();
                } else if(et_email_id.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "Email Id field blank", Toast.LENGTH_SHORT).show();
                }else if(et_password_id.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "Password field blank", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
