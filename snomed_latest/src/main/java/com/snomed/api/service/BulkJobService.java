package com.snomed.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.snomed.api.controller.dto.BulkJobsListResponse;
import com.snomed.api.controller.dto.CleanUpServiceResponse;
import com.snomed.api.controller.dto.UserDTO;
import com.snomed.api.domain.BulkJob;
import com.snomed.api.domain.SchemeId;
import com.snomed.api.domain.Sctid;
import com.snomed.api.exception.APIException;
import com.snomed.api.helper.ModelsConstants;
import com.snomed.api.repository.BulkJobRepository;
import com.snomed.api.repository.SctidRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@Service
public class BulkJobService {
    @Autowired
    BulkSctidService bulkSctidService;

    @Autowired
    AuthenticateToken authenticateToken;


    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    BulkJobRepository bulkJobRepository;

    @Autowired
    CleanUpServiceResponse cleanUpServiceResponse;

    @Autowired
    BulkJobsListResponse bulkJobsListResponse;

    @Autowired
    SctidRepository sctidRepository;

    public List<BulkJob> getJobs(String token) throws APIException {
        List<BulkJob> result = null;
        if (bulkSctidService.authenticateToken(token)) {
            Map<String, Object> queryObject = new HashMap();
            Map<String, Integer> fields = new LinkedHashMap<>();
            fields.put("id", 1);
            fields.put("name", 1);
            fields.put("status", 1);
            fields.put("created_at", 1);
            fields.put("modified_at", 1);
            Map<String, String> orderBy = new HashMap();
            orderBy.put("created_at","D");
            result = this.findFieldSelect(queryObject,fields,100,null,orderBy);
        }
            else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return result;
    }

    public List<BulkJob> findFieldSelect(Map<String, Object> queryObject, Map<String,Integer> fields, Integer limit, Integer skip, Map<String,String> orderBy){
        List<BulkJob> bulkJobList;
            if (queryObject.isEmpty()){
                queryObject= new HashMap<>();
            }
            var swhere="";
        if (queryObject.size() > 0) {
            for (var query :
                    queryObject.entrySet()) {
                swhere += " And " + query.getKey() + "=" + (query.getValue());
            }
        }

            if (swhere!=""){
                swhere = " WHERE " + swhere.substring(5);
            }
            var select="";
        if (fields.size() > 0) {
            for (var field :
                    fields.entrySet()) {
                select += "," + field.getKey();
            }
        }
            if (select!=""){
                select =  select.substring(1);
            }else{
                select ="*";
            }
            var dataOrder="";
            if(orderBy.size()>0)
            {
                for(var field: orderBy.entrySet())
                {
                    dataOrder += "," + field.getKey();
                    if(field.getValue()=="D")
                    {
                        dataOrder += " desc" ;
                    }
                }
            }

            if (dataOrder!=""){
                dataOrder =  dataOrder.substring(1);
            }else{
                dataOrder ="id";
            }
            String sql;
            if ((null != limit && limit>0) && ((null == skip || skip==0))) {
                sql = "SELECT " + "*" + " FROM bulkJob" + swhere + " order by " + dataOrder + " limit " + limit;
            }else{
                sql = "SELECT " + "*" + " FROM bulkJob" + swhere + " order by " + dataOrder ;
            }
        Query genQuery = entityManager.createNativeQuery(sql,BulkJob.class);
        System.out.println("genQuery BUlkJob:"+genQuery);
        List<BulkJob> resultList = genQuery.getResultList();
        for (Object resp:
        resultList) {
            System.out.println("reesp1:"+resp.getClass());

            //BulkJobsListResponse response = new BulkJobsListResponse(resp(0))
        }
        if(null == skip || skip==0)
        {
            bulkJobList = resultList;
        }
        else
        {
            var cont = 1;
            List<BulkJob> newRows = new ArrayList<>();
            for (var i = 0; i < resultList.size(); i++) {
                if (i >= skip) {
                    if (null != limit && limit > 0 && limit < cont) {
                        break;
                    }
                    newRows.add(resultList.get(i));
                    cont++;
                }
            }
            bulkJobList = newRows;
        }
     return bulkJobList;
    }

