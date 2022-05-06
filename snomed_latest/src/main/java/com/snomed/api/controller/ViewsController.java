package com.snomed.api.controller;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import java.io.IOException;

@Controller
public class ViewsController {
   // @Autowired
    //HandlebarsViewResolver handlebarsViewResolver;

    /*@RequestMapping(value="/views/items/main.hbs")
    public static String welcome() {
        return "main";
    }*/
    TemplateLoader viewsItemsLoader = new ClassPathTemplateLoader("/static/views/items", ".hbs");
    TemplateLoader viewsHomeLoader = new ClassPathTemplateLoader("/static/views/home", ".hbs");
    TemplateLoader generateIdsLoader = new ClassPathTemplateLoader("/static/views/generateIds", ".hbs");
    TemplateLoader bulkLoader = new ClassPathTemplateLoader("/static/views/bulk", ".hbs");
    TemplateLoader adminLoader = new ClassPathTemplateLoader("/static/views/admin", ".hbs");
    TemplateLoader reportsLoader = new ClassPathTemplateLoader("/static/views/reports", ".hbs");
    TemplateLoader searchIdsLoader = new ClassPathTemplateLoader("/static/views/searchIds", ".hbs");

    @GetMapping("/views/searchIds/formToFill.hbs")
    @ResponseBody
    public String formToFillHbs() throws IOException {
        TemplateSource source = reportsLoader.sourceAt("formToFill");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/searchIds/main.hbs")
    @ResponseBody
    public String searchIdsMainHbs() throws IOException {
        TemplateSource source = reportsLoader.sourceAt("main");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/reports/body.hbs")
    @ResponseBody
    public String reportsBodyHbs() throws IOException {
        TemplateSource source = reportsLoader.sourceAt("body");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/reports/main.hbs")
    @ResponseBody
    public String reportsMainHbs() throws IOException {
        TemplateSource source = reportsLoader.sourceAt("main");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/admin/detailsList.hbs")
    @ResponseBody
    public String adminDetailsListHbs() throws IOException {
        TemplateSource source = adminLoader.sourceAt("detailsList");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/admin/main.hbs")
    @ResponseBody
    public String adminMainHbs() throws IOException {
        TemplateSource source = adminLoader.sourceAt("main");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/admin/modalBody.hbs")
    @ResponseBody
    public String adminModalBodyHbs() throws IOException {
        TemplateSource source = adminLoader.sourceAt("modalBody");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/bulk/details.hbs")
    @ResponseBody
    public String detailsHbs() throws IOException {
        TemplateSource source = bulkLoader.sourceAt("details");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/bulk/list.hbs")
    @ResponseBody
    public String bulkListHbs() throws IOException {
        TemplateSource source = bulkLoader.sourceAt("list");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/bulk/main.hbs")
    @ResponseBody
    public String bulkMainHbs() throws IOException {
        TemplateSource source = bulkLoader.sourceAt("main");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/bulk/records.hbs")
    @ResponseBody
    public String bulkRecordsHbs() throws IOException {
        TemplateSource source = bulkLoader.sourceAt("records");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/generateIds/main.hbs")
    @ResponseBody
    public String generateIdsMainHbs() throws IOException {
        TemplateSource source = generateIdsLoader.sourceAt("main");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/home/profile.hbs")
    @ResponseBody
    public String profileHbs() throws IOException {
        TemplateSource source = viewsHomeLoader.sourceAt("profile");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/items/main.hbs")
    @ResponseBody
    public String mainHbs() throws IOException {
        TemplateSource source = viewsItemsLoader.sourceAt("main");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/items/list-schemes.hbs")
    @ResponseBody
    public String listSchemesHbs() throws IOException {
        TemplateSource source = viewsItemsLoader.sourceAt("list-schemes");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/items/list-namespaces.hbs")
    @ResponseBody
    public String testView() throws IOException {
        TemplateSource source = viewsItemsLoader.sourceAt("list-namespaces");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/items/permissions.hbs")
    @ResponseBody
    public String permissionsHbs() throws IOException {
        TemplateSource source = viewsItemsLoader.sourceAt("permissions");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/items/groupPermissions.hbs")
    @ResponseBody
    public String groupPermissionsHbs() throws IOException {
        TemplateSource source = viewsItemsLoader.sourceAt("groupPermissions");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/items/details-schemes.hbs")
    @ResponseBody
    public String detailsSchemesHbs() throws IOException {
        TemplateSource source = viewsItemsLoader.sourceAt("details-schemes");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/items/details-namespaces.hbs")
    @ResponseBody
    public String detailsNamespacesHbs() throws IOException {
        TemplateSource source = viewsItemsLoader.sourceAt("details-namespaces");
        String cont = source.content();
        return cont;
    }

    @GetMapping("/views/items/addPermission.hbs")
    @ResponseBody
    public String addPermissionHbs() throws IOException {
        TemplateSource source = viewsItemsLoader.sourceAt("addPermission");
        String cont = source.content();
        return cont;
    }
}
