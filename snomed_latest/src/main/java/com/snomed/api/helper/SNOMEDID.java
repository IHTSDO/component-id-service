package com.snomed.api.helper;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SNOMEDID {
    public static boolean validSchemeId(String id)
    {
        if (null==id || id.isBlank()){
            return false;
        }
        if (id.length()<6 || id.length()>8 || id.indexOf("-")<1 || id.indexOf("-")>2){
            return false;
        }
        return true;
    }

    public static String getNextId(String previousId)
    {
        var prefix=previousId.substring(0,previousId.indexOf("-"));
        var hexBase=previousId.substring(previousId.indexOf("-") + 1);
        Locale locale = Locale.ENGLISH;
        if ((hexBase.toUpperCase().compareTo("FFFFF"))>0) {


        var letter = prefix.substring(0, 1);
        var prefixCount = "";

        int j;
        for (j = 1; j < prefix.length(); j++) {
            prefixCount += "" + prefix.substring(j, 1);
        }
        if (prefixCount==""){
            prefix=letter + "0";
        }else{
            prefix = letter + Integer.toHexString((Integer.parseInt(prefixCount, 16) + 1)).toUpperCase();

        }
        return prefix + "-00000";

    }else {
            Integer newHexBase = (Integer.parseInt(hexBase, 16)) + 1;
            String hexBaseString = "00000"+(Integer.toHexString(newHexBase)).toUpperCase();
            var result = prefix+"-"+hexBaseString.substring(hexBaseString.length()-5);
            return result;
    }
    }
}
