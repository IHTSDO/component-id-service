package org.snomed.cis.util;

import org.springframework.stereotype.Component;

@Component
public class SchemeIdHelper {

    public String getSequence(String id)
    {
        return null;
    }

    public String getCheckDigit(String diffSchemeId) {
        return null;
    }

    /*public  String s4()
    {
        /*Double dubl = (Math.floor((1 + Math.random()) * 0x10000));
        return dubl.toString(16)
                .substring(1);*/
        /*return Integer.toHexString((int)(Math.floor(((1 +Math.random())  * 0x10000)))).substring(1);
    }
    public  String guid()
    {
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                s4() + '-' + s4() + s4() + s4();
    }*/

}
