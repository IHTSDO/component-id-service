package com.snomed.api.service;

import com.snomed.api.controller.SecurityController;
import com.snomed.api.controller.dto.NamespacePublicResponse;
import com.snomed.api.controller.dto.UserDTO;
import com.snomed.api.domain.Namespace;
import com.snomed.api.domain.Partitions;
import com.snomed.api.domain.PermissionsNamespace;
import com.snomed.api.exception.APIException;
import com.snomed.api.repository.NamespaceRepository;
import com.snomed.api.repository.PartitionsRepository;
import com.snomed.api.repository.PermissionsNamespaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@Service
public class NamespaceService {
    @Autowired
    public NamespaceRepository namespaceRepository;
    @Autowired
    private SecurityController securityController;

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

    public List<Namespace> getNamespaces() {
var sql = "Select namespace,organizationName,organizationAndContactDetails,dateIssued,email,notes  from namespace";
        Query genQuery = entityManager.createNativeQuery(sql,Namespace.class);
        List<Namespace> namespaceList = genQuery.getResultList();

        if (namespaceList != null) {
            //Collections.sort(namespaceList);
            //Collections.sort(namespaceList, Comparator.comparingInt(NamespacePublicResponse::getNamespace));
            Comparator<Namespace> comparator = new Comparator<Namespace>() {
                @Override
                public int compare(Namespace left, Namespace right) {
                    return left.getNamespace() - right.getNamespace(); // use your logic
                }
            };

            Collections.sort(namespaceList, comparator);
        }
        return namespaceList;
    }

    /*public Namespace createNamespace(Namespace namespace) throws APIException {
        if (authenticateToken()) return this.createNamespaces(namespace);
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }*/

    /*public Namespace createNamespaces(Namespace namespace) throws APIException {
        com.snomed.api.domain.BulkJob bulkJob = new com.snomed.api.domain.BulkJob();
        UserDTO userObj = this.getAuthenticatedUser();
        String namespaceString = namespace.getNamespace() + "";

        if (this.isAbletoEdit(*//*namespace.getNamespace()*//*Integer.valueOf("false"), userObj)) {
            if (namespaceString.length() != 7 && namespaceString != "0") {
                throw new APIException(HttpStatus.BAD_REQUEST, "Invalid namespace");
            } else {
                o / p = createNamespaceList(namespace);
            }


        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return namespace;
    }*/

    /*private Namespace createNamespaceList(Namespace namespace) {
        List<Partitions> partitionsOfObj = namespace.getPartitions();
        Partitions partitionsList = null;
        if (partitionsOfObj != null && !partitionsOfObj.isEmpty()) {
            partitionsList = (Partitions) partitionsOfObj;
        } else {
            if (namespace.getNamespace() == 0) {
                partitionsList.setNamespace(namespace);
                partitionsList.setPartitionId("00");
                partitionsList.setSequence(0);
                partitionsRepository.save(partitionsList);
                partitionsList.setNamespace(namespace);
                partitionsList.setPartitionId("01");
                partitionsList.setSequence(0);
                partitionsRepository.save(partitionsList);
                partitionsList.setNamespace(namespace);
                partitionsList.setPartitionId("02");
                partitionsList.setSequence(0);
                partitionsRepository.save(partitionsList);
            }//namespace=0
            else {
                partitionsList.setNamespace(namespace);
                partitionsList.setPartitionId("10");
                partitionsList.setSequence(0);
                partitionsRepository.save(partitionsList);
                partitionsList.setNamespace(namespace);
                partitionsList.setPartitionId("11");
                partitionsList.setSequence(0);
                partitionsRepository.save(partitionsList);
                partitionsList.setNamespace(namespace);
                partitionsList.setPartitionId("12");
                partitionsList.setSequence(0);
                partitionsRepository.save(partitionsList);

            }

        }
        return namespace;
    }*/

    private boolean isAbletoEdit(Integer namespace, UserDTO userObj) {
        boolean able = false;
        List<String> admins = Arrays.asList("a", "b", "c");
        for (String admin : admins) {
            if (admin.equalsIgnoreCase(userObj.getLogin())) {
                able = true;
            }
        }
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

    public Namespace updateNamespace(Namespace namespace) throws APIException {
        if (authenticateToken()) return this.updateNamespaces(namespace);
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public Namespace updateNamespaces(Namespace namespace) throws APIException {
        com.snomed.api.domain.BulkJob bulkJob = new com.snomed.api.domain.BulkJob();
        UserDTO userObj = this.getAuthenticatedUser();

        if (this.isAbletoEdit(namespace.getNamespace(), userObj)) {

            return (editNamespace(namespace.getNamespace(), namespace));

        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    private Namespace editNamespace(Integer namespaceId, Namespace namespaceObj) {

        /*
        * private Integer namespace;
    private String organizationName;
    private String organizationAndContactDetails;
    private Date dateIssued;
    private String email;
    private String notes;

    private String idPregenerate ;
partition
        * */
        Namespace namespace = null;
        namespace.setNamespace(namespaceObj.getNamespace());
        namespace.setOrganizationName(namespaceObj.getOrganizationName());
        namespace.setOrganizationAndContactDetails(namespaceObj.getOrganizationAndContactDetails());
        namespace.setDateIssued(namespaceObj.getDateIssued());
        namespace.setEmail(namespaceObj.getEmail());
        namespace.setNotes(namespaceObj.getNotes());
        //namespace.setIdPregenerate(namespaceObj.getIdPregenerate());
        namespaceRepository.save(namespace);

        return namespace;
    }


    public List<Namespace> getNamespacesForUser(String username) throws APIException {
        if (authenticateToken()) return this.getNamespacesListForUser(username);
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public List<Namespace> getNamespacesListForUser(String username) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        List<String> roleAsGroups;
        List<Namespace> namespaceList;
        List<String> namespacesFromGroup = new ArrayList<>();
        List<String> namespaces = new ArrayList<>();
        List<String> otherGroup = new ArrayList<>();

        try {
            roleAsGroups = userObj.getRoles();
        } catch (Exception e) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Error accessing groups");
        }

        if (roleAsGroups != null && !roleAsGroups.isEmpty()) {
            for (String group : roleAsGroups) {
                if ("namespace".equalsIgnoreCase(group.substring(0, group.indexOf("-")))) {
                    namespacesFromGroup.add(group.substring(group.indexOf("-") + 1));
                } else {
                    otherGroup.add(group);
                }
            }//for
        }
        otherGroup.add(username);
        List<PermissionsNamespace> permissionsNamespaceList = permissionsNamespaceRepository.findByUsername(String.valueOf(otherGroup));
        if (namespacesFromGroup != null && !namespacesFromGroup.isEmpty()) {
            namespaces = namespacesFromGroup;
        }
        for (PermissionsNamespace perm : permissionsNamespaceList) {
            if (namespaces.indexOf(perm.getNamespace()) == -1) namespaces.add(String.valueOf(perm.getNamespace()));
        }
        namespaceList = namespaceRepository.findByNamespace(Integer.valueOf(namespaces.toString()));
        if (namespaceList != null) {
            Collections.sort(namespaceList, Comparator.comparingInt(Namespace::getNamespace));
        }
        return namespaceList;
    }
}//class
