package com.metservice.argon.file;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonPermissionException;

public class TestUnit1Directory {

	@Test
	public void t10_userDir() {
		try {
			final String expected = "/a/b1/b2/c1/c2/d1/d2";
			final File cndirUser = ArgonDirectoryManagement
					.cndirUser("a", "/", "/b1/b2", "\\", "c1\\c2\\", "\\/", "\\d1/d2/");
			final String qtwPath = cndirUser.getPath().replace('\\', '/');
			if (!qtwPath.endsWith(expected)) {
				System.err.println("Expected tail: " + expected);
				System.err.println("Actual path: " + qtwPath);
				Assert.fail("Expected tail '" + expected + "' but path '" + qtwPath + "'");
			}
			Assert.assertTrue("Tail match", qtwPath.endsWith(expected));
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		}
	}

}
