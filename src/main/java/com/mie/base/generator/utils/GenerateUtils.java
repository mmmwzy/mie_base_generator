package com.mie.base.generator.utils;

import java.sql.Types;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;

public class GenerateUtils {
	
	public static String getIdType(IntrospectedTable introspectedTable){
		IntrospectedColumn colum = introspectedTable.getColumn("id");
		if(colum != null && Types.VARCHAR == colum.getJdbcType()){
			return "String";
		}else{
			return "Integer";
		}
	}

}
