/* *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.templatemanager.services.configuration;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongoCmdOptions;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.Slf4jStreamProcessor;

/**
 * This class provide test configuration for setting up embedded MONGODB for integration tests.
 *
 * @author echchik
 *
 */
@Configuration
public class EmbeddedMongoConfiguration {

    private static final String TEST_IP_ADDRESS = "127.0.0.1";

    private static final int TEST_PORT = 44444;

    @Autowired
    private MongodProcess mongodProcess;

    @Autowired
    private MongoClient mongoClient;

    @Bean(destroyMethod = "close")
    @DependsOn("mongodProcess")
    public MongoClient mongoClient() {
        final MongoClient mongoClient = new MongoClient(TEST_IP_ADDRESS, TEST_PORT);
        final DB db = mongoClient.getDB("aia");
        db.addUser("admin", "admin".toCharArray());
        return mongoClient;
    }

    @Bean(name = "mongodProcess", destroyMethod = "stop")
    public MongodProcess mongodProcess(final MongodExecutable mongodExecutable) throws IOException {
        return mongodExecutable.start();
    }

    @Bean(destroyMethod = "stop")
    public MongodExecutable mongodExecutable(final MongodStarter mongodStarter, final IMongodConfig iMongodConfig)
            throws IOException {
        return mongodStarter.prepare(iMongodConfig);
    }

    @Bean
    public IMongodConfig mongodConfig() throws IOException {
        final IMongoCmdOptions cmdOptions = new MongoCmdOptionsBuilder().verbose(false).build();
        return new MongodConfigBuilder().version(Version.Main.DEVELOPMENT)
                .net(new Net(TEST_IP_ADDRESS, TEST_PORT, false)).configServer(false).cmdOptions(cmdOptions).build();
    }

    @Bean
    public MongodStarter mongodStarter() {
        return MongodStarter.getInstance(buildRuntimeConfig());
    }

    @Bean
    public IRuntimeConfig buildRuntimeConfig() {
        return new RuntimeConfigBuilder().defaults(Command.MongoD).processOutput(buildOutputConfig()).build();
    }

    @Bean
    public ProcessOutput buildOutputConfig() {
        final Logger logger = LoggerFactory.getLogger(MongodProcess.class);

        return new ProcessOutput(new Slf4jStreamProcessor(logger, Slf4jLevel.TRACE), new Slf4jStreamProcessor(logger,
                Slf4jLevel.WARN), new Slf4jStreamProcessor(logger, Slf4jLevel.INFO));
    }
}
