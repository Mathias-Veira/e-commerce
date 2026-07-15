package com.project.e_commerce.domain;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    @Setter(AccessLevel.NONE)
    private int userId;
    @NonNull
    private String userEmail;
    @NonNull
    private String userName;
    @NonNull
    private String userPassword;

    public static boolean validateEmail(String userEmail){
        if(userEmail == null) return false;
        return userEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
    public static boolean validateUsername(String userName){
        if(userName == null) return false;
        userName = userName.trim();
        return userName.matches("^[a-zA-Z0-9_-]{3,15}$");
    }

    public static boolean validatePassword(String userPassword){
        if(userPassword == null) return false;
        if(userPassword.length()<8 || userPassword.length()>20){
            return false;
        }

        return true;
    }

}
