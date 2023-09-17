package com.hekmatullahamin.click.utils;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String groupName;
    private String groupProfile;
    private String groupAdminId;
    private String groupId;
    private ArrayList<String> groupMembers;

    public Group() {
    }

    public Group(String groupName, String groupProfile, String groupAdminId, String groupId, ArrayList<String> groupMembers) {
        this.groupName = groupName;
        this.groupProfile = groupProfile;
        this.groupAdminId = groupAdminId;
        this.groupId = groupId;
        this.groupMembers = groupMembers;
    }

    public Group(String groupName, String groupProfile, String groupAdminId, String groupId) {
        this.groupName = groupName;
        this.groupProfile = groupProfile;
        this.groupAdminId = groupAdminId;
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupProfile() {
        return groupProfile;
    }

    public void setGroupProfile(String groupProfile) {
        this.groupProfile = groupProfile;
    }

    public String getGroupAdminId() {
        return groupAdminId;
    }

    public void setGroupAdminId(String groupAdminId) {
        this.groupAdminId = groupAdminId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public ArrayList<String> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(ArrayList<String> groupMembers) {
        this.groupMembers = groupMembers;
    }
}
