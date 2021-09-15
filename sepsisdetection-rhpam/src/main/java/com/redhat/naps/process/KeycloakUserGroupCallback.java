package com.redhat.naps.process;

import java.util.List;
import java.util.Set;

import com.redhat.naps.process.util.FHIRUtil;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.jbpm.springboot.security.SpringSecurityIdentityProvider;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.identity.IdentityProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/* 
 * This is a substitute for:   org.jbpm.springboot.security.SpringSecurityUserGroupCallback
 * In order for SpringSecurityUserGroupCallback to utilize roles from JWT (when using keycloak), need to ensure the following setting is configured in the SSO client of the keycloak SSO Realm:
 *     "fullScopeAllowed":true
 */
public class KeycloakUserGroupCallback implements UserGroupCallback {

    private final static Logger log = LoggerFactory.getLogger(KeycloakUserGroupCallback.class);
    
    private Set<String> availableGroups = new HashSet<String>();

    private SpringSecurityIdentityProvider provider;

    public KeycloakUserGroupCallback(IdentityProvider x) {

        this.provider = (SpringSecurityIdentityProvider)x;

        String groups = System.getProperty(FHIRUtil.AVAILABLE_SSO_GROUPS);
        if(StringUtils.isNotEmpty(groups)){
            String[] gArray = groups.split(",");
            for(String group : gArray){
                availableGroups.add(group);
            }
        }
        log.info("constructor() provider = "+provider+ " :  # of groups = "+availableGroups.size());
    }

    @Override
    // When a task is created, verify that groupIds / roles assigned to that task are actually registered in Identity Provider
    public boolean existsGroup(String groupId) {
        /*StackTraceElement[] sTraceArray = new Throwable().getStackTrace();
        for(StackTraceElement sTrace : sTraceArray){
            System.out.println(sTrace.toString());
        }*/
        log.info("existsGroup() group = "+groupId+" = "+availableGroups.contains(groupId));
        return availableGroups.contains(groupId);
    }

    @Override
    // When a task is created, verify that userId assigned to task is actually registered in Identity Provider
    public boolean existsUser(String userId) {
        log.info("existsUser() : " + userId);
        if (userId.equals("kieserver") || userId.equals("wbadmin") || userId.equals("user") || userId.equals("Administrator"))
            return true;
        else
            return false;
    }

    @Override
    // Return all roles associated with an authenticated user attempting to manage tasks
    public List<String> getGroupsForUser(String userId) {
        /*StackTraceElement[] sTraceArray = new Throwable().getStackTrace();
        for(StackTraceElement sTrace : sTraceArray){
            System.out.println(sTrace.toString());
        }*/
        List<String> groupList = provider.getRoles();
        log.info("getGroupsForUser() "+userId+"'s groups =  " + Arrays.toString(groupList.toArray()));
        return groupList;
    }

}
