package org.vertx.mods;

import java.io.File;

import org.vertx.java.core.Handler;
import org.vertx.java.core.file.impl.PathAdjuster;
import org.vertx.java.core.http.HttpServerRequest;

public class StaticHttpResourceHandler implements Handler<HttpServerRequest> {

  private String webRootPrefix;

  private String indexPage;

  private boolean gzipFiles;

  public StaticHttpResourceHandler(String webRootPrefix, String indexPage,
      boolean gzipFiles) {
    super();
    this.webRootPrefix = webRootPrefix;
    this.indexPage = indexPage;
    this.gzipFiles = gzipFiles;
  }

  @Override
  public void handle(HttpServerRequest req) {
    // browser gzip capability check
    String acceptEncoding = req.headers().get("accept-encoding");
    boolean acceptEncodingGzip = acceptEncoding == null ? false : acceptEncoding.contains("gzip");

    String fileName = webRootPrefix + req.path;
    if (req.path.equals("/")) {
      req.response.sendFile(indexPage);
    } else if (!req.path.contains("..")) {
      // try to send *.gz file
      if (gzipFiles && acceptEncodingGzip) {
        File file = new File(PathAdjuster.adjust(fileName + ".gz"));
        if (file.exists()) {
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
    }  }

}
