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
package reactivefeign;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactivefeign.testcase.IcecreamServiceApi;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Sergii Karpenko
 */
abstract public class ConnectionTimeoutTest extends BaseReactorTest{

  private ServerSocket serverSocket;
  private Socket socket;
  private int port;

  abstract protected ReactiveFeignBuilder<IcecreamServiceApi> builder(long connectTimeoutInMillis);

  @BeforeEach
  public void before() throws IOException {
    // server socket with single element backlog queue (1) and dynamicaly allocated
    // port (0)
    serverSocket = new ServerSocket(0, 1);
    // just get the allocated port
    port = serverSocket.getLocalPort();
    // fill backlog queue by this request so consequent requests will be blocked
    socket = new Socket();
    socket.connect(serverSocket.getLocalSocketAddress());
  }

  @AfterEach
  public void after() throws IOException {
    // some cleanup
    if (serverSocket != null && !serverSocket.isClosed()) {
      serverSocket.close();
    }
  }

  // TODO investigate why doesn't work on codecov.io but works locally
  @Disabled
  @Test
  public void shouldFailOnConnectionTimeout() {

    Throwable exception = assertThrows(Exception.class, () -> {

      IcecreamServiceApi client = builder(300)
              .target(IcecreamServiceApi.class, "http://localhost:" + port);

      client.findOrder(1).subscribeOn(testScheduler()).block();
    });

    assertThat(exception.getCause(), Matchers.instanceOf(ConnectException.class));
  }

}