    public BulkJob getJob(String token, Integer jobId) throws APIException {
        BulkJob result = null;
        if (bulkSctidService.authenticateToken(token)) {
            BulkJob bulkJob = bulkJobRepository.findById(jobId).get();
            if(bulkJob.getId() == jobId)
                result = bulkJob;
            else
                throw new APIException(HttpStatus.NOT_FOUND,"There is no result from Database for jobId"+jobId);
        }
        else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return result;
    }

    public List<Object> getJobRecords(String token,Integer jobId) throws APIException, JsonProcessingException {
        var t2 = new Date().getTime();
        List<Object> list = new ArrayList<>();
        if (bulkSctidService.authenticateToken(token)) {
            BulkJob jobRecord = bulkJobRepository.findById(jobId).get();
            if (null != jobRecord) {
                JSONObject request = new JSONObject(jobRecord.getRequest());
                if ((request.getString("model")).equalsIgnoreCase(ModelsConstants.SCHEME_ID)) {
                    List<SchemeId> schemesByJobId = this.findSchemeByJobId(jobId);
                    if (null != schemesByJobId && !schemesByJobId.isEmpty()) {
                        list = Collections.singletonList(schemesByJobId);
                    }
                } else if((request.getString("model")).equalsIgnoreCase(ModelsConstants.SCTID)) {
                    List<Sctid> sctidByJobId = this.findSctidByJobId(jobId);
                    list = Collections.singletonList(sctidByJobId);
                }
                var t3 = new Date().getTime();
                System.out.println("getJobRecords took: " + (t3 - t2) + " milisecs");
            }
            else
            {
                list = null;
            }
        }
        else{
                    throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
                }
        return list;
            }


        public List<SchemeId> findSchemeByJobId(Integer jobId)
        {
            var sql = "SELECT * FROM schemeId WHERE jobId = " + (jobId) + " UNION SELECT * FROM schemeId_log WHERE jobId =  " + (jobId);
            Query genQuery = entityManager.createNativeQuery(sql, SchemeId.class);
            List<SchemeId> resultList = genQuery.getResultList();
            List cleanRows = new ArrayList();
            List ids = new ArrayList();
            if (ids.size() > 0) {
                for (var i = 0; i < resultList.size(); i++) {
                        if (!ids.contains(resultList.get(i).getSystemId()))
                        {
                            cleanRows.add(resultList.get(i));
                            ids.add(resultList.get(i).getSystemId());
                        }
                }
            }
        else{
            for (var i = 0; i < resultList.size(); i++) {
                cleanRows.add(resultList.get(i));
                ids.add(resultList.get(i).getSystemId());
            }
        }
        return cleanRows;
        }

    public List<Sctid> findSctidByJobId(Integer jobId)
    {
        var sql = "SELECT * FROM sctId WHERE jobId = " + jobId + " UNION SELECT * FROM sctId_log WHERE jobId =  " + jobId;
        Query genQuery = entityManager.createNativeQuery(sql, Sctid.class);
        List<Sctid> resultList = genQuery.getResultList();
        List cleanRows = new ArrayList();
        List ids = new ArrayList();
        if (ids.size() > 0) {
            for (var i = 0; i < resultList.size(); i++) {
                if (!ids.contains(resultList.get(i).getSystemId()))
                {
                    cleanRows.add(resultList.get(i));
                    ids.add(resultList.get(i).getSystemId());
                }
            }
        }
        else{
            for (var i = 0; i < resultList.size(); i++) {
                cleanRows.add(resultList.get(i));
                ids.add(resultList.get(i).getSystemId());
            }
        }
        return cleanRows;
    }

