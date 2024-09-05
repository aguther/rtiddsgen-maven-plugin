/*
 * SPDX-License-Identifier: MIT
 * Copyright 2024 Andreas Guther
 */

package io.github.aguther.dds.maven.rtiddsgen;

import java.util.Arrays;
import org.apache.maven.plugin.logging.Log;

public class BindingFactory {

  private BindingFactory() {
  }

  public static Binding getBinding(
    Log logger,
    Configuration configuration
  ) {
    // get versions
    var ddsVersionComponents = Arrays.stream(DetectVersion.getDdsVersion().split("\\."))
      .map(Integer::parseInt)
      .toList();

    try {
      // the minimum required version is 6.1.2
      if (ddsVersionComponents.get(0) < 6) {
        logger.error("RTI DDS Versions 5.x are not supported.");
        return null;
      }
      if (ddsVersionComponents.get(0) == 6 && ddsVersionComponents.get(1) < 1) {
        logger.error("Non-LTS RTI DDS Versions 6.0.x are not supported.");
        return null;
      }
      if (ddsVersionComponents.get(0) == 6 && ddsVersionComponents.get(1) == 1 && ddsVersionComponents.get(2) < 2) {
        logger.error("Non-LTS RTI DDS Versions 6.1.0 or 6.1.1 are not supported.");
        return null;
      }

      // the default case
      if (configuration.isEnableJavaInvocationMode()) {
        return new io.github.aguther.dds.maven.rtiddsgen.version612.JavaBinder(logger);
      } else {
        return new io.github.aguther.dds.maven.rtiddsgen.version612.ExecutableBinder(logger);
      }
    } catch (InstantiationException e) {
      return null;
    }
  }

}
