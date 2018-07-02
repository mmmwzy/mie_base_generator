package com.mie.base.generator.main;

import org.mybatis.generator.api.ShellRunner;

public class MbgPluginsMain {

	public static void main(String[] args) {
		/**
		 * 填写 配置文件的路径
		 */
//		args = new String[] { "-configfile", "/Users/hzy/work/code/mcoding/tools/generator/generator.xml", "-overwrite" };
		//args = new String[] { "-configfile", "/Users/hzy/work/code/qqt/base_parent/base_generator/src/main/resources/generator.xml", "-overwrite" };
		
		//可以使用相对路径
		args = new String[] { "-configfile", "src/main/resources/generator.xml", "-overwrite" };
		ShellRunner.main(args);
	}

}
