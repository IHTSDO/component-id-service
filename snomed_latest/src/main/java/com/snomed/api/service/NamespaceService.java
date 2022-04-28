package com.snomed.api.service;

import com.google.common.collect.Lists;
import com.snomed.api.controller.SecurityController;
import com.snomed.api.controller.dto.*;
import com.snomed.api.domain.Namespace;
import com.snomed.api.domain.Partitions;
import com.snomed.api.domain.PartitionsPk;
import com.snomed.api.domain.PermissionsNamespace;
import com.snomed.api.exception.APIException;
import com.snomed.api.repository.NamespaceRepository;
import com.snomed.api.repository.PartitionsRepository;
import com.snomed.api.repository.PermissionsNamespaceRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NamespaceService {
    @Autowired
    public NamespaceRepository namespaceRepository;

    @Autowired
    AuthenticateToken authenticateToken;

    @Autowired
    private SecurityController securityController;
    @Autowired
    BulkSctidService bulkSctidService;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private PermissionsNamespaceRepository permissionsNamespaceRepository;

    @Autowired
    private PartitionsRepository partitionsRepository;


    public boolean authenticateToken() throws APIException {
        UserDTO obj = this.securityController.authenticate();
        if (null != obj) return true;
        else return false;
    }

    public UserDTO getAuthenticatedUser() throws APIException {
        return this.securityController.authenticate();
    }

    public List<Namespace> getNamespaces(String token) throws APIException {
        if (bulkSctidService.authenticateToken(token)) return this.getNamespaceslist();
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public List<Namespace> getNamespaceslist() {

        Namespace namespace = null;
        List<NamespaceDto> namespaceDtoList = new ArrayList<>();
        NamespaceDto namespaceDto = new NamespaceDto();
        PartitionsDto partitionsDto = null;
        List<PartitionsDto> partitionsDtoList = new ArrayList<>();
        List<Partitions> partitionsList = partitionsRepository.findAll();
        List<Namespace> namespaceList = namespaceRepository.findAll();
        //namespaceGet

        for (Partitions partitions : partitionsList) {
            partitionsDto = new PartitionsDto(partitions.namespace.getNamespace(), partitions.getPartitionId(), partitions.getSequence());
            partitionsDtoList.add(partitionsDto);
        }
        namespaceDto.setPartitions(partitionsDtoList);

        for (Namespace namespace1 : namespaceList) {
            namespaceDto = new NamespaceDto(namespace1.getNamespace(), namespace1.getOrganizationName(), namespace1.getOrganizationAndContactDetails(), namespace1.getDateIssued(),
                    namespace1.getEmail(), namespace1.getNotes(), namespace1.getIdPregenerate(), partitionsDtoList);
            namespaceDtoList.add(namespaceDto);

        }
        //  namespace.setPartitions( partitionsList);


        if (namespaceDto != null) {
            //Collections.sort(namespaceList);
            Collections.sort(namespaceDtoList, Comparator.comparingInt(NamespaceDto::getNamespace));
        }

        return namespaceList;

    }

    public String createNamespace(String token, Namespace namespace) throws APIException {
        if (bulkSctidService.authenticateToken(token)) return this.createNamespaces(token, namespace);
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public String createNamespaces(String token, Namespace namespace) throws APIException {
        com.snomed.api.domain.BulkJob bulkJob = new com.snomed.api.domain.BulkJob();
        UserDTO userObj = this.getAuthenticatedUser();
        String namespaceString = namespace.getNamespace() + "";
NamespaceDto output = new NamespaceDto();
        if (this.isAbletoEdit(namespace.getNamespace(), userObj)) {
            if (namespaceString.length() != 7 && namespaceString != "0") {
                 throw new APIException(HttpStatus.BAD_REQUEST, "Invalid namespace");
            } else {
                output = createNamespaceList(namespace);
            }


        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        if(null!=output.getNamespace() && output.getPartitions().size()>0)
            return "Success";
        else
            return "Record Not Created";
    }

    private NamespaceDto createNamespaceList(Namespace namespace) {
        NamespaceDto output = new NamespaceDto();
        List<Partitions> partitionsOfObj = namespace.getPartitions();
        List<PartitionsDto> partitionsDtoList = new ArrayList<>();
        PartitionsDto partitionsDto = new PartitionsDto();
        List<Partitions> partitionsList = new ArrayList<>();
    Namespace createdNamespace = namespaceRepository.save(namespace);
        output.setNamespace(createdNamespace.getNamespace());
        output.setOrganizationName(createdNamespace.getOrganizationName());
        output.setOrganizationAndContactDetails(createdNamespace.getOrganizationAndContactDetails());
        output.setDateIssued(createdNamespace.getDateIssued());
        output.setEmail(createdNamespace.getEmail());
        output.setNotes(createdNamespace.getNotes());
        output.setIdPregenerate(createdNamespace.getIdPregenerate());
     if (partitionsOfObj != null && !partitionsOfObj.isEmpty()) {
            partitionsList = partitionsOfObj;
            /*for(PartitionsDto part:partitionsOfObj){
               // part.setNamespace(namespace1);
            }*/
        }
        else {
            if (namespace.getNamespace() == 0) {
                // PartitionsPk partitionsPk=new PartitionsPk(namespace.getNamespace(),"00");
                Partitions partitionsObj1 = new Partitions(null, "00", 0);
                partitionsList.add(0, partitionsObj1);
                //partitionsRepository.inserWithQuery(namespace, "00", 0);
                Partitions p1 = partitionsRepository.save(partitionsObj1);
PartitionsDto p1Dto = new PartitionsDto();
p1Dto.setNamespace(p1.getNamespace().getNamespace());
p1Dto.setPartitionId(p1.getPartitionId());
p1Dto.setSequence(p1.getSequence());
                Partitions partitionsObj2 = new Partitions(null, "01", 0);
                partitionsList.add(1, partitionsObj2);
             //   partitionsRepository.inserWithQuery(namespace, "01", 0);
                Partitions p2 = partitionsRepository.save(partitionsObj2);
                PartitionsDto p2Dto = new PartitionsDto();
                p2Dto.setNamespace(p2.getNamespace().getNamespace());
                p2Dto.setPartitionId(p2.getPartitionId());
                p2Dto.setSequence(p2.getSequence());

                Partitions partitionsObj3 = new Partitions(null, "02", 0);
                partitionsList.add(2, partitionsObj3);
               // partitionsRepository.inserWithQuery(namespace, "02", 0);
                Partitions p3 = partitionsRepository.save(partitionsObj3);
                PartitionsDto p3Dto = new PartitionsDto();
                p3Dto.setNamespace(p3.getNamespace().getNamespace());
                p3Dto.setPartitionId(p3.getPartitionId());
                p3Dto.setSequence(p3.getSequence());
                partitionsDtoList.add(p1Dto);
                partitionsDtoList.add(p2Dto);
                partitionsDtoList.add(p3Dto);
            }//namespace=0
            else {

                Partitions partitionsObj1 = new Partitions(null, "10", 0);
                partitionsList.add(0, partitionsObj1);
                Partitions p1 = partitionsRepository.save(partitionsObj1);
                PartitionsDto p1Dto = new PartitionsDto();
                p1Dto.setNamespace(p1.getNamespace().getNamespace());
                p1Dto.setPartitionId(p1.getPartitionId());
                p1Dto.setSequence(p1.getSequence());

                Partitions partitionsObj2 = new Partitions(null, "11", 0);
                partitionsList.add(1, partitionsObj2);
                Partitions p2 = partitionsRepository.save(partitionsObj2);
                PartitionsDto p2Dto = new PartitionsDto();
                p2Dto.setNamespace(p2.getNamespace().getNamespace());
                p2Dto.setPartitionId(p2.getPartitionId());
                p2Dto.setSequence(p2.getSequence());

                Partitions partitionsObj3 = new Partitions(null, "12", 0);
                partitionsList.add(2, partitionsObj3);
                Partitions p3 = partitionsRepository.save(partitionsObj3);
                PartitionsDto p3Dto = new PartitionsDto();
                p3Dto.setNamespace(p3.getNamespace().getNamespace());
                p3Dto.setPartitionId(p3.getPartitionId());
                p3Dto.setSequence(p3.getSequence());
                partitionsDtoList.add(p1Dto);
                partitionsDtoList.add(p2Dto);
                partitionsDtoList.add(p3Dto);
            }
output.setPartitions(partitionsDtoList);
        }
       /* namespaceRepository.insertWithQuery(namespace.getNamespace(), namespace.getOrganizationName(), namespace.getOrganizationAndContactDetails(),
                namespace.getDateIssued(), namespace.getEmail(), namespace.getNotes(), namespace.getIdPregenerate(), partitionsOfObj);
       */

        return output;
    }

    private boolean isAbletoEdit(Integer namespace, UserDTO userObj) throws APIException {
        List<String> groups = authenticateToken.getGroupsList();
        boolean able = false;
        for(String group:groups)
        {
            if(group.equalsIgnoreCase("component-identifier-service-admin"))
            {
                able = true;
            }
        }
      /*  List<String> admins = Arrays.asList("a", "b", "c");
        for (String admin : admins) {
            if (admin.equalsIgnoreCase(userObj.getLogin())) {
                able = true;
            }
        }*/
        if (!able) {
            if (!String.valueOf(namespace).equalsIgnoreCase("false")) {
                List<PermissionsNamespace> permissionsNamespaceList = permissionsNamespaceRepository.findByNamespace(Integer.valueOf(namespace));
                //

                for (PermissionsNamespace perm : permissionsNamespaceList) {
                    if (("manager").equalsIgnoreCase(perm.getRole()) && (perm.getUsername().equalsIgnoreCase(userObj.getFirstName()))) {
                        able = true;
                    }
                }
                //
            } else {
                return able;
            }

        } else {
            return able;
        }
        return able;
    }//isAbleToEdit

    public Namespace updateNamespace(String token, Namespace namespace) throws APIException {
        if (bulkSctidService.authenticateToken(token)) return this.updateNamespaces(token, namespace);
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public Namespace updateNamespaces(String token, Namespace namespace) throws APIException {
        com.snomed.api.domain.BulkJob bulkJob = new com.snomed.api.domain.BulkJob();
        UserDTO userObj = this.getAuthenticatedUser();

        if (this.isAbletoEdit(namespace.getNamespace(), userObj)) {

            return (editNamespace(namespace.getNamespace(), namespace));

        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    private Namespace editNamespace(Integer namespaceId, Namespace namespaceObj) {

        NamespaceDto namespacesObj = new NamespaceDto();
        List<Partitions> partitionsList = null;
        PartitionsDto partitionsDto = null;
        List<PartitionsDto> partitionsDtoList = new ArrayList<>();

        Namespace namespace = new Namespace();
        Optional<Namespace> namespaces = namespaceRepository.findById(namespaceId);
        Namespace namespaceGet = namespaces.get();


        namespaceGet.setOrganizationName(namespaceObj.getOrganizationName());
        namespaceGet.setOrganizationAndContactDetails(namespaceObj.getOrganizationName());
        namespaceGet.setDateIssued(namespaceObj.getDateIssued());
        namespaceGet.setEmail(namespaceObj.getEmail());
        namespaceGet.setNotes(namespaceObj.getNotes());
        namespaceGet.setIdPregenerate(namespaceObj.getIdPregenerate());

        namespace=namespaceRepository.save(namespaceGet);
        return namespace;
    }


    public List<Namespace> getNamespacesForUser(String token, String userName) throws APIException {
        if (authenticateToken()) return this.getNamespacesListForUser(token, userName);
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public List<Namespace> getNamespacesListForUser(String token, String userName) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        List<String> roleAsGroups;
        List<Namespace> namespaceList;
        List<String> namespacesFromGroup = new ArrayList<>();
        List<String> namespaces = new ArrayList<>();
        List<String> otherGroup = new ArrayList<>();

        List<String> groups = securityController.getUserGroup(userName, token);

        if (groups != null && !groups.isEmpty()) {
            for (String group : groups) {
                if ("namespace".equalsIgnoreCase(group.substring(0, group.indexOf("-")))) {
                    namespacesFromGroup.add(group.substring(group.indexOf("-") + 1));
                } else {
                    otherGroup.add(group);
                }
            }//for
        }
        otherGroup.add(userName);
        List<PermissionsNamespace> permissionsNamespaceList = permissionsNamespaceRepository.findByUsernameIn(otherGroup/*"srs-dev"*/);
        if (namespacesFromGroup != null && !namespacesFromGroup.isEmpty()) {
            namespaces = namespacesFromGroup;
        }
        for (PermissionsNamespace perm : permissionsNamespaceList) {
            if (namespaces.indexOf(perm.getNamespace()) == -1) namespaces.add(String.valueOf(perm.getNamespace()));
        }
        // Convert List of String to list of Integer
        List<Integer> namespacesIntList = convertStringListToIntList(
                namespaces,
                Integer::parseInt);
        namespaceList = namespaceRepository.findByNamespaceIn(namespacesIntList);
        //othergroups value  array find
        //using findbynamespaceIN
        // namespaceList = namespaceRepository.findByNamespace(0);
        if (namespaceList != null) {
            Collections.sort(namespaceList, Comparator.comparingInt(Namespace::getNamespace));
        }
        return namespaceList;
    }

    public static <T, U> List<U>
    convertStringListToIntList(List<T> listOfString,
                               Function<T, U> function) {
        return listOfString.stream()
                .map(function)
                .collect(Collectors.toList());
    }

    public NamespaceDto getNamespace(String token, String namespaceId) throws APIException {
        if (bulkSctidService.authenticateToken(token)) return this.getNamespaceId(token, namespaceId);
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    @Transactional
    public NamespaceDto getNamespaceId(String token, String namespaceId) {
        // List<Namespace> namespaceList = null;
        NamespaceDto namespacesObj = new NamespaceDto();
        List<Partitions> partitionsList = null;
        PartitionsDto partitionsDto = null;
        List<PartitionsDto> partitionsDtoList = new ArrayList<>();
        //namespaceList = namespaceRepository.findByNamespace(Integer.parseInt(namespaceId));
        Optional<Namespace> namespaces = namespaceRepository.findById(Integer.parseInt(namespaceId));
        Namespace namespaceGet = namespaces.get();
        partitionsList = partitionsRepository.findByNamespace(Integer.parseInt(namespaceId));

        for (Partitions partitions : partitionsList) {
            partitionsDto = new PartitionsDto(partitions.namespace.getNamespace(), partitions.getPartitionId(), partitions.getSequence());
            partitionsDtoList.add(partitionsDto);
        }
        namespacesObj.setNamespace(namespaceGet.getNamespace());
        namespacesObj.setOrganizationName(namespaceGet.getOrganizationName());
        namespacesObj.setOrganizationAndContactDetails(namespaceGet.getOrganizationAndContactDetails());
        namespacesObj.setDateIssued(namespaceGet.getDateIssued());
        namespacesObj.setEmail(namespaceGet.getEmail());
        namespacesObj.setNotes(namespaceGet.getNotes());
        namespacesObj.setIdPregenerate(namespaceGet.getIdPregenerate());
        namespacesObj.setPartitions(partitionsDtoList);
        return namespacesObj;

    }


    public String deleteNamespace(String token, String namespaceId) throws APIException {
        boolean a = true;
        if (bulkSctidService.authenticateToken(token)) return this.deleteNamespaces(token, namespaceId);
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public String deleteNamespaces(String token, String namespaceId) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        JSONObject response = new JSONObject();

        if (this.isAbletoEdit(Integer.valueOf(namespaceId), userObj)) {
            Optional<Namespace> namespaceObj = namespaceRepository.findById(Integer.valueOf(namespaceId));
            if (namespaceObj.isPresent()) {
                namespaceRepository.delete(namespaceObj.get());
            }
            response.put("message", "Success");
            return response.toString();

        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }


    public Namespace updatePartitionSequence(String token, String namespaceId, String partitionId, String value) throws APIException {
        if (bulkSctidService.authenticateToken(token))
            return this.updatePartitionSequences(token, namespaceId, partitionId, value);
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public Namespace updatePartitionSequences(String token, String namespaceId, String partitionId, String value) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
Integer namespaceIdint= Integer.valueOf(namespaceId);
        if (this.isAbletoEdit(Integer.valueOf(namespaceId), userObj)) {

            Optional<Partitions> partitions = partitionsRepository.findById(new PartitionsPk(namespaceIdint, partitionId));
            partitions.get().setSequence(Integer.parseInt(value));
            partitionsRepository.save(partitions.get());
            return partitions.get().getNamespace();
        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }

    }

}//class
