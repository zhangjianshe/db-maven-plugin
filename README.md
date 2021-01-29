# how to compile it

mvn clean package deploy -Pziroom

#how to use it

pom.xml文件中加入 profile

```xml
 <profile>
    <id>code</id>
    <plugin>
        <groupId>cn.mapway</groupId>
        <artifactId>db-maven-plugin</artifactId>
        <version>1.0.0</version>
        <executions>
            <execution>
                <phase>generate-sources</phase>
                <goals>
                    <goal>mybatis</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <jdbcUrl>
                jdbc:mysql://ip_or_domain_name:3306/database_name?useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false
            </jdbcUrl>
            <driver>com.mysql.jdbc.Driver</driver>
            <user>user_name</user>
            <pwd>password</pwd>
            <schema>hddp_common</schema>
            <author>ZhangJianshe</author>
            <lombok>true</lombok>
            <dateFormat>yyyy-MM-dd HH:mm:ss</dateFormat>
            <entityPackage>com.ziroom.hddp.common.dao.entity</entityPackage>
            <daoPackage>com.ziroom.hddp.common.dao.entity</daoPackage>
            <includes>table_name1;table_name2</includes>
            <excludes>table_name1;table_name2</excludes>

        </configuration>
    </plugin>
</profile>
```

然后执行 ```mvn clean package -Pcode``` 就可以生成相应的数据库访问代码，这个代码是基于MyBatisPlus,请引入相应的包


