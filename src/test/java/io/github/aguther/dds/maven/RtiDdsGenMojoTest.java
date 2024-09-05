/*
 * SPDX-License-Identifier: MIT
 * Copyright 2024 Andreas Guther
 */

package io.github.aguther.dds.maven;

import io.takari.maven.testing.TestMavenRuntime5;
import io.takari.maven.testing.TestResources5;
import java.nio.file.Path;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class RtiDdsGenMojoTest {

  @RegisterExtension
  final TestResources5 resources = new TestResources5();

  @RegisterExtension
  final TestMavenRuntime5 maven = new TestMavenRuntime5();

  @Disabled
  @Test
  void simpleCase() throws Exception {
    // get project base directory
    Path baseDir = resources.getBasedir("simple").toPath();

    // get directory of IDL files
    Path idlDir = baseDir.resolve("src/main/idl");

    // get directory of generated files
    Path generatedSources = baseDir.resolve("target/generated-sources/idl");

    // get project resources and context
    MavenProject project = maven.readMavenProject(baseDir.toFile());
    MavenSession session = maven.newMavenSession(project);
    MojoExecution exec = maven.newMojoExecution("rtiddsgen");

    // -------------------------------------------------------------------------------------------------------------
    // first execution to see if files are generated
    // -------------------------------------------------------------------------------------------------------------

    // ensure generated files are not existing

    // execute plugin
    maven.executeMojo(session, project, exec);
    maven.executeMojo(session, project, exec);

    // ensure expected generated files have been created

    // -------------------------------------------------------------------------------------------------------------
    // second execution with no changes → nothing needs to be processed
    // -------------------------------------------------------------------------------------------------------------

    // store checksum of generated files, maybe also store the timestamp of the files?

    // execute plugin
//        maven.executeMojo(session, project, exec);

    // ensure checksum is still the same and maybe check also the timestamp of generated files to ensure they have not been touched?

    // -------------------------------------------------------------------------------------------------------------
    // third execution with a changed IDL file → update needs to be processed
    // -------------------------------------------------------------------------------------------------------------

    // modify the grammar to make checksum comparison detect a change
//        try (Change change = Change.of(baseGrammar, "DOT: '.' ;")) {
//
//            // execute plugin
//            maven.executeMojo(session, project, exec);
//
//            // test that files are actually changed
//        }

    // restore file and confirm it was restored

    // execute plugin
//        maven.executeMojo(session, project, exec);

    // ensure original checksums are again valid
  }
}
