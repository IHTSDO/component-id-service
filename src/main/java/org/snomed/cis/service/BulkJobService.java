package org.snomed.cis.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.BulkJobsListResponse;
import org.snomed.cis.dto.CleanUpServiceResponse;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.BulkJobRepository;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.util.ModelsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.print.DocFlavor;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BulkJobService {
    private final Logger logger = LoggerFactory.getLogger(BulkJobService.class);
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

    public List<BulkJob> getJobs() {
        logger.debug("BulkJobService.getJobs()- Inside Service");
        List<BulkJob> result = null;
        Map<String, Object> queryObject = new HashMap();
        Map<String, Integer> fields = new LinkedHashMap<>();
        fields.put("id", 1);
        fields.put("name", 1);
        fields.put("status", 1);
        fields.put("created_at", 1);
        fields.put("modified_at", 1);
        Map<String, String> orderBy = new HashMap();
        orderBy.put("created_at", "D");
        result = this.findFieldSelect(queryObject, fields, 100, null, orderBy);
        logger.info("BulkJobService.getJobs() - Response :: {}", result);
        return result;
    }

    public List<BulkJob> findFieldSelect(Map<String, Object> queryObject, Map<String, Integer> fields, Integer limit, Integer skip, Map<String, String> orderBy) {
        logger.debug("BulkJobService.findFieldSelect() queryObject-{} :: fields-{}, limit :: {}, skip :: {}, orderBy :: {}", queryObject, fields, limit, skip, orderBy);
        List<BulkJob> bulkJobList;
        if (queryObject.isEmpty()) {
            queryObject = new HashMap<>();
        }
        StringBuffer swhere = new StringBuffer("");
        if (queryObject.size() > 0) {
            for (var query :
                    queryObject.entrySet()) {
                swhere.append(" And ").append(query.getKey()).append("=").append(query.getValue());
            }
        }

        if (!(swhere.toString().equalsIgnoreCase(""))) {
            swhere.append(" WHERE ").append(swhere.substring(5));
        }
        String selectStmnt;
        StringBuffer select = new StringBuffer("");
        if (fields.size() > 0) {
            for (var field :
                    fields.entrySet()) {
                select.append(",").append(field.getKey());
            }
        }
        if (!(select.toString().equalsIgnoreCase(""))) {
            selectStmnt = select.toString().substring(1);
        } else {
            selectStmnt = "*";
        }
        String dataOrderOutput;
        StringBuffer dataOrder = new StringBuffer("");
        if (orderBy.size() > 0) {
            for (var field : orderBy.entrySet()) {
                dataOrder.append(",").append(field.getKey());
                if (field.getValue() == "D") {
                    dataOrder.append(" desc");
                }
            }
        }

        if (dataOrder.toString() != "") {
            dataOrderOutput = dataOrder.toString().substring(1);
        } else {
            dataOrderOutput = "id";
        }
        StringBuffer sql = new StringBuffer();
        if ((null != limit && limit > 0) && ((null == skip || skip == 0))) {
        sql.append("SELECT ").append("*").append(" FROM bulkJob").append(swhere).append(" order by ")
                .append(dataOrderOutput).append(" limit ").append(limit);
        } else {
            sql.append("SELECT ").append("*").append(" FROM bulkJob").append(swhere).append(" order by ")
                    .append(dataOrderOutput);
        }
        Query genQuery = entityManager.createNativeQuery(sql.toString(), BulkJob.class);
        List<BulkJob> resultList = genQuery.getResultList();
        if (null == skip || skip == 0) {
            bulkJobList = resultList;
        } else {
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
        logger.info("BulkJobService.findFieldSelect() - Response-bulkJobList :: {}", bulkJobList);
        return bulkJobList;
    }

    public BulkJob getJob(Integer jobId) throws CisException {
        logger.debug("BulkJobService.getJob() jobId :: {}", jobId);
        BulkJob result = null;
        BulkJob bulkJob = (bulkJobRepository.findById(jobId).isPresent()) ? bulkJobRepository.findById(jobId).get() : null;
        if (null != bulkJob) {
            if (bulkJob.getId() == jobId)
                result = bulkJob;
        } else {
            logger.error("error getJob():: There is no result from Database for jobId {}", jobId);
            throw new CisException(HttpStatus.NOT_FOUND, "There is no result from Database for jobId" + jobId);
        }
        logger.info("BulkJobService.getJob() - Response :: {}", result);
        return result;
    }

    public List<Object> getJobRecords(Integer jobId) {
        logger.debug("BulkJobService.getJobRecords() jobId :: {}", jobId);
        var t2 = new Date().getTime();
        List<Object> list = new ArrayList<>();
        BulkJob jobRecord = (bulkJobRepository.findById(jobId).isPresent()) ? (bulkJobRepository.findById(jobId).get()) : null;
        if (null != jobRecord) {
            JSONObject request = new JSONObject(jobRecord.getRequest());
            if ((request.getString("model")).equalsIgnoreCase(ModelsConstants.SCHEME_ID)) {
                List<SchemeId> schemesByJobId = this.findSchemeByJobId(jobId);
                if (null != schemesByJobId && !schemesByJobId.isEmpty()) {
                    list = Collections.singletonList(schemesByJobId);
                }
            } else if ((request.getString("model")).equalsIgnoreCase(ModelsConstants.SCTID)) {
                List<Sctid> sctidByJobId = this.findSctidByJobId(jobId);
                list = Collections.singletonList(sctidByJobId);
            }
            var t3 = new Date().getTime();
        } else {
            list = null;
        }
        logger.info("BulkJobService.getJobRecords() - Response List<Object> :: {}", list);
        return list;
    }


    public List<SchemeId> findSchemeByJobId(Integer jobId) {
        logger.debug("BulkJobService.findSchemeByJobId() jobId :: {}", jobId);
       // var sql = "SELECT * FROM schemeId WHERE jobId = " + (jobId) + " UNION SELECT * FROM schemeId_log WHERE jobId =  " + (jobId);
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT * FROM schemeId WHERE jobId = ").append((jobId))
                .append(" UNION SELECT * FROM schemeId_log WHERE jobId =  ")
                .append((jobId));
        Query genQuery = entityManager.createNativeQuery(sql.toString(), SchemeId.class);
        List<SchemeId> resultList = genQuery.getResultList();
        List cleanRows = new ArrayList();
        List ids = new ArrayList();
        if (ids.size() > 0) {
            for (var i = 0; i < resultList.size(); i++) {
                if (!ids.contains(resultList.get(i).getSystemId())) {
                    cleanRows.add(resultList.get(i));
                    ids.add(resultList.get(i).getSystemId());
                }
            }
        } else {
            for (var i = 0; i < resultList.size(); i++) {
                cleanRows.add(resultList.get(i));
                ids.add(resultList.get(i).getSystemId());
            }
        }
        logger.info("BulkJobService.findSchemeByJobId() - Response :: {}", cleanRows);
        return cleanRows;
    }

    public List<Sctid> findSctidByJobId(Integer jobId) {
        logger.debug("BulkJobService.findSctidByJobId() jobId :: {}", jobId);
        //var sql = "SELECT * FROM sctId WHERE jobId = " + jobId + " UNION SELECT * FROM sctId_log WHERE jobId =  " + jobId;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT * FROM sctId WHERE jobId = ").append(jobId).append(" UNION SELECT * FROM sctId_log WHERE jobId =  ")
                .append(jobId);
        Query genQuery = entityManager.createNativeQuery(sql.toString(), Sctid.class);
        List<Sctid> resultList = genQuery.getResultList();
        List cleanRows = new ArrayList();
        List ids = new ArrayList();
        if (ids.size() > 0) {
            for (var i = 0; i < resultList.size(); i++) {
                if (!ids.contains(resultList.get(i).getSystemId())) {
                    cleanRows.add(resultList.get(i));
                    ids.add(resultList.get(i).getSystemId());
                }
            }
        } else {
            for (var i = 0; i < resultList.size(); i++) {
                cleanRows.add(resultList.get(i));
                ids.add(resultList.get(i).getSystemId());
            }
        }
        logger.info("BulkJobService.findSctidByJobId() - Response :: {}", cleanRows);
        return cleanRows;
    }

    public List<CleanUpServiceResponse> cleanUpExpiredIds(AuthenticateResponseDto token) throws CisException {
        logger.debug("BulkJobService.cleanUpExpiredIds() AuthenticateResponseDto :: {}", token);
        List<CleanUpServiceResponse> result = null;

        if (this.isAbleUser(token)) {
            String strErr = "";
            String strData = "";
            String strMsg = "";
            ArrayList<String> arrMsg = new ArrayList<String>();

            int step = 0;
            try {
                String sql = "Update sctId set expirationDate=null,status='Available',software='Clean Service' where status='Reserved' and expirationDate<now()";
                Query genQuery = entityManager.createNativeQuery(sql, Sctid.class);
                int returnVal = genQuery.executeUpdate();
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
                    var strD = " SctId Expiration date clean up process:" + returnVal;
                    strData += strD;
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
            } catch (Exception e) {
                logger.error("error cleanUpExpiredIds():: [Error] in clean up service - SctId expiration date: {}", e.getMessage());
                throw new CisException(HttpStatus.UNAUTHORIZED, " [Error] in clean up service - SctId expiration date: " + e.getMessage());
            }
            try {
                String sql = "Update schemeId set expirationDate=null,status='Available',software='Clean Service' where status='Reserved' and expirationDate<now()";
                Query genQuery = entityManager.createNativeQuery(sql, SchemeId.class);
                int returnVal = genQuery.executeUpdate();
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
                    var strD = " SchemeId Expiration date clean up process:" + returnVal;
                    strData += strD;
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
            } catch (Exception e) {
                logger.error("error cleanUpExpiredIds():: [Error] in clean up service - SctId expiration date: {}", e.getMessage());
                throw new CisException(HttpStatus.UNAUTHORIZED, " [Error] in clean up service - SctId expiration date: " + e.getMessage());
            }
        } else {
            logger.error("error cleanUpExpiredIds():: No permission for the selected operation.");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
        logger.info("BulkJobService.cleanUpExpiredIds() - Response :: {}", result);
        return result;
    }

    public boolean isAbleUser(AuthenticateResponseDto authenticateResponseDto) throws CisException {
        logger.debug("BulkJobService.isAbleUser() {} :: {}", authenticateResponseDto);
        List<String> groups = authenticateResponseDto.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
        boolean isAble = false;
        if (groups.contains("component-identifier-service-admin")) {
            isAble = true;
        }
        logger.info("BulkJobService.isAbleUser() - Response :: {}", isAble);
        return isAble;
    }
}