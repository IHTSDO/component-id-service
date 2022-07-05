package org.snomed.cis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.controller.SecurityController;
import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeIdBase;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.BulkSchemeIdRepository;
import org.snomed.cis.repository.PermissionsSchemeRepository;
import org.snomed.cis.repository.SchemeIdBaseRepository;
import org.snomed.cis.util.*;
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
        logger.debug("Request Received : schemeName-{} :: authToken - {} ", schemeName, authToken);
        List<String> groups = authToken.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
        boolean isAble = false;
        if (groups.contains("component-identifier-service-admin") || hasSchemePermission(schemeName, authToken)) {
            isAble = true;
        }
        logger.info("isAbleUser() - Response: {}", isAble);
        return isAble;
    }

    public boolean hasSchemePermission(String schemeName, AuthenticateResponseDto authToken) throws CisException {
        logger.debug("Request Received : schemeName-{} :: authToken - {} ", schemeName, authToken);
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
        logger.info("hasSchemePermission() - Response: {}", able);
        return able;
    }

    public List<SchemeId> getSchemeIds(AuthenticateResponseDto authToken, String limit, String skip, SchemeName schemeName) throws CisException {
        logger.debug("Request Received :authToken - {} ::limit - {} :: skip - {}:: schemeName-{} :: ", authToken, limit, skip, schemeName);
        return this.getSchemeIdsList(limit, skip, schemeName, authToken);
    }

    private List<SchemeId> getSchemeIdsList(String limit, String skip, SchemeName schemeName, AuthenticateResponseDto authToken) throws CisException {
        logger.debug("Request Received : limit - {} :: skip - {} :: schemeName-{} :: authToken - {} ", limit, skip, schemeName, authToken);
        List<SchemeId> schemeidList = new ArrayList<>();
        if (this.isAbleUser("false", authToken)) {
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

            StringBuffer swhere = new StringBuffer("");
            StringBuffer whereResult = new StringBuffer("");
            if (objQuery.size() > 0) {
                for (var query :
                        objQuery.entrySet()) {
                    swhere.append(" And ").append(query.getKey()).append("=").append("'").append((query.getValue()))
                            .append("'");
                }
            }
            if (!(swhere.toString().equalsIgnoreCase(""))) {
                whereResult.append(" WHERE ").append(swhere.substring(5));
            }
            StringBuffer sql = new StringBuffer();
            if ((limitR > 0) && (skipTo == 0)) {
                sql.append("Select * FROM schemeid").append(whereResult).append(" order by schemeId limit ")
                        .append(limitR);
            } else {
                sql.append("Select * FROM schemeid").append(whereResult).append(" order by schemeId");
            }
            Query genQuery = entityManager.createNativeQuery(sql.toString(), SchemeId.class);

            List<SchemeId> resultList = genQuery.getResultList();

            if ((skipTo == 0)) {
                schemeidList = resultList;
            } else {
                var cont = 1;
                List<SchemeId> newRows = new ArrayList<>();
                for (var i = 0; i < (resultList.size()); i++) {
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
        logger.info("getSchemeIdsList-Response:{}", schemeidList);
        return schemeidList;
    }

    public SchemeId getSchemeId(AuthenticateResponseDto authToken, SchemeName scheme, String schemeid) throws CisException {
        logger.debug("Request Received : schemeName-{} :: authToken - {} :: schemeid - {}", scheme, authToken, schemeid);
        return this.getSchemeIdsByschemeIdList(scheme.toString(), schemeid);
    }

    public SchemeId getSchemeIdsByschemeIdList(String schemeName, String schemeid) throws CisException {
        logger.debug("Request Received : schemeName-{} :: schemeid - {} ", schemeName, schemeid);
        SchemeId record = new SchemeId();
        Optional<SchemeId> schemeIdObj = null;
        if (null == schemeid || schemeid.isEmpty() || schemeid.isBlank()) {
            logger.error("error getSchemeIdsByschemeIdList():: Not valid schemeId.");
            throw new CisException(HttpStatus.BAD_REQUEST, "Not valid schemeId.");
        } else {
            boolean isValidScheme = false;
            if ("SNOMEDID".equalsIgnoreCase(schemeName.toString().toUpperCase())) {
                isValidScheme = SNOMEDID.validSchemeId(schemeid);
            } else if ("CTV3ID".equalsIgnoreCase(schemeName.toString().toUpperCase())) {
                isValidScheme = CTV3ID.validSchemeId(schemeid);
            }
            if (!isValidScheme) {
                logger.error("error getSchemeIdsByschemeIdList():: Not valid schemeId.");
                throw new CisException(HttpStatus.BAD_REQUEST, "Not valid schemeId.");
            }
        }
        schemeIdObj = bulkSchemeIdRepository.findBySchemeAndSchemeId(schemeName, schemeid);
        if (schemeIdObj.isEmpty()) {
            record = getFreeRecords(schemeName, schemeid);
        } else {
            record = schemeIdObj.get();
        }
        logger.info("getSchemeIdsByschemeIdList(): Response - {}", record);
        return record;
    }

    public SchemeId getFreeRecords(String schemeName, String schemeid) throws CisException {
        logger.debug("Request Received : schemeName-{} :: schemeid - {} ", schemeName, schemeid);
        Map<String, Object> schemeIdRecord = getNewRecord(schemeName, schemeid);
        schemeIdRecord.put("status", "Available");
        return insertSchemeIdRecord(schemeIdRecord);
    }

    private Map<String, Object> getNewRecord(String schemeName, String schemeid) {
        logger.debug("Request Received : schemeName-{} :: schemeid - {} ", schemeName, schemeid);
        Map<String, Object> schemeIdRecord = new LinkedHashMap<>();
        schemeIdRecord.put("scheme", schemeName);
        schemeIdRecord.put("schemeId", schemeid);
        schemeIdRecord.put("sequence", null);
        schemeIdRecord.put("checkDigit", null);
        schemeIdRecord.put("systemId", sctIdHelper.guid());
        logger.info("getNewRecord():Response - {}", schemeIdRecord);
        return schemeIdRecord;
    }

    public SchemeId insertSchemeIdRecord(Map<String, Object> schemeIdRecord) throws CisException {
        logger.debug("Request Received : schemeIdRecord-{}", schemeIdRecord);
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
                if (mapObj.getKey().equalsIgnoreCase("scheme")) {
                    scheme = (String) mapObj.getValue();
                } else if (mapObj.getKey().equalsIgnoreCase("schemeId")) {
                    schemeId = (String) mapObj.getValue();
                } else if (mapObj.getKey().equalsIgnoreCase("sequence")) {
                    Object sequenceValue = mapObj.getValue();
                    if (sequenceValue instanceof Integer) {
                        sequence = (Integer) sequenceValue; // 1
                    }
                } else if (mapObj.getKey().equalsIgnoreCase("checkDigit")) {
                    Object checkDigitvalue = mapObj.getValue();
                    if (checkDigitvalue instanceof Integer) {
                        checkDigit = (Integer) checkDigitvalue; // 1
                    }
                } else if (mapObj.getKey().equalsIgnoreCase("systemId")) {
                    systemId = (String) mapObj.getValue();
                } else if (mapObj.getKey().equalsIgnoreCase("status")) {
                    status = (String) mapObj.getValue();
                } else if (mapObj.getKey().equalsIgnoreCase("author")) {
                    author = (String) mapObj.getValue();
                } else if (mapObj.getKey().equalsIgnoreCase("software")) {
                    software = (String) mapObj.getValue();
                } else if (mapObj.getKey().equalsIgnoreCase("expirationDate")) {
                    expirationDate = (LocalDateTime) mapObj.getValue();
                } else if (mapObj.getKey().equalsIgnoreCase("jobId")) {
                    //jobId = (Integer) mapObj.getValue();
                    Object jobIdvalue = mapObj.getValue();
                    if (jobIdvalue instanceof Integer) {
                        jobId = (Integer) jobIdvalue; // 1
                    }

                } else if (mapObj.getKey().equalsIgnoreCase("created_at")) {
                    Object created_atValue = (LocalDateTime) mapObj.getValue();
                    if (created_atValue instanceof LocalDateTime) {
                        created_at = (LocalDateTime) created_atValue;
                    }
                } else if (mapObj.getKey().equalsIgnoreCase("modified_at")) {
                    Object modified_atValue = (LocalDateTime) mapObj.getValue();
                    if (modified_atValue instanceof LocalDateTime) {
                        modified_at = (LocalDateTime) modified_atValue;
                    }
                }
            }
            SchemeId schemeIdObj = SchemeId.builder().scheme(scheme).schemeId(schemeId).sequence(sequence).checkDigit(checkDigit).systemId(systemId).status(status).author(author).software(software).expirationDate(expirationDate).jobId(jobId)
                    .created_at(created_at).modified_at(modified_at).build();
            SchemeId schemeId1 = bulkSchemeIdRepository.save(schemeIdObj);
            logger.info("insertSchemeIdRecord():Response - {}", schemeId1);
            return schemeId1;
        } catch (Exception e) {
            throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

    }


    public SchemeId getSchemeIdsBySystemId(AuthenticateResponseDto authToken, SchemeName scheme, String systemid) throws CisException {
        logger.debug("Request Received : schemeName-{} :: authToken - {} :: systemid - {}", scheme, authToken, systemid);
        return this.getSchemeIdsBysystemList(scheme.toString(), systemid, authToken);
    }

    private SchemeId getSchemeIdsBysystemList(String scheme, String systemid, AuthenticateResponseDto authToken) throws CisException {
        logger.debug("Request Received : schemeName-{} ::systemid - {} :: authToken - {} ", scheme, systemid, authToken);
        if (isAbleUser(scheme, authToken)) {
            List<SchemeId> schemeIdList = bulkSchemeIdRepository.findBySchemeAndSystemId(scheme, systemid);

            if (!schemeIdList.isEmpty() && schemeIdList.size() > 0) {
                logger.info("getSchemeIdsBysystemList()-Response: {}", schemeIdList.get(0));
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
        logger.debug("Request Received :authToken - {} :: schemeName-{} :: SchemeIdUpdateRequestDto - {} ", authToken, schemeName, request);
        return this.deprecateSchemeIdList(schemeName, request, authToken);
    }

    private SchemeId deprecateSchemeIdList(SchemeName schemeName, SchemeIdUpdateRequestDto request, AuthenticateResponseDto authToken) throws CisException {
        logger.debug("Request Received : schemeName-{} ::SchemeIdUpdateRequestDto-{} :: authToken - {} ", schemeName, request, authToken);
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
                    logger.error("error deprecateSchemeIdList():: Cannot deprecate SchemeId:{}, current status:{}", schemeIdrecord.getSchemeId(), schemeIdrecord.getStatus());
                    throw new CisException(HttpStatus.BAD_REQUEST, "Cannot deprecate SchemeId:" + schemeIdrecord.getSchemeId() + ", current status:" + schemeIdrecord.getStatus());
                }
            }
        } else {
            logger.error("error deprecateSchemeIdList():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
        logger.info("deprecateSchemeIdList()-Response: {}", schemeId);
        return schemeId;

    }
    //releaseSchmeId

    public SchemeId releaseSchemeIds(AuthenticateResponseDto authToken, SchemeName schemeName, SchemeIdUpdateRequestDto request) throws CisException {
        logger.debug("Request Received :AuthenticateResponseDto - {} :: schemeName-{} :: SchemeIdUpdateRequestDto - {} ", authToken, schemeName, request);
        return this.releaseSchemeIdList(schemeName, request, authToken);
    }

    private SchemeId releaseSchemeIdList(SchemeName schemeName, SchemeIdUpdateRequestDto request, AuthenticateResponseDto authToken) throws CisException {
        logger.debug("Request Received : schemeName-{} ::SchemeIdUpdateRequestDto - {}:: authToken - {} ", schemeName, request, authToken);
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
                    logger.error("error releaseSchemeIdList():: Cannot release SchemeId:{} , current status:{}", schemeIdrecord.getSchemeId(), schemeIdrecord.getStatus());
                    throw new CisException(HttpStatus.BAD_REQUEST, "Cannot release SchemeId:" + schemeIdrecord.getSchemeId() + ", current status:" + schemeIdrecord.getStatus());
                }
            }
        } else {
            logger.error("error releaseSchemeIdList():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        logger.info("releaseSchemeIdList()-Response: {}", schemeId);
        return schemeId;
    }

    //publish

    public SchemeId publishSchemeId(AuthenticateResponseDto authToken, SchemeName schemeName, SchemeIdUpdateRequestDto request) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto - {}:: schemeName-{} :: SchemeIdUpdateRequestDto - {} ", authToken, schemeName, request);
        return this.publishSchemeIdList(schemeName, request, authToken);

    }

    private SchemeId publishSchemeIdList(SchemeName schemeName, SchemeIdUpdateRequestDto request, AuthenticateResponseDto authToken) throws CisException {
        logger.debug("Request Received : schemeName-{} :: SchemeIdUpdateRequestDto-{}:: authToken - {} ", schemeName, request, authToken);
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
                    logger.error("error publishSchemeIdList():: Cannot publish SchemeId:{}, current status:{}", schemeIdrecord.getSchemeId(), schemeIdrecord.getStatus());
                    throw new CisException(HttpStatus.BAD_REQUEST, "Cannot publish SchemeId:" + schemeIdrecord.getSchemeId() + ", current status:" + schemeIdrecord.getStatus());
                }
            }
        } else {
            logger.error("error publishSchemeIdList():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        logger.info("publishSchemeIdList() - Response: {}", schemeId);
        return schemeId;
    }

    public SchemeId reserveSchemeId(AuthenticateResponseDto authToken, SchemeName schemeName, SchemeIdReserveRequestDto request) throws CisException {
        logger.debug("Request Received :AuthenticateResponseDto - {} :: schemeName-{} :: SchemeIdReserveRequestDto - {} ", authToken, schemeName, request);
        SchemeId schemeId = this.reserveSchemeIdList(schemeName, request, authToken);
        logger.info("reserveSchemeId() - Response: {}", schemeId);
        return schemeId;
    }

    private SchemeId reserveSchemeIdList(SchemeName schemeName, SchemeIdReserveRequestDto request, AuthenticateResponseDto authToken) throws CisException {
        logger.debug("Request Received : schemeName-{} :: SchemeIdReserveRequestDto - {} :: AuthenticateResponseDto - {}", schemeName, request, authToken);
        SchemeId schemeId = null;
        if (this.isAbleUser(schemeName.toString(), authToken)) {
            SchemeIdReserveRequest reserveRequest = new SchemeIdReserveRequest();
            reserveRequest.setSoftware(request.getSoftware());
            reserveRequest.setExpirationDate(request.getExpirationDate());
            reserveRequest.setComment(request.getComment());
            reserveRequest.setAuthor(authToken.getName());
            schemeId = setNewSchemeIdRecord(schemeName, reserveRequest, stateMachine.actions.get("reserve"));
        } else {
            logger.error("error reserveSchemeIdList():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return schemeId;
    }

    //List
    private SchemeId setNewSchemeIdRecord(SchemeName schemeName, SchemeIdReserveRequest request, String reserve) throws CisException {
        logger.debug("Request Received : schemeName-{} ::  SchemeIdReserveRequest -{} :: authToken - {} ", schemeName, request, reserve);
        SchemeId record = setAvailableSchemeIdRecord2NewStatus(schemeName, request, reserve);
        try {
            if (record != null) {
                return record;
            } else {
                SchemeId schemeIdRec = counterMode(schemeName, request, reserve);
                if (schemeIdRec != null) {
                    logger.info("setNewSchemeIdRecord(): Response- {}", schemeIdRec);
                    return schemeIdRec;
                } else {
                    logger.error("error setNewSchemeIdRecord():: Error");
                    throw new CisException(HttpStatus.NOT_FOUND, "Error Generating SchemeId Record.");
                }

            }
        } catch (Exception e) {
            logger.error("error setNewSchemeIdRecord():: error getting available schemeId for:{}, Exception msg: {}", schemeName, e.getMessage());
            throw new CisException(HttpStatus.NOT_FOUND, "error getting available schemeId for:" + schemeName + e.getMessage());
        }

    }

    private SchemeId setAvailableSchemeIdRecord2NewStatus(SchemeName schemeName, SchemeIdReserveRequest request, String reserve) throws CisException {
        logger.debug("Request Received : schemeName-{} :: SchemeIdReserveRequest - {} ::  reserve - {} ", schemeName, request, reserve);
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
                updatedrecord = bulkSchemeIdRepository.save(schemeIdRecords.get(0));
                return updatedrecord;
            } else {
                counterMode(schemeName, request, reserve);
            }
        } else {
            return null;
        }
        logger.info("setAvailableSchemeIdRecord2NewStatus: Response - {}", updatedrecord);
        return updatedrecord;

    }

    public SchemeId counterMode(SchemeName schemeName, SchemeIdReserveRequest request, String reserve) throws CisException {
        logger.debug("Request Received : schemeName-{} :: SchemeIdReserveRequest - {} :: reserve - {}", schemeName, request, reserve);
        String newSchemeId = getNextSchemeId(schemeName, request);
        SchemeId updatedrecord = null;
        if (newSchemeId != null) {
            SchemeId schemeIdRecord = getSchemeIdsByschemeIdList(schemeName.toString(), newSchemeId.toString());
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
                } else {
                    updatedrecord = counterMode(schemeName, request, reserve);
                }
            }
        }
        return updatedrecord;
    }

    private String getNextSchemeId(SchemeName schemeName, SchemeIdReserveRequest request) {
        logger.debug("Request Received : schemeName-{} :: SchemeIdReserveRequest - {} ", schemeName, request);
        Optional<SchemeIdBase> schemeIdBaseList = schemeIdBaseRepository.findByScheme(schemeName.toString());
       // SchemeIdBase schemeIdBase = null;
        String nextId = null;

        if (schemeIdBaseList.isPresent()) {
            if (schemeName.toString().toUpperCase().equalsIgnoreCase("SNOMEDID")) {
                if (schemeIdBaseList.isPresent())
                    nextId = SNOMEDID.getNextId(schemeIdBaseList.get().getIdBase());
            } else if (schemeName.toString().toUpperCase().equalsIgnoreCase("CTV3ID")) {
                if (schemeIdBaseList.isPresent())
                    nextId = CTV3ID.getNextId(schemeIdBaseList.get().getIdBase());
            }
            schemeIdBaseList.get().setIdBase(nextId);
            schemeIdBaseRepository.save(schemeIdBaseList.get());
        }
        return nextId;
    }

    public List<SchemeId> findschemeRecord(Map<String, String> objQuery, String limit, String skip) {
        logger.debug("Request Received : schemeName-{} :: authToken - {} ", objQuery, limit, skip);
        SchemeId schemeidList = null;
        var limitR = 100;
        var skipTo = 0;
        if (limit != null)
            limitR = Integer.parseInt(limit);
        if (skip != null)
            skipTo = Integer.parseInt(skip);

        StringBuffer swhere = new StringBuffer("");
        StringBuffer whereResult = new StringBuffer();
        if (objQuery.size() > 0) {
            for (var query :
                    objQuery.entrySet()) {
                swhere = swhere.append(" And ").append(query.getKey()).append("=").append("'")
                        .append(query.getValue()).append("'");
            }
        }
        if (!(swhere.toString().equalsIgnoreCase(""))) {
            whereResult.append(" WHERE ").append(swhere.substring(5));
        }
        StringBuffer sql = new StringBuffer();
        if ((limitR > 0) && (skipTo == 0)) {
            sql.append("SELECT * FROM schemeId").append(whereResult).append(" order by schemeId limit ").append(limit);
        } else {
            sql.append("SELECT * FROM schemeId").append(whereResult).append(" order by schemeId");
        }
        Query genQuery = entityManager.createNativeQuery(sql.toString(), SchemeId.class);

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
            logger.info("findschemeRecord() - Response::{}", newRows);
            return newRows;
        }
    }


    public SchemeId generateSchemeId(AuthenticateResponseDto authToken, SchemeName schemeName, SchemeIdGenerateRequestDto request) throws CisException {
        logger.debug("Request Received : authToken - {} :: schemeName-{} :: SchemeIdGenerateRequestDto - {}", authToken, schemeName, request);
        SchemeId schemeId = this.generateSchemeIds(schemeName.toString(), request, authToken);
        logger.info("generateSchemeId() - Response::{}", schemeId);
        return schemeId;
    }

    public SchemeId generateSchemeIds(String schemeName, SchemeIdGenerateRequestDto request, AuthenticateResponseDto authToken) throws CisException {
        logger.debug("Request Received : schemeName-{} ::SchemeIdGenerateRequestDto-{}:: authToken - {} ", schemeName, request, authToken);
        SchemeId schemeIdRec = new SchemeId();
        if (this.isAbleUser(schemeName, authToken)) {
            SchemeIdGenerateRequest generateRequest = new SchemeIdGenerateRequest();
            generateRequest.setSystemId(request.getSystemId());
            generateRequest.setSoftware(request.getSoftware());
            generateRequest.setComment(request.getComment());

            if (request.getSystemId().isBlank() || request.getSystemId().isEmpty() || null == request.getSystemId()) {
                generateRequest.setSystemId(sctIdHelper.guid());
                generateRequest.setAutoSysId(true);
            }
            generateRequest.setAuthor(generateRequest.getAuthor());
            if (!generateRequest.isAutoSysId()) {
                schemeIdRec = getSchemeIdBySystemId(schemeName, request.systemId);
                if (schemeIdRec != null) {
                    return schemeIdRec;
                } else {
                    schemeIdRec = setNewSchemeIdRecordGen(schemeName, generateRequest, stateMachine.actions.get("generate"));
                }
            } else {
                schemeIdRec = setNewSchemeIdRecordGen(schemeName, generateRequest, stateMachine.actions.get("generate"));
            }

        } else {
            logger.error("error generateSchemeIds():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        logger.info("generateSchemeIds() - Response::{}", schemeIdRec);
        return schemeIdRec;
    }

    public SchemeId getSchemeIdBySystemId(String schemeName, String systemId) {
        logger.debug("Request Received : schemeName-{} :: systemId - {} ", schemeName, systemId);
        List<SchemeId> schemeId = bulkSchemeIdRepository.findBySchemeAndSystemId(schemeName, systemId);
        if (schemeId.size() > 0) {
            logger.info("getSchemeIdBySystemId() - Response::{}", schemeId.get(0));
            return schemeId.get(0);
        } else {
            logger.info("getSchemeIdBySystemId() - Response::{}", "null");
            return null;
        }

    }

    public SchemeId setNewSchemeIdRecordGen(String schemeName, SchemeIdGenerateRequest request, String reserve) throws CisException {
        logger.debug("Request Received : schemeName-{} :: request - {} :: SchemeIdGenerateRequest - {} ", schemeName, request, reserve);
        SchemeId record = setAvailableSchemeIdRecord2NewStatusGen(schemeName, request, reserve);
        try {
            if (record != null) {
                return record;
            } else {
                SchemeId schemeIdRec = counterModeGen(schemeName, request, reserve);
                if (schemeIdRec != null) {
                    logger.info("setNewSchemeIdRecordGen() - Response::{}", schemeIdRec);
                    return schemeIdRec;
                } else {
                    logger.error("error setNewSchemeIdRecordGen():: Not found Error");
                    throw new CisException(HttpStatus.NOT_FOUND, "Error");
                }

            }
        } catch (Exception e) {
            logger.error("error setNewSchemeIdRecordGen():: error getting available schemeId for:{}, Exception msg: {}", schemeName, e.getMessage());
            throw new CisException(HttpStatus.NOT_FOUND, "error getting available schemeId for:" + schemeName + e.getMessage());
        }

    }

    public SchemeId setAvailableSchemeIdRecord2NewStatusGen(String schemeName, SchemeIdGenerateRequest request, String generate) throws CisException {
        logger.debug("Request Received : schemeName-{} :: SchemeIdGenerateRequest - {} :: generate - {} ", schemeName, request, generate);
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
                return updatedrecord;
            } else {
                updatedrecord = counterModeGen(schemeName, request, generate);
            }
        } else if (schemeIdRecords.size() == 0) {
            return counterModeGen(schemeName, request, generate);
        } else {
            logger.error("error setAvailableSchemeIdRecord2NewStatusGen():: error getting available schemeId for:{}", schemeName);
            throw new CisException(HttpStatus.ACCEPTED, "error getting available schemeId for:" + schemeName + ", err: ");
        }
        logger.info("setAvailableSchemeIdRecord2NewStatusGen() - Response::{}", updatedrecord);
        return updatedrecord;

    }

    public SchemeId counterModeGen(String schemeName, SchemeIdGenerateRequest request, String reserve) throws CisException {
        logger.debug("Request Received : schemeName-{} :: SchemeIdGenerateRequest - {} ::  reserve - {} ", schemeName, request, reserve);
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
                } else {
                    counterModeGen(schemeName, request, reserve);
                }
            }
        }
        logger.info("counterModeGen() - Response::{}", updatedrecord);
        return updatedrecord;
    }

    public String getNextSchemeIdGen(String schemeName, SchemeIdGenerateRequest request) {
        logger.debug("Request Received : schemeName-{} :: SchemeIdGenerateRequest - {} ", schemeName, request);
        Optional<SchemeIdBase> schemeIdBaseList = schemeIdBaseRepository.findByScheme(schemeName.toString());
        String nextId = "";
        if (schemeName.toUpperCase().equalsIgnoreCase("SNOMEDID")) {
            if (schemeIdBaseList.isPresent())
                nextId = SNOMEDID.getNextId(schemeIdBaseList.get().getIdBase());
        } else if (schemeName.toUpperCase().equalsIgnoreCase("CTV3ID")) {
            if (schemeIdBaseList.isPresent())
                nextId = CTV3ID.getNextId(schemeIdBaseList.get().getIdBase());
        }
        // schemaIdBaseRecord.idBase = nextId;
        SchemeIdBase schemeIdBase = new SchemeIdBase(schemeName.toUpperCase(), nextId);
        SchemeIdBase schemeId = schemeIdBaseRepository.save(schemeIdBase);
        if (null != schemeId) {
            logger.info("getNextSchemeIdGen() - Response::{}", nextId);
            return nextId;
        } else {
            logger.info("getNextSchemeIdGen() - Response::{}", "null");
            return null;
        }
    }

    //registerSchemeId
    public SchemeId registerSchemeId(AuthenticateResponseDto authToken, SchemeName schemeName, SchemeIdRegisterRequestDto request) throws CisException {
        logger.debug("Request Received : schemeName-{} :: authToken - {} :: SchemeIdRegisterRequestDto - {} ", schemeName, authToken, request);
        SchemeId schemeId = this.registerSchemeIds(schemeName, request, authToken);
        logger.info("registerSchemeIds() - Response::{}", schemeId);
        return schemeId;
    }

    public SchemeId registerSchemeIds(SchemeName schemeName, SchemeIdRegisterRequestDto request, AuthenticateResponseDto authToken) throws CisException {
        logger.debug("Request Received : schemeName-{} :: SchemeIdRegisterRequestDto -{} :: authToken - {} ", schemeName, request, authToken);
        SchemeId schemeIdRec = null;
        if (this.isAbleUser(schemeName.toString(), authToken)) {
            SchemeIdRegisterRequest registerRequest = new SchemeIdRegisterRequest();
            registerRequest.setSchemeId(request.getSchemeId());
            registerRequest.setSystemId(request.getSystemId());
            registerRequest.setSoftware(request.getSoftware());
            registerRequest.setComment(request.getComment());

            if (request.getSystemId().isBlank() || request.getSystemId().isEmpty() || request.getSystemId() == null) {
                registerRequest.setSystemId(sctIdHelper.guid());
                registerRequest.setAutoSysId(true);
            }
                registerRequest.setAuthor(registerRequest.getAuthor());
                if (!registerRequest.isAutoSysId()) {
                    schemeIdRec = getSchemeIdBySystemId(schemeName.toString(), registerRequest.getSystemId());
                    if (schemeIdRec != null) {
                        if (!(schemeIdRec.getSchemeId().equalsIgnoreCase(request.getSchemeId()))) {
                            logger.error("error registerSchemeIds():: Bad Request: SystemId : {} already exists with SchemeId:{}", request.getSystemId(), schemeIdRec.getSchemeId());
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
           // }
        } else {
            logger.error("error registerSchemeIds():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        logger.info("registerSchemeIds() - Response::{}", schemeIdRec);
        return schemeIdRec;
    }

    private SchemeId registerNewSchemeId(SchemeName schemeName, SchemeIdRegisterRequest request) throws CisException {
        logger.debug("Request Received : schemeName-{} :: request - {} ", schemeName, request);
        SchemeId schemeIdrecord = getSchemeIdsByschemeIdList(schemeName.schemeName, request.getSchemeId());
        SchemeId schemeId = new SchemeId();

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
                schemeId = bulkSchemeIdRepository.save(schemeIdrecord);
            } else {
                logger.error("error registerNewSchemeId():: Cannot register SchemeId:{}, current status:{}", request.getSchemeId(), schemeIdrecord.getStatus());
                throw new CisException(HttpStatus.BAD_REQUEST, "Cannot register SchemeId:" + request.getSchemeId() + ", current status:" + schemeIdrecord.getStatus());
            }
        }
        logger.info("registerNewSchemeId() - Response::{}", schemeId);
        return schemeId;
    }

}
