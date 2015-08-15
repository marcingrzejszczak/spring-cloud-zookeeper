/*
 * Copyright 2013-2015 the original author or authors.
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
 */

package org.springframework.cloud.zookeeper.discovery.dependency;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Load balancing rule that returns always the same instance.
 *
 * Ported from {@link org.apache.curator.x.discovery.strategies.StickyStrategy}
 *
 * author: Marcin Grzejszczak, 4financeIT
 */
@Slf4j
public class StickyRule extends AbstractLoadBalancerRule {
	private final IRule masterStrategy;
	private final AtomicReference<Server> ourInstance = new AtomicReference<>(null);
	private final AtomicInteger instanceNumber = new AtomicInteger(-1);

	public StickyRule(IRule masterStrategy) {
		this.masterStrategy = masterStrategy;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig iClientConfig) {

	}

	@Override
	public Server choose(Object key) {
		final List<Server> instances = getLoadBalancer().getServerList(true);
		log.debug("Instances taken from load balancer {}", instances);
		Server localOurInstance = ourInstance.get();
		log.debug("Current saved instance [{}]", localOurInstance);
		if (!instances.contains(localOurInstance)) {
			ourInstance.compareAndSet(localOurInstance, null);
		}
		if (ourInstance.get() == null) {
			Server instance = masterStrategy.choose(key);
			if (ourInstance.compareAndSet(null, instance)) {
				instanceNumber.incrementAndGet();
			}
		}
		return ourInstance.get();
	}

	/**
	 * Each time a new instance is picked, an internal counter is incremented. This way you
	 * can track when/if the instance changes. The instance can change when the selected instance
	 * is not in the current list of instances returned by the instance provider
	 *
	 * @return instance number
	 */
	public int getInstanceNumber() {
		return instanceNumber.get();
	}

}
