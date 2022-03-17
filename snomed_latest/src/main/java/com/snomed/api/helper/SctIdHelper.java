package com.snomed.api.helper;

import com.sun.jdi.DoubleValue;
import org.springframework.stereotype.Component;

import javax.xml.bind.SchemaOutputResolver;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
@Component
public class SctIdHelper {

    int FnF[][] = new int[][]{{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, {1, 5, 7, 6, 2, 8, 3, 0, 9, 4}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};


    int Dihedral[][] = {{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 }, { 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 }, { 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 }, { 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2},
            { 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 }, { 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 }, { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };

    int InverseD5[] = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };

    public boolean validSCTId(String sctId){

        // var validSCTId=function (sctid){
        String tmp=sctId;
        try{
            var cd = Character.toString((tmp.charAt((tmp.length()) -1)));
            var num = tmp.substring(0,(tmp.length()-1));
            var ret=verhoeffCompute(num);
            return parseInt(cd)==ret;
        }catch (Exception e){
            System.out.println("parser error:" + e);
        }


        return false;
    }

    public int verhoeffCompute(String idAsString)
    {
        int check = 0;
        for (int i = (idAsString.length()- 1); i >= 0; i--) {
            int ct = Character.getNumericValue (idAsString.charAt(i));
            int mod = ((idAsString.length() - i) % 8);
            check = Dihedral[check][FnF[mod][ct]];

        }
        return InverseD5[check];
    }

    // getSequence(Sctid type=?)

    public Float getSequence(String sctid)
    {
        if(sctid!=null){
            if (!validSCTId(sctid)){
                return null;
            }
            String tmp=sctid.toString();
            String partition=getPartition(tmp);
            if (partition.substring(0,1)=="1" ){
                if ( tmp.length()<11){
                    return null;
                }else{
                    return parseFloat(tmp.substring(0,tmp.length()-10));
                }
            }
            var ret=parseFloat(tmp.substring(0,tmp.length()-3));
            return  ret;
        }
        return null;
    }
    //

    // getPartition

    // ? sctid type
    public String getPartition(String sctid){
        if(sctid!= null)
        {
            String tmp=sctid.toString();
            if ( tmp.length()>3){
                String subString = tmp.substring(((tmp.length())-3),(tmp.length()-1));
                return subString;
            }
            return null;
        }
       return null;
    }

    public Integer getCheckDigit(String sctid){
        if(sctid!=null){
            if (!validSCTId(sctid)){
                return null;
            }
            var tmp=sctid.toString();

            return  parseInt(tmp.substring(tmp.length()-1,1));
        }
        return null;
    }

    public Integer getNamespace(String sctid){
        if(sctid!=null){
            if (!validSCTId(sctid)){
                return null;
            }
            String tmp=sctid.toString();
            String partition=getPartition(tmp);
            if (partition.substring(0,1)=="1" ){
                if ( tmp.length()<11){
                    return null;
                }else{
                    return parseInt(tmp.substring(tmp.length()-10,7));
                }
            }
            return 0;
        }
        return null;
    }

    public static String s4()
    {
        Double dubl = (Math.floor((1 + Math.random()) * 0x10000));
        return dubl.toString(16)
                .substring(1);
    }
    public static String guid()
    {
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                s4() + '-' + s4() + s4() + s4();
    }

}
