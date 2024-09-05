/*
 * SPDX-License-Identifier: MIT
 * Copyright 2024 Andreas Guther
 */

package io.github.aguther.dds.maven.rtiddsgen.version612;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.aguther.dds.maven.rtiddsgen.Configuration;
import java.util.ArrayList;
import java.util.List;

class Arguments {

  private Arguments() {
  }

  static List<String> getArgumentsFromConfiguration(
    Configuration configuration
  ) {
    checkNotNull(configuration);

    var result = new ArrayList<String>();

    // rtiddsgen should always update files
    result.add("-update");
    result.add("typefiles");

    // add include directory
    result.add("-I");
    result.add(configuration.getSourceDirectory().getAbsolutePath());

    // add language for output
    result.add("-language");
    result.add("Java");

    // add output directory
    result.add("-d");
    result.add(configuration.getOutputDirectory().getAbsolutePath());

    // enable unbounded sequence support if requested
    if (configuration.isEnableUnboundedSupport()) {
      result.add("-unboundedSupport");
    }

    // check if a prefix for the package should be added
    if (configuration.getJavaPackagePrefix() != null && !configuration.getJavaPackagePrefix().isEmpty()) {
      result.add("-package");
      result.add(configuration.getJavaPackagePrefix());
    }

    return result;
  }

}
