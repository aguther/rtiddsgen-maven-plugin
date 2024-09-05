/*
 * SPDX-License-Identifier: MIT
 * Copyright 2024 Andreas Guther
 */

package io.github.aguther.dds.maven.rtiddsgen.version612;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.aguther.dds.maven.rtiddsgen.Binding;
import io.github.aguther.dds.maven.rtiddsgen.Configuration;
import io.github.aguther.dds.maven.rtiddsgen.Constants;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.logging.Log;

public class ExecutableBinder implements Binding {

  private final Log logger;

  private Path executablePath;
  private List<String> argumentsFromConfiguration;

  public ExecutableBinder(
    Log logger
  ) {
    this.logger = logger;
  }

  @Override
  public void configure(
    Configuration configuration
  ) {
    // check arguments
    checkNotNull(configuration);

    // get default arguments based on configuration
    argumentsFromConfiguration = Arguments.getArgumentsFromConfiguration(configuration);

    // determine executable path
    var executablePrefix = "bin/";
    var executableName = configuration.isEnableServerMode() ? "rtiddsgen" : "rtiddsgen_server";
    var executableExtension = System.getProperty("os.name").contains("Windows") ? ".bat" : "";
    executablePath = Path.of(
      System.getenv(Constants.ENVIRONMENT_NDDS_HOME),
      String.join("", executablePrefix, executableName, executableExtension)
    );
  }

  @Override
  public boolean execute(
    Path file
  ) {
    // check arguments
    checkNotNull(file);

    try {
      // get process builder
      var processBuilder = getProcessBuilder(executablePath, argumentsFromConfiguration, file);

      // start the process
      var process = processBuilder.start();

      // wait for the process to finish
      process.waitFor();

      // check exit code
      if (process.exitValue() == 0) {
        // success
        return true;
      }
    } catch (IOException e) {
      logger.debug(e);
    } catch (InterruptedException e) {
      logger.debug(e);
      Thread.currentThread().interrupt();
    }

    // failed
    return false;
  }

  private static ProcessBuilder getProcessBuilder(
    Path executablePath,
    List<String> argumentsFromConfiguration,
    Path file
  ) {
    // build arguments
    var arguments = new ArrayList<String>();
    arguments.add(executablePath.toString());
    arguments.addAll(argumentsFromConfiguration);
    arguments.add(file.toString());

    // create a process object
    return new ProcessBuilder(arguments).inheritIO();
  }

}
