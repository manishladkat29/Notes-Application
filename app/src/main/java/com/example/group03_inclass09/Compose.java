package com.example.group03_inclass09;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Compose extends AppCompatActivity {
    private Spinner spinner_user_id;
    private EditText et_subject_id, et_messagebody_id;
    private Button btn_send_id, btn_cancel_id;
    private ArrayAdapter<String> stringArrayAdapter;
    private final ArrayList<User> userArrayList = new ArrayList<>();
    private final ArrayList<String> userName = new ArrayList<>();
    private User userInfo;
    private JSONObject jsonObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compose);
        setTitle("Add a Note");

        et_subject_id = findViewById(R.id.et_subject_id);
        et_messagebody_id = findViewById(R.id.et_messagebody_id);
        btn_send_id = findViewById(R.id.btn_send_id);
        btn_cancel_id = findViewById(R.id.btn_cancel_id);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.SHARED_PREFERENCES_LOGGED_USER_INFO), MODE_PRIVATE);
        String userInfoListJsonString = sharedPreferences.getString(getString(R.string.SHARED_PREFERENCES_LOGGED_USER_INFO), "");

        Log.d("Demo", userInfoListJsonString);
        // Create Gson object and translate the json string to related java object array.
        Gson gson = new Gson();
        userInfo = gson.fromJson(userInfoListJsonString, User.class);
        btn_cancel_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Compose.this, Inbox.class);
                startActivity(intent);
                finish();
            }
        });

        btn_send_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = et_subject_id.getText().toString();

                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormBody.Builder()
                        .add("text", subject)
                        .build();
                Request request = new Request.Builder()
                        .url("http://ec2-18-191-172-10.us-east-2.compute.amazonaws.com:3000/api/note/post")
                        .header("x-access-token", userInfo.token)
                        .post(formBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override public void onResponse(Call call, Response response) throws IOException {
                        try (ResponseBody responseBody = response.body()) {
                            //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                            String responseLoggedUser = responseBody.string();
                            Log.d("Response", responseLoggedUser);
                            //String responseLoggedUser = responseBody.string();
                            jsonObject = new JSONObject(responseLoggedUser);
                            if(jsonObject.getString("posted").equals("true")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(Compose.this, "Note Added", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Compose.this, Inbox.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Toast.makeText(Compose.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
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
            }
        });
    }
}
