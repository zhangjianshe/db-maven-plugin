package cn.mapway.tools.db;

import cn.mapway.tools.db.naming.CamelConvert;
import cn.mapway.tools.db.naming.INameConvertor;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.squareup.javapoet.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.*;
import schemacrawler.schemacrawler.*;
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource;
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * NutzImpl
 *
 * @author zhangjianshe@gmail.com
 */
@Slf4j
public class NutzImpl {
    IConfigure configure;
    INameConvertor camelConvert = new CamelConvert();

    public NutzImpl(IConfigure configure) {
        this.configure = configure;
    }

    private Connection getConnection() {
        final String connectionUrl = configure.getJdbcUrl();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        System.out.println(Json.toJson(drivers));
        final DatabaseConnectionSource dataSource = new DatabaseConnectionSource(connectionUrl);

        dataSource.setUserCredentials(new SingleUseUserCredentials(configure.getUser(), configure.getPassword()));
        return dataSource.get();
    }

    public void run() {
        log.info("Nutz export");
        if (Strings.isBlank(configure.getSchema())) {
            log.warn("没有定义数据库schema,本次操作不会生成任何DAO代码");
            return;
        }

        final List<String> includes = configure.includes();
        final List<String> excludes = configure.excludes();

        // Create the options
        final LimitOptionsBuilder limitOptionsBuilder =
                LimitOptionsBuilder.builder()
                        .includeSchemas(new RegularExpressionInclusionRule(configure.getSchema()))
                        .includeTables(tableFullName -> {
                            if (tableFullName.contains("$"))
                                return false;
                            int index = tableFullName.lastIndexOf('.');

                            String tableName = tableFullName;
                            if (index >= 0) {
                                tableName = tableFullName.substring(index + 1);
                            }
                            if (includes.size() > 0) {
                                return inList(includes, tableName);
                            } else {
                                return true;
                            }
                        });

        final LoadOptionsBuilder loadOptionsBuilder =
                LoadOptionsBuilder.builder()
                        // Set what details are required in the schema - this affects the
                        // time taken to crawl the schema
                        .withSchemaInfoLevel(SchemaInfoLevelBuilder.detailed());

        final SchemaCrawlerOptions options =
                SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                        .withLimitOptions(limitOptionsBuilder.toOptions())
                        .withLoadOptions(loadOptionsBuilder.toOptions());


        // Get the schema definition
        final Catalog catalog;
        try {
            catalog = SchemaCrawlerUtility.getCatalog(getConnection(), options);
        } catch (SchemaCrawlerException e) {
            e.printStackTrace();
            return;
        }

        for (final Schema schema : catalog.getSchemas()) {

            for (final Table table : catalog.getTables(schema)) {
                String tableName = table.getName();
                if (inList(excludes, tableName)) {
                    continue;
                }

                if (includes.size() > 0) {
                    if (!inList(includes, tableName)) {
                        continue;
                    }
                }
                exportEntity(table, configure);
                exportDao(table, configure);
            }
        }
    }

