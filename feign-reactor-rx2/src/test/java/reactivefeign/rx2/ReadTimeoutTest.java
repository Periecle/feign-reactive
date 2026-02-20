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
package reactivefeign.rx2;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import reactivefeign.ReactiveFeign;
import reactivefeign.ReactiveOptions;
import reactivefeign.client.ReadTimeoutException;
import reactivefeign.rx2.testcase.IcecreamServiceApi;
import reactivefeign.webclient.WebReactiveOptions;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author Sergii Karpenko
 */
public class ReadTimeoutTest {

  @RegisterExtension
  public static WireMockExtension wireMockRule = WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig()
          .dynamicPort()).build();

  protected ReactiveFeign.Builder<IcecreamServiceApi> builder(ReactiveOptions options){
    return Rx2ReactiveFeign.<IcecreamServiceApi>builder().options(options);
  }

  @Test
  void shouldFailOnReadTimeout() throws Exception {

    String orderUrl = "/icecream/orders/1";

    wireMockRule.stubFor(get(urlEqualTo(orderUrl))
        .withHeader("Accept", equalTo("application/json"))
        .willReturn(aResponse().withFixedDelay(200)));

    IcecreamServiceApi client = builder(
        new WebReactiveOptions.Builder()
            .setReadTimeoutMillis(100)
            .setConnectTimeoutMillis(300)
            .build())
                .target(IcecreamServiceApi.class,
                    "http://localhost:" + wireMockRule.getPort());

    client.findOrder(1).test()
            .await()
            .assertSubscribed()
            .assertError(ReadTimeoutException.class);
  }
}
