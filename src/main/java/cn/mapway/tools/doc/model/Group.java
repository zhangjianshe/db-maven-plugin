package cn.mapway.tools.doc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Group
 * 接口的集合
 *
 * @author zhangjianshe@gmail.com
 */

public class Group {
    private List<Group> subGroups;
    private String name;
    private String fullName;
    private List<String> tags;
    private List<Entry> entries;
    private Group parent;

    public Group() {
        init();
    }

    public Group(String name) {
        init();
        this.name = name;
    }

    private void init() {
        parent = null;
        name = "";
        fullName = "";
        tags = new ArrayList<>();
        entries = new ArrayList<>();
        subGroups = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return this.fullName;
    }

    protected void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int subGroupCount() {
        return subGroups.size();
    }

    public Group subGroup(int index) {
        if (index >= 0 && index < subGroups.size()) {
            return subGroups.get(index);
        }
        return null;
    }

    public void addSubGroup(Group g) {
        if (g == null) {
            return;
        }
        g.setParent(this);
        subGroups.add(g);
        g.setFullName(this.getFullName() + "/" + g.name);
    }

    public void addEntry(Entry entry) {
        entry.setParent(this);
        this.entries.add(entry);
    }


    public List<Entry> getEntries() {
        return entries;
    }

    public List<Group> getSubGroups() {
        return subGroups;
    }

    public Group getParent() {
        return parent;
    }

    public void setParent(Group parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return
                Objects.equals(name, group.name) &&
                        Objects.equals(fullName, group.fullName);
    }

}
