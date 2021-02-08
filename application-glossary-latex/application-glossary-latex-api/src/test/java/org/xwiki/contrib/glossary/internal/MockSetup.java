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
package org.xwiki.contrib.glossary.internal;

import java.io.InputStream;
import java.io.Writer;
import java.util.Locale;
import java.util.Properties;

import javax.script.ScriptContext;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.mockito.stubbing.Answer;
import org.xwiki.localization.LocalizationContext;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.script.service.ScriptServiceManager;
import org.xwiki.template.Template;
import org.xwiki.template.TemplateContent;
import org.xwiki.template.TemplateManager;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.text.StringUtils;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.VelocityFactory;
import org.xwiki.velocity.tools.EscapeTool;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mocks for the tests.
 *
 * @version $Id$
 * @since 1.0
 */
public class MockSetup
{
    public static void setUp(MockitoComponentManager componentManager) throws Exception
    {
        // Mock the TemplateManager in order to not depend on oldcore. We implement a TemplateManager
        // using a Velocity Engine.
        TemplateManager templateManager = componentManager.registerMockComponent(TemplateManager.class);
        when(templateManager.getTemplate(any(String.class))).thenAnswer(invocation -> {
            String templateName = invocation.getArgument(0);
            String location = String.format("templates/%s", templateName);
            InputStream templateStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
            if (templateStream != null) {
                Template template = mock(Template.class, templateName);
                TemplateContent templateContent = mock(TemplateContent.class, templateName);
                when(templateContent.getContent()).thenReturn(IOUtils.toString(templateStream, "UTF-8"));
                when(template.getContent()).thenReturn(templateContent);
                return template;
            }
            return null;
        });

        VelocityFactory velocityFactory = componentManager.getInstance(VelocityFactory.class);
        VelocityEngine velocityEngine = velocityFactory.createVelocityEngine("somekey", new Properties());

        VelocityContext vcontext = new VelocityContext();
        vcontext.put("escapetool", new EscapeTool());
        vcontext.put("stringtool", new StringUtils());
        vcontext.put("mathtool", new MathTool());
        vcontext.put("numbertool", new NumberTool());
        vcontext.put("services", componentManager.getInstance(ScriptServiceManager.class));

        // Bridge the Script Context bindings into the Velocity Context
        ScriptContextManager scriptContextManager = componentManager.registerMockComponent(ScriptContextManager.class);
        ScriptContext scriptContext = mock(ScriptContext.class);
        when(scriptContextManager.getCurrentScriptContext()).thenReturn(scriptContext);
        doAnswer((Answer<Object>) invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            vcontext.put(key, value);
            return null;
        }).when(scriptContext).setAttribute(any(String.class), any(Object.class), anyInt());
        doAnswer((Answer<Object>) invocation -> {
            String key = invocation.getArgument(0);
            return vcontext.get(key);
        }).when(scriptContext).getAttribute(any(String.class));

        doAnswer((Answer<Object>) invocation -> {
            Template template = invocation.getArgument(0);
            Writer writer = invocation.getArgument(1);
            velocityEngine.evaluate(vcontext, writer, "velocity template", template.getContent().getContent());
            return null;
        }).when(templateManager).render(any(Template.class), any(Writer.class));

        // Mock to get the current locale
        LocalizationContext lc = componentManager.registerMockComponent(LocalizationContext.class);
        when(lc.getCurrentLocale()).thenReturn(Locale.ENGLISH);
    }
}
