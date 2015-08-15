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

package org.springframework.cloud.zookeeper.discovery;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.client.discovery.AbstractDiscoveryLifecycle;

/**
 * @author Spencer Gibb
 */
@Slf4j
public class ZookeeperLifecycle extends AbstractDiscoveryLifecycle {

	private ZookeeperDiscoveryProperties properties;
	private ZookeeperServiceDiscovery serviceDiscovery;

	public ZookeeperLifecycle(ZookeeperDiscoveryProperties properties,
			ZookeeperServiceDiscovery serviceDiscovery) {
		this.properties = properties;
		this.serviceDiscovery = serviceDiscovery;
	}

	@Override
	@SneakyThrows
	protected void register() {
		this.serviceDiscovery.getServiceDiscovery().start();
	}

	// TODO: implement registerManagement

	@Override
	@SneakyThrows
	protected void deregister() {
		this.serviceDiscovery.getServiceDiscovery().unregisterService(
				this.serviceDiscovery.getServiceInstance());
	}

	// TODO: implement deregisterManagement

	@Override
	protected boolean isEnabled() {
		return properties.isEnabled();
	}

	@Override
	protected int getConfiguredPort() {
		return serviceDiscovery.getPort();
	}

	@Override
	protected void setConfiguredPort(int port) {
		serviceDiscovery.setPort(port);
		serviceDiscovery.build();
	}

	@Override
	protected Object getConfiguration() {
		return properties;
	}
}
