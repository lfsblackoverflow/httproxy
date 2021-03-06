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
import java.io.InputStream;
import java.io.OutputStream;

import static org.baswell.httproxy.PipedMessageStreamMethods.*;

class PipedResponseStream extends PipedResponse
{
  private final PipedExchangeStream exchangeStream;

  final OutputStream outputStream;

  private final byte[] readBytes;

  private final int sleepSecondsOnReadWait;

  InputStream currentInputStream;

  boolean overSSL;

  PipedResponseStream(IOProxyDirector proxyDirector, PipedExchangeStream exchangeStream, OutputStream outputStream)
  {
    super(proxyDirector);

    this.exchangeStream = exchangeStream;
    this.outputStream = outputStream;

    readBytes = new byte[bufferSize];
    sleepSecondsOnReadWait = proxyDirector.getSleepSecondsOnReadWait();
  }

  void readAndWriteMessage() throws ProxiedIOException, HttpProtocolException, EndProxiedRequestException
  {
    doReadAndWriteMessage(this, currentInputStream, readBytes, sleepSecondsOnReadWait);
  }

  @Override
  boolean write() throws ProxiedIOException
  {
    return doWrite(this, outputStream, readBytes);
  }

  @Override
  void onResponse(HttpResponse response) throws IOException, EndProxiedRequestException
  {
    exchangeStream.onResponse();
    outputStream.write(currentResponse.toBytes());
    exchangeStream.onResponseHeaderSent();
  }

  @Override
  void onMessageDone() throws IOException
  {}

  @Override
  boolean overSSL()
  {
    return overSSL;
  }
}
