package cn.mapway.tools.doc.model;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.utils.Pair;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data
 *
 * @author zhangjianshe@gmail.com
 */
@Slf4j
public class DataItem {

    private static Map<String, DataType> basicTypes = new HashMap<>();

    static {
        basicTypes.put("String", DataType.StringType);
        basicTypes.put("Integer", DataType.IntType);
        basicTypes.put("int", DataType.IntType);
        basicTypes.put("Long", DataType.LongType);
        basicTypes.put("long", DataType.LongType);
        basicTypes.put("Float", DataType.FloatType);
        basicTypes.put("float", DataType.FloatType);
        basicTypes.put("Date", DataType.DateType);
        basicTypes.put("Timestamp", DataType.DateType);
        basicTypes.put("Double", DataType.DoubleType);
        basicTypes.put("double", DataType.DoubleType);


    }

    public static DataItem parseType(Type returnType) {
        if (returnType.isPrimitiveType()) {
            return parseSimpleType(returnType);
        }
        if (returnType.isClassOrInterfaceType()) {
            return parseObjectType(returnType);
        }
        return null;

    }

    private static ObjectData parseObjectType(Type returnType) {
        ObjectData obj = new ObjectData();
        ResolvedType resolve = returnType.resolve();
        if (resolve.isReferenceType()) {
            ResolvedReferenceType resolvedReferenceType = resolve.asReferenceType();
            List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap = resolvedReferenceType.getTypeParametersMap();

            Set<ResolvedFieldDeclaration> declaredFields = resolvedReferenceType.getDeclaredFields();
            for (ResolvedFieldDeclaration f : declaredFields) {
                extractField(f, obj, typeParametersMap);

            }

        }
        return obj;
    }

    private static void extractField(ResolvedFieldDeclaration f, ObjectData obj, List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        String name = f.getName();

        if (f instanceof JavaParserFieldDeclaration) {
            JavaParserFieldDeclaration jpfd = (JavaParserFieldDeclaration) f;
            VariableDeclarator variableDeclarator = jpfd.getVariableDeclarator();
            SimpleData simpleData = new SimpleData();
            simpleData.setName(name);
            simpleData.setDataType(fromParserType(variableDeclarator.getType(),typeParametersMap));
            obj.addField(simpleData);
        } else {
            ResolvedType type = f.getType();

            SimpleData simpleData = new SimpleData();
            simpleData.setName(name);
            simpleData.setDataType(fromResolveType(type,typeParametersMap));
            obj.addField(simpleData);
        }
    }

    private static DataType fromParserType(Type type, List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) type;
            String typeName = classOrInterfaceType.getName().asString();
            for (Map.Entry e : basicTypes.entrySet()) {
                if (e.getKey().equals(typeName)) {
                    return (DataType) e.getValue();
                }
            }
            for( Pair<ResolvedTypeParameterDeclaration, ResolvedType> typeParameter: typeParametersMap)
            {
                if(typeName.equals(typeParameter.a.getName()))
                {
                    return DataType.ObjectType;
                }
            }
        }
        return DataType.StringType;
    }

    private static DataType fromResolveType(ResolvedType type,List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap) {
        log.info(type.describe());
        return DataType.StringType;
    }

    private static SimpleData parseSimpleType(Type returnType) {
        return null;
    }
}
