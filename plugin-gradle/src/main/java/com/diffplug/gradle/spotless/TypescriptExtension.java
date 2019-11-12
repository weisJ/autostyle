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
package com.diffplug.gradle.spotless;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.annotation.Nullable;

import org.gradle.api.Project;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.npm.PrettierFormatterStep;
import com.diffplug.spotless.npm.TsConfigFileType;
import com.diffplug.spotless.npm.TsFmtFormatterStep;
import com.diffplug.spotless.npm.TypedTsFmtConfigFile;

public class TypescriptExtension extends FormatExtension {

  static final String NAME = "typescript";

  public TypescriptExtension(SpotlessExtension root) {
    super(root);
  }

  /** Uses the default version of typescript-format. */
  public TypescriptFormatExtension tsfmt() {
    return tsfmt(TsFmtFormatterStep.defaultDevDependencies());
  }

  /** Uses the specified version of typescript-format. */
  public TypescriptFormatExtension tsfmt(String version) {
    return tsfmt(TsFmtFormatterStep.defaultDevDependenciesWithTsFmt(version));
  }

  /** Creates a {@code TypescriptFormatExtension} using exactly the specified npm packages. */
  public TypescriptFormatExtension tsfmt(Map<String, String> devDependencies) {
    TypescriptFormatExtension tsfmt = new TypescriptFormatExtension(devDependencies);
    addStep(tsfmt.createStep());
    return tsfmt;
  }

  public class TypescriptFormatExtension extends NpmStepConfig<TypescriptFormatExtension> {

    private Map<String, Object> config = Collections.emptyMap();

    @Nullable
    TsConfigFileType configFileType = null;

    @Nullable
    Object configFilePath = null;

    private final Map<String, String> devDependencies;

    TypescriptFormatExtension(Map<String, String> devDependencies) {
      this.devDependencies = Objects.requireNonNull(devDependencies);
    }

    public void config(final Map<String, Object> config) {
      this.config = new TreeMap<>(requireNonNull(config));
      replaceStep(createStep());
    }

    public void tsconfigFile(final Object path) {
      configFile(TsConfigFileType.TSCONFIG, path);
    }

    public void tslintFile(final Object path) {
      configFile(TsConfigFileType.TSLINT, path);
    }

    public void vscodeFile(final Object path) {
      configFile(TsConfigFileType.VSCODE, path);
    }

    public void tsfmtFile(final Object path) {
      configFile(TsConfigFileType.TSFMT, path);
    }

    private void configFile(TsConfigFileType filetype, Object path) {
      this.configFileType = requireNonNull(filetype);
      this.configFilePath = requireNonNull(path);
      replaceStep(createStep());
    }

    public FormatterStep createStep() {
      final Project project = getProject();

      return TsFmtFormatterStep.create(
          devDependencies,
          GradleProvisioner.fromProject(project),
          project.getBuildDir(),
          npmFileOrNull(),
          typedConfigFile(),
          config);
    }

    private TypedTsFmtConfigFile typedConfigFile() {
      if (this.configFileType != null && this.configFilePath != null) {
        return new TypedTsFmtConfigFile(this.configFileType, getProject().file(this.configFilePath));
      }
      return null;
    }
  }

  /** Uses the default version of prettier. */
  @Override
  public PrettierConfig prettier() {
    return prettier(PrettierFormatterStep.defaultDevDependencies());
  }

  /** Uses the specified version of prettier. */
  @Override
  public PrettierConfig prettier(String version) {
    return prettier(PrettierFormatterStep.defaultDevDependenciesWithPrettier(version));
  }

  /** Uses exactly the npm packages specified in the map. */
  @Override
  public PrettierConfig prettier(Map<String, String> devDependencies) {
    PrettierConfig prettierConfig = new TypescriptPrettierConfig(devDependencies);
    addStep(prettierConfig.createStep());
    return prettierConfig;
  }

  /**
   * Overrides the parser to be set to typescript, no matter what the user's config says.
   */
  public class TypescriptPrettierConfig extends PrettierConfig {
    TypescriptPrettierConfig(Map<String, String> devDependencies) {
      super(devDependencies);
    }

    @Override
    FormatterStep createStep() {
      fixParserToTypescript();
      return super.createStep();
    }

    private void fixParserToTypescript() {
      if (this.prettierConfig == null) {
        this.prettierConfig = Collections.singletonMap("parser", "typescript");
      } else {
        final Object replaced = this.prettierConfig.put("parser", "typescript");
        if (replaced != null) {
          getProject().getLogger().warn("overriding parser option to 'typescript'. Was set to '{}'", replaced);
        }
      }
    }
  }

  @Override
  protected void setupTask(SpotlessTask task) {
    // defaults to all typescript files
    if (target == null) {
      target = parseTarget("**/*.ts");
    }
    super.setupTask(task);
  }
}
