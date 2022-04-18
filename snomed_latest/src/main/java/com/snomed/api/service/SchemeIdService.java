package com.snomed.api.service;

import com.google.common.collect.MutableClassToInstanceMap;
import com.snomed.api.controller.SecurityController;
import com.snomed.api.controller.dto.*;
import com.snomed.api.domain.*;
import com.snomed.api.exception.APIException;
import com.snomed.api.helper.CTV3ID;
import com.snomed.api.helper.SNOMEDID;
import com.snomed.api.helper.SchemeIdHelper;
import com.snomed.api.helper.StateMachine;
import com.snomed.api.repository.BulkSchemeIdRepository;
import com.snomed.api.repository.PermissionsSchemeRepository;
import com.snomed.api.repository.SchemeIdBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@Service
public class SchemeIdService {
    @Autowired
    private SecurityController securityController;

    @Autowired
    private BulkSchemeIdRepository bulkSchemeIdRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private PermissionsSchemeRepository permissionsSchemeRepository;
    @Autowired
    private SchemeIdBaseRepository schemeIdBaseRepository;

    private SchemeIdHelper schemeIdHelper;


    @Autowired
    StateMachine stateMachine;

    public boolean isAbleUser(SchemeName schemeName, UserDTO user) throws APIException {
        boolean able = false;
        List<String> admins = Arrays.asList("keerthika", "b", "c");
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

    public UserDTO getAuthenticatedUser() throws APIException {
        return this.securityController.authenticate();
    }

    public List<SchemeId> getSchemeIds(String limit, String skip, SchemeName schemeName) throws APIException {
        if (authenticateToken())
            return this.getSchemeIdsList(limit, skip, schemeName);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    private List<SchemeId> getSchemeIdsList(String limit, String skip, SchemeName schemeName) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        List<SchemeId> schemeidList = new ArrayList<>();

        if (this.isAbleUser(schemeName, userObj)) {
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
                objQuery.put("schemeName", schemeName.toString());
            }

            String swhere = "";
            if (objQuery.size() > 0) {
                for (var query :
                        objQuery.entrySet()) {
                    swhere += " And " + query.getKey() + "=" + (query.getValue());
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
            Query genQuery = entityManager.createQuery(sql);
            List<SchemeId> resultList = genQuery.getResultList();
            if ((skipTo == 0)) {
                schemeidList = resultList;
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
                schemeidList = newRows;
            }
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return schemeidList;
    }

    public SchemeId getSchemeIdsByschemeId(SchemeName schemeName, String schemeid) throws APIException {
        if (authenticateToken())
            return this.getSchemeIdsByschemeIdList(schemeName, schemeid);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public SchemeId getSchemeIdsByschemeIdList(SchemeName schemeName, String schemeid) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        SchemeId record = new SchemeId();
        if (isAbleUser(schemeName, userObj)) {
            if (schemeid == null || schemeid == "") {

                throw new APIException(HttpStatus.UNAUTHORIZED, "Not a valid schemeid");
            } else {
                boolean isValidScheme = false;
                if ("SNOMEDID".equalsIgnoreCase(schemeName.toString().toUpperCase())) {
                    isValidScheme = SNOMEDID.validSchemeId(schemeid);
                } else if ("CTV3ID".equalsIgnoreCase(schemeName.toString().toUpperCase())) {
                    isValidScheme = CTV3ID.validSchemeId(schemeid);
                }
                List<SchemeId> schemeIdList = bulkSchemeIdRepository.findBySchemeAndSchemeId(schemeName, schemeid);
                if (!schemeIdList.isEmpty()) {
                    record = getFreeRecords(schemeName, schemeid);
                }
                return record;
            }
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }

    }

    private SchemeId getFreeRecords(SchemeName schemeName, String schemeid) throws APIException {
        Map<String, Object> schemeIdRecord = getNewRecord(schemeName, schemeid);
        schemeIdRecord.put("status", "avilable");
        return insertSchemeIdRecord(schemeIdRecord);
    }

    private Map<String, Object> getNewRecord(SchemeName schemeName, String schemeid) {
        Map<String, Object> schemeIdRecord = new LinkedHashMap<>();
        schemeIdRecord.put("scheme", schemeName);
        schemeIdRecord.put("schemeId", schemeid);
        schemeIdRecord.put("sequence", schemeIdHelper.getSequence(schemeid));
        schemeIdRecord.put("checkDigit", schemeIdHelper.getCheckDigit(schemeid));
        schemeIdRecord.put("systemId", schemeIdHelper.guid());
        return schemeIdRecord;
    }

    private SchemeId insertSchemeIdRecord(Map<String, Object> schemeIdRecord) throws APIException {
        String error;
        SchemeId schemeIdBulk = null;
        SchemeName scheme = null;
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
                    scheme = (SchemeName) mapObj.getValue();
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
            bulkSchemeIdRepository.insertWithQuery(String.valueOf(scheme), schemeId, sequence, checkDigit, systemId, status, author, software, expirationDate, jobId, created_at, modified_at);
            schemeIdBulk = (SchemeId) bulkSchemeIdRepository.findBySchemeAndSchemeId(scheme, schemeId.toString());
            return schemeIdBulk;
        } catch (Exception e) {
            error = e.toString();
        }

        return null;
    }


    public SchemeId getSchemeIdsBySystemId(SchemeName schemeName, String systemid) throws APIException {
        if (authenticateToken())
            return this.getSchemeIdsBysystemList(schemeName, systemid);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");

    }

    private SchemeId getSchemeIdsBysystemList(SchemeName schemeName, String systemid) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        if(isAbleUser(schemeName,userObj)){
            List<SchemeId> schemeIdList=bulkSchemeIdRepository.findBySchemeAndSystemId(schemeName,systemid);

            if(!schemeIdList.isEmpty() && schemeIdList.size()>0){
                return schemeIdList.get(0);
            }
            else
            {
                throw new APIException(HttpStatus.UNAUTHORIZED, "SchemeId list is empty");
            }
        }
        else{
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }


    }

    public SchemeId deprecateSchemeIds(SchemeName schemeName, SchemeIdUpdateRequestDto request) throws APIException {
        if (authenticateToken())
            return this.deprecateSchemeIdList(schemeName, request);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");

    }

    private SchemeId deprecateSchemeIdList(SchemeName schemeName, SchemeIdUpdateRequestDto request) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        SchemeId schemeId=new SchemeId();
        if (isAbleUser(schemeName, userObj)) {

            request.setAuthor(userObj.getLogin());
            SchemeId schemeIdrecord=getSchemeIdsByschemeId(schemeName,request.getSchemeId());

            //
            if(schemeIdrecord.getSchemeId().isEmpty())
            {
                throw new APIException(HttpStatus.NOT_FOUND,"SchemeId record is empty");
            }
            else
            {
                var newStatus = stateMachine.getNewStatus(schemeIdrecord.getStatus(), stateMachine.actions.get("deprecate"));
                if(null!=newStatus)
                {
                    schemeIdrecord.setStatus(newStatus);
                    schemeIdrecord.setAuthor(request.getAuthor());
                    schemeIdrecord.setSoftware(request.getSoftware());
                    schemeIdrecord.setComment(request.getComment());
                    schemeIdrecord.setJobId(Integer.parseInt("null"));
                    schemeId = bulkSchemeIdRepository.save(schemeIdrecord);
                }
            }
            //
        }
        else{
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
return schemeId;

    }
    //releaseSchmeId

    public SchemeId releaseSchemeIds(SchemeName schemeName, SchemeIdUpdateRequestDto request) throws APIException {

        if (authenticateToken())
            return this.releaseSchemeIdList(schemeName, request);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");

    }

    private SchemeId releaseSchemeIdList(SchemeName schemeName, SchemeIdUpdateRequestDto request) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        SchemeId schemeId=new SchemeId();

        if (isAbleUser(schemeName, userObj)) {
            request.setAuthor(userObj.getLogin());
            SchemeId schemeIdrecord=getSchemeIdsByschemeId(schemeName,request.getSchemeId());

            //
            if(schemeIdrecord.getSchemeId().isEmpty())
            {
                throw new APIException(HttpStatus.NOT_FOUND,"SchemeId record is empty");
            }
            else
            {
                var newStatus = stateMachine.getNewStatus(schemeIdrecord.getStatus(), stateMachine.actions.get("release"));
                if(null!=newStatus)
                {
                    schemeIdrecord.setStatus(newStatus);
                    schemeIdrecord.setAuthor(request.getAuthor());
                    schemeIdrecord.setSoftware(request.getSoftware());
                    schemeIdrecord.setComment(request.getComment());
                    schemeIdrecord.setJobId(Integer.parseInt("null"));
                    schemeId = bulkSchemeIdRepository.save(schemeIdrecord);
                }
            }
            //
        }
        else
        {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return schemeId;
    }

    //pblish

    public SchemeId publishSchemeId(SchemeName schemeName, SchemeIdUpdateRequestDto request) throws APIException {

        if (authenticateToken())
            return this.publishSchemeIdList(schemeName, request);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");

    }

    private SchemeId publishSchemeIdList(SchemeName schemeName, SchemeIdUpdateRequestDto request) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        SchemeId schemeId=new SchemeId();

        if (isAbleUser(schemeName, userObj)) {
            request.setAuthor(userObj.getLogin());
            SchemeId schemeIdrecord=getSchemeIdsByschemeId(schemeName,request.getSchemeId());

            //
            if(schemeIdrecord.getSchemeId().isEmpty())
            {
                throw new APIException(HttpStatus.NOT_FOUND,"SchemeId record is empty");
            }
            else
            {
                var newStatus = stateMachine.getNewStatus(schemeIdrecord.getStatus(), stateMachine.actions.get("publish"));
                if(null!=newStatus)
                {
                    schemeIdrecord.setStatus(newStatus);
                    schemeIdrecord.setAuthor(request.getAuthor());
                    schemeIdrecord.setSoftware(request.getSoftware());
                    schemeIdrecord.setComment(request.getComment());
                    schemeIdrecord.setJobId(Integer.parseInt("null"));
                    schemeId = bulkSchemeIdRepository.save(schemeIdrecord);
                }
            }
            //
        }
        else
        {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return schemeId;
    }
    public SchemeId reserveSchemeId(SchemeName schemeName, SchemeIdReserveRequestDto request) throws APIException {
        if (authenticateToken())
            return this.reserveSchemeIdList(schemeName, request);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    private SchemeId reserveSchemeIdList(SchemeName schemeName, SchemeIdReserveRequestDto request) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();

        if (this.isAbleUser(schemeName, userObj)) {
            request.setAuthor(userObj.getLogin());
            setNewSchemeIdRecord(schemeName, request, stateMachine.actions.get("reserve"));
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return null;
    }

    //List
    private SchemeId setNewSchemeIdRecord(SchemeName schemeName, SchemeIdReserveRequestDto request, String reserve) throws APIException {
        SchemeId record = setAvailableSchemeIdRecord2NewStatus(schemeName, request, reserve);
        try {
            if (record != null) {
                return record;
            } else {
                SchemeId schemeIdRec = counterMode(schemeName, request, reserve);
                if (schemeIdRec != null) {
                    return schemeIdRec;
                } else {
                    throw new APIException(HttpStatus.NOT_FOUND, "Error");
                }

            }
        } catch (Exception e) {
            throw new APIException(HttpStatus.NOT_FOUND, "error getting available schemeId for:" + schemeName + e.getMessage());
        }

    }

    private SchemeId setAvailableSchemeIdRecord2NewStatus(SchemeName schemeName, SchemeIdReserveRequestDto request, String reserve) throws APIException {
        Map<String, String> objQuery = new HashMap<String, String>();
        SchemeId outputSchemeRec = new SchemeId();
        SchemeId updatedrecord = null;
        if (null != schemeName) {
            objQuery.put("schemeName", schemeName.toString());
            objQuery.put("status", "available");
        }

        SchemeId schemeIdRecords = findschemeRecord(objQuery, "1", null);
        //no list so no size
        if (schemeIdRecords != null /*&& schemeIdRecords.size() > 0*/) {

            var newStatus = stateMachine.getNewStatus(schemeIdRecords.getStatus(), reserve);
            if (!newStatus.isBlank()) {

                if (null != request.getSystemId() && request.getSystemId().trim() != "") {
                    schemeIdRecords.setSystemId(request.getSystemId());
                }
                schemeIdRecords.setStatus(newStatus);
                schemeIdRecords.setAuthor(request.getAuthor());
                schemeIdRecords.setSoftware(request.getSoftware());
                //Expiration Date Not available in request.
                schemeIdRecords.setExpirationDate(null);
                schemeIdRecords.setComment(request.getComment());
                schemeIdRecords.setJobId(Integer.parseInt("null"));
                // outputSchemeRec = bulkSchemeIdRepository.save(schemeIdRecords.get(0));
                updatedrecord = updateSchemeIdRecord(schemeIdRecords, schemeName);
                return updatedrecord;
            } else {
                counterMode(schemeName, request, reserve);
            }
        } else {
            return null;
        }
        return updatedrecord;

    }

    public SchemeId counterMode(SchemeName schemeName, SchemeIdReserveRequestDto request, String reserve) throws APIException {
        SchemeId newSchemeId = getNextSchemeId(schemeName,  request);
        if (newSchemeId != null) {
            SchemeId schemeIdRecord = getSchemeIdsByschemeIdList(schemeName, newSchemeId.toString());
            SchemeId updatedrecord;
            if (schemeIdRecord != null) {
                var newStatus = stateMachine.getNewStatus(schemeIdRecord.getStatus(), reserve);
                if (!newStatus.isBlank()) {

                    if (null != request.getSystemId() && request.getSystemId().trim() != "") {
                        schemeIdRecord.setSystemId(request.getSystemId());
                    }
                    schemeIdRecord.setStatus(newStatus);
                    schemeIdRecord.setAuthor(request.getAuthor());
                    schemeIdRecord.setSoftware(request.getSoftware());
                    //Expiration Date Not available in request.
                    schemeIdRecord.setExpirationDate(null);
                    schemeIdRecord.setComment(request.getComment());
                    schemeIdRecord.setJobId(Integer.parseInt("null"));
                    // outputSchemeRec = bulkSchemeIdRepository.save(schemeIdRecords.get(0));
                    updatedrecord = updateSchemeIdRecord(schemeIdRecord, schemeName);
                    return updatedrecord;
                } else {
                    counterMode(schemeName, request, reserve);
                }
            }
        }
        return newSchemeId;
    }

    private SchemeId getNextSchemeId(SchemeName schemeName, SchemeIdReserveRequestDto request) {
        List<SchemeIdBase> schemeIdBaseList = schemeIdBaseRepository.findByScheme(schemeName.toString());
        SchemeIdBase schemeIdBase = null;
        schemeIdBase.setIdBase(schemeIdBaseList.get(0).getIdBase());
        schemeIdBaseRepository.save(schemeIdBase);
        return null;//List<SchmeId>
    }

    public SchemeId updateSchemeIdRecord(SchemeId schemeId, SchemeName schemeName) {
        Map<String, String> objQuery = new HashMap<String, String>();

        if (null != schemeId) {
            objQuery.put("schemeId", String.valueOf(schemeId));
            objQuery.put("schemeName", schemeName.toString());
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

    public SchemeId findschemeRecord(Map<String, String> objQuery, String limit, String skip) {
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
                swhere += " And " + query.getKey() + "=" + (query.getValue());
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
        Query genQuery = entityManager.createQuery(sql);
        SchemeId resultList = (SchemeId) genQuery.getResultList();
        if ((skipTo == 0)) {
            schemeidList = resultList;
        } /*else {
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
            schemeidList = newRows;
        }*/
        return schemeidList;
    }


    public SchemeId generateSchemeId(SchemeName schemeName, SchemeIdGenerateRequestDto request) throws APIException {
        if (authenticateToken())
            return this.generateSchemeIds(schemeName, request);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public SchemeId generateSchemeIds(SchemeName schemeName, SchemeIdGenerateRequestDto request) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        SchemeId schemeIdRec = new SchemeId();
        if (this.isAbleUser(schemeName, userObj)) {
            if (request.getSystemId() != null || request.getSystemId().trim() == "") {
                request.setSystemId(schemeIdHelper.guid());
                request.setAutoSysId(true);
            }
            request.setAuthor(request.getAuthor());
            if (!request.isAutoSysId()) {
                schemeIdRec = getSchemeIdBySystemId(schemeName, request.systemId);
                if (schemeIdRec != null) {
                    return schemeIdRec;
                } else {
                    setNewSchemeIdRecordGen(schemeName, request, stateMachine.actions.get("generate"));
                }
            } else {
                setNewSchemeIdRecordGen(schemeName, request, stateMachine.actions.get("generate"));
            }

        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return schemeIdRec;
    }

    public SchemeId getSchemeIdBySystemId(SchemeName schemeName, String systemId) {
        List<SchemeId> schemeId = bulkSchemeIdRepository.findBySchemeAndSystemId(schemeName, systemId);
        if (schemeId.size() > 0) {
            return (SchemeId) schemeId.get(0);
        } else {
            return (SchemeId) schemeId;
        }

    }

    public SchemeId setNewSchemeIdRecordGen(SchemeName schemeName, SchemeIdGenerateRequestDto request, String reserve) throws APIException {
        SchemeId record = setAvailableSchemeIdRecord2NewStatusGen(schemeName, request, reserve);
        try {
            if (record != null) {
                return record;
            } else {
                SchemeId schemeIdRec = counterModeGen(schemeName, request, reserve);
                if (schemeIdRec != null) {
                    return schemeIdRec;
                } else {
                    throw new APIException(HttpStatus.NOT_FOUND, "Error");
                }

            }
        } catch (Exception e) {
            throw new APIException(HttpStatus.NOT_FOUND, "error getting available schemeId for:" + schemeName + e.getMessage());
        }

    }

    public SchemeId setAvailableSchemeIdRecord2NewStatusGen(SchemeName schemeName, SchemeIdGenerateRequestDto request, String generate) throws APIException {
        Map<String, String> objQuery = new HashMap<String, String>();
        SchemeId outputSchemeRec = new SchemeId();
        SchemeId updatedrecord = null;
        if (null != schemeName) {
            objQuery.put("schemeName", schemeName.toString());
            objQuery.put("status", "available");
        }

        SchemeId schemeIdRecords = findschemeRecord(objQuery, "1", null);
        //no list so no size
        if (schemeIdRecords != null /*&& schemeIdRecords.size() > 0*/) {

            var newStatus = stateMachine.getNewStatus(schemeIdRecords.getStatus(), generate);
            if (!newStatus.isBlank()) {

                if (null != request.getSystemId() && request.getSystemId().trim() != "") {
                    schemeIdRecords.setSystemId(request.getSystemId());
                }
                schemeIdRecords.setStatus(newStatus);
                schemeIdRecords.setAuthor(request.getAuthor());
                schemeIdRecords.setSoftware(request.getSoftware());
                //Expiration Date Not available in request.
                schemeIdRecords.setExpirationDate(null);
                schemeIdRecords.setComment(request.getComment());
                schemeIdRecords.setJobId(Integer.parseInt("null"));
                // outputSchemeRec = bulkSchemeIdRepository.save(schemeIdRecords.get(0));
                updatedrecord = updateSchemeIdRecord(schemeIdRecords, schemeName);
                return updatedrecord;
            } else {
                counterModeGen(schemeName, request, generate);
            }
        } else {
            return null;
        }
        return updatedrecord;

    }

    public SchemeId counterModeGen(SchemeName schemeName, SchemeIdGenerateRequestDto request, String reserve) throws APIException {
        SchemeId newSchemeId = getNextSchemeIdGen(schemeName, request);
        if (newSchemeId != null) {
            SchemeId schemeIdRecord = getSchemeIdsByschemeIdList(schemeName, newSchemeId.toString());
            SchemeId updatedrecord;
            if (schemeIdRecord != null) {
                var newStatus = stateMachine.getNewStatus(schemeIdRecord.getStatus(), reserve);
                if (!newStatus.isBlank()) {

                    if (null != request.getSystemId() && request.getSystemId().trim() != "") {
                        schemeIdRecord.setSystemId(request.getSystemId());
                    }
                    schemeIdRecord.setStatus(newStatus);
                    schemeIdRecord.setAuthor(request.getAuthor());
                    schemeIdRecord.setSoftware(request.getSoftware());
                    //Expiration Date Not available in request.
                    schemeIdRecord.setExpirationDate(null);
                    schemeIdRecord.setComment(request.getComment());
                    schemeIdRecord.setJobId(Integer.parseInt("null"));
                    // outputSchemeRec = bulkSchemeIdRepository.save(schemeIdRecords.get(0));
                    updatedrecord = updateSchemeIdRecord(schemeIdRecord, schemeName);
                    return updatedrecord;
                } else {
                    counterModeGen(schemeName, request, reserve);
                }
            }
        }
        return newSchemeId;
    }

    public SchemeId getNextSchemeIdGen(SchemeName schemeName, SchemeIdGenerateRequestDto request) {
        List<SchemeIdBase> schemeIdBaseList = schemeIdBaseRepository.findByScheme(schemeName.toString());
        SchemeIdBase schemeIdBase = null;
        schemeIdBase.setIdBase(schemeIdBaseList.get(0).getIdBase());
        schemeIdBaseRepository.save(schemeIdBase);
        return null;//List<SchmeId>
    }

    //registerSchemeId
    public SchemeId registerSchemeId(SchemeName schemeName, SchemeIdRegisterRequestDto request) throws APIException {
        if (authenticateToken())
            return this.registerSchemeIds(schemeName, request);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public SchemeId registerSchemeIds(SchemeName schemeName, SchemeIdRegisterRequestDto request) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        SchemeId schemeIdRec = null;

        if (this.isAbleUser(schemeName, userObj)) {

            if (request.getSystemId() != null || request.getSystemId() == "") {
                request.setSystemId(schemeIdHelper.guid());
                request.setAutoSysId(true);
                if (!request.isAutoSysId()) {
                    schemeIdRec = getSchemeIdBySystemId(schemeName, request.getSystemId());
                    if (schemeIdRec != null) {
                        if (schemeIdRec.getSchemeId() != request.getSchemeId()) {
                            throw new APIException(HttpStatus.BAD_REQUEST, "SystemId" + request.getSystemId() + " already exists with SchemeId:" + schemeIdRec.getSchemeId());
                        }
                        if (Objects.equals(schemeIdRec.getStatus(), stateMachine.statuses.get("assigned"))) {
                            return schemeIdRec;
                        } else {
                            schemeIdRec = registerNewSchemeId(schemeName, request);
                        }
                    }
                } else {
                    schemeIdRec = registerNewSchemeId(schemeName, request);
                }
            }
            request.setAuthor(request.getAuthor());
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return schemeIdRec;
    }

    private SchemeId registerNewSchemeId(SchemeName schemeName, SchemeIdRegisterRequestDto request) throws APIException {
        SchemeId schemeIdrecord = getSchemeIdsByschemeId(schemeName, request.getSchemeId());
        SchemeId schemeId = new SchemeId();

        //
        if (schemeIdrecord.getSchemeId().isEmpty()) {
            throw new APIException(HttpStatus.NOT_FOUND, "SchemeId record is empty");
        } else {
            var newStatus = stateMachine.getNewStatus(schemeIdrecord.getStatus(), stateMachine.actions.get("register"));
            if (!newStatus.isBlank()) {
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
                schemeId=updateSchemeIdRecord(schemeIdrecord, schemeName);
            }else
            {
                throw new APIException(HttpStatus.BAD_REQUEST,"Cannot register SchemeId:" + request.getSchemeId() + ", current status:" + schemeIdrecord.getStatus());
            }
        }
        return schemeId;
    }


}
