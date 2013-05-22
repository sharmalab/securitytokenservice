package edu.emory.cci.bindaas.sts.util;

import java.io.File;

public class FileUtils {

	public static void deleteDirectory(File dir) {
		if (!dir.isDirectory())
			return;

		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.isDirectory())
				deleteDirectory(f);
			else
				f.delete();
		}

		dir.delete();
	}
}