    /**
     * 输出DAO
     *
     * @param table
     * @param configure
     */
    private void exportDao(Table table, IConfigure configure) {
        if (Strings.isBlank(configure.daoPackage())) {
            //只有配置DAO Package 才会输出DAO
            return;
        }
        String fileName = getClassTypeName(table.getName()) + "Dao";

        ClassName entityName = ClassName.get(configure.entityPackage(), getClassTypeName(table.getName()) + "Entity");

        ClassName parentClassName = ClassName.get("cn.mapway.dao", "BaseDao");
        ParameterizedTypeName t = ParameterizedTypeName.get(parentClassName,entityName);

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(fileName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(t);


        typeBuilder.addAnnotation(AnnotationSpec.builder(Component.class).build());


        if (!configure.overrideDao()) {//不覆盖
            Boolean exist = isFileExist(configure.daoPath(), configure.daoPackage() , fileName + ".java");
            if (exist) {
                log.warn("存在Dao文件" + fileName);
                return;
            }
        }

        try {
            JavaFile javaFile = JavaFile.builder(configure.daoPackage() , typeBuilder.build())
                    .skipJavaLangImports(true)
                    .build();
            javaFile.writeTo(new File(configure.daoPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件存在
     *
     * @param daoPath    刀路
     * @param daoPackage dao包
     * @param fileName   文件名称
     * @return {@link Boolean}
     */
    private Boolean isFileExist(String daoPath, String daoPackage, String fileName) {
        Path outputDirectory = new File(daoPath).toPath();
        if (!daoPackage.isEmpty()) {
            String temp = daoPackage.replace(".", File.separator);
            outputDirectory = outputDirectory.resolve(temp);
            try {
                Files.createDirectories(outputDirectory);
            } catch (IOException e) {
            }
        }

        Path outputPath = outputDirectory.resolve(fileName);
        return outputPath.toFile().exists();

    }

    /**
     * 出口单位
     *
     * @param table     表格
     * @param configure 配置
     */
    private void exportEntity(Table table, IConfigure configure) {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(getClassTypeName(table.getName()) + "Entity")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(
                        AnnotationSpec.builder(org.nutz.dao.entity.annotation.Table.class).addMember("value", "value=$S", table.getName()).build());
        typeBuilder.addJavadoc("<b>$L.$L</b>\r\n$L\r\n$L\r\n@author $L", table.getSchema().getCatalogName(), table.getName(), u(table.getRemarks()), table.getDefinition(), configure.author());

        if (configure.lombok()) {
            typeBuilder.addAnnotation(AnnotationSpec.builder(Data.class).build());
            if (table.getColumns().size() <= 15) {
                typeBuilder.addAnnotation(AnnotationSpec.builder(Builder.class).build());
                typeBuilder.addAnnotation(AnnotationSpec.builder(NoArgsConstructor.class).build());
                typeBuilder.addAnnotation(AnnotationSpec.builder(AllArgsConstructor.class).build());
            }
            typeBuilder.addAnnotation(AnnotationSpec.builder(Accessors.class).addMember("chain", "true").build());
        } else {
            //需要添加 get set方法
            for (Column column : table.getColumns()) {
                MethodSpec.Builder builder = MethodSpec.methodBuilder("get" + getClassTypeName(column.getName()));
                builder.returns(getDataType(column, column.getColumnDataType()));
                builder.addModifiers(Modifier.PUBLIC);
                builder.addStatement("return this.$L", camelConvert.convert(column.getName()));

                typeBuilder.addMethod(builder.build());
                builder = MethodSpec.methodBuilder("set" + getClassTypeName(column.getName()));
                builder.returns(TypeName.VOID);
                builder.addModifiers(Modifier.PUBLIC);
                builder.addParameter(getDataType(column, column.getColumnDataType()), camelConvert.convert(column.getName()));
                builder.addStatement("this.$L=$L;", camelConvert.convert(column.getName()), camelConvert.convert(column.getName()));
                typeBuilder.addMethod(builder.build());
            }
        }

        //输出静态字段名称
        if (configure.withStaticField()) {
            for (Column column : table.getColumns()) {
                FieldSpec.Builder fieldBuilder = FieldSpec.builder(String.class, "FLD_" + column.getName().toUpperCase(), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", column.getName());
                fieldBuilder.addJavadoc("$L\r\n数据库字段序号:$L", column.getName(), "" + column.getOrdinalPosition());
                typeBuilder.addField(fieldBuilder.build());
            }
        }

        PrimaryKey primaryKey = table.getPrimaryKey();
        if (primaryKey.getColumns().size() > 1) {
            //有联合主键
            AnnotationSpec.Builder unniColumns = AnnotationSpec.builder(PK.class);
            StringBuilder sb = new StringBuilder();
            sb.append("{");

            for (TableConstraintColumn c : primaryKey.getColumns()) {
                if (sb.length() > 1) {
                    sb.append(",");
                }
                sb.append("\"").append(camelConvert.convert(c.getName())).append("\"");
            }
            sb.append("}");
            unniColumns.addMember("value", "value=$L", sb.toString());
            typeBuilder.addAnnotation(unniColumns.build());

            for (Column column : table.getColumns()) {

                Class type = getDataType(column, column.getColumnDataType());
                FieldSpec.Builder fieldBuilder = FieldSpec.builder(type,
                        camelConvert.convert(column.getName()),
                        Modifier.PRIVATE);

                if (type.getCanonicalName().equals(Timestamp.class.getCanonicalName())
                        || type.getCanonicalName().endsWith(Date.class.getCanonicalName())
                        || type.getCanonicalName().endsWith(java.sql.Date.class.getCanonicalName())
                ) {
                    if (!Strings.isBlank(configure.dateFormat())) {
                        fieldBuilder.addAnnotation(AnnotationSpec.builder(JsonFormat.class)
                                .addMember("value", "pattern=$S", configure.dateFormat()).build());
                    }
                }

                fieldBuilder.addJavadoc("$L \r\n缺省值:$L\r\n数据库字段长度:$L($L)",
                        u(column.getRemarks()), column.getDefaultValue(), column.getSize(),
                        column.isNullable() ? "允许为空" : "不允许为空");


                if (column.isPartOfPrimaryKey()) {
                    //数据库字段和POJO字段不一致
                    if (!camelConvert.convert(column.getName()).equals(column.getName())) {
                        fieldBuilder.addAnnotation(AnnotationSpec.builder(org.nutz.dao.entity.annotation.Column.class).addMember("value", "$S", column.getName()).build());
                    }
                } else {
                    // 已经在类的PK注解上声明过了
                }
                typeBuilder.addField(fieldBuilder.build());
            }
        } else {
            //单主键
            for (Column column : table.getColumns()) {

                Class type = getDataType(column, column.getColumnDataType());
                FieldSpec.Builder fieldBuilder = FieldSpec.builder(type,
                        camelConvert.convert(column.getName()),
                        Modifier.PRIVATE);

                if (type.getCanonicalName().equals(Timestamp.class.getCanonicalName())
                        || type.getCanonicalName().endsWith(Date.class.getCanonicalName())
                        || type.getCanonicalName().endsWith(java.sql.Date.class.getCanonicalName())
                ) {
                    if (!Strings.isBlank(configure.dateFormat())) {
                        fieldBuilder.addAnnotation(AnnotationSpec.builder(JsonFormat.class)
                                .addMember("value", "pattern=$S", configure.dateFormat()).build());
                    }
                }

                fieldBuilder.addJavadoc("$L \r\n缺省值:$L\r\n数据库字段长度:$L($L)",
                        u(column.getRemarks()), column.getDefaultValue(), column.getSize(),
                        column.isNullable() ? "允许为空" : "不允许为空");


                if (column.isPartOfPrimaryKey()) {
                    if (isNumber(column)) {
                        //数值型主键
                        if (column.isAutoIncremented()) {
                            fieldBuilder.addAnnotation(AnnotationSpec.builder(Id.class)
                                    .build());
                        } else {
                            fieldBuilder.addAnnotation(AnnotationSpec.builder(Id.class)
                                    .addMember("auto", "$L", "false")
                                    .build());
                        }
                    } else {
                        //字符型主键
                        fieldBuilder.addAnnotation(AnnotationSpec.builder(Name.class).build());
                    }
                    if (!camelConvert.convert(column.getName()).equals(column.getName())) {// 字段名称和数据库列名称不一致，需要添加Column映射
                        fieldBuilder.addAnnotation(AnnotationSpec.builder(org.nutz.dao.entity.annotation.Column.class).addMember("value", "$S", column.getName()).build());
                    }
                } else {
                    //非主键 字段
                    fieldBuilder.addAnnotation(AnnotationSpec.builder(org.nutz.dao.entity.annotation.Column.class).addMember("value", "$S", column.getName()).build());
                }
                typeBuilder.addField(fieldBuilder.build());
            }
        }


        typeBuilder.addSuperinterface(Serializable.class);
        if (configure.getUseGwt()) {
            ClassName cn = ClassName.get("com.google.gwt.user.client.rpc", "IsSerializable");
            typeBuilder.addSuperinterface(cn);
        }

        JavaFile javaFile = JavaFile.builder(configure.entityPackage(), typeBuilder.build()).build();

        try {

            javaFile.writeTo(new File(configure.entityPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断列是否是整数列
     *
     * @param column
     * @return
     */
    private boolean isNumber(Column column) {
        log.info("column " + column.getName() + " is " + column.getColumnDataType().getName());
        String[] numbers = Lang.array("INT", "BIGINT", "NUMBER");
        return Lang.contains(numbers, column.getColumnDataType().getName());
    }

    private boolean inList(List<String> includes, String table) {
        for (String s : includes) {
            if (s.compareToIgnoreCase(table) == 0) {
                return true;
            }
        }
        return false;
    }

    private String u(String s) {
        return s;
    }

    private String getClassTypeName(String name) {
        String temp = camelConvert.convert(name);
        return Strings.upperFirst(temp);
    }


    Class getDataType(Column c, ColumnDataType columnDataType) {

        if (columnDataType.getName().equals("NUMBER")) {

            if (c.getDecimalDigits() <= 0) {
                if (c.getSize() < 10) {
                    return Integer.class;
                }
                return Long.class;
            } else {
                return Double.class;
            }
        }
        String typeName = columnDataType.getName().toUpperCase();
        if (
                typeName.startsWith("TIMESTAMP")
                        || typeName.startsWith("DATETIME")
                        || typeName.startsWith("DATE")

        ) {
            return Date.class;
        }
        return columnDataType.getTypeMappedClass();
    }
}
