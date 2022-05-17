package com.snomed.api.domain;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class SchemeIdKey implements Serializable {

    private String scheme;

    private String schemeId;

}
