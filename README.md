<img src="http://img.jcabi.com/logo-square.svg" width="64px" height="64px" />

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-maven-plugin)](http://www.rultor.com/p/jcabi/jcabi-maven-plugin)

[![Build Status](https://travis-ci.org/jcabi/jcabi-maven-plugin.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-maven-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-maven-plugin)

More details are here: [plugin.jcabi.com](http://plugin.jcabi.com/index.html)

## Questions?

If you have any questions about the framework, or something doesn't work as expected,
please [submit an issue here](https://github.com/jcabi/jcabi-maven-plugin/issues/new).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```

## Java 8 Support

There are new two options which you can choose in the pom.xml to setup custom jdk source and target version.
For example, if you need to add support for java 8 you have to add this lines in you pom:
```
<configuration>
	<source>1.8</source>
  <target>1.8</target>
</configuration>
```
You have also to indicate a compatible version of aspectj (now the latest compatible is 1.8.3), to do this add these lines
```
<dependency>
	<groupId>org.aspectj</groupId>
	<artifactId>aspectjrt</artifactId>
	<version>1.8.3</version>
</dependency>
<dependency>
	<groupId>org.aspectj</groupId>
	<artifactId>aspectjtools</artifactId>
  <version>1.8.3</version>
</dependency>
```
At the end you must have these lines in the build section related to jcabi-maven-plugin:
```
<plugin>
	<groupId>com.jcabi</groupId>
	<artifactId>jcabi-maven-plugin</artifactId>
	<version>0.10</version>
	<configuration>
  	<source>1.8</source>
  	<target>1.8</target>
	</configuration>
	<executions>
  	<execution>
	      <goals>
	  			<goal>ajc</goal>
      	</goals>
	  </execution>
	</executions>
  <dependencies>
  	<dependency>
	    <groupId>org.aspectj</groupId>
	    <artifactId>aspectjrt</artifactId>
	    <version>1.8.3</version>
	  </dependency>
	  <dependency>
    	<groupId>org.aspectj</groupId>
    	<artifactId>aspectjtools</artifactId>
    	<version>1.8.3</version>
	  </dependency>
	</dependencies>
</plugin>
```
