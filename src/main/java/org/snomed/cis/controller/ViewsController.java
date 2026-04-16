package org.snomed.cis.controller;


import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/ui")
public class ViewsController {
    private final Logger logger = LoggerFactory.getLogger(ViewsController.class);


    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private String loadTemplate(String path) throws IOException {
        org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource(path);
        return org.springframework.util.StreamUtils.copyToString(resource.getInputStream(), DEFAULT_CHARSET);
    }


    @Operation(summary = "formToFillHbs")
    @GetMapping("/api/views/searchIds/formToFill.hbs")
    @ResponseBody
    public String formToFillHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/searchIds/formToFill.hbs");
    }

    @Operation(summary = "searchIdsMainHbs")
    @GetMapping("/api/views/searchIds/main.hbs")
    @ResponseBody
    public String searchIdsMainHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/searchIds/main.hbs");
    }

    @Operation(summary = "reportsBodyHbs")
    @GetMapping("/api/views/reports/body.hbs")
    @ResponseBody
    public String reportsBodyHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/reports/body.hbs");
    }

    @Operation(summary = "reportsMainHbs")
    @GetMapping("/api/views/reports/main.hbs")
    @ResponseBody
    public String reportsMainHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/reports/main.hbs");
    }

    @Operation(summary = "adminDetailsListHbs")
    @GetMapping("/api/views/admin/detailsList.hbs")
    @ResponseBody
    public String adminDetailsListHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/admin/detailsList.hbs");
    }

    @Operation(summary = "adminModalBodyHbs")
    @GetMapping("/api/views/admin/modalBody.hbs")
    @ResponseBody
    public String adminModalBodyHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/admin/modalBody.hbs");
    }

    @Operation(summary = "detailsHbs")
    @GetMapping("/api/views/bulk/details.hbs")
    @ResponseBody
    public String detailsHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/bulk/details.hbs");
    }

    @Operation(summary = "bulkListHbs")
    @GetMapping("/api/views/bulk/list.hbs")
    @ResponseBody
    public String bulkListHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/bulk/list.hbs");
    }

    @Operation(summary = "bulkMainHbs")
    @GetMapping("/api/views/bulk/main.hbs")
    @ResponseBody
    public String bulkMainHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/bulk/main.hbs");
    }

    @Operation(summary = "bulkRecordsHbs")
    @GetMapping("/api/views/bulk/records.hbs")
    @ResponseBody
    public String bulkRecordsHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/bulk/records.hbs");
    }

    @Operation(summary = "generateIdsMainHbs")
    @GetMapping("/api/views/generateIds/main.hbs")
    @ResponseBody
    public String generateIdsMainHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/generateIds/main.hbs");
    }

    @Operation(summary = "profileHbs")
    @GetMapping("/api/views/home/profile.hbs")
    @ResponseBody
    public String profileHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/home/profile.hbs");
    }

    @Operation(summary = "mainHbs")
    @GetMapping("/api/views/items/main.hbs")
    @ResponseBody
    public String mainHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/items/main.hbs");
    }

    @Operation(summary = "listSchemesHbs")
    @GetMapping("/api/views/items/list-schemes.hbs")
    @ResponseBody
    public String listSchemesHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/items/list-schemes.hbs");
    }

    @Operation(summary = "testView")
    @GetMapping("/api/views/items/list-namespaces.hbs")
    @ResponseBody
    public String testView() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/items/list-namespaces.hbs");
    }

    @Operation(summary = "permissionsHbs")
    @GetMapping("/api/views/items/permissions.hbs")
    @ResponseBody
    public String permissionsHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/items/permissions.hbs");
    }

    @Operation(summary = "groupPermissionsHbs")
    @GetMapping("/api/views/items/groupPermissions.hbs")
    @ResponseBody
    public String groupPermissionsHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/items/groupPermissions.hbs");
    }

    @Operation(summary = "detailsSchemesHbs")
    @GetMapping("/api/views/items/details-schemes.hbs")
    @ResponseBody
    public String detailsSchemesHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/items/details-schemes.hbs");
    }

    @Operation(summary = "detailsNamespacesHbs")
    @GetMapping("/api/views/items/details-namespaces.hbs")
    @ResponseBody
    public String detailsNamespacesHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/items/details-namespaces.hbs");
    }

    @Operation(summary = "addPermissionHbs")
    @GetMapping("/api/views/items/addPermission.hbs")
    @ResponseBody
    public String addPermissionHbs() throws IOException {
        logger.info("Request received");
        return loadTemplate("/static/admin/views/items/addPermission.hbs");
    }

    @Operation(summary = "redirectInfo")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public String redirectInfo() {
        return "redirect:/info/index.html";
    }
    @Operation(summary = "redirectAdmin")
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String redirectAdmin() {
        return "redirect:/admin/index.html";
    }
    @Operation(summary = "redirectSwagger")
    @RequestMapping(value = "/docs", method = RequestMethod.GET)
    public String redirectSwagger() {
        return "redirect:/swagger-ui/index.html";
    }
}
