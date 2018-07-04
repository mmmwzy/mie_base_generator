package com.mie.base.generator.plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

public class PageExamplePlugin extends PluginAdapter {
	
	private FullyQualifiedJavaType serializable;

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}
	
	public PageExamplePlugin(){
		super();  
        serializable = new FullyQualifiedJavaType("java.io.Serializable");
	}

	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		this.addSuperClass(topLevelClass, introspectedTable);
		this.addPageView(topLevelClass, introspectedTable);
		this.makeSerializable(topLevelClass, introspectedTable); 
		
		for (InnerClass innerClass : topLevelClass.getInnerClasses()) {  
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) { 
                innerClass.addSuperInterface(serializable);  
            }  
            if ("Criteria".equals(innerClass.getType().getShortName())) {
                innerClass.addSuperInterface(serializable);  
            }  
            if ("Criterion".equals(innerClass.getType().getShortName())) {  
                innerClass.addSuperInterface(serializable);  
            }  
            
            Field field = new Field();  
            field.setFinal(true);  
            field.setInitializationString("1L"); 
            field.setName("serialVersionUID");  
            field.setStatic(true);  
            field.setType(new FullyQualifiedJavaType("long")); 
            field.setVisibility(JavaVisibility.PRIVATE);  
            context.getCommentGenerator().addFieldComment(field, introspectedTable);  
  
            innerClass.addField(field);  
		}
//		this.addIExampleInterface(topLevelClass, introspectedTable);

		return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
//		this.addToJsonMethod(topLevelClass, introspectedTable);
	}
	
	private void addToJsonMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		/*
		@Override
		public String toJson() throws JsonProcessingException {
			return JsonUtils.writeValueAsString(this);
		}
		*/
		
		Method toJsonMethod = new Method();
		toJsonMethod.setVisibility(JavaVisibility.PUBLIC);
		toJsonMethod.setName("toJson");
		toJsonMethod.setReturnType(new FullyQualifiedJavaType("String"));
		toJsonMethod.addException(new FullyQualifiedJavaType("JsonProcessingException"));
		toJsonMethod.addBodyLine("return JsonUtils.writeValueAsString(this);");
		
		topLevelClass.addMethod(toJsonMethod);
		topLevelClass.addImportedType("com.fasterxml.jackson.core.JsonProcessingException");
		topLevelClass.addImportedType("com.mie.base.utils.json.JsonUtils");
		
	}

	private void addPageView(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        /*	    
        protected PageView<DicGroup> pageView;
        
        @Override
		public PageView<DicGroup> getPageView() {
			return pageView;
		}

		@Override
		public void setPageView(PageView<DicGroup> pageView) {
			this.pageView = pageView;
		}
        */		
		String className = introspectedTable.getTableConfiguration().getDomainObjectName();
		String pageViewTypeStr = "PageView<"+ className +">";
		
		FullyQualifiedJavaType pageViewType = new FullyQualifiedJavaType(pageViewTypeStr);
		Field pageViewField = new Field("pageView", pageViewType);
		pageViewField.setVisibility(JavaVisibility.PROTECTED);
		pageViewField.setInitializationString("new PageView<"+ className +">(1, 10)");
		
		Method getPageViewMethod = new Method();
		getPageViewMethod.setVisibility(JavaVisibility.PUBLIC);
		getPageViewMethod.setName("getPageView");
		getPageViewMethod.setReturnType(pageViewType);
		getPageViewMethod.addAnnotation("@Override");
		getPageViewMethod.addBodyLine("return pageView;");
		
		Method setPageViewMethod = new Method();
		setPageViewMethod.setVisibility(JavaVisibility.PUBLIC);
		setPageViewMethod.setName("setPageView");
		setPageViewMethod.addParameter(new Parameter(pageViewType, "pageView"));
		setPageViewMethod.addAnnotation("@Override");
		setPageViewMethod.addBodyLine("this.pageView = pageView;");
		
		topLevelClass.addField(pageViewField);
		topLevelClass.addMethod(getPageViewMethod);
		topLevelClass.addMethod(setPageViewMethod);
		topLevelClass.addImportedType("com.mie.base.core.entity.PageView");
	}

	private void addIExampleInterface(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		String className = introspectedTable.getTableConfiguration().getDomainObjectName();
		String interfaceOrExample = "IExample<" + className + ">";

		topLevelClass.addSuperInterface(new FullyQualifiedJavaType(interfaceOrExample));
		topLevelClass.addImportedType("com.mie.base.core.entity.IExample");
	}

	private void addSuperClass(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		String className = introspectedTable.getTableConfiguration().getDomainObjectName();
		String superExample = "AbstractExample<" + className + ">";
		
		topLevelClass.setSuperClass(new FullyQualifiedJavaType(superExample));
		topLevelClass.addImportedType("com.mie.base.core.entity.AbstractExample");
	}
	
	/** 
     * 添加给Example类序列化的方法 
     * @param topLevelClass 
     * @param introspectedTable 
     * @return 
     */  
  
    protected void makeSerializable(TopLevelClass topLevelClass,  
                                    IntrospectedTable introspectedTable) {  
  
            topLevelClass.addImportedType(serializable);  
            topLevelClass.addSuperInterface(serializable);  
  
            Field field = new Field();  
            field.setFinal(true);  
            field.setInitializationString("1L"); 
            field.setName("serialVersionUID");  
            field.setStatic(true);  
            field.setType(new FullyQualifiedJavaType("long")); 
            field.setVisibility(JavaVisibility.PRIVATE);  
            context.getCommentGenerator().addFieldComment(field, introspectedTable);  
  
            topLevelClass.addField(field);  
        }  
    }
