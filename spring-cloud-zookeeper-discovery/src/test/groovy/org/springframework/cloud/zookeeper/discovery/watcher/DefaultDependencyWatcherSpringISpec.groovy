package org.springframework.cloud.zookeeper.discovery.watcher

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.test.TestingServer
import org.apache.curator.x.discovery.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.cloud.zookeeper.discovery.base.AvailablePortScanner
import org.springframework.cloud.zookeeper.discovery.watcher.presence.DependencyPresenceOnStartupVerifier
import org.springframework.cloud.zookeeper.discovery.watcher.presence.LogMissingDependencyChecker
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@ContextConfiguration(classes = Config, loader = SpringApplicationContextLoader)
@ActiveProfiles('watcher')
class DefaultDependencyWatcherSpringISpec extends Specification {

    @Autowired AssertableDependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier
    @Autowired AssertableDependencyWatcherListener dependencyWatcherListener
    @Autowired ServiceDiscovery serviceDiscovery
    @Autowired ServiceInstance serviceInstance

    def 'should verify that presence of a dependency has been checked'() {
        expect:
            dependencyPresenceOnStartupVerifier.startupPresenceVerified
    }

    def 'should verify that dependency watcher listener is successfully registered and operational'() {
        when:
            serviceDiscovery.unregisterService(serviceInstance)
        then:
            new PollingConditions().eventually {
                dependencyWatcherListener.dependencyState == DependencyState.DISCONNECTED
            }
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableConfigurationProperties
    @Import(DependencyWatcherAutoConfiguration)
    @ComponentScan('org.springframework.cloud.zookeeper.discovery.watcher.dependency')
    static class Config {

        @Bean static PropertySourcesPlaceholderConfigurer propertiesConfigurer() {
            return new PropertySourcesPlaceholderConfigurer()
        }

        @Bean(destroyMethod = 'close') TestingServer testingServer() {
            return new TestingServer(AvailablePortScanner.getNextFreePort())
        }

        @Bean ServiceInstance serviceInstance() {
            return ServiceInstance.builder().uriSpec(new UriSpec("{scheme}://{address}:{port}/"))
                    .address('anyUrl')
                    .port(10)
                    .name('testInstance')
                    .build()
        }

        @Bean(initMethod = 'start', destroyMethod = 'close') ServiceDiscovery serviceDiscovery() {
            return ServiceDiscoveryBuilder
                    .builder(Void)
                    .basePath('/')
                    .client(curatorFramework())
                    .thisInstance(serviceInstance())
                    .build()
        }

        @Bean(initMethod = 'start', destroyMethod = 'close') CuratorFramework curatorFramework() {
            return CuratorFrameworkFactory.newClient(testingServer().connectString, new ExponentialBackoffRetry(20, 20, 500))
        }

        @Bean DependencyWatcherListener dependencyWatcherListener() {
            return new AssertableDependencyWatcherListener()
        }

        @Bean DependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier() {
            return new AssertableDependencyPresenceOnStartupVerifier()
        }

    }

    static class AssertableDependencyWatcherListener implements DependencyWatcherListener {

        DependencyState dependencyState = DependencyState.CONNECTED

        @Override
        void stateChanged(String dependencyName, DependencyState newState) {
            dependencyState = newState
        }
    }

    static class AssertableDependencyPresenceOnStartupVerifier extends DependencyPresenceOnStartupVerifier {

        boolean startupPresenceVerified = false

        AssertableDependencyPresenceOnStartupVerifier() {
            super(new LogMissingDependencyChecker())
        }

        @Override
        void verifyDependencyPresence(String dependencyName, ServiceCache serviceCache, boolean required) {
            startupPresenceVerified = true
        }
    }
}
