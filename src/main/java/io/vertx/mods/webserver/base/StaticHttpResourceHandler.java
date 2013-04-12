/*
 * Copyright 2013 the original author or authors.
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
package io.vertx.mods.webserver.base;

import org.vertx.java.core.Handler;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author swilliams
 *
 */
public class StaticHttpResourceHandler implements Handler<HttpServerRequest> {

  private String webRootPrefix;

  private String indexPage;

  private boolean gzipFiles;

  private FileSystem fileSystem;

  public StaticHttpResourceHandler(FileSystem fileSystem, String webRootPrefix, String indexPage,
      boolean gzipFiles) {
    super();
    this.fileSystem = fileSystem;
    this.webRootPrefix = webRootPrefix;
    this.indexPage = indexPage;
    this.gzipFiles = gzipFiles;
  }

  @Override
  public void handle(HttpServerRequest req) {
    String acceptEncoding = req.headers().get("accept-encoding");
    boolean acceptEncodingGzip = acceptEncoding == null ? false : acceptEncoding.contains("gzip");
    String fileName = webRootPrefix + req.path;
    try {
      if (req.path.equals("/")) {
        req.response.sendFile(indexPage);
      } else if (!req.path.contains("..")) {
        // try to send *.gz file
        if (gzipFiles && acceptEncodingGzip) {
          boolean exists = fileSystem.existsSync(fileName + ".gz");
          if (exists) {
            // found file with gz extension
            req.response.putHeader("content-encoding", "gzip");
            req.response.sendFile(fileName + ".gz");
          } else {
            // not found gz file, try to send uncompressed file
            req.response.sendFile(fileName);
          }
        } else {
          // send not gzip file
          req.response.sendFile(fileName);
        }
      } else {
        req.response.statusCode = 404;
        req.response.end();
      }
    } catch (Exception e) {
      throw new IllegalStateException("Failed to check file");
    }
  }

}
