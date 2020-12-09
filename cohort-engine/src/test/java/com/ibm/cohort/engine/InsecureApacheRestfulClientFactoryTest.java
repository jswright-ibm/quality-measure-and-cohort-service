/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.client.api.IHttpClient;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class InsecureApacheRestfulClientFactoryTest {

	private final String urlSuffix = "/url";
	private final FhirContext context = FhirContext.forR4();

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(
			WireMockConfiguration.wireMockConfig()
					.dynamicPort()
					.dynamicHttpsPort()
					.enableBrowserProxying(true)
	);

	@Before
	public void setup() {
		wireMockRule.stubFor(WireMock.get(urlSuffix));
	}

	@Test
	public void requestWithoutProxy() throws Exception {
		InsecureApacheRestfulClientFactory factory = new InsecureApacheRestfulClientFactory(context);
		factory.setProxy(null, null);

		IHttpClient client = factory.getHttpClient(
				new StringBuilder(getHttpsURL()),
				null,
				null,
				RequestTypeEnum.GET,
				null
		);
		IHttpRequest request = client.createGetRequest(context, EncodingEnum.JSON);
		IHttpResponse response = request.execute();
		Assert.assertEquals(200, response.getStatus());
	}

	@Test
	public void requestWithProxy() throws Exception {
		InsecureApacheRestfulClientFactory factory = new InsecureApacheRestfulClientFactory(context);
		factory.setProxy("localhost", wireMockRule.port());
		factory.setProxyCredentials("user", "password");

		IHttpClient client = factory.getHttpClient(
				new StringBuilder(getHttpsURL()),
				null,
				null,
				RequestTypeEnum.GET,
				null
		);
		IHttpRequest request = client.createGetRequest(context, EncodingEnum.JSON);
		IHttpResponse response = request.execute();
		Assert.assertEquals(200, response.getStatus());
	}

	@Test
	public void setClient() {
		HttpClient expected = HttpClientBuilder.create().build();
		InsecureApacheRestfulClientFactory factory = new InsecureApacheRestfulClientFactory(context);
		factory.setHttpClient(expected);

		Object actual = factory.getNativeHttpClient();
		Assert.assertSame(expected, actual);
	}

	@Test
	public void resetClient() {
		HttpClient unexpected = HttpClientBuilder.create().build();
		InsecureApacheRestfulClientFactory factory = new InsecureApacheRestfulClientFactory(context);
		factory.setHttpClient(unexpected);
		factory.resetHttpClient();
		Object actual = factory.getNativeHttpClient();
		Assert.assertNotSame(unexpected, actual);
	}

	private String getHttpsURL() {
		return "https://localhost:" + wireMockRule.httpsPort() + urlSuffix;
	}
}