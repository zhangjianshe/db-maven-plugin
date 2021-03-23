package cn.mapway.tools.doc.types;

import cn.mapway.tools.doc.model.BaseData;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * ApiType
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class ApiType extends BaseData {
    String typeName;
    String author;
    String example;
    Map<String, ApiType> fields;

    ApiType(String typeName) {
        this.typeName = typeName;
        fields = new HashMap<>();
        example = "";
        author = "";
    }

    public void addField(String fieldName, ApiType apiType) {
        fields.put(fieldName, apiType);
    }
}
