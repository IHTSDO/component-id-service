package org.snomed.cis.service.DM;

import org.snomed.cis.domain.Partitions;
import org.snomed.cis.domain.PartitionsPk;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.SCTIDRegisterRequest;
import org.snomed.cis.dto.SCTIDReserveRequest;
import org.snomed.cis.dto.SctidGenerate;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.PartitionsRepository;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.service.BulkSctidService;
import org.snomed.cis.util.ModelsConstants;
import org.snomed.cis.util.SctIdHelper;
import org.snomed.cis.util.StateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public Sctid registerSctid(Object request, String requestDto) throws CisException {
        Sctid resultSct = null;
        //  SCTIDRegistrationRequest sctidRegistrationRequest = null;
        SCTIDRegisterRequest sctidRegisterRequest = null;
        if (requestDto.equalsIgnoreCase("SCTIDRegisterRequest"))
            sctidRegisterRequest = (SCTIDRegisterRequest) request;
        if (!(sctidRegisterRequest.isAutoSysId())) {
            List<Sctid> result = sctidRepository.findBySystemIdAndNamespace(sctidRegisterRequest.getSystemId(), sctidRegisterRequest.getNamespace());
            Sctid sct = result.get(0);
            if (!(sct.getSctid().equalsIgnoreCase(sctidRegisterRequest.getSctid()))) {
                throw new CisException(HttpStatus.BAD_REQUEST, "SystemId:" + sctidRegisterRequest.getSystemId() + " already exists with SctId:" + sct.getSctid());
            }
            if (sct.getStatus().equalsIgnoreCase(stateMachine.statuses.get("assigned"))) {
                resultSct = sct;
            } else {
                Sctid sctid = this.registerNewSctId(sctidRegisterRequest);
                if (null != sctid)
                    resultSct = sctid;
            }
        } else {
            Sctid sctid = this.registerNewSctId(sctidRegisterRequest);
            if (null != sctid)
                resultSct = sctid;
        }

        return resultSct;
    }

    public Sctid registerNewSctId(SCTIDRegisterRequest sctidRegistrationRequest) throws CisException {
        Sctid result = null;
        Sctid sctIdRecord = this.getSctid(sctidRegistrationRequest.getSctid());
        if (null != sctIdRecord) {
            var newStatus = stateMachine.getNewStatus(sctIdRecord.getStatus(), stateMachine.actions.get("register"));
            if (null != newStatus) {
                if (!sctidRegistrationRequest.getSystemId().isEmpty() && !sctidRegistrationRequest.getSystemId().isBlank()
                        && null != sctidRegistrationRequest.getSystemId()) {
                    sctIdRecord.setSystemId(sctidRegistrationRequest.getSystemId());
                }
                sctIdRecord.setStatus(newStatus);
                sctIdRecord.setAuthor(sctidRegistrationRequest.getAuthor());
                sctIdRecord.setSoftware(sctidRegistrationRequest.getSoftware());
                sctIdRecord.setExpirationDate(sctIdRecord.getExpirationDate());
                sctIdRecord.setComment(sctidRegistrationRequest.getComment());
                sctIdRecord.setJobId(null);
                sctIdRecord.setCreated_at(sctIdRecord.getCreated_at()!=null?sctIdRecord.getCreated_at():LocalDateTime.now());
                sctIdRecord.setModified_at(LocalDateTime.now());
                Sctid updatedRecord = sctidRepository.save(sctIdRecord);
                result = updatedRecord;
            } else {
                throw new CisException(HttpStatus.BAD_REQUEST, ("Cannot register SCTID:" + sctidRegistrationRequest.getSctid() + ", current status: " + sctIdRecord.getStatus()));
            }
        }
        return result;
    }

    public Sctid getSctid(String sctId) throws CisException {
        Sctid newSct = null;
        if (!sctIdHelper.validSCTId(sctId)) {
            throw new CisException(HttpStatus.BAD_REQUEST, "Not valid SCTID.");
        } else {
            //refactor Changes
            Optional<Sctid> sctid = sctidRepository.findById(sctId);
            Sctid sctRec = (sctid.isPresent()) ? sctid.get() : null;
            //refactor Changes
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

    //request body change
    // first parameter type chnage
    public Sctid counterMode(SCTIDReserveRequest operation, String action) throws CisException {
        Sctid result = new Sctid();
       /* if (requestDto.equalsIgnoreCase("SCTIDRegistrationRequest"))
            operation = (SCTIDRegistrationRequest) operation;
        else if(requestDto.equalsIgnoreCase("SCTIDReservationRequest"))
            operation = (SCTIDReservationRequest) operation;*/
        Integer nextNumber = this.getNextNumber(operation);
        var newSCTId = computeSCTID(operation, nextNumber);
        Sctid sctIdRecord = this.getSctid(newSCTId);
        var newStatus = stateMachine.getNewStatus(sctIdRecord.getStatus(), action);
        if (null != newStatus) {

            sctIdRecord.setStatus(newStatus);
            sctIdRecord.setAuthor(operation.getAuthor());
            sctIdRecord.setSoftware(operation.getSoftware());
            LocalDateTime expirationDateTime = null;
            if(!operation.getExpirationDate().isEmpty() && !operation.getExpirationDate().isBlank()
            && null != operation.getExpirationDate())
            {
                String str = operation.getExpirationDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                expirationDateTime = LocalDate.parse(str, formatter).atStartOfDay();
                sctIdRecord.setExpirationDate(expirationDateTime);
            }
            else
            {
                sctIdRecord.setExpirationDate(null);
            }
            sctIdRecord.setComment(operation.getComment());
            sctIdRecord.setJobId(null);
            sctIdRecord.setCreated_at(sctIdRecord.getCreated_at()!=null?sctIdRecord.getCreated_at():LocalDateTime.now());
            sctIdRecord.setModified_at(LocalDateTime.now());
            Sctid updatedRecord = sctidRepository.save(sctIdRecord);
            result = updatedRecord;
        }
        return result;
    }

    public Integer getNextNumber(SCTIDReserveRequest operation) throws CisException {
        Optional<Partitions> partitionsList = partitionsRepository.findById(new PartitionsPk(operation.getNamespace(), operation.getPartitionId()));
        Integer nextNumber = null;
        if (partitionsList.isPresent()) {
            nextNumber = ((partitionsList.get().getSequence()) + 1);
            partitionsList.get().setSequence(nextNumber);
            partitionsRepository.save(partitionsList.get());
        }
        return nextNumber;
    }
//requestbody change
    //first parmeter type change

    public String computeSCTID(SCTIDReserveRequest operation, Integer sequence) {

        var tmpNsp = operation.getNamespace().toString();
        if (tmpNsp.equalsIgnoreCase("0")) {
            tmpNsp = "";
        }
        var base = sequence.toString() + tmpNsp + operation.getPartitionId();
        var SCTId = base + sctIdHelper.verhoeffCompute(base);
        return SCTId;

    }

    public Sctid counterMode(SctidGenerate operation, String action) throws CisException {
        Sctid result = new Sctid();
       /* if (requestDto.equalsIgnoreCase("SCTIDRegistrationRequest"))
            operation = (SCTIDRegistrationRequest) operation;
        else if(requestDto.equalsIgnoreCase("SCTIDReservationRequest"))
            operation = (SCTIDReservationRequest) operation;*/
        Integer nextNumber = this.getNextNumber(operation);
        var newSCTId = computeSCTID(operation, nextNumber);
        Sctid sctIdRecord = this.getSctid(newSCTId);
        var newStatus = stateMachine.getNewStatus(sctIdRecord.getStatus(), action);
        if (null != newStatus) {

            if (!operation.getSystemId().isBlank() && !operation.getSystemId().isEmpty() && null != operation.getSystemId()) {
                sctIdRecord.setSystemId(operation.getSystemId());
            }
            sctIdRecord.setStatus(newStatus);
            sctIdRecord.setAuthor(operation.getAuthor());
            sctIdRecord.setSoftware(operation.getSoftware());
            sctIdRecord.setComment(operation.getComment());
            sctIdRecord.setJobId(null);
            sctIdRecord.setCreated_at(sctIdRecord.getCreated_at()!=null?sctIdRecord.getCreated_at():LocalDateTime.now());
            sctIdRecord.setModified_at(LocalDateTime.now());
            Sctid updatedRecord = sctidRepository.save(sctIdRecord);
            result = updatedRecord;
        }
        /*else {
            counterMode(operation, action);
        }*/
        return result;
    }

    public Integer getNextNumber(SctidGenerate operation) throws CisException {
        Optional<Partitions> partitionsList = partitionsRepository.findById(new PartitionsPk(operation.getNamespace(), operation.getPartitionId()));
        if (partitionsList.isPresent()) {
            Integer nextNumber = ((partitionsList.get().getSequence()) + 1);
            Partitions partitions = new Partitions(operation.getNamespace(), operation.getPartitionId(), nextNumber);
            Partitions partOutput = partitionsRepository.save(partitions);
            if (null != partOutput)
                return nextNumber;
            else
                return null;
        } else {
            return null;
        }
    }

    public String computeSCTID(SctidGenerate operation, Integer sequence) {

        var tmpNsp = operation.getNamespace().toString();
        if (tmpNsp.equalsIgnoreCase("0")) {
            tmpNsp = "";
        }
        var base = sequence.toString() + tmpNsp + operation.getPartitionId();
        var SCTId = base + sctIdHelper.verhoeffCompute(base);
        return SCTId;

    }
}
