/*
 * SPDX-License-Identifier: MIT
 * Copyright 2024 Andreas Guther
 */

package io.github.aguther.dds.maven.rtiddsgen;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class DetectVersion {

  private static URLClassLoader urlClassLoader;
  private static Class<?> versionsClass;

  private static String ddsVersion;
  private static String nddsgenVersion;

  private DetectVersion() {
  }

  private static void ensureInitializedClassLoaderAndClass() throws MalformedURLException, ClassNotFoundException {
    if (urlClassLoader == null) {
      // determine path of rtiddsgen jar
      var rtiDdsGenJarPath = Path.of(
        System.getenv(Constants.ENVIRONMENT_NDDS_HOME),
        "resource/app/lib/java/rtiddsgen2.jar"
      );

      // get the file from path
      File rtiDdsGenJar = new File(rtiDdsGenJarPath.toUri());

      // get url class loader
      urlClassLoader = new URLClassLoader(
        new URL[]{rtiDdsGenJar.toURI().toURL()},
        ClassLoader.getSystemClassLoader()
      );
    }

    if (versionsClass == null) {
      // load main class
      versionsClass = Class.forName("com.rti.ndds.nddsgen.Versions", true, urlClassLoader);
    }
  }

  public static String getDdsVersion() {
    if (ddsVersion == null) {
      try {
        // ensure base types are initialized
        ensureInitializedClassLoaderAndClass();

        // get the main method
        var getMethod = versionsClass.getDeclaredMethod("getNddsMainVersion");

        // call
        if (getMethod.trySetAccessible()) {
          ddsVersion = (String) getMethod.invoke(null);
        }

      } catch (MalformedURLException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
               IllegalAccessException e) {
        // nothing
      }
    }
    return ddsVersion;
  }

  public static String getNddsgenVersion() {
    if (nddsgenVersion == null) {
      try {
        // ensure base types are initialized
        ensureInitializedClassLoaderAndClass();

        // get the main method
        var getMethod = versionsClass.getDeclaredMethod("getNddsgenVersion");

        // call
        if (getMethod.trySetAccessible()) {
          nddsgenVersion = (String) getMethod.invoke(null);
        }

      } catch (MalformedURLException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
               IllegalAccessException e) {
        // nothing
      }
    }
    return nddsgenVersion;
  }
}
