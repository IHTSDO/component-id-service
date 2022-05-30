package org.snomed.cis.controller;

import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/ui")
public class ViewsController {
    private final Logger logger = LoggerFactory.getLogger(ViewsController.class);
    TemplateLoader viewsItemsLoader = new ClassPathTemplateLoader("/static/admin/views/items", ".hbs");
    TemplateLoader viewsHomeLoader = new ClassPathTemplateLoader("/static/admin/views/home", ".hbs");
    TemplateLoader generateIdsLoader = new ClassPathTemplateLoader("/static/admin/views/generateIds", ".hbs");
    TemplateLoader bulkLoader = new ClassPathTemplateLoader("/static/admin/views/bulk", ".hbs");
    TemplateLoader adminLoader = new ClassPathTemplateLoader("/static/admin/views/admin", ".hbs");
    TemplateLoader reportsLoader = new ClassPathTemplateLoader("/static/admin/views/reports", ".hbs");
    TemplateLoader searchIdsLoader = new ClassPathTemplateLoader("/static/admin/views/searchIds", ".hbs");

    @GetMapping("/api/views/searchIds/formToFill.hbs")
    @ResponseBody
    public String formToFillHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = searchIdsLoader.sourceAt("formToFill");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/searchIds/main.hbs")
    @ResponseBody
    public String searchIdsMainHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = searchIdsLoader.sourceAt("main");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/reports/body.hbs")
    @ResponseBody
    public String reportsBodyHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = reportsLoader.sourceAt("body");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/reports/main.hbs")
    @ResponseBody
    public String reportsMainHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = reportsLoader.sourceAt("main");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/admin/detailsList.hbs")
    @ResponseBody
    public String adminDetailsListHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = adminLoader.sourceAt("detailsList");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/admin/main.hbs")
    @ResponseBody
    public String adminMainHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = adminLoader.sourceAt("main");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/admin/modalBody.hbs")
    @ResponseBody
    public String adminModalBodyHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = adminLoader.sourceAt("modalBody");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/bulk/details.hbs")
    @ResponseBody
    public String detailsHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = bulkLoader.sourceAt("details");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/bulk/list.hbs")
    @ResponseBody
    public String bulkListHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = bulkLoader.sourceAt("list");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/bulk/main.hbs")
    @ResponseBody
    public String bulkMainHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = bulkLoader.sourceAt("main");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/bulk/records.hbs")
    @ResponseBody
    public String bulkRecordsHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = bulkLoader.sourceAt("records");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/generateIds/main.hbs")
    @ResponseBody
    public String generateIdsMainHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = generateIdsLoader.sourceAt("main");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/home/profile.hbs")
    @ResponseBody
    public String profileHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = viewsHomeLoader.sourceAt("profile");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/items/main.hbs")
    @ResponseBody
    public String mainHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = viewsItemsLoader.sourceAt("main");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/items/list-schemes.hbs")
    @ResponseBody
    public String listSchemesHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = viewsItemsLoader.sourceAt("list-schemes");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/items/list-namespaces.hbs")
    @ResponseBody
    public String testView() throws IOException {
        logger.info("Request received");
        TemplateSource source = viewsItemsLoader.sourceAt("list-namespaces");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/items/permissions.hbs")
    @ResponseBody
    public String permissionsHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = viewsItemsLoader.sourceAt("permissions");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/items/groupPermissions.hbs")
    @ResponseBody
    public String groupPermissionsHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = viewsItemsLoader.sourceAt("groupPermissions");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/items/details-schemes.hbs")
    @ResponseBody
    public String detailsSchemesHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = viewsItemsLoader.sourceAt("details-schemes");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/items/details-namespaces.hbs")
    @ResponseBody
    public String detailsNamespacesHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = viewsItemsLoader.sourceAt("details-namespaces");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/api/views/items/addPermission.hbs")
    @ResponseBody
    public String addPermissionHbs() throws IOException {
        logger.info("Request received");
        TemplateSource source = viewsItemsLoader.sourceAt("addPermission");
        String cont = source.content();
        return cont;
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public String redirectInfo() {
        return "redirect:/info/index.html";
    }
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String redirectAdmin() {
        return "redirect:/admin/index.html";
    }
    @RequestMapping(value = "/docs", method = RequestMethod.GET)
    public String redirectSwagger() {
        return "redirect:/swagger-ui/index.html";
    }
}
