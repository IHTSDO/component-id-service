package org.snomed.cis.util;

import org.springframework.stereotype.Component;

@Component
public class CTV3ID {

    static final String baseDigits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static boolean validSchemeId(String schemeId) {
        if (schemeId == null || schemeId.isEmpty() || schemeId.isBlank() || schemeId.length() != 5) {
            return false;
        }
        for (int i = 0; i < 5; i++) {
            String schemeIdSubStr = schemeId.substring(i,i+1);
            if(!baseDigits.contains(schemeIdSubStr) && !schemeIdSubStr.equals(".")){
                return false;
            }
        }
        return true;
    }

    public static String getNextId(String previousId) {
        var iterator = previousId.length();
        var decimalValue = 0;
        var multiplier = 1;
        for (int i = 0; i < previousId.length(); i++) {
            decimalValue = decimalValue + (baseDigits.indexOf(previousId.substring(iterator - 1, iterator)) * multiplier);
            multiplier = multiplier * 62;
            --iterator;
        }
        decimalValue++;

        var tempVal = decimalValue == 0 ? "0" : "";
        var mod = 0;

        while ((decimalValue) != 0) {
            mod = (decimalValue % 62);
            tempVal = baseDigits.substring(mod, mod + 1) + tempVal;
            decimalValue = decimalValue / 62;
        }
        return tempVal;
    }
}
