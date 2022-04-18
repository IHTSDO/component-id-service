package com.snomed.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.snomed.api.controller.dto.*;
import com.snomed.api.domain.SchemeId;
import com.snomed.api.domain.SchemeIdBase;
import com.snomed.api.domain.SchemeName;
import com.snomed.api.domain.Sctid;
import com.snomed.api.exception.APIException;
import com.snomed.api.helper.*;
import com.snomed.api.repository.BulkSchemeIdRepository;
import com.snomed.api.repository.SchemeIdBaseRepository;
import com.snomed.api.repository.SchemeIdRepository;
import com.snomed.api.repository.SctidRepository;
import com.snomed.api.service.DM.SCTIdDM;
import com.snomed.api.service.DM.SchemeIdDM;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@Service
public class SctidService {
    @Autowired
    BulkSctidService bulkSctidService;

    @Autowired
    SchemeIdService schemeIdService;

    @Autowired
    SctidRepository sctidRepository;

    @Autowired
    SctIdHelper sctIdHelper;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    SchemeIdDM schemeIdDM;

    @Autowired
    SCTIdDM sctIdDM;

    @Autowired
    StateMachine stateMachine;

    @Autowired
    SchemeIdRepository schemeIdRepository;

    @Autowired
    static
    private BulkSchemeIdRepository bulkSchemeIdRepository;

    @Autowired
    private SchemeIdBaseRepository schemeIdBaseRepository;

    public Sctid getTestSct(String sctId)
    {
        return sctidRepository.findById(sctId).get();
    }

