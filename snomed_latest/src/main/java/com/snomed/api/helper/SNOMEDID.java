package com.snomed.api.helper;

import org.springframework.stereotype.Component;

@Component
public class SNOMEDID {
    public static boolean validSchemeId(String id)
    {
        if (id!=null){
            return false;
        }
        if (id.length()<6 || id.length()>8 || id.indexOf("-")<1 || id.indexOf("-")>2){
            return false;
        }
        return true;
    }
}
