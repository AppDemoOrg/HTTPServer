package com.abt.http.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

	/**
	 * 返回指定路径下的所有文件的全路径
	 * @param isName 是列出文件夹的名字，还是文件夹的路径
	 */
	public static List<String> getFilePaths(String dirPath, boolean isName) {
		List <String> filePaths = new ArrayList<String>();
        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!dirPath.endsWith(File.separator)) {
        	dirPath = dirPath + File.separator;
        }
        File dirFile = new File(dirPath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return null;
        }
        // 删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (File file : files) {
        	String path = null;
        	if (isName) {
        		path = file.getName();
        	} else {
        		path = file.getAbsolutePath();
        	}
        	filePaths.add(path);
        }
		return filePaths;
	}
}
