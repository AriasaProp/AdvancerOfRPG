package com.ariasaproject.advancerofrpg.net;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.ariasaproject.advancerofrpg.utils.Pool;
import com.ariasaproject.advancerofrpg.utils.Pool.Poolable;

public abstract class Net {
	public static final String Method_HEAD = "HEAD";
	public static final String Method_GET = "GET";
	public static final String Method_POST = "POST";
	public static final String Method_PUT = "PUT";
	public static final String Method_PATCH = "PATCH";
	public static final String Method_DELETE = "DELETE";

	public static class HttpRequest implements Poolable {
		public HttpURLConnection connection;
		public HttpResponseListener listener;

		public void set(HttpURLConnection connection, HttpResponseListener listener) {
			this.connection = connection;
			this.listener = listener;
		}

		@Override
		public void reset() {
			connection = null;
			listener = null;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof HttpRequest))
				return false;
			HttpRequest r = (HttpRequest) obj;
			if (!r.connection.equals(connection))
				return false;
			if (r.listener != listener)
				return false;
			return true;
		}
	}

	public static class HttpRequestPool extends Pool<HttpRequest> {
		@Override
		protected HttpRequest newObject() {
			return new HttpRequest();
		}

		public HttpRequest gainRequest(HttpURLConnection connection, HttpResponseListener listener) {
			HttpRequest req = obtain();
			req.set(connection, listener);
			return req;
		}
	}

	public static interface HttpResponseListener {
		void handle(long time, HttpURLConnection connection);
	}

	final ExecutorService executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60000L, TimeUnit.MILLISECONDS,
			new SynchronousQueue<Runnable>(), new ThreadFactory() {
				final AtomicInteger threadID = new AtomicInteger();

				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r, "NetThread" + threadID.getAndIncrement());
					thread.setDaemon(true);
					return thread;
				}
			});
	final HttpRequestPool connectorPool = new HttpRequestPool();
	final Map<String, HttpRequest> connector = new HashMap<String, HttpRequest>();

	public synchronized void sendHttpRequest(final String sUrl, String methods,
			final HttpResponseListener responseListener, int timeout, final Map<String, String> headerReq) {
		if (sUrl == null) {
			responseListener.handle(-1, null);
			return;
		}
		try {
			final URL url = new URL(sUrl);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			// should be enabled to upload data.
			final boolean doingOutPut = methods == "POST" || methods == "PUT" || methods == "PATCH";
			connection.setDoOutput(doingOutPut);
			connection.setDoInput(true);
			connection.setRequestMethod(methods);
			// follow redirect default is true
			HttpURLConnection.setFollowRedirects(true);
			connector.put(sUrl, connectorPool.gainRequest(connection, responseListener));
			// Headers get set regardless of the method
			if (headerReq != null)
				for (final Map.Entry<String, String> header : headerReq.entrySet())
					connection.addRequestProperty(header.getKey(), header.getValue());
			// Set Timeouts
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						long start = System.currentTimeMillis();
						connection.connect();
						HttpResponseListener listener = connector.get(sUrl).listener;
						if (listener != null)
							listener.handle(System.currentTimeMillis() - start, connection);
						connector.remove(sUrl);
					} catch (final Exception e) {
						cancelHttpRequest(sUrl);
					} finally {
						connection.disconnect();
					}
				}
			});
		} catch (Exception e) {
			cancelHttpRequest(sUrl);
		}
	}

	public synchronized void cancelHttpRequest(String url) {
		HttpRequest req = connector.remove(url);
		if (req != null && req.listener != null) {
			req.listener.handle(-1, req.connection);

		}
	}

	public synchronized void destroy() {
		if (connector.size() == 0)
			return;
		for (final HttpRequest req : connector.values()) {
			if (req != null && req.listener != null) {
				req.listener.handle(-1, req.connection);
			}
		}
		connector.clear();
	}

	public abstract boolean openURI(String URI);
}
