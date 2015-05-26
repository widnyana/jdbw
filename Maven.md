# Using Maven #

If you want to use the jdbw library through [Apache Maven](http://maven.apache.org), it's very easy to do. Just put this dependency in your `pom.xml`:
```
    <dependencies>
    ...
        <dependency>
            <groupId>com.googlecode.jdbw</groupId>
            <artifactId>jdbw</artifactId>
            <version>1.0.0</version>
        </dependency>
    ...
    </dependencies>
```

Adjust the version number as required (I'm probably not going to remember to update this page every time I make a release). Since the Sonatype OSS repository is synchronized with Maven Central, you don't need to add any extra repository definitions to your project or your maven settings.