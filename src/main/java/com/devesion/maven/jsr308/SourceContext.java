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

import java.util.List;

/**
 *
 * @author dembol
 */
class SourceContext {
    
    private final List<String> compileSourceDirs;

    private final List<String> includes;
    
    private final List<String> excludes;
    
    SourceContext(List<String> compileSourceDirs, List<String> includes, List<String> excludes) {
        this.compileSourceDirs = compileSourceDirs;
        this.includes = includes;
        this.excludes = excludes;
    }

    public List<String> getCompileSourceDirs() {
        return compileSourceDirs;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public List<String> getIncludes() {
        return includes;
    }
}
