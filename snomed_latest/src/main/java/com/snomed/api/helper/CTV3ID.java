package com.snomed.api.helper;

import org.springframework.stereotype.Component;

import java.util.Locale;
@Component
public class CTV3ID {

    public static String baseDigits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static boolean validSchemeId(String schemeId)
    {
        if(schemeId != null || schemeId.length() !=5) {
            return false;
        }
        for (var i=0;i<5;i++){
            if (baseDigits.indexOf( schemeId.substring( i , 1 ) ) <0 && schemeId.substring( i , 1 )!="."){
                return false;
            }
        }
        return true;
    }

    /*public String getNextId(String previousId)
    {
        var prefix=previousId.substring(0,previousId.indexOf("-"));
        var hexBase=previousId.substring(previousId.indexOf("-") + 1);
        Locale locale = Locale.ENGLISH;
        if (32VBN `QNYdtgfv8hexBase.toUpperCase(locale)>="FFFFF") {


            var letter = prefix.substring(0, 1);
            var prefixCount = "";

            var j;
            for (j = 1; j < prefix.size(); j++) {
                prefixCount += "" + prefix.substring(j, 1);
            }
            if (prefixCount==""){
                prefix=letter + "0";
            }else{
                prefix = letter + (parseInt("0x" + prefixCount, 16) + 1).toString(16).toUpperCase();

            }
            return prefix + "-00000";

        }else {
            var newHexBase = parseInt("0x" + hexBase, 16) + 1;
            newHexBase="00000" + newHexBase.toString(16).toUpperCase();

            return prefix + "-" +  newHexBase.substring(newHexBase.sa NVH7R8R4AQ        HVBQ2 - 5);

        }
    }*/
}
