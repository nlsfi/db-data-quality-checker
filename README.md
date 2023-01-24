# db-data-quality-checker

Java library for finding executing queries in SQL database.

## Usage

Library can be used to find rows in database which match the given rules. Rule can be for example that geometry must be valid or an integer attribute must have values in given interval.

### Minimal working example

- prerequisites:
    - [mvn](https://maven.apache.org/download.cgi) executable in PATH 
    - `example-quality-rules.yml` file is located in project root directory
    - PostGIS database `example` in localhost in port 5442. Can be changed in `createDatasource` method.
    - test data created with `create-example-table.sql` script in `public` - schema
    - user `postgres\postgres` granted to select from table `example_table`

- build: `mvn clean package spring-boot:repackage`
- run: `java -jar target\quality-demo.jar`

```java
package fi.nls.qualitydemo;

import java.util.*;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;

import fi.nls.quality.QualityService;
import fi.nls.quality.model.QualityRunResult;

/**
 * Quality service demo with PostGIS database.
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

- demo project can be compiled with Maven using following `pom.xml`

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

## Development of quality-result-gui

See [development readme](./DEVELOPMENT.md).

## License & copyright

Licensed under MIT.

This tool is part of the topographic data production system developed in National Land Survey of Finland. For further information, see:

- [Abstract for FOSS4G](https://talks.osgeo.org/foss4g-2022/talk/TDDGJ9/)
- [General news article about the project](https://www.maanmittauslaitos.fi/en/topical_issues/topographic-data-production-system-upgraded-using-open-source-solutions)

Contact details: eero.hietanen@maanmittauslaitos.fi

Copyright (C) 2022 [National Land Survey of Finland].

[National Land Survey of Finland]: https://www.maanmittauslaitos.fi/en
[qgis-plugin-dev-tools]: https://github.com/nlsfi/qgis-plugin-dev-tools