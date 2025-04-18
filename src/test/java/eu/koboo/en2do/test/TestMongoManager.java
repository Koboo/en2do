package eu.koboo.en2do.test;

import com.mongodb.MongoCompressor;
import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.SettingsBuilder;
import eu.koboo.en2do.configurators.ClientConfiguratorCompressors;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public final class TestMongoManager implements TestExecutionListener {

    public static MongoManager MANAGER;

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        log.info("Initializing MongoManager singleton instance..");
        SettingsBuilder settingsBuilder = new SettingsBuilder()
            .clientConfigurator(new ClientConfiguratorCompressors(Collections.singletonList(MongoCompressor.createZlibCompressor())))
            .appendMethodNameAsQueryComment()
            .disableMongoDBLogger();
        MANAGER = new MongoManager(settingsBuilder);
        assertNotNull(MANAGER);
        log.info("Finished initializing MongoManager singleton instance!");
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        log.info("Tearing down MongoManager singleton instance..");
        assertNotNull(MANAGER);
        MANAGER.close();
        log.info("Finished tearing down MongoManager singleton instance..");
    }
}
