/*
 * Copyright 2016 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.autostyle;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * Many Autostyle steps require third-party libraries, but we want to keep
 * Autostyle dependencies minimal.
 */
public interface Provisioner {
  /**
   * Given a set of Maven coordinates, returns a set of jars which include all
   * of the specified coordinates and optionally their transitive dependencies.
   */
  default Set<File> provisionWithTransitives(boolean withTransitives, String... mavenCoordinates) {
    return provisionWithTransitives(withTransitives, Arrays.asList(mavenCoordinates));
  }

  /**
   * Given a set of Maven coordinates, returns a set of jars which include all
   * of the specified coordinates and optionally their transitive dependencies.
   */
  Set<File> provisionWithTransitives(boolean withTransitives, Collection<String> mavenCoordinates);
}
