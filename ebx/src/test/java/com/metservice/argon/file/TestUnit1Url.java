package com.metservice.argon.file;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonApiException;

public class TestUnit1Url {

	private static void matchURL(String prefix, String suffix, URL actual) {
		final String qccActual = actual.toExternalForm();
		if (prefix.length() > 0 && !qccActual.startsWith(prefix)) {
			System.err.println("Expected prefix: " + prefix);
			System.err.println("Actual URL: " + qccActual);
		}
		if (suffix.length() > 0 && !qccActual.endsWith(suffix)) {
			System.err.println("Expected suffix: " + suffix);
			System.err.println("Actual URL: " + qccActual);
		}
		if (prefix.length() > 0) {
			Assert.assertTrue("URL prefix", qccActual.startsWith(prefix));
		}
		if (suffix.length() > 0) {
			Assert.assertTrue("URL suffix", qccActual.endsWith(suffix));
		}
	}

	@Test
	public void t10_userFileGood()
			throws MalformedURLException {
		try {
			final URL url = ArgonUrlManagement.newUrl("~/themes");
			matchURL("file:", "themes", url);
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t15_userHome() {
		try {
			final URL url = ArgonUrlManagement.newUrl(null);
			matchURL("file:", "", url);
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t20_absFileGood() {
		try {
			final URL url = ArgonUrlManagement.newUrl("/var/www/themes");
			matchURL("file:", "/var/www/themes", url);
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t30_httpGood() {
		try {
			final URL url = ArgonUrlManagement.newUrl("http://assets.com/themes");
			matchURL("http:", "assets.com/themes", url);
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t40_userSubFileGood()
			throws MalformedURLException {
		try {
			final URL parent = ArgonUrlManagement.newUrl("~/themes");
			final URL url1 = ArgonUrlManagement.newSubUrl(parent, "images", "logo");
			final URL url2 = ArgonUrlManagement.newSubUrl(parent, "images/", "/", "/logo");
			matchURL("file:", "themes/images/logo", url1);
			matchURL("file:", "themes/images/logo", url2);
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t80_userFileBad() {
		try {
			ArgonUrlManagement.newReadableUrl("~/badTest/bogus");
			Assert.fail("Should have rejected non-readble url");
		} catch (final ArgonApiException ex) {
			System.out.println("Good exception t80: " + ex.getMessage());
		}
	}

	@Test
	public void t85_illegalUrl() {
		try {
			ArgonUrlManagement.newUrl("bogus://badserv.com");
			Assert.fail("Should have rejected malformed url");
		} catch (final ArgonApiException ex) {
			System.out.println("Good exception t85: " + ex.getMessage());
		}
	}

}
