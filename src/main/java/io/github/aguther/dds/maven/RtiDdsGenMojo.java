/*
 * SPDX-License-Identifier: MIT
 * Copyright 2024 Andreas Guther
 */

package io.github.aguther.dds.maven;

import io.github.aguther.dds.maven.rtiddsgen.BindingFactory;
import io.github.aguther.dds.maven.rtiddsgen.Configuration;
import io.github.aguther.dds.maven.rtiddsgen.DetectVersion;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(
  name = "rtiddsgen",
  defaultPhase = LifecyclePhase.GENERATE_SOURCES,
  requiresProject = true
)
public class RtiDdsGenMojo extends AbstractMojo {

  /**
   * The current Maven project.
   */
  @Parameter(
    property = "project",
    required = true,
    readonly = true
  )
  protected MavenProject project;

  /**
   * Specifies whether sources are added to the {@code compile} or {@code test} scope.
   */
  @Parameter(
    property = "rtiddsgen.generateTestSources",
    defaultValue = "false"
  )
  private boolean generateTestSources;

  /**
   * The directory where the IDL files ({@code *.idl}) are located.
   */
  @Parameter(
    property = "rtiddsgen.sourceDirectory",
    defaultValue = "${basedir}/src/main/idl"
  )
  private File sourceDirectory;

  /**
   * The directory where build status information is located.
   */
  @Parameter(
    property = "rtiddsgen.statusDirectory",
    defaultValue = "${project.build.directory}/maven-status/rtiddsgen"
  )
  private File statusDirectory;

  /**
   * Specify the output directory where the Java files are generated.
   */
  @Parameter(
    property = "rtiddsgen.outputDirectory",
    defaultValue = "${project.build.directory}/generated-sources/idl"
  )
  private File outputDirectory;

  @Parameter(
    property = "rtiddsgen.enableJavaInvocationMode",
    defaultValue = "true"
  )
  private boolean enableJavaInvocationMode;

  @Parameter(
    property = "rtiddsgen.enableServerMode",
    defaultValue = "true"
  )
  private boolean enableServerMode;

  @Parameter(
    property = "rtiddsgen.disablePreprocessor",
    defaultValue = "false"
  )
  private boolean disablePreprocessor;

  @Parameter(
    property = "rtiddsgen.enableUnboundedSupport",
    defaultValue = "false"
  )
  private boolean enableUnboundedSupport;

  @Parameter(
    property = "rtiddsgen.preprocessorPath"
  )
  private File preprocessorPath;

  @Parameter(
    property = "rtiddsgen.preprocessorOptions"
  )
  private String preprocessorOptions;

  @Parameter(
    property = "rtiddsgen.javaPackagePrefix",
    defaultValue = "idl"
  )
  private String javaPackagePrefix;

  void addSourceRoot(File outputDir) {
    if (generateTestSources) {
      project.addTestCompileSourceRoot(outputDir.getPath());
    } else {
      project.addCompileSourceRoot(outputDir.getPath());
    }
  }

  /**
   * The main entry point for this Mojo, it is responsible for converting IDL files into the target language.
   *
   * @throws MojoExecutionException if a configuration or idl error causes the code generation process to fail
   */
  @Override
  public void execute() throws MojoExecutionException {
    // get logger
    var logger = getLog();

    // log version of DDS and rtiddsgen
    if (logger.isInfoEnabled()) {
      logger.info(String.format(
        "Version of DDS = '%s', rtiddsgen = '%s'",
        DetectVersion.getDdsVersion(),
        DetectVersion.getNddsgenVersion()
      ));
    }

    // check if source directory is actually a directory
    if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
      // no need to continue
      if (logger.isInfoEnabled()) {
        logger.info(String.format(
          "Source directory '%s' does not exist or is not a directory",
          sourceDirectory.getAbsolutePath()
        ));
      }
      return;
    }

