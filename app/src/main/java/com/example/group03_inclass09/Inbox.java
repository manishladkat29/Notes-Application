package com.example.group03_inclass09;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Inbox extends AppCompatActivity {

    private TextView tv_userName_id;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager rv_layoutManager;
    private RecyclerView.Adapter rv_adapter;
    private SharedPreferences sharedPreferences;
    private User userInfo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inbox_screen);
        setTitle("My Notes");

        sharedPreferences = getSharedPreferences(getString(R.string.SHARED_PREFERENCES_LOGGED_USER_INFO), MODE_PRIVATE);
        String userInfoListJsonString = sharedPreferences.getString(getString(R.string.SHARED_PREFERENCES_LOGGED_USER_INFO), "");

        Log.d("Demo", userInfoListJsonString);
        // Create Gson object and translate the json string to related java object array.
        Gson gson = new Gson();
        userInfo = gson.fromJson(userInfoListJsonString, User.class);
         //Loop the UserInfoDTO array and print each UserInfoDTO data in android monitor as debug log.
        tv_userName_id = findViewById(R.id.tv_userName_id);
        tv_userName_id.setText(userInfo.user_name);


        Log.d("Test", "BEARER "+userInfo.token);
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://ec2-18-191-172-10.us-east-2.compute.amazonaws.com:3000/api/note/getall")
                .header("x-access-token", userInfo.token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    Log.d("Test", "Hello");
                    //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);



                    String responseLoggedUser = responseBody.string();
                    Log.d("Response", responseLoggedUser);
                    //String responseLoggedUser = responseBody.string();
                    final JSONObject json = new JSONObject(responseLoggedUser);
                    JSONArray jsonArray = json.getJSONArray("notes");
                    if(jsonArray.length() >= 0){
                        Log.d("Text", json.toString());
                        final ArrayList<Messages> messages = new ArrayList<>();
                        if(jsonArray.length() == 0){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Inbox.this, "No Notes added", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        for(int i = 0; i < jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Messages message1 = new Messages(jsonObject.getString("_id"), jsonObject.getString("userId"), jsonObject.getString("text"));
                            messages.add(message1);
                        }
                        //User user1 = new User(jsonObject.getString("token"), jsonObject.getString("user_id"), jsonObject.getString("user_email"), jsonObject.getString("user_fname"), jsonObject.getString("user_lname"), jsonObject.getString("user_role"));

                        // Create Gson object.
                        Gson gson = new Gson();

                        // Get java object list json format string.
                        String messagesList = gson.toJson(messages);

                        // Create SharedPreferences object.
                        Context ctx = getApplicationContext();
                        sharedPreferences = ctx.getSharedPreferences(getResources().getString(R.string.SHARED_PREFERENCES_MESSSAGES), MODE_PRIVATE);

                        // Put the json format string to SharedPreferences object.
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(getResources().getString(R.string.SHARED_PREFERENCES_MESSSAGES), messagesList);
                        editor.commit();
                        // Popup a toast message in screen bottom.



                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                recyclerView = findViewById(R.id.recycler_view);
                                recyclerView.setHasFixedSize(true);
                                rv_layoutManager = new LinearLayoutManager(getApplicationContext());
                                recyclerView.setLayoutManager(rv_layoutManager);
                                rv_adapter = new MessageAdapter(Inbox.this, messages, sharedPreferences.getString(getResources().getString(R.string.SHARED_PREFERENCES_MESSSAGES), null), userInfo);
                                recyclerView.setAdapter(rv_adapter);
                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(Inbox.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.imageButton_compose_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Inbox.this, Compose.class);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.imageButton_logout_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Request request = new Request.Builder()
                        .url("http://ec2-18-191-172-10.us-east-2.compute.amazonaws.com:3000/api/auth/logout")
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        ResponseBody responseBody = response.body();
                        try {
                            final JSONObject jsonObject = new JSONObject(responseBody.string());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if(jsonObject.getString("auth").equals("false")) {
                                            Toast.makeText(Inbox.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(Inbox.this, "Logout Unsuccessful", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            Intent intent = new Intent(Inbox.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
