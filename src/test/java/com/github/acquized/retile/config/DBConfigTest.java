package com.github.acquized.retile.config;

import net.cubespace.Yamler.Config.InvalidConfigurationException;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.fail;

public class DBConfigTest {

    public static final File DIRECTORY = new File("build" + File.separator + "test-output" + File.separator + "config");
    public DBConfig file;

    @Before
    public void createDir() {
        if((!DIRECTORY.exists()) && (!DIRECTORY.mkdirs())) {
            fail("Could not create Directory");
        }
    }

    @Test
    public void testCreation() throws InvalidConfigurationException {
        file = new DBConfig(new File(DIRECTORY, "database.yml"));
        file.init();
    }

}