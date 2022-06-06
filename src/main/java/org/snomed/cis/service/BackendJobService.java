package org.snomed.cis.service;

import com.google.common.collect.Sets;
import org.json.JSONObject;
import org.snomed.cis.dto.BulkJobResponseDto;
import org.snomed.cis.domain.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.BulkSchemeIdRepository;
import org.snomed.cis.repository.PartitionsRepository;
import org.snomed.cis.repository.SchemeIdBaseRepository;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.service.DM.SCTIdDM;
import org.snomed.cis.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

public class BackendJobService {
    @Autowired
    SchemeIdService schemeIdService;
    @PersistenceContext
    static
    EntityManager entityManager;
    @Autowired
    JobTypeConstants jobType;
    @Autowired
    StateMachine stateMachine;
    @Autowired
    SctidRepository sctidRepository;
    @Autowired
    PartitionsRepository partitionsRepository;
    @Autowired
    SctidService sctidService;
    @Autowired
    SCTIdDM sctIdDM;
    @Autowired
    BulkSchemeIdRepository schemeIdRepository;
    @Autowired
    SchemeIdBaseRepository schemeIdBaseRepository;

    int chunk = 1000;
    @Autowired
    private SctIdHelper sctIdHelper;

    /*public static void main(String[] args) throws CisException {
        BankEndJobService jobObj=new BankEndJobService();
        jobObj.runner();
    }*/
    public static List<BulkJobResponseDto> findFieldSelect(Map<String, String> queryObject, Map<String, Integer> queryObj, String limit, String skip, Map<String, String> orderBy) {
        //var record= ;
        String swhere = "";
        var limitR = 100;
        var skipTo = 0;
        if (limit != null) limitR = Integer.parseInt(limit);
        if (skip != null) skipTo = Integer.parseInt(skip);
        if (queryObject.size() > 0) {
            for (var query : queryObject.entrySet()) {
                swhere += " And " + query.getKey() + "=" + (query.getValue());
            }
        }
        if (swhere != "") {
            swhere = " WHERE " + swhere.substring(5);
        }
        String select = "";
        if (queryObject != null) {
            for (var query : queryObject.entrySet()) {
                select += "," + query.getKey();
            }
        }
        if (select != "") {
            select = select.substring(1);
        } else {
            select = "*";
        }
        var dataOrder = "";
        if (orderBy != null) {
            for (var field : orderBy.entrySet()) {

                dataOrder += "," + field;
                    /*if (orderBy[field]=="D"){
                        dataOrder += " desc" ;
                    }*/
                // To DO

            }
        }

        if (dataOrder != "") {
            dataOrder = dataOrder.substring(1);
        } else {
            dataOrder = "id";
        }
        String sql = null;
        if ((limitR > 0) && (skipTo == 0)) {
            sql = "SELECT " + select + " FROM bulkJob" + swhere + " order by " + dataOrder + " limit " + limit;
        } else {
            sql = "SELECT " + select + " FROM bulkJob" + swhere + " order by " + dataOrder;
        }

        Query genQuery = entityManager.createNativeQuery(sql, BulkJob.class);

        List<BulkJobResponseDto> resultList = genQuery.getResultList();
        if ((skipTo == 0)) {
            return resultList;
        } else {
            var cont = 1;
            List<BulkJobResponseDto> newRows = new ArrayList<>();
            for (var i = 0; i < (resultList.size() / 2); i++) {
                if (i >= skipTo) {
                    if (null != limit && limitR > 0 && limitR < cont) {
                        break;
                    }
                    newRows.add(resultList.get(i));
                    cont++;
                }
            }
            return newRows;
        }
    }
    public List<SchemeId> saveScheme(List<SchemeId> schemeIds, String scheme) throws CisException {
        String supdate = "";
        List<SchemeId> resultList = null;
        if (schemeIds.size() > 0) {
            for (SchemeId query : schemeIds) {
                if (query.getSchemeId()!=null && query.getScheme()!=null) {
                    supdate += " ," + query+ "=";
                }
            }
        }

        if (supdate != "") {
            supdate = supdate.substring(2);

            String sql = "UPDATE schemid SET " + supdate + " ,modified_at=now() WHERE scheme=" +scheme;
            Query genQuery = entityManager.createNativeQuery(sql, BulkJob.class);
            resultList = genQuery.getResultList();
            return resultList;

        }
        return resultList;
    }
    public  List<BulkJob> save(Map<String, Object> qObj, List<BulkJobResponseDto> bulkJobsRecord) throws CisException {
        String supdate = "";
        List<BulkJob> resultList = null;
        if (qObj.size() > 0) {
            for (var query : qObj.entrySet()) {
                if (query.getKey() != "id") {
                    supdate += " ," + query.getKey() + "=";
                }
            }
        }

        if (supdate != "") {
            supdate = supdate.substring(2);

            String sql = "UPDATE bulkJob SET " + supdate + " ,modified_at=now() WHERE id=" + qObj.entrySet();
            Query genQuery = entityManager.createNativeQuery(sql, BulkJob.class);
            resultList = genQuery.getResultList();
            return resultList;

        }
        processJob(bulkJobsRecord.get(0));
        return resultList;
    }

    //@Scheduled(fixedRate = 3000)
    public  void runner() throws CisException {
        Map<String, String> objQuery1 = new HashMap<String, String>();
        Map<String, Integer> objQuery2 = new HashMap<String, Integer>();
        Map<String, String> objQuery3 = new HashMap<String, String>();
        Map<String, Integer> objQuery4 = new HashMap<String, Integer>();
        Map<String, String> objQuery5 = new HashMap<String, String>();

        if (null == objQuery1) {
            objQuery1.put("status", "1");
        }
        if (null == objQuery2) {
            objQuery2.put("id", 1);
        }

        List<BulkJobResponseDto> record = findFieldSelect(objQuery1, objQuery2, "1", null, null);
        if (record != null && record.size() == 0) {
            return;
        }
        if (null == objQuery3) {
            objQuery3.put("status", "0");
        }
        if (null == objQuery2) {
            objQuery2.put("id", 1);
            objQuery2.put("name", 1);
            objQuery2.put("request", 1);
        }
        if (null == objQuery5) {
            objQuery5.put("created_at", "A");
        }
        List<BulkJobResponseDto> bulkJobsRecord = findFieldSelect(objQuery3, objQuery4, "1", null, objQuery5);
        if (bulkJobsRecord != null && bulkJobsRecord.size() > 0) {
            Map<String, Object> objQuery = new HashMap<String, Object>();
            objQuery.put("id", bulkJobsRecord.get(0).getId());
            objQuery.put("status", "1");
            bulkJobsRecord.get(0).setStatus("1");
            List<BulkJob> job = save(objQuery, bulkJobsRecord);

        }
    }//runner()

