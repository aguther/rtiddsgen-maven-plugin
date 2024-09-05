/*
 * SPDX-License-Identifier: MIT
 * Copyright 2024 Andreas Guther
 */

package io.github.aguther.dds.maven;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Utils {

  private Utils() {
  }

  /**
   * Creates the SHA256 checksum for the given file.
   *
   * @param file the file.
   * @return the checksum.
   */
  public static String checksum(File file) throws IOException, NoSuchAlgorithmException {
    byte[] data = Files.readAllBytes(file.toPath());
    byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
    return new BigInteger(1, hash).toString(16);
  }
}
