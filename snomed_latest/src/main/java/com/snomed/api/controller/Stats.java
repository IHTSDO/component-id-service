package com.snomed.api.controller;

import com.snomed.api.exception.APIException;
import com.snomed.api.service.BulkSctidService;
import com.snomed.api.service.SctidService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Api(tags = "Stats", value = "Stats")
@RestController
@RequestMapping(path = "/api")
public class Stats {
    @Autowired
    SctidService sctidService;

    /*@ApiOperation(
            value="Bulk Job Service",
            notes="Returns a list bulk jobs"
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; bullJobService &ndash; the list of descendants of the concept<br>",tags = { "Bulk Job Operations" })
    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })
    @GetMapping("/stats")
    @ResponseBody
    public List getStats(@RequestParam String token, @RequestParam String username) throws APIException {
        List result = new ArrayList();
        result.add("lakshmana");
        result.add("keerthika");*/
        //return sctidService.getStats(token,username);
    //}

}
