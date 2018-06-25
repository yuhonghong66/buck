/*
 * Copyright 2017-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.android;

import com.facebook.buck.config.BuckConfig;
import com.facebook.buck.core.description.DescriptionCreationContext;
import com.facebook.buck.core.model.targetgraph.DescriptionProvider;
import com.facebook.buck.core.model.targetgraph.DescriptionWithTargetGraph;
import com.facebook.buck.cxx.toolchain.CxxBuckConfig;
import com.facebook.buck.jvm.java.JavaBuckConfig;
import com.facebook.buck.jvm.kotlin.KotlinBuckConfig;
import com.facebook.buck.jvm.scala.ScalaBuckConfig;
import com.facebook.buck.sandbox.SandboxExecutionStrategy;
import com.facebook.buck.toolchain.ToolchainProvider;
import com.facebook.buck.util.environment.Platform;
import java.util.Arrays;
import java.util.Collection;
import org.pf4j.Extension;

@Extension
public class AndroidDescriptionsProvider implements DescriptionProvider {

  @Override
  public Collection<DescriptionWithTargetGraph<?>> getDescriptions(
      DescriptionCreationContext context) {
    SandboxExecutionStrategy sandboxExecutionStrategy = context.getSandboxExecutionStrategy();
    ToolchainProvider toolchainProvider = context.getToolchainProvider();
    BuckConfig config = context.getBuckConfig();
    CxxBuckConfig cxxBuckConfig = new CxxBuckConfig(config);
    JavaBuckConfig javaConfig = config.getView(JavaBuckConfig.class);
    ProGuardConfig proGuardConfig = new ProGuardConfig(config);
    DxConfig dxConfig = new DxConfig(config);
    ScalaBuckConfig scalaConfig = new ScalaBuckConfig(config);
    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(config);
    AndroidBuckConfig androidBuckConfig = new AndroidBuckConfig(config, Platform.detect());
    ApkConfig apkConfig = new ApkConfig(config);

    AndroidLibraryCompilerFactory defaultAndroidCompilerFactory =
        new DefaultAndroidLibraryCompilerFactory(javaConfig, scalaConfig, kotlinBuckConfig);

    AndroidManifestFactory androidManifestFactory = new AndroidManifestFactory();

    return Arrays.asList(
        new AndroidAarDescription(androidManifestFactory, cxxBuckConfig, toolchainProvider),
        new AndroidManifestDescription(androidManifestFactory),
        new AndroidAppModularityDescription(),
        new AndroidBinaryDescription(
            javaConfig,
            androidBuckConfig,
            proGuardConfig,
            config,
            cxxBuckConfig,
            dxConfig,
            apkConfig,
            toolchainProvider,
            new AndroidBinaryGraphEnhancerFactory()),
        new AndroidBuildConfigDescription(toolchainProvider),
        new AndroidInstrumentationApkDescription(
            javaConfig, proGuardConfig, cxxBuckConfig, dxConfig, apkConfig, toolchainProvider),
        new AndroidInstrumentationTestDescription(config),
        new AndroidLibraryDescription(javaConfig, defaultAndroidCompilerFactory, toolchainProvider),
        new AndroidPrebuiltAarDescription(toolchainProvider),
        new AndroidResourceDescription(androidBuckConfig),
        new RobolectricTestDescription(
            toolchainProvider, javaConfig, defaultAndroidCompilerFactory),
        new PrebuiltNativeLibraryDescription(),
        new NdkLibraryDescription(),
        new GenAidlDescription(),
        new ApkGenruleDescription(toolchainProvider, sandboxExecutionStrategy));
  }
}
