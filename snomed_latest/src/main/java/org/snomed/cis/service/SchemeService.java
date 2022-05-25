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
    private SecurityController securityController;

    @Autowired
    BulkSctidService bulkSctidService;

    @Autowired
    private SchemeIdBaseRepository schemeIdBaseRepository;

    @Autowired
    private PermissionsSchemeRepository permissionsSchemeRepository;

    @Autowired
    private AuthenticateToken authenticateToken;

    public boolean authenticateToken() throws CisException {
        if (1 == 1)
            return true;
        UserDTO obj = this.securityController.authenticate();
        if (null != obj) return true;
        else return false;
    }

    public UserDTO getAuthenticatedUser() throws CisException {
        return this.securityController.authenticate();
    }

    private boolean isAbleToEdit(SchemeName schemeName, UserDTO userObj) throws CisException {
        List<String> groups = authenticateToken.getGroupsList();
        boolean able = false;
        for (String group : groups) {
            if (group.equalsIgnoreCase("component-identifier-service-admin")) {
                able = true;
            }
        }
       /* List<String> admins = Arrays.asList("a", "b", "c");
        for (String admin : admins) {
            if (admin.equalsIgnoreCase(userObj.getLogin())) {
                able = true;
            }
        }*/
        if (!able) {
            if (!String.valueOf(schemeName).equalsIgnoreCase("false")) {
                List<PermissionsScheme> permissionsSchemesList = permissionsSchemeRepository.findByScheme(schemeName.schemeName);
                //

                for (PermissionsScheme perm : permissionsSchemesList) {
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
    }

    private boolean isAbleToEdit(String schemeName, AuthenticateResponseDto authenticateResponseDto) {
        List<String> groups = authenticateResponseDto.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
        boolean isAble = false;
        if (groups.contains("component-identifier-service-admin") || hasSchemePermission(schemeName, authenticateResponseDto.getFirstName())) {
            isAble = true;
        }
        return isAble;
    }

    public boolean hasSchemePermission(String schemeName, String firstName) {
        boolean hasSchemePermission = false;

        if (!String.valueOf(schemeName).equalsIgnoreCase("false")) {
            List<PermissionsScheme> permissionsSchemes = permissionsSchemeRepository.findByScheme(schemeName);
            for (PermissionsScheme permissionsScheme : permissionsSchemes) {
                if (("manager").equalsIgnoreCase(permissionsScheme.getRole()) && (permissionsScheme.getUsername().equalsIgnoreCase(firstName))) {
                    hasSchemePermission = true;
                    break;
                }
            }
        }
        return hasSchemePermission;
    }

    public List<Scheme> getSchemesForUser(String token, String user) throws CisException {
        if (bulkSctidService.authenticateToken(token)) return this.getSchemesForUsers(token, user);
        else throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public List<Scheme> getSchemesForUsers(String token, String user) throws CisException {
        List<PermissionsScheme> permissionsSchemes = null;
        List<Scheme> scheme = new ArrayList<>();
        Scheme schemeObj = null;
        List<String> groups = securityController.getUserGroup(user, token);


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
        if (bulkSctidService.authenticateToken(token)) return this.getSchemesAll(token);
        else throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
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


    //@GetMapping("/schemes/{schemeName}")
    public SchemeIdBase getScheme(String token, String schemeName) throws CisException {
        if (bulkSctidService.authenticateToken(token)) return this.getSchemeAll(token, schemeName);
        else throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public SchemeIdBase getSchemeAll(String token, String scheme) throws CisException {

        Optional<SchemeIdBase> schemeIdBase = schemeIdBaseRepository.findByScheme(scheme);
       /* Scheme schemeObj=new Scheme();

        if(schemeIdBase.isPresent()){
            SchemeIdBase schemeIdBase1=schemeIdBase.get();
             schemeObj= new Scheme(schemeIdBase1.getScheme(),schemeIdBase1.getIdBase());
        }*/

        logger.info("Get All Scheme - ", schemeIdBase);
        if (null != (schemeIdBase.isPresent() ? schemeIdBase.get() : null))
            return schemeIdBase.get();
        else
            return new SchemeIdBase();
    }

    //updateScheme
    public String updateScheme(String token, SchemeName schemeName, String schemeSeq) throws CisException {
        if (bulkSctidService.authenticateToken(token))
            return this.updateSchemes(token, schemeName, schemeSeq);
        else
            throw new CisException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public String updateSchemes(String token, SchemeName schemeName, String schemeSeq) throws CisException {
        UserDTO userObj = this.getAuthenticatedUser();
        Scheme schemeObj = null;

        if (this.isAbleToEdit(schemeName, userObj)) {

            Optional<SchemeIdBase> schemeIdBase = schemeIdBaseRepository.findByScheme(schemeName.toString());
            JSONObject response = new JSONObject();

            if (schemeIdBase.isPresent()) {

                SchemeIdBase schemeIdBase1 = schemeIdBase.get();
                schemeObj = new Scheme(schemeIdBase1.getScheme(), schemeIdBase1.getIdBase());
                schemeObj.setDescription(schemeSeq);

                //SchemeIdBase schemeIdBaseObj = schemeIdBase.get();
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
