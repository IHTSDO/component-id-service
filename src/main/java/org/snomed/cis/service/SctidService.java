package org.snomed.cis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.controller.SecurityController;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeIdBase;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.BulkSchemeIdRepository;
import org.snomed.cis.repository.NamespaceRepository;
import org.snomed.cis.repository.SchemeIdBaseRepository;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.service.DM.SCTIdDM;
import org.snomed.cis.service.DM.SchemeIdDM;
import org.snomed.cis.util.CTV3ID;
import org.snomed.cis.util.SNOMEDID;
import org.snomed.cis.util.SctIdHelper;
import org.snomed.cis.util.StateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
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
        StringBuffer resultWhere = new StringBuffer("");
        StringBuffer swhere = new StringBuffer("");
        if (!systemId.isEmpty() && null != systemId) {
            objQuery = systemId;
            swhere = swhere.append(" And ").append("systemId").append("=").append("'").append((objQuery)).append("'");
        }
        if (!(swhere.toString().equalsIgnoreCase(""))) {
            resultWhere.append(" WHERE ").append(swhere.toString().substring(5));
        }
        StringBuffer sql = new StringBuffer("");
        if (limitR > 0 && (skipTo == 0)) {
            sql.append("SELECT * FROM schemeid").append((resultWhere)).append(" order by schemeId limit ").append(limit);
        } else {
            sql.append("SELECT * FROM schemeid").append((resultWhere)).append(" order by schemeId");
        }
        Query genQuery = entityManager.createNativeQuery(sql.toString(), SchemeId.class);
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
                logger.debug("getSct - Response size :: {}", (null==sctList?"0":sctList.size()));
                return sctList;
            } else {
                logger.debug("getSct - Response size :: {}", "0");
                return Collections.EMPTY_LIST;
            }
        } else {
            logger.error("error getSct():: user:{} has neither admin access nor namespace permission for the selected operation.Namespace value:{}",authToken.toString(),"false");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }

    }

    public SctWithSchemeResponseDTO getSctWithId(AuthenticateResponseDto authToken, String sctid, String includeAdditionalIds) throws CisException {
        logger.debug("Request Received :authToken - {} :: sctid-{} :: includeAdditionalIds - {} ", authToken.toString(), sctid, includeAdditionalIds);
        Sctid sctResult = new Sctid();
        SctWithSchemeResponseDTO output = new SctWithSchemeResponseDTO();
        SctWithSchemeResponseDTO sctWithSchemeResponseDTO = new SctWithSchemeResponseDTO();

        var namespace = sctIdHelper.getNamespace(sctid);
        if (null != namespace) {
            if (bulkSctidService.isAbleUser(String.valueOf(namespace), authToken)) {
                sctWithSchemeResponseDTO = this.getSctCommon(output, sctid, includeAdditionalIds);
            } else {
                logger.error("error getSctWithId():: user :{} has neither admin access no namespace permission for the selected operation. namespace:{}",authToken.toString(),namespace);
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        } else {
            sctWithSchemeResponseDTO = this.getSctCommon(output, sctid, includeAdditionalIds);
        }
        logger.debug("getSctWithId() - Response :: {}", sctWithSchemeResponseDTO);
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

        StringBuffer resultWhere = new StringBuffer("");
        StringBuffer swhere = new StringBuffer("");
        if (queryObject.size() > 0) {
            for (var query :
                    queryObject.entrySet()) {
                swhere = swhere.append(" And ").append(query.getKey()).append("=").append(query.getValue());
            }
        }
        if (!(swhere.toString().equalsIgnoreCase(""))) {
            resultWhere.append(" WHERE ").append(swhere.substring(5));
        }
        StringBuffer sql = new StringBuffer();
        if ((limitR > 0) && (skipTo == 0)) {

            if (swhere.toString() != "")
                sql.append("Select * FROM sctid USE INDEX (nam_par_st)").append(resultWhere).append(" order by sctid limit ").append(limitR);
            else
                sql.append("Select * FROM sctid ").append(resultWhere).append(" order by sctid limit ").append(limitR);
        } else {

            if (swhere.toString() != "")
                sql.append("Select * FROM sctid USE INDEX (nam_par_st)").append(resultWhere).append(" order by sctid");
            else
                sql.append("Select * FROM sctid ").append(resultWhere).append(" order by sctid");
        }
        Query genQuery = entityManager.createNativeQuery(sql.toString(), Sctid.class);
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
        logger.debug("findSctWithIndexAndLimit() - Response Size :: {}", (null==sctList?"0":sctList.size()));
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
            output.setJobId(newSct.getJobId());
            output.setCreated_at(newSct.getCreated_at());
            output.setModified_at(newSct.getModified_at());
            if (!includeAdditionalIds.isEmpty() && includeAdditionalIds.equalsIgnoreCase("true")) {
                List<SchemeId> schemeResult = getSchemeIds(newSct.getSystemId(), "10", "0");
                output.setAdditionalIds(schemeResult);
                return output;
            } else {
                logger.debug("getSctCommon() - Response :: {}", output);
                return output;
            }
        } else {
            logger.error("error getSctCommon():: Not valid Sctid:{}", sctid);
            throw new CisException(HttpStatus.BAD_REQUEST, "Not valid SCTID.");
        }
    }

    public CheckSctidResponseDTO checkSctid(String sctid) throws CisException {
        CheckSctidResponseDTO checkSctid = SctIdHelper.checkSctid(sctid);
        logger.debug("checkSctid() - Response :: {}", checkSctid);
        return checkSctid;
    }

    public Sctid getSctWithSystemId(AuthenticateResponseDto authToken, Integer namespaceId, String systemId) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto-{} :: namespaceId - {} :: systemId - {}", authToken.toString(), namespaceId, systemId);
        Sctid sct = null;
        if (bulkSctidService.isAbleUser(String.valueOf(namespaceId), authToken)) {
            List<Sctid> result = sctidRepository.findBySystemIdAndNamespace(systemId, namespaceId);
            sct = result.size() > 0 ? result.get(0) : null;
        } else {
            logger.error("error getSctWithSystemId():: user :{} has neither admin access nor namespace permission for the selected operation. namespace:{}",authToken.toString(),namespaceId);
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
        logger.debug("getSctWithSystemId() - Response :: {}", sct);
        return sct;
    }

    public Sctid deprecateSct(AuthenticateResponseDto authToken, DeprecateSctRequestDTO request) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto-{} :: DeprecateSctRequestDTO - {} ", authToken.toString(), request);
        Sctid output = new Sctid();
        DeprecateSctRequest deprecateSctRequest = new DeprecateSctRequest();
        deprecateSctRequest.setSctid(request.getSctid());
        deprecateSctRequest.setNamespace(request.getNamespace());
        deprecateSctRequest.setSoftware(request.getSoftware());
        deprecateSctRequest.setComment(request.getComment());

        Integer returnedNamespace = sctIdHelper.getNamespace(request.getSctid());
        if (!returnedNamespace.equals(request.getNamespace())) {
            logger.error("error deprecateSct():: Difference between generated namespace : {} from sctid - {} and input 'namespace' - {}",returnedNamespace,request.getSctid(),request.getNamespace());
            throw new CisException(HttpStatus.BAD_REQUEST, "Namespaces differences between sctId and parameter");
        } else {
            if (bulkSctidService.isAbleUser(String.valueOf(returnedNamespace), authToken)) {
                deprecateSctRequest.setAuthor(authToken.getName());
                Sctid sctRec = sctIdHelper.getSctid(deprecateSctRequest.getSctid());
                if (sctRec.getSctid().isEmpty()) {
                    logger.error("error deprecateSct():: No Sctid Record found in Database. Sctid:{}",deprecateSctRequest.getSctid());
                    throw new CisException(HttpStatus.ACCEPTED, "No Sctid Record Found");
                } else {
                    var newStatus = stateMachine.getNewStatus(sctRec.getStatus(), stateMachine.actions.get("deprecate"));
                    if (null != newStatus) {
                        sctRec.setStatus(newStatus);
                        sctRec.setAuthor(deprecateSctRequest.getAuthor());
                        sctRec.setSoftware(deprecateSctRequest.getSoftware());
                        sctRec.setComment(deprecateSctRequest.getComment());
                        sctRec.setJobId(null);
                        sctRec.setCreated_at(sctRec.getCreated_at()!=null?sctRec.getCreated_at():LocalDateTime.now());
                        sctRec.setModified_at(LocalDateTime.now());
                        output = sctidRepository.save(sctRec);
                    } else {
                        logger.error("error deprecateSct():: Cannot deprecate SCTID:{}, current status: {}", request.getSctid(), sctRec.getStatus());
                        throw new CisException(HttpStatus.BAD_REQUEST, "Cannot deprecate SCTID:" + request.getSctid() + ", current status: " + sctRec.getStatus());
                    }
                }

            } else {
                logger.error("error deprecateSct():: user:{} has neither admin access nor namespace permission for the selected operation. namespace:{}",authToken.toString(),returnedNamespace);
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        }
        logger.debug("deprecateSct() - Response :: {}", output);
        return output;
    }

    public Sctid releaseSct(AuthenticateResponseDto authToken, DeprecateSctRequestDTO request) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto-{} :: DeprecateSctRequestDTO - {} ", authToken.toString(), request);
        Sctid output = new Sctid();
        DeprecateSctRequest deprecateSctRequest = new DeprecateSctRequest();
        deprecateSctRequest.setSctid(request.getSctid());
        deprecateSctRequest.setNamespace(request.getNamespace());
        deprecateSctRequest.setSoftware(request.getSoftware());
        deprecateSctRequest.setComment(request.getComment());


        Integer returnedNamespace = sctIdHelper.getNamespace(request.getSctid());
        if (!returnedNamespace.equals(request.getNamespace())) {
            logger.error("error releaseSct():: Difference between generated namespace:{} from sctid: {} and input 'namespace':{}",returnedNamespace,request.getSctid(),request.getNamespace());
            throw new CisException(HttpStatus.ACCEPTED, "Namespaces differences between sctId and parameter");
        } else {
            if (bulkSctidService.isAbleUser(String.valueOf(returnedNamespace), authToken)) {
                deprecateSctRequest.setAuthor(authToken.getName());
                Sctid sctRec = sctIdHelper.getSctid(request.getSctid());
                if (sctRec.getSctid().isEmpty()) {
                    logger.error("error releaseSct():: No sctid record found from Database. Sctid:{}",request.getSctid());
                    throw new CisException(HttpStatus.ACCEPTED, "No Sctid Record Found");
                } else {
                    var newStatus = stateMachine.getNewStatus(sctRec.getStatus(), stateMachine.actions.get("release"));
                    if (null != newStatus) {
                        sctRec.setStatus(newStatus);
                        sctRec.setAuthor(deprecateSctRequest.getAuthor());
                        sctRec.setSoftware(deprecateSctRequest.getSoftware());
                        sctRec.setComment(deprecateSctRequest.getComment());
                        sctRec.setCreated_at(sctRec.getCreated_at()!=null?sctRec.getCreated_at():LocalDateTime.now());
                        sctRec.setModified_at(LocalDateTime.now());
                        sctRec.setJobId(null);
                        output = sctidRepository.save(sctRec);
                    } else {
                        logger.error("error releaseSct()::Cannot release SCTID:{}, current status: {}", request.getSctid(), sctRec.getStatus());
                        throw new CisException(HttpStatus.BAD_REQUEST, "Cannot release SCTID:" + request.getSctid() + ", current status: " + sctRec.getStatus());
                    }
                }

            } else {
                logger.error("error releaseSct()::user:{} has neither admin access nor namespace permission for the selected operation. namespace:{}",authToken.toString(),returnedNamespace);
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        }
        logger.debug("releaseSct() - Response :: {}", output);
        return output;
    }

    public Sctid publishSct(AuthenticateResponseDto authToken, DeprecateSctRequestDTO request) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto-{} :: DeprecateSctRequestDTO - {} ", authToken.toString(), request);
        Sctid output = new Sctid();
        DeprecateSctRequest deprecateSctRequest = new DeprecateSctRequest();
        deprecateSctRequest.setSctid(request.getSctid());
        deprecateSctRequest.setNamespace(request.getNamespace());
        deprecateSctRequest.setSoftware(request.getSoftware());
        deprecateSctRequest.setComment(request.getComment());


        Integer returnedNamespace = sctIdHelper.getNamespace(request.getSctid());
        if (!returnedNamespace.equals(request.getNamespace())) {
            logger.error("error publishSct()::Difference between generated namespace:{} from sctid:{} and input 'namespace':{}",returnedNamespace,request.getSctid(),request.getNamespace());
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
                        sctRec.setCreated_at(sctRec.getCreated_at()!=null?sctRec.getCreated_at():LocalDateTime.now());
                        sctRec.setModified_at(LocalDateTime.now());
                        sctRec.setJobId(null);
                        output = sctidRepository.save(sctRec);
                    } else {
                        logger.error("error publishSct():: Cannot publish SCTID:{}, current status:{}", request.getSctid(), sctRec.getStatus());
                        throw new CisException(HttpStatus.BAD_REQUEST, "Cannot publish SCTID:" + request.getSctid() + ", current status: " + sctRec.getStatus());
                    }
                }

            } else {
                logger.error("error publishSct()::user:{} has neither admin access nor namespace permission for the selected operation. namespace:{}",authToken.toString(),returnedNamespace);
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        }
        logger.debug("publishSct() - Response :: {}", output);
        return output;
    }

    public SctWithSchemeResponseDTO generateSctid(AuthenticateResponseDto authToken, SctidsGenerateRequestDto generationData) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto-{} :: SctidsGenerateRequestDto - {} ", authToken.toString(), generationData);
        SctWithSchemeResponseDTO sctResponse = new SctWithSchemeResponseDTO();

        SctidGenerate generate = new SctidGenerate();
        generate.setNamespace(generationData.getNamespace());
        generate.setPartitionId(generationData.getPartitionId());
        generate.setSystemId(generationData.getSystemId());
        generate.setSoftware(generationData.getSoftware());
        generate.setComment(generationData.getComment());
        generate.setGenerateLegacyIds(generationData.isGenerateLegacyIds());


        if (bulkSctidService.isAbleUser((generationData.getNamespace()).toString(), authToken)) {
            if (
                    (generationData.getPartitionId().isBlank() || generationData.getPartitionId().isEmpty())
                            ||
                            ((generationData.getNamespace() == 0 && (!generationData.getPartitionId().substring(0, 1).equalsIgnoreCase("0")))
                                    || (generationData.getNamespace() != 0 && !generationData.getPartitionId().substring(0, 1).equalsIgnoreCase("1")))
            ) {
                logger.error("error generateSctid():: Namespace and partitionId parameters are not consistent.namespace:{}, partitionId:{}",generationData.getNamespace(),generationData.getPartitionId());
                throw new CisException(HttpStatus.BAD_REQUEST, "Namespace and partitionId parameters are not consistent.");
            }
            if ((null==generationData.getSystemId()) || ((null!=generationData.getSystemId())&&(generationData.getSystemId().isBlank()))) {

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
            logger.error("error generateSctid()::user:{} has neither admin access nor namespace permission for the selected operation.namespace:{}",authToken.toString(),generationData.getNamespace());
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
        logger.debug("generateSctid() - Response :: {}", sctResponse);
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
        logger.debug("generateSctidSubFun() - Response :: {}", sctOut);
        return sctOut;
    }

    public Sctid setNewSCTIdRecord(SctidGenerate generationData, String action) throws CisException {
        logger.debug("Request Received : SctidGenerate-{} :: action - {} ", generationData, action);
        Sctid sctOutput = new Sctid();
        sctOutput = this.setAvailableSCTIDRecord2NewStatus(generationData, action);
        logger.debug("setNewSCTIdRecord() - Response :: {}", sctOutput);
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
                    sctList.get(0).setCreated_at( sctList.get(0).getCreated_at()!=null? sctList.get(0).getCreated_at():LocalDateTime.now());
                    sctList.get(0).setModified_at(LocalDateTime.now());
                    sctOutput = sctidRepository.save(sctList.get(0));
                } else {
                    sctOutput = sctIdDM.counterMode(generationData, action);
                }
            } else {
                sctOutput = sctIdDM.counterMode(generationData, action);
            }
        } else {
            logger.error("error setAvailableSCTIDRecord2NewStatus():: input namespace:{} and partitionId:{} is empty.",generationData.getNamespace(),generationData.getPartitionId());
            throw new CisException(HttpStatus.BAD_REQUEST, "Request Cannot be Empty");
        }
        logger.debug("setAvailableSCTIDRecord2NewStatus() - Response :: {}", sctOutput);
        return sctOutput;
    }


    public Sctid setAvailableSCTIDRecord2NewStatus(SCTIDReserveRequest sctidReservationRequest, String action) throws CisException {
        logger.debug("Request Received : SCTIDReserveRequest-{} :: action - {} ", sctidReservationRequest, action);
        Sctid result = null;
        List<Sctid> sctIdRecords = null;
        Map<String, Object> queryObject = new HashMap<>();
        if (null != sctidReservationRequest.getNamespace() && !sctidReservationRequest.getPartitionId().isBlank()) {
            queryObject.put("namespace", sctidReservationRequest.getNamespace());
            queryObject.put("partitionId", "'" + sctidReservationRequest.getPartitionId() + "'");
            queryObject.put("status", "'" + stateMachine.statuses.get("available") + "'");
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
            } else {
                result = sctIdDM.counterMode(sctidReservationRequest, action);
            }
        }
        logger.debug("setAvailableSCTIDRecord2NewStatus() - Response :: {}", result);
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
        logger.debug("generateSchemeId() - Response :: {}", schemeId);
        return schemeId;
    }

    public SchemeId setNewSchemeIdRecord(SchemeName scheme, SctidGenerate generationData, String action) throws CisException {
        logger.debug("Request Received : schemeName-{} :: SctidGenerate - {} :: action - {} ", scheme, generationData, action);
        SchemeId schemeId;
        schemeId = this.setAvailableSchemeIdRecord2NewStatus(scheme, generationData, action);
        if (null != schemeId) {
            logger.debug("setNewSchemeIdRecord() - Response :: {}", schemeId);
            return schemeId;
        } else {
            SchemeId schemeId1 = counterMode(scheme, generationData, action);
            logger.debug("setNewSchemeIdRecord() - Response :: {}", schemeId1);
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
            logger.error("error setAvailableSchemeIdRecord2NewStatus():: error getting available schemeId for scheme:{}", scheme);
            throw new CisException(HttpStatus.ACCEPTED, "error getting available schemeId for:" + scheme + ", err: ");
        }
        logger.debug("setAvailableSchemeIdRecord2NewStatus() - Response :: {}", outputSchemeRec);
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

        StringBuffer swhere = new StringBuffer("");
        StringBuffer resultWhere = new StringBuffer("");
        if (queryObject.size() > 0) {
            for (var query :
                    queryObject.entrySet()) {
                swhere = swhere.append(" And ").append(query.getKey()).append("=").append((query.getValue()));
            }
        }
        if (!(swhere.toString().equalsIgnoreCase(""))) {
            resultWhere.append(" WHERE ").append(swhere.substring(5));
        }
        StringBuffer sql = new StringBuffer();
        if ((limitR > 0) && (skipTo == 0)) {
            sql.append("SELECT * FROM schemeId").append(resultWhere).append(" order by schemeId limit ").append(limit);
        } else {
            sql.append("SELECT * FROM schemeId").append(resultWhere).append(" order by schemeId");
        }
        Query genQuery = entityManager.createQuery(sql.toString());
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
        logger.debug("findSchemeWithIndexAndLimit() - Response size :: {}", (null==schemeList?"0":schemeList.size()));
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
                    updatedrecord = bulkSchemeIdRepository.save(schemeIdRecord);

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
        logger.debug("getNextSchemeId() Response: {}", nextId);
        return nextId;
    }

    public Sctid registerSctid(AuthenticateResponseDto authToken, SCTIDRegistrationRequest registrationData) throws CisException {
        logger.debug("Request Received : SCTIDRegistrationRequest-{} :: authToken - {} ", registrationData, authToken.toString());
        Sctid result = new Sctid();
        SCTIDRegisterRequest registerRequest = new SCTIDRegisterRequest();
        registerRequest.setSctid(registrationData.getSctid());
        registerRequest.setNamespace(registrationData.getNamespace());
        registerRequest.setSystemId(registrationData.getSystemId());
        registerRequest.setSoftware(registrationData.getSoftware());
        registerRequest.setComment(registrationData.getComment());

        Integer returnedNamespace = sctIdHelper.getNamespace(registrationData.getSctid());
        if (!(returnedNamespace.equals(registrationData.getNamespace()))) {
            logger.error("error registerSctid()::Difference between generated namespace:{} from sctid:{} and input 'namespace':{}",returnedNamespace,registrationData.getSctid(),registrationData.getNamespace());
            throw new CisException(HttpStatus.BAD_REQUEST, "Namespaces differences between sctId and parameter");
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
                logger.error("error registerSctid()::user:{} has neither admin access nor namespace permission for the selected operation. namespace:{}",authToken.toString(),registrationData.getNamespace());
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        }
        logger.debug("registerSctid() - Response :: {}", result);
        return result;
    }

    public Sctid reserveSctid(AuthenticateResponseDto token, SCTIDReservationRequest reservationData) throws CisException {
        logger.debug("Request Received : SCTIDReservationRequest-{} :: authToken - {} ", reservationData, token.toString());
        Sctid result = null;
        SCTIDReserveRequest reserveRequest = new SCTIDReserveRequest();
        reserveRequest.setNamespace(reservationData.getNamespace());
        reserveRequest.setPartitionId(reservationData.getPartitionId());
        reserveRequest.setExpirationDate(reservationData.getExpirationDate());
        reserveRequest.setSoftware(reservationData.getSoftware());
        reserveRequest.setComment(reservationData.getComment());

        if (bulkSctidService.isAbleUser((reservationData.getNamespace()).toString(), token)) {
            if ((reservationData.getNamespace() == 0 && !(reservationData.getPartitionId().substring(0, 1).equalsIgnoreCase("0")))
                    || (reservationData.getNamespace() != 0 && !(reservationData.getPartitionId().substring(0, 1).equalsIgnoreCase("1")))) {
                logger.error("error reserveSctid():: Namespace:{} and partitionId:{} parameters are not consistent.",reservationData.getNamespace(),reservationData.getPartitionId());
                throw new CisException(HttpStatus.BAD_REQUEST, ("Namespace and partitionId parameters are not consistent."));
            }
            reserveRequest.setAuthor(token.getName());
            Sctid sct = this.reserveSctid(reserveRequest);
            if (null != (sct))
                result = sct;
        } else {
            logger.error("error reserveSctid()::user:{} has neither admin access nor namespace permission for the selected operation.namespace:{}",token.toString(),reservationData.getNamespace());
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
        logger.debug("reserveSctid() - Response :: {}", result);
        return result;
    }

    public Sctid reserveSctid(SCTIDReserveRequest sctidReservationRequest) throws CisException {
        logger.debug("Request Received : sctidReservationRequest-{}", sctidReservationRequest);
        Sctid result = null;
        result = this.setAvailableSCTIDRecord2NewStatus(sctidReservationRequest, stateMachine.actions.get("reserve"));
        logger.debug("reserveSctid()  with single Request param - Response :: {}", result);
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

}