    public List<Sctid> findSctWithIndexAndLimit(Map<String, Object> queryObject, String limit, String skip) {
        List<Sctid> sctList;
        var limitR = 100;
        var skipTo = 0;
        if (!limit.isEmpty() && null != limit)
            limitR = Integer.parseInt(limit);
        if (!skip.isEmpty() && null != skip)
            skipTo = Integer.parseInt(skip);
        //sctidRepository.findSct(queryObject,limitR,skipTo);
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
            //sql = "SELECT * FROM sctId" + swhere + " order by sctid limit " + limit;

            sql = "Select * FROM sctid USE INDEX (nam_par_st)" + swhere + " order by sctid limit " + limit;
        } else {
            //sql = "SELECT * FROM sctId" + swhere + " order by sctid";
            sql = "Select * FROM sctid USE INDEX (nam_par_st)" + swhere + " order by sctid";
        }
        Query genQuery = entityManager.createNativeQuery(sql,Sctid.class);
        System.out.println("genQuery:"+genQuery);
        List<Sctid> resultList = genQuery.getResultList();
        if ((skipTo == 0)) {
            sctList = resultList;
        } else {
            var cont = 1;
            List<Sctid> newRows = new ArrayList<>();
            for (var i = 0; i < resultList.size(); i++) {
                if (i >= skipTo) {
                    if (null != limit && limitR > 0 && limitR < cont) {
                        break;
                    }
                    newRows.add(resultList.get(i));
                    cont++;
                }
            }
            sctList = newRows;
        }
        return sctList;
    }

    public List<SchemeId> getSchemeIds(String systemId, String limit, String skip)
    {

        List<SchemeId> schemeList;
        String objQuery = "";
        var limitR = 100;
        var skipTo = 0;
        if (null!=limit && !limit.isEmpty())
            limitR = Integer.parseInt(limit);
        if (null!=skip && !skip.isEmpty())
            skipTo = Integer.parseInt(skip);
        String swhere ="";
        if (!systemId.isEmpty() && null!=systemId) {
            objQuery = systemId;
            swhere += " And " + "systemId" + "=" +"'"+ (objQuery)+"'";
        }
        if (swhere!=""){
            swhere = " WHERE " + swhere.substring(5);
        }
        String sql;
        if (limitR>0 && (skipTo==0)) {
            sql = "SELECT * FROM schemeid" + swhere + " order by schemeId limit " + limit;
        }else{
            sql = "SELECT * FROM schemeid" + swhere + " order by schemeId";
        }
        Query genQuery = entityManager.createNativeQuery(sql,SchemeId.class);
        List<SchemeId> resultList = genQuery.getResultList();
        if ((skipTo==0)) {
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
    }

    public List<Sctid> getSct(String token, String limit, String skip, String namespace) throws APIException, JsonProcessingException {
        List<Sctid> sctList = new ArrayList<>();
//List<Sctid> sc = new ArrayList<>();
        if (bulkSctidService.authenticateToken(token)) {
            UserDTO userObj = bulkSctidService.getAuthenticatedUser();
            if (bulkSctidService.isAbleUser("false", userObj)) {
                Map<String, Object> queryObject = new HashMap();
                if (!namespace.isEmpty() && null != namespace) {
                    queryObject.put("namespace", namespace);
                }
                sctList = this.findSctWithIndexAndLimit(queryObject, limit, skip);
                /*Sctid sct = (Sctid) sctList.get(0);
                sct.setSctid(sctList.get(0).getSctid());
                sct.setAuthor(sctList.get(0).getAuthor());
                sct.setComment(sctList.get(0).getComment());
                sct.setSequence(sctList.get(0).getSequence());
                sct.setPartitionId(sctList.get(0).getPartitionId());
                sct.setCheckDigit(sctList.get(0).getCheckDigit());
                sct.setSystemId(sctList.get(0).getSystemId());
                sct.setStatus(sctList.get(0).getStatus());
                sct.setSoftware(sctList.get(0).getSoftware());
                sct.setExpirationDate(sctList.get(0).getExpirationDate());
                sct.setJobId(sctList.get(0).getJobId());
                sct.setCreated_at(sctList.get(0).getCreated_at());
                sct.setModified_at(sctList.get(0).getModified_at());*/

                //ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                //String json = ow.writeValueAsString(sct);
                //sc.add(sct);
            } else {
                throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return sctList;
    }

    public SctWithSchemeResponseDTO getSctWithId(String token, String sctid, String includeAdditionalIds) throws APIException {
        Sctid sctResult = new Sctid();
        SctWithSchemeResponseDTO output = new SctWithSchemeResponseDTO();
        SctWithSchemeResponseDTO sctWithSchemeResponseDTO = new SctWithSchemeResponseDTO();
        if (bulkSctidService.authenticateToken(token)) {
            UserDTO userObj = bulkSctidService.getAuthenticatedUser();
            var namespace = sctIdHelper.getNamespace(sctid);
            if (null != namespace) {
                if (bulkSctidService.isAbleUser(String.valueOf(namespace), userObj)) {
                    sctWithSchemeResponseDTO = this.getSctCommon(output, sctid, includeAdditionalIds);
                } else {
                    throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
                }
            } else {
                sctWithSchemeResponseDTO = this.getSctCommon(output, sctid, includeAdditionalIds);
            }
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return sctWithSchemeResponseDTO;
    }

    public SctWithSchemeResponseDTO getSctCommon(SctWithSchemeResponseDTO output, String sctid, String includeAdditionalIds) throws APIException {
        if (sctIdHelper.validSCTId(sctid)) {
            Sctid sctRec = sctidRepository.getSctidsById(sctid);
            List<SchemeId> respSchemeList = new ArrayList<>();
            Sctid newSct = new Sctid();
            if (null != sctRec) {
                newSct = sctRec;
            }
            else
            {
                newSct = sctIdDM.getFreeRecord(sctid);
            }
                output.setSctid(newSct.getSctid());
                output.setSequence(newSct.getSequence());
                output.setNamespace(newSct.getNamespace());
                output.setPartitionId(newSct.getPartitionId());
                output.setCheckDigit(newSct.getCheckDigit());
                output.setSystemId(newSct.getSystemId());
                output.setStatus(newSct.getStatus());
                output.setAuthor(newSct.getAuthor());
                output.setSoftware(newSct.getSoftware());
                output.setExpirationDate(newSct.getExpirationDate());
                output.setComment(newSct.getComment());
                if (!includeAdditionalIds.isEmpty() && includeAdditionalIds.equalsIgnoreCase("true")) {
                    List<SchemeId> schemeResult = getSchemeIds(newSct.getSystemId(), "10", "0");
                   /* for (SchemeId res :
                            schemeResult) {
                        SchemeIdResponseDTO schemeResp = new SchemeIdResponseDTO();
                        schemeResp.setSchemeId(res.getSchemeId());
                        schemeResp.setScheme(res.getScheme());
                        schemeResp.setSequence(res.getSequence());
                        schemeResp.setCheckDigit(res.getCheckDigit());
                        schemeResp.setSystemId(res.getSystemId());
                        schemeResp.setStatus(res.getStatus());
                        schemeResp.setAuthor(res.getAuthor());
                        schemeResp.setSoftware(res.getSoftware());
                        schemeResp.setExpirationDate(res.getExpirationDate());
                        schemeResp.setComment(res.getComment());
                        respSchemeList.add(res);
                    }*/
                    output.setAdditionalIds(schemeResult);
                }

            //}
            else {
                throw new APIException(HttpStatus.ACCEPTED, "No SctRecords Returned.");
            }
            return output;
        } else {
            throw new APIException(HttpStatus.BAD_REQUEST, "Not valid SCTID.");
        }
    }

    public CheckSctidResponseDTO checkSctid(String sctid) throws APIException {
        return SctIdHelper.checkSctid(sctid);
    }

    public Sctid getSctWithSystemId(String token, Integer namespaceId, String systemId) throws APIException {
      //  SctWithSchemeResponseDTO output = new SctWithSchemeResponseDTO();
        Sctid sct = new Sctid();
        if (bulkSctidService.authenticateToken(token)) {
            UserDTO userObj = bulkSctidService.getAuthenticatedUser();
            if (bulkSctidService.isAbleUser(String.valueOf(namespaceId), userObj)) {
                List<Sctid> result = sctidRepository.findBySystemIdAndNamespace(systemId, namespaceId);
                sct = result.get(0);
                /*output.setSctid(sct.getSctid());
                output.setSequence(sct.getSequence());
                output.setNamespace(sct.getNamespace());
                output.setPartitionId(sct.getPartitionId());
                output.setCheckDigit(sct.getCheckDigit());
                output.setSystemId(sct.getSystemId());
                output.setStatus(sct.getStatus());
                output.setAuthor(sct.getAuthor());
                output.setSoftware(sct.getSoftware());
                output.setExpirationDate(sct.getExpirationDate());
                output.setComment(sct.getComment());*/
            } else {
                throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return sct;
    }

    public Sctid deprecateSct(String token, DeprecateSctRequestDTO request) throws APIException {
        Sctid output = new Sctid();
        if (bulkSctidService.authenticateToken(token)) {
            UserDTO userObj = bulkSctidService.getAuthenticatedUser();
            Integer returnedNamespace = sctIdHelper.getNamespace(request.getSctid());
            if (returnedNamespace != request.getNamespace()) {
                throw new APIException(HttpStatus.ACCEPTED, "Namespaces differences between sctId and parameter");
            } else {
                if (bulkSctidService.isAbleUser(String.valueOf(returnedNamespace), userObj)) {
                    request.setAuthor(userObj.getLogin());
                    Sctid sctRec = sctIdHelper.getSctid(request.getSctid());
                    if (sctRec.getSctid().isEmpty()) {
                        throw new APIException(HttpStatus.ACCEPTED, "No Sctid Rec Found");
                    } else {
                        var newStatus = stateMachine.getNewStatus(sctRec.getStatus(), stateMachine.actions.get("deprecate"));
                        if (null!=newStatus) {
                            sctRec.setStatus(newStatus);
                            sctRec.setAuthor(request.getAuthor());
                            sctRec.setSoftware(request.getSoftware());
                            sctRec.setComment(request.getComment());
                            sctRec.setJobId(null);
                            output = sctidRepository.save(sctRec);
                        } else {
                            throw new APIException(HttpStatus.ACCEPTED, "Cannot deprecate SCTID:" + request.getSctid() + ", current status: " + sctRec.getStatus());
                        }
                    }

                } else {
                    throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
                }
            }
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return output;
    }

    public Sctid releaseSct(String token, DeprecateSctRequestDTO request) throws APIException {
        Sctid output = new Sctid();
        if (bulkSctidService.authenticateToken(token)) {
            UserDTO userObj = bulkSctidService.getAuthenticatedUser();
            Integer returnedNamespace = sctIdHelper.getNamespace(request.getSctid());
            if (returnedNamespace != request.getNamespace()) {
                throw new APIException(HttpStatus.ACCEPTED, "Namespaces differences between sctId and parameter");
            } else {
                if (bulkSctidService.isAbleUser(String.valueOf(returnedNamespace), userObj)) {
                    request.setAuthor(userObj.getLogin());
                    Sctid sctRec = sctIdHelper.getSctid(request.getSctid());
                    if (sctRec.getSctid().isEmpty()) {
                        throw new APIException(HttpStatus.ACCEPTED, "No Sctid Rec Found");
                    } else {
                        var newStatus = stateMachine.getNewStatus(sctRec.getStatus(), stateMachine.actions.get("release"));
                        if (null !=newStatus) {
                            sctRec.setStatus(newStatus);
                            sctRec.setAuthor(request.getAuthor());
                            sctRec.setSoftware(request.getSoftware());
                            sctRec.setComment(request.getComment());
                            sctRec.setJobId(Integer.valueOf("null"));
                            output = sctidRepository.save(sctRec);
                        } else {
                            throw new APIException(HttpStatus.ACCEPTED, "Cannot release SCTID:" + request.getSctid() + ", current status: " + sctRec.getStatus());
                        }
                    }

                } else {
                    throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
                }
            }
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return output;
    }

    public Sctid publishSct(String token, DeprecateSctRequestDTO request) throws APIException {
        Sctid output = new Sctid();
        if (bulkSctidService.authenticateToken(token)) {
            UserDTO userObj = bulkSctidService.getAuthenticatedUser();
            Integer returnedNamespace = sctIdHelper.getNamespace(request.getSctid());
            if (returnedNamespace != request.getNamespace()) {
                throw new APIException(HttpStatus.ACCEPTED, "Namespaces differences between sctId and parameter");
            } else {
                if (bulkSctidService.isAbleUser(String.valueOf(returnedNamespace), userObj)) {
                    request.setAuthor(userObj.getLogin());
                    Sctid sctRec = sctIdHelper.getSctid(request.getSctid());
                    if (sctRec.getSctid().isEmpty()) {
                        throw new APIException(HttpStatus.ACCEPTED, "No Sctid Rec Found");
                    } else {
                        var newStatus = stateMachine.getNewStatus(sctRec.getStatus(), stateMachine.actions.get("publish"));
                        if (null!=newStatus) {
                            sctRec.setStatus(newStatus);
                            sctRec.setAuthor(request.getAuthor());
                            sctRec.setSoftware(request.getSoftware());
                            sctRec.setComment(request.getComment());
                            sctRec.setJobId(Integer.valueOf("null"));
                            output = sctidRepository.save(sctRec);
                        } else {
                            throw new APIException(HttpStatus.ACCEPTED, "Cannot publish SCTID:" + request.getSctid() + ", current status: " + sctRec.getStatus());
                        }
                    }

                } else {
                    throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
                }
            }
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return output;
    }

    public SctWithSchemeResponseDTO generateSctid(String token, SctidsGenerateRequestDto generationData) throws APIException {
        SctWithSchemeResponseDTO output = new SctWithSchemeResponseDTO();
        if (bulkSctidService.authenticateToken(token)) {
            UserDTO userObj = bulkSctidService.getAuthenticatedUser();
            if (bulkSctidService.isAbleUser((generationData.getNamespace()).toString(), userObj)) {
                if ((generationData.getNamespace() == 0 && generationData.getPartitionId().substring(0, 1) != "0")
                        || (generationData.getNamespace() != 0 && generationData.getPartitionId().substring(0, 1) != "1")) {
                    throw new APIException(HttpStatus.ACCEPTED, "Namespace and partitionId parameters are not consistent.");
                }
                if (generationData.getSystemId().isBlank()) {
                    generationData.setSystemId(sctIdHelper.guid());
                    generationData.setAutoSysId(true);
                }
                generationData.setAuthor(userObj.getLogin());
                Sctid sctRec1 = this.generateSctidSubFun(generationData);
                var sctIdRecordArray = new ArrayList<>();
                if (generationData.isGenerateLegacyIds() &&
                        generationData.getPartitionId().substring(1, 1) == "0") {
                    if (bulkSctidService.isSchemeAbleUser(SchemeName.CTV3ID.schemeName, userObj)) {
                        SchemeId schemeId = this.generateSchemeId(SchemeName.valueOf(SchemeName.CTV3ID.schemeName), generationData);
                    }
                }
            } else {
                throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return output;
    }

    public Sctid generateSctidSubFun(SctidsGenerateRequestDto generationData) throws APIException {
        Sctid sctOut = new Sctid();
        if (!generationData.isAutoSysId()) {
            List<Sctid> sctids = sctidRepository.findBySystemIdAndNamespace(generationData.getSystemId(), generationData.getNamespace());
            if (null != sctids && sctids.size() > 0) {
                sctOut = sctids.get(0);
            } else if (sctids.size() == 0) {
                sctOut = this.setNewSCTIdRecord(generationData, stateMachine.actions.get("generate"));
            }
        } else {
            sctOut = this.setNewSCTIdRecord(generationData, stateMachine.actions.get("generate"));
        }
        return sctOut;
    }

    public Sctid setNewSCTIdRecord(SctidsGenerateRequestDto generationData, String action) throws APIException {
        Sctid sctOutput = new Sctid();
        sctOutput = this.setAvailableSCTIDRecord2NewStatus(generationData, action);
        return sctOutput;
    }

    public Sctid setAvailableSCTIDRecord2NewStatus(SctidsGenerateRequestDto generationData, String action) throws APIException {
        Sctid sctOutput = new Sctid();
        List<Sctid> sctList = new ArrayList<>();
        Map<String, Object> queryObject = new HashMap<>();
        if (null != generationData.getNamespace() && !generationData.getPartitionId().isBlank()) {
            queryObject.put("namespace", generationData.getNamespace());
            queryObject.put("partitionId", generationData.getPartitionId());
            queryObject.put("status", stateMachine.statuses.get("available"));
            sctList = this.findSctWithIndexAndLimit(queryObject, "1", null);
            if (sctList.size() > 0) {
                var newStatus = stateMachine.getNewStatus(sctList.get(0).getStatus(), action);
                if (!newStatus.isBlank()) {
                    if (null != generationData.getSystemId() && generationData.getSystemId().trim() != "") {
                        sctList.get(0).setSystemId(generationData.getSystemId());
                    }
                    sctList.get(0).setStatus(newStatus);
                    sctList.get(0).setAuthor(generationData.getAuthor());
                    sctList.get(0).setSoftware(generationData.getSoftware());
                    //Doubt - need to be clarified- there is no ExpirationDate in Request body.
                    sctList.get(0).setExpirationDate(new Date());
                    sctList.get(0).setComment(generationData.getComment());
                    sctList.get(0).setJobId(Integer.valueOf("null"));
                    sctList.get(0).setModified_at(new Date());
                    sctOutput = sctidRepository.save(sctList.get(0));
                } else {
                   // sctIdDM.counterMode(generationData, action);
                }
            } else {
                //throw new APIException(HttpStatus.ACCEPTED,"error getting available partitionId:" + generationData.getPartitionId() + " and namespace:" + generationData.getNamespace() + ", err: ");
                //sctIdDM.counterMode(generationData, action);
            }
        } else {
            throw new APIException(HttpStatus.BAD_REQUEST, "Request Cannot be Empty");
        }
        return sctOutput;
    }
    public Sctid setAvailableSCTIDRecord2NewStatus(SCTIDReservationRequest sctidReservationRequest, String action) throws APIException {
        Sctid result = null;
        List<Sctid> sctIdRecords = null;
        Map<String, Object> queryObject = new HashMap<>();
        if (null != sctidReservationRequest.getNamespace() && !sctidReservationRequest.getPartitionId().isBlank()) {
            queryObject.put("namespace", sctidReservationRequest.getNamespace());
            queryObject.put("partitionId", sctidReservationRequest.getPartitionId());
            queryObject.put("status", stateMachine.statuses.get("available"));
            sctIdRecords = this.findSctWithIndexAndLimit(queryObject, "1", null);
            if (!sctIdRecords.isEmpty() && sctIdRecords.size() > 0) {
                var newStatus = stateMachine.getNewStatus(sctIdRecords.get(0).getStatus(), action);
                if (null != newStatus) {

                    sctIdRecords.get(0).setStatus(newStatus);
                    Sctid updatedRecord = sctidRepository.save(sctIdRecords.get(0));
                    result = updatedRecord;
                } else {
                    result = sctIdDM.counterMode(sctidReservationRequest, action);
                }
            }
        }
        return result;
    }
    public SchemeId generateSchemeId(SchemeName scheme, SctidsGenerateRequestDto generationData) throws APIException {
        SchemeId schemeId = new SchemeId();
        List<SchemeId> schemeList = new ArrayList<>();
        if(!generationData.isAutoSysId())
        {
            schemeList = schemeIdRepository.findBySchemeAndSystemId(scheme,generationData.getSystemId());
            if(schemeList.size()>0)
                schemeId = schemeList.get(0);
            else
            {
                schemeId = setNewSchemeIdRecord(scheme, generationData, stateMachine.actions.get("generate"));
            }
        }
        else
        {
            schemeId  = setNewSchemeIdRecord(scheme, generationData, stateMachine.actions.get("generate"));
        }
        return schemeId;
    }

    public SchemeId setNewSchemeIdRecord(SchemeName scheme,SctidsGenerateRequestDto generationData, String action) throws APIException {
        SchemeId schemeId;
        schemeId = this.setAvailableSchemeIdRecord2NewStatus(scheme,generationData,action);
        if(null!=schemeId)
        {
            return schemeId;
        }
        else
        {
           return counterMode(scheme,generationData,action);
        }
    }

    public SchemeId setAvailableSchemeIdRecord2NewStatus(SchemeName scheme,SctidsGenerateRequestDto generationData,String action) throws APIException {
        List<SchemeId> schemeIdRecords = new ArrayList<>();
        SchemeId outputSchemeRec = new SchemeId();
        Map<String, Object> queryObject = new HashMap();
        if (!scheme.toString().isEmpty() && null != scheme) {
            queryObject.put("scheme", scheme);
            queryObject.put("status",stateMachine.statuses.get("available"));
        }
        schemeIdRecords = this.findSchemeWithIndexAndLimit(queryObject, "1", null);
        if(schemeIdRecords.size()>0) {
            var newStatus = stateMachine.getNewStatus(schemeIdRecords.get(0).getStatus(), action);
            if (!newStatus.isBlank()) {

                if (null != generationData.getSystemId() && generationData.getSystemId().trim() != "") {
                    schemeIdRecords.get(0).setSystemId(generationData.getSystemId());
                }
                schemeIdRecords.get(0).setStatus(newStatus);
                schemeIdRecords.get(0).setAuthor(generationData.getAuthor());
                schemeIdRecords.get(0).setSoftware(generationData.getSoftware());
                //Expiration Date Not available in request.
                schemeIdRecords.get(0).setExpirationDate(null);
                schemeIdRecords.get(0).setComment(generationData.getComment());
                schemeIdRecords.get(0).setJobId(Integer.parseInt("null"));
                outputSchemeRec = schemeIdRepository.save(schemeIdRecords.get(0));
            } else {
                counterMode(scheme, generationData, action);
            }
        }
            else
            {
                throw new APIException(HttpStatus.ACCEPTED,"error getting available schemeId for:" + scheme  + ", err: " );
            }
        return outputSchemeRec;
    }

   public List<SchemeId> findSchemeWithIndexAndLimit(Map<String, Object> queryObject, String limit, String skip) {
        List<SchemeId> schemeList;
        var limitR = 100;
        var skipTo = 0;
        if (!limit.isEmpty() && null != limit)
            limitR = Integer.parseInt(limit);
        if (!skip.isEmpty() && null != skip)
            skipTo = Integer.parseInt(skip);
        //sctidRepository.findSct(queryObject,limitR,skipTo);
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
            //sql = "SELECT * FROM sctId" + swhere + " order by sctid limit " + limit;

            sql = "SELECT * FROM schemeId" + swhere + " order by schemeId limit " + limit;
        } else {
            //sql = "SELECT * FROM sctId" + swhere + " order by sctid";
            sql = "SELECT * FROM schemeId" + swhere + " order by schemeId";
        }
        Query genQuery = entityManager.createQuery(sql);
        List<SchemeId> resultList = genQuery.getResultList();
        if ((skipTo == 0)) {
            schemeList = resultList;
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
            schemeList = newRows;
        }
        return schemeList;
    }
    public SchemeId counterMode(SchemeName schemeName, SctidsGenerateRequestDto request, String reserve) throws APIException {
        SchemeId newSchemeId = getNextSchemeId(schemeName,  request);
        if (newSchemeId != null) {
            SchemeId schemeIdRecord = schemeIdService.getSchemeIdsByschemeIdList(schemeName, newSchemeId.toString());
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
                    updatedrecord = schemeIdService.updateSchemeIdRecord(schemeIdRecord, schemeName);
                    return updatedrecord;
                } else {
                    counterMode(schemeName, request, reserve);
                }
            }
        }
        return newSchemeId;
    }


    private SchemeId getNextSchemeId(SchemeName schemeName, SctidsGenerateRequestDto request) {
        List<SchemeIdBase> schemeIdBaseList = schemeIdBaseRepository.findByScheme(schemeName.toString());
        SchemeIdBase schemeIdBase = null;
        schemeIdBase.setIdBase(schemeIdBaseList.get(0).getIdBase());
        schemeIdBaseRepository.save(schemeIdBase);
        return null;//List<SchmeId>
    }
    public Sctid registerSctid(String token,SCTIDRegistrationRequest registrationData) throws APIException {
Sctid result = new Sctid();
        if (bulkSctidService.authenticateToken(token)) {
            UserDTO userObj = bulkSctidService.getAuthenticatedUser();
            Integer returnedNamespace = sctIdHelper.getNamespace(registrationData.getSctid());
            if (returnedNamespace != registrationData.getNamespace()) {
                throw new APIException(HttpStatus.ACCEPTED, "Namespaces differences between sctId and parameter");
            } else {
                if (bulkSctidService.isAbleUser((registrationData.getNamespace()).toString(), userObj)) {
                    if (registrationData.getSystemId().isBlank() || registrationData.getSystemId().isEmpty()){
                        registrationData.setAutoSysId(true);
                    }
                    registrationData.setAuthor(userObj.getLogin());
                    Sctid sct = sctIdDM.registerSctid(registrationData,"SCTIDRegistrationRequest");
                    if(null!= (sct))
                        result = sct;
                } else {
                    throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
                }
            }
        }
        else
        {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
return result;
    }

    public Sctid reserveSctid(String token, SCTIDReservationRequest reservationData) throws APIException {
Sctid result = null;
        if (bulkSctidService.authenticateToken(token)) {
            UserDTO userObj = bulkSctidService.getAuthenticatedUser();
                if (bulkSctidService.isAbleUser((reservationData.getNamespace()).toString(), userObj)) {
                    if ((reservationData.getNamespace()==0 && reservationData.getPartitionId().substring(0,1)!="0")
                            || (reservationData.getNamespace()!=0 && reservationData.getPartitionId().substring(0,1)!="1")){
                        throw new APIException(HttpStatus.ACCEPTED,("Namespace and partitionId parameters are not consistent."));
                    }
                    reservationData.setAuthor(userObj.getLogin());
                    Sctid sct = this.reserveSctid(reservationData);
                    if(null!= (sct))
                        result = sct;
                } else {
                    throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
                }
        }
        else
        {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
return result;
    }
    public Sctid reserveSctid(SCTIDReservationRequest sctidReservationRequest) throws APIException {
        Sctid result = null;
        result = this.setAvailableSCTIDRecord2NewStatus(sctidReservationRequest, stateMachine.actions.get("reserve"));
        return result;
    }

}

