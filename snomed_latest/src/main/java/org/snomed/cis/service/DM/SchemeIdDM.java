package org.snomed.cis.service.DM;

import org.snomed.cis.domain.SchemeId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Component
public class SchemeIdDM {

@Autowired
    EntityManager entityManager;

   public List<SchemeId> getSchemeIds(String systemId, String limit, String skip)
    {

    List<SchemeId> schemeList = new ArrayList<>();
        String objQuery = "";
        var limitR = 100;
        var skipTo = 0;
        if (null!=limit && !limit.isEmpty())
            limitR = Integer.parseInt(limit);
        if (null!=skip && !skip.isEmpty())
            skipTo = Integer.parseInt(skip);
        String swhere ="";
        if (!systemId.isEmpty() && null!=systemId) {
            objQuery = systemId;
            swhere += " And " + "systemId" + "=" +"'"+ (objQuery)+"'";
        }
        if (swhere!=""){
            swhere = " WHERE " + swhere.substring(5);
        }
        String sql;
        if (limitR>0 && (skipTo==0)) {
            sql = "SELECT * FROM schemeid" + swhere + " order by schemeId limit " + limit;
        }else{
            sql = "SELECT * FROM schemeid" + swhere + " order by schemeId";
        }
        Query genQuery = entityManager.createNativeQuery(sql,SchemeId.class);
        SchemeId resultList = (SchemeId) genQuery.getResultList();
        //System.out.println("tst:"+genQuery.getResultList().get(0).toString());
        /*if ((skipTo==0)) {
            schemeList = (List<SchemeId>) resultList;
        }else {
            var cont = 1;
            List<SchemeId> newRows = new ArrayList<>();
            for (var i = 0; i < resultList.size(); i++) {
                if (i >= skipTo) {
                    if (null != limit && limitR > 0 && limitR < cont) {
                        break;
                    }
                    newRows.add(resultList.get(i));
                    cont++;
                }
            }
            schemeList = newRows;
        }*/
        return schemeList;
    }
}
