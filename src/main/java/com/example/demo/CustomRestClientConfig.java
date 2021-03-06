package com.example.demo;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CustomRestClientConfig {

	// Add http.client.ssl.trust-store=file:///... to application.properties
	@Value("${http.client.ssl.trust-store}")
	private Resource trustStore;

	@Value("${discover.proxy.host}")
	private String proxyHost;
	
	@Value("${discover.proxy.port}")
	private int proxyPort;

	@Bean
	public ClientHttpRequestFactory httpRequestFactory() {
		return new HttpComponentsClientHttpRequestFactory(httpClient());
	}

	@Bean
	public HttpClient httpClient() {

		SSLContext sslContext = null;
		SSLConnectionSocketFactory sslSocketFactory = null;

		// Trust own CA and all child certs
		try {
			sslContext = SSLContexts.custom().loadTrustMaterial(trustStore.getURL(), "changeit".toCharArray()).build();
			System.out.println("Set sslContext from URL -> " + trustStore.getURL().toString());

			// Since only our own certs are trusted, hostname verification can be bypassed
			sslSocketFactory = new SSLConnectionSocketFactory(sslContext, new javax.net.ssl.HostnameVerifier() {

				@Override
				public boolean verify(final String hostname, final SSLSession session) {
					return true;
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

		HttpHost proxy = new HttpHost(proxyHost, proxyPort);
		System.out.println("Set proxy -> " + proxy.toString());

		CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext)
				.setSSLSocketFactory(sslSocketFactory).setProxy(proxy).build();

		return httpClient;
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new CustomRestTemplate();
		//RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(httpRequestFactory());
		return restTemplate;
	}
	
}
