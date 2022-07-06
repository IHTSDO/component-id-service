package org.snomed.cis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeIdBase;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.BulkJobRepository;
import org.snomed.cis.repository.BulkSchemeIdRepository;
import org.snomed.cis.repository.PermissionsSchemeRepository;
import org.snomed.cis.repository.SchemeIdBaseRepository;
import org.snomed.cis.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BulkSchemeIdService {
    private final Logger logger = LoggerFactory.getLogger(BulkSchemeIdService.class);
    @Autowired
    private BulkSchemeIdRepository bulkSchemeIdRepository;
    @Autowired
    AuthenticateToken authenticateToken;

    @Autowired
    private SchemeIdBaseRepository schemeIdBaseRepository;
    @Autowired
    private BulkJobRepository bulkJobRepository;
    @Autowired
    private BulkSctidService bulkSctidService;

    @Autowired
    private SctIdHelper sctIdHelper;

    @Autowired
    private SchemeIdService schemeIdService;

    @Autowired
    private PermissionsSchemeRepository permissionsSchemeRepository;

    private SchemeIdHelper schemeIdHelper;
    private SNOMEDID snomedid;

    public List<SchemeId> getSchemeIds(AuthenticateResponseDto token, SchemeName schemeName, String schemeIds) throws CisException {
        logger.debug("BulkSchemeIdService.getSchemeIds() token-{} :: schemeName-{} :: schemeIds -{}", token, schemeName, schemeIds);
        String[] schemedIdArray = schemeIds.replaceAll("\\s+", "").split(",");
        List<SchemeId> resSchemeArrayList = new ArrayList<>();
        boolean able = schemeIdService.isAbleUser(String.valueOf(schemeName), token);
        if (able) {
            for (String schemeId : schemedIdArray) {
                if (schemeId == null || schemeId.isEmpty()) {
                    logger.error("error getSchemeIds():: SchemeId is null.");
                    throw new CisException(HttpStatus.BAD_REQUEST, "SchemeId is null.");
                } else {
                    boolean isValidScheme = false;
                    if ("SNOMEDID".equalsIgnoreCase(schemeName.toString().toUpperCase())) {
                        isValidScheme = SNOMEDID.validSchemeId(schemeId);
                    } else if ("CTV3ID".equalsIgnoreCase(schemeName.toString().toUpperCase())) {
                        isValidScheme = CTV3ID.validSchemeId(schemeId);
                    }
                   /* if (!isValidScheme) {
                        throw new APIException(HttpStatus.BAD_REQUEST, "Not a valid schemeId");
                    }*/

                    ArrayList<String> schemeIdsArrayList = new ArrayList<String>(Arrays.asList(schemedIdArray));
                    resSchemeArrayList = bulkSchemeIdRepository.findBySchemeAndSchemeIdIn(schemeName.toString().toUpperCase(), List.of(schemedIdArray));
                    // resSchemeArrayList push
                    List<String> respSchemeIdArray = new ArrayList<>();
                    for (int i = 0; i < resSchemeArrayList.size(); i++) {
                        SchemeId schemeIdBulkObj = resSchemeArrayList.get(i);
                        respSchemeIdArray.add(schemeIdBulkObj.getSchemeId());
                    }
                    Set<String> rqSet = new HashSet<>(respSchemeIdArray);
                    Set<String> respSet = new HashSet<>(schemeIdsArrayList);
                    Set<String> resultDiff = new HashSet<>(rqSet);
                    respSet.removeAll(resultDiff);
                    if (respSet.size() > 0) {
                        for (String diffSchemeId :
                                respSet) {
                            SchemeId schemeIdBulkObj = getFreeRecord(String.valueOf(schemeName)/*.toString()*/, diffSchemeId, null, "true");
                            resSchemeArrayList.add(schemeIdBulkObj);
                        }
                    }
                }
            }//validate schemeId
        } else {
            logger.error("error getSchemeIds():: No permission for the selected operation.");
            throw new CisException(HttpStatus.BAD_REQUEST, "No permission for the selected operation");
        }
        logger.debug("BulkSchemeIdService.getSchemeIds() - Response size-:: {}", (null==resSchemeArrayList?"0":resSchemeArrayList.size()));
        return resSchemeArrayList;
    }


    public SchemeId getFreeRecord(String schemeName, String diffSchemeId, String systemId, String autoSysId) throws CisException {
        logger.debug("BulkSchemeIdService.getFreeRecord() schemeName - {} :: diffSchemeId - {}:: systemId - {} :: autoSysId - {}", schemeName, diffSchemeId, systemId, autoSysId);
        Map<String, Object> schemeIdRecord = getNewRecord(schemeName, diffSchemeId, systemId);
        schemeIdRecord.put("status", "available");
        SchemeId SchemeId = insertSchemeIdRecord(schemeIdRecord);
        logger.debug("BulkSchemeIdService.getFreeRecord() - Response :: {}", SchemeId);
        return SchemeId;
    }

    private SchemeId insertSchemeIdRecord(Map<String, Object> schemeIdRecord) throws CisException {
        logger.debug("BulkSchemeIdService.insertSchemeIdRecord() schemeIdRecord - {}", schemeIdRecord);
        String error;
        SchemeId schemeIdBulk = null;
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
                    sequence = (Integer) mapObj.getValue();
                } else if (mapObj.getKey().equalsIgnoreCase("checkDigit")) {
                    checkDigit = (Integer) mapObj.getValue();
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
                    jobId = (Integer) mapObj.getValue();
                } else if (mapObj.getKey().equalsIgnoreCase("created_at")) {
                    created_at = (LocalDateTime) mapObj.getValue();
                } else if (mapObj.getKey().equalsIgnoreCase("modified_at")) {
                    modified_at = (LocalDateTime) mapObj.getValue();
                }
            }
            SchemeId schemeId1 = SchemeId.builder().scheme(String.valueOf(scheme)).schemeId(String.valueOf(schemeId))
                    .sequence(sequence).checkDigit(checkDigit).systemId(systemId).status(status).author(author).software(software).jobId(jobId)
                    .expirationDate(expirationDate).created_at(created_at).modified_at(modified_at).build();
            schemeIdBulk = bulkSchemeIdRepository.save(schemeId1);
            //Optional<SchemeId> schemeDB = bulkSchemeIdRepository.findBySchemeAndSchemeId(String.valueOf(scheme), schemeId.toString());
            //schemeIdBulk = schemeDB.isPresent() ? schemeDB.get() : null;
            return schemeIdBulk;
        } catch (Exception e) {
            logger.error("error insertSchemeIdRecord():: ", e);
            error = e.getMessage();
        }
        if (error != null) {
            Optional<SchemeIdBase> schemeIdBaseList = schemeIdBaseRepository.findByScheme(schemeIdBulk.getScheme().toString());

            if (error.indexOf("ER_DUP_ENTRY") > -1) {
                if (error.indexOf("'PRIMARY'") > -1 /*&& ()*/) {
                    //    schemeIdRecord.getScheme();
                }
                /*else if(schemeIdRecord.get())
                {
                    schemeIdBaseList.ge
                }*/
                else {
                    logger.error("error insertSchemeIdRecord():: Unable to attempt to solve error");
                    throw new CisException(HttpStatus.BAD_REQUEST, "Unable to attempt to solve error");
                }

            } else {
                logger.debug("BulkSchemeIdService.insertSchemeIdRecord() - Response :: {}", schemeIdBulk);
                return schemeIdBulk;
            }
        }

        return null;
    }


    public Map<String, Object> getNewRecord(String schemeName, String diffSchemeId, String systemId) {
        logger.debug("BulkSchemeIdService.getNewRecord() schemeName-{} :: diffSchemeId - {}:: systemId - {}", schemeName, diffSchemeId, systemId);
        Map<String, Object> schemeIdRecord = new LinkedHashMap<>();
        schemeIdRecord.put("scheme", schemeName);
        schemeIdRecord.put("schemeId", diffSchemeId);
        schemeIdRecord.put("sequence", sctIdHelper.getSequence(diffSchemeId));
        schemeIdRecord.put("checkDigit", sctIdHelper.getCheckDigit(diffSchemeId));

        if (systemId != "null") {
            schemeIdRecord.put("systemId", systemId);
        } else {
            schemeIdRecord.put("systemId", sctIdHelper.guid());
        }
        logger.debug("BulkSchemeIdService.getNewRecord() - Response size-:: {}", (null==schemeIdRecord?"0":schemeIdRecord.size()));
        return schemeIdRecord;
    }


    public BulkJob generateSchemeIds(AuthenticateResponseDto token, SchemeName schemeName, SchemeIdBulkGenerationRequestDto schemeIdBulkDto) throws CisException {
        logger.debug("BulkSchemeIdService.generateSchemeIds() token - {} :: schemeName - {} :: schemeIdBulkDto - {}", token, schemeName, schemeIdBulkDto);
        SchemeIdBulkGenerate bulkGenerate = new SchemeIdBulkGenerate();
        bulkGenerate.setQuantity(schemeIdBulkDto.getQuantity());
        bulkGenerate.setSystemIds(schemeIdBulkDto.getSystemIds());
        bulkGenerate.setSoftware(schemeIdBulkDto.getSoftware());
        bulkGenerate.setComment(schemeIdBulkDto.getComment());


        boolean able = schemeIdService.isAbleUser(String.valueOf(schemeName), token);
        if (!able) {
            logger.error("error generateSchemeIds():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
        if ((schemeIdBulkDto.getSystemIds().length != 0 && (schemeIdBulkDto.getSystemIds().length != schemeIdBulkDto.getQuantity()))) {
            logger.error("error generateSchemeIds():: SystemIds quantity is not equal to quantity requirement.");
            throw new CisException(HttpStatus.BAD_REQUEST, "SystemIds quantity is not equal to quantity requirement");
        }
        if (schemeIdBulkDto.getSystemIds() != null || schemeIdBulkDto.getSystemIds().length == 0) {
            bulkGenerate.setAutoSysId(true);
        }
        bulkGenerate.setType(JobTypeConstants.GENERATE_SCHEMEIDS);
        bulkGenerate.setAuthor(token.getName());
        bulkGenerate.setModel(ModelsConstants.SCHEME_ID);
        bulkGenerate.setScheme(schemeName);
// Type is set here not as an attribute
        BulkJob bulk = new BulkJob();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String regString = objectMapper.writeValueAsString(bulkGenerate);
            bulk.setName(JobTypeConstants.GENERATE_SCHEMEIDS);
            bulk.setStatus("0");
            bulk.setRequest(regString);

        } catch (JsonProcessingException e) {
            logger.error("error generateSchemeIds() :: ", e);
            throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        BulkJob resultJob = this.bulkJobRepository.save(bulk);
        logger.debug("BulkSchemeIdService.generateSchemeIds() - Response :: {}", resultJob);
        return resultJob;
    }

    // Register SchemeId

    public BulkJob registerSchemeIds(AuthenticateResponseDto token, SchemeName schemeName, SchemeIdBulkRegisterRequestDto request) throws CisException {
        logger.debug("BulkSchemeIdService.registerSchemeIds() token - {} :: schemeName - {} :: request - {}", token, schemeName, request);
        BulkJob bulk = this.registerBulkSchemeIds(token, schemeName, request);
        logger.debug("BulkSchemeIdService.registerSchemeIds() - Response :: {}", bulk);
        return bulk;
    }

    public BulkJob registerBulkSchemeIds(AuthenticateResponseDto token, SchemeName schemeName, SchemeIdBulkRegisterRequestDto schemeIdBulkRegisterDto) throws CisException {
        logger.debug("BulkSchemeIdService.registerBulkSchemeIds() token - {} ::schemeName- {} :: schemeIdBulkRegisterDto - {}", token, schemeName, schemeIdBulkRegisterDto);
        BulkJob bulkJob = new BulkJob();
        if (schemeIdService.isAbleUser(String.valueOf(schemeName), token)) {
            SchemeIdBulkRegister bulkRegister = new SchemeIdBulkRegister();
            bulkRegister.setRecords(schemeIdBulkRegisterDto.getRecords());
            bulkRegister.setSoftware(schemeIdBulkRegisterDto.getSoftware());
            bulkRegister.setComment(schemeIdBulkRegisterDto.getComment());

            if (schemeIdBulkRegisterDto.getRecords() == null || schemeIdBulkRegisterDto.getRecords().size() == 0) {
                logger.error("error registerBulkSchemeIds():: Records property cannot be empty.");
                throw new CisException(HttpStatus.BAD_REQUEST, "Records property cannot be empty.");
            }
            bulkRegister.setType(JobTypeConstants.REGISTER_SCHEMEIDS);
            bulkRegister.setAuthor(token.getName());
            bulkRegister.setModel(ModelsConstants.SCHEME_ID);
            bulkRegister.setScheme(schemeName.toString());

            BulkJob bulk = new BulkJob();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String regString = objectMapper.writeValueAsString(bulkRegister);
                bulk.setName(JobTypeConstants.REGISTER_SCHEMEIDS);
                bulk.setStatus("0");
                bulk.setRequest(regString);

            } catch (JsonProcessingException e) {
                logger.error("error registerBulkSchemeIds():: ", e);
                throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            BulkJob resultJob = this.bulkJobRepository.save(bulk);
            logger.debug("BulkSchemeIdService.registerBulkSchemeIds() - Response :: {}", resultJob);
            return resultJob;

        } else {
            logger.error("error registerBulkSchemeIds():: No permission for the selected operation.");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    // Reserve SchemeId bulk


    public BulkJob reserveSchemeIds(AuthenticateResponseDto token, SchemeName schemeName, SchemeIdBulkReserveRequestDto request) throws CisException {
        logger.debug("BulkSchemeIdService.reserveSchemeIds() token - {} :: schemeName - {} :: request - {}", token, schemeName, request);
        BulkJob bulkJob = this.reserveBulkSchemeIds(token, schemeName, request);
        logger.debug("BulkSchemeIdService.reserveSchemeIds() - Response :: {}", bulkJob);
        return bulkJob;
    }

    public BulkJob reserveBulkSchemeIds(AuthenticateResponseDto token, SchemeName schemeName, SchemeIdBulkReserveRequestDto request) throws CisException {
        logger.debug("BulkSchemeIdService.reserveBulkSchemeIds() token - {} ::schemeName -  {} :: request - {}", token, schemeName, request);
        BulkJob bulkJob = new BulkJob();
        if (schemeIdService.isAbleUser(String.valueOf(schemeName), token)) {

            SchemeIdBulkReserve bulkReserve = new SchemeIdBulkReserve();
            bulkReserve.setQuantity(request.getQuantity());
            bulkReserve.setSoftware((request.getSoftware()));
            bulkReserve.setExpirationDate(request.getExpirationDate());
            bulkReserve.setComment(request.getComment());

            if ((null == request.getQuantity()) || request.getQuantity() < 1) {
                logger.error("error reserveBulkSchemeIds():: Quantity property cannot be lower to 1.");
                throw new CisException(HttpStatus.BAD_REQUEST, "Quantity property cannot be lower to 1.");
            }
            bulkReserve.setType(JobTypeConstants.RESERVE_SCHEMEIDS);
            bulkReserve.setModel(ModelsConstants.SCHEME_ID);
            bulkReserve.setAuthor(token.getName());
            bulkReserve.setScheme(schemeName.toString());

            BulkJob bulk = new BulkJob();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String regString = objectMapper.writeValueAsString(bulkReserve);
                bulk.setName(JobTypeConstants.RESERVE_SCHEMEIDS);
                bulk.setStatus("0");
                bulk.setRequest(regString);

            } catch (JsonProcessingException e) {
                logger.error("error reserveBulkSchemeIds():: ", e);
                throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            BulkJob resultJob = this.bulkJobRepository.save(bulk);
            logger.debug("BulkSchemeIdService.reserveBulkSchemeIds() - Response :: {}", resultJob);
            return resultJob;

        } else {
            logger.error("error reserveBulkSchemeIds():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    //deprecateSchemeIds

    public BulkJob deprecateSchemeIds(AuthenticateResponseDto token, SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws CisException {
        logger.debug("BulkSchemeIdService.deprecateSchemeIds() token - {} ::schemeName- {} :: request - {}", token, schemeName, request);
        BulkJob bulk = this.deprecateBulkSchemeIds(token, schemeName, request);
        logger.debug("BulkSchemeIdService.deprecateSchemeIds() - Response :: {}", bulk);
        return bulk;
    }

    public BulkJob deprecateBulkSchemeIds(AuthenticateResponseDto token, SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws CisException {
        logger.debug("BulkSchemeIdService.deprecateBulkSchemeIds() token - {} :: schemeName - {} :: request - {} ", token, schemeName, request);
        BulkJob bulkJob = new BulkJob();
        if (schemeIdService.isAbleUser(String.valueOf(schemeName), token)) {
            BulkSchemeIdUpdate bulkSchemeIdUpdate = new BulkSchemeIdUpdate();
            bulkSchemeIdUpdate.setSchemeIds(request.getSchemeIds());
            bulkSchemeIdUpdate.setSoftware(request.getSoftware());
            bulkSchemeIdUpdate.setComment(request.getComment());

            if (request.getSchemeIds() == null || request.getSchemeIds().size() < 1) {
                logger.error("error deprecateBulkSchemeIds():: SchemeIds property cannot be empty.");
                throw new CisException(HttpStatus.UNAUTHORIZED, "SchemeIds property cannot be empty.");
            }
            bulkSchemeIdUpdate.setType(JobTypeConstants.DEPRECATE_SCHEMEIDS);
            bulkSchemeIdUpdate.setModel(ModelsConstants.SCHEME_ID);
            bulkSchemeIdUpdate.setAuthor(token.getName());
            bulkSchemeIdUpdate.setScheme(schemeName.toString());/*schemeName.schemeName.toUpperCase();*/

            return createJob(bulkSchemeIdUpdate, "deprecate");

        } else {
            logger.error("error deprecateBulkSchemeIds():: No permission for the selected operation.");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }


    public BulkJob createJob(BulkSchemeIdUpdate bulkSchemeIdUpdate, String functionType) throws CisException {
        logger.debug("BulkSchemeIdService.createJob()bulkSchemeIdUpdate- {} :: functionType-{}", bulkSchemeIdUpdate, functionType);
        BulkJob bulk = new BulkJob();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String regString = objectMapper.writeValueAsString(bulkSchemeIdUpdate);
            if (functionType.equalsIgnoreCase("release"))
                bulk.setName(JobTypeConstants.RELEASE_SCHEMEIDS);
            else if (functionType.equalsIgnoreCase("publish"))
                bulk.setName(JobTypeConstants.PUBLISH_SCHEMEIDS);
            else if (functionType.equalsIgnoreCase("deprecate"))
                bulk.setName(JobTypeConstants.DEPRECATE_SCHEMEIDS);
            bulk.setStatus("0");
            bulk.setRequest(regString);

        } catch (JsonProcessingException e) {
            logger.error("error createJob():: ", e);
            throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        BulkJob resultJob = this.bulkJobRepository.save(bulk);
        logger.debug("BulkSchemeIdService.createJob() - Response :: {}", resultJob);
        return resultJob;
    }

//releaseSchemeIds

    public BulkJob releaseSchemeIds(AuthenticateResponseDto token, SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws CisException {
        logger.debug("BulkSchemeIdService.releaseSchemeIds() token - {} ::schemeName- {} :: request -{}", token, schemeName, request);
        BulkJob bulkJob = this.releaseBulkSchemeIds(token, schemeName, request);
        logger.debug("BulkSchemeIdService.releaseSchemeIds() - Response :: {}", bulkJob);
        return bulkJob;
    }

    public BulkJob releaseBulkSchemeIds(AuthenticateResponseDto token, SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws CisException {
        logger.debug("BulkSchemeIdService.releaseBulkSchemeIds() token - {} ::schemeName- {} :: request - {}", token, schemeName, request);
        BulkJob bulkJob = new BulkJob();

        if (schemeIdService.isAbleUser(String.valueOf(schemeName), token)) {

            BulkSchemeIdUpdate bulkSchemeIdUpdate = new BulkSchemeIdUpdate();
            bulkSchemeIdUpdate.setSchemeIds(request.getSchemeIds());
            bulkSchemeIdUpdate.setSoftware(request.getSoftware());
            bulkSchemeIdUpdate.setComment(request.getComment());

            if (request.getSchemeIds() == null || request.getSchemeIds().size() < 1) {
                logger.error("error releaseBulkSchemeIds():: SchemeIds property cannot be empty.");
                throw new CisException(HttpStatus.UNAUTHORIZED, "SchemeIds property cannot be empty.");
            }

            bulkSchemeIdUpdate.setType(JobTypeConstants.RELEASE_SCHEMEIDS);
            bulkSchemeIdUpdate.setModel(ModelsConstants.SCHEME_ID);
            bulkSchemeIdUpdate.setAuthor(token.getName());
            bulkSchemeIdUpdate.setScheme(schemeName.toString());/*schemeName.schemeName.toUpperCase();*/

            BulkJob bulk = createJob(bulkSchemeIdUpdate, "release");
            logger.debug("BulkSchemeIdService.releaseBulkSchemeIds() - Response :: {}", bulk);
            return bulk;


        } else {
            logger.error("error releaseBulkSchemeIds():: No permission for the selected operation.");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    //publishSchemeIds
    public BulkJob publishSchemeIds(AuthenticateResponseDto token, SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws CisException {
        logger.debug("BulkSchemeIdService.publishSchemeIds() token-{} ::schemeName- {} :: request -{}", token, schemeName, request);
        BulkJob bulk = this.publishBulkSchemeIds(token, schemeName, request);
        logger.debug("BulkSchemeIdService.publishSchemeIds() - Response :: {}", bulk);
        return bulk;
    }

    public BulkJob publishBulkSchemeIds(AuthenticateResponseDto token, SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws CisException {
        logger.debug("BulkSchemeIdService.publishBulkSchemeIds() token-{} ::schemeName- {} :: request -{}", token, schemeName, request);
        BulkJob bulkJob = new BulkJob();
        if (schemeIdService.isAbleUser(String.valueOf(schemeName), token)) {

            BulkSchemeIdUpdate bulkSchemeIdUpdate = new BulkSchemeIdUpdate();
            bulkSchemeIdUpdate.setSchemeIds(request.getSchemeIds());
            bulkSchemeIdUpdate.setSoftware(request.getSoftware());
            bulkSchemeIdUpdate.setComment(request.getComment());

            if (request.getSchemeIds() == null || request.getSchemeIds().size() < 1) {
                logger.error("error publishBulkSchemeIds():: SchemeIds property cannot be empty.");
                throw new CisException(HttpStatus.UNAUTHORIZED, "SchemeIds property cannot be empty.");
            }

            bulkSchemeIdUpdate.setType(JobTypeConstants.PUBLISH_SCHEMEIDS);
            bulkSchemeIdUpdate.setModel(ModelsConstants.SCHEME_ID);
            bulkSchemeIdUpdate.setAuthor(token.getName());
            bulkSchemeIdUpdate.setScheme(schemeName.toString());/*schemeName.schemeName.toUpperCase();*/

            BulkJob bulk = createJob(bulkSchemeIdUpdate, "publish");
            logger.debug("BulkSchemeIdService.publishBulkSchemeIds() - Response :: {}", bulk);
            return bulk;

        } else {
            logger.error("error publishBulkSchemeIds():: No permission for the selected operation.");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }


}

