package org.snomed.cis.helper;

import org.springframework.stereotype.Component;

import java.util.Locale;
@Component
public class CTV3ID {

    static String baseDigits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

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

    public static String getNextId(String previousId)
    {
        var iterator = previousId.length();
        var decimalValue = 0;
        var multiplier = 1;
        for (int i = 0; i < previousId.length(); i++) {
            decimalValue = decimalValue + ( baseDigits.indexOf( previousId.substring( iterator - 1, iterator ) ) * multiplier );
            multiplier = multiplier * 62;
            --iterator;
        }
        decimalValue++;

        var tempVal = decimalValue == 0 ? "0" : "";
        var mod = 0;

        while( (decimalValue) != 0 ) {
            mod = (decimalValue % 62);
            tempVal = baseDigits.substring( mod, mod + 1 ) + tempVal;
            decimalValue = decimalValue / 62;
        }
        return tempVal;
    }
}
