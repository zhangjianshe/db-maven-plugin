package cn.mapway.tools.db;

import cn.mapway.tools.db.naming.CamelConvert;
import cn.mapway.tools.db.naming.INameConvertor;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.squareup.javapoet.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
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
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * DB2 CODE
 *
 * @author zhangjianshe
 */
public class DB2Code {

    private static Logger logger = Logger.getLogger(DB2Code.class.getName());

    /**
     * configuration
     */
    IConfigure configure;
    INameConvertor camelConvert = new CamelConvert();

    /**
     * constuction function
     *
     * @param configuration config
     */
    public DB2Code(IConfigure configuration) {
        this.configure = configuration;
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

        logger.info("============EXPORT DB DAO CODE=============");

        if (Strings.isBlank(configure.getSchema())) {
            logger.warning("没有定义数据库schema,本次操作不会生成任何DAO代码");
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
                exportTableEntity(table, configure);
                exportTableDao(table, configure);
            }
        }

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

    /**
     * 输出DAO类
     *
     * @param table
     * @param configure
     */
    private void exportTableDao(Table table, IConfigure configure) {
        ClassName cn = ClassName.get(configure.entityPackage(), getClassTypeName(table.getName()) + "Entity");
        ParameterizedTypeName t = ParameterizedTypeName.get(ClassName.get(BaseMapper.class), cn);
        TypeSpec.Builder typeBuilder = TypeSpec.interfaceBuilder(getClassTypeName(table.getName()) + "Dao")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(t);

        typeBuilder.addJavadoc("<b>$L.$L</b>\r\n$L\r\n$L\r\n@author $L",
                table.getSchema().getCatalogName(), table.getName(),
                u(table.getRemarks()), table.getDefinition(), configure.author());

        JavaFile javaFile = JavaFile.builder(configure.daoPackage(), typeBuilder.build()).build();
        try {
            javaFile.writeTo(new File(configure.daoPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出table实体类
     *
     * @param table
     * @param configure
     */
    private void exportTableEntity(Table table, IConfigure configure) {


        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(getClassTypeName(table.getName()) + "Entity")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(
                        AnnotationSpec.builder(TableName.class).addMember("value", "value=$S", table.getName()).build());
        typeBuilder.addJavadoc("<b>$L.$L</b>\r\n$L\r\n$L\r\n@author $L", table.getSchema().getCatalogName(), table.getName(), u(table.getRemarks()), table.getDefinition(), configure.author());

        if (configure.lombok()) {
            typeBuilder.addAnnotation(AnnotationSpec.builder(Data.class).build());
            typeBuilder.addAnnotation(AnnotationSpec.builder(Builder.class).build());
            typeBuilder.addAnnotation(AnnotationSpec.builder(NoArgsConstructor.class).build());
            typeBuilder.addAnnotation(AnnotationSpec.builder(AllArgsConstructor.class).build());
            typeBuilder.addAnnotation(AnnotationSpec.builder(Accessors.class).addMember("chain", "true").build());
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
                if (column.isAutoIncremented()) {
                    fieldBuilder.addAnnotation(AnnotationSpec.builder(TableId.class)
                            .addMember("value", "$S", column.getName())
                            .addMember("type", "$T.$L", IdType.class, IdType.AUTO.name())
                            .build());
                } else {
                    fieldBuilder.addAnnotation(AnnotationSpec.builder(TableId.class).addMember("value", "$S", column.getName()).build());
                }
            } else {
                fieldBuilder.addAnnotation(AnnotationSpec.builder(TableField.class).addMember("value", "$S", column.getName()).build());
            }
            typeBuilder.addField(fieldBuilder.build());
        }
        typeBuilder.addSuperinterface(Serializable.class);

        JavaFile javaFile = JavaFile.builder(configure.entityPackage(), typeBuilder.build()).build();
        try {

            javaFile.writeTo(new File(configure.entityPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Class getDataType(Column c, ColumnDataType columnDataType) {

        if (columnDataType.getName().equals("NUMBER")) {

            if (c.getDecimalDigits() == 0) {
                if (c.getSize() < 10) {
                    return Integer.class;
                }
                return Long.class;
            } else {
                return Double.class;
            }
        }
        return columnDataType.getTypeMappedClass();
    }
}
