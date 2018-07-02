package com.mie.base.generator.plugins;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.internal.db.ConnectionFactory;

import com.mie.base.generator.InsertBatchElementCreater;

public class InsertBatchPlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	/**
	 * 在接口中添加方法
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		try {
			JDBCConnectionConfiguration configuration = introspectedTable.getContext().getJdbcConnectionConfiguration();
			Connection connection = ConnectionFactory.getInstance().getConnection(configuration);
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			String databaseProductName = databaseMetaData.getDatabaseProductName().toLowerCase();
			if (!databaseProductName.contains("mysql")) {
				System.out.println("批量插入只支持mysql");
				return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
		}


		String objectName = introspectedTable.getTableConfiguration().getDomainObjectName();// 对象名称
		interfaze.addImportedType(new FullyQualifiedJavaType("java.util.List"));

		Method method = new Method();//
		method.setName("insertBatch");
		method.addParameter(new Parameter(new FullyQualifiedJavaType("java.util.List<" + objectName + ">"), "list"));
		method.setReturnType(new FullyQualifiedJavaType("void"));
		interfaze.addMethod(method);

		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}

	/**
	 * 在xml文件中添加需要的元素
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		try {
			JDBCConnectionConfiguration configuration = introspectedTable.getContext().getJdbcConnectionConfiguration();
			Connection connection = ConnectionFactory.getInstance().getConnection(configuration);
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			String databaseProductName = databaseMetaData.getDatabaseProductName().toLowerCase();
			if (!databaseProductName.contains("mysql")) {
				System.out.println("批量插入只支持mysql");
				return super.sqlMapDocumentGenerated(document, introspectedTable);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return super.sqlMapDocumentGenerated(document, introspectedTable);
		}
		
		
		XmlElement parentElement = document.getRootElement();
		String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
		InsertBatchElementCreater insertBatchElementCreater = new InsertBatchElementCreater(introspectedTable,
				tableName);
		parentElement.addElement(insertBatchElementCreater.createElement());
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

}
