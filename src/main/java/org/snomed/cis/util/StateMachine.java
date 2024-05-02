package org.snomed.cis.util;

import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Map.entry;
@Component
public class StateMachine {
    public Map<String,String> statuses=Map.ofEntries(
   entry("available","Available"),
            entry("assigned","Assigned"),
    entry("reserved","Reserved"),
    entry("published","Published"),
    entry("deprecated","Deprecated")
    );

    public Map<String,String> actions=Map.ofEntries(
            entry("register","Register"),
            entry("generate","Generate"),
            entry("deprecate","Deprecate"),
            entry("publish","Publish"),
            entry("release","Release"),
            entry("reserve","Reserve")
    );

    public Map<String,String> map1 = Map.ofEntries(
            entry(actions.get("register"),statuses.get("assigned")),
            entry(actions.get("generate"),statuses.get("assigned")),
            entry(actions.get("reserve"),statuses.get("reserved"))
    );

    public Map<String,String> map2 = Map.ofEntries(
            entry(actions.get("deprecate"),statuses.get("deprecated")),
            entry(actions.get("publish"),statuses.get("published")),
            entry(actions.get("release"),statuses.get("available"))
    );

    public Map<String,String> map3 = Map.ofEntries(
            entry(actions.get("release"),statuses.get("available")),
            entry(actions.get("register"),statuses.get("assigned"))
    );

    public Map<String,String> map4 = Map.ofEntries(
            entry(actions.get("deprecate"),statuses.get("deprecated"))
    );

    public Map<String,String> map5 = Map.ofEntries(
            entry(actions.get("publish"),statuses.get("published"))
    );
    public Map<String,Map> state = Map.ofEntries(
            entry(statuses.get("available"),map1),
            entry(statuses.get("assigned"),map2),
            entry(statuses.get("reserved"),map3),
            entry(statuses.get("published"),map4),
            entry(statuses.get("deprecated"),map5)
    );


    public String getNewStatus(String status, String action){
        String newStat=null;
        status = status.substring(0,1).toUpperCase() + status.substring(1);
        if (!(state.get(status).isEmpty())){
            var actions=state.get(status);
            if(null!=actions.get(action))
                newStat = (String) actions.get(action);
        }
        return newStat;
    };
}

