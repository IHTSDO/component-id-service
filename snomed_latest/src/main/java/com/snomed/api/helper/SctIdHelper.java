package com.snomed.api.helper;

import com.snomed.api.controller.dto.CheckSctidResponseDTO;
import com.snomed.api.domain.Namespace;
import com.snomed.api.domain.Sctid;
import com.snomed.api.exception.APIException;
import com.snomed.api.repository.NamespaceRepository;
import com.snomed.api.repository.SctidRepository;
import com.sun.jdi.DoubleValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.xml.bind.SchemaOutputResolver;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
@Component
public class SctIdHelper {

    private static NamespaceRepository namespaceRepository;

    @Autowired
    SctidRepository sctidRepository;

    @Autowired
    ModelsConstants modelsConstants;

    @Autowired
    public SctIdHelper(NamespaceRepository namespaceRepository)
    {
        SctIdHelper.namespaceRepository = namespaceRepository;
    }

    public static void init(){

        for (var i = 2; i < 8; i++) {
            for (var j = 0; j < 10; j++) {
                FnF[i][j] = FnF[i - 1][FnF[1][j]];
            }
        }
    };

    //int[][]FnFNew ={};

    static int[][] FnF = {{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, {1, 5, 7, 6, 2, 8, 3, 0, 9, 4}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};


    static int[][] Dihedral = {{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 }, { 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 }, { 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 }, { 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2},
            { 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 }, { 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 }, { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };

    static int[] InverseD5 = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };

    public static boolean validSCTId(String sctId){
        // var validSCTId=function (sctid){
        init();
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

    public static int verhoeffCompute(String idAsString)
    {
        int check = 0;
        for (int i = (idAsString.length()- 1); i >= 0; i--) {
            int ct = Character.getNumericValue (idAsString.charAt(i));
            int mod = ((idAsString.length() - i) % 8);
            //FnF[mod][ct] = FnF[mod - 1][FnF[1][ct]];
            check = Dihedral[check][FnF[mod][ct]];

        }
        return InverseD5[check];
    }

    // getSequence(Sctid type=?)

    public Long getSequence(String sctid)
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
                    return Long.valueOf(tmp.substring(0,tmp.length()-10));
                }
            }
            var ret=Long.valueOf(tmp.substring(0,tmp.length()-3));
            return  ret;
        }
        return null;
    }
    //

    // getPartition

    // ? sctid type
    public static String getPartition(String sctid){
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

            return  parseInt(tmp.substring(tmp.length()-1));
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
            if (partition.substring(0,1).equalsIgnoreCase("1") ){
                if ( tmp.length()<11){
                    return null;
                }else{
                    return parseInt(tmp.substring(tmp.length()-10,((tmp.length()-10)+7)));
                }
            }
            return 0;
        }
        return null;
    }

    public static String s4()
    {
        /*Double dubl = (Math.floor((1 + Math.random()) * 0x10000));
        return dubl.toHexString(1.0)
                .substring(1);*/
        return Integer.toHexString((int)(Math.floor(((1 +Math.random())  * 0x10000)))).substring(1);
    }
    public static String guid()
    {
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                s4() + '-' + s4() + s4() + s4();
    }

    public static CheckSctidResponseDTO checkSctid(String sctid) throws APIException {
        String err = "";

        String partitionId = "";
        Integer checkDigit = null;
        Long sequence = null;
        Integer namespaceId  = null;
        String isValid = "true";
        String comment = "";

        //if (sctid == null) {
        if(sctid.equals(null)) {
            err = "SctId is not a number.";
            isValid = "false";

        } else if (Pattern.matches("\\D",sctid)) {
        err = "SctId is not a number.";
        isValid = "false";

    } else if (sctid.length() < 6) {
        err = "SctId length is less than 6 digits.";
        isValid = "false";
    } else if (sctid.length() > 18) {
        err = "SctId length is greater than 18 digits.";
        isValid = "false";
    }

       // if (isValid == "true") {
        if(isValid.equals("true")) {
            if (!validSCTId(sctid)) {
                err = "SctId is not valid.";
                isValid = "false";
            }

            try {
                partitionId = getPartition(sctid);
            } catch (Exception e) {
                err += " PartitionId error:" + e.getMessage();
            }

            var partitionCtrl = true;
            //if (partitionId != "") {
            if(!partitionId.equals("")){
                if (!partitionId.equalsIgnoreCase("00")
                        && !partitionId.equalsIgnoreCase("01")
                        && !partitionId.equalsIgnoreCase( "02")
                        && !partitionId.equalsIgnoreCase( "10")
                        && !partitionId.equalsIgnoreCase( "11")
                        && !partitionId.equalsIgnoreCase( "12")
                        && !partitionId.equalsIgnoreCase( "03")
                        && !partitionId.equalsIgnoreCase( "13")
                        && !partitionId.equalsIgnoreCase("04")
                        && !partitionId.equalsIgnoreCase( "14")
                        && !partitionId.equalsIgnoreCase( "05")
                        && !partitionId.equalsIgnoreCase("15")) {

                    err += " Partition Id " + partitionId + " is not valid.";
                    isValid = "false";
                    partitionCtrl = false;
                }
            }

            var tmp ="";
            try {
                tmp = sctid.toString();
                //tmp.substring(tmp.length() - 1);

                checkDigit = Integer.parseInt((tmp.substring(tmp.length() - 1)));
            } catch (Exception e) {
                err += " Check digit error:" + e.getMessage();
            }

            if (isValid == "false" && partitionCtrl && checkDigit != null) {
                try {
                    var id = sctid.substring(0, sctid.length() - 1);
                    var cd = verhoeffCompute(id);
                    if (cd != checkDigit) {
                        err += " Check digit should be " + cd + ".";
                    }
                } catch (Exception e) {
                    err += " Check digit error:" + e.getMessage();
                }

            }
            try {
                tmp = sctid.toString();
                if (partitionId.substring(0, 1) == "1") {
                    if (tmp.length() > 10) {
                        sequence = Long.valueOf(tmp.substring(0, tmp.length() - 10));
                    }
                } else {
                    sequence = Long.valueOf(tmp.substring(0, tmp.length() - 3));
                }
            } catch (Exception e) {
                err += " Sequence error:" + e.getMessage();
            }
            try {
                tmp = sctid.toString();
                if (partitionId.substring(0, 1) == "1") {
                    if (tmp.length() > 10) {
                        namespaceId = parseInt(tmp.substring(tmp.length() - 10, 7));
                    } else {
                        err += " PartitionId first digit is '1', it identifies an extension SCTID, " +
                                "but no namespace could be identified";
                    }
                } else {
                    namespaceId = 0;
                }

            } catch (Exception e) {
                err += " Namespace error:" + e.getMessage();
            }
            if (null!=namespaceId && partitionCtrl) {
                //if (partitionId.substring(0, 1) == "1" && namespaceId == 0) {
                if(partitionId.substring(0,1) == "1" && (namespaceId.equals(0))) {
                    isValid = "false";
                    err += " PartitionId first digit is '1', it identifies an extension SCTID, " +
                            "but no namespace could be identified";
                }
            }
          //  if (sequence == null
            if(sequence.equals(null)
                    || namespaceId.equals(null)
                    || checkDigit.equals(null)
                    || partitionId.equals(null)
                    || partitionId.equals("")) {
                isValid = "false";

            }

            if (isValid == "true") {
                if (namespaceId == 0) {
                    comment = "Core ";
                } else {
                    comment = "Extension ";
                }
                if (null!=partitionId && partitionId != "") {
                    var art = partitionId.substring(1);
                    if (art.equalsIgnoreCase("0")) {
                        comment += "concept Id";
                    } else if (art.equalsIgnoreCase("1")) {
                        comment += "description Id";
                    } else if (art.equalsIgnoreCase("2")) {
                        comment += "relationship Id";
                    } else if (art.equalsIgnoreCase("3")) {
                        comment += "subset Id (RF1)";
                    } else if (art.equalsIgnoreCase("4")) {
                        comment += "mapset Id (RF1)";
                    } else if (art.equalsIgnoreCase("5")) {
                        comment += "target Id (RF1)";
                    } else {
                        comment += "artifact";
                    }
                }
            }
        }
        CheckSctidResponseDTO checkResp = new CheckSctidResponseDTO();
        checkResp.setSctid(sctid);
        checkResp.setSequence(sequence);
        checkResp.setNamespace(namespaceId);
        checkResp.setPartitionID(partitionId);
        checkResp.setComponentType(comment);
        checkResp.setCheckDigit(checkDigit);
        checkResp.setIsSCTIDValid(isValid);
        checkResp.setErrorMessage(err);
        checkResp.setNamespaceOrganization("");
        checkResp.setNamespaceContactEmail("");

        if (isValid == "true" && namespaceId != null) {
            Optional<Namespace> namespaceObj = namespaceRepository.findById(namespaceId);
           if(null!=namespaceObj) {
               checkResp.setNamespaceOrganization(namespaceObj.get().getOrganizationName());
               checkResp.setNamespaceContactEmail(namespaceObj.get().getEmail());
           }
           else {
               throw new APIException(HttpStatus.ACCEPTED,"Namespace is not Returned.");
           }
            }
        return checkResp;
    }

    public Sctid getSctid(String sctid) throws APIException {
        Sctid newRec = new Sctid();
        if(!this.validSCTId(sctid))
        {
            throw new APIException(HttpStatus.ACCEPTED,"Not valid SCTID.");
        }
        Optional<Sctid> sctid1 = sctidRepository.findById(sctid);
        if(sctid1.isPresent())
        {
            newRec = sctid1.get();
        }
        else
        {
            newRec = this.getFreeRecord(sctid);
        }
        return newRec;
    }

    public Sctid getFreeRecord(String sctid)
    {
        Map<String,Object> sctIdRecord = this.getNewRecord(sctid);
        sctIdRecord.put("status",modelsConstants.AVAILABLE);
        var newRecord = insertSCTIDRecord(sctIdRecord);
       return newRecord;
    }

    public Sctid insertSCTIDRecord(Map<String, Object> sctIdRecord) {
        String sctid = null;
        long sequence = 0;
        int namespace = 0;
        String partitionId = null;
        int checkDigit = 0;
        String systemId = null;
        String status = null;
        // Using entrySet() to get the entry's of the map
        Set<Map.Entry<String, Object>> s = sctIdRecord.entrySet();

        for (Map.Entry<String, Object> it : s) {
            if (it.getKey() == "sctid") {
                sctid = (String) it.getValue();
            } else if (it.getKey() == "sequence") {
                sequence = (long) it.getValue();
            } else if (it.getKey() == "namespace") {
                namespace = (int) it.getValue();
            } else if (it.getKey() == "partitionId") {
                partitionId = (String) it.getValue();
            } else if (it.getKey() == "checkDigit") {
                checkDigit = (int) it.getValue();
            } else if (it.getKey() == "systemId") {
                systemId = (String) it.getValue();
            } else if (it.getKey() == "status") {
                status = (String) it.getValue();
            }
        }
        sctidRepository.insertWithQuery(sctid, sequence, namespace, partitionId, checkDigit, systemId, status);
        Sctid sct = sctidRepository.getSctidsById(sctid);
        return sct;
    }

    public Map<String,Object> getNewRecord(String sctIdInput)
    {
        Map<String, Object> sctIdRecord = new LinkedHashMap<>();
        sctIdRecord.put("sctid", sctIdInput);
        sctIdRecord.put("sequence", this.getSequence(sctIdInput));
        sctIdRecord.put("namespace", this.getNamespace(sctIdInput));
        sctIdRecord.put("partitionId", this.getPartition(sctIdInput));
        sctIdRecord.put("checkDigit", this.getCheckDigit(sctIdInput));
        sctIdRecord.put("systemId", this.guid());
        return sctIdRecord;
    }

}
