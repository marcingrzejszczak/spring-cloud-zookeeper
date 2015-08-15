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

package org.springframework.cloud.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * @author Spencer Gibb
 */
public class ZookeeperHealthIndicator extends AbstractHealthIndicator {
	@Autowired
	CuratorFramework curator;

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		try {
			if (curator.getState() != CuratorFrameworkState.STARTED) {
				builder.down().withDetail("error", "Client not started");
			}
			else if (curator.checkExists().forPath("/") == null) {
				builder.down().withDetail("error", "Root for namespace does not exist");
			}
			else {
				builder.up();
			}
			builder.withDetail("connectionString",
					curator.getZookeeperClient().getCurrentConnectionString())
					.withDetail("state", curator.getState());
		}
		catch (Exception e) {
			builder.down(e);
		}
	}
}
