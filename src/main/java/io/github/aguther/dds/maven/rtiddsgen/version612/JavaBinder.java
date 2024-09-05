/*
 * SPDX-License-Identifier: MIT
 * Copyright 2024 Andreas Guther
 */

package io.github.aguther.dds.maven.rtiddsgen.version612;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.aguther.dds.maven.rtiddsgen.Binding;
import io.github.aguther.dds.maven.rtiddsgen.Configuration;
import io.github.aguther.dds.maven.rtiddsgen.Constants;
import io.github.aguther.dds.maven.rtiddsgen.DetectVersion;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.maven.plugin.logging.Log;

public class JavaBinder implements Binding {

  private final Log logger;

  private Method generateCodeMethod;
  private Object rtiDdsGenMainClassInstance;
  private List<String> argumentsFromConfiguration;

  public JavaBinder(
    Log logger
  ) throws InstantiationException {
    // check argument
    checkNotNull(logger);

    // store logger
    this.logger = logger;

    try {
      // check if environment variable is valid
      if (!System.getenv().containsKey(Constants.ENVIRONMENT_NDDS_HOME)) {
        logger.error(Constants.ENVIRONMENT_NDDS_HOME + " not set");
        throw new InstantiationException(Constants.ENVIRONMENT_NDDS_HOME + " not set");
      }

      // determine path of rtiddsgen jar
      var rtiDdsGenJarPath = Path.of(
        System.getenv(Constants.ENVIRONMENT_NDDS_HOME),
        "resource/app/lib/java/rtiddsgen2.jar"
      );
      var rtiDdsGenJarFile = rtiDdsGenJarPath.toFile();
      if (!rtiDdsGenJarFile.exists()) {
        logger.error(String.format("JAR file of rtiddsgen not found (%s)", rtiDdsGenJarPath.toString()));
        throw new InstantiationException("JAR file of rtiddsgen not found");
      }
      if (logger.isDebugEnabled()) {
        logger.debug(String.format("JAR file of rtiddsgen = '%s'", rtiDdsGenJarPath.toString()));
      }

      // get url class loader
      var urlClassLoader = new URLClassLoader(
        new URL[]{rtiDdsGenJarFile.toURI().toURL()},
        ClassLoader.getSystemClassLoader()
      );

      // load main class
      var rtiDdsGenMainClass = Class.forName("com.rti.ndds.nddsgen.Main", true, urlClassLoader);

      // get the main method
      generateCodeMethod = rtiDdsGenMainClass.getDeclaredMethod("generateCode", String[].class);

      // instantiate instance
      rtiDdsGenMainClassInstance = rtiDdsGenMainClass.getDeclaredConstructor().newInstance();

      // set ndds resource directory
      var nddsResourceDir = Path.of(
        System.getenv(Constants.ENVIRONMENT_NDDS_HOME),
        "resource/app/app_support/rtiddsgen"
      );
      System.setProperty("NDDS_RESOURCE_DIR", nddsResourceDir.toString());
      System.setProperty("NDDS_VERSION", DetectVersion.getDdsVersion());
      if (logger.isDebugEnabled()) {
        logger.debug(String.format("NDDS_RESOURCE_DIR = '%s'", nddsResourceDir.toString()));
        logger.debug(String.format("NDDS_VERSION = '%s'", DetectVersion.getDdsVersion()));
      }

      // get LoggerCreator class
      var rtiLoggerCreatorClass = Class.forName("com.rti.ndds.nddsgen.LoggerCreator", true, urlClassLoader);
      var rtiLoggerCreatorInstance = rtiLoggerCreatorClass.getDeclaredConstructor().newInstance();
      // the called method name has a typo that is the reason for the wrong typing here
      var getLoggerMethod = rtiLoggerCreatorInstance.getClass().getDeclaredMethod("getLooger");
      var rtiLoggerInstance = getLoggerMethod.invoke(rtiLoggerCreatorInstance);

      // set logger
      var fieldLogger = rtiDdsGenMainClass.getDeclaredField("logger");
      if (fieldLogger.trySetAccessible()) {
        fieldLogger.set(rtiDdsGenMainClassInstance, rtiLoggerInstance);
      }

      // create arguments object
      var fieldArguments = rtiDdsGenMainClass.getDeclaredField("arguments");
      if (fieldArguments.trySetAccessible()) {
        fieldArguments.set(rtiDdsGenMainClassInstance, new HashMap<String, Object>());
      }

      // make method accessible
      generateCodeMethod.setAccessible(true);
    } catch (
      NoSuchFieldException |
      NoSuchMethodException |
      ClassNotFoundException |
      MalformedURLException |
      InvocationTargetException |
      IllegalAccessException e
    ) {
      // nothing to do
    }
  }

  @Override
  public void configure(
    Configuration configuration
  ) {
    // check argument
    checkNotNull(configuration);

    // get default arguments based on configuration
    argumentsFromConfiguration = Arguments.getArgumentsFromConfiguration(configuration);
  }

  @Override
  @SuppressWarnings("squid:S3878")
  public boolean execute(
    Path file
  ) {
    // check argument
    checkNotNull(file);

    // create the list of arguments
    var list = new ArrayList<String>(argumentsFromConfiguration);
    list.add(file.toString());

    // print arguments for call
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("rtiddsgen arguments = '%s'", list.toString()));
    }

    try {
      // it seems there is no other way to correctly pass the argument → ignore S3878
      return (boolean) generateCodeMethod.invoke(
        rtiDdsGenMainClassInstance,
        new Object[]{list.toArray(new String[0])}
      );
    } catch (IllegalAccessException | InvocationTargetException ignored) {
      // nothing to do → failed
    }
    // failed
    return false;
  }

}
