package com.mie.base.generator.plugins;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.sql.Types;
import java.util.List;
import java.util.Properties;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class PageMapperPlugin extends PluginAdapter {
	
	private boolean isExtendsIMapper = false;

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}
	
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		boolean isIdNotIntType = true;
		IntrospectedColumn colunmn = introspectedTable.getColumn("id");
		if (colunmn != null) {
			isIdNotIntType = colunmn.getJdbcType() != Types.INTEGER;
		}
		
		if (!isExtendsIMapper || isIdNotIntType) {
			this.methodSelectByExampleByPage(interfaze, topLevelClass, introspectedTable);
			return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
		}
		
		this.addIMapper(interfaze, topLevelClass, introspectedTable);
		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}
	
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		this.addPageElement(document.getRootElement(), introspectedTable);
		
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	public void addPageElement(XmlElement parentElement, IntrospectedTable introspectedTable){
		String fqjt = introspectedTable.getExampleType();

        XmlElement answer = new XmlElement("select"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", //$NON-NLS-1$
                introspectedTable.getSelectByExampleStatementId() + "ByPage"));
        answer.addAttribute(new Attribute(
                "resultMap", introspectedTable.getBaseResultMapId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterType", fqjt)); //$NON-NLS-1$

        context.getCommentGenerator().addComment(answer);

        answer.addElement(new TextElement("select")); //$NON-NLS-1$
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "distinct")); //$NON-NLS-1$ //$NON-NLS-2$
        ifElement.addElement(new TextElement("distinct")); //$NON-NLS-1$
        answer.addElement(ifElement);

        StringBuilder sb = new StringBuilder();
        if (stringHasValue(introspectedTable
                .getSelectByExampleQueryId())) {
            sb.append('\'');
            sb.append(introspectedTable.getSelectByExampleQueryId());
            sb.append("' as QUERYID,"); //$NON-NLS-1$
            answer.addElement(new TextElement(sb.toString()));
        }
        answer.addElement(getBaseColumnListElement(introspectedTable));

        sb.setLength(0);
        sb.append("from "); //$NON-NLS-1$
        sb.append(introspectedTable
                .getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement((new TextElement(sb.toString())));
        answer.addElement(getExampleIncludeElement(introspectedTable));

        ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "orderByClause != null")); //$NON-NLS-1$ //$NON-NLS-2$
        ifElement.addElement(new TextElement("order by ${orderByClause}")); //$NON-NLS-1$
        answer.addElement(ifElement);
        parentElement.addElement(answer);
	}
	
/*	public void addElements(XmlElement parentElement, IntrospectedTable introspectedTable) {
        String fqjt = introspectedTable.getExampleType();

        XmlElement answer = new XmlElement("select"); //$NON-NLS-1$
        answer
                .addAttribute(new Attribute(
                        "id", introspectedTable.getSelectByExampleWithBLOBsStatementId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute(
                "resultMap", introspectedTable.getResultMapWithBLOBsId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterType", fqjt)); //$NON-NLS-1$

        context.getCommentGenerator().addComment(answer);

        answer.addElement(new TextElement("select")); //$NON-NLS-1$
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "distinct")); //$NON-NLS-1$ //$NON-NLS-2$
        ifElement.addElement(new TextElement("distinct")); //$NON-NLS-1$
        answer.addElement(ifElement);

        StringBuilder sb = new StringBuilder();
        if (stringHasValue(introspectedTable
                .getSelectByExampleQueryId())) {
            sb.append('\'');
            sb.append(introspectedTable.getSelectByExampleQueryId());
            sb.append("' as QUERYID,"); //$NON-NLS-1$
            answer.addElement(new TextElement(sb.toString()));
        }

        answer.addElement(getBaseColumnListElement(introspectedTable));
        answer.addElement(new TextElement(",")); //$NON-NLS-1$
        answer.addElement(getBlobColumnListElement(introspectedTable));

        sb.setLength(0);
        sb.append("from "); //$NON-NLS-1$
        sb.append(introspectedTable
                .getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));
        answer.addElement(getExampleIncludeElement());

        ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "orderByClause != null")); //$NON-NLS-1$ //$NON-NLS-2$
        ifElement.addElement(new TextElement("order by ${orderByClause}")); //$NON-NLS-1$
        answer.addElement(ifElement);
        parentElement.addElement(answer);
    }*/
	
	protected XmlElement getBaseColumnListElement(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("include"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("refid", //$NON-NLS-1$
                introspectedTable.getBaseColumnListId()));
        return answer;
    }
	
	protected XmlElement getExampleIncludeElement(IntrospectedTable introspectedTable) {
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "_parameter != null")); //$NON-NLS-1$ //$NON-NLS-2$

        XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
        includeElement.addAttribute(new Attribute("refid", //$NON-NLS-1$
                introspectedTable.getExampleWhereClauseId()));
        ifElement.addElement(includeElement);

        return ifElement;
    }

	public void addIMapper(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable){
        /*		
        import com.mcoding.base.ui.bean.dictionary.DicGroup;
		import com.mcoding.base.ui.bean.dictionary.DicGroupExample;
		import com.mcoding.base.ui.persistence.common.IMapper;

		public interface DicGroupMapper extends IMapper<DicGroup, DicGroupExample> 
	    */
		
		String beanPackageStr = introspectedTable.getContext().getJavaModelGeneratorConfiguration().getTargetPackage();
		String className = introspectedTable.getTableConfiguration().getDomainObjectName();
		String exampleName = className + "Example";
		
		String fullClassNameStr = beanPackageStr + "." + className;
		String fullExampleNameStr = beanPackageStr + "." + exampleName;
		interfaze.addImportedType(new FullyQualifiedJavaType(fullClassNameStr));
		interfaze.addImportedType(new FullyQualifiedJavaType(fullExampleNameStr));
		
		String iMapperStr = "IMapper<"+ className +", "+ exampleName+">";
		FullyQualifiedJavaType fullIMapper = new FullyQualifiedJavaType(iMapperStr);
		
		interfaze.addImportedType(new FullyQualifiedJavaType("com.els.base.core.dao.IMapper"));
		interfaze.addSuperInterface(fullIMapper);
	}
	
	public void methodSelectByExampleByPage(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable){
		String modelClassName = introspectedTable.getTableConfiguration().getDomainObjectName();
		
		Method method = new Method("selectByExampleByPage");
		FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("List<"+modelClassName+">");
		method.setReturnType(returnType);
		
		Parameter parameter = new Parameter(this.getExampleType(introspectedTable, modelClassName), "example");
		method.addParameter(parameter);
		
		interfaze.addMethod(method);
	}
	
	@Override
	public void setProperties(Properties properties) {
		super.setProperties(properties);
		isExtendsIMapper = Boolean.valueOf(properties.getProperty("isExtendsIMapper")); //$NON-NLS-1$
	}

	@Override
	public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientInsertMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	@Override
	public boolean clientSelectAllMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return !this.isExtendsIMapper;
	}

	private FullyQualifiedJavaType getExampleType(IntrospectedTable introspectedTable, String modelClassName) {
		String beanPackageStr = introspectedTable.getContext().getJavaModelGeneratorConfiguration().getTargetPackage();
		String exampleName = modelClassName + "Example";

		String fullExampleNameStr = beanPackageStr + "." + exampleName;
		FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(fullExampleNameStr);
		return exampleType;
	}
}
