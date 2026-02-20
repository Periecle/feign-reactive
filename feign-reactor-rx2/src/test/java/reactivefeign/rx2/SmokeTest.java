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
import io.reactivex.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import reactivefeign.ReactiveFeign;
import reactivefeign.rx2.testcase.IcecreamServiceApi;
import reactivefeign.rx2.testcase.domain.Bill;
import reactivefeign.rx2.testcase.domain.Flavor;
import reactivefeign.rx2.testcase.domain.IceCreamOrder;
import reactivefeign.rx2.testcase.domain.Mixin;
import reactivefeign.rx2.testcase.domain.OrderGenerator;

import java.util.Map;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static reactivefeign.rx2.TestUtils.equalsComparingFieldByFieldRecursively;
import static reactivefeign.rx2.TestUtils.equalsComparingFieldByFieldRecursivelyRx;

/**
 * @author Sergii Karpenko
 */

public class SmokeTest {

  @RegisterExtension
  public static WireMockExtension wireMockRule = WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig()
          .dynamicPort()).build();

  @BeforeEach
  void resetServers() {
    wireMockRule.resetAll();
  }

  protected ReactiveFeign.Builder<IcecreamServiceApi> builder(){
    return Rx2ReactiveFeign.builder();
  }

  private IcecreamServiceApi client;

  private OrderGenerator generator = new OrderGenerator();
  private Map<Integer, IceCreamOrder> orders = generator.generateRange(10).stream()
      .collect(Collectors.toMap(IceCreamOrder::getId, o -> o));

  @BeforeEach
  void setUp() {
    String targetUrl = "http://localhost:" + wireMockRule.getPort();
    client = builder()
        .decode404()
        .target(IcecreamServiceApi.class, targetUrl);
  }

  @Test
  void simpleGetSuccess() throws Exception {

    wireMockRule.stubFor(get(urlEqualTo("/icecream/flavors"))
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(TestUtils.MAPPER.writeValueAsString(Flavor.values()))));

    wireMockRule.stubFor(get(urlEqualTo("/icecream/mixins"))
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(TestUtils.MAPPER.writeValueAsString(Mixin.values()))));

    client.getAvailableFlavors().test()
            .await()
            .assertResult(Flavor.values());

    client.getAvailableMixins().test()
            .await()
            .assertResult(Mixin.values());
    }

  @Test
  void findOrderSuccess() throws Exception {
    IceCreamOrder orderExpected = orders.get(1);
    wireMockRule.stubFor(get(urlEqualTo("/icecream/orders/1"))
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(TestUtils.MAPPER.writeValueAsString(orderExpected))));

    client.findOrder(1).test()
            .await()
            .assertSubscribed()
            .assertValue(equalsComparingFieldByFieldRecursivelyRx(orderExpected))
            .assertNoErrors()
            .assertComplete();
  }

  @Test
  void findOrderEmpty() throws Exception {

    client.findOrder(123).test()
            .await()
            .assertSubscribed()
            .assertNoValues()
            .assertNoErrors()
            .assertComplete();
  }

  @Test
  void makeOrderSuccess() throws Exception {

    IceCreamOrder order = new OrderGenerator().generate(20);
    Bill billExpected = Bill.makeBill(order);

    wireMockRule.stubFor(post(urlEqualTo("/icecream/orders"))
        .withRequestBody(equalTo(TestUtils.MAPPER.writeValueAsString(order)))
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(TestUtils.MAPPER.writeValueAsString(billExpected))));

    Single<Bill> bill = client.makeOrder(order);
    assertThat(bill.blockingGet())
            .matches(equalsComparingFieldByFieldRecursively(billExpected));
  }

  @Test
  void payBillSuccess() throws Exception {

    Bill bill = Bill.makeBill(new OrderGenerator().generate(30));

    wireMockRule.stubFor(post(urlEqualTo("/icecream/bills/pay"))
            .withRequestBody(equalTo(TestUtils.MAPPER.writeValueAsString(bill)))
            .willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(Long.toString(321))));

    Single<Long> result = client.payBill(bill);
    assertThat(result.blockingGet()).isEqualTo(321);
  }
}
