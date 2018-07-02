package com.mie.base.generator.plugins;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.internal.db.ConnectionFactory;

import com.mie.base.generator.utils.TableCommentStorage;

public class ApiModelPlugin extends PluginAdapter {

	private Map<String, String> storage = new Hashtable<>();

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		String tableComment = null;
		String tableName = introspectedTable.getTableConfiguration().getTableName();

		if (tableName.length() > 30) {
			System.out.println("【警告】table [" + tableName + "] ,名字长度大于30，在Oracle中，将不兼容");
		}

		try {
			tableComment = TableCommentStorage.getInstance().get(introspectedTable);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		topLevelClass.addImportedType(new FullyQualifiedJavaType("io.swagger.annotations.ApiModel"));
		if (StringUtils.isNotBlank(tableComment)) {
			topLevelClass.addAnnotation("@ApiModel(value=\"" + tableComment + "\")");
		} else {
			topLevelClass.addAnnotation("@ApiModel(value=\"" + tableName + "\")");
		}

		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}

	@Override
	public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
			IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		String databaseProductName = null;
		Connection connection = null;
		String remark = null;
		
		try {
			JDBCConnectionConfiguration configuration = introspectedColumn.getContext()
					.getJdbcConnectionConfiguration();
			connection = ConnectionFactory.getInstance().getConnection(configuration);
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			databaseProductName = databaseMetaData.getDatabaseProductName().toLowerCase();

			if (StringUtils.isNotBlank(databaseProductName) && databaseProductName.contains("oracle")) {
				String tableName = introspectedTable.getTableConfiguration().getTableName();
				String columName = introspectedColumn.getActualColumnName();
				remark = this.getRemarkForOracle(connection, tableName, columName);
			}
			
			
			if (StringUtils.isNotBlank(introspectedColumn.getRemarks())) {
				try {
					remark = new String(introspectedColumn.getRemarks().getBytes(), "UTF-8");
					
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			System.out.println("查询数据库类型失败");
			e1.printStackTrace();
		}



		if (StringUtils.isNotBlank(remark)) {
			topLevelClass.addImportedType(new FullyQualifiedJavaType("io.swagger.annotations.ApiModelProperty"));
			field.addAnnotation("@ApiModelProperty(\"" + remark + "\")");
		}
		return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
	}

	private String getRemarkForOracle(Connection connection, String tableName, String columName) throws SQLException {
		String remark = null;

		String key = tableName + "-" + columName;
		remark = storage.get(key);
		if (StringUtils.isNotBlank(remark)) {
			return remark;
		}
		
		Statement statement = connection.createStatement();
		String querySql = "select * from user_col_comments WHERE TABLE_NAME = '"+tableName+"'";
		try {
			ResultSet resultSet = statement.executeQuery(querySql);

			while (resultSet.next()) {
				if (StringUtils.isBlank(resultSet.getString(3))) {
					continue;
				}
				storage.put(tableName + "-" + resultSet.getString(2), resultSet.getString(3));
			}
		} catch (Exception e) {
			System.out.println("查询 table[" + tableName + "]的注解失败，原因是:" + e.getMessage());
		}

		
		remark = storage.get(key);
		if (StringUtils.isNotBlank(remark)) {
			return remark;
		}
		
		return null;
	}

}
