/*
 * SPDX-License-Identifier: MIT
 * Copyright 2024 Andreas Guther
 */

package io.github.aguther.dds.maven.rtiddsgen;

import java.nio.file.Path;

public interface Binding {

  void configure(
    Configuration configuration
  );

  boolean execute(
    Path file
  );
}
