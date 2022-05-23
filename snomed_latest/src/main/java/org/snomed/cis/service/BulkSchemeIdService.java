package org.snomed.cis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.snomed.cis.controller.SecurityController;
import org.snomed.cis.controller.dto.*;
import org.snomed.cis.domain.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.util.*;
import org.snomed.cis.repository.BulkJobRepository;
import org.snomed.cis.repository.BulkSchemeIdRepository;
import org.snomed.cis.repository.PermissionsSchemeRepository;
import org.snomed.cis.repository.SchemeIdBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BulkSchemeIdService {
    @Autowired
    private BulkSchemeIdRepository bulkSchemeIdRepository;
    @Autowired
    AuthenticateToken authenticateToken;

    @Autowired
    private SchemeIdBaseRepository schemeIdBaseRepository;
    @Autowired
    private SecurityController securityController;
    @Autowired
    private BulkJobRepository bulkJobRepository;
    @Autowired
    private BulkSctidService bulkSctidService;

    @Autowired
    private SctIdHelper sctIdHelper;

    @Autowired
    private PermissionsSchemeRepository permissionsSchemeRepository;
    private SchemeIdHelper schemeIdHelper;
    private SNOMEDID snomedid;

    public boolean isAbleUser(SchemeName schemeName, UserDTO user) throws CisException {
        List<String> groups = authenticateToken.getGroupsList();
        boolean able = false;
        for (String group : groups) {
            if (group.equalsIgnoreCase("component-identifier-service-admin")) {
                able = true;
            }
        }
       /* List<String> admins = Arrays.asList("keerthika", "lakshmana", "c");
        for (String admin : admins) {
            if (admin.equalsIgnoreCase(user.getLogin())) {
                able = true;
            }
        }*/
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
                        throw new CisException(HttpStatus.BAD_REQUEST, "Error accessing groups");
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
        return able;
    }

    public boolean authenticateToken() throws CisException {
        UserDTO obj = this.securityController.authenticate();
        if (null != obj)
            return true;
        else
            return false;
    }

    public List<SchemeId> getSchemeIds(String token, SchemeName schemeName, String schemeIds) throws CisException {
        String[] schemedIdArray = schemeIds.replaceAll("\\s+", "").split(",");
        List<SchemeId> resSchemeArrayList = new ArrayList<>();
        if (bulkSctidService.authenticateToken(token)) {
            UserDTO userObj = bulkSctidService.getAuthenticatedUser();
            boolean able = isAbleUser(schemeName, userObj);
            if (able) {
                for (String schemeId : schemedIdArray) {
                    if (schemeId == null || schemeId.isEmpty()) {
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
                throw new CisException(HttpStatus.BAD_REQUEST, "No permission for the selected operation");
            }
        } else {
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
        }
        return resSchemeArrayList;
    }


    public SchemeId getFreeRecord(String schemeName, String diffSchemeId, String systemId, String autoSysId) throws CisException {
        Map<String, Object> schemeIdRecord = getNewRecord(schemeName, diffSchemeId, systemId);
        schemeIdRecord.put("status", "available");
        return insertSchemeIdRecord(schemeIdRecord);
    }

    private SchemeId insertSchemeIdRecord(Map<String, Object> schemeIdRecord) throws CisException {
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
                if (mapObj.getKey() == "scheme") {
                    scheme = (String) mapObj.getValue();
                } else if (mapObj.getKey() == "schemeId") {
                    schemeId = (String) mapObj.getValue();
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
                    expirationDate = (LocalDateTime) mapObj.getValue();
                } else if (mapObj.getKey() == "jobId") {
                    jobId = (Integer) mapObj.getValue();
                } else if (mapObj.getKey() == "created_at") {
                    created_at = (LocalDateTime) mapObj.getValue();
                } else if (mapObj.getKey() == "modified_at") {
                    modified_at = (LocalDateTime) mapObj.getValue();
                }
            }
            //refactor changes
            SchemeId schemeId1 = SchemeId.builder().scheme(String.valueOf(scheme)).schemeId(String.valueOf(schemeId))
                    .sequence(sequence).checkDigit(checkDigit).systemId(systemId).status(status).author(author).software(software).jobId(jobId)
                    .build();
            bulkSchemeIdRepository.save(schemeId1);
            //bulkSchemeIdRepository.insertWithQuery(String.valueOf(scheme), schemeId.toString(), sequence, checkDigit, systemId, status, author, software, expirationDate, jobId, created_at, modified_at);
            //refactor changes
            Optional<SchemeId> schemeDB = bulkSchemeIdRepository.findBySchemeAndSchemeId(String.valueOf(scheme), schemeId.toString());
            schemeIdBulk = schemeDB.isPresent()?schemeDB.get():null;
            return schemeIdBulk;
        } catch (Exception e) {
            error = e.toString();
        }
        if (error != null) {
            Optional<SchemeIdBase> schemeIdBaseList = schemeIdBaseRepository.findByScheme(schemeIdBulk.getScheme().toString());

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
                    throw new CisException(HttpStatus.BAD_REQUEST, "Unable to attempt to solve error");
                }

            } else {
                return schemeIdBulk;
            }
        }

        return null;
    }


    public Map<String, Object> getNewRecord(String schemeName, String diffSchemeId, String systemId) {
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
        return schemeIdRecord;
    }


    public UserDTO getAuthenticatedUser() throws CisException {
        return this.securityController.authenticate();
    }

    public BulkJob generateSchemeIds(String token, SchemeName schemeName, SchemeIdBulkGenerationRequestDto schemeIdBulkDto) throws CisException {

        /*
    * {
  "quantity": 0,
  "systemIds": [
    "string"
  ],
  "software": "string",
  "comment"
    * */

        //requestbody change
        if (bulkSctidService.authenticateToken(token)) {
            SchemeIdBulkGenerate bulkGenerate = new SchemeIdBulkGenerate();
            bulkGenerate.setQuantity(schemeIdBulkDto.getQuantity());
            bulkGenerate.setSystemIds(schemeIdBulkDto.getSystemIds());
            bulkGenerate.setSoftware(schemeIdBulkDto.getSoftware());
            bulkGenerate.setComment(schemeIdBulkDto.getComment());


            UserDTO userObj = bulkSctidService.getAuthenticatedUser();

            boolean able = isAbleUser(schemeName, userObj);
            if (!able) {
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
            if ((schemeIdBulkDto.getSystemIds().length != 0 && (schemeIdBulkDto.getSystemIds().length != schemeIdBulkDto.getQuantity()))) {
                throw new CisException(HttpStatus.BAD_REQUEST, "SystemIds quantity is not equal to quantity requirement");
            }
            if (schemeIdBulkDto.getSystemIds() != null || schemeIdBulkDto.getSystemIds().length == 0) {
                bulkGenerate.setAutoSysId(true);
            }
            bulkGenerate.setType(JobTypeConstants.GENERATE_SCHEMEIDS);
            bulkGenerate.setAuthor(userObj.getLogin());
            bulkGenerate.setModel(ModelsConstants.SCHEME_ID);
            bulkGenerate.setScheme(schemeName);
            /*SchemeIdBulkGenerationRequestDto requestDto = new SchemeIdBulkGenerationRequestDto(schemeIdBulkDto.getQuantity(), schemeIdBulkDto.getSystemIds(),
                    schemeIdBulkDto.getSoftware(), schemeIdBulkDto.getComment(), true, schemeIdBulkDto.getAuthor(),schemeIdBulkDto.getModel(),schemeIdBulkDto.getScheme(),schemeIdBulkDto.getType());
*/
// Type is set here not as an attribute
            BulkJob bulk = new BulkJob();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String regString = objectMapper.writeValueAsString(bulkGenerate);
                bulk.setName(JobTypeConstants.GENERATE_SCHEMEIDS);
                bulk.setStatus("0");
                bulk.setRequest(regString);

            } catch (JsonProcessingException e) {
                throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            BulkJob resultJob = this.bulkJobRepository.save(bulk);
            System.out.println("result:" + resultJob);
            return resultJob;
        } else {
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
        }

    }

    // Register SchemeId

    public BulkJob registerSchemeIds(String token, SchemeName schemeName, SchemeIdBulkRegisterRequestDto request) throws CisException {
        if (bulkSctidService.authenticateToken(token))
            return this.registerBulkSchemeIds(schemeName, request);
        else
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public BulkJob registerBulkSchemeIds(SchemeName schemeName, SchemeIdBulkRegisterRequestDto schemeIdBulkRegisterDto) throws CisException {
        BulkJob bulkJob = new BulkJob();
        UserDTO userObj = bulkSctidService.getAuthenticatedUser();
/*
*
* {
  "records": [
    {
      "schemeId": "string",
      "systemId": "string"
    }
  ],
  "software": "string",
  "comment": "string"
}
* */
        //requestbody change
        if (this.isAbleUser(schemeName, userObj)) {
            SchemeIdBulkRegister bulkRegister = new SchemeIdBulkRegister();
            bulkRegister.setRecords(schemeIdBulkRegisterDto.getRecords());
            bulkRegister.setSoftware(schemeIdBulkRegisterDto.getSoftware());
            bulkRegister.setComment(schemeIdBulkRegisterDto.getComment());


//requestbody change


            if (schemeIdBulkRegisterDto.getRecords() == null || schemeIdBulkRegisterDto.getRecords().size() == 0) {
                throw new CisException(HttpStatus.BAD_REQUEST, "Records property cannot be empty.");
            }
            bulkRegister.setType(JobTypeConstants.REGISTER_SCHEMEIDS);
            bulkRegister.setAuthor(userObj.getLogin());
            bulkRegister.setModel(ModelsConstants.SCHEME_ID);
            bulkRegister.setScheme(schemeName.toString());

           /* SchemeIdBulkRegisterRequestDto requestDto = new SchemeIdBulkRegisterRequestDto(schemeIdBulkRegisterDto.getRecords(), schemeIdBulkRegisterDto.getSoftware(),
                    schemeIdBulkRegisterDto.getComment(),
                    schemeIdBulkRegisterDto.getAuthor(),
                    schemeIdBulkRegisterDto.getModel(),schemeIdBulkRegisterDto.getScheme(),schemeIdBulkRegisterDto.getType());
*/
            BulkJob bulk = new BulkJob();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String regString = objectMapper.writeValueAsString(bulkRegister);
                bulk.setName(JobTypeConstants.REGISTER_SCHEMEIDS);
                bulk.setStatus("0");
                bulk.setRequest(regString);

            } catch (JsonProcessingException e) {
                throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            BulkJob resultJob = this.bulkJobRepository.save(bulk);
            System.out.println("result:" + resultJob);
            return resultJob;

        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    // Reserve SchemeId bulk


    public BulkJob reserveSchemeIds(String token, SchemeName schemeName, SchemeIdBulkReserveRequestDto request) throws CisException {
        if (bulkSctidService.authenticateToken(token))
            return this.reserveBulkSchemeIds(schemeName, request);
        else
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public BulkJob reserveBulkSchemeIds(SchemeName schemeName, SchemeIdBulkReserveRequestDto request) throws CisException {
        BulkJob bulkJob = new BulkJob();
        UserDTO userObj = bulkSctidService.getAuthenticatedUser();
/*
* {
  "quantity": 0,
  "software": "string",
  "expirationDate": "string",
  "comment": "string"
}
* */
        //request body change
        if (this.isAbleUser(schemeName, userObj)) {

            SchemeIdBulkReserve bulkReserve = new SchemeIdBulkReserve();
            bulkReserve.setQuantity(request.getQuantity());
            bulkReserve.setSoftware((request.getSoftware()));
            bulkReserve.setExpirationDate(request.getExpirationDate());
            bulkReserve.setComment(request.getComment());

            if ((null == request.getQuantity()) || request.getQuantity() < 1) {
                throw new CisException(HttpStatus.BAD_REQUEST, "Quantity property cannot be lower to 1.");
            }
            bulkReserve.setType(JobTypeConstants.RESERVE_SCHEMEIDS);
            bulkReserve.setModel(ModelsConstants.SCHEME_ID);
            bulkReserve.setAuthor(userObj.getLogin());
            bulkReserve.setScheme(schemeName.toString());

          /*  SchemeIdBulkReserveRequestDto requestDto=new SchemeIdBulkReserveRequestDto(request.getQuantity(),
                    request.getSoftware(),request.getExpirationDate(),
                    request.getComment(), request.getAuthor(), request.getModel(),
                    request.getScheme(), request.getType());
*/
            BulkJob bulk = new BulkJob();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String regString = objectMapper.writeValueAsString(bulkReserve);
                bulk.setName(JobTypeConstants.RESERVE_SCHEMEIDS);
                bulk.setStatus("0");
                bulk.setRequest(regString);

            } catch (JsonProcessingException e) {
                throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            BulkJob resultJob = this.bulkJobRepository.save(bulk);
            System.out.println("result:" + resultJob);
            return resultJob;

        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    //deprecateSchemeIds

    public BulkJob deprecateSchemeIds(String token, SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws CisException {
        if (bulkSctidService.authenticateToken(token))
            return this.deprecateBulkSchemeIds(schemeName, request);
        else
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public BulkJob deprecateBulkSchemeIds(SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws CisException {
        BulkJob bulkJob = new BulkJob();
        UserDTO userObj = bulkSctidService.getAuthenticatedUser();
  /*
    *
    * {
  "schemeIds": [
    "string"
  ],
  "software": "string",
  "comment": "string"
}*/

        //requestbody change

        if (this.isAbleUser(schemeName, userObj)) {
            BulkSchemeIdUpdate bulkSchemeIdUpdate = new BulkSchemeIdUpdate();
            bulkSchemeIdUpdate.setSchemeIds(request.getSchemeIds());
            bulkSchemeIdUpdate.setSoftware(request.getSoftware());
            bulkSchemeIdUpdate.setComment(request.getComment());

            if (request.getSchemeIds() == null || request.getSchemeIds().size() < 1) {
                throw new CisException(HttpStatus.UNAUTHORIZED, "SchemeIds property cannot be empty.");
            }
//requestbody change
            bulkSchemeIdUpdate.setType(JobTypeConstants.DEPRECATE_SCHEMEIDS);
            bulkSchemeIdUpdate.setModel(ModelsConstants.SCHEME_ID);
            bulkSchemeIdUpdate.setAuthor(userObj.getLogin());
            bulkSchemeIdUpdate.setScheme(schemeName.toString());/*schemeName.schemeName.toUpperCase();*/

            /*SchemeIdBulkDeprecateRequestDto requestDto=new SchemeIdBulkDeprecateRequestDto(request.getSchemeIds(),
                    request.getSoftware(),request.getComment(), request.getAuthor(), request.getModel(),
                    request.getScheme(), request.getType());*/
            return createJob(bulkSchemeIdUpdate, "deprecate");

        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    //requestbody change
    public BulkJob createJob(BulkSchemeIdUpdate bulkSchemeIdUpdate, String functionType) throws CisException {
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
            throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        BulkJob resultJob = this.bulkJobRepository.save(bulk);
        System.out.println("result:" + resultJob);
        return resultJob;
    }

//releaseSchemeIds

    public BulkJob releaseSchemeIds(String token, SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws CisException {
        if (bulkSctidService.authenticateToken(token))
            return this.releaseBulkSchemeIds(schemeName, request);
        else
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public BulkJob releaseBulkSchemeIds(SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws CisException {
        BulkJob bulkJob = new BulkJob();
        UserDTO userObj = bulkSctidService.getAuthenticatedUser();

        if (this.isAbleUser(schemeName, userObj)) {
//requestbody change
            BulkSchemeIdUpdate bulkSchemeIdUpdate = new BulkSchemeIdUpdate();
            bulkSchemeIdUpdate.setSchemeIds(request.getSchemeIds());
            bulkSchemeIdUpdate.setSoftware(request.getSoftware());
            bulkSchemeIdUpdate.setComment(request.getComment());

            if (request.getSchemeIds() == null || request.getSchemeIds().size() < 1) {
                throw new CisException(HttpStatus.UNAUTHORIZED, "SchemeIds property cannot be empty.");
            }

            bulkSchemeIdUpdate.setType(JobTypeConstants.RELEASE_SCHEMEIDS);
            bulkSchemeIdUpdate.setModel(ModelsConstants.SCHEME_ID);
            bulkSchemeIdUpdate.setAuthor(userObj.getLogin());
            bulkSchemeIdUpdate.setScheme(schemeName.toString());/*schemeName.schemeName.toUpperCase();*/

           /* SchemeIdBulkDeprecateRequestDto requestDto=new SchemeIdBulkDeprecateRequestDto(request.getSchemeIds(),
                    request.getSoftware(),request.getComment(), request.getAuthor(), request.getModel(),
                    request.getScheme(), request.getType());*/
            return createJob(bulkSchemeIdUpdate, "release");


        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    //publishSchemeIds
    public BulkJob publishSchemeIds(String token, SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws CisException {
        if (bulkSctidService.authenticateToken(token))
            return this.publishBulkSchemeIds(schemeName, request);
        else
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public BulkJob publishBulkSchemeIds(SchemeName schemeName, SchemeIdBulkDeprecateRequestDto request) throws CisException {
        BulkJob bulkJob = new BulkJob();
        UserDTO userObj = bulkSctidService.getAuthenticatedUser();
        if (this.isAbleUser(schemeName, userObj)) {
            //requestbody change
            BulkSchemeIdUpdate bulkSchemeIdUpdate = new BulkSchemeIdUpdate();
            bulkSchemeIdUpdate.setSchemeIds(request.getSchemeIds());
            bulkSchemeIdUpdate.setSoftware(request.getSoftware());
            bulkSchemeIdUpdate.setComment(request.getComment());

            if (request.getSchemeIds() == null || request.getSchemeIds().size() < 1) {
                throw new CisException(HttpStatus.UNAUTHORIZED, "SchemeIds property cannot be empty.");
            }

            bulkSchemeIdUpdate.setType(JobTypeConstants.PUBLISH_SCHEMEIDS);
            bulkSchemeIdUpdate.setModel(ModelsConstants.SCHEME_ID);
            bulkSchemeIdUpdate.setAuthor(userObj.getLogin());
            bulkSchemeIdUpdate.setScheme(schemeName.toString());/*schemeName.schemeName.toUpperCase();*/

           /* SchemeIdBulkDeprecateRequestDto requestDto=new SchemeIdBulkDeprecateRequestDto(request.getSchemeIds(),
                    request.getSoftware(),request.getComment(), request.getAuthor(), request.getModel(),
                    request.getScheme(), request.getType());*/
            return createJob(bulkSchemeIdUpdate, "publish");

        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }


}

