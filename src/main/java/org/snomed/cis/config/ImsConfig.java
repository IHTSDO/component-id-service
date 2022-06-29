package org.snomed.cis.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ImsConfig {

    @Value("${ims.cookiename}")
    private String cookieName;

    @Value("${ims.urls.base}")
    private String baseUrl;

    @Value("${ims.urls.login}")
    private String loginUrl;

    @Value("${ims.urls.logout}")
    private String logoutUrl;

    @Value("${ims.urls.authenticate}")
    private String authenticateUrl;

}
