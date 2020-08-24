package com.example.group03_inclass09;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.content.Context.MODE_PRIVATE;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private Context ctx;
    private ArrayList<Messages> messages;
    private String sharedPreferencesMessgae;
    private User user;
    private Messages[] messagesObject;

    public MessageAdapter(Context ctx, ArrayList<Messages> messages, String sharedPreferencesMessgae, User user) {
        this.messages = messages;
        this.ctx = ctx;
        this.sharedPreferencesMessgae = sharedPreferencesMessgae;
        this.user = user;
    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.messages, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.tv_subject_id.setText(messages.get(position).text);

        Log.d("Demo", sharedPreferencesMessgae);
        // Create Gson object and translate the json string to related java object array.
        Gson gson = new Gson();
        messagesObject = gson.fromJson(sharedPreferencesMessgae, Messages[].class);

        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setMessage("Do you want to delete the note")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!

                                OkHttpClient client = new OkHttpClient();

                                RequestBody formBody = new FormBody.Builder()
                                        .add("id", messagesObject[position].messageId)
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
                                                Intent intent = new Intent(ctx, Inbox.class);
                                                ctx.startActivity(intent);
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
                return true;
            }
        });


        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, DisplayActivity.class);
                intent.putExtra("Message", messagesObject[position]);
                intent.putExtra("User", user);
                ctx.startActivity(intent);
                ((AppCompatActivity)ctx).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_subject_id;
        LinearLayout linearLayout;
        public MyViewHolder(View itemView) {
            super(itemView);

            tv_subject_id = itemView.findViewById(R.id.tv_subject_id);
            linearLayout = itemView.findViewById(R.id.rv_message_id);
        }
    }
}
