package org.snomed.cis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetStatsResponseDto {

    private HashMap<String, Long> namespaces;

    private Long schemes;

    private Long users;

    public GetStatsResponseDto() {
        this.schemes = 2L;
        this.users = 0L;
    }

}
