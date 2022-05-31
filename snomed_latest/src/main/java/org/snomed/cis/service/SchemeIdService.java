package org.snomed.cis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.controller.SecurityController;
import org.snomed.cis.controller.dto.*;
import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeIdBase;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.util.*;
import org.snomed.cis.repository.BulkSchemeIdRepository;
import org.snomed.cis.repository.PermissionsSchemeRepository;
import org.snomed.cis.repository.SchemeIdBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchemeIdService {
    private final Logger logger = LoggerFactory.getLogger(SchemeIdService.class);
    @Autowired
    private SecurityController securityController;

    @Autowired
    private BulkSchemeIdRepository bulkSchemeIdRepository;

    @Autowired
    AuthenticateToken authenticateToken;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private PermissionsSchemeRepository permissionsSchemeRepository;
    @Autowired
    private SchemeIdBaseRepository schemeIdBaseRepository;

    @Autowired
    private SchemeIdHelper schemeIdHelper;

    @Autowired
    private SctIdHelper sctIdHelper;

    @Autowired
    private AuthenticateToken authenticateTokenService;

    @Autowired
    StateMachine stateMachine;
    public boolean isAbleUser(String schemeName, AuthenticateResponseDto authToken) throws CisException {
        List<String> groups = authToken.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
        boolean isAble = false;
        if (groups.contains("component-identifier-service-admin") || hasSchemePermission(schemeName, authToken)) {
            isAble = true;
        }
        return isAble;
    }

    public boolean hasSchemePermission(String schemeName,AuthenticateResponseDto authToken) throws CisException {
        boolean able = false;
        if (!"false".equalsIgnoreCase(schemeName.toString())) {
            List<PermissionsScheme> permissionsSchemeList = permissionsSchemeRepository.findByScheme(schemeName.toString());
            List<String> possibleGroups = new ArrayList<>();
            for (PermissionsScheme perm : permissionsSchemeList) {
                if (("group").equalsIgnoreCase(perm.getRole())) {
                    possibleGroups.add(perm.getUsername());
                } else if ((authToken.getName()).equalsIgnoreCase(perm.getUsername())) {
                    able = true;
                }
            }
            if (!able && possibleGroups.size() > 0) {
                List<String> roleAsGroups;
                try {
                    roleAsGroups = authToken.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
                } catch (Exception e) {
                    logger.error("error hasSchemePermission():: Error accessing groups");
                    throw new CisException(HttpStatus.BAD_REQUEST, "Error accessing groups");
                }
                for (String group : roleAsGroups) {
                    if (possibleGroups.contains(group))
                        able = true;
                }
            }
        }
        return able;
    }

    public UserDTO getAuthenticatedUser() throws CisException {
        return this.securityController.authenticate();
    }

    public List<SchemeId> getSchemeIds(AuthenticateResponseDto authToken, String limit, String skip, SchemeName schemeName) throws CisException {
            return this.getSchemeIdsList(limit, skip, schemeName,authToken);
    }

    private List<SchemeId> getSchemeIdsList(String limit, String skip, SchemeName schemeName,AuthenticateResponseDto authToken) throws CisException {
        List<SchemeId> schemeidList = new ArrayList<>();
        if (this.isAbleUser("false", authToken)) {
            //ArrayList<String> schemeIdsArrayList = new ArrayList<String>(Arrays.asList(schemedIdArray));

            // String[] objQuery = (schemeName.toString()).split(",");
            var limitR = 100;
            var skipTo = 0;
            if (limit != null)
                limitR = Integer.parseInt(limit);
            if (skip != null)
                skipTo = Integer.parseInt(skip);
            Map<String, String> objQuery = new HashMap<String, String>();
            if (null != schemeName) {
                objQuery.put("scheme", schemeName.toString());
            }

            String swhere = "";
            if (objQuery.size() > 0) {
                for (var query :
                        objQuery.entrySet()) {
                    swhere += " And " + query.getKey() + "=" + "'" + (query.getValue()) + "'";
                }
            }
            if (swhere != "") {
                swhere = " WHERE " + swhere.substring(5);
            }
            String sql;
            if ((limitR > 0) && (skipTo == 0)) {
                sql = "Select * FROM schemeid" + swhere + " order by schemeId limit " + limitR;
            } else {
                sql = "Select * FROM schemeid" + swhere + " order by schemeId";
            }
            Query genQuery = entityManager.createNativeQuery(sql, SchemeId.class);

            List<SchemeId> resultList = genQuery.getResultList();

            /*
            * if ((skipTo==0)) {
            schemeList = resultList;
        }else {
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
            schemeList = newRows;
        }
        return schemeList;
            * */
            if ((skipTo == 0)) {
                schemeidList = resultList;
            } else {
                var cont = 1;
                List<SchemeId> newRows = new ArrayList<>();
                for (var i = 0; i < (resultList.size() / 1000); i++) {
                    if (i >= skipTo) {
                        if (limitR > 0 && limitR < cont) {
                            break;
                        }
                        newRows.add(resultList.get(i));
                        cont++;
                    }
                }
                schemeidList = newRows;
            }
        } else {
            logger.error("error getSchemeIdsList():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return schemeidList;
    }

    public SchemeId getSchemeId(AuthenticateResponseDto authToken, SchemeName scheme, String schemeid) throws CisException {
            return this.getSchemeIdsByschemeIdList(scheme.toString(), schemeid);
    }

    public SchemeId getSchemeIdsByschemeIdList(String schemeName, String schemeid) throws CisException {
        SchemeId record = new SchemeId();
        Optional<SchemeId> schemeIdObj = null;
            if (schemeid == null || schemeid == "") {
                logger.error("error getSchemeIdsByschemeIdList():: Not valid schemeId.");
                throw new CisException(HttpStatus.UNAUTHORIZED, "Not valid schemeId.");
            } else {
                boolean isValidScheme = false;
                if ("SNOMEDID".equalsIgnoreCase(schemeName.toString().toUpperCase())) {
                    isValidScheme = SNOMEDID.validSchemeId(schemeid);
                } else if ("CTV3ID".equalsIgnoreCase(schemeName.toString().toUpperCase())) {
                    isValidScheme = CTV3ID.validSchemeId(schemeid);
                }
                if (!isValidScheme) {
                    logger.error("error getSchemeIdsByschemeIdList():: Not valid schemeId.");
                    throw new CisException(HttpStatus.UNAUTHORIZED, "Not valid schemeId.");
                }
            }
            schemeIdObj = bulkSchemeIdRepository.findBySchemeAndSchemeId(schemeName, schemeid);
            if (schemeIdObj.isEmpty()) {
                record = getFreeRecords(schemeName, schemeid);
                return record;
            } else {
                return schemeIdObj.get();
            }
    }

    public SchemeId getFreeRecords(String schemeName, String schemeid) throws CisException {
        Map<String, Object> schemeIdRecord = getNewRecord(schemeName, schemeid);
        schemeIdRecord.put("status", "Available");
        return insertSchemeIdRecord(schemeIdRecord);
    }

    private Map<String, Object> getNewRecord(String schemeName, String schemeid) {
        Map<String, Object> schemeIdRecord = new LinkedHashMap<>();
        schemeIdRecord.put("scheme", schemeName);
        schemeIdRecord.put("schemeId", schemeid);
        schemeIdRecord.put("sequence", sctIdHelper.getSequence(schemeid));
        schemeIdRecord.put("checkDigit", sctIdHelper.getCheckDigit(schemeid));
        schemeIdRecord.put("systemId", sctIdHelper.guid());
        return schemeIdRecord;
    }

    private SchemeId insertSchemeIdRecord(Map<String, Object> schemeIdRecord) throws CisException {
        String error;
        Optional<SchemeId> schemeIdBulk = null;
        String scheme = null;
        String schemeId = null;
        Integer sequence = 0;
        Integer checkDigit = 0;
        String systemId = null;
        String status = null;
        String author = null;
        String software = null;
        LocalDateTime expirationDate = null;
        Integer jobId = 0;
        LocalDateTime created_at = null;
        LocalDateTime modified_at = null;

        Set<Map.Entry<String, Object>> s = schemeIdRecord.entrySet();
        try {
            for (Map.Entry<String, Object> mapObj : s) {
                if (mapObj.getKey() == "scheme") {
                    scheme = (String) mapObj.getValue();
                } else if (mapObj.getKey() == "schemeId") {
                    schemeId = (String) mapObj.getValue();
                } else if (mapObj.getKey() == "sequence") {
                    Object sequenceValue = mapObj.getValue();
                    if (sequenceValue instanceof Integer) {
                        sequence = (Integer) sequenceValue; // 1
                    }
                } else if (mapObj.getKey() == "checkDigit") {
                    Object checkDigitvalue = mapObj.getValue();
                    if (checkDigitvalue instanceof Integer) {
                        checkDigit = (Integer) checkDigitvalue; // 1
                    }
                } else if (mapObj.getKey() == "systemId") {
                    systemId = (String) mapObj.getValue();
                } else if (mapObj.getKey() == "status") {
                    status = (String) mapObj.getValue();
                } else if (mapObj.getKey() == "author") {
                    author = (String) mapObj.getValue();
                } else if (mapObj.getKey() == "software") {
                    software = (String) mapObj.getValue();
                } else if (mapObj.getKey() == "expirationDate") {
                    expirationDate = (LocalDateTime) mapObj.getValue();
                } else if (mapObj.getKey() == "jobId") {
                    //jobId = (Integer) mapObj.getValue();
                    Object jobIdvalue = mapObj.getValue();
                    if (jobIdvalue instanceof Integer) {
                        jobId = (Integer) jobIdvalue; // 1
                    }

                } else if (mapObj.getKey() == "created_at") {
                    Object created_atValue = (LocalDateTime) mapObj.getValue();
                    if (created_atValue instanceof LocalDateTime) {
                        created_at = (LocalDateTime) created_atValue;
                    }
                } else if (mapObj.getKey() == "modified_at") {
                    Object modified_atValue = (LocalDateTime) mapObj.getValue();
                    if (modified_atValue instanceof LocalDateTime) {
                        modified_at = (LocalDateTime) modified_atValue;
                    }
                }
            }
            //refactor changes
            SchemeId schemeIdObj = SchemeId.builder().scheme(scheme).schemeId(schemeId).sequence(sequence).checkDigit(checkDigit).systemId(systemId).status(status).author(author).software(software).expirationDate(expirationDate).jobId(jobId).build();
            //SchemeId schemeIdObj=new SchemeId("SNOMEDID","A-22335", 0, 0, "systemId0op0o0o0k0k0", "Available", null, null, null, null, null, null);
            //refactor changes
           // schemeIdBulk = bulkSchemeIdRepository.findBySchemeAndSchemeId(scheme.toString(), schemeId.toString());
            return bulkSchemeIdRepository.save(schemeIdObj);
        } catch (Exception e) {
            error = e.toString();
        }

        return null;
    }


    public SchemeId getSchemeIdsBySystemId(AuthenticateResponseDto authToken, SchemeName scheme, String systemid) throws CisException {
            return this.getSchemeIdsBysystemList(scheme.toString(), systemid,authToken);
    }

    private SchemeId getSchemeIdsBysystemList(String scheme, String systemid,AuthenticateResponseDto authToken) throws CisException {
        if (isAbleUser(scheme, authToken)) {
            List<SchemeId> schemeIdList = bulkSchemeIdRepository.findBySchemeAndSystemId(scheme, systemid);

            if (!schemeIdList.isEmpty() && schemeIdList.size() > 0) {
                return schemeIdList.get(0);
            } else {
                logger.error("error getSchemeIdsBysystemList()::SchemeId list is empty");
                throw new CisException(HttpStatus.UNAUTHORIZED, "SchemeId list is empty");
            }
        } else {
            logger.error("error getSchemeIdsBysystemList():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }


    }

    public SchemeId deprecateSchemeIds(AuthenticateResponseDto authToken, SchemeName schemeName, SchemeIdUpdateRequestDto request) throws CisException {
            return this.deprecateSchemeIdList(schemeName, request,authToken);
    }

    private SchemeId deprecateSchemeIdList(SchemeName schemeName, SchemeIdUpdateRequestDto request,AuthenticateResponseDto authToken) throws CisException {
        SchemeId schemeId = new SchemeId();
        if (isAbleUser(schemeName.toString(), authToken)) {
            SchemeIdUpdateRequest updateRequest = new SchemeIdUpdateRequest();
            updateRequest.setSchemeId(request.getSchemeId());
            updateRequest.setSoftware(request.getSoftware());
            updateRequest.setComment(request.getComment());
            updateRequest.setAuthor(authToken.getName());
            SchemeId schemeIdrecord = getSchemeIdsByschemeIdList(schemeName.toString(), updateRequest.getSchemeId());
            if (schemeIdrecord.getSchemeId().isEmpty()) {
                logger.error("error deprecateSchemeIdList():: SchemeId record is empty");
                throw new CisException(HttpStatus.NOT_FOUND, "SchemeId record is empty");
            } else {
                var newStatus = stateMachine.getNewStatus(schemeIdrecord.getStatus(), stateMachine.actions.get("deprecate"));
                if (null != newStatus) {
                    schemeIdrecord.setStatus(newStatus);
                    schemeIdrecord.setAuthor(updateRequest.getAuthor());
                    schemeIdrecord.setSoftware(updateRequest.getSoftware());
                    schemeIdrecord.setComment(updateRequest.getComment());
                    schemeIdrecord.setJobId(null);
                    schemeId = bulkSchemeIdRepository.save(schemeIdrecord);
                } else {
                    logger.error("error deprecateSchemeIdList():: Cannot deprecate SchemeId:{}, current status:{}",schemeIdrecord.getSchemeId(),schemeIdrecord.getStatus());
                    throw new CisException(HttpStatus.BAD_REQUEST, "Cannot deprecate SchemeId:" + schemeIdrecord.getSchemeId() + ", current status:" + schemeIdrecord.getStatus());
                }
            }
        } else {
            logger.error("error deprecateSchemeIdList():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
        return schemeId;

    }
    //releaseSchmeId

    public SchemeId releaseSchemeIds(AuthenticateResponseDto authToken, SchemeName schemeName, SchemeIdUpdateRequestDto request) throws CisException {
            return this.releaseSchemeIdList(schemeName, request, authToken);
    }

    private SchemeId releaseSchemeIdList(SchemeName schemeName, SchemeIdUpdateRequestDto request,AuthenticateResponseDto authToken) throws CisException {
        SchemeId schemeId = new SchemeId();
        if (isAbleUser(schemeName.toString(), authToken)) {

            SchemeIdUpdateRequest updateRequest = new SchemeIdUpdateRequest();
            updateRequest.setSchemeId(request.getSchemeId());
            updateRequest.setSoftware(request.getSoftware());
            updateRequest.setComment(request.getComment());
            updateRequest.setAuthor(authToken.getName());
            SchemeId schemeIdrecord = getSchemeIdsByschemeIdList(schemeName.toString(), updateRequest.getSchemeId());

            if (schemeIdrecord.getSchemeId().isEmpty()) {
                logger.error("error releaseSchemeIdList():: SchemeId record is empty");
                throw new CisException(HttpStatus.NOT_FOUND, "SchemeId record is empty");
            } else {
                var newStatus = stateMachine.getNewStatus(schemeIdrecord.getStatus(), stateMachine.actions.get("release"));
                if (null != newStatus) {
                    schemeIdrecord.setStatus(newStatus);
                    schemeIdrecord.setAuthor(updateRequest.getAuthor());
                    schemeIdrecord.setSoftware(updateRequest.getSoftware());
                    schemeIdrecord.setComment(updateRequest.getComment());
                    schemeIdrecord.setJobId(null);
                    schemeId = bulkSchemeIdRepository.save(schemeIdrecord);
                } else {
                    logger.error("error releaseSchemeIdList():: Cannot release SchemeId:{} , current status:{}",schemeIdrecord.getSchemeId(),schemeIdrecord.getStatus());
                    throw new CisException(HttpStatus.BAD_REQUEST, "Cannot release SchemeId:" + schemeIdrecord.getSchemeId() + ", current status:" + schemeIdrecord.getStatus());
                }
            }
        } else {
            logger.error("error releaseSchemeIdList():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return schemeId;
    }

    //publish

    public SchemeId publishSchemeId(AuthenticateResponseDto authToken, SchemeName schemeName, SchemeIdUpdateRequestDto request) throws CisException {
            return this.publishSchemeIdList(schemeName, request,authToken);

    }

    private SchemeId publishSchemeIdList(SchemeName schemeName, SchemeIdUpdateRequestDto request,AuthenticateResponseDto authToken) throws CisException {
        SchemeId schemeId = new SchemeId();
        if (isAbleUser(schemeName.toString(), authToken)) {
            SchemeIdUpdateRequest updateRequest = new SchemeIdUpdateRequest();
            updateRequest.setSchemeId(request.getSchemeId());
            updateRequest.setSoftware(request.getSoftware());
            updateRequest.setComment(request.getComment());
            updateRequest.setAuthor(authToken.getName());
            SchemeId schemeIdrecord = getSchemeIdsByschemeIdList(schemeName.toString(), updateRequest.getSchemeId());

            if (schemeIdrecord.getSchemeId().isEmpty()) {
                logger.error("error publishSchemeIdList():: SchemeId record is empty");
                throw new CisException(HttpStatus.NOT_FOUND, "SchemeId record is empty");
            } else {
                var newStatus = stateMachine.getNewStatus(schemeIdrecord.getStatus(), stateMachine.actions.get("publish"));
                if (null != newStatus) {
                    schemeIdrecord.setStatus(newStatus);
                    schemeIdrecord.setAuthor(updateRequest.getAuthor());
                    schemeIdrecord.setSoftware(updateRequest.getSoftware());
                    schemeIdrecord.setComment(updateRequest.getComment());
                    schemeIdrecord.setJobId(null);
                    schemeId = bulkSchemeIdRepository.save(schemeIdrecord);
                } else {
                    logger.error("error publishSchemeIdList():: Cannot publish SchemeId:{}, current status:{}",schemeIdrecord.getSchemeId(),schemeIdrecord.getStatus());
                    throw new CisException(HttpStatus.BAD_REQUEST, "Cannot publish SchemeId:" + schemeIdrecord.getSchemeId() + ", current status:" + schemeIdrecord.getStatus());
                }
            }
        } else {
            logger.error("error publishSchemeIdList():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return schemeId;
    }

    public SchemeId reserveSchemeId(AuthenticateResponseDto authToken, SchemeName schemeName, SchemeIdReserveRequestDto request) throws CisException {
            return this.reserveSchemeIdList(schemeName, request,authToken);
    }

    private SchemeId reserveSchemeIdList(SchemeName schemeName, SchemeIdReserveRequestDto request,AuthenticateResponseDto authToken) throws CisException {

        if (this.isAbleUser(schemeName.toString(), authToken)) {
            SchemeIdReserveRequest reserveRequest = new SchemeIdReserveRequest();
            reserveRequest.setSoftware(request.getSoftware());
            reserveRequest.setExpirationDate(request.getExpirationDate());
            reserveRequest.setComment(request.getComment());
            reserveRequest.setAuthor(authToken.getName());
            setNewSchemeIdRecord(schemeName, reserveRequest, stateMachine.actions.get("reserve"));
        } else {
            logger.error("error reserveSchemeIdList():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return null;
    }

    //List
    private SchemeId setNewSchemeIdRecord(SchemeName schemeName, SchemeIdReserveRequest request, String reserve) throws CisException {
        SchemeId record = setAvailableSchemeIdRecord2NewStatus(schemeName, request, reserve);
        //TO DO
        try {
            if (record != null) {
                return record;
            } else {
                String  schemeIdRec = counterMode(schemeName, request, reserve);
                if (schemeIdRec != null) {
                    return null;
                } else {
                    logger.error("error setNewSchemeIdRecord():: Error");
                    throw new CisException(HttpStatus.NOT_FOUND, "Error");
                }

            }
        } catch (Exception e) {
            logger.error("error setNewSchemeIdRecord():: error getting available schemeId for:{}, Exception msg: {}",schemeName,e.getMessage());
            throw new CisException(HttpStatus.NOT_FOUND, "error getting available schemeId for:" + schemeName + e.getMessage());
        }

    }

    private SchemeId setAvailableSchemeIdRecord2NewStatus(SchemeName schemeName, SchemeIdReserveRequest request, String reserve) throws CisException {
        Map<String, String> objQuery = new HashMap<String, String>();
        SchemeId outputSchemeRec = new SchemeId();
        SchemeId updatedrecord = null;
        if (null != schemeName) {
            objQuery.put("scheme", schemeName.toString());
            objQuery.put("status", "available");
        }

        List<SchemeId> schemeIdRecords = findschemeRecord(objQuery, "1", null);
        if (schemeIdRecords != null && schemeIdRecords.size() > 0) {

            var newStatus = stateMachine.getNewStatus(schemeIdRecords.get(0).getStatus(), reserve);
            if (null != newStatus) {

                if (null != request.getSystemId() && request.getSystemId().trim() != "") {
                    schemeIdRecords.get(0).setSystemId(request.getSystemId());
                }
                schemeIdRecords.get(0).setStatus(newStatus);
                schemeIdRecords.get(0).setAuthor(request.getAuthor());
                schemeIdRecords.get(0).setSoftware(request.getSoftware());
                //Expiration Date Not available in request.
                schemeIdRecords.get(0).setExpirationDate(null);
                schemeIdRecords.get(0).setComment(request.getComment());
                schemeIdRecords.get(0).setJobId(null);
                // outputSchemeRec = bulkSchemeIdRepository.save(schemeIdRecords.get(0));
                updatedrecord = updateSchemeIdRecord(schemeIdRecords.get(0), schemeName.toString());
                return updatedrecord;
            } else {
                counterMode(schemeName, request, reserve);
            }
        } else {
            return null;
        }
        return updatedrecord;

    }

    public String counterMode(SchemeName schemeName, SchemeIdReserveRequest request, String reserve) throws CisException {
        String newSchemeId = getNextSchemeId(schemeName, request);
        if (newSchemeId != null) {
            SchemeId schemeIdRecord = getSchemeIdsByschemeIdList(schemeName.toString(), newSchemeId.toString());
            SchemeId updatedrecord;
            if (schemeIdRecord != null) {
                var newStatus = stateMachine.getNewStatus(schemeIdRecord.getStatus(), reserve);
                if (null != newStatus) {

                    if (null != request.getSystemId() && request.getSystemId().trim() != "") {
                        schemeIdRecord.setSystemId(request.getSystemId());
                    }
                    schemeIdRecord.setStatus(newStatus);
                    schemeIdRecord.setAuthor(request.getAuthor());
                    schemeIdRecord.setSoftware(request.getSoftware());
                    //Expiration Date Not available in request.
                    schemeIdRecord.setExpirationDate(null);
                    schemeIdRecord.setComment(request.getComment());
                    schemeIdRecord.setJobId(null);
                    // outputSchemeRec = bulkSchemeIdRepository.save(schemeIdRecords.get(0));
                    updatedrecord = updateSchemeIdRecord(schemeIdRecord, schemeName.toString());
                    return updatedrecord.toString();
                } else {
                    counterMode(schemeName, request, reserve);
                }
            }
        }
        return newSchemeId;
    }

    private String getNextSchemeId(SchemeName schemeName, SchemeIdReserveRequest request) {
        Optional<SchemeIdBase> schemeIdBaseList = schemeIdBaseRepository.findByScheme(schemeName.toString());
        SchemeIdBase schemeIdBase = null;
        String nextId = null;

        if(schemeIdBaseList.isPresent()) {
            if (schemeName.toString().toUpperCase().equalsIgnoreCase("SNOMEDID")) {
                if(schemeIdBaseList.isPresent())
                    nextId = SNOMEDID.getNextId(schemeIdBaseList.get().getIdBase());
            }
            else if (schemeName.toString().toUpperCase().equalsIgnoreCase("CTV3ID")) {
                if(schemeIdBaseList.isPresent())
                    nextId = CTV3ID.getNextId(schemeIdBaseList.get().getIdBase());
            }
            schemeIdBase.setIdBase(nextId);
        }
        schemeIdBaseRepository.save(schemeIdBase);
        return nextId;
    }

    public SchemeId updateSchemeIdRecord(SchemeId schemeId, String schemeName) {
        Map<String, String> objQuery = new HashMap<String, String>();

        if (null != schemeId) {
            objQuery.put("schemeId", String.valueOf(schemeId));
            objQuery.put("scheme", schemeName.toString());
        }
        String supdate = "";

        for (var query :
                objQuery.entrySet()) {
            if (!"schemeId".equalsIgnoreCase(query.toString()) && !"schemeName".equalsIgnoreCase(query.toString())) {
                supdate += " ," + query.getKey() + "=" + (query.getValue());
            }

        }
        if (supdate != "") {
            supdate = supdate.substring(2);
        }
        String sql = "UPDATE schemeId SET " + supdate + " ,modified_at=now() WHERE scheme=" + schemeName +
                " And schemeId=" + schemeId;
        Query genQuery = entityManager.createQuery(sql);
        SchemeId resultList = (SchemeId) genQuery.getResultList();
        return resultList;

    }

    public List<SchemeId> findschemeRecord(Map<String, String> objQuery, String limit, String skip) {
        SchemeId schemeidList = null;
        var limitR = 100;
        var skipTo = 0;
        if (limit != null)
            limitR = Integer.parseInt(limit);
        if (skip != null)
            skipTo = Integer.parseInt(skip);
        /*Map<String, String> objQuery = new HashMap<String, String>();
        if (null != schemeName) {
            objQuery.put("schemeName", schemeName.toString());
        }*/

        String swhere = "";
        if (objQuery.size() > 0) {
            for (var query :
                    objQuery.entrySet()) {
                swhere += " And " + query.getKey() + "=" + "'" + query.getValue() + "'";
            }
        }
        if (swhere != "") {
            swhere = " WHERE " + swhere.substring(5);
        }
        String sql;
        if ((limitR > 0) && (skipTo == 0)) {
            sql = "SELECT * FROM schemeId" + swhere + " order by schemeId limit " + limit;
        } else {
            sql = "SELECT * FROM schemeId" + swhere + " order by schemeId";
        }
        Query genQuery = entityManager.createNativeQuery(sql, SchemeId.class);

        List<SchemeId> resultList = genQuery.getResultList();
        if ((skipTo == 0)) {
            return resultList;
        } else {
            var cont = 1;
            List<SchemeId> newRows = new ArrayList<>();
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
        // return schemeidList;
    }


    public SchemeId generateSchemeId(AuthenticateResponseDto authToken, SchemeName schemeName, SchemeIdGenerateRequestDto request) throws CisException {
            return this.generateSchemeIds(schemeName.toString(), request,authToken);
    }

    public SchemeId generateSchemeIds(String schemeName, SchemeIdGenerateRequestDto request,AuthenticateResponseDto authToken) throws CisException {
        SchemeId schemeIdRec = new SchemeId();
        if (this.isAbleUser(schemeName, authToken)) {
            SchemeIdGenerateRequest generateRequest = new SchemeIdGenerateRequest();
            generateRequest.setSystemId(request.getSystemId());
            generateRequest.setSoftware(request.getSoftware());
            generateRequest.setComment(request.getComment());

            if (request.getSystemId().isBlank() || request.getSystemId().trim() == "") {
                generateRequest.setSystemId(sctIdHelper.guid());
                generateRequest.setAutoSysId(true);
            }
            generateRequest.setAuthor(generateRequest.getAuthor());
            if (!generateRequest.isAutoSysId()) {
                schemeIdRec = getSchemeIdBySystemId(schemeName, request.systemId);
                if (schemeIdRec != null) {
                    return schemeIdRec;
                } else {
                    //request body change
                    return setNewSchemeIdRecordGen(schemeName, generateRequest, stateMachine.actions.get("generate"));
                }
            } else {
                return setNewSchemeIdRecordGen(schemeName, generateRequest, stateMachine.actions.get("generate"));
            }

        } else {
            logger.error("error generateSchemeIds():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        //return schemeIdRec;
    }

    public SchemeId getSchemeIdBySystemId(String schemeName, String systemId) {
        List<SchemeId> schemeId = bulkSchemeIdRepository.findBySchemeAndSystemId(schemeName, systemId);
        if (schemeId.size() > 0) {
            return schemeId.get(0);
        } else {
            //requestbody change fix
            return null;
        }

    }

    public SchemeId setNewSchemeIdRecordGen(String schemeName, SchemeIdGenerateRequest request, String reserve) throws CisException {
        SchemeId record = setAvailableSchemeIdRecord2NewStatusGen(schemeName, request, reserve);
        try {
            if (record != null) {
                return record;
            } else {
                SchemeId schemeIdRec = counterModeGen(schemeName, request, reserve);
                if (schemeIdRec != null) {
                    return schemeIdRec;
                } else {
                    logger.error("error setNewSchemeIdRecordGen():: Not found Error");
                    throw new CisException(HttpStatus.NOT_FOUND, "Error");
                }

            }
        } catch (Exception e) {
            logger.error("error setNewSchemeIdRecordGen():: error getting available schemeId for:{}, Exception msg: {}",schemeName,e.getMessage());
            throw new CisException(HttpStatus.NOT_FOUND, "error getting available schemeId for:" + schemeName + e.getMessage());
        }

    }

    public SchemeId setAvailableSchemeIdRecord2NewStatusGen(String schemeName, SchemeIdGenerateRequest request, String generate) throws CisException {
        Map<String, String> objQuery = new HashMap<String, String>();
        SchemeId outputSchemeRec = new SchemeId();
        SchemeId updatedrecord = null;
        if (null != schemeName) {
            objQuery.put("scheme", schemeName.toString());
            objQuery.put("status", "Available");
        }

        List<SchemeId> schemeIdRecords = findschemeRecord(objQuery, "1", null);
        //no list so no size
        if (schemeIdRecords != null && schemeIdRecords.size() > 0) {

            var newStatus = stateMachine.getNewStatus(schemeIdRecords.get(0).getStatus(), generate);
            if (null != newStatus) {

                if (null != request.getSystemId() && request.getSystemId().trim() != "") {
                    schemeIdRecords.get(0).setSystemId(request.getSystemId());
                }
                schemeIdRecords.get(0).setStatus(newStatus);
                schemeIdRecords.get(0).setAuthor(request.getAuthor());
                schemeIdRecords.get(0).setSoftware(request.getSoftware());
                //Expiration Date Not available in request.
                schemeIdRecords.get(0).setExpirationDate(null);
                schemeIdRecords.get(0).setComment(request.getComment());
                schemeIdRecords.get(0).setJobId(null);
                updatedrecord = bulkSchemeIdRepository.save(schemeIdRecords.get(0));
                //updatedrecord = updateSchemeIdRecord(schemeIdRecords.get(0), schemeName);
                return updatedrecord;
            } else {
                updatedrecord = counterModeGen(schemeName, request, generate);
            }
        } else if (schemeIdRecords.size() == 0) {
            return counterModeGen(schemeName, request, generate);
        } else {
            logger.error("error setAvailableSchemeIdRecord2NewStatusGen():: error getting available schemeId for:{}",schemeName);
            throw new CisException(HttpStatus.ACCEPTED, "error getting available schemeId for:" + schemeName + ", err: ");
        }
        return updatedrecord;

    }

    public SchemeId counterModeGen(String schemeName, SchemeIdGenerateRequest request, String reserve) throws CisException {
        String newSchemeId = getNextSchemeIdGen(schemeName, request);
        SchemeId updatedrecord = null;
        if (newSchemeId != null) {
            SchemeId schemeIdRecord = getSchemeIdsByschemeIdList(schemeName, newSchemeId.toString());
            if (schemeIdRecord != null) {
                var newStatus = stateMachine.getNewStatus(schemeIdRecord.getStatus(), reserve);
                if (null != newStatus) {

                    if (null != request.getSystemId() && request.getSystemId().trim() != "") {
                        schemeIdRecord.setSystemId(request.getSystemId());
                    }
                    schemeIdRecord.setStatus(newStatus);
                    schemeIdRecord.setAuthor(request.getAuthor());
                    schemeIdRecord.setSoftware(request.getSoftware());
                    //Expiration Date Not available in request.
                    schemeIdRecord.setExpirationDate(null);
                    schemeIdRecord.setComment(request.getComment());
                    schemeIdRecord.setJobId(null);
                    updatedrecord = bulkSchemeIdRepository.save(schemeIdRecord);
                    //updatedrecord = updateSchemeIdRecord(schemeIdRecord, schemeName.toString());
                } else {
                    counterModeGen(schemeName, request, reserve);
                }
            }
        }
        return updatedrecord;
    }

    public String getNextSchemeIdGen(String schemeName, SchemeIdGenerateRequest request) {
        Optional<SchemeIdBase> schemeIdBaseList = schemeIdBaseRepository.findByScheme(schemeName.toString());
        String nextId = "";
        if (schemeName.toUpperCase().equalsIgnoreCase("SNOMEDID")) {
            if(schemeIdBaseList.isPresent())
            nextId = SNOMEDID.getNextId(schemeIdBaseList.get().getIdBase());
        }
        else if (schemeName.toUpperCase().equalsIgnoreCase("CTV3ID")) {
            if(schemeIdBaseList.isPresent())
            nextId = CTV3ID.getNextId(schemeIdBaseList.get().getIdBase());
        }
        // schemaIdBaseRecord.idBase = nextId;
        SchemeIdBase schemeIdBase = new SchemeIdBase(schemeName.toUpperCase(), nextId);
        SchemeIdBase schemeId = schemeIdBaseRepository.save(schemeIdBase);
        if (null != schemeId)
            return nextId;
        else
            return null;
    }

    //registerSchemeId
    public SchemeId registerSchemeId(AuthenticateResponseDto authToken, SchemeName schemeName, SchemeIdRegisterRequestDto request) throws CisException {
            return this.registerSchemeIds(schemeName, request,authToken);
    }

    public SchemeId registerSchemeIds(SchemeName schemeName, SchemeIdRegisterRequestDto request,AuthenticateResponseDto authToken) throws CisException {
        SchemeId schemeIdRec = null;
        if (this.isAbleUser(schemeName.toString(), authToken)) {
            SchemeIdRegisterRequest registerRequest = new SchemeIdRegisterRequest();
            registerRequest.setSchemeId(request.getSchemeId());
            registerRequest.setSystemId(request.getSystemId());
            registerRequest.setSoftware(request.getSoftware());
            registerRequest.setComment(request.getComment());

            if (request.getSystemId() != null || request.getSystemId() == "") {
                registerRequest.setSystemId(sctIdHelper.guid());
                registerRequest.setAutoSysId(true);
                registerRequest.setAuthor(registerRequest.getAuthor());
                if (!registerRequest.isAutoSysId()) {
                    schemeIdRec = getSchemeIdBySystemId(schemeName.toString(), registerRequest.getSystemId());
                    if (schemeIdRec != null) {
                        if (schemeIdRec.getSchemeId() != request.getSchemeId()) {
                            logger.error("error registerSchemeIds():: Bad Request: SystemId : {} already exists with SchemeId:{}",request.getSystemId(),schemeIdRec.getSchemeId());
                            throw new CisException(HttpStatus.BAD_REQUEST, "SystemId" + request.getSystemId() + " already exists with SchemeId:" + schemeIdRec.getSchemeId());
                        }
                        if (Objects.equals(schemeIdRec.getStatus(), stateMachine.statuses.get("assigned"))) {
                            return schemeIdRec;
                        } else {
                            schemeIdRec = registerNewSchemeId(schemeName, registerRequest);
                        }
                    }
                } else {
                    schemeIdRec = registerNewSchemeId(schemeName, registerRequest);
                }
            }
        } else {
            logger.error("error registerSchemeIds():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return schemeIdRec;
    }

    private SchemeId registerNewSchemeId(SchemeName schemeName, SchemeIdRegisterRequest request) throws CisException {
        SchemeId schemeIdrecord = getSchemeIdsByschemeIdList(schemeName.schemeName, request.getSchemeId());
        SchemeId schemeId = new SchemeId();

        //
        if (schemeIdrecord.getSchemeId().isEmpty()) {
            logger.error("error registerNewSchemeId():: SchemeId record is empty");
            throw new CisException(HttpStatus.NOT_FOUND, "SchemeId record is empty");
        } else {
            var newStatus = stateMachine.getNewStatus(schemeIdrecord.getStatus(), stateMachine.actions.get("register"));
            if (null != newStatus) {
                if (request.getSystemId() != null && request.getSystemId().trim() != "") {
                    schemeIdrecord.setSystemId(request.getSystemId());
                }
                schemeIdrecord.setStatus(newStatus);
                schemeIdrecord.setAuthor(request.getAuthor());
                schemeIdrecord.setSoftware(request.getSoftware());
                schemeId.setExpirationDate(request.getExpirationDate());
                schemeIdrecord.setComment(request.getComment());
                schemeIdrecord.setJobId(null);
                //schemeId = bulkSchemeIdRepository.save(schemeIdrecord);
                schemeId = updateSchemeIdRecord(schemeIdrecord, schemeName.toString());
            } else {
                logger.error("error registerNewSchemeId():: Cannot register SchemeId:{}, current status:{}",request.getSchemeId(),schemeIdrecord.getStatus());
                throw new CisException(HttpStatus.BAD_REQUEST, "Cannot register SchemeId:" + request.getSchemeId() + ", current status:" + schemeIdrecord.getStatus());
            }
        }
        return schemeId;
    }

}
