package com.snomed.api.service;

import com.snomed.api.controller.SecurityController;
import com.snomed.api.controller.dto.Scheme;
import com.snomed.api.controller.dto.UserDTO;
import com.snomed.api.domain.PermissionsScheme;
import com.snomed.api.domain.SchemeIdBase;
import com.snomed.api.domain.SchemeName;
import com.snomed.api.exception.APIException;
import com.snomed.api.repository.PermissionsSchemeRepository;
import com.snomed.api.repository.SchemeIdBaseRepository;
import net.bytebuddy.description.type.RecordComponentList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public boolean authenticateToken() throws APIException {
        if (1 == 1)
            return true;
        UserDTO obj = this.securityController.authenticate();
        if (null != obj) return true;
        else return false;
    }

    public UserDTO getAuthenticatedUser() throws APIException {
        return this.securityController.authenticate();
    }

    private boolean isAbleToEdit(SchemeName schemeName, UserDTO userObj) throws APIException {
        List<String> groups = authenticateToken.getGroupsList();
        boolean able = false;
        for(String group:groups)
        {
            if(group.equalsIgnoreCase("component-identifier-service-admin"))
            {
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

    public List<Scheme> getSchemesForUser(String token,String user) throws APIException {
        if (bulkSctidService.authenticateToken(token)) return this.getSchemesForUsers(token,user);
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public List<Scheme> getSchemesForUsers(String token,String user) throws APIException {
        List<PermissionsScheme> permissionsSchemes = null;
        List<Scheme> scheme = new ArrayList<>();
        Scheme schemeObj=null;
        List<String> groups = securityController.getUserGroup(user,token);


        permissionsSchemes=permissionsSchemeRepository.findByUsername(groups.get(0));
        for (PermissionsScheme perm : permissionsSchemes) {
            schemeObj = new Scheme(perm.getScheme(),perm.getRole());
            scheme.add(schemeObj);
            scheme.size();

        }


        return scheme;
    }


    //@GetMapping("/schemes")
    public List<SchemeIdBase> getSchemes(String token) throws APIException {
        if (bulkSctidService.authenticateToken(token)) return this.getSchemesAll(token);
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public List<SchemeIdBase> getSchemesAll(String token) throws APIException {
        List<Scheme> schemeList = new ArrayList<>();
        List<SchemeIdBase> schemeIdBase = schemeIdBaseRepository.findAll();
        List<SchemeIdBase> finalList = new ArrayList();
        if (schemeIdBase != null) {
           /* for (SchemeIdBase schemeIdBase1 : schemeIdBase) {
                Scheme schemeObj = new Scheme(schemeIdBase1.getScheme(), schemeIdBase1.getIdBase());
                schemeList.add(schemeObj);
            }*/
            finalList = schemeIdBase;
        }
        else
        {
            finalList = Collections.EMPTY_LIST;
        }

        return finalList;
    }


    //@GetMapping("/schemes/{schemeName}")
    public SchemeIdBase getScheme(String token, String schemeName) throws APIException {
        if (bulkSctidService.authenticateToken(token)) return this.getSchemeAll(token,schemeName);
        else throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public SchemeIdBase getSchemeAll(String token,String scheme) throws APIException {

        Optional<SchemeIdBase> schemeIdBase = schemeIdBaseRepository.findByScheme(scheme);
       /* Scheme schemeObj=new Scheme();

        if(schemeIdBase.isPresent()){
            SchemeIdBase schemeIdBase1=schemeIdBase.get();
             schemeObj= new Scheme(schemeIdBase1.getScheme(),schemeIdBase1.getIdBase());
        }*/

        logger.info("Get All Scheme - ", schemeIdBase);
if(null!=schemeIdBase.get())
        return schemeIdBase.get();
else
    return new SchemeIdBase();
    }

    //updateScheme
    public String updateScheme(String token,SchemeName schemeName, String schemeSeq) throws APIException {
        if (bulkSctidService.authenticateToken(token))
            return this.updateSchemes(token,schemeName, schemeSeq);
        else
            throw new APIException(HttpStatus.NOT_FOUND, "Invalid Token/User Not authenticated");
    }

    public String updateSchemes(String token,SchemeName schemeName, String schemeSeq) throws APIException {
        UserDTO userObj = this.getAuthenticatedUser();
        Scheme schemeObj=null;

        if (this.isAbleToEdit(schemeName, userObj)) {

            Optional<SchemeIdBase> schemeIdBase = schemeIdBaseRepository.findByScheme(schemeName.toString());
            JSONObject response = new JSONObject();

            if (schemeIdBase.isPresent()) {

                SchemeIdBase schemeIdBase1=schemeIdBase.get();
                schemeObj = new Scheme(schemeIdBase1.getScheme(), schemeIdBase1.getIdBase());
                schemeObj.setDescription(schemeSeq);

                //SchemeIdBase schemeIdBaseObj = schemeIdBase.get();
                schemeIdBase1.setIdBase(schemeSeq);
                schemeIdBaseRepository.save(schemeIdBase1);
                response.put("message","Success");
                logger.info("Update Scheme - ", response.toString());
                return response.toString();

            }

        } else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");

        }
        return null;
    }

    /* Method for "/schemes/{schemeName}/permissions" */
    public List<PermissionsScheme> getPermissionForScheme(String token, String schemeName) throws APIException {
        List permissionsListFinal;
        if (bulkSctidService.authenticateToken(token))
        {
            List<PermissionsScheme> permissionsSchemeList = permissionsSchemeRepository.findByScheme(schemeName);
            if(permissionsSchemeList.size()>0)
                permissionsListFinal = permissionsSchemeList;
            else
                permissionsListFinal = Collections.EMPTY_LIST;
        }
        else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
        return permissionsListFinal;
    }
    /** method for Authorization Controller*/
    public String deleteSchemesPermissions(String token,String schemeName,String username) throws APIException {
        if (bulkSctidService.authenticateToken(token))
        {
            UserDTO userObj = this.getAuthenticatedUser();
            if (this.isAbleToEdit(SchemeName.valueOf(schemeName), userObj)) {
                JSONObject response = new JSONObject();
               permissionsSchemeRepository.deleteBySchemeAndUsername(schemeName,username);
                    response.put("message", "success");
                return response.toString();
            }
            else {
                throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        }
        else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
    }

    public String createSchemesPermissions (String token, String schemeName,
                                              String username, String role) throws APIException {
        if (bulkSctidService.authenticateToken(token))
        {
            UserDTO userObj = this.getAuthenticatedUser();
            if (this.isAbleToEdit(SchemeName.valueOf(schemeName), userObj)) {
                JSONObject response = new JSONObject();
                PermissionsScheme permissionsScheme = new PermissionsScheme(schemeName,username,role);
                PermissionsScheme permissionsScheme1 = permissionsSchemeRepository.save(permissionsScheme);
                response.put("message", "success");
                return response.toString();
            }
            else {
                throw new APIException(HttpStatus.UNAUTHORIZED, "No permission for the selected operation");
            }
        }
        else {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid Token/User Not authenticated");
        }
    }

}
