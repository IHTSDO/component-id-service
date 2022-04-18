package com.snomed.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snomed.api.config.CacheConfig;
import com.snomed.api.controller.SecurityController;
import com.snomed.api.controller.dto.*;
import com.snomed.api.domain.*;
import com.snomed.api.exception.APIException;
import com.snomed.api.exception.GlobalExceptionHandler;
import com.snomed.api.helper.JobTypeConstants;
import com.snomed.api.helper.ModelsConstants;
import com.snomed.api.helper.SctIdHelper;
import com.snomed.api.repository.*;
import net.sf.ehcache.CacheManager;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.sql.Time;
import java.util.*;

@Service
public class BulkSctidService {
    @Autowired
    private SctidRepository repo;

    @Autowired
    private BulkJobRepository bulkJobRepository;

    @Autowired
    private PermissionsNamespaceRepository permissionsNamespaceRepository;

    @Autowired
    private PermissionsSchemeRepository permissionsSchemeRepository;

    @Autowired
    private TestRepository testRepo;

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

    public List<Sctid> getAllSct() {
        return repo.getAllSctidsUsingQL();
    }

    public Sctid getSctById(String id) {
        return repo.getSctidsById(id);
    }

    public List<Sctid> getAllTest() {
        return repo.getAllSctidsUsingQL();
    }

    public List<Sctid> getSctByIds(String token,String ids) throws APIException {
        if (authenticateToken(token))
            return this.validScts(ids);
        else
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
    }

    public boolean authenticateToken(String userToken) throws APIException {
        /*UserDTO obj = this.securityController.authenticate();
        if (null != obj)
            return true;
        else
            return false;*/
        return this.securityController.validateUserToken(userToken);
    }

    public UserDTO getAuthenticatedUser() throws APIException {
        return this.securityController.authenticate();
    }

    public List<Sctid> getByIds(String ids) {
        return repo.getSctidsByIds(ids);
    }

