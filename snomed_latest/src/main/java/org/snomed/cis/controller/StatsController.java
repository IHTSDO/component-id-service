package org.snomed.cis.controller;

import org.snomed.cis.controller.dto.ResultDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.service.SctidService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Stats", value = "Stats")
@RestController
@RequestMapping(path = "/api")
public class StatsController {

        @Autowired
        SctidService sctidService;


        @GetMapping("/stats")
        @ResponseBody
        public ResultDto getStats(@RequestParam String token, @RequestParam String username) throws CisException {
            return sctidService.getStats(token,username);
        }


}
