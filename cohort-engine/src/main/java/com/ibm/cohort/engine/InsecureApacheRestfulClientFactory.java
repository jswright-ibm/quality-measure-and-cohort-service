/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

/**
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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.client.apache.ApacheHttpClient;
import ca.uhn.fhir.rest.client.api.Header;
import ca.uhn.fhir.rest.client.api.IHttpClient;
import ca.uhn.fhir.rest.client.impl.RestfulClientFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A copy of ApacheRestfulClientFactory that ignores SSL CA and hostname verification.
 *
 * THIS SHOULD NOT BE USED IN PRODUCTION
 */
public class InsecureApacheRestfulClientFactory extends RestfulClientFactory {

	private HttpClient myHttpClient;
	private HttpHost myProxy;

	/**
	 * Constructor
	 *
	 * @param theContext
	 *            The context
	 */
	public InsecureApacheRestfulClientFactory(FhirContext theContext) {
		super(theContext);
	}

	@Override
	protected synchronized ApacheHttpClient getHttpClient(String theServerBase) {
		return new ApacheHttpClient(getNativeHttpClient(), new StringBuilder(theServerBase), null, null, null, null);
	}

	@Override
	public synchronized IHttpClient getHttpClient(StringBuilder theUrl, Map<String, List<String>> theIfNoneExistParams,
												  String theIfNoneExistString, RequestTypeEnum theRequestType, List<Header> theHeaders) {
		return new ApacheHttpClient(getNativeHttpClient(), theUrl, theIfNoneExistParams, theIfNoneExistString, theRequestType,
				theHeaders);
	}

	public HttpClient getNativeHttpClient() {
		if (myHttpClient == null) {

			PoolingHttpClientConnectionManager connectionManager = createInsecureHttpConnectionManager(5000, TimeUnit.MILLISECONDS);
			connectionManager.setMaxTotal(getPoolMaxTotal());
			connectionManager.setDefaultMaxPerRoute(getPoolMaxPerRoute());

			//TODO: Use of a deprecated method should be resolved.
			RequestConfig defaultRequestConfig =
					RequestConfig.custom()
							.setSocketTimeout(getSocketTimeout())
							.setConnectTimeout(getConnectTimeout())
							.setConnectionRequestTimeout(getConnectionRequestTimeout())
							.setStaleConnectionCheckEnabled(true)
							.setProxy(myProxy)
							.build();

			HttpClientBuilder builder = HttpClients.custom().setConnectionManager(connectionManager)
					.setDefaultRequestConfig(defaultRequestConfig).disableCookieManagement();

			if (myProxy != null && StringUtils.isNotBlank(getProxyUsername()) && StringUtils.isNotBlank(getProxyPassword())) {
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(new AuthScope(myProxy.getHostName(), myProxy.getPort()),
						new UsernamePasswordCredentials(getProxyUsername(), getProxyPassword()));
				builder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
				builder.setDefaultCredentialsProvider(credsProvider);
			}

			myHttpClient = builder.build();

		}

		return myHttpClient;
	}

	@Override
	protected void resetHttpClient() {
		this.myHttpClient = null;
	}

	/**
	 * Only allows to set an instance of type org.apache.http.client.HttpClient
	 * @see ca.uhn.fhir.rest.client.api.IRestfulClientFactory#setHttpClient(Object)
	 */
	@Override
	public synchronized void setHttpClient(Object theHttpClient) {
		this.myHttpClient = (HttpClient) theHttpClient;
	}

	@Override
	public void setProxy(String theHost, Integer thePort) {
		if (theHost != null) {
			myProxy = new HttpHost(theHost, thePort, "http");
		} else {
			myProxy = null;
		}
	}

	private PoolingHttpClientConnectionManager createInsecureHttpConnectionManager(int timeToLive, TimeUnit timeUnit) {
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException { }
			@Override
			public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException { }
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, new TrustManager[] { trustManager }, new SecureRandom());
		}
		catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new RuntimeException("Error creating SSLContext", e);
		}

		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("https", new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.build();

		return new PoolingHttpClientConnectionManager(
				socketFactoryRegistry,
				null,
				null,
				null,
				timeToLive,
				timeUnit
		);
	}

}
