package cn.mapway.tools.doc.model;

import lombok.Data;
import org.nutz.lang.Lang;

import java.util.ArrayList;
import java.util.List;

/**
 * TableRow
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class TableRow {
    Entry entry;
    List<String> cells;


    TableRow(Entry e) {
        entry = e;


        cells = new ArrayList<>(6);
        cells.add(Lang.concat(e.comments.toArray()).toString());
        cells.add(Lang.concat(e.methods.toArray()).toString());
        cells.add(e.getPath());
        cells.add(e.getAuthor());
        cells.add(Lang.concat(e.getTags().toArray()).toString());
        cells.add(Lang.concat(e.getNotes().toArray()).toString());
    }


    /**
     * @param catCells
     * @param i
     * @return
     */
    public String toHtml(CatCell[][] catCells, int i) {

        StringBuilder sb = new StringBuilder();
        sb.append("<tr>");


        int c = 0;
        for (CatCell cell : catCells[i]) {
            if (c == 0) {
                c++;
                continue;
            }
            if (cell.group != null) {
                sb.append("<td style='padding:3px;' rowspan='" + cell.rowspan + "' colspan='" + cell.colspan + "'>")
                        .append(cell.group.getName()).append("</td>");
            }
            c++;
        }
        sb.append("<td style='padding:3px;'>").append((i + 1) + "").append("</td>");
        for (String s : cells) {
            sb.append("<td style='padding:3px;'>").append(s).append("</td>");
        }
        sb.append("</tr>");
        return sb.toString();
    }
}