    // get output directory and create output directory if it does not exist
    if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
      throw new MojoExecutionException(
        String.format("Failed to create output directory '%s'", outputDirectory.getAbsolutePath()));
    }

    // instantiate the RtiDdsGen compiler
    var configuration = getConfiguration();
    var binding = BindingFactory.getBinding(logger, configuration);
    if (binding == null) {
      throw new MojoExecutionException("Failed to get binding");
    }
    binding.configure(configuration);

    // advanced feature (to be done later): test it if precompiler is available and works

    // load old status of idl files
    var lastIdlFilesMap = loadIdlFilesMap(getStatusFile());

    // determine all IDL files that need to be generated
    var currentIdlFilesMap = getIdlFiles(sourceDirectory);

    // get the list of files that need to be processed
    var processingIdlFilesMap = filterIdlFilesMap(lastIdlFilesMap, currentIdlFilesMap);

    // start generation of IDL files in batches
    for (var file : processingIdlFilesMap.entrySet()) {
      var result = binding.execute(file.getKey().toPath());
      if (!result) {
        throw new MojoExecutionException("Failed to generate code");
      }
    }

    // somehow store state of input files to detect if files have changed
    saveIdlFilesMap(getStatusFile(), currentIdlFilesMap);

    // add generated source files to compilation target
    if (project != null) {
      addSourceRoot(outputDirectory);
    }
  }

  Map<File, String> getIdlFiles(
    File sourceDirectory
  ) {
    var result = new HashMap<File, String>();

    if (!sourceDirectory.exists()) {
      return result;
    }

    try {
      Files.walkFileTree(sourceDirectory.toPath(), new FileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          // check if the file ending matches, if not → early exit
          if (file.getFileName().toString().endsWith(".idl")) {
            try {
              result.put(file.toFile(), Utils.checksum(file.toFile()));
            } catch (NoSuchAlgorithmException e) {
              // nothing to do → ignores the file
            }
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (
      IOException e) {
      return Collections.emptyMap();
    }
    return result;
  }

  static Map<File, String> filterIdlFilesMap(
    Map<File, String> last,
    Map<File, String> current
  ) {
    var result = new HashMap<File, String>();
    for (var entry : current.entrySet()) {
      if (!entry.getValue().equals(last.getOrDefault(entry.getKey(), ""))) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  static Map<File, String> loadIdlFilesMap(
    File statusFile
  ) {
    // check if the file exists
    if (statusFile.exists()) {
      // open a file input stream
      try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(statusFile))) {
        // read the object as a distinct step to allow setting suppressed warning
        @SuppressWarnings("unchecked")
        var readObject = (Map<File, String>) in.readObject();
        // return the object
        return readObject;
      } catch (ClassNotFoundException | IOException ignored) {
        // nothing
      }
    }
    return new HashMap<>();
  }

  static void saveIdlFilesMap(
    File statusFile,
    Map<File, String> idlFiles
  ) {
    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(statusFile))) {
      out.writeObject(idlFiles);
    } catch (IOException ignored) {
      // nothing
    }
  }

  File getStatusFile() {
    File statusFile = new File(statusDirectory, "idlFiles.status");
    if (!statusFile.getParentFile().exists()) {
      //noinspection ResultOfMethodCallIgnored
      statusFile.getParentFile().mkdirs();
    }
    return statusFile;
  }

  Configuration getConfiguration() {
    var configuration = new Configuration();
    configuration.setDisablePreprocessor(disablePreprocessor);
    configuration.setEnableJavaInvocationMode(enableJavaInvocationMode);
    configuration.setEnableServerMode(enableServerMode);
    configuration.setEnableUnboundedSupport(enableUnboundedSupport);
    configuration.setJavaPackagePrefix(javaPackagePrefix);
    configuration.setOutputDirectory(outputDirectory);
    configuration.setPreprocessorOptions(preprocessorOptions);
    configuration.setPreprocessorPath(preprocessorPath);
    configuration.setSourceDirectory(sourceDirectory);
    return configuration;
  }
}