        public List<CleanUpServiceResponse> cleanUpExpiredIds(String token) throws APIException {
            List<CleanUpServiceResponse> result = null;
            if (bulkSctidService.authenticateToken(token)) {
                UserDTO userObj = bulkSctidService.getAuthenticatedUser();
                if (this.isAbleUser(userObj.getLogin())) {
                    String strErr="";
                    String strData="";
                    String strMsg="";
                    ArrayList<String> arrMsg = new ArrayList<String>();
                    //Arrays.toString(arrMsg);
                    int step=0;
                    try {
                        String sql = "Update sctId set expirationDate=null,status='Available',software='Clean Service' where status='Reserved' and expirationDate<now()";
                        Query genQuery = entityManager.createNativeQuery(sql, Sctid.class);
                        int returnVal = genQuery.executeUpdate();
                        System.out.println("return Val:" + returnVal);
                        List<Sctid> outputList = genQuery.getResultList();
                        if (outputList.size() >= 0) {
                            cleanUpServiceResponse.setModel("SctId");
                            cleanUpServiceResponse.setAffectedRows(returnVal);
                            cleanUpServiceResponse.setChangedRows(returnVal);
                            cleanUpServiceResponse.setFieldCount(0);
                            cleanUpServiceResponse.setInsertId(0);
                            cleanUpServiceResponse.setServerStatus(34);
                            cleanUpServiceResponse.setProtocol41(true);
                            cleanUpServiceResponse.setWarningCount(0);
                            cleanUpServiceResponse.setMessage("(Rows matched:" + returnVal + "  Changed:" + 0 + " Warnings:" + 0);
                            result.add(cleanUpServiceResponse);
                            var strD=" SctId Expiration date clean up process:" + returnVal;
                                strData+=strD;
                                arrMsg.add(strD);
                                step++;
                                /*if (step>1) {
                                    if (strErr=="") {
                                        callback( null,arrMsg);
                                    }else{
                                        strMsg=strErr + strData;
                                        callback(strMsg, null);
                                    }
                                }*/
                        }
                    }
                    catch(Exception e)
                    {
                        throw new APIException(HttpStatus.UNAUTHORIZED," [Error] in clean up service - SctId expiration date: "+ e.getMessage());
                    }
                    try {
                        String sql = "Update schemeId set expirationDate=null,status='Available',software='Clean Service' where status='Reserved' and expirationDate<now()";
                        Query genQuery = entityManager.createNativeQuery(sql, SchemeId.class);
                        int returnVal = genQuery.executeUpdate();
                        System.out.println("return Val:" + returnVal);
                        List<SchemeId> outputList = genQuery.getResultList();
                        if (outputList.size() >= 0) {
                            cleanUpServiceResponse.setModel("SchemeId");
                            cleanUpServiceResponse.setAffectedRows(returnVal);
                            cleanUpServiceResponse.setChangedRows(returnVal);
                            cleanUpServiceResponse.setFieldCount(0);
                            cleanUpServiceResponse.setInsertId(0);
                            cleanUpServiceResponse.setServerStatus(34);
                            cleanUpServiceResponse.setProtocol41(true);
                            cleanUpServiceResponse.setWarningCount(0);
                            cleanUpServiceResponse.setMessage("(Rows matched:" + returnVal + "  Changed:" + 0 + " Warnings:" + 0);
                            result.add(cleanUpServiceResponse);
                            var strD=" SchemeId Expiration date clean up process:" + returnVal;
                            strData+=strD;
                            arrMsg.add(strD);
                            step++;
                                /*if (step>1) {
                                    if (strErr=="") {
                                        callback( null,arrMsg);
                                    }else{
                                        strMsg=strErr + strData;
                                        callback(strMsg, null);
                                    }
                                }*/
                        }
                    }
                    catch(Exception e)
                    {
                        throw new APIException(HttpStatus.UNAUTHORIZED," [Error] in clean up service - SctId expiration date: "+ e.getMessage());
                    }
                } else {
                    throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
                }
            } else {
                throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
            }
            return result;
        }

        public boolean isAbleUser(String username) throws APIException {
            List<String> groups = authenticateToken.getGroupsList();
            boolean able = false;
            for(String group:groups)
            {
                if(group.equalsIgnoreCase("component-identifier-service-admin"))
                {
                    able = true;
                }
            }
       /* List<String> admins = Arrays.asList("keerthika", "lakshmana", "c");
        for (String admin : admins) {
            if (admin.equalsIgnoreCase(username)) {
                able = true;
            }
        }*/
        return able;
    }
    }