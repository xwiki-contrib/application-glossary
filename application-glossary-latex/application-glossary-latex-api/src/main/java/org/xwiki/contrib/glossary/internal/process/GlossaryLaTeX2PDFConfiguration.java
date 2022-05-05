/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.glossary.internal.process;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.contrib.latex.pdf.process.LaTeX2PDFConfiguration;

/**
 * Overridden configuration to execute {@code makeglossaries} in the docker image.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class GlossaryLaTeX2PDFConfiguration implements LaTeX2PDFConfiguration
{
    private static final String PREFIX = "latex.pdf.process.";

    private static final String DEFAULT_COMMAND = "pdflatex -shell-escape index.tex";

    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource configurationSource;

    @Override
    public List<String> getCommands()
    {
        // Note: we run the compilation twice so that the TOC is generated properly (it requires two passes from
        // pdflatex).
        return this.configurationSource.getProperty(PREFIX + "commands",
            Arrays.asList(DEFAULT_COMMAND, "makeglossaries index", DEFAULT_COMMAND));
    }
}
