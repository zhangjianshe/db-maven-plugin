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

    /**
     * 初始化
     */
    private void init() {
        parent = null;
        name = "";
        fullName = "";
        tags = new ArrayList<>();
        entries = new ArrayList<>();
        subGroups = new ArrayList<>();
    }

    /**
     * 得到的名字
     *
     * @return {@link String}
     */
    public String getName() {
        return this.name;
    }

    /**
     * 集名称
     *
     * @param name 的名字
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 得到的全名
     *
     * @return {@link String}
     */
    public String getFullName() {
        return this.fullName;
    }

    protected void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * 子组数
     *
     * @return int
     */
    public int subGroupCount() {
        return subGroups.size();
    }

    /**
     * 子组
     *
     * @param index 指数
     * @return {@link Group}
     */
    public Group subGroup(int index) {
        if (index >= 0 && index < subGroups.size()) {
            return subGroups.get(index);
        }
        return null;
    }

    /**
     * 添加子组
     *
     * @param g g
     */
    public void addSubGroup(Group g) {
        if (g == null) {
            return;
        }
        g.setParent(this);
        subGroups.add(g);
        g.setFullName(this.getFullName() + "/" + g.name);
    }

    /**
     * 添加条目
     *
     * @param entry 条目
     */
    public void addEntry(Entry entry) {
        entry.setParent(this);
        this.entries.add(entry);
    }


    /**
     * 得到的条目
     *
     * @return data
     */
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * 得到的子组
     *
     * @return data
     */
    public List<Group> getSubGroups() {
        return subGroups;
    }

    /**
     * 得到父母
     *
     * @return {@link Group}
     */
    public Group getParent() {
        return parent;
    }

    /**
     * 设置父
     *
     * @param parent 父
     */
    public void setParent(Group parent) {
        this.parent = parent;
    }

    /**
     * =
     *
     * @param o o
     * @return boolean
     */
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
