/*
 * Copyright (C) 2012 devesion.com
 * Contact: Lukas Dembinski <dembol@devesion.com>
 * 
 * Initial developer: Lukas Dembinski <dembol@devesion.com>
 * Contributor(s):
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.devesion.maven.jsr308;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Says "Hi" to the user.
 * 
 * @goal check
 * @author dembol
 * @requiresDependencyResolution compile 
 */
public class CheckersPlugin extends AbstractMojo {

    /**
     * A list of checkers.
     * 
     * @parameter
     * @description a list of checkers
     */
    private final List<String> checkers = new LinkedList<String>();
    
    /** 
     * A list of plugin dependencies.
     * 
     * @parameter default-value="${plugin.artifacts}" 
     * @required
     */
    private List<Artifact> dependencies;
    
    /**
     * The source directories containing the sources to be processed.
     *
     * @parameter expression="${project.compileSourceRoots}" 
     * @required 
     * @readonly
     */
    private List<String> compileSourceDirs;

    /**
     * A list of project classpath elements.
     * 
     * @parameter expression="${project.compileClasspathElements}" 
     * @required
     * @readonly
     */
    private List<String> compileClasspathElements;

    /**
     * A list of source filename patterns to be included by checkers. Default is ** /*.java.
     * 
     * @parameter
     **/
    private final List<String> includes = new LinkedList<String>();
    
    /**
     * A list of source filename patterns to be excluded by checkers.
     * 
     * @parameter
     **/
    private final List<String> excludes = new LinkedList<String>();

    /**
     * Additional user parameters for java.
     * 
     * @parameter
     */
    private String userJavaParams;
    
    /**
     * Additional user parameters for javac.
     * 
     * @parameter
     */
    private String userJavacParams;
        
    /**
     * Indicates whether the build cycle will fail on JSR-308 checkers errors. Default value is false.
     * 
     * @parameter default-value="false"
     */
    private boolean failOnError;
    
    /**
     * The path containing valid jsr308-all.jar. Default is empty (jsr308-all.jar will be 
     * get from the jsr308-checker-plugin dependencies from the local repository)
     * 
     * @parameter
     */
    private String checkerJar;
    
    private static final String JSR308_ALL_GROUP_ID = "types.checkers";
    
    private static final String JSR308_ALL_ARTIFACT_ID = "jsr308-all";
    
    private static final int EXIT_CODE_OK = 0;
    
    /**
     * Plugin Entry point.
     * 
     * @throws MojoExecutionException exception
     * @throws MojoFailureException exception
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        
        getLog().info("Executing JSR-308 Checkers");
        
        if (checkers.size() <= 0) {
            getLog().info("No checkers found, omitting checkers execution");
            return;
        }

        final SourceContext sourceCtx = new SourceContext(compileSourceDirs, includes, excludes);
        final List<String> sources = SourceUtils.getProjectSources(sourceCtx);
        if (sources.isEmpty()) {
            getLog().info("The project does not contains any sources, omitting checkers execution");
            return;
        }
        
        final CommandLine cl = new CommandLine("java");
        if (checkerJar == null || checkerJar.isEmpty()) {
            checkerJar = ArtifactUtils.getArtifactPath(JSR308_ALL_GROUP_ID, JSR308_ALL_ARTIFACT_ID, dependencies);
            if (checkerJar == null) {
                throw new MojoExecutionException("Cannot find " + JSR308_ALL_GROUP_ID + ":" + JSR308_ALL_ARTIFACT_ID + " artifact jar in the local repository.");
            }
        }
        
        cl.addArgument("-Xbootclasspath/p:" + checkerJar);
        cl.addArgument("-ea:com.sun.tools");
        if (userJavaParams != null) {
            cl.addArgument(userJavaParams);
        }
        cl.addArgument("-jar");
        cl.addArgument(checkerJar);
        cl.addArgument("-proc:only");
    
        // adding checkers
        for (String checker : checkers) {
            cl.addArgument("-processor");
            cl.addArgument(checker);
        }

        // adding project sources
        cl.addArguments(sources.toArray(new String[sources.size()]));
        
        // adding classpath
        final StringBuilder sb = new StringBuilder();
        for (String element : compileClasspathElements) {
            sb.append(element);
            sb.append(File.pathSeparator);
        }
        
        cl.addArgument("-classpath");
        cl.addArgument(sb.toString());
        if (userJavacParams != null) {
            cl.addArgument(userJavacParams);
        }
        
        // executing compiler
        final DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(EXIT_CODE_OK);

        try {
            executor.execute(cl);
        } catch (ExecuteException ex) {
            
            if (failOnError) {
                throw new MojoExecutionException("Unable to continue because of some errors reported by checkers - " + ex.getMessage());
            } else {
                getLog().error("Some errors has been reported by checkers - " + ex.getMessage());
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("cannot execute checkers", ex);
        }
    }
}

