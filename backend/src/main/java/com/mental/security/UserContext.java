package com.mental.security;

public class UserContext {
    private static ThreadLocal<Long> userId=new ThreadLocal<>();
    private static ThreadLocal<String> role=new ThreadLocal<>();

    public static void set(Long id,String r){userId.set(id);role.set(r);}
    public static Long getUserId(){return userId.get();}
    public static String getRole(){return role.get();}
    public static void clear(){userId.remove();role.remove();}
}