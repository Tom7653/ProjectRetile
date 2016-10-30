package com.github.acquized.retile.config;

import net.cubespace.Yamler.Config.InvalidConfigurationException;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.fail;

public class BlacklistTest {

    public static final File DIRECTORY = new File("build" + File.separator + "test-output" + File.separator + "config");
    public Blacklist file;

    @Before
    public void createDir() {
        if((!DIRECTORY.exists()) && (!DIRECTORY.mkdirs())) {
            fail("Could not create Directory");
        }
    }

    @Test
    public void testCreation() throws InvalidConfigurationException {
        file = new Blacklist(new File(DIRECTORY, "blacklist.yml"));
        file.init();
    }

}