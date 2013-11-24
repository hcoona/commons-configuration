/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration.builder.combined;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationAssert;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration.reloading.CombinedReloadingController;
import org.apache.commons.configuration.reloading.ReloadingController;
import org.apache.commons.configuration.reloading.ReloadingControllerSupport;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code ReloadingCombinedConfigurationBuilder}.
 *
 * @version $Id$
 */
public class TestReloadingCombinedConfigurationBuilder
{
    /** The builder to be tested. */
    private ReloadingCombinedConfigurationBuilder builder;

    @Before
    public void setUp() throws Exception
    {
        builder = new ReloadingCombinedConfigurationBuilder();
    }

    /**
     * Tests a definition configuration which does not contain sources with
     * reloading support.
     */
    @Test
    public void testNoReloadableSources() throws ConfigurationException
    {
        File testFile =
                ConfigurationAssert
                        .getTestFile("testDigesterConfiguration.xml");
        builder.configure(new CombinedBuilderParametersImpl()
                .setDefinitionBuilder(
                        new FileBasedConfigurationBuilder<XMLConfiguration>(
                                XMLConfiguration.class))
                .setDefinitionBuilderParameters(
                        new FileBasedBuilderParametersImpl().setFile(testFile)));
        builder.getConfiguration();
        CombinedReloadingController rc =
                (CombinedReloadingController) builder.getReloadingController();
        assertTrue("Got sub reloading controllers", rc.getSubControllers()
                .isEmpty());
    }

    /**
     * Tests whether the definition builder created by default supports
     * reloading.
     */
    @Test
    public void testReloadableDefinitionBuilder() throws ConfigurationException
    {
        File testFile =
                ConfigurationAssert
                        .getTestFile("testDigesterConfiguration.xml");
        builder.configure(new FileBasedBuilderParametersImpl()
                .setFile(testFile));
        builder.getConfiguration();
        CombinedReloadingController rc =
                (CombinedReloadingController) builder.getReloadingController();
        Collection<ReloadingController> subControllers = rc.getSubControllers();
        assertEquals("Wrong number of sub controllers", 1,
                subControllers.size());
        ReloadingController subctrl =
                ((ReloadingControllerSupport) builder.getDefinitionBuilder())
                        .getReloadingController();
        assertSame("Wrong sub controller", subctrl, subControllers.iterator()
                .next());
    }

    /**
     * Tests whether a nested combined configuration definition can be loaded
     * with reloading support.
     */
    @Test
    public void testNestedReloadableSources() throws ConfigurationException
    {
        File testFile =
                ConfigurationAssert.getTestFile("testCCReloadingNested.xml");
        builder.configure(new FileBasedBuilderParametersImpl()
                .setFile(testFile));
        builder.getConfiguration();
        CombinedReloadingController rc =
                (CombinedReloadingController) builder.getReloadingController();
        Collection<ReloadingController> subControllers = rc.getSubControllers();
        assertEquals("Wrong number of sub controllers", 2,
                subControllers.size());
        ReloadingControllerSupport ccBuilder =
                (ReloadingControllerSupport) builder.getNamedBuilder("cc");
        assertTrue("Sub controller not found",
                subControllers.contains(ccBuilder.getReloadingController()));
        CombinedReloadingController rc2 =
                (CombinedReloadingController) ccBuilder
                        .getReloadingController();
        assertEquals("Wrong number of sub controllers (2)", 3, rc2
                .getSubControllers().size());
    }

    /**
     * Tests whether initialization parameters are correctly processed.
     */
    @Test
    public void testInitWithParameters() throws ConfigurationException
    {
        FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl();
        params.setFile(ConfigurationAssert
                .getTestFile("testDigesterConfiguration.xml"));
        builder =
                new ReloadingCombinedConfigurationBuilder(
                        params.getParameters());
        CombinedConfiguration cc = builder.getConfiguration();
        assertTrue("Property not found", cc.getBoolean("test.boolean"));
    }

    /**
     * Tests whether the failOnInit flag is passed to the super constructor.
     */
    @Test
    public void testInitWithFailOnInitFlag()
    {
        builder = new ReloadingCombinedConfigurationBuilder(null, true);
        assertTrue("Flag not set", builder.isAllowFailOnInit());
    }
}