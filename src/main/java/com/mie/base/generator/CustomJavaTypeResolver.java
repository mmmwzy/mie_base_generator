package com.mie.base.generator;

import java.math.BigDecimal;
import java.sql.Types;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

public class CustomJavaTypeResolver extends JavaTypeResolverDefaultImpl {
	
	public CustomJavaTypeResolver() {
		super();
	}

	@Override
	public FullyQualifiedJavaType calculateJavaType(IntrospectedColumn introspectedColumn) {
		FullyQualifiedJavaType answer;
		JdbcTypeInformation jdbcTypeInformation = typeMap.get(introspectedColumn.getJdbcType());

		if (jdbcTypeInformation == null) {
			switch (introspectedColumn.getJdbcType()) {
			case Types.DECIMAL:
			case Types.NUMERIC:
				if (introspectedColumn.getScale() > 0 || introspectedColumn.getLength() > 18 || forceBigDecimals) {
					answer = new FullyQualifiedJavaType(BigDecimal.class.getName());
				} else if (introspectedColumn.getLength() > 9) {
					answer = new FullyQualifiedJavaType(Long.class.getName());

				} else {
					answer = new FullyQualifiedJavaType(Integer.class.getName());
				}
				break;

			default:
				answer = null;
				break;
			}
			
		} else {
			answer = jdbcTypeInformation.getFullyQualifiedJavaType();
		}
		
		if (Types.OTHER == introspectedColumn.getJdbcType()) {
			introspectedColumn.setJdbcType(Types.VARCHAR);
			answer = new FullyQualifiedJavaType(String.class.getName());
		}

		return answer;
	}

	@Override
	public String calculateJdbcTypeName(IntrospectedColumn introspectedColumn) {
		String answer;
		JdbcTypeInformation jdbcTypeInformation = typeMap.get(introspectedColumn.getJdbcType());

		if (jdbcTypeInformation == null) {
			switch (introspectedColumn.getJdbcType()) {
			case Types.DECIMAL:
				answer = "DECIMAL"; //$NON-NLS-1$
				break;
			case Types.NUMERIC:
				answer = "NUMERIC"; //$NON-NLS-1$
				break;
			default:
				answer = null;
				break;
			}
			
		} else {
			answer = jdbcTypeInformation.getJdbcTypeName();
		}
		
		if (Types.OTHER == introspectedColumn.getJdbcType()) {
			introspectedColumn.setJdbcType(Types.VARCHAR);
			answer = "VARCHAR";
		}

		return answer;
	}

}