    public List<Sctid> validScts(String ids) throws APIException {

        String idsWthtSpace = ids.replaceAll("\\s+", "");
        String[] sctidsArray = idsWthtSpace.split(",");
        ArrayList<String> sctidsReqArray = new ArrayList<String>(Arrays.asList(sctidsArray));
        this.validSctidCheck(sctidsReqArray);
        List<Sctid> resArr = this.getByIds(ids);
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

    public void validSctidCheck(ArrayList<String> sctidsArray) throws APIException {
        for (int i = 0; i < sctidsArray.size(); i++) {
            if (!(sctIdHelper.validSCTId(sctidsArray.get(i))))
                throw new APIException(HttpStatus.NOT_ACCEPTABLE, "Not a Valid Sctid:" + sctidsArray.get(i));
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
        Sctid sctObj = new Sctid();
        sctObj.setSctid(sctid);
        sctObj.setSequence(sequence);
        sctObj.setNamespace(namespace);
        sctObj.setPartitionId(partitionId);
        sctObj.setCheckDigit(checkDigit);
        sctObj.setSystemId(systemId);
        sctObj.setStatus(status);
        Sctid sct = repo.save(sctObj);
        //repo.insertWithQuery(sctid, sequence, namespace, partitionId, checkDigit, systemId, status);
       // Sctid sct = this.getSctById(sctid);
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


    public List<Sctid> getSctidBySystemIds(String token,String systemIdStr, Integer namespaceId) {

        String[] systemIdsArray = systemIdStr.replaceAll("\\s+", "").split(",");

        /* fetch the sctid id record with systemId and namespaceId */
        return repo.findSctidBySystemIds(Arrays.asList(systemIdsArray), namespaceId);
       // return repo.findBySystemIdAndNamespace(systemIdStr, namespaceId);

    }

    public BulkJob registerSctids(String token,RegistrationDataDTO request) throws APIException {
        if (authenticateToken(token))
            return this.registerScts(request);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public BulkJob registerScts(RegistrationDataDTO registrationData) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        BulkJob resultJob = new BulkJob();
        if (this.isAbleUser(registrationData.getNamespace().toString(), userObj)) {
            if ((registrationData.getRecords() == null) || (registrationData.getRecords().length == 0)) {

                throw new APIException(HttpStatus.ACCEPTED, "Records property cannot be empty.");
            } else {
                int namespace;
                boolean error = false;
                for (RegistrationRecordsDTO record : registrationData.getRecords()) {
                    namespace = sctIdHelper.getNamespace(record.getSctid());
                    if (namespace != registrationData.getNamespace()) {
                        error = true;
                        throw new APIException(HttpStatus.CONFLICT, "Namespaces differences between schemeid: " + record.getSctid() + " and parameter: " + registrationData.getNamespace());
                    }
                }
                if (!error) {
                    registrationData.setAuthor(userObj.getLogin());
                    registrationData.setModel(modelsConstants.SCTID);
                    registrationData.setType(jobType.REGISTER_SCTIDS);
                    RegistrationDataDTO registrationDataDTO = new RegistrationDataDTO(registrationData.getRecords(), registrationData.getNamespace(),
                            registrationData.getSoftware(), registrationData.getComment(), registrationData.getModel(), registrationData.getAuthor(), registrationData.getType());
                    BulkJob bulk = new BulkJob();
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String regString = objectMapper.writeValueAsString(registrationDataDTO);
                        bulk.setName(jobType.REGISTER_SCTIDS);
                        bulk.setStatus("0");
                        bulk.setRequest(regString);
                        bulk.setCreated_at(new Date());
                        bulk.setRequested_at(new Date());
                    } catch (JsonProcessingException e) {
                        throw new APIException(HttpStatus.BAD_REQUEST, e.getMessage());
                    }

                   resultJob = this.bulkJobRepository.save(bulk);
                    System.out.println("result:" + resultJob);
                }
            }
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
        return resultJob;
    }

    public boolean isAbleUser(String namespace, UserDTO user) {
        boolean able = false;
        List<String> admins = Arrays.asList("keerthika", "b", "c");
        for (String admin : admins) {
            if (admin.equalsIgnoreCase(user.getLogin())) {
                able = true;
            }
        }
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

    public BulkJobResponseDto generateSctids(String token,SCTIDBulkGenerationRequestDto sctidBulkGenerationRequestDto) throws APIException, JsonProcessingException, APIException {
        if (authenticateToken(token)) {
            UserDTO userObj = this.getAuthenticatedUser();
            boolean able = isAbleUser(sctidBulkGenerationRequestDto.getNamespace().toString(), userObj);
            if (!able) {
                throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }

            // if (able) {
            if (((sctidBulkGenerationRequestDto.getNamespace() == 0) && (!"0".equalsIgnoreCase(sctidBulkGenerationRequestDto.getPartitionId().substring(0, 1))))
                    || (0 != (sctidBulkGenerationRequestDto.getNamespace()) && (!"1".equalsIgnoreCase(sctidBulkGenerationRequestDto.getPartitionId().substring(0, 1))))) {
                throw new APIException(HttpStatus.BAD_REQUEST, "Namespace and partitionId parameters are not consistent.");
            }
            if ((sctidBulkGenerationRequestDto.getSystemIds().size() != 0 && (sctidBulkGenerationRequestDto.getSystemIds().size() != sctidBulkGenerationRequestDto.getQuantity()))) {
                throw new APIException(HttpStatus.BAD_REQUEST, "SystemIds quantity is not equal to quantity requirement");
            }

            sctidBulkGenerationRequestDto.setAuthor("a");
            sctidBulkGenerationRequestDto.model = modelsConstants.SCTID;

            if ((sctidBulkGenerationRequestDto.getSystemIds() != null || sctidBulkGenerationRequestDto.getSystemIds().size() == 0) &&
                    ("TRUE".equalsIgnoreCase(sctidBulkGenerationRequestDto.getGenerateLegacyIds().toUpperCase()) && ("0".equalsIgnoreCase(sctidBulkGenerationRequestDto.getPartitionId().substring(1, 1))))) {
                List<String> arrayUid = new ArrayList<>();
                for (int i = 0; i < sctidBulkGenerationRequestDto.getQuantity(); i++) {
                    arrayUid.add(SctIdHelper.guid());
                }
                sctidBulkGenerationRequestDto.setSystemIds(arrayUid);
                sctidBulkGenerationRequestDto.setAutoSysId(true);

            }
            List<String> additionalJob = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            String reqAsString = objectMapper.writeValueAsString(sctidBulkGenerationRequestDto);
            BulkJob bulkJob = new BulkJob();
            sctidBulkGenerationRequestDto.setType(jobType.GENERATE_SCTIDS);
            bulkJob.setName(jobType.GENERATE_SCTIDS);
            bulkJob.setStatus("0");
            bulkJob.setRequest(reqAsString);
            bulkJob = bulkJobRepository.save(bulkJob);

            if (sctidBulkGenerationRequestDto.getGenerateLegacyIds() != null && ("true".equalsIgnoreCase(sctidBulkGenerationRequestDto.getGenerateLegacyIds().toUpperCase()) && ("0".equalsIgnoreCase(sctidBulkGenerationRequestDto.getPartitionId().substring(1, 1))))) {
                SCTIDBulkGenerationRequestDto generationMetaData = sctidBulkGenerationRequestDto.copy();
                generationMetaData.model = modelsConstants.SCHEME_ID;

                if (isSchemeAbleUser("SNOMEDID", userObj)) {
                    if (able) {
                        generationMetaData.setScheme("SNOMEDID");
                        generationMetaData.setType(jobType.GENERATE_SCHEMEIDS);
                        BulkJob bulkJobScheme = new BulkJob();
                        bulkJobScheme.setName(jobType.GENERATE_SCHEMEIDS);
                        bulkJobScheme.setStatus("0");
                        String genAsString = objectMapper.writeValueAsString(generationMetaData);
                        bulkJobScheme.setRequest(genAsString);
                        bulkJobScheme = bulkJobRepository.save(bulkJobScheme);
                        additionalJob.add(bulkJob.toString());
                        if (isSchemeAbleUser("CTV3ID", userObj)) {
                            if (able) {
                                SCTIDBulkGenerationRequestDto generationCTV3IDMetadata = generationMetaData.copy();
                                generationCTV3IDMetadata.setScheme("CTV3ID");
                                generationCTV3IDMetadata.setType(jobType.GENERATE_SCHEMEIDS);
                                BulkJob bulkJobSchemeCTV = new BulkJob();
                                bulkJobSchemeCTV.setStatus("0");
                                String genMetaAsStr = objectMapper.writeValueAsString(generationCTV3IDMetadata);
                                bulkJobSchemeCTV.setRequest(genMetaAsStr);
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
                                SCTIDBulkGenerationRequestDto generationCTV3IDMetadata = generationMetaData.copy();
                                generationCTV3IDMetadata.setScheme("CTV3ID");
                                generationCTV3IDMetadata.setType(jobType.GENERATE_SCHEMEIDS);
                                BulkJob bulkJobCtvMetaData = new BulkJob();
                                bulkJobCtvMetaData.setName(jobType.GENERATE_SCHEMEIDS);
                                bulkJobCtvMetaData.setStatus("0");
                                String genCTVAsStr = objectMapper.writeValueAsString(generationCTV3IDMetadata);
                                bulkJobCtvMetaData.setRequest(genCTVAsStr);
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
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
        }
    }

    public boolean isSchemeAbleUser(String schemeName, UserDTO user) {
        boolean able = false;
        List<String> admins = Arrays.asList("a", "b", "c");
        for (String admin : admins) {
            if (admin.equalsIgnoreCase(user.getLogin())) {
                able = true;
                break;
            }
        }
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
    public BulkJob deprecateSctid(String token,BulkSctRequestDTO deprecateBulkSctRequestDTO) throws APIException {
        BulkJob resultJob = new BulkJob();
        if (authenticateToken(token)) {
            UserDTO userObj = this.getAuthenticatedUser();
            boolean able = isAbleUser(deprecateBulkSctRequestDTO.getNamespace().toString(), userObj);
            if (!able) {
                throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
            else
            {
                if (null==deprecateBulkSctRequestDTO.getSctids() || deprecateBulkSctRequestDTO.getSctids().length<1){

                    throw new APIException(HttpStatus.ACCEPTED,"Sctids property cannot be empty.");
                }
                else
                {
                    int namespace;
                    boolean error=false;
                    for (String sctid : deprecateBulkSctRequestDTO.getSctids())
                    {
                        namespace = sctIdHelper.getNamespace(sctid);
                        if (namespace!=deprecateBulkSctRequestDTO.getNamespace()){
                            error=true;
                            throw new APIException(HttpStatus.ACCEPTED,"Namespaces differences between sctid: " + sctid + " and parameter: " + deprecateBulkSctRequestDTO.getNamespace());
                        }
                    }
                    if(!error) {
                        deprecateBulkSctRequestDTO.setAuthor(userObj.getLogin());
                        deprecateBulkSctRequestDTO.setModel(modelsConstants.SCTID);
                        deprecateBulkSctRequestDTO.setType(jobType.DEPRECATE_SCTIDS);
                        //BulkSctRequestDTO deprecateBulkSctRequestDTO = new RegistrationDataDTO(registrationData.getRecords(), registrationData.getNamespace(),
                          //      registrationData.getSoftware(), registrationData.getComment(), registrationData.getModel(), registrationData.getAuthor(), registrationData.getType());
                        BulkJob bulk = new BulkJob();
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            String regString = objectMapper.writeValueAsString(deprecateBulkSctRequestDTO);
                            bulk.setName(jobType.REGISTER_SCTIDS);
                            bulk.setStatus("0");
                            bulk.setRequest(regString);
                            bulk.setCreated_at(new Date());
                            bulk.setRequested_at(new Date());
                        } catch (JsonProcessingException e) {
                            throw new APIException(HttpStatus.BAD_REQUEST, e.getMessage());
                        }

                        resultJob = this.bulkJobRepository.save(bulk);
                        System.out.println("result:" + resultJob);
                    }
                }

            }

        }
        else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return resultJob;
    }

    //Publish API
    public BulkJob publishSctid(String token,BulkSctRequestDTO publishBulkSctRequestDTO) throws APIException {
        BulkJob resultJob = new BulkJob();
        if (authenticateToken(token)) {
            UserDTO userObj = this.getAuthenticatedUser();
            boolean able = isAbleUser(publishBulkSctRequestDTO.getNamespace().toString(), userObj);
            if (!able) {
                throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
            else
            {
                if (null==publishBulkSctRequestDTO.getSctids() || publishBulkSctRequestDTO.getSctids().length<1){

                    throw new APIException(HttpStatus.ACCEPTED,"Sctids property cannot be empty.");
                }
                else
                {
                    int namespace;
                    boolean error=false;
                    for (String sctid : publishBulkSctRequestDTO.getSctids())
                    {
                        namespace = sctIdHelper.getNamespace(sctid);
                        if (namespace!=publishBulkSctRequestDTO.getNamespace()){
                            error=true;
                            throw new APIException(HttpStatus.ACCEPTED,"Namespaces differences between sctid: " + sctid + " and parameter: " + publishBulkSctRequestDTO.getNamespace());
                        }
                    }
                    if(!error) {
                        publishBulkSctRequestDTO.setAuthor(userObj.getLogin());
                        publishBulkSctRequestDTO.setModel(modelsConstants.SCTID);
                        publishBulkSctRequestDTO.setType(jobType.PUBLISH_SCTIDS);
                        //BulkSctRequestDTO deprecateBulkSctRequestDTO = new RegistrationDataDTO(registrationData.getRecords(), registrationData.getNamespace(),
                        //      registrationData.getSoftware(), registrationData.getComment(), registrationData.getModel(), registrationData.getAuthor(), registrationData.getType());
                        BulkJob bulk = new BulkJob();
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            String regString = objectMapper.writeValueAsString(publishBulkSctRequestDTO);
                            bulk.setName(jobType.PUBLISH_SCTIDS);
                            bulk.setStatus("0");
                            bulk.setRequest(regString);
                            bulk.setCreated_at(new Date());
                            bulk.setRequested_at(new Date());
                        } catch (JsonProcessingException e) {
                            throw new APIException(HttpStatus.BAD_REQUEST, e.getMessage());
                        }

                        resultJob = this.bulkJobRepository.save(bulk);
                        System.out.println("result:" + resultJob);
                    }
                }

            }

        }
        else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return resultJob;
    }

    //Release Sctid API
    public BulkJob releaseSctid(String token,BulkSctRequestDTO releaseBulkSctRequestDTO) throws APIException {
        BulkJob resultJob = new BulkJob();
        if (authenticateToken(token)) {
            UserDTO userObj = this.getAuthenticatedUser();
            boolean able = isAbleUser(releaseBulkSctRequestDTO.getNamespace().toString(), userObj);
            if (!able) {
                throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
            else
            {
                if (null==releaseBulkSctRequestDTO.getSctids() || releaseBulkSctRequestDTO.getSctids().length<1){

                    throw new APIException(HttpStatus.ACCEPTED,"Sctids property cannot be empty.");
                }
                else
                {
                    int namespace;
                    boolean error=false;
                    for (String sctid : releaseBulkSctRequestDTO.getSctids())
                    {
                        namespace = sctIdHelper.getNamespace(sctid);
                        if (namespace!=releaseBulkSctRequestDTO.getNamespace()){
                            error=true;
                            throw new APIException(HttpStatus.ACCEPTED,"Namespaces differences between sctid: " + sctid + " and parameter: " + releaseBulkSctRequestDTO.getNamespace());
                        }
                    }
                    if(!error) {
                        releaseBulkSctRequestDTO.setAuthor(userObj.getLogin());
                        releaseBulkSctRequestDTO.setModel(modelsConstants.SCTID);
                        releaseBulkSctRequestDTO.setType(jobType.RELEASE_SCTIDS);
                        //BulkSctRequestDTO deprecateBulkSctRequestDTO = new RegistrationDataDTO(registrationData.getRecords(), registrationData.getNamespace(),
                        //      registrationData.getSoftware(), registrationData.getComment(), registrationData.getModel(), registrationData.getAuthor(), registrationData.getType());
                        BulkJob bulk = new BulkJob();
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            String regString = objectMapper.writeValueAsString(releaseBulkSctRequestDTO);
                            bulk.setName(jobType.RELEASE_SCTIDS);
                            bulk.setStatus("0");
                            bulk.setRequest(regString);
                            bulk.setCreated_at(new Date());
                            bulk.setRequested_at(new Date());
                        } catch (JsonProcessingException e) {
                            throw new APIException(HttpStatus.BAD_REQUEST, e.getMessage());
                        }

                        resultJob = this.bulkJobRepository.save(bulk);
                        System.out.println("result:" + resultJob);
                    }
                }

            }

        }
        else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return resultJob;
    }
}