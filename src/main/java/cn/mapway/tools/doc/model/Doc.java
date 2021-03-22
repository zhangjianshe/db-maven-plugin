package cn.mapway.tools.doc.model;

import org.nutz.lang.Strings;

import java.util.*;

/**
 * Doc
 * 文档对象
 *
 * @author zhangjianshe@gmail.com
 */
public class Doc extends BaseData {
    Group root;

    int depth = 1;
    int count = 0;

    public Doc() {
        root = new Group();
        root.setFullName("/");
        root.setName("");
    }

    /**
     * 处理Group信息
     *
     * @param entries
     * @return
     */
    public static final Doc parseEntries(List<Entry> entries) {
        Doc doc = new Doc();
        for (Entry entry : entries) {
            String groupPath = entry.getGroup();
            Group g = doc.findGroup(groupPath);
            g.addEntry(entry);
        }
        doc.count = entries.size();
        return doc;
    }


    public Group findGroup(String groupPath) {
        if (Strings.isBlank(groupPath)) {
            return root;
        }

        String[] items = Strings.split(groupPath, false, false, '/');
        if (depth < items.length) {
            depth = items.length;
        }
        Group g = root;
        for (String item : items) {
            g = sureGroup(g, item);
        }
        return g;
    }

    private Group sureGroup(Group parent, String name) {
        for (int i = 0; i < parent.subGroupCount(); i++) {
            Group g = parent.subGroup(i);
            if (g.getName().equals(name)) {
                return g;
            }
        }
        Group g = new Group(name);
        g.setFullName(parent.getFullName() + name + "/");
        parent.addSubGroup(g);
        return g;
    }

    public void sort() {
        innerSort(root);
    }

    private void innerSort(Group root) {

        Collections.sort(root.getEntries(), Comparator.comparing(BaseData::toComment));
        Collections.sort(root.getSubGroups(), Comparator.comparing(Group::getName));
        for (Group g : root.getSubGroups()) {
            innerSort(g);
        }
    }

    /**
     * 输出HTML TABLE
     * Group  Group  POST  COMMENT URL AUTHOR NOTES
     * <p>
     * root
     * 1->
     * 2->
     * 3-4-5
     * ->
     *
     * @return
     */
    public String toHtml() {

        sort();
        List<TableRow> table = new ArrayList<>();
        addGroupEntry(root, table);

        CatCell[][] catCells = new CatCell[table.size()][depth + 1];
        //处理分类信息
        //填充所有的目录表格
        for (int i = 0; i < table.size(); i++) {
            catCells[i] = new CatCell[depth + 1];
            TableRow tr = table.get(i);
            List<Group> groups = tr.entry.allParents();
            groups.stream().forEach(t -> System.out.print(t.getName() + "\t"));
            System.out.println();
            for (int j = 0; j < groups.size(); j++) {
                Group g = groups.get(j);
                CatCell cell = new CatCell();
                cell.setGroup(g);
                catCells[i][j] = cell;
            }
            for (int j = groups.size(); j < depth + 1; j++) {
                CatCell cell = new CatCell();
                cell.setGroup(new Group(""));
                catCells[i][j] = cell;
            }
            Arrays.stream(catCells[i]).map(t -> t.group.getName()).forEach(t -> System.out.print(t + "\t"));
            System.out.println("\r\n");
        }

        for (int j = 0; j < depth + 1; j++) {//分析表格的SPAN值 按列分析
            if (table.size() <= 1) {
                break;
            }
            int rowspan = 1;
            CatCell currentCell = catCells[0][j];
            int row = 1;
            while (row < table.size()) {
                CatCell catCell = catCells[row++][j];
                //如果前一列再次有分隔,则本列也分割
                if (j > 0 && catCells[row - 1][j - 1].group != null) {
                    currentCell.rowspan = rowspan;
                    currentCell = catCell;
                    rowspan = 1;
                    continue;
                }
                if (catCell.group.equals(currentCell.group)) { //
                    catCell.group = null;
                    rowspan++;
                } else {
                    currentCell.rowspan = rowspan;
                    currentCell = catCell;
                    rowspan = 1;
                }
            }
            //结束了 赋值最后一个SPAN
            currentCell.rowspan = rowspan;
        }


        StringBuilder sb = new StringBuilder();
        sb.append("<table border=1 style='border-collapse:collapse'>");
        //输出表头
        sb.append("<tr style='font-weight:bold;'>");
        sb.append("<td colspan=" + depth + ">分类</td>");
        sb.append("<td style='padding:3px'>编号</td>");
        sb.append("<td style='padding:3px'>名称</td>");
        sb.append("<td style='padding:3px'>方法</td>");
        sb.append("<td style='padding:3px'>URL</td>");
        sb.append("<td style='padding:3px'>作者</td>");
        sb.append("<td style='padding:3px'>标签</td>");
        sb.append("<td style='padding:3px'>说明</td>");
        sb.append("</tr>");
        int row = 0;
        for (TableRow r : table) {
            sb.append(r.toHtml(catCells, row++));
        }
        sb.append("</table>");
        return sb.toString();
    }

    private void addGroupEntry(Group group, List<TableRow> table) {
        for (Entry e : group.getEntries()) {
            table.add(new TableRow(e));
        }
        for (Group subGroup : group.getSubGroups()) {
            addGroupEntry(subGroup, table);
        }
    }
}
