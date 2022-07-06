package org.snomed.cis.dto;

import java.util.HashMap;

public class ResultDto {
    private HashMap<String,Long> namespaces;
    private Long schemes = Long.valueOf(2);
    private Long users = Long.valueOf(0);

    public HashMap<String,Long> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(HashMap<String,Long> namespaces) {
        this.namespaces = namespaces;
    }

    public Long getSchemes() {
        return schemes;
    }

    public void setSchemes(Long schemes) {
        this.schemes = schemes;
    }

    public Long getUsers() {
        return users;
    }

    public void setUsers(Long users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "{" +
                "namespaces size=" + (namespaces==null?"0":namespaces.size()) +
                ", schemes=" + schemes +
                ", users=" + users +
                '}';
    }
}
