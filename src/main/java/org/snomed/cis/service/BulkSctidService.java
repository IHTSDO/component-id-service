package org.snomed.cis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.controller.SecurityController;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.BulkJobRepository;
import org.snomed.cis.repository.PermissionsNamespaceRepository;
import org.snomed.cis.repository.PermissionsSchemeRepository;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.util.JobTypeConstants;
import org.snomed.cis.util.ModelsConstants;
import org.snomed.cis.util.SctIdHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BulkSctidService implements CisConstants {
    private final Logger logger = LoggerFactory.getLogger(BulkSctidService.class);
    @Autowired
    private SctidRepository repo;

    @Autowired
    AuthenticateToken authenticateToken;

    @Autowired
    private BulkJobRepository bulkJobRepository;

    @Autowired
    private PermissionsNamespaceRepository permissionsNamespaceRepository;

    @Autowired
    private PermissionsSchemeRepository permissionsSchemeRepository;

    @Autowired
    private SctIdHelper sctIdHelper;

    @Autowired
    JobTypeConstants jobType;

    @Autowired
    ModelsConstants modelsConstants;

    @Autowired
    private SecurityController securityController;

    public List<Sctid> postSctByIds(SctIdRequest ids) throws CisException {
        return this.postValidScts(ids);
    }

    public List<Sctid> getSctByIds(String ids) throws CisException {
        return this.validScts(ids);
    }

    public List<Sctid> getByIds(List<String> ids) {
        return repo.findBySctidIn(ids);
    }

    public List<Sctid> validScts(String ids) throws CisException {
        logger.debug("BulkSctidService.validScts() ids-{}", ids);
        String idsWthtSpace = ids.replaceAll("\\s+", "");
        String[] sctidsArray = idsWthtSpace.split(",");
        ArrayList<String> sctidsReqArray = new ArrayList<String>(Arrays.asList(sctidsArray));
        this.validSctidCheck(sctidsReqArray);
        List<Sctid> resArr = this.getByIds(sctidsReqArray);
        List<String> respSctArr = new ArrayList<>();
        for (int i = 0; i < resArr.size(); i++) {
            Sctid sctObj = resArr.get(i);
            respSctArr.add(sctObj.getSctid());
        }
        Set<String> rqSet = new HashSet<>(sctidsReqArray);
        Set<String> respSet = new HashSet<>(respSctArr);
        Set<String> resultDiff = new HashSet<>(rqSet);
        resultDiff.removeAll(respSet);
        if (resultDiff.size() > 0) {
            for (String diffSctid :
                    resultDiff) {
                Sctid sctidObj = this.getFreeRecord(diffSctid, null);
                resArr.add(sctidObj);
            }
        }
        logger.debug("BulkSctidService.validScts() - Response size :: {}", (null==resArr?"0":resArr.size()));
        return resArr;
    }

    public List<Sctid> postValidScts(SctIdRequest ids) throws CisException {
        logger.debug("BulkSctidService.postValidScts() ids :: {}", ids);
        String idsWthtSpace = ids.getSctids().replaceAll("\\s+", "");
        String[] sctidsArray = idsWthtSpace.split(",");
        ArrayList<String> sctidsReqArray = new ArrayList<String>(Arrays.asList(sctidsArray));
        this.validSctidCheck(sctidsReqArray);
        List<Sctid> resArr = this.getByIds(sctidsReqArray);
        List<String> respSctArr = new ArrayList<>();
        for (int i = 0; i < resArr.size(); i++) {
            Sctid sctObj = resArr.get(i);
            respSctArr.add(sctObj.getSctid());
        }
        Set<String> rqSet = new HashSet<>(sctidsReqArray);
        Set<String> respSet = new HashSet<>(respSctArr);
        Set<String> resultDiff = new HashSet<>(rqSet);
        resultDiff.removeAll(respSet);
        if (resultDiff.size() > 0) {
            for (String diffSctid :
                    resultDiff) {
                Sctid sctidObj = this.getFreeRecord(diffSctid, null);
                resArr.add(sctidObj);
            }
        }
        logger.debug("BulkSctidService.postValidScts() - Response size :: {}", (null==resArr?"0":resArr.size()));
        return resArr;
    }

    public void validSctidCheck(ArrayList<String> sctidsArray) throws CisException {
        logger.debug("BulkSctidService.validSctidCheck() sctidsArray :: {} ", sctidsArray);
        for (int i = 0; i < sctidsArray.size(); i++) {
            if (!(sctIdHelper.validSCTId(sctidsArray.get(i)))) {
                logger.error("error validSctidCheck():: Not a Valid Sctid: {}", sctidsArray.get(i));
                throw new CisException(HttpStatus.NOT_ACCEPTABLE, "Not a Valid Sctid:" + sctidsArray.get(i));
            }
        }
    }

    public Sctid getFreeRecord(String sctId, String systemId) {
        logger.debug("BulkSctidService.getFreeRecord() sctId :: {} , systemId :: {} ", sctId, systemId);
        Map<String, Object> sctIdRecord = getNewRecord(sctId, systemId);
        sctIdRecord.put("status", AVAILABLE);
        var newRecord = insertSCTIDRecord(sctIdRecord);
        logger.debug("BulkSctidService.getFreeRecord() - Response :: {}", newRecord);
        return newRecord;
    }

    public Sctid insertSCTIDRecord(Map<String, Object> sctIdRecord) {
        logger.debug("BulkSctidService.insertSCTIDRecord() sctIdRecord - {} ", sctIdRecord);
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
            if (it.getKey().equalsIgnoreCase("sctid")) {
                sctid = (String) it.getValue();
            } else if (it.getKey().equalsIgnoreCase("sequence")) {
                sequence = (long) it.getValue();
            } else if (it.getKey().equalsIgnoreCase("namespace")) {
                namespace = (int) it.getValue();
            } else if (it.getKey().equalsIgnoreCase("partitionId")) {
                partitionId = (String) it.getValue();
            } else if (it.getKey().equalsIgnoreCase("checkDigit")) {
                checkDigit = (int) it.getValue();
            } else if (it.getKey().equalsIgnoreCase("systemId")) {
                systemId = (String) it.getValue();
            } else if (it.getKey().equalsIgnoreCase("status")) {
                status = (String) it.getValue();
            }
        }
        Sctid sctObj = Sctid.builder().sctid(sctid).sequence(sequence).namespace(namespace)
                .partitionId(partitionId).checkDigit(checkDigit).systemId(systemId).status(status)
                .build();
        Sctid sct = repo.save(sctObj);
        logger.debug("BulkSctidService.insertSCTIDRecord() - Response :: {}", sct);
        return sct;
    }

    public Map<String, Object> getNewRecord(String sctIdInput, String inpSystemId) {
        logger.debug("BulkSctidService.getNewRecord() sctIdInput - {} :: inpSystemId - {}", sctIdInput, inpSystemId);
        Map<String, Object> sctIdRecord = new LinkedHashMap<>();
        sctIdRecord.put("sctid", sctIdInput);
        sctIdRecord.put("sequence", sctIdHelper.getSequence(sctIdInput));
        sctIdRecord.put("namespace", sctIdHelper.getNamespace(sctIdInput));
        sctIdRecord.put("partitionId", sctIdHelper.getPartition(sctIdInput));
        sctIdRecord.put("checkDigit", sctIdHelper.getCheckDigit(sctIdInput));

        if (null != inpSystemId && !(inpSystemId.isEmpty())) {
            sctIdRecord.put("systemId", inpSystemId);
        } else {
            sctIdRecord.put("systemId", sctIdHelper.guid());
        }
        logger.debug("BulkSctidService.getNewRecord() - Response size:: {}", (null==sctIdRecord?"0":sctIdRecord.size()));
        return sctIdRecord;
    }


    public List<Sctid> getSctidBySystemIds(String systemIdStr, Integer namespaceId) {
        logger.debug("BulkSctidService.getSctidBySystemIds() systemIdStr - {} :: namespaceId - {} :: schemeIds -{}", systemIdStr, namespaceId);
        String[] systemIdsArray = systemIdStr.replaceAll("\\s+", "").split(",");
        List<Sctid> sctidList = repo.findBySystemIdInAndNamespace(Arrays.asList(systemIdsArray), namespaceId);
        logger.debug("BulkSctidService.getSctidBySystemIds() - Response size :: {}", (null==sctidList?"0":sctidList.size()));
        return sctidList;

    }

    public BulkJob registerSctids(AuthenticateResponseDto token, RegistrationDataDTO request) throws CisException {
        logger.debug("BulkSctidService.registerSctids() token-{} :: request-{} ", token.toString(), request);
        BulkJob bulk = this.registerScts(token, request);
        logger.debug("BulkSctidService.registerSctids() - Response :: {}", bulk);
        return bulk;
    }

    public BulkJob registerScts(AuthenticateResponseDto token, RegistrationDataDTO registrationData) throws CisException {
        logger.debug("BulkSctidService.registerScts() token-{} :: registrationData - {} ", token.toString(), registrationData);
        BulkJob resultJob = new BulkJob();
        if (this.isAbleUser(registrationData.getNamespace().toString(), token)) {
            SctidBulkRegister sctidBulkRegister = new SctidBulkRegister();
            sctidBulkRegister.setRecords(registrationData.getRecords());
            sctidBulkRegister.setNamespace(registrationData.getNamespace());
            sctidBulkRegister.setSoftware(registrationData.getSoftware());
            sctidBulkRegister.setComment(registrationData.getComment());

            if ((registrationData.getRecords() == null) || (registrationData.getRecords().length == 0)) {
                logger.error("error registerScts():: Input 'records' cannot be 0 or empty or null.");
                throw new CisException(HttpStatus.ACCEPTED, "Input 'records' cannot be 0 or empty or null.");
            } else {
                int namespace;
                boolean error = false;
                for (RegistrationRecordsDTO record : registrationData.getRecords()) {
                    namespace = sctIdHelper.getNamespace(record.getSctid());
                    if (namespace != registrationData.getNamespace()) {
                        error = true;
                        logger.error("error registerScts():: Differences between generated namespace and input 'namespace'. Generated namespace : {} from SCTID: {} and input 'namespace': {} ",namespace, record.getSctid(), registrationData.getNamespace());
                        throw new CisException(HttpStatus.CONFLICT, "Differences between generated namespace and input 'namespace'.");
                    }
                }
                if (!error) {
                    sctidBulkRegister.setAuthor(token.getName());
                    sctidBulkRegister.setModel(modelsConstants.SCTID);
                    sctidBulkRegister.setType(jobType.REGISTER_SCTIDS);
                    /*RegistrationDataDTO registrationDataDTO = new RegistrationDataDTO(registrationData.getRecords(), registrationData.getNamespace(),
                            registrationData.getSoftware(), registrationData.getComment(), registrationData.getModel(), registrationData.getAuthor(), registrationData.getType());
                  */
                    BulkJob bulk = new BulkJob();
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String regString = objectMapper.writeValueAsString(sctidBulkRegister);
                        bulk.setName(jobType.REGISTER_SCTIDS);
                        bulk.setStatus("0");
                        bulk.setRequest(regString);
                        bulk.setCreated_at(LocalDateTime.now());
                        bulk.setModified_at(LocalDateTime.now());
                    } catch (JsonProcessingException e) {
                        logger.error("error registerScts():: While converting json object with namespace: {} to String. Exception: {}",sctidBulkRegister.getNamespace(), e);
                        throw new CisException(HttpStatus.BAD_REQUEST, "Json Processing Exception:"+e.getMessage());
                    }

                    resultJob = this.bulkJobRepository.save(bulk);
                    logger.debug("BulkSctidService.registerScts() - Response :: {}", resultJob);
                }
            }
        } else {
            logger.error("error registerScts():: User: {} has neither admin access nor namespace Permission for namespace: {} for the selected operation.",token.toString(),registrationData.getNamespace().toString());
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation.");
        }
        return resultJob;
    }

    public boolean isAbleUser(String namespace, AuthenticateResponseDto authenticateResponseDto) {
        logger.debug("BulkSctidService.isAbleUser() namespace - {} :: authenticateResponseDto - {} :: schemeIds -{}", namespace, authenticateResponseDto.toString());
        List<String> groups = authenticateResponseDto.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
        boolean isAble = false;
        if (groups.contains("component-identifier-service-admin") || hasNamespacePermission(namespace, authenticateResponseDto)) {
            isAble = true;
        }
        logger.debug("BulkSctidService.isAbleUser() - Response :: {}", isAble);
        return isAble;
    }

    public boolean hasNamespacePermission(String namespace, AuthenticateResponseDto authenticateResponseDto) {
        logger.debug("BulkSctidService.hasNamespacePermission() namespace - {} :: authenticateResponseDto - {} ", namespace, authenticateResponseDto.toString());
        boolean able = false;
        if (!"false".equalsIgnoreCase(namespace)) {
            List<PermissionsNamespace> permissionsNamespaceList = permissionsNamespaceRepository.findByNamespace(Integer.valueOf(namespace));
            List<String> possibleGroups = new ArrayList<>();
            for (PermissionsNamespace perm : permissionsNamespaceList) {
                if (("group").equalsIgnoreCase(perm.getRole())) {
                    possibleGroups.add(perm.getUsername());
                } else if ((authenticateResponseDto.getName()).equalsIgnoreCase(perm.getUsername())) {
                    able = true;
                }
            }
            if (!able) {
                List<String> roleAsGroups = authenticateResponseDto.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
                for (String group : roleAsGroups) {
                    if (group.equalsIgnoreCase("namespace-" + namespace))
                        able = true;
                    else if (possibleGroups.contains(group))
                        able = true;
                }
            } else {
                return able;
            }
        }
        logger.debug("BulkSctidService.hasNamespacePermission() - Response :: {}", able);
        return able;

    }


    public BulkJobResponseDto generateSctids(AuthenticateResponseDto token, SCTIDBulkGenerationRequestDto sctidBulkGenerationRequestDto) throws CisException {
        SctidBulkGenerate bulkGenerate = new SctidBulkGenerate();
        bulkGenerate.setNamespace(sctidBulkGenerationRequestDto.getNamespace());
        bulkGenerate.setPartitionId(sctidBulkGenerationRequestDto.getPartitionId());
        bulkGenerate.setQuantity(sctidBulkGenerationRequestDto.getQuantity());
        bulkGenerate.setSystemIds(sctidBulkGenerationRequestDto.getSystemIds());
        bulkGenerate.setSoftware(sctidBulkGenerationRequestDto.getSoftware());
        bulkGenerate.setComment(sctidBulkGenerationRequestDto.getComment());
        bulkGenerate.setGenerateLegacyIds(sctidBulkGenerationRequestDto.getGenerateLegacyIds());

        boolean able = isAbleUser(sctidBulkGenerationRequestDto.getNamespace().toString(), token);
        if (!able) {
            logger.error("error generateSctids():: User: {} has neither admin access nor namespace permission for namespace: {} for the selected operation.",token.toString(),sctidBulkGenerationRequestDto.getNamespace().toString());
            throw new CisException(HttpStatus.UNAUTHORIZED, "user has no permission for the selected operation.");
        }
        if(null!=sctidBulkGenerationRequestDto.getQuantity() && sctidBulkGenerationRequestDto.getQuantity() <= 0)
            throw new CisException(HttpStatus.BAD_REQUEST, "quantity value must be positive number");
        if (((sctidBulkGenerationRequestDto.getNamespace() == 0) && (!"0".equalsIgnoreCase(sctidBulkGenerationRequestDto.getPartitionId().substring(0, 1))))
                || (0 != (sctidBulkGenerationRequestDto.getNamespace()) && (!"1".equalsIgnoreCase(sctidBulkGenerationRequestDto.getPartitionId().substring(0, 1))))) {
            logger.error("error generateSctids():: namespace - {} and partitionId - {} parameters are not consistent.",sctidBulkGenerationRequestDto.getNamespace(),sctidBulkGenerationRequestDto.getPartitionId());
            throw new CisException(HttpStatus.BAD_REQUEST, "Namespace and partitionId parameters are not consistent.");
        }
        if ((null!=bulkGenerate.getSystemIds() && sctidBulkGenerationRequestDto.getSystemIds().size()> 0 && (sctidBulkGenerationRequestDto.getSystemIds().size() != sctidBulkGenerationRequestDto.getQuantity()))) {
            logger.error("error generateSctids():: SystemIds quantity -{} is not equal to quantity -{} requirement",sctidBulkGenerationRequestDto.getSystemIds().size(),sctidBulkGenerationRequestDto.getQuantity());
            throw new CisException(HttpStatus.BAD_REQUEST, "SystemIds quantity is not equal to input 'quantity'.");
        }

        bulkGenerate.setAuthor(token.getName());
        bulkGenerate.model = modelsConstants.SCTID;
        //bulkGenerate.setType(jobType.GENERATE_SCTIDS);
        bulkGenerate.type = jobType.GENERATE_SCTIDS;

        if (
                ((null==bulkGenerate.getSystemIds()) || (null!=bulkGenerate.getSystemIds() && sctidBulkGenerationRequestDto.getSystemIds().size() == 0))
                &&
                (null!=bulkGenerate.getGenerateLegacyIds() && "TRUE".equalsIgnoreCase((sctidBulkGenerationRequestDto.getGenerateLegacyIds()))
                        && ("0".equalsIgnoreCase(sctidBulkGenerationRequestDto.getPartitionId().substring(1))))) {
            List<String> arrayUid = new ArrayList<>();
            for (int i = 0; i < sctidBulkGenerationRequestDto.getQuantity(); i++) {
                arrayUid.add(SctIdHelper.guid());
            }
            bulkGenerate.setSystemIds(arrayUid);
            bulkGenerate.setAutoSysId(true);
        }
        List<BulkJob> additionalJob = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String reqAsString = "";
        try {
            reqAsString = objectMapper.writeValueAsString(bulkGenerate);
        } catch (JsonProcessingException e) {
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "error while parsing json");
        }
        String jsonFormattedString = reqAsString.replaceAll("\\\\", "");

        BulkJob bulkJob = new BulkJob();

        bulkJob.setName(jobType.GENERATE_SCTIDS);
        bulkJob.setStatus("0");
        bulkJob.setRequest(jsonFormattedString);
        bulkJob = bulkJobRepository.save(bulkJob);

        if (/*bulkGenerate.isAutoSysId() != null*/  ("true".equalsIgnoreCase(bulkGenerate.getGenerateLegacyIds()) && ("0".equalsIgnoreCase(bulkGenerate.getPartitionId().substring(1))))) {
            // SCTIDBulkGenerationRequestDto generationMetaData = sctidBulkGenerationRequestDto.copy();
            SctidBulkGenerate bulkGenerate1 = bulkGenerate.copy();
            bulkGenerate.model = modelsConstants.SCHEME_ID;

            if (isSchemeAbleUser("SNOMEDID", token)) {
                if (able) {
                    bulkGenerate1.setScheme("SNOMEDID");
                    bulkGenerate1.setType(jobType.GENERATE_SCHEMEIDS);
                    BulkJob bulkJobScheme = new BulkJob();
                    bulkJobScheme.setName(jobType.GENERATE_SCHEMEIDS);
                    bulkJobScheme.setStatus("0");
                    String genAsStringFormat = reqAsString.replaceAll("\\\\", "");
                    bulkJobScheme.setRequest(genAsStringFormat);
                    bulkJobScheme = bulkJobRepository.save(bulkJobScheme);
                    additionalJob.add(bulkJobScheme);
                    if (isSchemeAbleUser("CTV3ID", token)) {
                        if (able) {
                            //   SCTIDBulkGenerationRequestDto generationCTV3IDMetadata = generationMetaData.copy();
                            SctidBulkGenerate generationCTV3IDMetadata = bulkGenerate1.copy();
                            generationCTV3IDMetadata.setScheme("CTV3ID");
                            generationCTV3IDMetadata.setType(jobType.GENERATE_SCHEMEIDS);
                            BulkJob bulkJobSchemeCTV = new BulkJob();
                            bulkJobSchemeCTV.setStatus("0");
                            String genMetaAsStrFormat = reqAsString.replaceAll("\\\\", "");
                            bulkJobSchemeCTV.setRequest(genMetaAsStrFormat);
                            bulkJobSchemeCTV = bulkJobRepository.save(bulkJobSchemeCTV);
                            additionalJob.add(bulkJobSchemeCTV);
                            BulkJobResponseDto bulkJobResponseDto = new BulkJobResponseDto(bulkJob,additionalJob);
                            return bulkJobResponseDto;

                        }//if(able)
                        else {
                            BulkJobResponseDto bulkJobResponseDto = new BulkJobResponseDto(bulkJob,additionalJob);
                            return bulkJobResponseDto;
                        }

                    }// isSchemableUser
                } else {
                    if (isSchemeAbleUser("CTV3ID", token)) {
                        if (able) {
                            SctidBulkGenerate generationCTV3IDMetadata = bulkGenerate1.copy();
                            generationCTV3IDMetadata.setScheme("CTV3ID");
                            generationCTV3IDMetadata.setType(jobType.GENERATE_SCHEMEIDS);
                            BulkJob bulkJobCtvMetaData = new BulkJob();
                            bulkJobCtvMetaData.setName(jobType.GENERATE_SCHEMEIDS);
                            bulkJobCtvMetaData.setStatus("0");
                            String genCTVAsStrFormat = reqAsString.replaceAll("\\\\", "");
                            bulkJobCtvMetaData.setRequest(genCTVAsStrFormat);
                            bulkJobCtvMetaData = bulkJobRepository.save(bulkJobCtvMetaData);
                            additionalJob.add(bulkJobCtvMetaData);
                            BulkJobResponseDto bulkJobResponseDto = new BulkJobResponseDto(bulkJob,additionalJob);
                            return bulkJobResponseDto;

                        } else {
                            BulkJobResponseDto bulkJobResponseDto = new BulkJobResponseDto(bulkJob,additionalJob);
                            return bulkJobResponseDto;
                        }
                    }

                }
            }
        } else {
            BulkJobResponseDto bulkJobResponseDto = new BulkJobResponseDto(bulkJob,additionalJob);
            return bulkJobResponseDto;
        }

        // }// if(able)
        return null;
    }

    public boolean isSchemeAbleUser(String schemeName, AuthenticateResponseDto authToken) {
        logger.debug("BulkSctidService.isSchemeAbleUser() schemeName-{} :: authToken -{}", schemeName, authToken.toString());
        List<String> groups = authToken.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
        boolean isAble = false;
        if (groups.contains("component-identifier-service-admin") || hasSchemePermission(schemeName, authToken)) {
            isAble = true;
        }
        logger.debug("BulkSctidService.isSchemeAbleUser() - Response :: {}", isAble);
        return isAble;
    }

    public boolean hasSchemePermission(String schemeName, AuthenticateResponseDto authToken) {
        logger.debug("BulkSctidService.hasSchemePermission() tauthTokenoken-{} :: schemeName-{} ", authToken.toString(), schemeName);
        boolean able = false;
        if (!"false".equalsIgnoreCase(schemeName)) {
            List<PermissionsScheme> permissionsSchemesList = permissionsSchemeRepository.findByScheme(schemeName);
            List<String> possibleGroups = new ArrayList<>();
            for (PermissionsScheme perm : permissionsSchemesList) {
                if (("group").equalsIgnoreCase(perm.getRole())) {
                    possibleGroups.add(perm.getUsername());
                } else if ((authToken.getName()).equalsIgnoreCase(perm.getUsername())) {
                    able = true;
                }
            }// possible groups
            if (!able) {
                List<String> roleAsGroups = authToken.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
                for (String group : roleAsGroups) {
                    if (possibleGroups.contains(group))
                        able = true;
                }
            }
        }
        logger.debug("BulkSctidService.hasSchemePermission() - Response :: {}", able);
        return able;
    }

    //Deprecate API
    public BulkJob deprecateSctid(AuthenticateResponseDto token, BulkSctRequestDTO deprecateBulkSctRequestDTO) throws CisException {
        logger.debug("BulkSctidService.deprecateSctid() token-{} :: deprecateBulkSctRequestDTO-{} ", token.toString(), deprecateBulkSctRequestDTO);
        BulkJob resultJob = new BulkJob();
        BulkSctRequest bulkSctRequest = new BulkSctRequest();
        bulkSctRequest.setSctids(deprecateBulkSctRequestDTO.getSctids());
        bulkSctRequest.setNamespace(deprecateBulkSctRequestDTO.getNamespace());
        bulkSctRequest.setSoftware(deprecateBulkSctRequestDTO.getSoftware());
        bulkSctRequest.setComment(deprecateBulkSctRequestDTO.getComment());

        boolean able = isAbleUser(deprecateBulkSctRequestDTO.getNamespace().toString(), token);
        if (!able) {
            logger.error("error deprecateSctid():: User: {} has neither admin access nor namespace permission for namespace: {} for the selected operation.",token.toString(),deprecateBulkSctRequestDTO.getNamespace().toString());
            throw new CisException(HttpStatus.UNAUTHORIZED, "user has no permission for the selected operation.");
        } else {
            if (null == deprecateBulkSctRequestDTO.getSctids() || deprecateBulkSctRequestDTO.getSctids().length < 1) {
                logger.error("error deprecateSctid():: input 'sctids' cannot be 0 or empty or null.");
                throw new CisException(HttpStatus.ACCEPTED, "input 'sctids' cannot be 0 or empty or null.");
            } else {
                int namespace;
                boolean error = false;
                for (String sctid : deprecateBulkSctRequestDTO.getSctids()) {
                    namespace = sctIdHelper.getNamespace(sctid);
                    if (namespace != deprecateBulkSctRequestDTO.getNamespace()) {
                        error = true;
                        logger.error("error deprecateSctid():: Difference between generated namespace and input 'namespace'.Generated namespace: {} sctid: {} and input 'namespace': {}",namespace, sctid, deprecateBulkSctRequestDTO.getNamespace());
                        throw new CisException(HttpStatus.ACCEPTED, "Difference between generated namespace and input 'namespace'.");
                    }
                }
                if (!error) {
                    bulkSctRequest.setAuthor(token.getName());
                    bulkSctRequest.setModel(modelsConstants.SCTID);
                    bulkSctRequest.setType(jobType.DEPRECATE_SCTIDS);
                    //BulkSctRequestDTO deprecateBulkSctRequestDTO = new RegistrationDataDTO(registrationData.getRecords(), registrationData.getNamespace(),
                    //      registrationData.getSoftware(), registrationData.getComment(), registrationData.getModel(), registrationData.getAuthor(), registrationData.getType());
                    BulkJob bulk = new BulkJob();
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String regString = objectMapper.writeValueAsString(bulkSctRequest);
                        bulk.setName(jobType.DEPRECATE_SCTIDS);
                        bulk.setStatus("0");
                        bulk.setRequest(regString);
                        bulk.setCreated_at(LocalDateTime.now());
                        bulk.setModified_at(LocalDateTime.now());
                    } catch (JsonProcessingException e) {
                        logger.error("error deprecateSctid():: while converting JsonObject with namespace : {} to String. Exception is : {}",bulkSctRequest.getNamespace(), e);
                        throw new CisException(HttpStatus.BAD_REQUEST,"Json processing Exception: "+ e.getMessage());
                    }
                    resultJob = this.bulkJobRepository.save(bulk);
                }
            }

        }
        logger.debug("BulkSctidService.deprecateSctid() - Response :: {}", resultJob);
        return resultJob;
    }

    //Publish API
    public BulkJob publishSctid(AuthenticateResponseDto token, BulkSctRequestDTO publishBulkSctRequestDTO) throws CisException {
        logger.debug("BulkSctidService.publishSctid() token-{} :: publishBulkSctRequestDTO-{} ", token.toString(), publishBulkSctRequestDTO);
        BulkJob resultJob = new BulkJob();
        BulkSctRequest bulkSctRequest = new BulkSctRequest();
        bulkSctRequest.setSctids(publishBulkSctRequestDTO.getSctids());
        bulkSctRequest.setNamespace(publishBulkSctRequestDTO.getNamespace());
        bulkSctRequest.setSoftware(publishBulkSctRequestDTO.getSoftware());
        bulkSctRequest.setComment(publishBulkSctRequestDTO.getComment());
        boolean able = isAbleUser(publishBulkSctRequestDTO.getNamespace().toString(), token);
        if (!able) {
            logger.error("error publishSctid():: User: {} has neither admin access nor namespace Permission for namespace: {} for the selected operation.",token.toString(),publishBulkSctRequestDTO.getNamespace().toString());
            throw new CisException(HttpStatus.UNAUTHORIZED, "user has no permission for the selected operation.");
        } else {
            if (null == publishBulkSctRequestDTO.getSctids() || publishBulkSctRequestDTO.getSctids().length < 1) {
                logger.error("error publishSctid():: input 'sctids' cannot be empty or null.");
                throw new CisException(HttpStatus.ACCEPTED, "input 'sctids' cannot be empty or null.");
            } else {
                int namespace;
                boolean error = false;
                for (String sctid : publishBulkSctRequestDTO.getSctids()) {
                    namespace = sctIdHelper.getNamespace(sctid);
                    if (namespace != publishBulkSctRequestDTO.getNamespace()) {
                        error = true;
                        logger.error("error publishSctid():: Difference between generated namespace and input 'namespace'. Generated namespace: {} , sctid: {} and input 'namespace': {}",namespace, sctid, publishBulkSctRequestDTO.getNamespace());
                        throw new CisException(HttpStatus.ACCEPTED, "Difference between generated namespace and input 'namespace'.");
                    }
                }
                if (!error) {
                    bulkSctRequest.setAuthor(token.getName());
                    bulkSctRequest.setModel(modelsConstants.SCTID);
                    bulkSctRequest.setType(jobType.PUBLISH_SCTIDS);
                    //BulkSctRequestDTO deprecateBulkSctRequestDTO = new RegistrationDataDTO(registrationData.getRecords(), registrationData.getNamespace(),
                    //      registrationData.getSoftware(), registrationData.getComment(), registrationData.getModel(), registrationData.getAuthor(), registrationData.getType());
                    BulkJob bulk = new BulkJob();
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String regString = objectMapper.writeValueAsString(bulkSctRequest);
                        bulk.setName(jobType.PUBLISH_SCTIDS);
                        bulk.setStatus("0");
                        bulk.setRequest(regString);
                        bulk.setCreated_at(LocalDateTime.now());
                        bulk.setModified_at(LocalDateTime.now());
                    } catch (JsonProcessingException e) {
                        logger.error("error publishSctid():: Converting jsonObject with namespace - {} to String. Exception is: {}",bulkSctRequest.getNamespace(), e.getMessage());
                        throw new CisException(HttpStatus.BAD_REQUEST,"Json processing Exception: "+e.getMessage());
                    }

                    resultJob = this.bulkJobRepository.save(bulk);
                }
            }

        }
        logger.debug("BulkSctidService.publishSctid() - Response :: {}", resultJob);
        return resultJob;
    }

    //Release Sctid API
    public BulkJob releaseSctid(AuthenticateResponseDto token, BulkSctRequestDTO releaseBulkSctRequestDTO) throws CisException {
        logger.debug("BulkSctidService.releaseSctid() token-{} :: releaseBulkSctRequestDTO-{} ", token.toString(), releaseBulkSctRequestDTO);
        BulkJob resultJob = new BulkJob();
        BulkSctRequest bulkSctRequest = new BulkSctRequest();
        bulkSctRequest.setSctids(releaseBulkSctRequestDTO.getSctids());
        bulkSctRequest.setNamespace(releaseBulkSctRequestDTO.getNamespace());
        bulkSctRequest.setSoftware(releaseBulkSctRequestDTO.getSoftware());
        bulkSctRequest.setComment(releaseBulkSctRequestDTO.getComment());

        //UserDTO userObj = this.getAuthenticatedUser();
        boolean able = isAbleUser(releaseBulkSctRequestDTO.getNamespace().toString(), token);
        if (!able) {
            logger.error("error releaseSctid():: User: {} has neither admin access nor namespace permission for namespace: {} for the selected operation.",token.toString(),releaseBulkSctRequestDTO.getNamespace().toString());
            throw new CisException(HttpStatus.UNAUTHORIZED, "user has no permission for the selected operation.");
        } else {
            if (null == releaseBulkSctRequestDTO.getSctids() || releaseBulkSctRequestDTO.getSctids().length < 1) {
                logger.error("error releaseSctid():: input 'sctids' cannot be empty or null.");
                throw new CisException(HttpStatus.ACCEPTED, "input 'sctids' cannot be empty or null.");
            } else {
                int namespace;
                boolean error = false;
                for (String sctid : releaseBulkSctRequestDTO.getSctids()) {
                    namespace = sctIdHelper.getNamespace(sctid);
                    if (namespace != releaseBulkSctRequestDTO.getNamespace()) {
                        error = true;
                        logger.error("error releaseSctid()::Difference between generated namespace and input 'namespace'. Generated namespace: {}, sctid: {} and input 'namespace': {}",namespace, sctid, releaseBulkSctRequestDTO.getNamespace());
                        throw new CisException(HttpStatus.ACCEPTED, "Difference between generated namespace and input 'namespace'.");
                    }
                }
                if (!error) {
                    bulkSctRequest.setAuthor(token.getName());
                    bulkSctRequest.setModel(modelsConstants.SCTID);
                    bulkSctRequest.setType(jobType.RELEASE_SCTIDS);
                    //BulkSctRequestDTO deprecateBulkSctRequestDTO = new RegistrationDataDTO(registrationData.getRecords(), registrationData.getNamespace(),
                    //      registrationData.getSoftware(), registrationData.getComment(), registrationData.getModel(), registrationData.getAuthor(), registrationData.getType());
                    BulkJob bulk = new BulkJob();
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String regString = objectMapper.writeValueAsString(bulkSctRequest);
                        bulk.setName(jobType.RELEASE_SCTIDS);
                        bulk.setStatus("0");
                        bulk.setRequest(regString);
                        bulk.setCreated_at(LocalDateTime.now());
                        bulk.setModified_at(LocalDateTime.now());
                    } catch (JsonProcessingException e) {
                        logger.error("error releaseSctid():: While Converting jsonObject with namespace: {} to String. Exception is- {}",bulkSctRequest.getNamespace(), e);
                        throw new CisException(HttpStatus.BAD_REQUEST, "Json processing Exception: "+e.getMessage());
                    }
                    resultJob = this.bulkJobRepository.save(bulk);
                }
            }

        }
        logger.debug("BulkSctidService.releaseSctid() - Response :: {}", resultJob);
        return resultJob;
    }

    public BulkJob reserveSctids(AuthenticateResponseDto authToken, SCTIDBulkReservationRequestDto sctidBulkReservationRequestDto) throws CisException {
        logger.debug("BulkSctidService.reserveSctids() token-{} :: sctidBulkReservationRequestDto - {} ", authToken.toString(), sctidBulkReservationRequestDto);
        BulkJob output = new BulkJob();
        boolean able = isAbleUser(sctidBulkReservationRequestDto.getNamespace().toString(), authToken);
        if (able)
            output = bulkReserveSctids(sctidBulkReservationRequestDto, authToken.getName());
        else {
            logger.error("error reserveSctids():: User: {} has neither admin access nor namespace permission for namespace: {} for the selected operation.",authToken.toString(),sctidBulkReservationRequestDto.getNamespace().toString());
            throw new CisException(HttpStatus.FORBIDDEN, "No permission for the selected operation.");
        }
        logger.debug("BulkSctidService.reserveSctids() - Response :: {}", output);
        return output;
    }

    private BulkJob bulkReserveSctids(SCTIDBulkReservationRequestDto sctidBulkReservationRequestDto, String username) throws CisException {
        logger.debug("BulkSctidService.bulkReserveSctids() sctidBulkReservationRequestDto - {} :: username-{} ", sctidBulkReservationRequestDto, username);
        SctidBulkReserve sctidBulkReserve = new SctidBulkReserve();
        sctidBulkReserve.setNamespace(sctidBulkReservationRequestDto.getNamespace());
        sctidBulkReserve.setPartitionId(sctidBulkReservationRequestDto.getPartitionId());
        sctidBulkReserve.setExpirationDate(sctidBulkReservationRequestDto.getExpirationDate());
        sctidBulkReserve.setQuantity(sctidBulkReservationRequestDto.getQuantity());
        sctidBulkReserve.setSoftware(sctidBulkReservationRequestDto.getSoftware());
        sctidBulkReserve.setComment(sctidBulkReservationRequestDto.getComment());

        if (((sctidBulkReservationRequestDto.getNamespace() == 0) && (!("0".equalsIgnoreCase(sctidBulkReservationRequestDto.getPartitionId().substring(0, 1)))))
                || (sctidBulkReservationRequestDto.getNamespace() != 0 && (!("1".equalsIgnoreCase(sctidBulkReservationRequestDto.getPartitionId().substring(0, 1)))))) {
            logger.error("error bulkReserveSctids():: namespace - {} and partitionId - {} parameters are not consistent.",sctidBulkReservationRequestDto.getNamespace(),sctidBulkReservationRequestDto.getPartitionId());
            throw new CisException(HttpStatus.BAD_REQUEST, "Namespace and partitionId parameters are not consistent.");
        } else if (sctidBulkReservationRequestDto.getQuantity() == null || sctidBulkReservationRequestDto.getQuantity() < 1) {
            logger.error("error bulkReserveSctids():: input 'quantity': {} - cannot be lower to 1.",sctidBulkReservationRequestDto.getQuantity());
            throw new CisException(HttpStatus.BAD_REQUEST, "input 'quantity' cannot be lower to 1.");
        }
        {
            sctidBulkReserve.setAuthor(username);
            sctidBulkReserve.setModel(modelsConstants.SCTID);
            sctidBulkReserve.setType(JobTypeConstants.RESERVE_SCTIDS);
            BulkJob bulkJob = new BulkJob();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String regString = objectMapper.writeValueAsString(sctidBulkReserve);
                bulkJob.setName(JobTypeConstants.RESERVE_SCTIDS);
                bulkJob.setStatus("0");
                bulkJob.setRequest(regString);
                bulkJob.setCreated_at(LocalDateTime.now());
                bulkJob.setModified_at(LocalDateTime.now());
            } catch (JsonProcessingException e) {
                logger.error("error bulkReserveSctids():: Error while converting jsonObject with namespace - {} to String. Exception : {}",sctidBulkReserve.getNamespace(), e);
                throw new CisException(HttpStatus.BAD_REQUEST, "Json processing exception:" +e.getMessage());
            }
            bulkJob = bulkJobRepository.save(bulkJob);
            logger.debug("BulkSctidService.bulkReserveSctids() - Response :: {}", bulkJob);
            return bulkJob;

        }
    }
}