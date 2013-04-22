package com.metservice.xenon;

import org.junit.Assert;
import org.junit.Test;

public class TestUnit1Canvas {

	@Test
	public void t10() {
		try {
			final XenonCanvasFactoryCfg.Builder cfb = XenonCanvasFactoryCfg.newBuilder();
			final XenonCanvasFactoryCfg cfc = XenonCanvasFactoryCfg.newInstance(cfb);
			final XenonCanvasFactory cf = XenonCanvasFactory.newInstance(cfc);
			System.out.println(cf);
		} catch (final XenonCfgSyntaxException ex) {
			Assert.fail(ex.getMessage());
		} catch (final XenonPlatformException ex) {
			Assert.fail(ex.getMessage());
		}

	}

}
