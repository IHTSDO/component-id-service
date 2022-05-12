package com.snomed.api.controller;

import com.snomed.api.controller.dto.ResultDto;
import com.snomed.api.exception.APIException;
import com.snomed.api.service.SctidService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Stats", value = "Stats")
@RestController
@RequestMapping(path = "/api")
public class StatsController {

        @Autowired
        SctidService sctidService;


        @GetMapping("/stats")
        @ResponseBody
        public ResultDto getStats(@RequestParam String token, @RequestParam String username) throws APIException {
            return sctidService.getStats(token,username);
        }


}
