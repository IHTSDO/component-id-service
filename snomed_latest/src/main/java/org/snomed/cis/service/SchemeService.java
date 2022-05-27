package org.snomed.cis.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.controller.SecurityController;
import org.snomed.cis.controller.dto.AuthenticateResponseDto;
import org.snomed.cis.controller.dto.Scheme;
import org.snomed.cis.controller.dto.UserDTO;
import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.domain.SchemeIdBase;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.PermissionsSchemeRepository;
import org.snomed.cis.repository.SchemeIdBaseRepository;
import org.snomed.cis.security.Token;
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

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    BulkSctidService bulkSctidService;

    @Autowired
    private SchemeIdBaseRepository schemeIdBaseRepository;

    @Autowired
    private PermissionsSchemeRepository permissionsSchemeRepository;

    @Autowired
    private AuthenticateToken authenticateToken;


    private boolean isAbleToEdit(String schemeName, AuthenticateResponseDto authenticateResponseDto) {
        List<String> groups = authenticateResponseDto.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
        boolean isAble = false;
        if (groups.contains("component-identifier-service-admin") || hasSchemePermission(schemeName, authenticateResponseDto.getName())) {
            isAble = true;
        }
        return isAble;
    }

    public boolean hasSchemePermission(String schemeName, String userName) {
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
        return hasSchemePermission;
    }

    public List<Scheme> getSchemesForUser(AuthenticateResponseDto token, String user) throws CisException {
         return this.getSchemesForUsers(token, user);
    }

    public List<Scheme> getSchemesForUsers(AuthenticateResponseDto token, String user) throws CisException {
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
        return scheme;
    }


    //@GetMapping("/schemes")
    public List<SchemeIdBase> getSchemes(String token) throws CisException {
        return this.getSchemesAll(token);
    }

    public List<SchemeIdBase> getSchemesAll(String token) throws CisException {
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

        return finalList;
    }

    public SchemeIdBase getScheme(String schemeName) throws CisException {
        return this.getSchemeAll(schemeName);
    }

    public SchemeIdBase getSchemeAll(String scheme) throws CisException {

        Optional<SchemeIdBase> schemeIdBase = schemeIdBaseRepository.findByScheme(scheme);
        logger.info("Get All Scheme - ", schemeIdBase);
        if (null != (schemeIdBase.isPresent() ? schemeIdBase.get() : null))
            return schemeIdBase.get();
        else
            return new SchemeIdBase();
    }

    //updateScheme
    public String updateScheme(AuthenticateResponseDto token, SchemeName schemeName, String schemeSeq) throws CisException {
            return this.updateSchemes(token, schemeName, schemeSeq);
    }

    public String updateSchemes(AuthenticateResponseDto token, SchemeName schemeName, String schemeSeq) throws CisException {
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
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return null;
    }

    public List<PermissionsScheme> getPermissionsForScheme(String schemeName) {
        return permissionsSchemeRepository.findByScheme(schemeName);
    }

    public String deleteSchemePermissions(String schemeName, String username, AuthenticateResponseDto authenticateResponseDto) throws CisException {
        if (isAbleToEdit(schemeName, authenticateResponseDto)) {
            permissionsSchemeRepository.deleteBySchemeAndUsername(schemeName, username);
            JSONObject response = new JSONObject();
            response.put("message", "Success");
            return response.toString();
        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }

    }

    public String createSchemePermissions(String schemeName, String role, AuthenticateResponseDto authenticateResponseDto) throws CisException {
        if (isAbleToEdit(schemeName, authenticateResponseDto)) {
            JSONObject response = new JSONObject();
            PermissionsScheme permissionsScheme = new PermissionsScheme(schemeName, authenticateResponseDto.getName(), role);
            permissionsSchemeRepository.save(permissionsScheme);
            response.put("message", "Success");
            return response.toString();
        } else {
            throw new CisException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
        }

    }

}
