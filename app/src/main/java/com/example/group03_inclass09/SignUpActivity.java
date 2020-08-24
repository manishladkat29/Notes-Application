package com.example.group03_inclass09;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
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

public class SignUpActivity extends AppCompatActivity {

    private EditText et_email,et_choose_password,et_reapeat_password,et_fname,et_lname;
    private Button btn_signUp,btn_cancel;
    private String password;
    private SharedPreferences sharedPref;
    private JSONObject jsonObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Register");



        et_email=findViewById(R.id.et_email);
        et_choose_password=findViewById(R.id.et_choosePassword);
        et_reapeat_password=findViewById(R.id.et_reapeat_password);
        et_fname=findViewById(R.id.et_firstName);
        btn_signUp =findViewById(R.id.btn_sign_up);
        btn_cancel=findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email= et_email.getText().toString();
                String fname = et_fname.getText().toString();
                String choose_password=et_choose_password.getText().toString();
                String repeat_password=et_reapeat_password.getText().toString();
                if(email.equals("") ||fname.equals("")||choose_password.equals("")||repeat_password.equals("")){
                    Toast.makeText(SignUpActivity.this, "Fields can not be empty", Toast.LENGTH_SHORT).show();
                }else{
                    if(!choose_password.equals(repeat_password)){
                        Toast.makeText(SignUpActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
                        et_reapeat_password.setError("Password does not match");
                    }else{
                        password = et_choose_password.getText().toString();
                        Log.d("demo","email: "+ et_email.getText().toString());
                        Log.d("demo","password: "+ password);
                        Log.d("demo","fname: "+ et_fname.getText().toString());

                        RequestBody formBody = new FormBody.Builder()
                                .add("name", et_fname.getText().toString())
                                .add("email", et_email.getText().toString())
                                .add("password", et_choose_password.getText().toString())
                                .build();
                        Log.d("demo","formBody generated"+formBody.toString());

                        Request requestSignUp = new Request.Builder()
                                .url("http://ec2-18-191-172-10.us-east-2.compute.amazonaws.com:3000/api/auth/register")
                                .post(formBody)
                                .build();

                        final OkHttpClient client_signUp = new OkHttpClient();
                        client_signUp.newCall(requestSignUp).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                Log.d("demo","Failure occured while requst enqueue");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Log.d("demo","response message: "+response.message());
                                String responseBody = response.body().string();
                                Log.d("demo","response body: "+responseBody);

                                try {
                                     jsonObject = new JSONObject(responseBody);
                                    String status=jsonObject.getString("auth");
                                    Log.d("demo","status: "+status);

                                    if(status.equals("true")){
                                        //TODO store token in shared preferences
                                        final String token = jsonObject.getString("token");

                                        Request request = new Request.Builder()
                                                .url("http://ec2-18-191-172-10.us-east-2.compute.amazonaws.com:3000/api/auth/me")
                                                .header("x-access-token", token)
                                                .build();
                                        final OkHttpClient client = new OkHttpClient();
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
                                                            Intent intent = new Intent(SignUpActivity.this, Inbox.class);
                                                            startActivity(intent);
                                                            finish();
                                                            Toast.makeText(SignUpActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }else{
                                        //display error message received from request
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(SignUpActivity.this,"Put a valid username, email, and password.",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                } catch (JSONException e) {
                                    Log.d("demo","JSONException received: "+e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
