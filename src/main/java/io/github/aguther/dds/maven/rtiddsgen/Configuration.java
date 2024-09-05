/*
 * SPDX-License-Identifier: MIT
 * Copyright 2024 Andreas Guther
 */

package io.github.aguther.dds.maven.rtiddsgen;

import java.io.File;

public class Configuration {

  private File outputDirectory;
  private File preprocessorPath;
  private File sourceDirectory;
  private String javaPackagePrefix;
  private String preprocessorOptions;
  private boolean disablePreprocessor;
  private boolean enableJavaInvocationMode;
  private boolean enableServerMode;
  private boolean enableUnboundedSupport;

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public File getPreprocessorPath() {
    return preprocessorPath;
  }

  public void setPreprocessorPath(File preprocessorPath) {
    this.preprocessorPath = preprocessorPath;
  }

  public File getSourceDirectory() {
    return sourceDirectory;
  }

  public void setSourceDirectory(File sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  public String getJavaPackagePrefix() {
    return javaPackagePrefix;
  }

  public void setJavaPackagePrefix(String javaPackagePrefix) {
    this.javaPackagePrefix = javaPackagePrefix;
  }

  public String getPreprocessorOptions() {
    return preprocessorOptions;
  }

  public void setPreprocessorOptions(String preprocessorOptions) {
    this.preprocessorOptions = preprocessorOptions;
  }

  public boolean isDisablePreprocessor() {
    return disablePreprocessor;
  }

  public void setDisablePreprocessor(boolean disablePreprocessor) {
    this.disablePreprocessor = disablePreprocessor;
  }

  public boolean isEnableJavaInvocationMode() {
    return enableJavaInvocationMode;
  }

  public void setEnableJavaInvocationMode(boolean enableJavaInvocationMode) {
    this.enableJavaInvocationMode = enableJavaInvocationMode;
  }

  public boolean isEnableServerMode() {
    return enableServerMode;
  }

  public void setEnableServerMode(boolean enableServerMode) {
    this.enableServerMode = enableServerMode;
  }

  public boolean isEnableUnboundedSupport() {
    return enableUnboundedSupport;
  }

  public void setEnableUnboundedSupport(boolean enableUnboundedSupport) {
    this.enableUnboundedSupport = enableUnboundedSupport;
  }
}
