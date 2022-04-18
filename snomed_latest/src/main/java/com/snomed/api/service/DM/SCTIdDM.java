package com.snomed.api.service.DM;

import com.snomed.api.controller.dto.SCTIDRegistrationRequest;
import com.snomed.api.controller.dto.SCTIDReservationRequest;
import com.snomed.api.controller.dto.SctidsGenerateRequestDto;
import com.snomed.api.domain.Partitions;
import com.snomed.api.domain.Sctid;
import com.snomed.api.exception.APIException;
import com.snomed.api.helper.ModelsConstants;
import com.snomed.api.helper.SctIdHelper;
import com.snomed.api.helper.StateMachine;
import com.snomed.api.repository.PartitionsRepository;
import com.snomed.api.repository.SctidRepository;
import com.snomed.api.service.BulkSctidService;
import com.snomed.api.service.SctidService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SCTIdDM {
    @Autowired
    SctidRepository sctidRepository;

    @Autowired
    PartitionsRepository partitionsRepository;

    @Autowired
    StateMachine stateMachine;

    @Autowired
    ModelsConstants modelsConstants;

    @Autowired
    BulkSctidService bulkSctidService;

    @Autowired
    SctIdHelper sctIdHelper;

    public Sctid registerSctid(Object request, String requestDto) throws APIException {
        Sctid resultSct = null;
        SCTIDRegistrationRequest sctidRegistrationRequest = null;
        if (requestDto.equalsIgnoreCase("SCTIDRegistrationRequest"))
            sctidRegistrationRequest = (SCTIDRegistrationRequest) request;
        if (!(sctidRegistrationRequest.isAutoSysId())) {
            List<Sctid> result = sctidRepository.findBySystemIdAndNamespace(sctidRegistrationRequest.getSystemId(), sctidRegistrationRequest.getNamespace());
            Sctid sct = result.get(0);
            if (!(sct.getSctid().equalsIgnoreCase(sctidRegistrationRequest.getSctid()))) {
                throw new APIException(HttpStatus.ACCEPTED, "SystemId:" + sctidRegistrationRequest.getSystemId() + " already exists with SctId:" + sct.getSctid());
            }
            if (sct.getStatus().equalsIgnoreCase(stateMachine.statuses.get("assigned"))) {
                resultSct = sct;
            } else {
                Sctid sctid = this.registerNewSctId(sctidRegistrationRequest);
                if (null != sctid)
                    resultSct = sctid;
            }
        } else {
            Sctid sctid = this.registerNewSctId(sctidRegistrationRequest);
            if (null != sctid)
                resultSct = sctid;
        }

        return resultSct;
    }

    public Sctid registerNewSctId(SCTIDRegistrationRequest sctidRegistrationRequest) throws APIException {
        Sctid result = null;
        Sctid sctIdRecord = this.getSctid(sctidRegistrationRequest.getSctid());
        if (null != sctIdRecord) {
            var newStatus = stateMachine.getNewStatus(sctIdRecord.getStatus(), stateMachine.actions.get("register"));
            if (!newStatus.isBlank()) {
                if (!sctidRegistrationRequest.getSystemId().isEmpty()) {
                    sctIdRecord.setSystemId(sctidRegistrationRequest.getSystemId());
                }
                sctIdRecord.setStatus(newStatus);
                sctIdRecord.setAuthor(sctidRegistrationRequest.getAuthor());
                sctIdRecord.setSoftware(sctidRegistrationRequest.getSoftware());
                sctIdRecord.setExpirationDate(sctIdRecord.getExpirationDate());
                sctIdRecord.setComment(sctidRegistrationRequest.getComment());
                sctIdRecord.setJobId(null);
                Sctid updatedRecord = sctidRepository.save(sctIdRecord);
                result = updatedRecord;
            } else {
                throw new APIException(HttpStatus.ACCEPTED, ("Cannot register SCTID:" + sctidRegistrationRequest.getSctid() + ", current status: " + sctIdRecord.getStatus()));
            }
        }
        return result;
    }

    public Sctid getSctid(String sctId) throws APIException {
        Sctid newSct = null;
        if (!sctIdHelper.validSCTId(sctId)) {
            throw new APIException(HttpStatus.ACCEPTED, "Not valid SCTID.");
        } else {
            Sctid sctRec = sctidRepository.getSctidsById(sctId);
            if (null != sctRec) {
                newSct = sctRec;
            } else {
                newSct = this.getFreeRecord(sctId);
            }
        }
        return newSct;
    }

    public Sctid getFreeRecord(String sctid) {
        var sctIdRecord = getNewRecord(sctid);
        sctIdRecord.put("status", modelsConstants.AVAILABLE);
        sctIdRecord.put("created_at", new Date());
        return bulkSctidService.insertSCTIDRecord(sctIdRecord);
    }

    public Map<String, Object> getNewRecord(String sctid) {
        Map<String, Object> sctIdRecord = new LinkedHashMap<>();
        sctIdRecord.put("sctid", sctid);
        sctIdRecord.put("sequence", sctIdHelper.getSequence(sctid));
        sctIdRecord.put("namespace", sctIdHelper.getNamespace(sctid));
        sctIdRecord.put("partitionId", sctIdHelper.getPartition(sctid));
        sctIdRecord.put("checkDigit", sctIdHelper.getCheckDigit(sctid));
        sctIdRecord.put("systemId", sctIdHelper.guid());
        return sctIdRecord;
    }

    public Sctid counterMode(SCTIDReservationRequest operation,String action) throws APIException {
        Sctid result = new Sctid();
       /* if (requestDto.equalsIgnoreCase("SCTIDRegistrationRequest"))
            operation = (SCTIDRegistrationRequest) operation;
        else if(requestDto.equalsIgnoreCase("SCTIDReservationRequest"))
            operation = (SCTIDReservationRequest) operation;*/
        Integer nextNumber = this.getNextNumber(operation);
        var newSCTId = computeSCTID(operation, nextNumber);
        Sctid sctIdRecord = this.getSctid(newSCTId);
        var newStatus = stateMachine.getNewStatus(sctIdRecord.getStatus(), action);
        if (null!=newStatus) {

           /* if (operation. && operation.systemId.trim() != "") {
                sctIdRecord.systemId = operation.systemId;
            }*/
            sctIdRecord.setStatus(newStatus);
            /*sctIdRecord.setAuthor(); = operation.author;
            sctIdRecord.setSoftware(); = operation.software;
            sctIdRecord.setExpirationDate(); = operation.expirationDate;
            sctIdRecord.setComment(); = operation.comment;

            sctIdRecord.setJobId(null);*/
            Sctid updatedRecord = sctidRepository.save(sctIdRecord);
            result = updatedRecord;
        } else {
            counterMode(operation, action);
        }
        return result;
    }

    public Integer getNextNumber(SCTIDReservationRequest operation) throws APIException {
       List<Partitions> partitionsList = partitionsRepository.findByNamespaceAndPartitionId(operation.getNamespace(),operation.getPartitionId());
        Integer nextNumber = ((partitionsList.get(0).getSequence()) +1);
        return nextNumber;
    }

    public String computeSCTID(SCTIDReservationRequest operation,Integer sequence){

        var tmpNsp=operation.getNamespace().toString();
        if (tmpNsp=="0"){
            tmpNsp="";
        }
        var base = sequence.toString() + tmpNsp + operation.getPartitionId();
        var SCTId = base + sctIdHelper.verhoeffCompute(base);
        return SCTId;

    }

    public Sctid counterMode(SctidsGenerateRequestDto operation, String action) throws APIException {
        Sctid result = new Sctid();
       /* if (requestDto.equalsIgnoreCase("SCTIDRegistrationRequest"))
            operation = (SCTIDRegistrationRequest) operation;
        else if(requestDto.equalsIgnoreCase("SCTIDReservationRequest"))
            operation = (SCTIDReservationRequest) operation;*/
        Integer nextNumber = this.getNextNumber(operation);
        var newSCTId = computeSCTID(operation, nextNumber);
        Sctid sctIdRecord = this.getSctid(newSCTId);
        var newStatus = stateMachine.getNewStatus(sctIdRecord.getStatus(), action);
        if (null!=newStatus) {

           /* if (operation. && operation.systemId.trim() != "") {
                sctIdRecord.systemId = operation.systemId;
            }*/
            sctIdRecord.setStatus(newStatus);
            /*sctIdRecord.setAuthor(); = operation.author;
            sctIdRecord.setSoftware(); = operation.software;
            sctIdRecord.setExpirationDate(); = operation.expirationDate;
            sctIdRecord.setComment(); = operation.comment;

            sctIdRecord.setJobId(null);*/
            Sctid updatedRecord = sctidRepository.save(sctIdRecord);
            result = updatedRecord;
        } else {
            counterMode(operation, action);
        }
        return result;
    }

    public Integer getNextNumber(SctidsGenerateRequestDto operation) throws APIException {
        List<Partitions> partitionsList = partitionsRepository.findByNamespaceAndPartitionId(operation.getNamespace(),operation.getPartitionId());
        Integer nextNumber = ((partitionsList.get(0).getSequence()) +1);
        return nextNumber;
    }

    public String computeSCTID(SctidsGenerateRequestDto operation,Integer sequence){

        var tmpNsp=operation.getNamespace().toString();
        if (tmpNsp=="0"){
            tmpNsp="";
        }
        var base = sequence.toString() + tmpNsp + operation.getPartitionId();
        var SCTId = base + sctIdHelper.verhoeffCompute(base);
        return SCTId;

    }
}
