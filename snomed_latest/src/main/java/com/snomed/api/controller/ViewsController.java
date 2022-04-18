package com.snomed.api.controller;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ViewsController {
   // @Autowired
    //HandlebarsViewResolver handlebarsViewResolver;

    /*@GetMapping("/main.hbs")
    @ResponseBody
    public Template getMainHbs() throws IOException {
        Handlebars handlebars = new Handlebars();
       Template template = handlebars.compile("/views/items/main.hbs");
        return template;
    }*/
}
