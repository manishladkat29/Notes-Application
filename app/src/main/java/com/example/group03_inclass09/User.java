package com.example.group03_inclass09;

import java.io.Serializable;

public class User implements Serializable {

    String token, user_id, user_name, user_email;

    public User(String token, String user_id, String user_name, String user_email) {
        this.token = token;
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_email = user_email;
    }


}
