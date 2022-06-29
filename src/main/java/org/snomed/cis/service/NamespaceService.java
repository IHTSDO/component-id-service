package org.snomed.cis.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.Namespace;
import org.snomed.cis.domain.Partitions;
import org.snomed.cis.domain.PartitionsPk;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.NamespaceDto;
import org.snomed.cis.dto.PartitionsDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.NamespaceRepository;
import org.snomed.cis.repository.PartitionsRepository;
import org.snomed.cis.repository.PermissionsNamespaceRepository;
import org.snomed.cis.util.CrowdRequestManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NamespaceService {
    private final Logger logger = LoggerFactory.getLogger(NamespaceService.class);
    @Autowired
    public NamespaceRepository namespaceRepository;

    @Autowired
    AuthenticateToken authenticateToken;

    @Autowired
    BulkSctidService bulkSctidService;

    @Autowired
    private CrowdRequestManager crowdRequestManager;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private PermissionsNamespaceRepository permissionsNamespaceRepository;

    @Autowired
    private PartitionsRepository partitionsRepository;


    public List<NamespaceDto> getNamespaces() throws CisException {
        return this.getNamespaceslist();
    }

    public List<NamespaceDto> getNamespaceslist() {
        logger.debug("NamespaceService.getNamespaceslist(): No Params ");
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

        if (namespaceDtoList.size() > 0) {
            //Collections.sort(namespaceDtoList);
            Collections.sort(namespaceDtoList, Comparator.comparingInt(NamespaceDto::getNamespace));
        }
        logger.info("getNamespaceslist() : Response :: {} ", namespaceDtoList);
        return namespaceDtoList;

    }

    public String createNamespace(AuthenticateResponseDto authenticateResponseDto, NamespaceDto namespace) throws CisException, ParseException {
        logger.debug("NamespaceService.createNamespace() authenticateResponseDto-{} :: NamespaceDto-{} ", authenticateResponseDto, namespace);
        return this.createNamespaces(authenticateResponseDto, namespace);
    }

    public String createNamespaces(AuthenticateResponseDto authenticateResponseDto, NamespaceDto namespace) throws CisException, ParseException {
        logger.debug("NamespaceService.createNamespaces() AuthenticateResponseDto-{} :: NamespaceDto-{} ", authenticateResponseDto, namespace);
        String namespaceString = namespace.getNamespace() + "";
        NamespaceDto output = new NamespaceDto();
        if (this.isAbleToEdit(namespace.getNamespace(), authenticateResponseDto)) {
            if (namespaceString.length() != 7 && !namespaceString.equalsIgnoreCase("0")) {
                logger.error("error createNamespaces():: Invalid namespace");
                throw new CisException(HttpStatus.BAD_REQUEST, "Invalid namespace");
            } else {
                return createNamespaceList(namespace);
            }
        } else {
            logger.error("error createNamespaces():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    private String createNamespaceList(NamespaceDto namespace) throws ParseException {
        logger.debug("NamespaceService.createNamespaceList() NamespaceDto-{} ", namespace);
        NamespaceDto output = new NamespaceDto();
        JSONObject response = new JSONObject();
        List<Partitions> partitionsOfObj = namespace.getPartitions();
        //List<Partitions> partitionsList = new ArrayList<>();
        PartitionsDto partitionsDto = new PartitionsDto();
        List<Partitions> partitionsList = new ArrayList<>();
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
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
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
        logger.info("createNamespaceList() Response-{} ", response.toString());
        return response.toString();
    }

    private boolean isAbleToEdit(Integer namespace, AuthenticateResponseDto authenticateResponseDto) {
        logger.debug("NamespaceService.isAbleToEdit() namespace-{} :: AuthenticateResponseDto-{} ", namespace, authenticateResponseDto);
        List<String> groups = authenticateResponseDto.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
        boolean isAble = false;
        if (groups.contains("component-identifier-service-admin") || hasNamespacePermission(namespace, authenticateResponseDto.getFirstName())) {
            isAble = true;
        }
        logger.info("isAbleToEdit() Response-{} ", isAble);
        return isAble;
    }

    public boolean hasNamespacePermission(Integer namespace, String firstName) {
        logger.debug("NamespaceService.hasNamespacePermission() namespace-{} :: firstName-{} ", namespace, firstName);
        boolean hasNamespacePermission = false;
        if (!String.valueOf(namespace).equalsIgnoreCase("false")) {
            List<PermissionsNamespace> permissionsNamespaces = permissionsNamespaceRepository.findByNamespace(namespace);
            for (PermissionsNamespace permissionsNamespace : permissionsNamespaces) {
                if (("manager").equalsIgnoreCase(permissionsNamespace.getRole()) && (permissionsNamespace.getUsername().equalsIgnoreCase(firstName))) {
                    hasNamespacePermission = true;
                    break;
                }
            }
        }
        logger.info("hasNamespacePermission() Response-{} ", hasNamespacePermission);
        return hasNamespacePermission;
    }

    public String updateNamespace(AuthenticateResponseDto authenticateResponseDto, NamespaceDto namespace) throws CisException {
        logger.debug("NamespaceService.updateNamespace() authenticateResponseDto-{} :: namespace-{} ", authenticateResponseDto, namespace);
        String result = this.updateNamespaces(authenticateResponseDto, namespace);
        logger.info("updateNamespace() Response-{} ", result);
        return result;
    }

    public String updateNamespaces(AuthenticateResponseDto authenticateResponseDto, NamespaceDto namespace) throws CisException {
        logger.debug("NamespaceService.updateNamespaces() AuthenticateResponseDto-{} :: NamespaceDto-{} ", authenticateResponseDto, namespace);
        if (this.isAbleToEdit(namespace.getNamespace(), authenticateResponseDto)) {
            String result = (editNamespace(namespace.getNamespace(), namespace));
            logger.info("updateNamespaces() Response-{} ", result);
            return result;

        } else {
            logger.error("error updateNamespaces():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
    }

    private String editNamespace(Integer namespaceId, NamespaceDto namespaceObj) throws CisException {
        logger.debug("NamespaceService.editNamespace() namespaceId-{} :: NamespaceDto-{} ", namespaceId, namespaceObj);
        NamespaceDto namespacesObj = new NamespaceDto();
        List<Partitions> partitionsList = null;
        PartitionsDto partitionsDto = null;
        List<PartitionsDto> partitionsDtoList = new ArrayList<>();
        //SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
        JSONObject response = new JSONObject();

        Namespace namespace = new Namespace();
        Optional<Namespace> namespaces = namespaceRepository.findById(namespaceId);
        Namespace namespaceGet = namespaces.isPresent() ? namespaces.get() : null;

        if (null != namespaceGet) {
            namespaceGet.setOrganizationName(namespaceObj.getOrganizationName());
            namespaceGet.setOrganizationAndContactDetails(namespaceObj.getOrganizationAndContactDetails());
            namespaceGet.setDateIssued(null != namespaceObj.getDateIssued() ? LocalDateTime.parse(namespaceObj.getDateIssued(), dateTimeFormatter) : null);
            namespaceGet.setEmail(namespaceObj.getEmail());
            namespaceGet.setNotes(namespaceObj.getNotes());
            namespaceGet.setIdPregenerate(namespaceObj.getIdPregenerate());
        }
        try {
            namespace = namespaceRepository.save(namespaceGet);
            response.put("message", "Success");
        } catch (Exception e) {
            logger.error("error editNamespace():: ", e.getMessage());
            throw new CisException(HttpStatus.BAD_REQUEST, String.valueOf(response.put("message", e.getMessage())));
        }
        logger.info("editNamespace() Response-{} ", response.toString());
        return response.toString();
    }


    public List<Namespace> getNamespacesForUser(String userName) throws CisException {
        logger.debug("NamespaceService.getNamespacesForUser() userName-{} ", userName);
        return this.getNamespacesListForUser(userName);
    }

    public List<Namespace> getNamespacesListForUser(String userName) throws CisException {
        logger.debug("BulkSctidService.getNamespacesListForUser() userName-{}  ", userName);
        List<String> roleAsGroups;
        List<Namespace> namespaceList;
        List<String> namespacesFromGroup = new ArrayList<>();
        List<String> namespaces = new ArrayList<>();
        List<String> otherGroup = new ArrayList<>();

        List<String> groups = crowdRequestManager.getUserGroups(userName);

        if (groups != null && !groups.isEmpty()) {
            for (String group : groups) {
                if ("namespace".equalsIgnoreCase(group.substring(0, group.indexOf("-")))) {
                    namespacesFromGroup.add(group.substring(group.indexOf("-") + 1));
                } else {
                    otherGroup.add(group);
                }
            }
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
        logger.info("getNamespacesListForUser() Response-{} ", namespaceList);
        return namespaceList;
    }

    public static <T, U> List<U>
    convertStringListToIntList(List<T> listOfString,
                               Function<T, U> function) {
        return listOfString.stream()
                .map(function)
                .collect(Collectors.toList());
    }

    public NamespaceDto getNamespace(String namespaceId) throws CisException {
        logger.debug("NamespaceService.getNamespace() namespaceId-{} ", namespaceId);
        NamespaceDto namespaceDto = null;
        if (namespaceId.equalsIgnoreCase("undefined"))
            namespaceDto = this.getNamespaceId("0");
        else
            namespaceDto = this.getNamespaceId(namespaceId);
        logger.info("getNamespace() Response-{} ", namespaceDto);
        return namespaceDto;
    }

    @Transactional
    public NamespaceDto getNamespaceId(String namespaceId) throws CisException {
        logger.debug("NamespaceService.getNamespaceId() namespaceId-{} ", namespaceId);
        JSONObject response = new JSONObject();
        if (namespaceId.length() != 7 && !(namespaceId.equalsIgnoreCase("0"))) {
            logger.error("error getSchemeIds()::Invalid namespace");
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid namespace");
        }
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
        logger.info(" getNamespaceId() Response :: {}", namespacesObj);
        return namespacesObj;

    }


    public String deleteNamespace(AuthenticateResponseDto authenticateResponseDto, String namespaceId) throws CisException {
        logger.debug("NamespaceService.deleteNamespace() AuthenticateResponseDto-{} :: namespaceId-{} ", authenticateResponseDto, namespaceId);
        String value = this.deleteNamespaces(authenticateResponseDto, namespaceId);
        logger.info(" deleteNamespace() Response :: {}", value);
        return value;
    }

    public String deleteNamespaces(AuthenticateResponseDto authenticateResponseDto, String namespaceId) throws CisException {
        logger.debug("NamespaceService.deleteNamespaces() authenticateResponseDto-{} :: namespaceId-{} ", authenticateResponseDto, namespaceId);
        JSONObject response = new JSONObject();
        if (namespaceId.length() != 7 && namespaceId != "0")
            response.put("message", "Invalid Namespace");
        else {
            int deletedRows = 0;
            if (this.isAbleToEdit(Integer.valueOf(namespaceId), authenticateResponseDto)) {
                Optional<Namespace> namespaceObj = namespaceRepository.findById(Integer.valueOf(namespaceId));
                if (namespaceObj.isPresent()) {
                    namespaceRepository.delete(namespaceObj.get());
                }
                List<Partitions> partNamespace = partitionsRepository.findByNamespace(Integer.valueOf(namespaceId));
                if (partNamespace.size() > 0)
                    partitionsRepository.deleteAll(partNamespace);
                response.put("message", "Success");
            } else {
                logger.error("error getSchemeIds():: No permission for the selected operation");
                throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

            }
        }
        logger.info("deleteNamespaces() Response :: {}", response.toString());
        return response.toString();
    }


    public String updatePartitionSequence(AuthenticateResponseDto authenticateResponseDto, String namespaceId, String partitionId, String value) throws CisException {
        logger.debug("NamespaceService.updatePartitionSequence() authenticateResponseDto-{} :: namespaceId-{} :: partitionId - {} :: value - {} ", authenticateResponseDto, namespaceId, partitionId, value);
        String updatedValue = this.updatePartitionSequences(authenticateResponseDto, namespaceId, partitionId, value);
        logger.info("updatePartitionSequence() Response :: {}", updatedValue);
        return updatedValue;
    }

    public String updatePartitionSequences(AuthenticateResponseDto authenticateResponseDto, String namespaceId, String partitionId, String value) throws CisException {
        logger.debug("NamespaceService.updatePartitionSequences() authenticateResponseDto-{} :: namespaceId -{} :: partitionId - {} :: value-{} ", authenticateResponseDto, namespaceId, partitionId, value);
        String out = "";
        Integer namespaceIdint = Integer.valueOf(namespaceId);
        JSONObject response = new JSONObject();
        if (this.isAbleToEdit(Integer.valueOf(namespaceId), authenticateResponseDto)) {

            Optional<Partitions> partitions = partitionsRepository.findById(new PartitionsPk(namespaceIdint, partitionId));
            if (partitions.isPresent())
                partitions.get().setSequence(Integer.parseInt(value));
            Partitions partResult = partitionsRepository.save(partitions.get());
            if ((partResult.getSequence()).equals(Integer.parseInt(value)))
                response.put("message", "Success");
        } else {
            logger.error("error getSchemeIds():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        logger.info("updatePartitionSequences() Response :: {}", response.toString());
        return response.toString();
    }

    public List<PermissionsNamespace> getNamespacePermissions(String namespaceId) throws CisException {
        logger.debug("NamespaceService.getNamespacePermissions() namespaceId-{} ", namespaceId);
        try {
            List<PermissionsNamespace> permissionsNamespaceList = permissionsNamespaceRepository.findByNamespace(Integer.valueOf(namespaceId));
            if ((permissionsNamespaceList).size() > 0) {
                logger.info("getNamespacePermissions() Response :: {}", permissionsNamespaceList);
                return permissionsNamespaceList;
            } else {
                logger.info("getNamespacePermissions() Response :: {}", "null");
                return Collections.EMPTY_LIST;
            }
        } catch (Exception e) {
            logger.error("error getSchemeIds():: BAd Request: {}", e.getMessage());
            throw new CisException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public String deleteNamespacePermissionsOfUser(String namespaceId, String username, AuthenticateResponseDto authenticateResponseDto) throws CisException {
        logger.debug("NamespaceService.deleteNamespacePermissionsOfUser() namespaceId-{} :: AuthenticateResponseDto-{} ", namespaceId, authenticateResponseDto);
        if (isAbleToEdit(Integer.valueOf(namespaceId), authenticateResponseDto)) {
            JSONObject response = new JSONObject();
            permissionsNamespaceRepository.deleteByNamespaceAndUsername(Integer.valueOf(namespaceId), username);
            response.put("message", "Success");
            logger.info("deleteNamespacePermissionsOfUser() Response :: {}", response.toString());
            return response.toString();
        } else {
            logger.error("error deleteNamespacePermissionsOfUser():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
    }

    public String createNamespacePermissionsOfUser(String namespaceId, String username, String role, AuthenticateResponseDto authenticateResponseDto) throws CisException {
        logger.debug("NamespaceService.createNamespacePermissionsOfUser() namespaceId-{} :: username - {} :: role - {} :: AuthenticateResponseDto-{} ", namespaceId, username, role, authenticateResponseDto);
        if (isAbleToEdit(Integer.valueOf(namespaceId), authenticateResponseDto)) {
            PermissionsNamespace permissionsNamespace = new PermissionsNamespace(Integer.valueOf(namespaceId), username, role);
            Optional<PermissionsNamespace> result = permissionsNamespaceRepository.findByNamespaceAndUsernameAndRole(Integer.valueOf(namespaceId), username, role);
            if (result.isPresent())
                throw new CisException(HttpStatus.BAD_REQUEST, "ER_DUP_ENTRY: Duplicate entry " + "'" + namespaceId + "-" + username + "'" + " for key 'PRIMARY'");
            permissionsNamespaceRepository.save(permissionsNamespace);
            JSONObject response = new JSONObject();
            response.put("message", "Success");
            logger.info("createNamespacePermissionsOfUser() Response :: {}", response.toString());
            return response.toString();
        } else {
            logger.error("error createNamespacePermissionsOfUser():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }
    }

}
