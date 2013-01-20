/*
 * Copyright 2011-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.mods;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;

import java.io.File;

/**
 * The base class for a  simple web server module that can serve static files, 
 * and also can bridge event bus messages to/from client side JavaScript and 
 * the server side event bus.
 *
 * Extend this class to customise it using a RouteMatcher.
 *
 * Please see the modules manual for full description of what configuration
 * parameters it takes.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author pidster
 */
public abstract class WebServerBase extends BusModBase {

  private String webRootPrefix;
  private String indexPage;
  private boolean gzipFiles;

  public void start() {
    super.start();

    HttpServer server = vertx.createHttpServer();

    gzipFiles = getOptionalBooleanConfig("gzip_files", false);
    String webRoot = getOptionalStringConfig("web_root", "web");
    String index = getOptionalStringConfig("index_page", "index.html");
    webRootPrefix = webRoot + File.separator;
    indexPage = webRootPrefix + index;

    if (getOptionalBooleanConfig("ssl", false)) {
      server.setSSL(true).setKeyStorePassword(getOptionalStringConfig("key_store_password", "wibble"))
                         .setKeyStorePath(getOptionalStringConfig("key_store_path", "server-keystore.jks"));
    }

    if (getOptionalBooleanConfig("route_matcher", false)) {
      server.requestHandler(routeMatcher());
    }
    else if (getOptionalBooleanConfig("static_files", true)) {
      server.requestHandler(new StaticHttpResourceHandler(webRootPrefix, indexPage, gzipFiles));
    }

    boolean bridge = getOptionalBooleanConfig("bridge", false);
    if (bridge) {
      SockJSServer sjsServer = vertx.createSockJSServer(server);
      JsonArray inboundPermitted = getOptionalArrayConfig("inbound_permitted", new JsonArray());
      JsonArray outboundPermitted = getOptionalArrayConfig("outbound_permitted", new JsonArray());

      sjsServer.bridge(getOptionalObjectConfig("sjs_config", new JsonObject().putString("prefix", "/eventbus")),
                       inboundPermitted, outboundPermitted,
                       getOptionalLongConfig("auth_timeout", 5 * 60 * 1000),
                       getOptionalStringConfig("auth_address", "vertx.basicauthmanager.authorise"));
    }

    server.listen(getOptionalIntConfig("port", 80), getOptionalStringConfig("host", "0.0.0.0"));
  }

  protected abstract RouteMatcher routeMatcher();

  protected String getWebRootPrefix() {
    return webRootPrefix;
  }

  protected String getIndexPage() {
    return indexPage;
  }

  protected boolean isGzipFiles() {
    return gzipFiles;
  }

}
