package org.snomed.cis.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.domain.SchemeIdBase;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.Scheme;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.PermissionsSchemeRepository;
import org.snomed.cis.repository.SchemeIdBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SchemeService {
    private final Logger logger = LoggerFactory.getLogger(SchemeService.class);

    @Autowired
    BulkSctidService bulkSctidService;

    @Autowired
    private SchemeIdBaseRepository schemeIdBaseRepository;

    @Autowired
    private PermissionsSchemeRepository permissionsSchemeRepository;

    @Autowired
    private AuthenticateToken authenticateToken;


    private boolean isAbleToEdit(String schemeName, AuthenticateResponseDto authenticateResponseDto) {
        logger.debug("Request Received : schemeName-{} :: authToken - {} ", schemeName, authenticateResponseDto);
        List<String> groups = authenticateResponseDto.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
        boolean isAble = false;
        if (groups.contains("component-identifier-service-admin") || hasSchemePermission(schemeName, authenticateResponseDto.getName())) {
            isAble = true;
        }
        logger.info("isAbleToEdit() Response: {}", isAble);
        return isAble;
    }

    public boolean hasSchemePermission(String schemeName, String userName) {
        logger.debug("Request Received : schemeName-{} :: userName - {} ", schemeName, userName);
        boolean hasSchemePermission = false;
        if (!String.valueOf(schemeName).equalsIgnoreCase("false")) {
            List<PermissionsScheme> permissionsSchemes = permissionsSchemeRepository.findByScheme(schemeName);
            for (PermissionsScheme permissionsScheme : permissionsSchemes) {
                if (("manager").equalsIgnoreCase(permissionsScheme.getRole()) && (permissionsScheme.getUsername().equalsIgnoreCase(userName))) {
                    hasSchemePermission = true;
                    break;
                }
            }
        }
        logger.info("hasSchemePermission() Response: {}", hasSchemePermission);
        return hasSchemePermission;
    }

    public List<Scheme> getSchemesForUser(AuthenticateResponseDto token, String user) throws CisException {
        logger.debug("Request Received : authToken - {} , user - {}", token, user);
        return this.getSchemesForUsers(token, user);
    }

    public List<Scheme> getSchemesForUsers(AuthenticateResponseDto token, String user) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto-{} :: user - {} ", token, user);
        List<PermissionsScheme> permissionsSchemes = null;
        List<Scheme> scheme = new ArrayList<>();
        Scheme schemeObj = null;
        List<String> groups = token.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());


        permissionsSchemes = permissionsSchemeRepository.findByUsername(groups.get(0));
        for (PermissionsScheme perm : permissionsSchemes) {
            schemeObj = new Scheme(perm.getScheme(), perm.getRole());
            scheme.add(schemeObj);
            scheme.size();
        }
        logger.info("getSchemesForUsers() Response: {}", scheme);
        return scheme;
    }


    //@GetMapping("/schemes")
    public List<SchemeIdBase> getSchemes() throws CisException {
        logger.debug("Request Received : No Params");
        return this.getSchemesAll();
    }

    public List<SchemeIdBase> getSchemesAll() throws CisException {
        logger.debug("Request Received : No Params");
        List<Scheme> schemeList = new ArrayList<>();
        List<SchemeIdBase> schemeIdBase = schemeIdBaseRepository.findAll();
        List<SchemeIdBase> finalList = new ArrayList();
        if (schemeIdBase != null) {
           /* for (SchemeIdBase schemeIdBase1 : schemeIdBase) {
                Scheme schemeObj = new Scheme(schemeIdBase1.getScheme(), schemeIdBase1.getIdBase());
                schemeList.add(schemeObj);
            }*/
            finalList = schemeIdBase;
        } else {
            finalList = Collections.EMPTY_LIST;
        }
        logger.info("Get All Schemes - response", finalList);
        return finalList;
    }

    public SchemeIdBase getScheme(String schemeName) throws CisException {
        logger.debug("Request Received : schemeName-{} ", schemeName);
        return this.getSchemeAll(schemeName);
    }

    public SchemeIdBase getSchemeAll(String scheme) throws CisException {
        logger.debug("Request Received : schemeName-{} :: authToken - {} ", scheme);
        SchemeIdBase result;
        Optional<SchemeIdBase> schemeIdBase = schemeIdBaseRepository.findByScheme(scheme);
        logger.info("Get All Scheme - ", schemeIdBase);
        if (null != (schemeIdBase.isPresent() ? schemeIdBase.get() : null))
            result = schemeIdBase.get();
        else
            result = new SchemeIdBase();
        logger.info("Get All Scheme - response", result);
        return result;
    }

    //updateScheme
    public String updateScheme(AuthenticateResponseDto token, SchemeName schemeName, String schemeSeq) throws CisException {
        logger.debug("Request Received : AuthenticateResponseDto-{} :: SchemeName - {} ::schemeSeq - {}  ", token, schemeName, schemeSeq);
        return this.updateSchemes(token, schemeName, schemeSeq);
    }

    public String updateSchemes(AuthenticateResponseDto token, SchemeName schemeName, String schemeSeq) throws CisException {
        logger.debug("Request Received :AuthenticateResponseDto- {} :: schemeName-{} :: schemeSeq - {} ", token, schemeName, schemeSeq);
        Scheme schemeObj = null;

        if (this.isAbleToEdit(String.valueOf(schemeName), token)) {

            Optional<SchemeIdBase> schemeIdBase = schemeIdBaseRepository.findByScheme(schemeName.toString());
            JSONObject response = new JSONObject();

            if (schemeIdBase.isPresent()) {

                SchemeIdBase schemeIdBase1 = schemeIdBase.get();
                schemeObj = new Scheme(schemeIdBase1.getScheme(), schemeIdBase1.getIdBase());
                schemeObj.setDescription(schemeSeq);
                schemeIdBase1.setIdBase(schemeSeq);
                schemeIdBaseRepository.save(schemeIdBase1);
                response.put("message", "Success");
                logger.info("Update Scheme - ", response.toString());
                return response.toString();

            }

        } else {
            logger.error("error updateSchemes():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return null;
    }

    public List<PermissionsScheme> getPermissionsForScheme(String schemeName) {
        logger.debug("Request Received : schemeName-{}", schemeName);
        List<PermissionsScheme> permissionsSchemeList = permissionsSchemeRepository.findByScheme(schemeName);
        logger.info("getPermissionsForScheme(): Response-{}", permissionsSchemeList);
        return permissionsSchemeList;
    }

    public String deleteSchemePermissions(String schemeName, String username, AuthenticateResponseDto authenticateResponseDto) throws CisException {
        logger.debug("Request Received : schemeName-{} :: username - {} :: authToken - {} ", schemeName, username, authenticateResponseDto);
        if (isAbleToEdit(schemeName, authenticateResponseDto)) {
            permissionsSchemeRepository.deleteBySchemeAndUsername(schemeName, username);
            JSONObject response = new JSONObject();
            response.put("message", "Success");
            logger.info("deleteSchemePermissions() Response:{}", response.toString());
            return response.toString();
        } else {
            logger.error("error deleteSchemePermissions():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }

    }

    public String createSchemePermissions(String schemeName, String role, AuthenticateResponseDto authenticateResponseDto) throws CisException {
        logger.debug("Request Received : schemeName-{} :: role-{} :: authToken - {} ", schemeName, role, authenticateResponseDto);
        if (isAbleToEdit(schemeName, authenticateResponseDto)) {
            JSONObject response = new JSONObject();
            PermissionsScheme permissionsScheme = new PermissionsScheme(schemeName, authenticateResponseDto.getName(), role);
            permissionsSchemeRepository.save(permissionsScheme);
            response.put("message", "Success");
            logger.info("createSchemePermissions() Response:{}", response.toString());
            return response.toString();
        } else {
            logger.error("error createSchemePermissions():: No permission for the selected operation");
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }

    }

}
