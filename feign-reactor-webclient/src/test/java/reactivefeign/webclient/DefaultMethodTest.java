/**
 * Copyright 2018 The Feign Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package reactivefeign.webclient;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import reactivefeign.ReactiveFeign;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import reactivefeign.testcase.IcecreamServiceApi;

/**
 * @author Sergii Karpenko
 */
public class DefaultMethodTest extends reactivefeign.DefaultMethodTest {

  @RegisterExtension
  public static WireMockExtension wireMockRule = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @RegisterExtension
  public static WireMockExtension wireMockRule2 = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @Override
  protected ReactiveFeign.Builder<IcecreamServiceApi> builder() {
    return WebReactiveFeign.builder();
  }

  @Override
  protected <API> ReactiveFeign.Builder<API> builder(Class<API> apiClass) {
    return WebReactiveFeign.builder();
  }

  @Override
  protected ReactiveFeign.Builder<IcecreamServiceApi> builder(long connectTimeoutInMillis) {
    return WebReactiveFeign.<IcecreamServiceApi>builder().options(
            new WebReactiveOptions.Builder().setConnectTimeoutMillis(connectTimeoutInMillis).build()
    );
  }

  @Override
  public WireMockExtension getWiremockRule() {
    return wireMockRule;
  }

  @Override
  public WireMockExtension getWiremockRule2() {
    return wireMockRule2;
  }
}
