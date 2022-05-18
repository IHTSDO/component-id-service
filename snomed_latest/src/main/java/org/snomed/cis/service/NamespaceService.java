package org.snomed.cis.service;

import org.json.JSONObject;
import org.snomed.cis.controller.SecurityController;
import org.snomed.cis.controller.dto.NamespaceDto;
import org.snomed.cis.controller.dto.PartitionsDto;
import org.snomed.cis.controller.dto.UserDTO;
import org.snomed.cis.domain.Namespace;
import org.snomed.cis.domain.Partitions;
import org.snomed.cis.domain.PartitionsPk;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.NamespaceRepository;
import org.snomed.cis.repository.PartitionsRepository;
import org.snomed.cis.repository.PermissionsNamespaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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


    public boolean authenticateToken() throws CisException {
        UserDTO obj = this.securityController.authenticate();
        if (null != obj) return true;
        else return false;
    }

    public UserDTO getAuthenticatedUser() throws CisException {
        return this.securityController.authenticate();
    }

    public List<NamespaceDto> getNamespaces(String token) throws CisException {
        if (bulkSctidService.authenticateToken(token)) return this.getNamespaceslist();
        else throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
    }

    public List<NamespaceDto> getNamespaceslist() {

        Namespace namespace = null;
        List<NamespaceDto> namespaceDtoList = new ArrayList<>();
        NamespaceDto namespaceDto = new NamespaceDto();
        PartitionsDto partitionsDto = null;
        List<PartitionsDto> partitionsDtoList = new ArrayList<>();
        List<Partitions> partitionsList = partitionsRepository.findAll();
        List<Namespace> namespaceList = namespaceRepository.findAll();
        for (int i = 0; i < namespaceList.size(); i++) {
            Namespace namespace1 = namespaceList.get(i);
            Integer namespace1Namespace = namespace1.getNamespace();
            NamespaceDto dto = new NamespaceDto();
            dto.setNamespace(namespace1.getNamespace());
            dto.setOrganizationName(namespace1.getOrganizationName());
            dto.setOrganizationAndContactDetails(namespace1.getOrganizationAndContactDetails());
            dto.setEmail(namespace1.getEmail());
            dto.setNotes(namespace1.getNotes());
            dto.setDateIssued(String.valueOf(namespace1.getDateIssued()));
            dto.setIdPregenerate(namespace1.getIdPregenerate());
            List<Partitions> partList = new ArrayList<>();
            for (int j = 0; j < partitionsList.size(); j++) {
                int partNamespace = partitionsList.get(j).getNamespace();
                if (namespace1Namespace.equals(partNamespace)) {
                    partList.add(partitionsList.get(j));
                }
            }
            dto.setPartitions(partList);
            namespaceDtoList.add(dto);
        }
//keerthika commenting
       /* for (Partitions partitions : partitionsList) {
            partitionsDto = new PartitionsDto(partitions.getNamespace().getNamespace(), partitions.getPartitionId(), partitions.getSequence());
            partitionsDtoList.add(partitionsDto);
        }
        namespaceDto.setPartitions(partitionsDtoList);

        for (Namespace namespace1 : namespaceList) {
            namespaceDto = new NamespaceDto(namespace1.getNamespace(), namespace1.getOrganizationName(), namespace1.getOrganizationAndContactDetails(), namespace1.getDateIssued().toString(),
                    namespace1.getEmail(), namespace1.getNotes(), namespace1.getIdPregenerate(), partitionsDtoList);
            namespaceDtoList.add(namespaceDto);

        }*/
        //keerthika commenting
        //  namespace.setPartitions( partitionsList);


        if (namespaceDtoList.size() > 0) {
            //Collections.sort(namespaceDtoList);
            Collections.sort(namespaceDtoList, Comparator.comparingInt(NamespaceDto::getNamespace));
        }

        return namespaceDtoList;

    }

    public String createNamespace(String token, NamespaceDto namespace) throws CisException, ParseException {
        if (bulkSctidService.authenticateToken(token)) return this.createNamespaces(token, namespace);
        else throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
    }

    public String createNamespaces(String token, NamespaceDto namespace) throws CisException, ParseException {
        UserDTO userObj = this.getAuthenticatedUser();
        String namespaceString = namespace.getNamespace() + "";
        NamespaceDto output = new NamespaceDto();
        if (this.isAbletoEdit(namespace.getNamespace(), userObj)) {
            if (namespaceString.length() != 7 && !namespaceString.equalsIgnoreCase("0")) {
                throw new CisException(HttpStatus.BAD_REQUEST, "Invalid namespace");
            } else {
                return createNamespaceList(namespace);
            }
        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    private String createNamespaceList(NamespaceDto namespace) throws ParseException {
        NamespaceDto output = new NamespaceDto();
        JSONObject response = new JSONObject();
        List<Partitions> partitionsOfObj = namespace.getPartitions();
        //List<Partitions> partitionsList = new ArrayList<>();
        PartitionsDto partitionsDto = new PartitionsDto();
        List<Partitions> partitionsList = new ArrayList<>();
        /*output.setNamespace(createdNamespace.getNamespace());
        output.setOrganizationName(createdNamespace.getOrganizationName());
        output.setOrganizationAndContactDetails(createdNamespace.getOrganizationAndContactDetails());
        output.setDateIssued(createdNamespace.getDateIssued().toString());
        output.setEmail(createdNamespace.getEmail());
        output.setNotes(createdNamespace.getNotes());
        output.setIdPregenerate(createdNamespace.getIdPregenerate());
        output.setNamespace(createdNamespace);*/
        if (partitionsOfObj != null && !partitionsOfObj.isEmpty() && partitionsOfObj.size() > 0) {
            partitionsList = partitionsOfObj;
        } else {
            if (namespace.getNamespace() == 0) {
                // PartitionsPk partitionsPk=new PartitionsPk(namespace.getNamespace(),"00");
                Partitions partitionsObj1 = new Partitions(namespace.getNamespace(), "00", 0);
                partitionsList.add(partitionsObj1);
                //partitionsRepository.inserWithQuery(namespace, "00", 0);
                //  Partitions p1 = partitionsRepository.save(partitionsObj1);
                Partitions partitionsObj2 = new Partitions(namespace.getNamespace(), "01", 0);
                partitionsList.add(partitionsObj2);
                //   partitionsRepository.inserWithQuery(namespace, "01", 0);
                //Partitions p2 = partitionsRepository.save(partitionsObj2);

                Partitions partitionsObj3 = new Partitions(namespace.getNamespace(), "02", 0);
                partitionsList.add(partitionsObj3);
                // partitionsRepository.inserWithQuery(namespace, "02", 0);
                //Partitions p3 = partitionsRepository.save(partitionsObj3);

            }//namespace=0
            else {

                Partitions partitionsObj1 = new Partitions(namespace.getNamespace(), "10", 0);
                partitionsList.add(partitionsObj1);
                //Partitions p1 = partitionsRepository.save(partitionsObj1);

                Partitions partitionsObj2 = new Partitions(namespace.getNamespace(), "11", 0);
                partitionsList.add(partitionsObj2);
                //Partitions p2 = partitionsRepository.save(partitionsObj2);

                Partitions partitionsObj3 = new Partitions(namespace.getNamespace(), "12", 0);
                partitionsList.add(partitionsObj3);
                //Partitions p3 = partitionsRepository.save(partitionsObj3);
            }
        }
       /* namespaceRepository.insertWithQuery(namespace.getNamespace(), namespace.getOrganizationName(), namespace.getOrganizationAndContactDetails(),
                namespace.getDateIssued(), namespace.getEmail(), namespace.getNotes(), namespace.getIdPregenerate(), partitionsOfObj);
       */
        //SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm:ss a",Locale.ENGLISH);
        List<Partitions> part = partitionsRepository.saveAll(partitionsList);
        Namespace toBeCreated = new Namespace();
        toBeCreated.setNamespace(namespace.getNamespace());
        toBeCreated.setOrganizationName(namespace.getOrganizationName());
        toBeCreated.setOrganizationAndContactDetails(namespace.getOrganizationAndContactDetails());
        toBeCreated.setDateIssued(null != namespace.getDateIssued() ? LocalDateTime.parse(namespace.getDateIssued(), dateTimeFormatter) : null);
        toBeCreated.setEmail(namespace.getEmail());
        toBeCreated.setNotes(namespace.getNotes());
        toBeCreated.setIdPregenerate(namespace.getIdPregenerate());
        Namespace createdNamespace = namespaceRepository.save(toBeCreated);
        if (part.size() > 0 && (createdNamespace.getNamespace().equals(namespace.getNamespace())))
            response.put("message", "Success");
        return response.toString();
    }

    private boolean isAbletoEdit(Integer namespace, UserDTO userObj) throws CisException {
        List<String> groups = authenticateToken.getGroupsList();
        boolean able = false;
        for (String group : groups) {
            if (group.equalsIgnoreCase("component-identifier-service-admin")) {
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

    public String updateNamespace(String token, NamespaceDto namespace) throws Exception {
        if (bulkSctidService.authenticateToken(token)) return this.updateNamespaces(token, namespace);
        else throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
    }

    public String updateNamespaces(String token, NamespaceDto namespace) throws Exception {
        UserDTO userObj = this.getAuthenticatedUser();

        if (this.isAbletoEdit(namespace.getNamespace(), userObj)) {

            return (editNamespace(namespace.getNamespace(), namespace));

        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    private String editNamespace(Integer namespaceId, NamespaceDto namespaceObj) throws Exception {

        NamespaceDto namespacesObj = new NamespaceDto();
        List<Partitions> partitionsList = null;
        PartitionsDto partitionsDto = null;
        List<PartitionsDto> partitionsDtoList = new ArrayList<>();
        //SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm:ss a",Locale.ENGLISH);
        JSONObject response = new JSONObject();

        Namespace namespace = new Namespace();
        Optional<Namespace> namespaces = namespaceRepository.findById(namespaceId);
        Namespace namespaceGet = namespaces.get();


        namespaceGet.setOrganizationName(namespaceObj.getOrganizationName());
        namespaceGet.setOrganizationAndContactDetails(namespaceObj.getOrganizationAndContactDetails());
        namespaceGet.setDateIssued(null != namespaceObj.getDateIssued() ? LocalDateTime.parse(namespaceObj.getDateIssued(),dateTimeFormatter) : null);
        namespaceGet.setEmail(namespaceObj.getEmail());
        namespaceGet.setNotes(namespaceObj.getNotes());
        namespaceGet.setIdPregenerate(namespaceObj.getIdPregenerate());

       /* namespaceGet.setOrganizationName("test");
        namespaceGet.setOrganizationAndContactDetails("test");
        namespaceGet.setDateIssued(null);
        namespaceGet.setEmail("keerth.san@gmail.com");
        namespaceGet.setNotes("test");
        namespaceGet.setIdPregenerate(null);*/
        try {
            namespace = namespaceRepository.save(namespaceGet);
            response.put("message", "Success");
        } catch (Exception e) {
            throw new Exception(String.valueOf(response.put("message", e.getMessage())));
        }
        return response.toString();
    }


    public List<Namespace> getNamespacesForUser(String token, String userName) throws CisException {
        if (authenticateToken()) return this.getNamespacesListForUser(token, userName);
        else throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
    }

    public List<Namespace> getNamespacesListForUser(String token, String userName) throws CisException {
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
        List<PermissionsNamespace> permissionsNamespaceList = permissionsNamespaceRepository.findByUsernameIn(otherGroup);
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

    public NamespaceDto getNamespace(String token, String namespaceId) throws CisException {
        if (bulkSctidService.authenticateToken(token)) return this.getNamespaceId(token, namespaceId);
        else throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
    }

    @Transactional
    public NamespaceDto getNamespaceId(String token, String namespaceId) throws CisException {
        JSONObject response = new JSONObject();
        if (namespaceId.length() != 7 && !(namespaceId.equalsIgnoreCase("0")))
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid namespace");
        NamespaceDto namespacesObj = new NamespaceDto();
        List<Partitions> partitionsList = null;
        PartitionsDto partitionsDto = null;
        List<PartitionsDto> partitionsDtoList = new ArrayList<>();
        //namespaceList = namespaceRepository.findByNamespace(Integer.parseInt(namespaceId));
        Optional<Namespace> namespaces = namespaceRepository.findById(Integer.parseInt(namespaceId));
        if (namespaces.isPresent()) {
            Namespace namespaceGet = namespaces.get();
            partitionsList = partitionsRepository.findByNamespace(Integer.parseInt(namespaceId));

      /*  for (Partitions partitions : partitionsList) {
            partitionsDto = new PartitionsDto(partitions.getNamespace(), partitions.getPartitionId(), partitions.getSequence());
            partitionsDtoList.add(partitionsDto);
        }*/
            // namespacesObj.setNamespace(namespaceGet);
            namespacesObj.setNamespace(namespaceGet.getNamespace());
            namespacesObj.setOrganizationName(namespaceGet.getOrganizationName());
            namespacesObj.setOrganizationAndContactDetails(namespaceGet.getOrganizationAndContactDetails());
            namespacesObj.setDateIssued(null != namespaceGet.getDateIssued() ? namespaceGet.getDateIssued().toString() : null);
            namespacesObj.setEmail(namespaceGet.getEmail());
            namespacesObj.setNotes(namespaceGet.getNotes());
            namespacesObj.setIdPregenerate(namespaceGet.getIdPregenerate());
            namespacesObj.setPartitions(partitionsList);
        } else {
            namespacesObj = null;
        }
        return namespacesObj;

    }


    public String deleteNamespace(String token, String namespaceId) throws CisException {
        if (bulkSctidService.authenticateToken(token)) return this.deleteNamespaces(token, namespaceId);
        else throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
    }

    public String deleteNamespaces(String token, String namespaceId) throws CisException {
        UserDTO userObj = this.getAuthenticatedUser();
        JSONObject response = new JSONObject();
        if (namespaceId.length() != 7 && namespaceId != "0")
            response.put("message", "Invalid Namespace");
        else {
            int deletedRows = 0;
            if (this.isAbletoEdit(Integer.valueOf(namespaceId), userObj)) {
                Optional<Namespace> namespaceObj = namespaceRepository.findById(Integer.valueOf(namespaceId));
                if (namespaceObj.isPresent()) {
                    namespaceRepository.delete(namespaceObj.get());
                }
                List<Partitions> partNamespace = partitionsRepository.findByNamespace(Integer.valueOf(namespaceId));
                if (partNamespace.size() > 0)
                    partitionsRepository.deleteAll(partNamespace);
                response.put("message", "Success");
            } else {
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

            }
        }
        return response.toString();
    }


    public String updatePartitionSequence(String token, String namespaceId, String partitionId, String value) throws CisException {
        if (bulkSctidService.authenticateToken(token))
            return this.updatePartitionSequences(token, namespaceId, partitionId, value);
        else throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
    }

    public String updatePartitionSequences(String token, String namespaceId, String partitionId, String value) throws CisException {
        UserDTO userObj = this.getAuthenticatedUser();
        String out = "";
        Integer namespaceIdint = Integer.valueOf(namespaceId);
        JSONObject response = new JSONObject();
        if (this.isAbletoEdit(Integer.valueOf(namespaceId), userObj)) {

            Optional<Partitions> partitions = partitionsRepository.findById(new PartitionsPk(namespaceIdint, partitionId));
            partitions.get().setSequence(Integer.parseInt(value));
            Partitions partResult = partitionsRepository.save(partitions.get());
            if ((partResult.getSequence()).equals(Integer.parseInt(value)))
                response.put("message", "Success");
        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return response.toString();
    }

    /* Method for "/sct/namespaces/{namespaceId}/permissions" */
    public List<PermissionsNamespace> getPermissionForNS(String token, String namespaceId) throws CisException {
        List<PermissionsNamespace> permissionsNamespaceListFinal;
        if (bulkSctidService.authenticateToken(token)) {
            List<PermissionsNamespace> permissionsNamespaceList = permissionsNamespaceRepository.findByNamespace(Integer.valueOf(namespaceId));
            if (permissionsNamespaceList.size() > 0)
                permissionsNamespaceListFinal = permissionsNamespaceList;
            else
                permissionsNamespaceListFinal = Collections.EMPTY_LIST;
        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return permissionsNamespaceListFinal;
    }

    /**
     * method for Authorization Controller
     */
    public String deleteNamespacePermissions(String token, String namespaceId, String username) throws CisException {
        if (bulkSctidService.authenticateToken(token)) {
            UserDTO userObj = this.getAuthenticatedUser();
            if (this.isAbletoEdit(Integer.valueOf(namespaceId), userObj)) {
                JSONObject response = new JSONObject();
                long deletedCount = permissionsNamespaceRepository.deleteByNamespaceAndUsername(Integer.valueOf(namespaceId), username);
                if (deletedCount > 0)
                    response.put("message", "success");
                else
                    response.put("message", "success");
                return response.toString();
            } else {
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
    }

    public String createNamespacePermissions(String token, String namespaceId,
                                             String username, String role) throws CisException {
        if (bulkSctidService.authenticateToken(token)) {
            UserDTO userObj = this.getAuthenticatedUser();
            if (this.isAbletoEdit(Integer.valueOf(namespaceId), userObj)) {
                JSONObject response = new JSONObject();
                PermissionsNamespace permissionsNamespace = new PermissionsNamespace(Integer.valueOf(namespaceId), username, role);
                PermissionsNamespace permissionsNamespace1 = permissionsNamespaceRepository.save(permissionsNamespace);
                response.put("message", "success");
                return response.toString();
            } else {
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
    }

}
