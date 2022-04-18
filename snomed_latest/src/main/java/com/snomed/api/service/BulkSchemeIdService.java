package com.snomed.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snomed.api.controller.SecurityController;
import com.snomed.api.controller.dto.*;
import com.snomed.api.domain.*;
import com.snomed.api.exception.APIException;
import com.snomed.api.helper.*;
import com.snomed.api.repository.BulkJobRepository;
import com.snomed.api.repository.BulkSchemeIdRepository;
import com.snomed.api.repository.PermissionsSchemeRepository;
import com.snomed.api.repository.SchemeIdBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BulkSchemeIdService {
    @Autowired
    private BulkSchemeIdRepository bulkSchemeIdRepository;
    @Autowired
    private SchemeIdBaseRepository schemeIdBaseRepository;
    @Autowired
    private SecurityController securityController;
    @Autowired
    private BulkJobRepository bulkJobRepository;

    @Autowired
    private PermissionsSchemeRepository permissionsSchemeRepository;
    private SchemeIdHelper schemeIdHelper;
    private SNOMEDID snomedid;

    public boolean isAbleUser(SchemeName schemeName, UserDTO user) throws APIException {
        boolean able = false;
        List<String> admins = Arrays.asList("a", "b", "c");
        for (String admin : admins) {
            if (admin.equalsIgnoreCase(user.getLogin())) {
                able = true;
            }
        }
        if (!able) {
            if (!"false".equalsIgnoreCase(schemeName.toString())) {
                List<PermissionsScheme> permissionsSchemeList = permissionsSchemeRepository.findByScheme(schemeName.toString());
                List<String> possibleGroups = new ArrayList<>();
                for (PermissionsScheme perm : permissionsSchemeList) {
                    if (("group").equalsIgnoreCase(perm.getRole())) {
                        possibleGroups.add(perm.getUsername());
                    } else if ((user.getLogin()).equalsIgnoreCase(perm.getUsername())) {
                        able = true;
                    }
                }//for
                if (!able && possibleGroups.size() > 0) {
                    List<String> roleAsGroups;
                    try {
                        roleAsGroups = user.getRoles();
                    } catch (Exception e) {
                        throw new APIException(HttpStatus.BAD_REQUEST, "Error accessing groups");
                    }
                    for (String group : roleAsGroups) {
                        if (possibleGroups.contains(group))
                            able = true;
                    }
                } else {
                    return able;
                }
            }
        }
        return false;
    }

    public boolean authenticateToken() throws APIException {
        UserDTO obj = this.securityController.authenticate();
        if (null != obj)
            return true;
        else
            return false;
    }

    public List<SchemeId> getSchemeIds(SchemeName schemeName, String schemeIds) throws APIException {
        String[] schemedIdArray = schemeIds.replaceAll("\\s+", "").split(",");
        //authenticate()
        //{
        //boolean isAble = isAbleUser();
        boolean able = true;
        if (able) {
            for (String schemeId : schemedIdArray) {
                if (schemeId == null || schemeId.isEmpty()) {
                    throw new APIException(HttpStatus.BAD_REQUEST, "SchemeId is null.");
                } else {
                    boolean isValidScheme = false;
                    if ("SNOMEDID".equalsIgnoreCase(schemeName.toString().toUpperCase())) {
                        isValidScheme = SNOMEDID.validSchemeId(schemeId);
                    } else if ("CTV3ID".equalsIgnoreCase(schemeName.toString().toUpperCase())) {
                        isValidScheme = CTV3ID.validSchemeId(schemeId);
                    }
                    if (!isValidScheme) {
                        throw new APIException(HttpStatus.BAD_REQUEST, "Not a valid schemeId");
                    }

                    ArrayList<String> schemeIdsArrayList = new ArrayList<String>(Arrays.asList(schemedIdArray));
                    List<SchemeId> resSchemeArrayList = bulkSchemeIdRepository.findBySchemeAndSchemeIdIn(schemeName.toString(), schemedIdArray);
                    // resSchemeArrayList push
                    List<String> respSchemeIdArray = new ArrayList<>();
                    for (int i = 0; i < resSchemeArrayList.size(); i++) {
                        SchemeId schemeIdBulkObj = resSchemeArrayList.get(i);
                        respSchemeIdArray.add(schemeIdBulkObj.getSchemeId());
                    }
                    Set<String> rqSet = new HashSet<>(respSchemeIdArray);
                    Set<String> respSet = new HashSet<>(schemeIdsArrayList);
                    Set<String> resultDiff = new HashSet<>(rqSet);
                    resultDiff.removeAll(respSet);
                    if (resultDiff.size() > 0) {
                        for (String diffSchemeId :
                                resultDiff) {
                            SchemeId schemeIdBulkObj = getFreeRecord(schemeName.toString(), diffSchemeId, null, "true");
                            resSchemeArrayList.add(schemeIdBulkObj);
                        }
                    }
                    return resSchemeArrayList;

                }
            }//validate schemeId
        } else {
            throw new APIException(HttpStatus.BAD_REQUEST, "No permission for the selected operation");
        }
        //}
        return null;
    }


    public SchemeId getFreeRecord(String schemeName, String diffSchemeId, String systemId, String autoSysId) throws APIException {
        Map<String, Object> schemeIdRecord = getNewRecord(schemeName, diffSchemeId, systemId);
        schemeIdRecord.put("status", "avilable");
        return insertSchemeIdRecord(schemeIdRecord);
    }

    private SchemeId insertSchemeIdRecord(Map<String, Object> schemeIdRecord) throws APIException {
        String error;
        SchemeId schemeIdBulk = null;
        String scheme = null;
        String[] schemeId = null;
        Integer sequence = 0;
        Integer checkDigit = 0;
        String systemId = null;
        String status = null;
        String author = null;
        String software = null;
        Date expirationDate = null;
        Integer jobId = 0;
        Date created_at = null;
        Date modified_at = null;

        Set<Map.Entry<String, Object>> s = schemeIdRecord.entrySet();
        try {
            for (Map.Entry<String, Object> mapObj : s) {
                if (mapObj.getKey() == "scheme") {
                    scheme = (String) mapObj.getValue();
                } else if (mapObj.getKey() == "schemeId") {
                    schemeId = new String[]{(String) mapObj.getValue()};
                } else if (mapObj.getKey() == "sequence") {
                    sequence = (Integer) mapObj.getValue();
                } else if (mapObj.getKey() == "checkDigit") {
                    checkDigit = (Integer) mapObj.getValue();
                } else if (mapObj.getKey() == "systemId") {
                    systemId = (String) mapObj.getValue();
                } else if (mapObj.getKey() == "status") {
                    status = (String) mapObj.getValue();
                } else if (mapObj.getKey() == "author") {
                    author = (String) mapObj.getValue();
                } else if (mapObj.getKey() == "software") {
                    software = (String) mapObj.getValue();
                } else if (mapObj.getKey() == "expirationDate") {
                    expirationDate = (Date) mapObj.getValue();
                } else if (mapObj.getKey() == "jobId") {
                    jobId = (Integer) mapObj.getValue();
                } else if (mapObj.getKey() == "created_at") {
                    created_at = (Date) mapObj.getValue();
                } else if (mapObj.getKey() == "modified_at") {
                    modified_at = (Date) mapObj.getValue();
                }
            }
            bulkSchemeIdRepository.insertWithQuery(scheme, schemeId, sequence, checkDigit, systemId, status, author, software, expirationDate, jobId, created_at, modified_at);
            schemeIdBulk = (SchemeId) bulkSchemeIdRepository.findBySchemeAndSchemeIdIn(scheme, schemeId);
            return schemeIdBulk;
        } catch (Exception e) {
            error = e.toString();
        }
        if (error != null) {
            List<SchemeIdBase> schemeIdBaseList = schemeIdBaseRepository.findByScheme(schemeIdBulk.getScheme().toString());
            if (error.indexOf("ER_DUP_ENTRY") > -1) {
                if (error.indexOf("'PRIMARY'") > -1 /*&& ()*/) {
                    System.out.println("Trying to solve the primary key error during scheme id insert.");
                    //    schemeIdRecord.getScheme();
                }
                /*else if(schemeIdRecord.get())
                {
                    schemeIdBaseList.ge
                }*/
                else {
                    throw new APIException(HttpStatus.BAD_REQUEST, "Unable to attempt to solve error");
                }

            } else {
                return schemeIdBulk;
            }
        }

        return null;
    }


    public Map<String, Object> getNewRecord(String schemeName, String diffSchemeId, String systemId) {
        Map<String, Object> schemeIdRecord = new LinkedHashMap<>();
        schemeIdRecord.put("scheme", schemeName.toUpperCase());
        schemeIdRecord.put("schemeId", diffSchemeId);
        schemeIdRecord.put("sequence", schemeIdHelper.getSequence(diffSchemeId));
        schemeIdRecord.put("checkDigit", schemeIdHelper.getCheckDigit(diffSchemeId));

        if (systemId != "null") {
            schemeIdRecord.put("systemId", systemId);
        } else {
            schemeIdRecord.put("systemId", schemeIdHelper.guid());
        }
        return schemeIdRecord;
    }


    public UserDTO getAuthenticatedUser() throws APIException {
        return this.securityController.authenticate();
    }

    public BulkJob generateSchemeIds(SchemeName schemeName, SchemeIdBulkGenerationRequestDto schemeIdBulkDto) throws APIException {
        if (authenticateToken()) {
            UserDTO userObj = this.getAuthenticatedUser();

            boolean able = isAbleUser(schemeName, userObj);
            if (!able) {
                throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
            if ((schemeIdBulkDto.getSystemIds().length != 0 && (schemeIdBulkDto.getSystemIds().length != schemeIdBulkDto.getQuantity()))) {
                throw new APIException(HttpStatus.BAD_REQUEST, "SystemIds quantity is not equal to quantity requirement");
            }
            if (schemeIdBulkDto.getSystemIds() != null || schemeIdBulkDto.getSystemIds().length == 0) {
                schemeIdBulkDto.setAutoSysId(true);
            }
            schemeIdBulkDto.setType(JobTypeConstants.GENERATE_SCHEMEIDS);
            schemeIdBulkDto.setAuthor(userObj.getLogin());
            schemeIdBulkDto.setModel(ModelsConstants.SCHEME_ID);
            schemeIdBulkDto.setScheme(schemeName);
            SchemeIdBulkGenerationRequestDto requestDto = new SchemeIdBulkGenerationRequestDto(schemeIdBulkDto.getQuantity(), schemeIdBulkDto.getSystemIds(),
                    schemeIdBulkDto.getSoftware(), schemeIdBulkDto.getComment(), true, schemeIdBulkDto.getAuthor(),schemeIdBulkDto.getModel(),schemeIdBulkDto.getScheme(),schemeIdBulkDto.getType());
// Type is set here not as an attribute
            BulkJob bulk = new BulkJob();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String regString = objectMapper.writeValueAsString(requestDto);
                bulk.setName(JobTypeConstants.GENERATE_SCHEMEIDS);
                bulk.setStatus("0");
                bulk.setRequest(regString);

            } catch (JsonProcessingException e) {
                throw new APIException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            BulkJob resultJob = this.bulkJobRepository.save(bulk);
            System.out.println("result:" + resultJob);
            return resultJob;
        } else {
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
        }

    }

    // Register SchemeId

    public BulkJob registerSchemeIds(SchemeName schemeName, SchemeIdBulkRegisterRequestDto request) throws APIException {
        if (authenticateToken())
            return this.registerBulkSchemeIds(schemeName, request);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public BulkJob registerBulkSchemeIds(SchemeName schemeName, SchemeIdBulkRegisterRequestDto schemeIdBulkRegisterDto) throws APIException {
        BulkJob bulkJob = new BulkJob();
        UserDTO userObj = this.getAuthenticatedUser();

        if (this.isAbleUser(schemeName, userObj)) {
            if (schemeIdBulkRegisterDto.getRecords() == null || schemeIdBulkRegisterDto.getRecords().size() == 0) {
                throw new APIException(HttpStatus.BAD_REQUEST,"Records property cannot be empty.");
            }
            schemeIdBulkRegisterDto.setType(JobTypeConstants.REGISTER_SCHEMEIDS);
            schemeIdBulkRegisterDto.setAuthor(userObj.getLogin());
            schemeIdBulkRegisterDto.setModel(ModelsConstants.SCHEME_ID);
            schemeIdBulkRegisterDto.setScheme(schemeName.toString());

            SchemeIdBulkRegisterRequestDto requestDto = new SchemeIdBulkRegisterRequestDto(schemeIdBulkRegisterDto.getRecords(), schemeIdBulkRegisterDto.getSoftware(),
                    schemeIdBulkRegisterDto.getComments(),
                    schemeIdBulkRegisterDto.getAuthor(),
                    schemeIdBulkRegisterDto.getModel(),schemeIdBulkRegisterDto.getScheme(),schemeIdBulkRegisterDto.getType());

            BulkJob bulk = new BulkJob();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String regString = objectMapper.writeValueAsString(requestDto);
                bulk.setName(JobTypeConstants.REGISTER_SCHEMEIDS);
                bulk.setStatus("0");
                bulk.setRequest(regString);

            } catch (JsonProcessingException e) {
                throw new APIException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            BulkJob resultJob = this.bulkJobRepository.save(bulk);
            System.out.println("result:" + resultJob);
            return resultJob;

        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    // Reserve SchemeId bulk


    public BulkJob reserveSchemeIds(SchemeName schemeName, SchemeIdBulkReserveRequestDto request) throws APIException {
        if (authenticateToken())
            return this.reserveBulkSchemeIds(schemeName, request);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public BulkJob reserveBulkSchemeIds(SchemeName schemeName, SchemeIdBulkReserveRequestDto request) throws APIException {
        BulkJob bulkJob = new BulkJob();
        UserDTO userObj = this.getAuthenticatedUser();

        if (this.isAbleUser(schemeName, userObj)) {
            if(request.getQuantity() != null || request.getQuantity()<1)
            {
                throw new APIException(HttpStatus.BAD_REQUEST,"Quantity property cannot be lower to 1.");
            }
            request.setType(JobTypeConstants.RESERVE_SCHEMEIDS);
            request.setModel(ModelsConstants.SCHEME_ID);
            request.setAuthor(userObj.getLogin());
            request.setScheme(schemeName.toString());

            SchemeIdBulkReserveRequestDto requestDto=new SchemeIdBulkReserveRequestDto(request.getQuantity(),
                    request.getSoftware(),request.getExpirationDate(),
                    request.getComment(), request.getAuthor(), request.getModel(),
                    request.getScheme(), request.getType());

            BulkJob bulk = new BulkJob();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String regString = objectMapper.writeValueAsString(requestDto);
                bulk.setName(JobTypeConstants.RESERVE_SCHEMEIDS);
                bulk.setStatus("0");
                bulk.setRequest(regString);

            } catch (JsonProcessingException e) {
                throw new APIException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            BulkJob resultJob = this.bulkJobRepository.save(bulk);
            System.out.println("result:" + resultJob);
            return resultJob;

        }
        else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    public BulkJob deprecateSchemeIds(SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws APIException {
        if (authenticateToken())
            return this.deprecateBulkSchemeIds(schemeName, request);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public BulkJob deprecateBulkSchemeIds(SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws APIException {
        com.snomed.api.domain.BulkJob bulkJob = new com.snomed.api.domain.BulkJob();
        UserDTO userObj = this.getAuthenticatedUser();

        if (this.isAbleUser(schemeName, userObj)) {
            if(request.getSchemeIds()==null || request.getSchemeIds().size() <1)
            {
                throw new APIException(HttpStatus.UNAUTHORIZED,"SchemeIds property cannot be empty.");
            }

            request.setType(JobTypeConstants.DEPRECATE_SCHEMEIDS);
            request.setModel(ModelsConstants.SCHEME_ID);
            request.setAuthor(userObj.getLogin());
            request.setScheme(schemeName.toString());/*schemeName.schemeName.toUpperCase();*/

            SchemeIdBulkDeprecateRequestDto requestDto=new SchemeIdBulkDeprecateRequestDto(request.getSchemeIds(),
                    request.getSoftware(),request.getComment(), request.getAuthor(), request.getModel(),
                    request.getScheme(), request.getType());
            return  createJob(requestDto);

        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    public BulkJob createJob(SchemeIdBulkDeprecateRequestDto requestDto) throws APIException {
        BulkJob bulk = new BulkJob();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String regString = objectMapper.writeValueAsString(requestDto);
            bulk.setName(JobTypeConstants.RESERVE_SCHEMEIDS);
            bulk.setStatus("0");
            bulk.setRequest(regString);

        } catch (JsonProcessingException e) {
            throw new APIException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        BulkJob resultJob = this.bulkJobRepository.save(bulk);
        System.out.println("result:" + resultJob);
        return resultJob;
    }

//releaseSchemeIds

    public BulkJob releaseSchemeIds(SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws APIException {
        if (authenticateToken())
            return this.releaseBulkSchemeIds(schemeName, request);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public BulkJob releaseBulkSchemeIds(SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws APIException {
        com.snomed.api.domain.BulkJob bulkJob = new com.snomed.api.domain.BulkJob();
        UserDTO userObj = this.getAuthenticatedUser();

        if (this.isAbleUser(schemeName, userObj)) {
            if(request.getSchemeIds()==null || request.getSchemeIds().size() <1)
            {
                throw new APIException(HttpStatus.UNAUTHORIZED,"SchemeIds property cannot be empty.");
            }

            request.setType(JobTypeConstants.RELEASE_SCHEMEIDS);
            request.setModel(ModelsConstants.SCHEME_ID);
            request.setAuthor(userObj.getLogin());
            request.setScheme(schemeName.toString());/*schemeName.schemeName.toUpperCase();*/

            SchemeIdBulkDeprecateRequestDto requestDto=new SchemeIdBulkDeprecateRequestDto(request.getSchemeIds(),
                    request.getSoftware(),request.getComment(), request.getAuthor(), request.getModel(),
                    request.getScheme(), request.getType());
            return  createJob(requestDto);


        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }
    //publishSchemeIds
    public BulkJob publishSchemeIds(SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws APIException {
        if (authenticateToken())
            return this.publishBulkSchemeIds(schemeName, request);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public BulkJob publishBulkSchemeIds(SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws APIException {
        com.snomed.api.domain.BulkJob bulkJob = new com.snomed.api.domain.BulkJob();
        UserDTO userObj = this.getAuthenticatedUser();

        if (this.isAbleUser(schemeName, userObj)) {
            if(request.getSchemeIds()==null || request.getSchemeIds().size() <1)
            {
                throw new APIException(HttpStatus.UNAUTHORIZED,"SchemeIds property cannot be empty.");
            }

            request.setType(JobTypeConstants.PUBLISH_SCHEMEIDS);
            request.setModel(ModelsConstants.SCHEME_ID);
            request.setAuthor(userObj.getLogin());
            request.setScheme(schemeName.toString());/*schemeName.schemeName.toUpperCase();*/

            SchemeIdBulkDeprecateRequestDto requestDto=new SchemeIdBulkDeprecateRequestDto(request.getSchemeIds(),
                    request.getSoftware(),request.getComment(), request.getAuthor(), request.getModel(),
                    request.getScheme(), request.getType());
            return  createJob(requestDto);

        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }




}

