/*
 * Copyright 2015 Corey Baswell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.baswell.httproxy;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

class ExchangeStreamDispatcher
{
  private final IOProxyDirector proxyDirector;

  private final KeepAliveTimeoutReaper keepAliveTimeoutReaper;

  ExchangeStreamDispatcher(IOProxyDirector proxyDirector)
  {
    this.proxyDirector = proxyDirector;
    keepAliveTimeoutReaper = new KeepAliveTimeoutReaper(proxyDirector);
  }

  void dispatch(Socket socket)
  {
    ExecutorService executorService = proxyDirector.getIOThreadPool();
    try
    {
      final PipedExchangeStream exchangeStream = new PipedExchangeStream(socket, proxyDirector);

      executorService.execute(new Runnable()
      {
        public void run()
        {
          exchangeStream.requestLoop();
        }
      });

      executorService.execute(new Runnable()
      {
        public void run()
        {
          exchangeStream.responseLoop();
        }
      });

      if (proxyDirector.useKeepAliveReaper())
      {
        keepAliveTimeoutReaper.watch(exchangeStream);
      }
    }
    catch (IOException e)
    {}
  }
}