    private void processJob(BulkJobResponseDto record) throws CisException {
        String request = record.getRequest();
       JSONObject requestJson= new JSONObject(request);


        if (request == null) {
            Map<String, Object> lightJob = new HashMap<String, Object>();
            lightJob.put("id", record.getId());
            lightJob.put("status", "3");
            lightJob.put("log", "Request property is null");
            save(lightJob, null);
        } else {
            Map<String, Object> lightJob = new HashMap<String, Object>();
            lightJob.put("id", record.getId());

            if (jobType.GENERATE_SCTIDS.equalsIgnoreCase(requestJson.getString("type"))) {

                if (record.getSystemId() != null || record.getSystemId().length == 0) {
                    List<String> arrayUuids = new ArrayList<>();
                    for (var i = 0; i < record.getQuantity(); i++) {
                        arrayUuids.add(sctIdHelper.guid());
                    }
                    record.setSystemId(new String[]{String.valueOf(arrayUuids)});
                    record.autoSysId = true;
                    stateMachine.actions.get("generate");
                    if (true) {
                        List<BulkJobResponseDto> job = generateSctids(record);
                        if (job == null) {
                            lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                            // Eroor log To DO
                        } else {
                            lightJob.put("status", "2");
                        }
                        List<BulkJob> job1 = save(lightJob, job);
                        /*if (err) {
                            console.log("Error-2 in back end service:" + err);
                            return;
                        } else {

                            console.log("End job " + record.name + " - id:" + record.id);
                        }*/
                        // Error log for save
                    } else {

                        List<BulkJobResponseDto> job = generateSctidsSmallRequest(record);
                        if (job == null) {
                            lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                            // Eroor log To DO
                        } else {
                            lightJob.put("status", "2");
                        }
                        List<BulkJob> job1 = save(lightJob, job);
                        try {
                            if (job == null) {
                                System.out.println("Error-2 in back end service:");
                                return;
                            } else {
                                System.out.println("End job " + record.getName() + " - id:" + record.getId());
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                        // Error log for save

                    }
                }

            } else if ((jobType.REGISTER_SCTIDS).equalsIgnoreCase(requestJson.getString("type"))) {

                if (true) {
                    List<BulkJobResponseDto> job = registerSctids(record);
                    if (job == null) {
                        lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                        // Eroor log To DO
                    } else {
                        lightJob.put("status", "2");
                    }
                    List<BulkJob> job1 = save(lightJob, job);
                    try {
                        if (job1 == null) {
                            System.out.println("Error-2 in back end service:");
                            return;
                        } else {
                            System.out.println("End job " + record.getName() + " - id:" + record.getId());
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    List<BulkJobResponseDto> job = registerSctidsSmallRequest(record);
                    if (job == null) {
                        lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                        // Eroor log To DO
                    } else {
                        lightJob.put("status", "2");
                    }
                    List<BulkJob> job1 = save(lightJob, job);
                    try {
                        if (job1 == null) {
                            System.out.println("Error-2 in back end service:");
                            return;
                        } else {
                            System.out.println("End job " + record.getName() + " - id:" + record.getId());
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    // Error log for save

                }
            } else if ((jobType.RESERVE_SCTIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                if (record.getSystemId() == null || record.getSystemId().length == 0) {
                    List<String> arrayUuids = new ArrayList<>();
                    for (var i = 0; i < record.getQuantity(); i++) {
                        arrayUuids.add(sctIdHelper.guid());
                    }
                    record.setSystemId(new String[]{String.valueOf(arrayUuids)});
                }
                record.setAction(stateMachine.actions.get("reserve"));
                //
                if (true) {
                    List<BulkJobResponseDto> job = generateSctids(record);
                    if (job == null) {
                        lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                        // Eroor log To DO
                    } else {
                        lightJob.put("status", "2");
                    }
                    List<BulkJob> job1 = save(lightJob, job);
                        /*if (err) {
                            console.log("Error-2 in back end service:" + err);
                            return;
                        } else {

                            console.log("End job " + record.name + " - id:" + record.id);
                        }*/
                    // Error log for save
                } else {

                    List<BulkJobResponseDto> job = generateSctidsSmallRequest(record);
                    if (job == null) {
                        lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                        // Eroor log To DO
                    } else {
                        lightJob.put("status", "2");
                    }
                    List<BulkJob> job1 = save(lightJob, job);
                    try {
                        if (job1 == null) {
                            System.out.println("Error-2 in back end service:");
                            return;
                        } else {
                            System.out.println("End job " + record.getName() + " - id:" + record.getId());
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    // Error log for save

                }
                //

            } else if ((jobType.DEPRECATE_SCTIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                record.setAction(stateMachine.actions.get("deprecate"));
                List<BulkJobResponseDto> job = updateSctids(record);
                if (job == null) {
                    lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                    // Eroor log To DO
                } else {
                    lightJob.put("status", "2");
                }
                List<BulkJob> job1 = save(lightJob, job);
                try {
                    if (job1 == null) {
                        System.out.println("Error-2 in back end service:");
                        return;
                    } else {
                        System.out.println("End job " + record.getName() + " - id:" + record.getId());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            } else if ((jobType.RELEASE_SCTIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                record.setAction(stateMachine.actions.get("releasse"));
                List<BulkJobResponseDto> job = updateSctids(record);
                if (job == null) {
                    lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                    // Eroor log To DO
                } else {
                    lightJob.put("status", "2");
                }
                List<BulkJob> job1 = save(lightJob, job);
                try {
                    if (job1 == null) {
                        System.out.println("Error-2 in back end service:");
                        return;
                    } else {
                        System.out.println("End job " + record.getName() + " - id:" + record.getId());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            } else if ((jobType.PUBLISH_SCTIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                record.setAction(stateMachine.actions.get("publish"));
                List<BulkJobResponseDto> job = updateSctids(record);
                if (job == null) {
                    lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                    // Eroor log To DO
                } else {
                    lightJob.put("status", "2");
                }
                List<BulkJob> job1 = save(lightJob, job);
                try {
                    if (job1 == null) {
                        System.out.println("Error-2 in back end service:");
                        return;
                    } else {
                        System.out.println("End job " + record.getName() + " - id:" + record.getId());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            } else if ((jobType.GENERATE_SCHEMEIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                if (record.getSystemId() == null || record.getSystemId().length == 0) {
                    List<String> arrayUuids = new ArrayList<>();
                    for (var i = 0; i < record.getQuantity(); i++) {
                        arrayUuids.add(sctIdHelper.guid());
                    }
                    record.setSystemId(new String[]{String.valueOf(arrayUuids)});
                }
                record.setAction(stateMachine.actions.get("generate"));
                if (true) {
                    List<BulkJobResponseDto> job = generateSchemeIds(record);
                    if (job == null) {
                        lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                        // Eroor log To DO
                    } else {
                        lightJob.put("status", "2");
                    }
                    List<BulkJob> job1 = save(lightJob, job);
                        /*if (err) {
                            console.log("Error-2 in back end service:" + err);
                            return;
                        } else {

                            console.log("End job " + record.name + " - id:" + record.id);
                        }*/
                    // Error log for save
                } else {
                    List<BulkJobResponseDto> job = generateSchemeIdSmallRequest(record);
                    if (job == null) {
                        lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                        // Eroor log To DO
                    } else {
                        lightJob.put("status", "2");
                    }
                    List<BulkJob> job1 = save(lightJob, job);
                    try {
                        if (job1 == null) {
                            System.out.println("Error-2 in back end service:");
                            return;
                        } else {
                            System.out.println("End job " + record.getName() + " - id:" + record.getId());
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    // Error log for save

                }

            } else if ((jobType.REGISTER_SCHEMEIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                List<BulkJobResponseDto> job = registerSchemeIds(record);
                if (job == null) {
                    lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                    // Eroor log To DO
                } else {
                    lightJob.put("status", "2");
                }
                List<BulkJob> job1 = save(lightJob, job);
                try {
                    if (job1 == null) {
                        System.out.println("Error-2 in back end service:");
                        return;
                    } else {
                        System.out.println("End job " + record.getName() + " - id:" + record.getId());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            } else if ((jobType.RESERVE_SCHEMEIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                if (record.getSystemId() == null || record.getSystemId().length == 0) {
                    List<String> arrayUuids = new ArrayList<>();
                    for (var i = 0; i < record.getQuantity(); i++) {
                        arrayUuids.add(sctIdHelper.guid());
                    }
                    record.setSystemId(new String[]{String.valueOf(arrayUuids)});
                }
                record.setAction(stateMachine.actions.get("reserve"));
                List<BulkJobResponseDto> job = generateSchemeIdSmallRequest(record);
                if (job == null) {
                    lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                    // Eroor log To DO
                } else {
                    lightJob.put("status", "2");
                }
                List<BulkJob> job1 = save(lightJob, job);
                try {
                    if (job1 == null) {
                        System.out.println("Error-2 in back end service:");
                        return;
                    } else {
                        System.out.println("End job " + record.getName() + " - id:" + record.getId());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            } else if ((jobType.DEPRECATE_SCHEMEIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                record.setAction(stateMachine.actions.get("deprecate"));
                List<BulkJobResponseDto> job=updateSchemeId(record);
                if (job == null) {
                    lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                    // Eroor log To DO
                } else {
                    lightJob.put("status", "2");
                }
                List<BulkJob> job1 = save(lightJob, job);
                try {
                    if (job1 == null) {
                        System.out.println("Error-2 in back end service:");
                        return;
                    } else {
                        System.out.println("End job " + record.getName() + " - id:" + record.getId());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }


            } else if ((jobType.RELEASE_SCHEMEIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                record.setAction(stateMachine.actions.get("release"));
                List<BulkJobResponseDto> job=updateSchemeId(record);
                if (job == null) {
                    lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                    // Eroor log To DO
                } else {
                    lightJob.put("status", "2");
                }
                List<BulkJob> job1 = save(lightJob, job);
                try {
                    if (job1 == null) {
                        System.out.println("Error-2 in back end service:");
                        return;
                    } else {
                        System.out.println("End job " + record.getName() + " - id:" + record.getId());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }



            } else if ((jobType.PUBLISH_SCHEMEIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                record.setAction(stateMachine.actions.get("publish"));
                List<BulkJobResponseDto> job=updateSchemeId(record);
                if (job == null) {
                    lightJob.put("status", "3");
                           /* if (typeof err == "object") {
                                lightJob.log = JSON.stringify(err);
                            } else {
                                lightJob.log = err;
                            }*/
                    // Eroor log To DO
                } else {
                    lightJob.put("status", "2");
                }
                List<BulkJob> job1 = save(lightJob, job);
                try {
                    if (job1 == null) {
                        System.out.println("Error-2 in back end service:");
                        return;
                    } else {
                        System.out.println("End job " + record.getName() + " - id:" + record.getId());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }


            }


        }// request not null
    }

    private List<BulkJobResponseDto> updateSchemeId(BulkJobResponseDto record) throws CisException {
        var schemeIdRecord = getSchemeId(record.getScheme(), record.getSchemeId(), record.getSystemId(), record.getAutoSysId());
try{
    var cont = 0;
    List<String> records = new ArrayList<>();
    var error = false;
    var scheme = record.getScheme();
    for (var i = 0; i < record.getRecords().length; i++) {
        var schemeId = record.getRecords()[i].getSctid();
        var systemId = record.getRecords()[i].getSystemId();
       // var schemeIdRecord = getSchemeId(record.getScheme(), record.getSchemeId(), record.getSystemId(), record.getAutoSysId());
        if (schemeIdRecord.getSchemeId() == schemeId && schemeIdRecord.getSystemId() != systemId) {
            schemeIdRecord.setSystemId(systemId);
        }
        String newStatus;
        if (stateMachine.getNewStatus(schemeIdRecord.getStatus(),"assigned").equalsIgnoreCase("assigned")){
            newStatus=stateMachine.getNewStatus(schemeIdRecord.getStatus(),"assigned");
        }else {
            newStatus = stateMachine.getNewStatus(schemeIdRecord.getStatus(), stateMachine.actions.get("register"));
        }
        if(newStatus!=null){
            schemeIdRecord.setStatus(newStatus);
            schemeIdRecord.setAuthor(record.getAuthor());
            schemeIdRecord.setSoftware(record.getSoftware());
            schemeIdRecord.setExpirationDate(record.getExpirationDate());
            schemeIdRecord.setComment(record.getComment());
            schemeIdRecord.setJobId(record.getJobId());
            saveScheme((List<SchemeId>) schemeIdRecord, record.getScheme());
            cont++;
            if (cont == record.getRecords().length) {
                cont = 0;
                for (var j = 0; j < records.size(); j++) {
                    // TO DO
                    List<BulkJob> job = null;/*save((Map<String, Object>) records.get(j), (List<BulkJobResponseDto>) record);*/

                    if (job == null) {
                        error = true;
                    }
                    cont++;
                    if (cont == records.size()) {
                        return null;
                    }
                }
            }
        }
        else{
            error = true;
            System.out.println("Cannot register SchemeId:" + schemeIdRecord.getSchemeId() + ", current status: " + schemeIdRecord.getStatus());
            return null;
        }
    }
}
catch (Exception e){
    System.out.println(e.getMessage());
}
        return null;
    }

    private List<BulkJobResponseDto> registerSchemeIds(BulkJobResponseDto record) {
try{
    var cont = 0;
    List<String> records = new ArrayList<>();
    var error = false;
    var scheme = record.getScheme();
    for (var i = 0; i < record.getRecords().length; i++) {
        var schemeId = record.getRecords()[i].getSctid();
        var systemId = record.getRecords()[i].getSystemId();
        var schemeIdRecord = getSchemeId(record.getScheme(), record.getSchemeId(), record.getSystemId(), record.getAutoSysId());
        if (schemeIdRecord.getSchemeId() == schemeId && schemeIdRecord.getSystemId() != systemId) {
            schemeIdRecord.setSystemId(systemId);
        }
        String newStatus;
        if (stateMachine.getNewStatus(schemeIdRecord.getStatus(),"assigned").equalsIgnoreCase("assigned")){
            newStatus=stateMachine.getNewStatus(schemeIdRecord.getStatus(),"assigned");
        }else {
            newStatus = stateMachine.getNewStatus(schemeIdRecord.getStatus(), stateMachine.actions.get("register"));
        }
        if(newStatus!=null){
            schemeIdRecord.setStatus(newStatus);
            schemeIdRecord.setAuthor(record.getAuthor());
            schemeIdRecord.setSoftware(record.getSoftware());
            schemeIdRecord.setExpirationDate(record.getExpirationDate());
            schemeIdRecord.setComment(record.getComment());
            schemeIdRecord.setJobId(record.getJobId());
            saveScheme((List<SchemeId>) schemeIdRecord, record.getScheme());
        }
        else{
            error = true;
            System.out.println("Cannot register SchemeId:" + schemeIdRecord.getSchemeId() + ", current status: " + schemeIdRecord.getStatus());
            return null;
        }
    }
}
catch (Exception e){
    System.out.println(e.getMessage());
}
return null;
    }

    private List<BulkJobResponseDto> generateSchemeIdSmallRequest(BulkJobResponseDto record) {
        var cont = 0;
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("scheme", record.getScheme());
                         Optional<SchemeIdBase> data=schemeIdBaseRepository.findById(record.getScheme());
var thisScheme=data;
        boolean canContinue;
        for (var i = 0; i < record.getQuantity(); i++) {
            canContinue = true;
            try {
                record.systemId = record.getSystemId();
                if (!record.autoSysId) {
                    var schemeIdRecord = schemeIdRepository.findBySchemeAndSchemeIdIn(record.getScheme(), List.of(record.getSystemId()));
                    if (schemeIdRecord != null) {
                        schemeIdRecord.get(i).setJobId(record.getJobId());
                        saveScheme(schemeIdRecord,record.getScheme());
                        canContinue = false;
                    }
                }
                if (canContinue) {
                    generateScheme(record, thisScheme);
                }
                cont++;
                if (record.getQuantity() == cont) {
                    saveScheme((List<SchemeId>) thisScheme.get(),record.getScheme());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }//for
        return null;
    }

    private void generateScheme(BulkJobResponseDto record, Optional<SchemeIdBase> thisScheme) {
        try {
            var rec = setAvailableSchemeIdRecord2NewStatus(record,thisScheme);
            if (rec == null) {
                setNewSchemeIdRecord(record, thisScheme);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private SchemeId setNewSchemeIdRecord(BulkJobResponseDto record, Optional<SchemeIdBase> thisScheme) throws CisException {

       try {
           var previousCode = thisScheme.get().getIdBase();

           String newSchemeId = null;
           String schemeName = thisScheme.get().getScheme().toUpperCase();
           if (schemeName.equalsIgnoreCase("SNOMEDID")) {
               newSchemeId = SNOMEDID.getNextId(previousCode);
           } else {
               newSchemeId = CTV3ID.getNextId(previousCode);

           }
           thisScheme.get().setIdBase(newSchemeId);
           var scheme = thisScheme.get().getScheme();
           String[] systemId;
           var action = record.getAction();
           if (record.getSystemId() != null) {
               systemId = record.getSystemId();
           }
           var schemeIdRecord = getSchemeId(record.getScheme(), record.getSchemeId(), record.getSystemId(), record.getAutoSysId());

           var newStatus = stateMachine.getNewStatus(record.getStatus(), action);
           if (newStatus != null) {
               schemeIdRecord.setStatus(newStatus);
               schemeIdRecord.setAuthor(record.getAuthor());
               schemeIdRecord.setSoftware(record.getSoftware());
               schemeIdRecord.setExpirationDate(record.getExpirationDate());
               schemeIdRecord.setComment(record.getComment());
               schemeIdRecord.setJobId(record.getJobId());
               saveScheme((List<SchemeId>) schemeIdRecord, record.getScheme());
           } else {
               setNewSchemeIdRecord(record, thisScheme);
           }
       }
       catch (Exception e){
           System.out.println(e.getMessage());
       }
        return null;
    }

    private SchemeId getSchemeId(String scheme, String schemeId, String[] systemId, Boolean autoSysId) throws CisException, CisException {
        boolean isValidScheme = false;
        SchemeId id = new SchemeId();
        if ("SNOMEDID".equalsIgnoreCase(scheme.toString().toUpperCase())) {
            isValidScheme = SNOMEDID.validSchemeId(schemeId);
        } else if ("CTV3ID".equalsIgnoreCase(scheme.toString().toUpperCase())) {
            isValidScheme = CTV3ID.validSchemeId(schemeId);
        }
        if(isValidScheme){
Optional<SchemeId> schemeId1=schemeIdRepository.findBySchemeAndSchemeId(scheme,schemeId);
if(schemeId1.isPresent())
{
    id=schemeIdService.getFreeRecords(scheme,schemeId);
}
        }
        return id;
    }

    private SchemeId setAvailableSchemeIdRecord2NewStatus(BulkJobResponseDto generationData, Optional<SchemeIdBase> thisScheme) {
        SchemeId sctOutput = new SchemeId();
        List<SchemeId> sctList = new ArrayList<>();
        Map<String, Object> queryObject = new HashMap<>();
            queryObject.put("scheme", generationData.getScheme());
            queryObject.put("status", "'" + stateMachine.statuses.get("available") + "'");

            sctList = findSchemeIdWithIndexAndLimit(queryObject, "1", null);
            if (sctList.size() > 0) {
                var newStatus = stateMachine.getNewStatus(sctList.get(0).getStatus(), stateMachine.actions.get("generate"));
                if (!newStatus.isBlank()) {
                    if (null != generationData.getSystemId() && generationData.getSystemId().length == 0) {
                        sctList.get(0).setSystemId(String.valueOf(generationData.getSystemId()));
                    }
                    sctList.get(0).setStatus(newStatus);
                    sctList.get(0).setAuthor(generationData.getAuthor());
                    sctList.get(0).setSoftware(generationData.getSoftware());
                    //Doubt - need to be clarified- there is no ExpirationDate in Request body.
                    sctList.get(0).setExpirationDate(generationData.getExpirationDate());
                    sctList.get(0).setComment(generationData.getComment());
                    sctList.get(0).setJobId(null);
                    sctList.get(0).setModified_at(generationData.getModified_at());
                    sctOutput = schemeIdRepository.save(sctList.get(0));
                } else {
                    return null;
                }
            } else {
                return null;
            }

        return sctOutput;
    }

        public List<SchemeId> findSchemeIdWithIndexAndLimit(Map<String, Object> queryObject, String limit, String skip) {
            List<SchemeId> sctList;
            var limitR = 100;
            var skipTo = 0;
            if (!limit.isEmpty() && null != limit)
                limitR = Integer.parseInt(limit);
            if (null != skip)
                skipTo = Integer.parseInt(skip);
            //sctidRepository.findSct(queryObject,limitR,skipTo);
            String swhere = "";
            if (queryObject.size() > 0) {
                for (var query :
                        queryObject.entrySet()) {
                    swhere += " And " + query.getKey() + "=" + (query.getValue());
                }
            }
            if (swhere != "") {
                swhere = " WHERE " + swhere.substring(5);
            }
            String sql;
            if ((limitR > 0) && (skipTo == 0)) {
                //sql = "SELECT * FROM sctId" + swhere + " order by sctid limit " + limit;

                sql = "Select * FROM schemeid " + swhere + " order by schemeid limit " + limit;
            } else {
                //sql = "SELECT * FROM sctId" + swhere + " order by sctid";
                sql = "Select * FROM sctid " + swhere + " order by schemeid";
            }
            Query genQuery = entityManager.createNativeQuery(sql,Sctid.class);
            System.out.println("genQuery:"+genQuery);
            List<SchemeId> resultList =(List<SchemeId> )genQuery.getResultList();
            if ((skipTo == 0)) {
                sctList = resultList;
            } else {
                var cont = 1;
                List<SchemeId> newRows = new ArrayList<>();
                for (var i = 0; i < resultList.size(); i++) {
                    if (i >= skipTo) {
                        if (null != limit && limitR > 0 && limitR < cont) {
                            break;
                        }
                        newRows.add(resultList.get(i));
                        cont++;
                    }
                }
                sctList = newRows;
            }
            return sctList;
        }



    //
    private List<BulkJobResponseDto> generateSchemeIds(BulkJobResponseDto record) {
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("scheme", record.getScheme());
        //
        var newStatus = stateMachine.getNewStatus(record.getStatus(), stateMachine.actions.get("generate"));
        Set<String> sysIdInChunk = new HashSet<String>();
        int insertedCount = 0;
        int quantityToCreate = record.getQuantity();

        for (int i = 1; i <= quantityToCreate; i++) {

            if (sysIdInChunk.contains(record.getSystemId()[i - 1])) {
                quantityToCreate--;
            } else {
                sysIdInChunk.add(record.getSystemId()[i - 1]);
            }

            try {
                if (i % chunk == 0 || i == (record.getQuantity())) {
                    Set<String> diff = new HashSet<String>();
                    boolean allExisting = false;
                    /*int n = sysIdInChunk.size();
                    String arr[] = new String[n];
                    int a = 0;
                    for (String x : sysIdInChunk) {
                        arr[a++] = x;
                    }*/

                    // String[] sysIdToCreate = arr;
                    String[] sysIdToCreate = converttoArray(sysIdInChunk);
                    if (!record.getAutoSysId()) {

                        Map<String, Object> obj1 = new HashMap<>();
                        obj1.put("systemIds", sysIdToCreate);
                        obj1.put("scheme", record.getScheme());
                        List<SchemeId> existingSystemId = schemeIdRepository.findBySchemeAndSystemId(record.getScheme(), String.valueOf(sysIdToCreate));

                        if (existingSystemId.size() > 0) {
                            //update
                            updateJobIdscheme(existingSystemId,record.getScheme(), record.getJobId());
                            if (existingSystemId.size() < sysIdInChunk.size()) {

                                Set<SchemeId> setExistSysId = existingSystemId.stream().collect(Collectors.toSet());
                                diff = Sets.difference(sysIdInChunk, setExistSysId);
                                insertedCount += setExistSysId.size();

                            } else {
                                insertedCount += existingSystemId.size();
                                allExisting = true;
                            }
                        }

                    }
                    if (!allExisting) {
                        Optional<SchemeIdBase> data=schemeIdBaseRepository.findById(record.getScheme());
                        if (data.isEmpty()) {
                            System.out.println("Scheme not found for key:" + record.getScheme());
                            return null;
                        }

                        var previousCode=data.get().getIdBase();

                        List<SchemeId> records = new ArrayList<>();
                        var createAt = new Date();

                        for (String systemId : sysIdToCreate) {

                            List<String> rec = new ArrayList<>();
                            String newSchemeid = record.getScheme().toUpperCase();
                            rec.set(0, newSchemeid);
                            rec.set(1, record.getSchemeId());
                            rec.set(2, String.valueOf(sctIdHelper.getSequence(newSchemeid)));
//partitionId
                            rec.set(3, String.valueOf(sctIdHelper.getCheckDigit(newSchemeid)));
                            rec.set(4, systemId);
                            rec.set(5, newStatus);
                            rec.set(6, record.getAuthor());
                            rec.set(7, record.getSoftware());
                            rec.set(8, String.valueOf(record.getExpirationDate()));
                            rec.set(9, record.getComment());
                            rec.set(10, String.valueOf(record.getJobId()));
                            rec.set(11, String.valueOf(record.getCreated_at()));
                            rec.set(12, String.valueOf(record.getModified_at()));

                            records.add((SchemeId) rec);
                            insertedCount += records.size();
                            //insertRecords(records,record);
                            //schemeIdRepository.insertWithQuery(newSchemeid, record.getSchemeId(), record.getNamespace(), record.getPartitionId(), sctIdHelper.getCheckDigit(newSctid), systemId, newStatus, record.getAuthor(), record.getSoftware(), record.getExpirationDate(), record.getComment(), record.getJobId(), record.getCreated_at());

                        }
                        schemeIdRepository.saveAll(records);//


                    }

                }
            }//try
            catch (Exception e) {
System.out.println("generateSchemeIds error:"+e.getMessage());
            }
        }//for
        if (insertedCount >= quantityToCreate) {
            return null;
        }
        return null;
    }


    private List<BulkJobResponseDto> updateSctids(BulkJobResponseDto record) throws CisException {
        var cont = 0;
        List<Sctid> records = new ArrayList<>();
        boolean error = false;
        for (var i = 0; i < record.getSctid().length; i++) {
            var sctId = record.getSctid()[i];
            Optional<Sctid> sctIdRecord = sctidRepository.findById(String.valueOf(sctId));
            if (!sctIdRecord.isEmpty()) {
                String newStatus = stateMachine.getNewStatus(record.getStatus(), stateMachine.actions.get("reserve"));
                if (newStatus != null) {
                    sctIdRecord.get().setStatus(newStatus);
                    sctIdRecord.get().setAuthor(record.getAuthor());
                    sctIdRecord.get().setSoftware(record.getSoftware());
                    sctIdRecord.get().setExpirationDate(record.getExpirationDate());
                    sctIdRecord.get().setComment(record.getComment());
                    sctIdRecord.get().setJobId(record.getJobId());
                    records.add(sctIdRecord.get());
                    cont++;
                    if (cont == record.getRecords().length) {
                        cont = 0;
                        for (var j = 0; j < records.size(); j++) {
                            List<BulkJob> job = save((Map<String, Object>) records.get(j), (List<BulkJobResponseDto>) record);

                            if (job == null) {
                                error = true;
                            }
                            cont++;
                            if (cont == records.size()) {
                                return null;
                            }
                        }
                    }
                } else {
                    error = true;
                    System.out.println("Cannot " + record.getAction() + " SCTID:" + sctIdRecord.get().getSctid() + ", current status: " + sctIdRecord.get().getStatus());
                    return null;
                }
            }
        }
        return null;
    }

    private List<BulkJobResponseDto> registerSctidsSmallRequest(BulkJobResponseDto record) {
        var cont = 0;
        List<Sctid> records = new ArrayList<>();
        var error = false;
        try {
            for (var i = 0; i < record.getRecords().length; i++) {
                var sctId = record.getRecords()[i].getSctid();
                var systemId = record.getRecords()[i].getSystemId();
                var sctIdRecord = sctidRepository.findBySctidAndSystemId(Integer.parseInt(sctId), systemId);
                if (sctIdRecord.getSctid() == sctId && sctIdRecord.getSystemId() != systemId) {
                    sctIdRecord.setSystemId(systemId);
                }
                String newStatus;
                if (stateMachine.actions.get("assigned").equalsIgnoreCase(sctIdRecord.getStatus())) {
                    newStatus = stateMachine.getNewStatus(record.getStatus(), stateMachine.actions.get("assigned"));
                    ;
                } else {
                    newStatus = stateMachine.getNewStatus(sctIdRecord.getStatus(), stateMachine.actions.get("register"));
                }
                if (newStatus != null) {
                    sctIdRecord.setStatus(newStatus);
                    sctIdRecord.setAuthor(record.getAuthor());
                    sctIdRecord.setSoftware(record.getSoftware());
                    sctIdRecord.setExpirationDate(record.getExpirationDate());
                    sctIdRecord.setComment(record.getComment());
                    sctIdRecord.setJobId(record.getJobId());
                    records.add(sctIdRecord);
                    cont++;
                    if (cont == record.getRecords().length) {
                        cont = 0;
                        for (var j = 0; j < records.size(); j++) {
                            List<BulkJob> job = save((Map<String, Object>) records.get(j), (List<BulkJobResponseDto>) record);

                            if (job == null) {
                                error = true;
                                return null;
                            }
                            cont++;
                            if (cont == records.size()) {
                                return null;

                            }


                        }
                    }
                } else {
                    error = true;
                    System.out.println("Cannot register SCTID:" + sctIdRecord.getSctid() + ", current status: " + sctIdRecord.getStatus());
                    return null;
                }

            }
        } catch (Exception e) {

        }
        return null;
    }

    private List<BulkJobResponseDto> registerSctids(BulkJobResponseDto record) {

        var newStatus = stateMachine.getNewStatus(record.getStatus(), stateMachine.actions.get("register"));

        Set<String> sctIdInChunk = new HashSet<String>();
        int insertedCount = 0;
        int quantityToRegister = record.getRecords().length;
        String[] uuidsMap = null;

        for (var i = 1; i <= quantityToRegister; i++) {
            var sctid = record.getSctid()[i - 1];
            if (sctIdInChunk.contains(sctid)) {
                quantityToRegister--;
            } else {
                sctIdInChunk.add(String.valueOf(record.getSctid()[i - 1]));

                uuidsMap[sctid] = record.getSystemId()[i - 1];
            }

            try {
                if (i % chunk == 0 || i == (quantityToRegister)) {
                    Set<String> diff = new HashSet<String>();
                    var sctIdToRegister = converttoArray(sctIdInChunk);
                    var allExisting = false;

                    List<Sctid> existingSctIds = findExistingSctIds(sctIdToRegister);
                    if (existingSctIds == null && existingSctIds.size() > 0) {
                        updateRegisterStatusAndJobId(existingSctIds, record.getJobId());
                        if (existingSctIds.size() < sctIdInChunk.size()) {
                            var setExistSctId = existingSctIds.stream().collect(Collectors.toSet());

                            //
                            diff = Sets.difference(sctIdInChunk, setExistSctId);
                            insertedCount += setExistSctId.size();
                        } else {
                            insertedCount += existingSctIds.size();
                            allExisting = true;
                        }
                    }
                    if (!allExisting) {
                        if (diff != null) {
                            sctIdToRegister = diff.toArray(new String[0]);
                        }
                        List<Sctid> records = new ArrayList<>();
                        var createAt = new Date();

                        for (String sid : sctIdToRegister) {
                            List<String> rec = new ArrayList<>();
                            int newSctid = Math.toIntExact((sctIdHelper.getSequence(sid)));
                            rec.set(0, sid);
                            rec.set(1, String.valueOf(sctIdHelper.getSequence(sid)));
                            rec.set(2, String.valueOf(record.getNamespace()));
//partitionId
                            rec.set(3, record.getPartitionId());
                            rec.set(4, String.valueOf(sctIdHelper.getCheckDigit(String.valueOf(newSctid))));
                            rec.set(5, uuidsMap[newSctid]);
                            rec.set(6, newStatus);
                            rec.set(7, record.getAuthor());
                            rec.set(8, record.getSoftware());
                            rec.set(9, String.valueOf(record.getExpirationDate()));
                            rec.set(10, record.getComment());
                            rec.set(11, String.valueOf(record.getJobId()));
                            rec.set(12, String.valueOf(record.getCreated_at()));

                            records.add((Sctid) rec);
                            insertedCount += records.size();
                            sctidRepository.saveAll(records);
                            //sctidRepository.bulkInsert(sid, newSctid, record.getNamespace(), record.getPartitionId(), sctIdHelper.getCheckDigit(String.valueOf(newSctid)), sid, newStatus, record.getAuthor(), record.getSoftware(), record.getExpirationDate(), record.getComment(), record.getJobId(), record.getCreated_at());


                        }
                    }
                    /*sctIdInChunk = new sets.StringSet();
                    uuidsMap={};*/
                }

            } catch (Exception e) {
                System.out.println("generateSctids error" + e.getMessage());
                return null;
            }

        }
        if (insertedCount >= quantityToRegister) {
            return null;
        }
        return null;
    }

    private List<Sctid> updateRegisterStatusAndJobId(List<Sctid> existingSctIds, Integer jobId) {
        List<Sctid> result = sctidRepository.updateSctid(existingSctIds, jobId);
        return result;
    }

    private List<Sctid> findExistingSctIds(String[] sctIdToRegister) {
        List<Sctid> sctid = sctidRepository.findBySctidIn(List.of(sctIdToRegister));
        return sctid;
    }

    private List<BulkJobResponseDto> generateSctidsSmallRequest(BulkJobResponseDto record) throws CisException {
        var cont = 0;
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("namespace", record.getNamespace());
        obj.put("partitionId", record.getPartitionId());
        Partitions partList = getPartitiion(record.getNamespace(), record.getPartitionId());
        var thisPartition = partList;
        boolean canContinue;
        for (var i = 0; i < record.getQuantity(); i++) {
            canContinue = true;
            try {
                record.systemId = record.getSystemId();
                if (!record.autoSysId) {
                    var sctIdRecord = getSyncSctidBySystemId(record.getNamespace(), record.getPartitionId());
                    if (sctIdRecord != null) {
                        sctIdRecord.setJobId(record.getJobId());
                        sctidRepository.save(sctIdRecord);
                        canContinue = false;
                    }
                }
                if (canContinue) {
                    generateSctid(record, thisPartition);
                }
                cont++;
                if (record.getQuantity() == cont) {
                    partitionsRepository.save(thisPartition);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }//for
        return null;
    }

    private void generateSctid(BulkJobResponseDto record, Partitions thisPartition) {
        try {
            var rec = setAvailableSCTIDRecord2NewStatus(record);
            if (rec == null) {
                setNewSCTIdRecord(record, thisPartition);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Sctid setNewSCTIdRecord(BulkJobResponseDto generationData, Partitions thisPartition) throws CisException {
        int sequence = thisPartition.getSequence() + 1;

        var newSCTId = computeSctId(generationData, sequence);
        var action = stateMachine.actions.get("generate");
        String systemId[] = null;
        if (generationData.getSystemId() == null && generationData.systemId.length == 0) {
            systemId = generationData.getSystemId();
        }
        Sctid sctIdRecord = getSctid(newSCTId, systemId);

        var newStatus = stateMachine.getNewStatus(sctIdRecord.getStatus(), action);
        if (newStatus != null) {
            sctIdRecord.setStatus(newStatus);
            sctIdRecord.setAuthor(generationData.getAuthor());
            sctIdRecord.setSoftware(generationData.getSoftware());
            sctIdRecord.setExpirationDate(generationData.getExpirationDate());
            sctIdRecord.setComment(generationData.getComment());
            sctIdRecord.setJobId(generationData.getJobId());
            sctidRepository.save(sctIdRecord);
        } else {
            setNewSCTIdRecord(generationData, thisPartition);
        }
        return sctIdRecord;
    }

    private Sctid getSctid(String newSCTId, String[] systemId) {
        if (!sctIdHelper.validSCTId(newSCTId)) {
            System.out.println("Not valid SCTID:" + newSCTId);
            return null;
        }
        Optional<Sctid> sctid = sctidRepository.findById(newSCTId);
        if (sctid.isEmpty()) {
            try {
                var record = sctIdDM.getFreeRecord(sctid.get().toString());
                return record;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            return sctid.get();
        }
        return null;
    }

    public Sctid setAvailableSCTIDRecord2NewStatus(BulkJobResponseDto generationData) throws CisException {
        Sctid sctOutput = new Sctid();
        List<Sctid> sctList = new ArrayList<>();
        Map<String, Object> queryObject = new HashMap<>();
        if (null != generationData.getNamespace() && !generationData.getPartitionId().isBlank()) {
            queryObject.put("namespace", generationData.getNamespace());
            queryObject.put("partitionId", "'" + generationData.getPartitionId() + "'");
            queryObject.put("status", "'" + stateMachine.statuses.get("available") + "'");
            sctList = sctidService.findSctWithIndexAndLimit(queryObject, "1", null);
            if (sctList.size() > 0) {
                var newStatus = stateMachine.getNewStatus(sctList.get(0).getStatus(), stateMachine.actions.get("generate"));
                if (!newStatus.isBlank()) {
                    if (null != generationData.getSystemId() && generationData.getSystemId().length == 0) {
                        sctList.get(0).setSystemId(String.valueOf(generationData.getSystemId()));
                    }
                    sctList.get(0).setStatus(newStatus);
                    sctList.get(0).setAuthor(generationData.getAuthor());
                    sctList.get(0).setSoftware(generationData.getSoftware());
                    //Doubt - need to be clarified- there is no ExpirationDate in Request body.
                    sctList.get(0).setExpirationDate(generationData.getExpirationDate());
                    sctList.get(0).setComment(generationData.getComment());
                    sctList.get(0).setJobId(null);
                    sctList.get(0).setModified_at(generationData.getModified_at());
                    sctOutput = sctidRepository.save(sctList.get(0));
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            throw new CisException(HttpStatus.BAD_REQUEST, "Error input");
        }
        return sctOutput;
    }


    private Sctid getSyncSctidBySystemId(Integer namespace, String partitionId) {
        List<Sctid> sctidList = sctidRepository.findByNamespaceAndPartitionId(namespace, partitionId);
        if (sctidList.size() != 0) {
            return sctidList.get(0);
        } else {
            return null;
        }
    }

    private Partitions getPartitiion(Integer namespace, String partitionId) throws CisException {
        Optional<Partitions> partList = partitionsRepository.findByNamespacePartition(namespace, partitionId);
        if (!partList.isEmpty()) {
            return partList.get();
        } else {
            throw new CisException(HttpStatus.BAD_REQUEST, "Partitions not found for key" + partitionId);

        }
    }

    private List<BulkJobResponseDto> generateSctids(BulkJobResponseDto record) {
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("namespace", record.getNamespace());
        obj.put("partitionId", record.getPartitionId());
        //
        var newStatus = stateMachine.getNewStatus(record.getStatus(), stateMachine.actions.get("generate"));
        Set<String> sysIdInChunk = new HashSet<String>();
        int insertedCount = 0;
        int quantityToCreate = record.getQuantity();

        for (int i = 1; i <= quantityToCreate; i++) {

            if (sysIdInChunk.contains(record.getSystemId()[i - 1])) {
                quantityToCreate--;
            } else {
                sysIdInChunk.add(record.getSystemId()[i - 1]);
            }

            try {
                if (i % chunk == 0 || i == (record.getQuantity())) {
                    Set<String> diff = new HashSet<String>();
                    boolean allExisting = false;
                    /*int n = sysIdInChunk.size();
                    String arr[] = new String[n];
                    int a = 0;
                    for (String x : sysIdInChunk) {
                        arr[a++] = x;
                    }*/

                    // String[] sysIdToCreate = arr;
                    String[] sysIdToCreate = converttoArray(sysIdInChunk);
                    if (!record.getAutoSysId()) {

                        Map<String, Object> obj1 = new HashMap<>();
                        obj1.put("systemIds", sysIdToCreate);
                        obj1.put("namespace", record.getNamespace());
                        List<Sctid> existingSystemId = findExistingSystemId(obj1);

                        if (existingSystemId.size() > 0) {
                            //update
                            updateJobId(existingSystemId, record.getJobId());
                            if (existingSystemId.size() < sysIdInChunk.size()) {

                                Set<Sctid> setExistSysId = existingSystemId.stream().collect(Collectors.toSet());
                                diff = Sets.difference(sysIdInChunk, setExistSysId);
                                insertedCount += setExistSysId.size();

                            } else {
                                insertedCount += existingSystemId.size();
                                allExisting = true;
                            }
                        }

                    }
                    if (!allExisting) {
                        int n1 = diff.size();
                        String differ[] = new String[n1];
                        if (diff == null) {
                            sysIdToCreate = diff.toArray(differ);
                        }
                        int seq = 0;
                        // PL TO DO
                        if (seq == 0) {
                            throw new CisException(HttpStatus.BAD_REQUEST, "Partition not found for key");
                        }

                        List<Sctid> records = new ArrayList<>();
                        var createAt = new Date();

                        for (String systemId : sysIdToCreate) {
                            seq++;
                            List<String> rec = new ArrayList<>();
                            String newSctid = computeSctId(record, seq);
                            rec.set(0, newSctid);
                            rec.set(1, String.valueOf(seq));
                            rec.set(2, String.valueOf(record.getNamespace()));
//partitionId
                            rec.set(3, record.getPartitionId());
                            rec.set(4, String.valueOf(sctIdHelper.getCheckDigit(newSctid)));
                            rec.set(5, systemId);
                            rec.set(6, newStatus);
                            rec.set(7, record.getAuthor());
                            rec.set(8, record.getSoftware());
                            rec.set(9, String.valueOf(record.getExpirationDate()));
                            rec.set(10, record.getComment());
                            rec.set(11, String.valueOf(record.getJobId()));
                            rec.set(12, String.valueOf(record.getCreated_at()));

                            records.add((Sctid) rec);
                            insertedCount += records.size();
                            //insertRecords(records,record);
                            sctidRepository.saveAll(records);
                           // sctidRepository.bulkInsert(newSctid, seq, record.getNamespace(), record.getPartitionId(), sctIdHelper.getCheckDigit(newSctid), systemId, newStatus, record.getAuthor(), record.getSoftware(), record.getExpirationDate(), record.getComment(), record.getJobId(), record.getCreated_at());

                        }

                    }

                }
            }//try
            catch (Exception e) {

            }
        }//for
        if (insertedCount >= quantityToCreate) {
            return null;
        }
        return null;
    }

    private String[] converttoArray(Set<String> sysIdInChunk) {
        int n = sysIdInChunk.size();
        String arr[] = new String[n];
        int a = 0;
        for (String x : sysIdInChunk) {
            arr[a++] = x;
        }

        String[] sysIdToCreate = arr;
        return sysIdToCreate;
    }

   /* private void insertRecords(List<Sctid> records, BulkJobResponseDto record) {
        String sctid= String.valueOf(records.get(0));
        int seq= Integer.parseInt(String.valueOf(records.get(1)));
        int namespace=Integer.parseInt(String.valueOf(records.get(2)));
        String partitionId= records.get(3);
        sctidRepository.bulkInsert(newSctid, Integer.parseInt(String.valueOf(records.get(1))), records.get(2), records.get(3), records.get(4), records.get(5), records.get(6),records.get(7),records.get(8)
                ,records.get(9)
                ,records.get(10)
                ,records.get(11)
                ,records.get(12)
                );

    }*/


    private List<Sctid> updateJobId(List<Sctid> existingSystemId, Integer jobId) {
        List<Sctid> result = sctidRepository.update(existingSystemId, jobId);
        return result;
    }


    private List<SchemeId> updateJobIdscheme(List<SchemeId> existingSystemId, String scheme,Integer jobId) {
        List<SchemeId> result = schemeIdRepository.update(existingSystemId,scheme, jobId);
        return result;
    }

    private List<Sctid> findExistingSystemId(Map<String, Object> obj1) {
        List<Sctid> result = sctidRepository.findBySystemIdAndNamespace(String.valueOf(obj1.get("systemId")), (Integer) obj1.get("namespace"));
        return result;
    }

    public String computeSctId(BulkJobResponseDto record, Integer seq) {

        var tmpNsp = record.namespace.toString();
        if (tmpNsp == "0") {
            tmpNsp = "";
        }
        var base = seq + tmpNsp + record.getPartitionId();
        var SCTId = base + sctIdHelper.verhoeffCompute(base);
        return SCTId;

    }


}
