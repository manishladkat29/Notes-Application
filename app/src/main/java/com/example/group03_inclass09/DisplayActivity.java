package com.example.group03_inclass09;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DisplayActivity extends AppCompatActivity {

    private TextView tv_sendername_id, tv_subject_display_id, et_message_body_id, tv_createdAt_display_id;
    private Button btn_close_id, btn_delete_id;
    private Messages messages;
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_activity);
        setTitle("Display Note");

        tv_subject_display_id = findViewById(R.id.tv_subject_display_id);
        et_message_body_id = findViewById(R.id.et_message_body_id);
        btn_close_id = findViewById(R.id.btn_close_id);
        et_message_body_id.setFocusable(false);
        btn_delete_id = findViewById(R.id.btn_delete_id);

        messages = (Messages) getIntent().getSerializableExtra("Message");
        user = (User) getIntent().getSerializableExtra("User");
        Log.d("Message", messages.toString());
        tv_subject_display_id.setText(messages.text);

        btn_close_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(DisplayActivity.this, Inbox.class);
                startActivity(intent);
                finish();
            }
        });

        btn_delete_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final AlertDialog.Builder builder = new AlertDialog.Builder(DisplayActivity.this);
                builder.setMessage("Do you want to delete the note")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                    // FIRE ZE MISSILES!

                                OkHttpClient client = new OkHttpClient();

                                RequestBody formBody = new FormBody.Builder()
                                        .add("id", messages.messageId)
                                        .build();

                                Request request = new Request.Builder()
                                        .url("http://ec2-18-191-172-10.us-east-2.compute.amazonaws.com:3000/api/note/delete")
                                        .header("x-access-token", user.token)
                                        .post(formBody)
                                        .build();

                                client.newCall(request).enqueue(new Callback() {
                                    @Override public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override public void onResponse(Call call, Response response) throws IOException {
                                        try (ResponseBody responseBody = response.body()) {
                                            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                                            String responseString = responseBody.string();
                                            JSONObject jsonObject = new JSONObject(responseString);
                                            if(jsonObject.getString("delete").equals("true")){
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(DisplayActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(DisplayActivity.this, Inbox.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                            }else{
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(DisplayActivity.this, "Note Deletion Failed", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });

                builder.create().show();
            }
        });


    }
}
