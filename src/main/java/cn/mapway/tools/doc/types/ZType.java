package cn.mapway.tools.doc.types;

import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ZType
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class ZType {
    String typeName;
    String example;
    List<String> comments;
    List<String> notes;

    Map<String, ZType> field;
    Map<String, ZType> typeParameters;

    public ZType() {
        typeParameters = new HashMap<>();
    }

    public static ZType fromParseType(JavaParserFieldDeclaration javaParserFieldDeclaration)
    {

    }
}
