package org.snomed.cis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.BuildVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@Api(tags = "Version", description = "Build Version")
public class VersionController {
	private final Logger logger = LoggerFactory.getLogger(VersionController.class);

	@Autowired
	BuildProperties buildProperties;

	@ApiOperation("Software build version and build timestamp.")
	@RequestMapping(value = "/version", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public BuildVersion getBuildInformation(@RequestParam String token, @ApiIgnore Authentication authentication) {
		logger.info("/version -> {}", buildProperties.getVersion());
		return new BuildVersion(buildProperties.getVersion(), buildProperties.getTime().toString());
	}
}
