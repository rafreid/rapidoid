package org.rapidoid.reverseproxy;

import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.concurrent.Callback;
import org.rapidoid.http.*;
import org.rapidoid.http.impl.lowlevel.HttpIO;
import org.rapidoid.job.Jobs;
import org.rapidoid.log.LogLevel;
import org.rapidoid.u.U;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
 * #%L
 * rapidoid-http-server
 * %%
 * Copyright (C) 2014 - 2017 Nikolche Mihajlovski and contributors
 * %%
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
 * #L%
 */

@Authors("Nikolche Mihajlovski")
@Since("5.2.0")
public class ReverseProxy extends AbstractReverseProxyBean<ReverseProxy> implements ReqRespHandler {

	private static final int RETRY_AFTER_MS = 300;

	private static final int TIMEOUT_MS = 10000;

	private final List<ProxyMapping> mappings = U.list();

	@Override
	public Object execute(final Req req, final Resp resp) throws Exception {

		ProxyMapping mapping = findMapping(req);
		if (mapping == null) return null; // not found!

		req.async();

		process(req, resp, mapping, 1, U.time());

		return req;
	}

	protected void process(final Req req, final Resp resp, final ProxyMapping mapping, final int attempts, final long since) {
		final String targetUrl = mapping.getTargetUrl(req);

		Map<String, String> headers = req.headers();
		headers.remove("transfer-encoding");
		headers.remove("content-length");

		HttpClient client = getOrCreateClient();

		client.req()
			.verb(req.verb())
			.url(targetUrl)
			.headers(headers)
			.cookies(req.cookies())
			.body(req.body())
			.raw(true)
			.execute(new Callback<HttpResp>() {

				@Override
				public void onDone(HttpResp result, Throwable error) {
					if (error == null) {

						resp.code(result.code());
						resp.body(result.bodyBytes());

						// process the response headers
						SimpleHttpResp proxyResp = new SimpleHttpResp();
						HttpUtils.proxyResponseHeaders(result.headers(), proxyResp);

						if (proxyResp.contentType != null) resp.contentType(proxyResp.contentType);
						if (proxyResp.headers != null) resp.headers().putAll(proxyResp.headers);
						if (proxyResp.cookies != null) resp.cookies().putAll(proxyResp.cookies);

						resp.done();

					} else {
						handleError(error, req, resp, mapping, attempts, since);
					}
				}

			});
	}

	protected void handleError(Throwable error, final Req req, final Resp resp, final ProxyMapping mapping, final int attempts, final long since) {
		if (error instanceof ConnectException || error instanceof IOException) {

			if (HttpUtils.isGetReq(req) && (U.time() - since < TIMEOUT_MS)) {

				Jobs.after(RETRY_AFTER_MS, TimeUnit.MILLISECONDS).run(new Runnable() {
					@Override
					public void run() {
						process(req, resp, mapping, attempts + 1, since);
					}
				});

			} else {
				HttpIO.INSTANCE.errorAndDone(req, U.rte("Couldn't connect to the upstream!", error), LogLevel.DEBUG);
			}

		} else {

			HttpIO.INSTANCE.errorAndDone(req, error, LogLevel.ERROR);
		}
	}

	protected ProxyMapping findMapping(Req req) {
		for (ProxyMapping mapping : mappings) {
			if (mapping.matches(req)) {
				return mapping;
			}
		}

		return null;
	}

	@Override
	protected HttpClient createClient() {
		return HTTP.client()
			.reuseConnections(reuseConnections())
			.keepCookies(false)
			.maxConnTotal(maxConnTotal())
			.maxConnPerRoute(maxConnPerRoute());
	}

	public ReverseProxyMapDSL map(String uriPrefix) {
		return new ReverseProxyMapDSL(this, uriPrefix);
	}

	public List<ProxyMapping> mappings() {
		return mappings;
	}

	public void reset() {
		mappings.clear();
	}

}
