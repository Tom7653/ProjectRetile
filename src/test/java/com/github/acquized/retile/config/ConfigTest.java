package com.github.acquized.retile.config;

import net.cubespace.Yamler.Config.InvalidConfigurationException;

import org.junit.Test;

import java.io.File;

public class ConfigTest {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void prepareDirectory() {
        new File("build" + File.separator + "tmp" + File.separator + "config").mkdirs();
    }

    @Test
    public void testConfig() throws InvalidConfigurationException {
        Config cfg = new Config(new File("build" + File.separator + "tmp" + File.separator + "config" + File.separator + "config.yml"));
        cfg.init();
    }

    @Test
    public void testDBConfig() throws InvalidConfigurationException {
        DBConfig cfg = new DBConfig(new File("build" + File.separator + "tmp" + File.separator + "config" + File.separator + "database.yml"));
        cfg.init();
    }

    @Test
    public void testBlacklistConfig() throws InvalidConfigurationException {
        Blacklist cfg = new Blacklist(new File("build" + File.separator + "tmp" + File.separator + "config" + File.separator + "blacklist.yml"));
        cfg.init();
    }

}