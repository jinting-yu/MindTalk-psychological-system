package com.mental.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MentalAIFloatUtil {

    @Autowired
    private DeepSeekUtil deepSeekUtil;

    public String chat(String msg){
        return deepSeekUtil.chat(msg);
    }
}