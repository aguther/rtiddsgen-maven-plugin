# Maven Plugin for rtiddsgen

This is a repository that contains a maven plugin to use rtiddsgen during build.

## Usage

### Prerequisites

- Environment variable `NDDSHOME` pointing to directory with RTI Connext SDK
- If not disabled a proper development environment with a preprocessor
- RTI Connext 6.1.2 or newer

### Configuration in `pom.xml`

The minimum configuration needed to use the plugin is shown below:

```
  <build>
    <plugins>
      <plugin>
        <groupId>io.github.aguther.dds.maven</groupId>
        <artifactId>rtiddsgen-maven-plugin</artifactId>
        <version>0.1.0</version>
        <executions>
          <execution>
            <id>rtiddsgen</id>
            <goals>
              <goal>rtiddsgen</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

## Features

- The plugin remembers the checksum of the input IDL files and only calls rtiddsgen for changed or new files.
- Support to call `rtiddsgen` in non-server, server and a JVM invocation mode (default and fastest)
- The following options can be used:
  - Unbounded sequences
  - Disable the preprocessor
  - Change preprocessor options
  - Configure the preprocessor executable path
  - Configure the java package prefix for the generated files
  - Change the default output directory
  - Change the default source directory

## Limitations

- When only the configuration of the plugin has been changed, `mvn clean` should be called to ensure the files are
  generated with the new configuration.
  The plugin does not automatically detect changes in the configuration right now
- Only the most commonly used features of `rtiddsgen` are supported right now
- Tests have been done with the current LTS releases RTI Connext 6.1.2 and RTI Connext 7.3.0
- Documentation of the plugin is very little up to now
