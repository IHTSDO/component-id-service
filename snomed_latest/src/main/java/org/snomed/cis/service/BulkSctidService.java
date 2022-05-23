package org.snomed.cis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.snomed.cis.config.CacheConfig;
import org.snomed.cis.controller.SecurityController;
import org.snomed.cis.controller.dto.*;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.util.JobTypeConstants;
import org.snomed.cis.util.ModelsConstants;
import org.snomed.cis.util.SctIdHelper;
import org.snomed.cis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BulkSctidService {
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
    private CacheConfig cache;

    @Autowired
    JobTypeConstants jobType;

    @Autowired
    ModelsConstants modelsConstants;

    @Autowired
    private SecurityController securityController;

    public List<Sctid> postSctByIds(String token, SctIdRequest ids) throws CisException {
        if (authenticateToken(token))
            return this.postValidScts(ids);
        else
            throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
    }

    public List<Sctid> getSctByIds(String token, String ids) throws CisException {
        if (authenticateToken(token))
            return this.validScts(ids);
        else
            throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
    }

    public boolean authenticateToken(String userToken) throws CisException {
        /*UserDTO obj = this.securityController.authenticate();
        if (null != obj)
            return true;
        else
            return false;*/
        return this.securityController.validateUserToken(userToken);
    }

    public UserDTO getAuthenticatedUser() throws CisException {
        return this.securityController.authenticate();
    }

    public List<Sctid> getByIds(List<String> ids) {
        return repo.findBySctidIn(ids);
    }

    public List<Sctid> validScts(String ids) throws CisException {

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
        Set<String> rqSet = new HashSet<>(respSctArr);
        Set<String> respSet = new HashSet<>(sctidsReqArray);
        Set<String> resultDiff = new HashSet<>(rqSet);
        resultDiff.removeAll(respSet);
        if (resultDiff.size() > 0) {
            for (String diffSctid :
                    resultDiff) {
                Sctid sctidObj = this.getFreeRecord(diffSctid, null);
                resArr.add(sctidObj);
            }
        }
        return resArr;
    }

    public List<Sctid> postValidScts(SctIdRequest ids) throws CisException {

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
        Set<String> rqSet = new HashSet<>(respSctArr);
        Set<String> respSet = new HashSet<>(sctidsReqArray);
        Set<String> resultDiff = new HashSet<>(rqSet);
        resultDiff.removeAll(respSet);
        if (resultDiff.size() > 0) {
            for (String diffSctid :
                    resultDiff) {
                Sctid sctidObj = this.getFreeRecord(diffSctid, null);
                resArr.add(sctidObj);
            }
        }
        return resArr;
    }

    public void validSctidCheck(ArrayList<String> sctidsArray) throws CisException {
        for (int i = 0; i < sctidsArray.size(); i++) {
            if (!(sctIdHelper.validSCTId(sctidsArray.get(i))))
                throw new CisException(HttpStatus.NOT_ACCEPTABLE, "Not a Valid Sctid:" + sctidsArray.get(i));
        }
    }

    public Sctid getFreeRecord(String sctId, String systemId) {
        Map<String, Object> sctIdRecord = getNewRecord(sctId, systemId);
        sctIdRecord.put("status", "available");
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
        //refactor changes
        /*Sctid sctObj = new Sctid();
        sctObj.setSctid(sctid);
        sctObj.setSequence(sequence);
        sctObj.setNamespace(namespace);
        sctObj.setPartitionId(partitionId);
        sctObj.setCheckDigit(checkDigit);
        sctObj.setSystemId(systemId);
        sctObj.setStatus(status);*/
        Sctid sctObj = Sctid.builder().sctid(sctid).sequence(sequence).namespace(namespace)
                .partitionId(partitionId).checkDigit(checkDigit).systemId(systemId).status(status)
                .build();
        //refactor changes
        Sctid sct = repo.save(sctObj);
        return sct;
    }

    public Map<String, Object> getNewRecord(String sctIdInput, String inpSystemId) {
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
        return sctIdRecord;
    }


    public List<Sctid> getSctidBySystemIds(String token, String systemIdStr, Integer namespaceId) {

        String[] systemIdsArray = systemIdStr.replaceAll("\\s+", "").split(",");

        /* fetch the sctid id record with systemId and namespaceId */

       // return repo.findSctidBySystemIds(Arrays.asList(systemIdsArray), namespaceId);
         return repo.findBySystemIdInAndNamespace(Arrays.asList(systemIdsArray), namespaceId);

    }

    public BulkJob registerSctids(String token, RegistrationDataDTO request) throws CisException {
        if (authenticateToken(token))
            return this.registerScts(request);
        else
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public BulkJob registerScts(RegistrationDataDTO registrationData) throws CisException {
         /*
    * {
  "records": [
    {
      "sctid": "string",
      "systemId": "string"
    }
  ],
  "namespace": 0,
  "software": "string",
  "comment": "string"
}
* */
        //requestbody change
        UserDTO userObj = this.getAuthenticatedUser();
        BulkJob resultJob = new BulkJob();
        if (this.isAbleUser(registrationData.getNamespace().toString(), userObj)) {
            SctidBulkRegister sctidBulkRegister = new SctidBulkRegister();
            sctidBulkRegister.setRecords(registrationData.getRecords());
            sctidBulkRegister.setNamespace(registrationData.getNamespace());
            sctidBulkRegister.setSoftware(registrationData.getSoftware());
            sctidBulkRegister.setComment(registrationData.getComment());

            if ((registrationData.getRecords() == null) || (registrationData.getRecords().length == 0)) {

                throw new CisException(HttpStatus.ACCEPTED, "Records property cannot be empty.");
            } else {
                int namespace;
                boolean error = false;
                for (RegistrationRecordsDTO record : registrationData.getRecords()) {
                    namespace = sctIdHelper.getNamespace(record.getSctid());
                    if (namespace != registrationData.getNamespace()) {
                        error = true;
                        throw new CisException(HttpStatus.CONFLICT, "Namespaces differences between schemeid: " + record.getSctid() + " and parameter: " + registrationData.getNamespace());
                    }
                }
                if (!error) {
                    sctidBulkRegister.setAuthor(userObj.getLogin());
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
                        throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
                    }

                    resultJob = this.bulkJobRepository.save(bulk);
                    System.out.println("result:" + resultJob);
                }
            }
        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
        return resultJob;
    }

    public boolean isAbleUser(String namespace, UserDTO user) throws CisException {
        List<String> groups = authenticateToken.getGroupsList();
        boolean able = false;
        for (String group : groups) {
            if (group.equalsIgnoreCase("component-identifier-service-admin")) {
                able = true;
            }
        }
        /*List<String> admins = Arrays.asList("keerthika", "b", "c");
        for (String admin : admins) {
            if (admin.equalsIgnoreCase(user.getLogin())) {
                able = true;
            }
        }*/
        if (!able) {
            if (!"false".equalsIgnoreCase(namespace)) {
                List<PermissionsNamespace> permissionsNamespaceList = permissionsNamespaceRepository.findByNamespace(Integer.valueOf(namespace));
                List<String> possibleGroups = new ArrayList<>();
                for (PermissionsNamespace perm : permissionsNamespaceList) {
                    if (("group").equalsIgnoreCase(perm.getRole())) {
                        possibleGroups.add(perm.getUsername());
                    } else if ((user.getLogin()).equalsIgnoreCase(perm.getUsername())) {
                        able = true;
                    }
                }
                if (!able) {
                    List<String> roleAsGroups = user.getRoles();
                    for (String group : roleAsGroups) {
                        if (group == "namespace-" + namespace)
                            able = true;
                        else if (possibleGroups.contains(group))
                            able = true;
                    }
                } else {
                    return able;
                }
            }
            return able;
        }// if(!able)
        else
            return able;
    }

    public BulkJobResponseDto generateSctids(String token, SCTIDBulkGenerationRequestDto sctidBulkGenerationRequestDto) throws CisException, JsonProcessingException, CisException {
/*
* @NotNull
    private Integer namespace;
    @NotNull
    private String partitionId;
    @NotNull
    private Integer quantity;
    private List<String> systemIds;
    private String software;
    private String comment;
    private boolean generateLegacyIds;
* */
        //requestbody change
        SctidBulkGenerate bulkGenerate = new SctidBulkGenerate();
        bulkGenerate.setNamespace(sctidBulkGenerationRequestDto.getNamespace());
        bulkGenerate.setPartitionId(sctidBulkGenerationRequestDto.getPartitionId());
        bulkGenerate.setQuantity(sctidBulkGenerationRequestDto.getQuantity());
        bulkGenerate.setSystemIds(sctidBulkGenerationRequestDto.getSystemIds());
        bulkGenerate.setSoftware(sctidBulkGenerationRequestDto.getSoftware());
        bulkGenerate.setComment(sctidBulkGenerationRequestDto.getComment());
        bulkGenerate.setGenerateLegacyIds(sctidBulkGenerationRequestDto.getGenerateLegacyIds());


        if (authenticateToken(token)) {
            UserDTO userObj = this.getAuthenticatedUser();
            boolean able = isAbleUser(sctidBulkGenerationRequestDto.getNamespace().toString(), userObj);
            if (!able) {
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }


            // if (able) {
            if (((sctidBulkGenerationRequestDto.getNamespace() == 0) && (!"0".equalsIgnoreCase(sctidBulkGenerationRequestDto.getPartitionId().substring(0, 1))))
                    || (0 != (sctidBulkGenerationRequestDto.getNamespace()) && (!"1".equalsIgnoreCase(sctidBulkGenerationRequestDto.getPartitionId().substring(0, 1))))) {
                throw new CisException(HttpStatus.BAD_REQUEST, "Namespace and partitionId parameters are not consistent.");
            }
            if ((sctidBulkGenerationRequestDto.getSystemIds().size() != 0 && (sctidBulkGenerationRequestDto.getSystemIds().size() != sctidBulkGenerationRequestDto.getQuantity()))) {
                throw new CisException(HttpStatus.BAD_REQUEST, "SystemIds quantity is not equal to quantity requirement");
            }

            bulkGenerate.setAuthor(userObj.getLogin());
            bulkGenerate.model = modelsConstants.SCTID;

            if ((sctidBulkGenerationRequestDto.getSystemIds() != null || sctidBulkGenerationRequestDto.getSystemIds().size() == 0) &&
                    ("TRUE".equalsIgnoreCase((sctidBulkGenerationRequestDto.getGenerateLegacyIds())) && ("0".equalsIgnoreCase(sctidBulkGenerationRequestDto.getPartitionId().substring(1, 1))))) {
                List<String> arrayUid = new ArrayList<>();
                for (int i = 0; i < sctidBulkGenerationRequestDto.getQuantity(); i++) {
                    arrayUid.add(SctIdHelper.guid());
                }
                bulkGenerate.setSystemIds(arrayUid);
                bulkGenerate.setAutoSysId(true);

            }
            List<String> additionalJob = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            String reqAsString = objectMapper.writeValueAsString(bulkGenerate);
            String jsonFormattedString = reqAsString.replaceAll("\\\\", "");

            BulkJob bulkJob = new BulkJob();

            bulkGenerate.setType(jobType.GENERATE_SCTIDS);
            bulkJob.setName(jobType.GENERATE_SCTIDS);
            bulkJob.setStatus("0");
            bulkJob.setRequest(jsonFormattedString);
            bulkJob = bulkJobRepository.save(bulkJob);

            if (/*bulkGenerate.isAutoSysId() != null*/  ("true".equalsIgnoreCase(bulkGenerate.getGenerateLegacyIds()) && ("0".equalsIgnoreCase(bulkGenerate.getPartitionId().substring(1, 1))))) {
                // SCTIDBulkGenerationRequestDto generationMetaData = sctidBulkGenerationRequestDto.copy();
                SctidBulkGenerate bulkGenerate1 = bulkGenerate.copy();
                bulkGenerate.model = modelsConstants.SCHEME_ID;

                if (isSchemeAbleUser("SNOMEDID", userObj)) {
                    if (able) {
                        bulkGenerate1.setScheme("SNOMEDID");
                        bulkGenerate1.setType(jobType.GENERATE_SCHEMEIDS);
                        BulkJob bulkJobScheme = new BulkJob();
                        bulkJobScheme.setName(jobType.GENERATE_SCHEMEIDS);
                        bulkJobScheme.setStatus("0");
                        String genAsString = objectMapper.writeValueAsString(bulkGenerate1);
                        String genAsStringFormat = reqAsString.replaceAll("\\\\", "");
                        bulkJobScheme.setRequest(genAsStringFormat);
                        bulkJobScheme = bulkJobRepository.save(bulkJobScheme);
                        additionalJob.add(bulkJob.toString());
                        if (isSchemeAbleUser("CTV3ID", userObj)) {
                            if (able) {
                                //   SCTIDBulkGenerationRequestDto generationCTV3IDMetadata = generationMetaData.copy();
                                SctidBulkGenerate generationCTV3IDMetadata = bulkGenerate1.copy();
                                generationCTV3IDMetadata.setScheme("CTV3ID");
                                generationCTV3IDMetadata.setType(jobType.GENERATE_SCHEMEIDS);
                                BulkJob bulkJobSchemeCTV = new BulkJob();
                                bulkJobSchemeCTV.setStatus("0");
                                String genMetaAsStr = objectMapper.writeValueAsString(generationCTV3IDMetadata);
                                String genMetaAsStrFormat = reqAsString.replaceAll("\\\\", "");
                                bulkJobSchemeCTV.setRequest(genMetaAsStrFormat);
                                bulkJobSchemeCTV = bulkJobRepository.save(bulkJobSchemeCTV);
                                additionalJob.add(bulkJobSchemeCTV.toString());
                                BulkJobResponseDto bulkJobResponseDto = new BulkJobResponseDto(bulkJob);
                                bulkJobResponseDto.setAdditionalJobs(additionalJob);
                                return bulkJobResponseDto;

                            }//if(able)
                            else {
                                BulkJobResponseDto bulkJobResponseDto = new BulkJobResponseDto(bulkJob);
                                bulkJobResponseDto.setAdditionalJobs(additionalJob);
                                return bulkJobResponseDto;
                            }

                        }// isSchemableUser
                    } else {
                        if (isSchemeAbleUser("CTV3ID", userObj)) {
                            if (able) {
                                SctidBulkGenerate generationCTV3IDMetadata = bulkGenerate1.copy();
                                generationCTV3IDMetadata.setScheme("CTV3ID");
                                generationCTV3IDMetadata.setType(jobType.GENERATE_SCHEMEIDS);
                                BulkJob bulkJobCtvMetaData = new BulkJob();
                                bulkJobCtvMetaData.setName(jobType.GENERATE_SCHEMEIDS);
                                bulkJobCtvMetaData.setStatus("0");
                                String genCTVAsStr = objectMapper.writeValueAsString(generationCTV3IDMetadata);
                                String genCTVAsStrFormat = reqAsString.replaceAll("\\\\", "");
                                bulkJobCtvMetaData.setRequest(genCTVAsStrFormat);
                                bulkJobCtvMetaData = bulkJobRepository.save(bulkJobCtvMetaData);
                                additionalJob.add(bulkJobCtvMetaData.toString());
                                BulkJobResponseDto bulkJobResponseDto = new BulkJobResponseDto(bulkJob);
                                bulkJobResponseDto.setAdditionalJobs(additionalJob);
                                return bulkJobResponseDto;

                            } else {
                                BulkJobResponseDto bulkJobResponseDto = new BulkJobResponseDto(bulkJob);
                                return bulkJobResponseDto;
                            }
                        }

                    }
                }
            } else {
                BulkJobResponseDto bulkJobResponseDto = new BulkJobResponseDto(bulkJob);
                return bulkJobResponseDto;
            }

            // }// if(able)
            return null;
        } else {
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
        }
    }

    public boolean isSchemeAbleUser(String schemeName, UserDTO user) throws CisException {
        List<String> groups = authenticateToken.getGroupsList();
        boolean able = false;
        for (String group : groups) {
            if (group.equalsIgnoreCase("component-identifier-service-admin")) {
                able = true;
            }
        }
       /* List<String> admins = Arrays.asList("a", "b", "c");
        for (String admin : admins) {
            if (admin.equalsIgnoreCase(user.getLogin())) {
                able = true;
                break;
            }
        }*/
        if (!able) {
            if (!"false".equalsIgnoreCase(schemeName)) {
                List<PermissionsScheme> permissionsSchemesList = permissionsSchemeRepository.findByScheme(schemeName);
                List<String> possibleGroups = new ArrayList<>();
                for (PermissionsScheme perm : permissionsSchemesList) {
                    if (("group").equalsIgnoreCase(perm.getRole())) {
                        possibleGroups.add(perm.getUsername());
                    } else if ((user.getLogin()).equalsIgnoreCase(perm.getUsername())) {
                        able = true;
                    }
                }// possible groups
                if (!able) {
                    List<String> roleAsGroups = user.getRoles();
                    for (String group : roleAsGroups) {
                        if (possibleGroups.contains(group))
                            able = true;
                    }
                } else {
                    return able;
                }
            }
        }//if(!able)

        return able;
    }

    //Deprecate API
    public BulkJob deprecateSctid(String token, BulkSctRequestDTO deprecateBulkSctRequestDTO) throws CisException {
        BulkJob resultJob = new BulkJob();

    /*
    * {
  "sctids": [
    "string"
  ],
  "namespace": 0,
  "software": "string",
  "comment": "string"
  *
  * //test data
  * {
  "sctids": [
    "123005000"
  ],
  "namespace": 0,
  "software": "string",
  "comment": "string"
}
}*/
        //requestbody change

        if (authenticateToken(token)) {
            UserDTO userObj = this.getAuthenticatedUser();
            BulkSctRequest bulkSctRequest = new BulkSctRequest();
            bulkSctRequest.setSctids(deprecateBulkSctRequestDTO.getSctids());
            bulkSctRequest.setNamespace(deprecateBulkSctRequestDTO.getNamespace());
            bulkSctRequest.setSoftware(deprecateBulkSctRequestDTO.getSoftware());
            bulkSctRequest.setComment(deprecateBulkSctRequestDTO.getComment());

            boolean able = isAbleUser(deprecateBulkSctRequestDTO.getNamespace().toString(), userObj);
            if (!able) {
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            } else {
                if (null == deprecateBulkSctRequestDTO.getSctids() || deprecateBulkSctRequestDTO.getSctids().length < 1) {

                    throw new CisException(HttpStatus.ACCEPTED, "Sctids property cannot be empty.");
                } else {
                    int namespace;
                    boolean error = false;
                    for (String sctid : deprecateBulkSctRequestDTO.getSctids()) {
                        namespace = sctIdHelper.getNamespace(sctid);
                        if (namespace != deprecateBulkSctRequestDTO.getNamespace()) {
                            error = true;
                            throw new CisException(HttpStatus.ACCEPTED, "Namespaces differences between sctid: " + sctid + " and parameter: " + deprecateBulkSctRequestDTO.getNamespace());
                        }
                    }
                    if (!error) {
                        bulkSctRequest.setAuthor(userObj.getLogin());
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
                            throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
                        }

                        resultJob = this.bulkJobRepository.save(bulk);
                        System.out.println("result:" + resultJob);
                    }
                }

            }

        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return resultJob;
    }

    //Publish API
    public BulkJob publishSctid(String token, BulkSctRequestDTO publishBulkSctRequestDTO) throws CisException {
        BulkJob resultJob = new BulkJob();
        /*
    * {
  "sctids": [
    "string"
  ],
  "namespace": 0,
  "software": "string",
  "comment": "string"
}*/
        //requestbody change
        if (authenticateToken(token)) {
            UserDTO userObj = this.getAuthenticatedUser();
            BulkSctRequest bulkSctRequest = new BulkSctRequest();
            bulkSctRequest.setSctids(publishBulkSctRequestDTO.getSctids());
            bulkSctRequest.setNamespace(publishBulkSctRequestDTO.getNamespace());
            bulkSctRequest.setSoftware(publishBulkSctRequestDTO.getSoftware());
            bulkSctRequest.setComment(publishBulkSctRequestDTO.getComment());
            boolean able = isAbleUser(publishBulkSctRequestDTO.getNamespace().toString(), userObj);
            if (!able) {
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            } else {
                if (null == publishBulkSctRequestDTO.getSctids() || publishBulkSctRequestDTO.getSctids().length < 1) {

                    throw new CisException(HttpStatus.ACCEPTED, "Sctids property cannot be empty.");
                } else {
                    int namespace;
                    boolean error = false;
                    for (String sctid : publishBulkSctRequestDTO.getSctids()) {
                        namespace = sctIdHelper.getNamespace(sctid);
                        if (namespace != publishBulkSctRequestDTO.getNamespace()) {
                            error = true;
                            throw new CisException(HttpStatus.ACCEPTED, "Namespaces differences between sctid: " + sctid + " and parameter: " + publishBulkSctRequestDTO.getNamespace());
                        }
                    }
                    if (!error) {
                        bulkSctRequest.setAuthor(userObj.getLogin());
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
                            throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
                        }

                        resultJob = this.bulkJobRepository.save(bulk);
                        System.out.println("result:" + resultJob);
                    }
                }

            }

        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return resultJob;
    }

    //Release Sctid API
    public BulkJob releaseSctid(String token, BulkSctRequestDTO releaseBulkSctRequestDTO) throws CisException {
        BulkJob resultJob = new BulkJob();
        if (authenticateToken(token)) {

                    /*
    * {
  "sctids": [
    "string"
  ],
  "namespace": 0,
  "software": "string",
  "comment": "string"
}*/
            //requestbody change
            BulkSctRequest bulkSctRequest = new BulkSctRequest();
            bulkSctRequest.setSctids(releaseBulkSctRequestDTO.getSctids());
            bulkSctRequest.setNamespace(releaseBulkSctRequestDTO.getNamespace());
            bulkSctRequest.setSoftware(releaseBulkSctRequestDTO.getSoftware());
            bulkSctRequest.setComment(releaseBulkSctRequestDTO.getComment());

            UserDTO userObj = this.getAuthenticatedUser();
            boolean able = isAbleUser(releaseBulkSctRequestDTO.getNamespace().toString(), userObj);
            if (!able) {
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            } else {
                if (null == releaseBulkSctRequestDTO.getSctids() || releaseBulkSctRequestDTO.getSctids().length < 1) {

                    throw new CisException(HttpStatus.ACCEPTED, "Sctids property cannot be empty.");
                } else {
                    int namespace;
                    boolean error = false;
                    for (String sctid : releaseBulkSctRequestDTO.getSctids()) {
                        namespace = sctIdHelper.getNamespace(sctid);
                        if (namespace != releaseBulkSctRequestDTO.getNamespace()) {
                            error = true;
                            throw new CisException(HttpStatus.ACCEPTED, "Namespaces differences between sctid: " + sctid + " and parameter: " + releaseBulkSctRequestDTO.getNamespace());
                        }
                    }
                    if (!error) {
                        bulkSctRequest.setAuthor(userObj.getLogin());
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
                            throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
                        }

                        resultJob = this.bulkJobRepository.save(bulk);
                        System.out.println("result:" + resultJob);
                    }
                }

            }

        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return resultJob;
    }

    public BulkJob reserveSctids(String token, SCTIDBulkReservationRequestDto sctidBulkReservationRequestDto) throws CisException {
        BulkJob output = new BulkJob();
        if (authenticateToken(token)) {
            UserDTO userObj = this.getAuthenticatedUser();
            boolean able = isAbleUser(sctidBulkReservationRequestDto.getNamespace().toString(), userObj);
            if (able)
                output = bulkReserveSctids(sctidBulkReservationRequestDto);
            else {
                throw new CisException(HttpStatus.FORBIDDEN, "No permission for the selected operation");
            }

        } else
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
        return output;
    }

    private BulkJob bulkReserveSctids(SCTIDBulkReservationRequestDto sctidBulkReservationRequestDto) throws CisException {
        UserDTO userObj = this.getAuthenticatedUser();

        /*
        * {
  "namespace": 0,
  "partitionId": "string",
  "expirationDate": "string",
  "quantity": 0,
  "software": "string",
  "comment": "string"
}
        * */
        //requestbody chNGE
        //if(isAbleUser(sctidBulkReservationRequestDto.getNamespace().toString(),userObj))
        //{
        SctidBulkReserve sctidBulkReserve = new SctidBulkReserve();
        sctidBulkReserve.setNamespace(sctidBulkReservationRequestDto.getNamespace());
        sctidBulkReserve.setPartitionId(sctidBulkReservationRequestDto.getPartitionId());
        sctidBulkReserve.setExpirationDate(sctidBulkReservationRequestDto.getExpirationDate());
        sctidBulkReserve.setQuantity(sctidBulkReservationRequestDto.getQuantity());
        sctidBulkReserve.setSoftware(sctidBulkReservationRequestDto.getSoftware());
        sctidBulkReserve.setComment(sctidBulkReservationRequestDto.getComment());

        if (((sctidBulkReservationRequestDto.getNamespace() == 0) && (!("0".equalsIgnoreCase(sctidBulkReservationRequestDto.getPartitionId().substring(0, 1)))))
                || (sctidBulkReservationRequestDto.getNamespace() != 0 && (!("1".equalsIgnoreCase(sctidBulkReservationRequestDto.getPartitionId().substring(0, 1)))))) {
            throw new CisException(HttpStatus.UNAUTHORIZED, "Namespace and partitionId parameters are not consistent.");
        } else if (sctidBulkReservationRequestDto.getQuantity() == null || sctidBulkReservationRequestDto.getQuantity() < 1) {

            throw new CisException(HttpStatus.UNAUTHORIZED, "Quantity property cannot be lower to 1.");
        }
        {
            sctidBulkReserve.setAuthor(userObj.getLogin());
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
                throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
            bulkJob = bulkJobRepository.save(bulkJob);
            return bulkJob;

        }
        //  }
        /*else{
            throw new APIException(HttpStatus.FORBIDDEN, "No permission for the selected operation");
        }*/

    }


}