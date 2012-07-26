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
import java.util.ArrayList;
import java.util.List;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 *
 * @author dembol
 */
final class SourceUtils {
    
    private static final String DEFAULT_INCLUDES = "**/*.java";

    private SourceUtils() {
    }
    
    private static List<String> getDirectorySources(SourceContext ctx, File dir) {

        final List<String> includes = ctx.getIncludes();
        final List<String> excludes = ctx.getExcludes();
        if (includes.isEmpty()) {
            includes.add(DEFAULT_INCLUDES);
        }
        
        final List<String> sources = new ArrayList<String>();
        final DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(dir);
        ds.setIncludes(includes.toArray(new String[includes.size()]));
        ds.setExcludes(excludes.toArray(new String[excludes.size()]));
        ds.scan();
      
        for (final String path : ds.getIncludedFiles()) {
            sources.add(dir.getAbsolutePath() + "/" + path);
        }
  
        return sources;
    }

    public static List<String> getProjectSources(SourceContext ctx) {
        
        final List<String> sources = new ArrayList<String>();
        for (final String dirPath : ctx.getCompileSourceDirs()) {
            final File dir = new File(dirPath);
            if (!dir.exists()) {
                continue;
            }
            
            sources.addAll(getDirectorySources(ctx, dir));
        }
        
        return sources;
    }
}
