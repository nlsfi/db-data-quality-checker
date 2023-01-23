# dbquality-service

Java library for executing PostGIS queries.

## Usage

Following requirements must be met to use this library:
- PostGIS database
- YAML file describing SQL queries
    - tables related to the queries have UUID primary key

### Minimal working example

- prerequisities    
    - `quality-rules.yml` file is found
    - PostGIS database `example` in localhost in port 5442
    - test data created with `create-example-table.sql` script in `public` - schema
    - user `postgres\postgres` granted to select from table `example_table`

```java
package fi.nls.qualitydemo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;

import fi.nls.quality.QualityService;
import fi.nls.quality.model.QualityRunResult;

/**
 * Quality service demo.
 *
 */
public class Demo {
    public static void main(String[] args) {
        new Demo().run();
    }

    public void run() {
        var dataSource = createDatasource();
        var qualityService = new QualityService("quality-rules.yml", "mttj_id");
        Map<String, List<UUID>> featuresByCategory = new HashMap<>();

        // execute rules with id criteria
        featuresByCategory.put("some_category", Arrays.asList(UUID.fromString("01d24c09-de46-4e93-93fa-5037e64edd34")));
        var runResult = qualityService.executeRules(dataSource, featuresByCategory);
        print(runResult);

        // execute rules without id criteria
        featuresByCategory.clear();
        featuresByCategory.put("some_category", null);
        runResult = qualityService.executeRules(dataSource, featuresByCategory);
        print(runResult);
    }

    public DataSource createDatasource() {
        var ds = DataSourceBuilder.create().username("postgres").password("postgres").url("jdbc:postgresql://localhost:5442/example").build();
        return ds;
    }

    public void print(QualityRunResult result) {
        result.getQualityResults().forEach(r -> {
            System.out.println(r.getCategory());
            System.out.println(r.getRuleId());
        });
    }
}
```

- demo project can be compiled using following `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>fi.nls</groupId>
    <artifactId>quality-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>quality-demo</name>
    <url>http://maven.apache.org</url>

    <properties>
        <qualityservice.version>0.1.3-SNAPSHOT</qualityservice.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven-compiler-plugin.version>3.10.1</maven-compiler-plugin.version>
        <project.artifactId>project.artifactId</project.artifactId>
        <java.version>11</java.version>
        <postgis-jdbc.version>2021.1.0</postgis-jdbc.version>
        <geolatte-geom.version>1.8.2</geolatte-geom.version>
        <snakeyaml.version>1.33</snakeyaml.version>
        <spring-jdbc.version>5.2.22.RELEASE</spring-jdbc.version>
    </properties>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.4</version>
        <relativePath /> <!-- lookup parent from repository -->
    </parent>
    <dependencies>
        <dependency>
            <groupId>fi.nls</groupId>
            <artifactId>qualityservice</artifactId>
            <version>${qualityservice.version}</version>
        </dependency>
        <dependency>
            <groupId>org.geolatte</groupId>
            <artifactId>geolatte-geom</artifactId>
            <version>${geolatte-geom.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.hibernate</groupId>
                    <artifactId>hibernate-core</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>net.postgis</groupId>
            <artifactId>postgis-jdbc</artifactId>
            <version>${postgis-jdbc.version}</version>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <!-- spring boot 2.7.1 has 42.3.6, which still has this issue 
                with param count https://github.com/pgjdbc/pgjdbc/issues/1311, override with
                fix for now -->
            <version>42.4.0</version>
        </dependency>
        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>${snakeyaml.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>${spring-jdbc.version}</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>3.8.1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <configuration>
                        <mainClass>fi.nls.qualitydemo.App</mainClass>
                        <layout>JAR</layout>
                    </configuration>
                    <executions>
                        <execution>
                            <goals>
                                <goal>repackage</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
                <plugin>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>${maven-compiler-plugin.version}</version>
                    <configuration>
                        <source>${java.version}</source>
                        <target>${java.version}</target>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```