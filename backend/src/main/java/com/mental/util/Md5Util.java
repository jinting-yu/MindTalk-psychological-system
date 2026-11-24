package com.mental.util;

import cn.hutool.crypto.digest.DigestUtil;

public class Md5Util {
    public static String md5(String s){
        return DigestUtil.md5Hex(s);
    }
}