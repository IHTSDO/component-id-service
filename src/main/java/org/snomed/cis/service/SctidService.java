package org.snomed.cis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.controller.SecurityController;
import org.snomed.cis.domain.*;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.pojo.Config;
import org.snomed.cis.repository.BulkSchemeIdRepository;
import org.snomed.cis.repository.NamespaceRepository;
import org.snomed.cis.repository.SchemeIdBaseRepository;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.service.DM.SCTIdDM;
import org.snomed.cis.service.DM.SchemeIdDM;
import org.snomed.cis.util.SctIdHelper;
import org.snomed.cis.util.StateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.snomed.cis.util.CTV3ID;
import org.snomed.cis.util.SNOMEDID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.print.DocFlavor;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SctidService {
    private final Logger logger = LoggerFactory.getLogger(SctidService.class);
    @Autowired
    BulkSctidService bulkSctidService;

    @Autowired
    SchemeIdService schemeIdService;

    @Autowired
    SecurityController securityController;

    @Autowired
    SctidRepository sctidRepository;

    @Autowired
    SctIdHelper sctIdHelper;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    SchemeIdDM schemeIdDM;

    @Autowired
    BulkSchemeIdRepository schemeIdRepository;

    @Autowired
    SCTIdDM sctIdDM;

    @Autowired
    StateMachine stateMachine;

    @Autowired
    NamespaceRepository namespaceRepository;

    @Autowired
    private HttpServletRequest servReq;

    @Autowired
    static
    private BulkSchemeIdRepository bulkSchemeIdRepository;

    @Autowired
    private Config config;

    @Autowired
    private SchemeIdBaseRepository schemeIdBaseRepository;

    public List<SchemeId> getSchemeIds(String systemId, String limit, String skip) {
        logger.debug("Request Received : systemId-{} :: limit - {} :: skip - {} ", systemId, limit, skip);
        List<SchemeId> schemeList;
        String objQuery = "";
        var limitR = 100;
        var skipTo = 0;
        if (null != limit && !limit.isEmpty())
            limitR = Integer.parseInt(limit);
        if (null != skip && !skip.isEmpty())
            skipTo = Integer.parseInt(skip);
        //String swhere = "";
        StringBuffer swhere = new StringBuffer("");
        if (!systemId.isEmpty() && null != systemId) {
            objQuery = systemId;
            //swhere += " And " + "systemId" + "=" + "'" + (objQuery) + "'";
            swhere = swhere.append(" And ").append("systemId").append("=").append("'").append((objQuery)).append("'");
        }
        if (!(swhere.toString().equalsIgnoreCase(""))) {
            //swhere = " WHERE " + swhere.substring(5);
            String subValue = swhere.substring(5);
            swhere = swhere.append(" WHERE ").append(subValue);
        }
        String sql;
        if (limitR > 0 && (skipTo == 0)) {
            sql = "SELECT * FROM schemeid" + swhere + " order by schemeId limit " + limit;
        } else {
            sql = "SELECT * FROM schemeid" + swhere + " order by schemeId";
        }
        Query genQuery = entityManager.createNativeQuery(sql, SchemeId.class);
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

    public List<Sctid> getSct(AuthenticateResponseDto authToken, String limit, String skip, String namespace) throws CisException {
        logger.debug("Request Received : authToken-{} :: limit - {} :: skip - {} ::  namespace - {}", authToken, limit, skip, namespace);
        List<Sctid> sctList = new ArrayList<>();

        if (bulkSctidService.isAbleUser("false", authToken)) {
            Map<String, Object> queryObject = new HashMap();
            if (null != namespace) {
                queryObject.put("namespace", namespace);
            }
            if (null != limit && null != skip)
                sctList = findSctWithIndexAndLimit(queryObject, limit, skip);
            else
                sctList = findSctWithIndexAndLimit(queryObject, null, null);
            if (sctList.size() > 0) {
                logger.info("getSct - Response :: {}", sctList);
                return sctList;
            } else {
                logger.info("getSct - Response :: {}", Collections.EMPTY_LIST);
                return Collections.EMPTY_LIST;
            }
        } else {
            logger.error("error getSct():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }

    }

    public SctWithSchemeResponseDTO getSctWithId(AuthenticateResponseDto authToken, String sctid, String includeAdditionalIds) throws CisException {
        logger.debug("Request Received :authToken - {} :: sctid-{} :: includeAdditionalIds - {} ", authToken, sctid, includeAdditionalIds);
        Sctid sctResult = new Sctid();
        SctWithSchemeResponseDTO output = new SctWithSchemeResponseDTO();
        SctWithSchemeResponseDTO sctWithSchemeResponseDTO = new SctWithSchemeResponseDTO();

        var namespace = sctIdHelper.getNamespace(sctid);
        if (null != namespace) {
            if (bulkSctidService.isAbleUser(String.valueOf(namespace), authToken)) {
                sctWithSchemeResponseDTO = this.getSctCommon(output, sctid, includeAdditionalIds);
            } else {
                logger.error("error getSctWithId():: No permission for the selected operation");
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        } else {
            sctWithSchemeResponseDTO = this.getSctCommon(output, sctid, includeAdditionalIds);
        }
        logger.info("getSctWithId() - Response :: {}", sctWithSchemeResponseDTO);
        return sctWithSchemeResponseDTO;
    }

    public List<Sctid> findSctWithIndexAndLimit(Map<String, Object> queryObject, String limit, String skip) {
        logger.debug("Request Received : queryObject-{} :: limit - {} :: skip - {} ", queryObject, limit, skip);
        List<Sctid> sctList;
        var limitR = 100;
        var skipTo = 0;
        if (null != limit)
            limitR = Integer.parseInt(limit);
        if (null != skip)
            skipTo = Integer.parseInt(skip);

       // String swhere = "";
        StringBuffer swhere = new StringBuffer("");
        if (queryObject.size() > 0) {
            for (var query :
                    queryObject.entrySet()) {
               // swhere += " And " + query.getKey() + "=" + (query.getValue());
                swhere = swhere.append(" And ").append(query.getKey()).append("=").append(query.getValue());
            }
        }
        if (!(swhere.toString().equalsIgnoreCase(""))) {
            String subvalue = swhere.substring(5);
            //swhere = " WHERE " + swhere.substring(5);
            swhere = swhere.append(" WHERE ").append(subvalue);
        }
        String sql;
        if ((limitR > 0) && (skipTo == 0)) {

            if (swhere.toString() != "")
                sql = "Select * FROM sctid USE INDEX (nam_par_st)" + swhere + " order by sctid limit " + limitR;
            else
                sql = "Select * FROM sctid " + swhere + " order by sctid limit " + limitR;
        } else {

            if (swhere.toString() != "")
                sql = "Select * FROM sctid USE INDEX (nam_par_st)" + swhere + " order by sctid";
            else
                sql = "Select * FROM sctid " + swhere + " order by sctid";
        }
        Query genQuery = entityManager.createNativeQuery(sql, Sctid.class);
        List<Sctid> resultList = genQuery.getResultList();
        if ((skipTo == 0)) {
            sctList = resultList;
        } else {
            var cont = 1;
            List<Sctid> newRows = new ArrayList<>();
            for (var i = 0; i < resultList.size(); i++) {
                if (i >= skipTo) {
                    if ((limitR > 0) && (limitR < cont)) {
                        break;
                    }
                    newRows.add(resultList.get(i));
                    cont++;
                }
            }
            sctList = newRows;
        }
        logger.info("findSctWithIndexAndLimit() - Response :: {}", sctList);
        return sctList;
    }

    public SctWithSchemeResponseDTO getSctCommon(SctWithSchemeResponseDTO output, String sctid, String includeAdditionalIds) throws CisException {
        logger.debug("Request Received : SctWithSchemeResponseDTO-{} :: sctid - {} :: includeAdditionalIds - {} ", output, sctid, includeAdditionalIds);
        if (sctIdHelper.validSCTId(sctid)) {


            Sctid sctRec = ((sctidRepository.findById(sctid)).isPresent()) ? ((sctidRepository.findById(sctid))).get() : null;

            List<SchemeId> respSchemeList = new ArrayList<>();
            Sctid newSct = new Sctid();
            if (null != sctRec) {
                newSct = sctRec;
            } else {
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
                output.setAdditionalIds(schemeResult);
                return output;
            } else {
                logger.info("getSctCommon() - Response :: {}", output);
                return output;
            }
        } else {
            logger.error("error getSctCommon():: Not valid SCTID {}", sctid);
            throw new CisException(HttpStatus.BAD_REQUEST, "Not valid SCTID.");
        }
    }

    public CheckSctidResponseDTO checkSctid(String sctid) throws CisException {
        CheckSctidResponseDTO checkSctid = SctIdHelper.checkSctid(sctid);
        logger.info("checkSctid() - Response :: {}", checkSctid);
        return checkSctid;
    }

    public Sctid getSctWithSystemId(AuthenticateResponseDto authToken, Integer namespaceId, String systemId) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto-{} :: namespaceId - {} :: systemId - {}", authToken, namespaceId, systemId);
        Sctid sct = new Sctid();
        if (bulkSctidService.isAbleUser(String.valueOf(namespaceId), authToken)) {
            List<Sctid> result = sctidRepository.findBySystemIdAndNamespace(systemId, namespaceId);
            sct = result.get(0);
        } else {
            logger.error("error getSctWithSystemId():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
        logger.info("getSctWithSystemId() - Response :: {}", sct);
        return sct;
    }

    public Sctid deprecateSct(AuthenticateResponseDto authToken, DeprecateSctRequestDTO request) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto-{} :: DeprecateSctRequestDTO - {} ", authToken, request);
        Sctid output = new Sctid();
        DeprecateSctRequest deprecateSctRequest = new DeprecateSctRequest();
        deprecateSctRequest.setSctid(request.getSctid());
        deprecateSctRequest.setNamespace(request.getNamespace());
        deprecateSctRequest.setSoftware(request.getSoftware());
        deprecateSctRequest.setComment(request.getComment());

        Integer returnedNamespace = sctIdHelper.getNamespace(request.getSctid());
        if (!returnedNamespace.equals(request.getNamespace())) {
            logger.error("error deprecateSct():: Namespaces differences between sctId and parameter");
            throw new CisException(HttpStatus.BAD_REQUEST, "Namespaces differences between sctId and parameter");
        } else {
            if (bulkSctidService.isAbleUser(String.valueOf(returnedNamespace), authToken)) {
                deprecateSctRequest.setAuthor(authToken.getName());
                Sctid sctRec = sctIdHelper.getSctid(deprecateSctRequest.getSctid());
                if (sctRec.getSctid().isEmpty()) {
                    logger.error("error deprecateSct():: No Sctid Rec Found");
                    throw new CisException(HttpStatus.ACCEPTED, "No Sctid Rec Found");
                } else {
                    var newStatus = stateMachine.getNewStatus(sctRec.getStatus(), stateMachine.actions.get("deprecate"));
                    if (null != newStatus) {
                        sctRec.setStatus(newStatus);
                        sctRec.setAuthor(deprecateSctRequest.getAuthor());
                        sctRec.setSoftware(deprecateSctRequest.getSoftware());
                        sctRec.setComment(deprecateSctRequest.getComment());
                        sctRec.setJobId(null);
                        output = sctidRepository.save(sctRec);
                    } else {
                        logger.error("error deprecateSct():: Cannot deprecate SCTID:{}, current status: {}", request.getSctid(), sctRec.getStatus());
                        throw new CisException(HttpStatus.BAD_REQUEST, "Cannot deprecate SCTID:" + request.getSctid() + ", current status: " + sctRec.getStatus());
                    }
                }

            } else {
                logger.error("error deprecateSct():: No permission for the selected operation");
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        }
        logger.info("deprecateSct() - Response :: {}", output);
        return output;
    }

    public Sctid releaseSct(AuthenticateResponseDto authToken, DeprecateSctRequestDTO request) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto-{} :: DeprecateSctRequestDTO - {} ", authToken, request);
        Sctid output = new Sctid();
        DeprecateSctRequest deprecateSctRequest = new DeprecateSctRequest();
        deprecateSctRequest.setSctid(request.getSctid());
        deprecateSctRequest.setNamespace(request.getNamespace());
        deprecateSctRequest.setSoftware(request.getSoftware());
        deprecateSctRequest.setComment(request.getComment());


        Integer returnedNamespace = sctIdHelper.getNamespace(request.getSctid());
        if (!returnedNamespace.equals(request.getNamespace())) {
            logger.error("error releaseSct():: Namespaces differences between sctId and parameter");
            throw new CisException(HttpStatus.ACCEPTED, "Namespaces differences between sctId and parameter");
        } else {
            if (bulkSctidService.isAbleUser(String.valueOf(returnedNamespace), authToken)) {
                deprecateSctRequest.setAuthor(authToken.getName());
                Sctid sctRec = sctIdHelper.getSctid(request.getSctid());
                if (sctRec.getSctid().isEmpty()) {
                    logger.error("error releaseSct():: No Sctid Rec Found");
                    throw new CisException(HttpStatus.ACCEPTED, "No Sctid Rec Found");
                } else {
                    var newStatus = stateMachine.getNewStatus(sctRec.getStatus(), stateMachine.actions.get("release"));
                    if (null != newStatus) {
                        sctRec.setStatus(newStatus);
                        sctRec.setAuthor(deprecateSctRequest.getAuthor());
                        sctRec.setSoftware(deprecateSctRequest.getSoftware());
                        sctRec.setComment(deprecateSctRequest.getComment());

                        sctRec.setJobId(null);
                        output = sctidRepository.save(sctRec);
                    } else {
                        logger.error("error releaseSct()::Cannot release SCTID:{}, current status: {}", request.getSctid(), sctRec.getStatus());
                        throw new CisException(HttpStatus.BAD_REQUEST, "Cannot release SCTID:" + request.getSctid() + ", current status: " + sctRec.getStatus());
                    }
                }

            } else {
                logger.error("error releaseSct():: No permission for the selected operation");
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        }
        logger.info("releaseSct() - Response :: {}", output);
        return output;
    }

    public Sctid publishSct(AuthenticateResponseDto authToken, DeprecateSctRequestDTO request) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto-{} :: DeprecateSctRequestDTO - {} ", authToken, request);
        Sctid output = new Sctid();
        DeprecateSctRequest deprecateSctRequest = new DeprecateSctRequest();
        deprecateSctRequest.setSctid(request.getSctid());
        deprecateSctRequest.setNamespace(request.getNamespace());
        deprecateSctRequest.setSoftware(request.getSoftware());
        deprecateSctRequest.setComment(request.getComment());


        Integer returnedNamespace = sctIdHelper.getNamespace(request.getSctid());
        if (!returnedNamespace.equals(request.getNamespace())) {
            logger.error("error publishSct()::Namespaces differences between sctId and parameter");
            throw new CisException(HttpStatus.ACCEPTED, "Namespaces differences between sctId and parameter");
        } else {
            if (bulkSctidService.isAbleUser(String.valueOf(returnedNamespace), authToken)) {
                deprecateSctRequest.setAuthor(authToken.getName());
                Sctid sctRec = sctIdHelper.getSctid(request.getSctid());
                if (sctRec.getSctid().isEmpty()) {
                    logger.error("error publishSct():: No Sctid Rec Found");
                    throw new CisException(HttpStatus.ACCEPTED, "No Sctid Rec Found");
                } else {
                    var newStatus = stateMachine.getNewStatus(sctRec.getStatus(), stateMachine.actions.get("publish"));
                    if (null != newStatus) {
                        sctRec.setStatus(newStatus);
                        sctRec.setAuthor(deprecateSctRequest.getAuthor());
                        sctRec.setSoftware(deprecateSctRequest.getSoftware());
                        sctRec.setComment(deprecateSctRequest.getComment());


                        sctRec.setJobId(null);
                        output = sctidRepository.save(sctRec);
                    } else {
                        logger.error("error publishSct():: Cannot publish SCTID:{}, current status:{}", request.getSctid(), sctRec.getStatus());
                        throw new CisException(HttpStatus.BAD_REQUEST, "Cannot publish SCTID:" + request.getSctid() + ", current status: " + sctRec.getStatus());
                    }
                }

            } else {
                logger.error("error publishSct():: No permission for the selected operation");
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        }
        logger.info("publishSct() - Response :: {}", output);
        return output;
    }

    public SctWithSchemeResponseDTO generateSctid(AuthenticateResponseDto authToken, SctidsGenerateRequestDto generationData) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto-{} :: SctidsGenerateRequestDto - {} ", authToken, generationData);
        SctWithSchemeResponseDTO sctResponse = new SctWithSchemeResponseDTO();

        SctidGenerate generate = new SctidGenerate();
        generate.setNamespace(generationData.getNamespace());
        generate.setPartitionId(generationData.getPartitionId());
        generate.setSystemId(generationData.getSystemId());
        generate.setSoftware(generationData.getSoftware());
        generate.setComment(generationData.getComment());
        generate.setGenerateLegacyIds(generationData.isGenerateLegacyIds());


        if (bulkSctidService.isAbleUser((generationData.getNamespace()).toString(), authToken)) {
            if ((generationData.getNamespace() == 0 && (!generationData.getPartitionId().substring(0, 1).equalsIgnoreCase("0")))
                    || (generationData.getNamespace() != 0 && !generationData.getPartitionId().substring(0, 1).equalsIgnoreCase("1"))) {
                logger.error("error generateSctid():: Namespace and partitionId parameters are not consistent.");
                throw new CisException(HttpStatus.ACCEPTED, "Namespace and partitionId parameters are not consistent.");
            }
            if (generationData.getSystemId().isBlank()) {

                generate.setSystemId(sctIdHelper.guid());
                generate.setAutoSysId(true);
            }

            generate.setAuthor(authToken.getName());
            Sctid sctRec1 = this.generateSctidSubFun(generate);
            var sctIdRecordArray = new ArrayList<SchemeId>();
            if (generationData.isGenerateLegacyIds() &&
                    generationData.getPartitionId().substring(1, 1).equalsIgnoreCase("0")) {
                if (bulkSctidService.isSchemeAbleUser(SchemeName.CTV3ID.schemeName, authToken)) {
                    SchemeId schemeId = this.generateSchemeId(SchemeName.valueOf(SchemeName.CTV3ID.schemeName), generate);
                    sctIdRecordArray.add(schemeId);
                }
                if (bulkSctidService.isSchemeAbleUser(SchemeName.SNOMEDID.schemeName, authToken)) {
                    SchemeId schemeId = this.generateSchemeId(SchemeName.valueOf(SchemeName.SNOMEDID.schemeName), generate);
                    sctIdRecordArray.add(schemeId);
                }
            }
            sctResponse.setSctid(sctRec1.getSctid());
            sctResponse.setSequence(sctRec1.getSequence());
            sctResponse.setNamespace(sctRec1.getNamespace());
            sctResponse.setPartitionId(sctRec1.getPartitionId());
            sctResponse.setCheckDigit(sctRec1.getCheckDigit());
            sctResponse.setSystemId(sctRec1.getSystemId());
            sctResponse.setStatus(sctRec1.getStatus());
            sctResponse.setAuthor(sctRec1.getAuthor());
            sctResponse.setSoftware(sctRec1.getSoftware());
            sctResponse.setExpirationDate(sctRec1.getExpirationDate());
            sctResponse.setComment(sctRec1.getComment());
            sctResponse.setAdditionalIds(sctIdRecordArray);
        } else {
            logger.error("error generateSctid():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
        logger.info("generateSctid() - Response :: {}", sctResponse);
        return sctResponse;
    }

    public Sctid generateSctidSubFun(SctidGenerate generationData) throws CisException {
        logger.debug("Request Received : SctidGenerate-{}", generationData);
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
        logger.info("generateSctidSubFun() - Response :: {}", sctOut);
        return sctOut;
    }

    public Sctid setNewSCTIdRecord(SctidGenerate generationData, String action) throws CisException {
        logger.debug("Request Received : SctidGenerate-{} :: action - {} ", generationData, action);
        Sctid sctOutput = new Sctid();
        sctOutput = this.setAvailableSCTIDRecord2NewStatus(generationData, action);
        logger.info("setNewSCTIdRecord() - Response :: {}", sctOutput);
        return sctOutput;
    }

    public Sctid setAvailableSCTIDRecord2NewStatus(SctidGenerate generationData, String action) throws CisException {
        logger.debug("Request Received : SctidGenerate-{} :: action - {} ", generationData, action);
        Sctid sctOutput = new Sctid();
        List<Sctid> sctList = new ArrayList<>();
        Map<String, Object> queryObject = new HashMap<>();
        if (null != generationData.getNamespace() && !generationData.getPartitionId().isBlank()) {
            queryObject.put("namespace", generationData.getNamespace());
            queryObject.put("partitionId", "'" + generationData.getPartitionId() + "'");
            queryObject.put("status", "'" + stateMachine.statuses.get("available") + "'");
            sctList = this.findSctWithIndexAndLimit(queryObject, "1", null);
            if (sctList.size() > 0) {
                var newStatus = stateMachine.getNewStatus(sctList.get(0).getStatus(), action);
                if (null != newStatus) {
                    if (null != generationData.getSystemId() && generationData.getSystemId().trim() != "") {
                        sctList.get(0).setSystemId(generationData.getSystemId());
                    }
                    sctList.get(0).setStatus(newStatus);
                    sctList.get(0).setAuthor(generationData.getAuthor());
                    sctList.get(0).setSoftware(generationData.getSoftware());

                    sctList.get(0).setExpirationDate(LocalDateTime.now());
                    sctList.get(0).setComment(generationData.getComment());
                    sctList.get(0).setJobId(null);
                    sctList.get(0).setModified_at(LocalDateTime.now());
                    sctOutput = sctidRepository.save(sctList.get(0));
                } else {
                    sctOutput = sctIdDM.counterMode(generationData, action);
                }
            } else {

                sctOutput = sctIdDM.counterMode(generationData, action);
            }
        } else {
            logger.error("error setAvailableSCTIDRecord2NewStatus():: Request Cannot be Empty");
            throw new CisException(HttpStatus.BAD_REQUEST, "Request Cannot be Empty");
        }
        logger.info("setAvailableSCTIDRecord2NewStatus() - Response :: {}", sctOutput);
        return sctOutput;
    }


    public Sctid setAvailableSCTIDRecord2NewStatus(SCTIDReserveRequest sctidReservationRequest, String action) throws CisException {
        logger.debug("Request Received : SCTIDReserveRequest-{} :: action - {} ", sctidReservationRequest, action);
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
        logger.info("setAvailableSCTIDRecord2NewStatus() - Response :: {}", result);
        return result;
    }

    public SchemeId generateSchemeId(SchemeName scheme, SctidGenerate generationData) throws CisException {
        logger.debug("Request Received : schemeName-{} :: SctidGenerate - {} ", scheme, generationData);
        SchemeId schemeId = new SchemeId();
        List<SchemeId> schemeList = new ArrayList<>();
        if (!generationData.isAutoSysId()) {
            schemeList = schemeIdRepository.findBySchemeAndSystemId(scheme.toString(), generationData.getSystemId());
            if (schemeList.size() > 0)
                schemeId = schemeList.get(0);
            else {
                schemeId = setNewSchemeIdRecord(scheme, generationData, stateMachine.actions.get("generate"));
            }
        } else {
            schemeId = setNewSchemeIdRecord(scheme, generationData, stateMachine.actions.get("generate"));
        }
        logger.info("generateSchemeId() - Response :: {}", schemeId);
        return schemeId;
    }

    public SchemeId setNewSchemeIdRecord(SchemeName scheme, SctidGenerate generationData, String action) throws CisException {
        logger.debug("Request Received : schemeName-{} :: SctidGenerate - {} :: action - {} ", scheme, generationData, action);
        SchemeId schemeId;
        schemeId = this.setAvailableSchemeIdRecord2NewStatus(scheme, generationData, action);
        if (null != schemeId) {
            logger.info("setNewSchemeIdRecord() - Response :: {}", schemeId);
            return schemeId;
        } else {
            SchemeId schemeId1 = counterMode(scheme, generationData, action);
            logger.info("setNewSchemeIdRecord() - Response :: {}", schemeId1);
            return schemeId1;
        }
    }

    public SchemeId setAvailableSchemeIdRecord2NewStatus(SchemeName scheme, SctidGenerate generationData, String action) throws CisException {
        logger.debug("Request Received : schemeName-{} :: SctidGenerate - {}  :: action - {}", scheme, generationData, action);
        List<SchemeId> schemeIdRecords = new ArrayList<>();
        SchemeId outputSchemeRec = new SchemeId();
        Map<String, Object> queryObject = new HashMap();
        if (!scheme.toString().isEmpty() && null != scheme) {
            queryObject.put("scheme", scheme);
            queryObject.put("status", stateMachine.statuses.get("available"));
        }
        schemeIdRecords = this.findSchemeWithIndexAndLimit(queryObject, "1", null);
        if (schemeIdRecords.size() > 0) {
            var newStatus = stateMachine.getNewStatus(schemeIdRecords.get(0).getStatus(), action);
            if (null != newStatus) {

                if (null != generationData.getSystemId() && generationData.getSystemId().trim() != "") {
                    schemeIdRecords.get(0).setSystemId(generationData.getSystemId());
                }
                schemeIdRecords.get(0).setStatus(newStatus);
                schemeIdRecords.get(0).setAuthor(generationData.getAuthor());
                schemeIdRecords.get(0).setSoftware(generationData.getSoftware());

                schemeIdRecords.get(0).setExpirationDate(null);
                schemeIdRecords.get(0).setComment(generationData.getComment());
                schemeIdRecords.get(0).setJobId(null);
                outputSchemeRec = schemeIdRepository.save(schemeIdRecords.get(0));
            } else {
                counterMode(scheme, generationData, action);
            }
        } else {
            logger.error("error setAvailableSchemeIdRecord2NewStatus():: error getting available schemeId for:{}", scheme);
            throw new CisException(HttpStatus.ACCEPTED, "error getting available schemeId for:" + scheme + ", err: ");
        }
        logger.info("setAvailableSchemeIdRecord2NewStatus() - Response :: {}", outputSchemeRec);
        return outputSchemeRec;
    }

    public List<SchemeId> findSchemeWithIndexAndLimit(Map<String, Object> queryObject, String limit, String skip) {
        logger.debug("Request Received : queryObject-{} :: limit - {}  :: skip - {}", queryObject, limit, skip);
        List<SchemeId> schemeList;
        var limitR = 100;
        var skipTo = 0;
        if (!limit.isEmpty() && null != limit)
            limitR = Integer.parseInt(limit);
        if (!skip.isEmpty() && null != skip)
            skipTo = Integer.parseInt(skip);

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


            sql = "SELECT * FROM schemeId" + swhere + " order by schemeId limit " + limit;
        } else {

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
        logger.info("findSchemeWithIndexAndLimit() - Response :: {}", schemeList);
        return schemeList;
    }

    public SchemeId counterMode(SchemeName schemeName, SctidGenerate request, String reserve) throws CisException {
        logger.debug("Request Received : schemeName-{} :: request - {} :: reserve - {}", schemeName, request, reserve);
        String newSchemeId = getNextSchemeId(schemeName, request);
        SchemeId updatedrecord = null;
        if (newSchemeId != null) {
            SchemeId schemeIdRecord = schemeIdService.getSchemeIdsByschemeIdList(schemeName.toString(), newSchemeId);
            if (schemeIdRecord != null) {
                var newStatus = stateMachine.getNewStatus(schemeIdRecord.getStatus(), reserve);
                if (null != newStatus) {

                    if (null != request.getSystemId() && request.getSystemId().trim() != "") {
                        schemeIdRecord.setSystemId(request.getSystemId());
                    }
                    schemeIdRecord.setStatus(newStatus);
                    schemeIdRecord.setAuthor(request.getAuthor());
                    schemeIdRecord.setSoftware(request.getSoftware());
                    schemeIdRecord.setExpirationDate(null);
                    schemeIdRecord.setComment(request.getComment());
                    schemeIdRecord.setJobId(null);

                    updatedrecord = schemeIdService.updateSchemeIdRecord(schemeIdRecord, schemeName.toString());

                } else {
                    updatedrecord = counterMode(schemeName, request, reserve);
                }
            }
        }
        return updatedrecord;
    }

    private String getNextSchemeId(SchemeName schemeName, SctidGenerate request) {
        logger.debug("Request Received : schemeName-{} :: SctidGenerate - {} ", schemeName, request);
        Optional<SchemeIdBase> schemeIdBaseList = schemeIdBaseRepository.findByScheme(schemeName.toString());
        SchemeIdBase schemeIdBase = null;
        String nextId = null;

        if (schemeIdBaseList.isPresent()) {
            if (schemeName.toString().toUpperCase().equalsIgnoreCase("SNOMEDID")) {
                if (schemeIdBaseList.isPresent())
                    nextId = SNOMEDID.getNextId(schemeIdBaseList.get().getIdBase());
            } else if (schemeName.toString().toUpperCase().equalsIgnoreCase("CTV3ID")) {
                if (schemeIdBaseList.isPresent())
                    nextId = CTV3ID.getNextId(schemeIdBaseList.get().getIdBase());
            }
            schemeIdBase.setIdBase(nextId);
        }
        schemeIdBaseRepository.save(schemeIdBase);
        logger.info("getNextSchemeId() Response: {}", nextId);
        return nextId;
    }

    public Sctid registerSctid(AuthenticateResponseDto authToken, SCTIDRegistrationRequest registrationData) throws CisException {
        logger.debug("Request Received : SCTIDRegistrationRequest-{} :: authToken - {} ", registrationData, authToken);
        Sctid result = new Sctid();
        SCTIDRegisterRequest registerRequest = new SCTIDRegisterRequest();
        registerRequest.setSctid(registrationData.getSctid());
        registerRequest.setNamespace(registrationData.getNamespace());
        registerRequest.setSystemId(registrationData.getSystemId());
        registerRequest.setSoftware(registrationData.getSoftware());
        registerRequest.setComment(registrationData.getComment());

        Integer returnedNamespace = sctIdHelper.getNamespace(registrationData.getSctid());
        if (!(returnedNamespace.equals(registrationData.getNamespace()))) {
            logger.error("error registerSctid():: Namespaces differences between sctId and parameter");
            throw new CisException(HttpStatus.ACCEPTED, "Namespaces differences between sctId and parameter");
        } else {
            if (bulkSctidService.isAbleUser((registrationData.getNamespace()).toString(), authToken)) {
                if (registrationData.getSystemId().isBlank() || registrationData.getSystemId().isEmpty()) {
                    registerRequest.setAutoSysId(true);
                }
                registerRequest.setAuthor(authToken.getName());

                Sctid sct = sctIdDM.registerSctid(registerRequest, "SCTIDRegisterRequest");
                if (null != (sct))
                    result = sct;
            } else {
                logger.error("error registerSctid():: No permission for the selected operation");
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        }
        logger.info("registerSctid() - Response :: {}", result);
        return result;
    }

    public Sctid reserveSctid(AuthenticateResponseDto token, SCTIDReservationRequest reservationData) throws CisException {
        logger.debug("Request Received : SCTIDReservationRequest-{} :: authToken - {} ", reservationData, token);
        Sctid result = null;
        SCTIDReserveRequest reserveRequest = new SCTIDReserveRequest();
        reserveRequest.setNamespace(reservationData.getNamespace());
        reserveRequest.setPartitionId(reservationData.getPartitionId());
        reserveRequest.setExpirationDate(reservationData.getExpirationDate());
        reserveRequest.setSoftware(reservationData.getSoftware());
        reserveRequest.setComment(reservationData.getComment());

        if (bulkSctidService.isAbleUser((reservationData.getNamespace()).toString(), token)) {
            if ((reservationData.getNamespace() == 0 && reservationData.getPartitionId().substring(0, 1) != "0")
                    || (reservationData.getNamespace() != 0 && reservationData.getPartitionId().substring(0, 1) != "1")) {
                logger.error("error reserveSctid():: Namespace and partitionId parameters are not consistent.");
                throw new CisException(HttpStatus.ACCEPTED, ("Namespace and partitionId parameters are not consistent."));
            }
            reserveRequest.setAuthor(token.getName());
            Sctid sct = this.reserveSctid(reserveRequest);
            if (null != (sct))
                result = sct;
        } else {
            logger.error("error reserveSctid():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
        logger.info("reserveSctid() - Response :: {}", result);
        return result;
    }

    public Sctid reserveSctid(SCTIDReserveRequest sctidReservationRequest) throws CisException {
        logger.debug("Request Received : sctidReservationRequest-{}", sctidReservationRequest);
        Sctid result = null;
        result = this.setAvailableSCTIDRecord2NewStatus(sctidReservationRequest, stateMachine.actions.get("reserve"));
        logger.info("reserveSctid()  with single Request param - Response :: {}", result);
        return result;
    }

    public ResultDto getStats(String token, String username) throws CisException {
        logger.debug("Request Received : token-{} :: username - {} ", token, username);
        ResultDto result = new ResultDto();
        List<String> users = new ArrayList<>();
        List<String> securityAdmins = new ArrayList<>();
        List<String> securityUsers = new ArrayList<>();
        securityAdmins.add("keerthika");
        securityAdmins.add("lakshmana");
        boolean adminU = false;

        for (String admin : securityAdmins) {
            if (admin.equalsIgnoreCase(username))
                adminU = true;
            if (!added(admin))
                users.add(admin);
        }
        for (String user : securityUsers) {
            if (!added(user))
                users.add(user);
        }
        result.setUsers((long) users.size());
        HashMap<String, Long> hash = new HashMap<>();
        if (adminU) {
            long schemeCount = schemeIdBaseRepository.count();
            result.setSchemes(schemeCount);
            long namespaceCount = namespaceRepository.count();
            List<Namespace> namespaceList = namespaceRepository.findAll();

            var total = namespaceList.size();
            var done = 0;
            if (total > 0) {
                for (int i = 0; i < namespaceList.size(); i++) {
                    Namespace namespace = namespaceList.get(i);
                    Map<String, Integer> queryObject = new HashMap();
                    if (null != namespace) {
                        queryObject.put("namespace", namespace.getNamespace());
                    }
                    long sctCount = sctidCount(queryObject);
                    if (sctCount > 0) {
                        hash.put(namespace.getNamespace().toString(), sctCount);

                    }
                    done++;
                    if (total == done) {
                        hash.put("total", namespaceCount);
                        result.setNamespaces(hash);
                        break;
                    }
                }
            }
        } else {
            var otherGroups = new ArrayList<String>();
            List<String> namespacesFromGroup = new ArrayList();
            List<String> groups = securityController.getUserGroup(username, token);
            if (groups.size() > 0) {
                for (String group :
                        groups) {
                    if (group.substring(0, group.indexOf("-")) == "namespace")
                        namespacesFromGroup.add(group.substring(group.indexOf("-") + 1));
                    else otherGroups.add(group);
                }
            }
            Map<String, ArrayList<String>> schemeQuery = new HashMap();
            if (otherGroups.size() > 0) {
                schemeQuery.put("username", otherGroups);
            }
            Long schemeCount = this.permissionsSchemeCount(schemeQuery);
            result.setSchemes(schemeCount);
            List<Namespace> namespaceList = this.findPermissionsNamespace(schemeQuery);
            if (namespacesFromGroup.size() > 0) {
                for (String namespLoop :
                        namespacesFromGroup) {
                    var foundNamespace = false;
                    for (Namespace namesp :
                            namespaceList) {
                        if ((namesp.getNamespace().toString()).equalsIgnoreCase(namespLoop))
                            foundNamespace = true;
                    }
                    if (!foundNamespace)
                        namespaceList.add(new Namespace(Integer.valueOf(namespLoop)));
                }
            }

            var total = namespaceList.size();
            var done = 0;
            if (total > 0) {
                Map<String, Integer> queryObject = new HashMap();
                for (Namespace namespaceR :
                        namespaceList) {
                    if (null != namespaceR) {
                        queryObject.put("namespace", namespaceR.getNamespace());
                    }
                    long sctCount = sctidCount(queryObject);
                    if (sctCount > 0) {
                        done++;

                        hash.put(namespaceR.getNamespace().toString(), sctCount);
                        if (total == done) {
                            hash.put("total", (long) total);
                            result.setNamespaces(hash);
                            break;
                        }
                    }
                }
            }
        }
        logger.info("getStats() - Response :: {}", result);
        return result;
    }

    public boolean added(String userToAdd) {
        List<String> users = new ArrayList<>();
        boolean found = false;
        for (String user : users) {
            if (user.equalsIgnoreCase(userToAdd)) {
                found = true;
            }
        }
        return found;
    }

    public Long sctidCount(Map<String, Integer> queryObject) {
        logger.debug("Request Received : queryObject-{} ", queryObject);
        var swhere = "";
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
        sql = "SELECT count(*) as count FROM sctId" + swhere;
        Query query = entityManager.createNativeQuery(sql);
        List<BigInteger> result = query.getResultList();
        logger.info("sctidCount() : response - {}", result.get(0).longValue());
        return result.get(0).longValue();
    }

    public Long permissionsSchemeCount(Map<String, ArrayList<String>> queryObject) {
        logger.debug("Request Received : queryObject-{} :: authToken - {} ", queryObject);
       // var swhere = "";
        StringBuffer swhere = new StringBuffer("");
        if (queryObject.size() > 0) {
            String groupList = "";
            for (var query :
                    queryObject.entrySet()) {
                for (String group :
                        query.getValue()) {
                    groupList += "," + "'" + group + "'";
                }

                groupList = groupList.substring(1);
                //swhere += " And " + query.getKey() + " in" + "(" + groupList + ")";
                swhere =  swhere.append(" And ").append(query.getKey()).append(" in").append("(").append(groupList).append(")");
            }
        }

        if (swhere.toString() != "") {
            String subValue =  swhere.substring(5);
           // swhere = " WHERE " + swhere.substring(5);
            swhere = swhere.append(" WHERE ").append(subValue);
        }
        String sql;
        sql = "SELECT count(*) as count FROM permissionsscheme" + swhere;
        Query query = entityManager.createNativeQuery(sql);
        List<BigInteger> result = query.getResultList();
        logger.info("permissionsSchemeCount() : response - {}", result.get(0).longValue());
        return result.get(0).longValue();
    }

    public List<Namespace> findPermissionsNamespace(Map<String, ArrayList<String>> queryObject) {
        logger.debug("Request Received : queryObject-{} ", queryObject);
       // var swhere = "";
        StringBuffer swhere = new StringBuffer("");
        if (queryObject.size() > 0) {
            String groupList = "";
            for (var query :
                    queryObject.entrySet()) {
                for (String group :
                        query.getValue()) {
                    groupList += "," + "'" + group + "'";
                }

                groupList = groupList.substring(1);
                //swhere += " And " + query.getKey() + " in" + "(" + groupList + ")";
                swhere = swhere.append(" And ").append(query.getKey()).append(" in").append("(").append(groupList).append(")");
            }
        }

        if (swhere.toString() != "") {
            String subValue = swhere.substring(5);
           // swhere = " WHERE " + swhere.substring(5);
            swhere = swhere.append(" WHERE ").append(subValue);
        }
        String sql;
        sql = "SELECT * FROM permissionsnamespace" + swhere;
        Query query = entityManager.createNativeQuery(sql);
        List<Namespace> result = query.getResultList();
        logger.info("findPermissionsNamespace() : response - {}", result);
        return result;
    }

}
