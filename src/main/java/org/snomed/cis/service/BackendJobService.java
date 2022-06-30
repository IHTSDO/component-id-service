package org.snomed.cis.service;

import com.google.common.collect.Sets;
import org.json.JSONArray;
import org.json.JSONObject;
import org.snomed.cis.domain.*;
import org.snomed.cis.dto.BulkJobResponseDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.*;
import org.snomed.cis.service.DM.SCTIdDM;
import org.snomed.cis.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BackendJobService {

    @Autowired
    SchemeIdService schemeIdService;

    @Autowired
    @PersistenceContext
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
    @Autowired
    private BulkJobRepository bulkJobRepository;
    @Autowired
    private BulkJobService bulkJobService;

    int chunk = 1000;
    @Autowired
    private SctIdHelper sctIdHelper;

    public List<SchemeId> saveScheme(List<SchemeId> schemeIds, String scheme) throws CisException {
        StringBuffer supdateBuf = new StringBuffer();
        List<SchemeId> resultList = null;
        if (schemeIds.size() > 0) {
            for (SchemeId query : schemeIds) {
                if (query.getSchemeId() != null && query.getScheme() != null) {
                    supdateBuf.append(" ,").append(query).append("=");
                }
            }
        }
        String supdate = supdateBuf.toString();

        if (!"".equalsIgnoreCase(supdate)) {
            supdate = supdate.substring(2);

            StringBuffer sqlBuf = new StringBuffer();
            sqlBuf.append("UPDATE schemid SET ").append(supdate).append(" ,modified_at=now() WHERE scheme=").append(scheme);
            Query genQuery = entityManager.createNativeQuery(sqlBuf.toString(), BulkJob.class);
            resultList = genQuery.getResultList();
            return resultList;

        }
        return resultList;
    }

    public List<BulkJob> save(Map<String, Object> qObj, List<BulkJobResponseDto> bulkJobsRecord) throws CisException {
        List<BulkJob> resultList = new LinkedList<>();

        Optional<Map.Entry<String, Object>> idEntryOpt = qObj.entrySet().stream().filter(e -> "id".equalsIgnoreCase(e.getKey())).findFirst();
        List<Map.Entry<String, Object>> entries = qObj.entrySet().stream().filter(e -> !"id".equalsIgnoreCase(e.getKey())).collect(Collectors.toList());

        if (idEntryOpt.isPresent()) {
            Integer bulkJobId = (Integer) idEntryOpt.get().getValue();
            Optional<BulkJob> bulkJobOpt = bulkJobRepository.findById(bulkJobId);
            if (bulkJobOpt.isPresent()) {
                BulkJob bulkJob = bulkJobOpt.get();
                bulkJob.setModified_at(LocalDateTime.now());
                for (Map.Entry<String, Object> e : entries) {
                    if ("name".equalsIgnoreCase(e.getKey())) {
                        bulkJob.setName((String) e.getValue());
                    } else if ("status".equalsIgnoreCase(e.getKey())) {
                        bulkJob.setStatus((String) e.getValue());
                    } else if ("request".equalsIgnoreCase(e.getKey())) {
                        bulkJob.setRequest((String) e.getValue());
                    } else if ("created_at".equalsIgnoreCase(e.getKey())) {
                        bulkJob.setCreated_at((LocalDateTime) e.getValue());
                    } else if ("modified_at".equalsIgnoreCase(e.getKey())) {
                        bulkJob.setModified_at((LocalDateTime) e.getValue());
                    } else if ("log".equalsIgnoreCase(e.getKey())) {
                        bulkJob.setLog((String) e.getValue());
                    }
                }
                bulkJob = bulkJobRepository.saveAndFlush(bulkJob);
                resultList.add(bulkJob);
            }
        }
        return resultList;
    }

    @Transactional
    public void updateJobStatus(Map<String, Object> jobRecord, BulkJob bulkJobRecord) throws CisException {
        StringBuffer supdate = new StringBuffer("");
        String updateValue;
        Integer count = 0;
        if (jobRecord.size() > 0)
            for (var query :
                    jobRecord.entrySet()) {
                if (!(query.getKey().equalsIgnoreCase("id"))) {
                    if (query.getKey().equalsIgnoreCase("status"))
                        supdate.append(" ,").append(query.getKey()).append("=").append("'").append(query.getValue()).append("'");
                    else
                        supdate.append(" ,").append(query.getKey()).append("=").append(query.getValue());
                }
            }
        if (!supdate.toString().equalsIgnoreCase("")) {
            updateValue = supdate.toString().substring(2);
            StringBuffer sql = new StringBuffer();
            sql.append("UPDATE bulkjob SET ").append(updateValue).append(" ,modified_at=now() WHERE id=")
                    .append(jobRecord.get("id"));
            BulkJob bulkJob = BulkJob.builder().name(bulkJobRecord.getName()).request(bulkJobRecord.getRequest()).
                    modified_at(LocalDateTime.now()).created_at(bulkJobRecord.getCreated_at()).
                    status("1").id((Integer) jobRecord.get("id")).build();
            int resultList = 0;
            try {
                bulkJobRepository.saveAndFlush(bulkJob);
            } catch (Exception e) {
                System.out.println("update error:" + e.getMessage());
            }
        }
    }

    @Scheduled(fixedDelay = 3000)
    public void runner() throws CisException {
        Map<String, String> objQuery1 = new HashMap<String, String>();
        Map<String, Integer> objQuery2 = new HashMap<String, Integer>();
        Map<String, String> objQuery3 = new HashMap<String, String>();
        Map<String, Integer> objQuery4 = new HashMap<String, Integer>();
        Map<String, String> objQuery5 = new HashMap<String, String>();

        Optional<BulkJob> bulkJobsRecord = bulkJobRepository.findTopByStatusOrderByCreated_at("0");
        if (bulkJobsRecord.isPresent()) {
            Map<String, Object> lightJob = new HashMap<String, Object>();
            lightJob.put("id", bulkJobsRecord.get().getId());
            lightJob.put("status", "1");
            bulkJobsRecord.get().setStatus("1");
            updateJobStatus(lightJob, bulkJobsRecord.get());
            processJob(bulkJobsRecord.get());
        } else {
            return;
        }
    }//runner()

    private void processJob(BulkJob record) throws CisException {
        String request = record.getRequest();
        JSONObject requestJson = new JSONObject(request);
        //if Request is not Present in record set Status to 3 and save bulkJob
        boolean isTypePresent = false;
        if (!requestJson.isEmpty() && null != requestJson) {
            if (null != requestJson.get("type")) {
                if (!(requestJson.get("type").toString().isEmpty()) && !(requestJson.get("type").toString().isBlank()))
                    isTypePresent = true;
            }
        }

        if (requestJson.isEmpty() || null == requestJson || !isTypePresent) {
            BulkJob bulkJob = BulkJob.builder().id(record.getId()).status("3")
                    .name(record.getName())
                    .log("Request property is null")
                    .build();
            try {
                bulkJobRepository.save(bulkJob);
            } catch (Exception e) {
                throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        } else {
            requestJson.put("jobId", record.getId());
            BulkJob finalBulkJobStatus = BulkJob.builder().id(record.getId()).build();
            Map<String, Object> lightJob = new HashMap<String, Object>();
            lightJob.put("id", record.getId());

            if (
                    jobType.GENERATE_SCTIDS.equalsIgnoreCase(requestJson.getString("type"))
                            ||
                            ((jobType.RESERVE_SCTIDS).equalsIgnoreCase(requestJson.getString("type")))
            ) {
                JSONArray list = null;
                if (requestJson.has("systemIds") && !("null".equalsIgnoreCase(requestJson.get("systemIds").toString()))
                && requestJson.getJSONArray("systemIds").length()>0)
                    list = (JSONArray) requestJson.get("systemIds");
                Integer quantity = (Integer) requestJson.get("quantity");
                requestJson.put("autoSysId", false);
                if (null == list) {
                    List<String> arrayUuids = new ArrayList<>();
                    for (var i = 0; i < quantity; i++) {
                        arrayUuids.add(sctIdHelper.guid());
                    }
                    requestJson.put("systemIds", arrayUuids);
                    requestJson.put("autoSysId", true);
                }
                if (jobType.GENERATE_SCTIDS.equalsIgnoreCase(requestJson.getString("type")))
                    requestJson.put("action", stateMachine.actions.get("generate"));
                else if ((jobType.RESERVE_SCTIDS).equalsIgnoreCase(requestJson.getString("type")))
                    requestJson.put("action", stateMachine.actions.get("reserve"));
                String result;
                String updatedStatus = null;
                String log = null;
                try {
                    result = generateSctids(requestJson);
                    if (result.equalsIgnoreCase("failure")) {
                        updatedStatus = "3";
                        log = "Sctid Not Processed.";
                    } else {
                        updatedStatus = "2";
                        log = null;
                    }
                } catch (Exception e) {
                    updatedStatus = "3";
                    log = "Sctid Not Processed." + e.getMessage();
                    //lightJob.put("status","3");
                    //lightJob.put("log","Sctid Not Processed."+e.getMessage());
                }
                try {
                    if (jobType.GENERATE_SCTIDS.equalsIgnoreCase(requestJson.getString("type"))) {
                        requestJson.remove("autoSysId");
                        if (requestJson.has("scheme"))
                            requestJson.remove("scheme");
                        if (requestJson.has("additionalJobs"))
                            requestJson.remove("additionalJobs");
                    } else if ((jobType.RESERVE_SCTIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                        if (requestJson.has("expirationDate"))
                            requestJson.remove("autoSysId");
                        if (requestJson.has("expirationDate"))
                            requestJson.remove("expirationDate");
                        if (requestJson.has("comment"))
                            requestJson.remove("comment");
                    }
                    bulkJobRepository.updateBulkJobStatusWithReq(updatedStatus, requestJson.toString(), log, requestJson.getInt("jobId"));
                } catch (Exception e) {
                    System.out.println("exception s:" + e.getMessage());
                }
            } else if ((jobType.REGISTER_SCTIDS).equalsIgnoreCase(requestJson.getString("type"))) {

                String jobFinalStatus = registerSctids(requestJson);
                String updatedStatus = null;
                String log = null;
                if (!jobFinalStatus.equalsIgnoreCase("success")) {
                    updatedStatus = "3";
                    log = "Register Sctid Not Processed.";
                } else {
                    updatedStatus = "2";
                }
                if (requestJson.has("comment"))
                    requestJson.remove("comment");
                bulkJobRepository.updateBulkJobStatusWithReq(updatedStatus, requestJson.toString(), log, requestJson.getInt("jobId"));
            } else if ((jobType.DEPRECATE_SCTIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                requestJson.put("action", stateMachine.actions.get("deprecate"));
                String job = updateSctids(requestJson);
                String updatedStatus = null;
                String log = null;
                if (!job.equalsIgnoreCase("success")) {
                    updatedStatus = "3";
                    log = job;
                } else {
                    updatedStatus = "2";
                }
                bulkJobRepository.updateBulkJobStatus(updatedStatus, log, requestJson.getInt("jobId"));
            } else if ((jobType.RELEASE_SCTIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                requestJson.put("action", stateMachine.actions.get("release"));
                String job = updateSctids(requestJson);
                String updatedStatus = null;
                String log = null;
                if (!job.equalsIgnoreCase("success")) {
                    updatedStatus = "3";
                    log = job;
                } else {
                    updatedStatus = "2";
                }
                bulkJobRepository.updateBulkJobStatus(updatedStatus, log, requestJson.getInt("jobId"));
            } else if ((jobType.PUBLISH_SCTIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                requestJson.put("action", stateMachine.actions.get("publish"));
                String job = updateSctids(requestJson);
                String updatedStatus = null;
                String log = null;
                if (!job.equalsIgnoreCase("success")) {
                    updatedStatus = "3";
                    log = job;
                } else {
                    updatedStatus = "2";
                }
                bulkJobRepository.updateBulkJobStatus(updatedStatus, log, requestJson.getInt("jobId"));
            } else if ((jobType.GENERATE_SCHEMEIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                JSONArray list = (JSONArray) requestJson.get("systemIds");
                Integer quantity = (Integer) requestJson.get("quantity");
                requestJson.put("autoSysId", false);
                if (null == list) {
                    List<String> arrayUuids = new ArrayList<>();
                    for (var i = 0; i < quantity; i++) {
                        arrayUuids.add(sctIdHelper.guid());
                    }
                    requestJson.put("systemIds", arrayUuids);
                    requestJson.put("autoSysId", true);
                }
                requestJson.put("action", stateMachine.actions.get("generate"));
                String job = generateSchemeIds(requestJson);
                String updatedStatus = null;
                String log = null;
                if (!job.equalsIgnoreCase("success")) {
                    updatedStatus = "3";
                    log = job;
                } else {
                    updatedStatus = "2";
                }
                requestJson.remove("autoSysId");
                bulkJobRepository.updateBulkJobStatusWithReq(updatedStatus, requestJson.toString(), log, requestJson.getInt("jobId"));
            } else if ((jobType.REGISTER_SCHEMEIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                String job = registerSchemeIds(requestJson);
                String updatedStatus = null;
                String log = null;
                if (!job.equalsIgnoreCase("success")) {
                    updatedStatus = "3";
                    log = job;
                } else {
                    updatedStatus = "2";
                }
                bulkJobRepository.updateBulkJobStatus(updatedStatus, log, requestJson.getInt("jobId"));
            } else if ((jobType.RESERVE_SCHEMEIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                JSONArray list = null;
                if (requestJson.has("systemIds") && !("null".equalsIgnoreCase(requestJson.get("systemIds").toString()))
                        && requestJson.getJSONArray("systemIds").length()>0)
                    list = requestJson.getJSONArray("systemIds");
                Integer quantity = (Integer) requestJson.get("quantity");
                requestJson.put("autoSysId", false);
                if (null == list) {
                    List<String> arrayUuids = new ArrayList<>();
                    for (var i = 0; i < quantity; i++) {
                        arrayUuids.add(sctIdHelper.guid());
                    }
                    requestJson.put("systemIds", arrayUuids);
                    requestJson.put("autoSysId", true);
                }
                requestJson.put("action", stateMachine.actions.get("reserve"));
                String job = generateSchemeIdSmallRequest(requestJson);
                String updatedStatus = null;
                String log = null;
                if (!job.equalsIgnoreCase("success")) {
                    //lightJob.put("status", "3");
                    updatedStatus = "3";
                    log = job;
                } else {
                    updatedStatus = "2";
                }
                bulkJobRepository.updateBulkJobStatus(updatedStatus, log, requestJson.getInt("jobId"));
            } else if ((jobType.DEPRECATE_SCHEMEIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                requestJson.put("action", stateMachine.actions.get("deprecate"));
                String job = updateSchemeId(requestJson);
                String updatedStatus = null;
                String log = null;
                if (!job.equalsIgnoreCase("success")) {
                    updatedStatus = "3";
                    log = job;
                } else {
                    updatedStatus = "2";
                }
                bulkJobRepository.updateBulkJobStatus(updatedStatus, log, requestJson.getInt("jobId"));
            } else if ((jobType.RELEASE_SCHEMEIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                requestJson.put("action", stateMachine.actions.get("release"));
                String updatedStatus = null;
                String log = null;
                String job = updateSchemeId(requestJson);
                if (!job.equalsIgnoreCase("success")) {
                    updatedStatus = "3";
                    log = job;
                } else {
                    updatedStatus = "2";
                }
                bulkJobRepository.updateBulkJobStatus(updatedStatus, log, requestJson.getInt("jobId"));
            } else if ((jobType.PUBLISH_SCHEMEIDS).equalsIgnoreCase(requestJson.getString("type"))) {
                requestJson.put("action", stateMachine.actions.get("publish"));
                String job = updateSchemeId(requestJson);
                String updatedStatus = null;
                String log = null;
                if (!job.equalsIgnoreCase("success")) {
                    updatedStatus = "3";
                    log = job;
                } else {
                    updatedStatus = "2";
                }
                bulkJobRepository.updateBulkJobStatus(updatedStatus, log, requestJson.getInt("jobId"));
            }


        }
    }

    private String updateSchemeId(JSONObject record) throws CisException {
        String scheme = record.getString("scheme");
        JSONArray schemeIdArr = record.getJSONArray("schemeIds");
        List<SchemeId> records = new ArrayList<>();
        for (int i = 0; i < schemeIdArr.length(); i++) {
            String schemeId = schemeIdArr.getString(i);
            SchemeId schemeIdRecord = getSchemeId(scheme, schemeId, "");
            if (null != schemeIdRecord) {
                String newStatus = stateMachine.getNewStatus(schemeIdRecord.getStatus(), record.getString("action"));
                if (null != newStatus) {
                    schemeIdRecord.setStatus(newStatus);
                    schemeIdRecord.setAuthor(record.getString("author"));
                    schemeIdRecord.setSoftware(record.getString("software"));
                    schemeIdRecord.setComment(record.getString("comment"));
                    schemeIdRecord.setJobId(record.getInt("jobId"));
                    records.add(schemeIdRecord);
                } else {
                    return ("Cannot " + record.getString("action") + " SchemeId:" + schemeIdRecord.getSchemeId() + ", current status: " + schemeIdRecord.getStatus());
                }
            } else {
                return "error Fetching SchemeId Record with Scheme:" + scheme + " and schemeId:" + schemeId;
            }
        }
        try {
            schemeIdRepository.saveAll(records);
        } catch (Exception e) {
            return "Exception while saving schemeRecords:" + e.getMessage();
        }

        return "success";
    }

    private String registerSchemeIds(JSONObject record) {
        try {
            var cont = 0;
            List<SchemeId> records = new ArrayList<>();
            var error = false;
            var scheme = record.getString("scheme");
            int quantityToRegister = record.getJSONArray("records").length();
            for (var i = 0; i < quantityToRegister; i++) {
                JSONArray records1 = record.getJSONArray("records");
                JSONObject schemeWithSystmId = records1.getJSONObject(i);
                String schemeId = schemeWithSystmId.getString("schemeId");
                String systemId = schemeWithSystmId.getString("systemId");
                var schemeIdRecord = getSchemeId(scheme, schemeId, systemId);
                String newStatus;
                if (null == schemeIdRecord) {
                    return "Invalid Scheme";
                }
                if (schemeIdRecord.getStatus().equalsIgnoreCase(stateMachine.statuses.get("assigned"))) {
                    newStatus = stateMachine.statuses.get("assigned");
                } else {
                    newStatus = stateMachine.getNewStatus(schemeIdRecord.getStatus(), stateMachine.actions.get("register"));
                }
                if (newStatus != null && !newStatus.isEmpty()) {
                    schemeIdRecord.setStatus(newStatus);
                    schemeIdRecord.setAuthor(record.getString("author"));
                    schemeIdRecord.setSoftware(record.getString("software"));
                    schemeIdRecord.setExpirationDate((record.has("expirationDate")) ? (LocalDateTime) record.get("expirationDate") : null);
                    schemeIdRecord.setComment(record.getString("comment"));
                    schemeIdRecord.setJobId(record.getInt("jobId"));
                    schemeIdRecord.setModified_at(LocalDateTime.now());
                    records.add(schemeIdRecord);
                } else {
                    return ("Cannot register SchemeId:" + schemeIdRecord.getSchemeId() + ", current status: " + schemeIdRecord.getStatus());
                }
            }
            schemeIdRepository.saveAll(records);
        } catch (Exception e) {
            return (e.getMessage());
        }
        return "success";
    }

    private void generateScheme(JSONObject record, Optional<SchemeIdBase> thisScheme) {
        try {
            var rec = setAvailableSchemeIdRecord2NewStatus(record, thisScheme);
            if (rec == null) {
                setNewSchemeIdRecord(record, thisScheme);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String setNewSchemeIdRecord(JSONObject record, Optional<SchemeIdBase> thisScheme) throws CisException {

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
            String systemId = record.has("systemId") ? record.getString("systemId") : "";
            var action = record.getString("action");
            //JSONArray systemIdsList = record.getJSONArray("systemIds");
            //List<String> systemIdsList = (List<String>) record.get("systemIds");
           /* if (systemIdsList != null && systemIdsList.length()>0) {
                systemId = systemIdsList.get(0);
            }*/
            var schemeIdRecord = getSchemeId(scheme, newSchemeId, systemId);

            var newStatus = stateMachine.getNewStatus(schemeIdRecord.getStatus(), action);
            LocalDateTime expirationDateTime = null;
            if (record.has("expirationDate") && !record.getString("expirationDate").equalsIgnoreCase("null") && record.get("expirationDate") != null) {
                String str = record.getString("expirationDate");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                expirationDateTime = LocalDate.parse(str, formatter).atStartOfDay();
            }
            if (newStatus != null) {
                schemeIdRecord.setStatus(newStatus);
                schemeIdRecord.setAuthor(record.getString("author"));
                schemeIdRecord.setSoftware(record.getString("software"));
                schemeIdRecord.setExpirationDate(expirationDateTime);
                schemeIdRecord.setComment(record.getString("comment"));
                schemeIdRecord.setJobId(record.getInt("jobId"));
                schemeIdRepository.save(schemeIdRecord);
                //saveScheme((List<SchemeId>) schemeIdRecord, record.getScheme());
            } else {
                setNewSchemeIdRecord(record, thisScheme);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
        return "success";
    }

    private SchemeId getSchemeId(String scheme, String schemeId, String systemId) throws CisException, CisException {
        boolean isValidScheme = false;
        SchemeId id = null;
        if ("SNOMEDID".equalsIgnoreCase(scheme.toString().toUpperCase())) {
            isValidScheme = SNOMEDID.validSchemeId(schemeId);
        } else if ("CTV3ID".equalsIgnoreCase(scheme.toString().toUpperCase())) {
            isValidScheme = CTV3ID.validSchemeId(schemeId);
        }
        if (isValidScheme) {
            Optional<SchemeId> schemeId1 = schemeIdRepository.findBySchemeAndSchemeId(scheme, schemeId);
            if (!schemeId1.isPresent()) {
                id = getFreeRecords(scheme, schemeId, systemId);
            } else {
                id = schemeId1.get();
            }
        } else {
            id = null;
        }
        return id;
    }

    public SchemeId getFreeRecords(String schemeName, String schemeid, String systemId) throws CisException {
        Map<String, Object> schemeIdRecord = getNewRecord(schemeName, schemeid, systemId);
        schemeIdRecord.put("status", stateMachine.statuses.get("available"));
        return schemeIdService.insertSchemeIdRecord(schemeIdRecord);
    }

    private Map<String, Object> getNewRecord(String schemeName, String schemeid, String systemId) {
        Map<String, Object> schemeIdRecord = new LinkedHashMap<>();
        schemeIdRecord.put("scheme", schemeName);
        schemeIdRecord.put("schemeId", schemeid);
        schemeIdRecord.put("sequence", null);
        schemeIdRecord.put("checkDigit", null);
        schemeIdRecord.put("systemId", (!(systemId.isBlank())) ? systemId : sctIdHelper.guid());
        return schemeIdRecord;
    }

    private String setAvailableSchemeIdRecord2NewStatus(JSONObject generationData, Optional<SchemeIdBase> thisScheme) {
        SchemeId schemeOutput = new SchemeId();
        List<SchemeId> schemeList = new ArrayList<>();
        Map<String, Object> queryObject = new HashMap<>();
        queryObject.put("scheme", "'" + thisScheme.get().getScheme() + "'");
        queryObject.put("status", "'" + stateMachine.statuses.get("available") + "'");
        try {
            schemeList = findSchemeIdWithIndexAndLimit(queryObject, "1", null);
            if (schemeList.size() > 0) {
                var newStatus = stateMachine.getNewStatus(schemeList.get(0).getStatus(), generationData.getString("action"));
                if (!newStatus.isBlank()) {
                    LocalDateTime expirationDateTime = null;
                    if (generationData.has("expirationDate") && !generationData.getString("expirationDate").equalsIgnoreCase("null") && generationData.get("expirationDate") != null) {
                        String str = generationData.getString("expirationDate");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        expirationDateTime = LocalDate.parse(str, formatter).atStartOfDay();
                    }
                    schemeList.get(0).setSystemId(generationData.getString("systemId"));
                    schemeList.get(0).setStatus(newStatus);
                    schemeList.get(0).setAuthor(generationData.getString("author"));
                    schemeList.get(0).setSoftware(generationData.getString("software"));
                    //Doubt - need to be clarified- there is no ExpirationDate in Request body.
                    schemeList.get(0).setExpirationDate(expirationDateTime);
                    schemeList.get(0).setComment(generationData.getString("comment"));
                    schemeList.get(0).setJobId(generationData.getInt("jobId"));
                    schemeList.get(0).setModified_at(LocalDateTime.now());
                    schemeOutput = schemeIdRepository.save(schemeList.get(0));
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "success";
    }

    public List<SchemeId> findSchemeIdWithIndexAndLimit(Map<String, Object> queryObject, String limit, String skip) {
        List<SchemeId> schemeIdList;
        var limitR = 100;
        var skipTo = 0;
        if (!limit.isEmpty() && null != limit)
            limitR = Integer.parseInt(limit);
        if (null != skip)
            skipTo = Integer.parseInt(skip);
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
            sql = "Select * FROM schemeid " + swhere + " order by schemeid limit " + limit;
        } else {
            sql = "Select * FROM sctid " + swhere + " order by schemeid";
        }
        Query genQuery = entityManager.createNativeQuery(sql, SchemeId.class);
        System.out.println("genQuery:" + genQuery);
        List<SchemeId> resultList = (List<SchemeId>) genQuery.getResultList();
        if ((skipTo == 0)) {
            schemeIdList = resultList;
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
            schemeIdList = newRows;
        }
        return schemeIdList;
    }


    //
    private String generateSchemeIds(JSONObject record) {
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("scheme", record.getString("scheme"));
        Set<String> sysIdInChunk = new HashSet<String>();
        int insertedCount = 0;
        int quantityToCreate = record.getInt("quantity");
        JSONArray systemIdsList = (JSONArray) record.get("systemIds");
        for (int i = 1; i <= quantityToCreate; i++) {

            if (sysIdInChunk.contains(systemIdsList.get(i - 1))) {
                quantityToCreate--;
            } else {
                sysIdInChunk.add((String) systemIdsList.get(i - 1));
            }

            try {
                if (i % chunk == 0 || i == (quantityToCreate)) {
                    Set<String> diff = new HashSet<String>();
                    boolean allExisting = false;
                    String[] sysIdToCreate = converttoArray(sysIdInChunk);
                    if (!record.getBoolean("autoSysId")) {
                        List<SchemeId> existingSystemId = schemeIdRepository.findBySchemeAndSchemeIdIn(record.getString("scheme"), List.of(sysIdToCreate));

                        if (existingSystemId.size() > 0) {
                            //update
                            updateJobIdscheme(existingSystemId, record.getString("scheme"), record.getInt("jobId"));
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
                        int n1 = diff.size();
                        String differ[] = new String[n1];
                        if (diff.size() > 0) {
                            sysIdToCreate = diff.toArray(differ);
                        }

                        Optional<SchemeIdBase> data = schemeIdBaseRepository.findById(record.getString("scheme"));
                        if (data.isEmpty()) {
                            return "Scheme not found for key:" + record.getString("scheme");
                        }

                        var previousCode = data.get().getIdBase();

                        List<SchemeId> records = new ArrayList<>();
                        LocalDateTime createAt = LocalDateTime.now();

                        for (String systemId : sysIdToCreate) {
                            String newSchemeId;
                            if (record.getString("scheme").toUpperCase().equalsIgnoreCase("SNOMEDID"))
                                newSchemeId = SNOMEDID.getNextId(previousCode);
                            else
                                newSchemeId = CTV3ID.getNextId(previousCode);
                            LocalDateTime expirationDateTime = null;
                            if (record.has("expirationDate") && !record.getString("expirationDate").equalsIgnoreCase("null") && record.get("expirationDate") != null) {
                                String str = record.getString("expirationDate");
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                expirationDateTime = LocalDate.parse(str, formatter).atStartOfDay();
                            }
                            SchemeId schemeId = SchemeId.builder()
                                    .scheme(record.getString("scheme"))
                                    .schemeId(newSchemeId)
                                    .sequence(null)
                                    .checkDigit(null)
                                    .systemId(systemId)
                                    .status(stateMachine.statuses.get("assigned"))
                                    .author(record.getString("author"))
                                    .software(record.getString("software"))
                                    .expirationDate(expirationDateTime)
                                    .comment(record.getString("comment"))
                                    .jobId(record.getInt("jobId"))
                                    .created_at(createAt)
                                    .build();
                            records.add(schemeId);
                            previousCode = newSchemeId;
                        }
                        data.get().setIdBase(previousCode);
                        schemeIdBaseRepository.save(data.get());
                        insertedCount += records.size();
                        schemeIdRepository.saveAll(records);
                    }
                    sysIdInChunk.clear();
                }
            }//try
            catch (Exception e) {
                return "generateSchemeIds error:" + e.getMessage();
            }
        }//for
        if (insertedCount >= quantityToCreate) {
            return "success";
        } else
            return "failure";
    }


    private String updateSctids(JSONObject record) throws CisException {
        List<Sctid> records = new ArrayList<>();
        boolean error = false;
        JSONArray sctidList = record.getJSONArray("sctids");
        for (var i = 0; i < sctidList.length(); i++) {
            var sctId = sctidList.get(i);
            Optional<Sctid> sctIdRecord;
            try {
                sctIdRecord = sctidRepository.findById(String.valueOf(sctId));
            } catch (Exception e) {
                throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
            if (sctIdRecord.isPresent()) {
                String newStatus = stateMachine.getNewStatus(sctIdRecord.get().getStatus(), record.getString("action"));
                if (newStatus != null) {
                    sctIdRecord.get().setStatus(newStatus);
                    sctIdRecord.get().setAuthor(record.getString("author"));
                    sctIdRecord.get().setSoftware(record.getString("software"));
                    sctIdRecord.get().setExpirationDate(null);
                    sctIdRecord.get().setComment(record.getString("comment"));
                    sctIdRecord.get().setJobId(record.getInt("jobId"));
                    records.add(sctIdRecord.get());
                } else {
                    error = true;
                    return ("Cannot " + record.getString("action") + " SCTID:" + sctIdRecord.get().getSctid() + ", current status: " + sctIdRecord.get().getStatus());
                }
            }
        }
        try {
            sctidRepository.saveAllAndFlush(records);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "success";
    }


    private String registerSctids(JSONObject record) throws CisException {

        var newStatus = stateMachine.getNewStatus(stateMachine.statuses.get("available"), stateMachine.actions.get("register"));

        Set<String> sctIdInChunk = new HashSet<String>();
        int insertedCount = 0;
        int quantityToRegister = record.getJSONArray("records").length();
        Map<String, String> uuidsMap = new HashMap<>();

        for (var i = 1; i <= quantityToRegister; i++) {
            JSONArray records1 = record.getJSONArray("records");
            JSONObject sctWithSystmId = records1.getJSONObject(i - 1);
            String sctid = sctWithSystmId.getString("sctid");
            String systemId = sctWithSystmId.getString("systemId");
            if (sctIdInChunk.contains(sctid)) {
                quantityToRegister--;
            } else {
                sctIdInChunk.add(sctid);
                uuidsMap.put(sctid, systemId);
            }

            try {
                if (i % chunk == 0 || i == (quantityToRegister)) {
                    Set<String> diff = new HashSet<String>();
                    var sctIdToRegister = converttoArray(sctIdInChunk);
                    var allExisting = false;

                    List<Sctid> existingSctIds = findExistingSctIds(sctIdToRegister);

                    if (null != existingSctIds && !existingSctIds.isEmpty() && existingSctIds.size() > 0) {
                        List<String> sctidList = new ArrayList<>();
                        for (Sctid sctidFullObj :
                                existingSctIds) {
                            sctidList.add(sctidFullObj.getSctid());
                        }
                        updateRegisterStatusAndJobId(sctidList, record.getInt("jobId"));
                        if (existingSctIds.size() < sctIdInChunk.size()) {
                            var setExistSctId = existingSctIds.stream().collect(Collectors.toSet());
                            diff = Sets.difference(sctIdInChunk, setExistSctId);
                            insertedCount += setExistSctId.size();
                        } else {
                            insertedCount += existingSctIds.size();
                            allExisting = true;
                        }
                    }
                    if (!allExisting) {
                        int n1 = diff.size();
                        String differ[] = new String[n1];
                        if (diff.size() > 0) {
                            sctIdToRegister = diff.toArray(differ);
                        }
                        List<Sctid> records = new ArrayList<>();
                        var createAt = LocalDateTime.now();

                        for (String sid : sctIdToRegister) {
                            Sctid sctidInsert = Sctid.builder()
                                    .sctid(sid)
                                    .sequence(sctIdHelper.getSequence(sid))
                                    .namespace(record.getInt("namespace"))
                                    .partitionId(sctIdHelper.getPartition(sid))
                                    .checkDigit(sctIdHelper.getCheckDigit(sid))
                                    .systemId(uuidsMap.get(sid))
                                    .status(newStatus)
                                    .author(record.getString("author"))
                                    .software(record.getString("software"))
                                    .expirationDate(null != record.get("expirationDate") ? (LocalDateTime) record.get("expirationDate") : null)
                                    .comment(record.getString("comment"))
                                    .jobId(record.getInt("jobId"))
                                    .created_at(createAt)
                                    .build();
                            records.add(sctidInsert);
                        }
                        insertedCount += records.size();
                        sctidRepository.saveAll(records);
                    }
                    sctIdInChunk.clear();
                    uuidsMap.clear();
                }

            } catch (Exception e) {
                throw new CisException(HttpStatus.BAD_REQUEST, "registerSctids error: " + e.getMessage());
            }

        }
        if (insertedCount >= quantityToRegister) {
            return "success";
        } else
            return "failure";
    }

    private Integer updateRegisterStatusAndJobId(List<String> existingSctIds, Integer jobId) {
        return sctidRepository.updateSctid(existingSctIds, jobId);
    }

    private List<Sctid> findExistingSctIds(String[] sctIdToRegister) {
        List<Sctid> sctid = sctidRepository.findBySctidIn(List.of(sctIdToRegister));
        return sctid;
    }


    private void generateSctid(JSONObject record, Partitions thisPartition) {
        try {
            var rec = setAvailableSCTIDRecord2NewStatus(record);
            if (rec == null) {
                setNewSCTIdRecord(record, thisPartition);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Sctid setNewSCTIdRecord(JSONObject generationData, Partitions thisPartition) throws CisException {
        int sequence = thisPartition.getSequence() + 1;

        var newSCTId = computeSctId(generationData, sequence);
        var action = stateMachine.actions.get("generate");
        String systemId = null;
        if (generationData.get("systemId") == null && !generationData.get("systemId").toString().isEmpty()) {
            systemId = (String) generationData.get("systemId");
        }
        Sctid sctIdRecord = getSctid(newSCTId, systemId);

        var newStatus = stateMachine.getNewStatus(sctIdRecord.getStatus(), action);
        if (newStatus != null) {
            sctIdRecord.setStatus(newStatus);
            sctIdRecord.setAuthor((String) generationData.get("author"));
            sctIdRecord.setSoftware((String) generationData.get("software"));
            sctIdRecord.setExpirationDate((LocalDateTime) generationData.get("expirationDate"));
            sctIdRecord.setComment((String) generationData.get("comment"));
            sctIdRecord.setJobId((Integer) generationData.get("jobId"));
            sctidRepository.save(sctIdRecord);
        } else {
            setNewSCTIdRecord(generationData, thisPartition);
        }
        return sctIdRecord;
    }

    private Sctid getSctid(String newSCTId, String systemId) {
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

    public Sctid setAvailableSCTIDRecord2NewStatus(JSONObject generationData) throws CisException {
        Sctid sctOutput = new Sctid();
        List<Sctid> sctList = new ArrayList<>();
        Map<String, Object> queryObject = new HashMap<>();
        if (null != generationData.get("namespace") && null != generationData.get("partitionId") && !(generationData.get("partitionId").toString().isEmpty())) {
            queryObject.put("namespace", generationData.get("namespace"));
            queryObject.put("partitionId", "'" + generationData.get("partitionId") + "'");
            queryObject.put("status", "'" + stateMachine.statuses.get("available") + "'");
            sctList = sctidService.findSctWithIndexAndLimit(queryObject, "1", null);
            if (sctList.size() > 0) {
                var newStatus = stateMachine.getNewStatus(sctList.get(0).getStatus(), stateMachine.actions.get("generate"));
                if (!newStatus.isBlank()) {
                    if (null != generationData.get("systemId") && !generationData.get("systemId").toString().isEmpty()) {
                        sctList.get(0).setSystemId(String.valueOf(generationData.get("systemId")));
                    }
                    sctList.get(0).setStatus(newStatus);
                    sctList.get(0).setAuthor((String) generationData.get("author"));
                    sctList.get(0).setSoftware((String) generationData.get("software"));
                    //Doubt - need to be clarified- there is no ExpirationDate in Request body.
                    sctList.get(0).setExpirationDate(null != generationData.get("expirationDate") ? (LocalDateTime) generationData.get("expirationDate") : null);
                    sctList.get(0).setComment((String) generationData.get("comment"));
                    sctList.get(0).setJobId(null);
                    sctList.get(0).setModified_at(null != generationData.get("modified_at") ? (LocalDateTime) generationData.get("modified_at") : LocalDateTime.parse(""));
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

    private String generateSctids(JSONObject record) throws CisException {
        List<Sctid> insertedRecords = new ArrayList<>();
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("namespace", record.get("namespace"));
        obj.put("partitionId", record.get("partitionId"));
        var newStatus = stateMachine.getNewStatus(stateMachine.statuses.get("available"), record.getString("action"));
        Set<String> sysIdInChunk = new HashSet<String>();
        int insertedCount = 0;
        int quantityToCreate = (Integer) record.get("quantity");
        JSONArray systemIdsList = (JSONArray) record.get("systemIds");

        for (int i = 1; i <= quantityToCreate; i++) {

            if (sysIdInChunk.contains(systemIdsList.get(i - 1))) {
                quantityToCreate--;
            } else {
                sysIdInChunk.add((String) systemIdsList.get(i - 1));
            }

            try {
                if (i % chunk == 0 || i == ((Integer) record.get("quantity"))) {
                    Set<String> diff = new HashSet<String>();
                    boolean allExisting = false;
                    String[] sysIdToCreate = converttoArray(sysIdInChunk);
                    if (record.getBoolean("autoSysId") == false) {

                        Map<String, Object> obj1 = new HashMap<>();
                        obj1.put("systemIds", sysIdToCreate);
                        obj1.put("namespace", record.get("namespace"));
                        List<String> existingSystemId = findExistingSystemId(obj1, sysIdToCreate);

                        if (existingSystemId.size() > 0) {
                            //update
                            updateJobId(existingSystemId, (Integer) record.get("jobId"));
                            if (existingSystemId.size() < sysIdInChunk.size()) {

                                Set<String> setExistSysId = existingSystemId.stream().collect(Collectors.toSet());
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
                        if (diff.size() > 0) {
                            sysIdToCreate = diff.toArray(differ);
                        }
                        Integer seq = null;
                        // PL TO DO
                        Optional<Partitions> part = null;
                        try {
                            part = partitionsRepository.findByNamespacePartition((Integer) record.get("namespace"), (String) record.get("partitionId"));
                        } catch (Exception e) {
                            System.out.println("error:" + e.getMessage());
                        }
                        if (part.isPresent()) {
                            seq = part.get().getSequence();
                            int updatedSeq = seq + sysIdToCreate.length;
                            part.get().setSequence(updatedSeq);
                            Partitions partitions = Partitions.builder().namespace(part.get().getNamespace()).partitionId(part.get().getPartitionId()).sequence(updatedSeq).build();
                            Partitions newPart = partitionsRepository.save(partitions);
                        }
                        if (seq == null || !(part.isPresent())) {
                            throw new CisException(HttpStatus.BAD_REQUEST, "Partition not found for partitionId:" + (String) record.get("partitionId") + " and namespace:" + (Integer) record.get("namespace"));
                        }

                        List<Sctid> records = new ArrayList<>();
                        var createAt = LocalDateTime.now();

                        for (String systemId : sysIdToCreate) {
                            seq++;
                            String newSctid = computeSctId(record, seq);
                            LocalDateTime expirationDateTime = null;
                            if (record.has("expirationDate") && !record.getString("expirationDate").equalsIgnoreCase("null") && record.get("expirationDate") != null) {
                                String str = record.getString("expirationDate");
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                expirationDateTime = LocalDate.parse(str, formatter).atStartOfDay();
                            }
                            Sctid rec = Sctid.builder().sctid(newSctid).sequence(seq).
                                    namespace(record.getInt("namespace")).
                                    partitionId(record.getString("partitionId")).
                                    checkDigit(sctIdHelper.getCheckDigit(newSctid)).
                                    systemId(systemId).
                                    status(newStatus).
                                    author(record.getString("author")).
                                    software(record.getString("software")).
                                    expirationDate(expirationDateTime).
                                    comment(record.getString("comment")).
                                    jobId(record.getInt("jobId")).
                                    created_at(createAt).
                                    build();
                            records.add(rec);
                        }
                        insertedCount += records.size();
                        insertedRecords = sctidRepository.saveAll(records);
                    }
                    sysIdInChunk.clear();
                }
            }//try
            catch (Exception e) {
                throw new CisException(HttpStatus.BAD_REQUEST, "generateSctids error:" + e.getMessage());
            }
        }//for
        if (insertedCount >= quantityToCreate)
            return "success";
        else
            return "failure";
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

    private void updateJobId(List<String> existingSystemId, Integer jobId) {
        try {
            sctidRepository.updateJobIdInSctid(jobId, existingSystemId);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private List<SchemeId> updateJobIdscheme(List<SchemeId> existingSystemId, String scheme, Integer jobId) {
        List<SchemeId> result = schemeIdRepository.update(existingSystemId, scheme, jobId);
        return result;
    }

    private List<String> findExistingSystemId(Map<String, Object> obj1, String[] sysIdToCreate) {
        List<String> sysIdList = new ArrayList<>();
        for (int i = 0; i < sysIdToCreate.length; i++) {
            sysIdList.add(sysIdToCreate[i]);
        }
        List<String> result = sctidRepository.getSystemIdByNamespace(sysIdList, (Integer) obj1.get("namespace"));
        return result;
    }

    public String computeSctId(JSONObject record, Integer seq) {
        var tmpNsp = String.valueOf(record.getInt("namespace"));
        if (tmpNsp.equalsIgnoreCase("0")) {
            tmpNsp = "";
        }
        var base = seq + tmpNsp + record.get("partitionId");
        var SCTId = base + sctIdHelper.verhoeffCompute(base);
        return SCTId;

    }

    private String generateSchemeIdSmallRequest(JSONObject record) {
        var cont = 0;
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("scheme", record.getString("scheme"));
        Optional<SchemeIdBase> data = schemeIdBaseRepository.findById(record.getString("scheme"));
        var quantityToCreate = record.getInt("quantity");
        if (data.isEmpty()) {
            return "Scheme not found for key:" + record.getString("scheme");
        }
        var thisScheme = data;
        boolean canContinue;
        for (var i = 0; i < quantityToCreate; i++) {
            canContinue = true;
            try {
                JSONArray systemIdList = null;
                if (record.has("systemIds"))
                    systemIdList = record.getJSONArray("systemIds");
                record.put("systemId", systemIdList.getString(i));
                if (systemIdList.length() > 0) {
                    if (!record.getBoolean("autoSysId")) {
                        var schemeIdRecord = schemeIdRepository.findBySchemeAndSystemId(record.getString("scheme"), systemIdList.getString(i));
                        if (schemeIdRecord != null && schemeIdRecord.size() > 0) {
                            schemeIdRecord.get(0).setJobId(record.getInt("jobId"));
                            schemeIdRecord.get(0).setModified_at(LocalDateTime.now());
                            schemeIdRepository.save(schemeIdRecord.get(0));
                            //saveScheme(schemeIdRecord, record.getString("scheme"));
                            canContinue = false;
                        }
                    }
                    if (canContinue) {
                        generateScheme(record, thisScheme);
                    }
                    cont++;
                    if (record.getInt("quantity") == cont) {
                        schemeIdBaseRepository.save(thisScheme.get());
                    }
                }
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        return "success";
    }

}
