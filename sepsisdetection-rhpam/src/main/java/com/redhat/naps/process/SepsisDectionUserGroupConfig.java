package com.redhat.naps.process;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.kie.api.task.UserGroupCallback;
import org.kie.internal.identity.IdentityProvider;

public class SepsisDectionUserGroupConfig implements UserGroupCallback {

    private IdentityProvider provider;

    public SepsisDectionUserGroupConfig(IdentityProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean existsGroup(String arg0) {
        if (arg0.equals("surgeon")) // Add New Groups here
            return true;
        else
            return false;
    }

    @Override
    public boolean existsUser(String arg0) {
        System.out.println("Checking User : " + arg0);
        if (arg0.equals("kieserver") || arg0.equals("wbadmin") || arg0.equals("user") || arg0.equals("Administrator"))
            return true;
        else
            return false;
    }

    @Override
    public List<String> getGroupsForUser(String arg0) {
        System.out.println("Getting Group for user : " + arg0);
        List<String> groupList = new ArrayList<String>();
        if (arg0.equals("kieserver")) {
            groupList.add("approver");
            groupList.add("broker");
        }
        System.out.println("List of Groups : " + Arrays.toString(groupList.toArray()));
        return groupList;
    }

}
