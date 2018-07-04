package com.mie.base.generator.plugins;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import com.mie.base.generator.utils.GenerateUtils;
import com.mie.base.generator.utils.ServiceGenerateDataStorage;
import com.mie.base.generator.utils.TableCommentStorage;

public class GenerateControllerPlugin extends PluginAdapter {

	private String targetPackage;
	private String targetProject;
	private String serviceTargetPackage;
	private String baseUrlPath;
	private String moduleName;

	@Override
	public boolean validate(List<String> warnings) {
		String targetPackage = this.properties.getProperty("targetPackage");
		if (StringUtils.isBlank(targetPackage)) {
			warnings.add("Controller 生成失败， targetPackage 配置失败");
			return false;
		}

		if (!targetPackage.matches("^(\\w+)(\\.\\w*)*\\w$")) {
			warnings.add("Controller 生成失败， targetPackage[" + targetPackage + "] 格式错误");
			return false;
		}

		String targetProject = this.properties.getProperty("targetProject");
		if (StringUtils.isBlank(targetProject)) {
			if (StringUtils.isBlank(targetPackage)) {
				warnings.add("Controller 生成失败， targetProject 配置失败");
				return false;
			}
		}

		String baseUrlPath = this.properties.getProperty("baseUrlPath");

		String moduleName = this.properties.getProperty("moduleName");
		if (StringUtils.isBlank(moduleName)) {
			moduleName = "";
		} else {
			moduleName = moduleName + "/";
		}

		this.targetPackage = targetPackage;
		this.targetProject = targetProject;
		this.baseUrlPath = baseUrlPath;
		this.moduleName = moduleName;
		return true;
	}

	@Override
	public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
		String modelName = introspectedTable.getTableConfiguration().getDomainObjectName();
		String tableName = introspectedTable.getTableConfiguration().getTableName();

		this.serviceTargetPackage = ServiceGenerateDataStorage.getInstance().getServicePackage(tableName);
		if (StringUtils.isBlank(serviceTargetPackage)) {
			throw new NullPointerException("controller 生成失败，因为关联的service未生成，请检查插件[GenerateServicePlugin]配置的顺序。");
		}

		GeneratedJavaFile controller = null;
		try {
			controller = new GeneratedJavaFile(this.createController(introspectedTable, modelName), this.targetProject,
					this.context.getJavaFormatter());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		List<GeneratedJavaFile> list = new ArrayList<>(2);
		list.add(controller);
		return list;
	}

	private CompilationUnit createController(IntrospectedTable introspectedTable, String modelClassName)
			throws SQLException {
		String controllerFullName = this.targetPackage + "." + modelClassName + "Controller";
		String tableComment = TableCommentStorage.getInstance().get(introspectedTable);
		if (StringUtils.isBlank(tableComment)) {
			tableComment = modelClassName;
		}

		FullyQualifiedJavaType controllerType = new FullyQualifiedJavaType(controllerFullName);
		TopLevelClass controller = new TopLevelClass(controllerType);
		controller.setVisibility(JavaVisibility.PUBLIC);

		controller.addAnnotation("@Api(value=\"" + tableComment + "\")");
		controller.addAnnotation("@Controller");

		if (StringUtils.isBlank(this.baseUrlPath)) {
			this.baseUrlPath = StringUtils.uncapitalize(modelClassName);
		}
		controller.addAnnotation("@RequestMapping(\"" + this.baseUrlPath + "\")");

		List<FullyQualifiedJavaType> importList = this.getImportList(introspectedTable, modelClassName);
		for (FullyQualifiedJavaType importItem : importList) {
			controller.addImportedType(importItem);
		}

		this.addFieldsAndMethod(controller, introspectedTable, modelClassName);

		return controller;
	}

	private void addFieldsAndMethod(TopLevelClass controller, IntrospectedTable introspectedTable,
			String modelClassName) throws SQLException {
		FullyQualifiedJavaType serviceType = this.getServiceType(introspectedTable, modelClassName);

		String serviceName = StringUtils.uncapitalize(modelClassName) + "Service";
		Field service = new Field(serviceName, serviceType);
		service.setVisibility(JavaVisibility.PROTECTED);

		service.addAnnotation("@Resource");
		controller.addField(service);

		// controller.addMethod(this.methodToAddView(modelClassName));
		// controller.addMethod(this.methodToMainView(modelClassName));
		// controller.addMethod(this.methodToUpdateViewById(modelClassName));

		controller.addMethod(this.methodCreate(introspectedTable, modelClassName));
		controller.addMethod(this.methodEdit(introspectedTable, modelClassName));
		controller.addMethod(this.methodDeleteById(introspectedTable, modelClassName));
		controller.addMethod(this.methodFindByPage(introspectedTable, modelClassName));
		// controller.addMethod(this.fin);

	}

	private Method methodToAddView(String modelClassName) {
		/*
		 * @ApiIgnore
		 * 
		 * @RequestMapping("service/toAddView") public ModelAndView toAddView()
		 * { ModelAndView view = new ModelAndView();
		 * view.setViewName("dictionary/dicGroup/toAddView"); return view; }
		 */

		Method method = new Method();
		method.setName("toAddView");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(new FullyQualifiedJavaType("ModelAndView"));

		String path = this.moduleName + StringUtils.uncapitalize(modelClassName);

		method.addBodyLine("return new ModelAndView(\"" + path + "/toAddView\");");

		method.addAnnotation("@ApiIgnore");
		method.addAnnotation("@RequestMapping(\"service/toAddView\")");
		return method;
	}

	private Method methodToMainView(String modelClassName) {
		/*
		 * @ApiIgnore
		 * 
		 * @RequestMapping("service/toListPageView") public ModelAndView
		 * toMainView() { ModelAndView view = new ModelAndView();
		 * view.setViewName("dictionary/dicGroup/toMainView"); return view; }
		 */

		Method method = new Method();
		method.setName("toMainView");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(new FullyQualifiedJavaType("ModelAndView"));

		String path = this.moduleName + StringUtils.uncapitalize(modelClassName);
		method.addBodyLine("return new ModelAndView(\"" + path + "/toMainView\");");

		method.addAnnotation("@ApiIgnore");
		method.addAnnotation("@RequestMapping(\"service/toMainView\")");
		return method;
	}

	public Method methodToUpdateViewById(String modelClassName) {
		/*
		 * @ApiIgnore
		 * 
		 * @RequestMapping("service/toUpdateViewById") public ModelAndView
		 * toDicGroupById(int id) { ModelAndView view = new ModelAndView();
		 * 
		 * DicGroup dicGroup = this.dicGroupService.queryObjById(id);
		 * 
		 * view.addObject("dicGroup", dicGroup);
		 * view.setViewName("dictionary/dicGroup/toAddView"); return view; }
		 */

		Method method = new Method();
		method.setName("toUpdateViewById");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(new FullyQualifiedJavaType("ModelAndView"));

		Parameter parameter = new Parameter(new FullyQualifiedJavaType("int"), "id");
		method.addParameter(parameter);

		String littleModel = StringUtils.uncapitalize(modelClassName);
		method.addBodyLine("ModelAndView view = new ModelAndView();");
		method.addBodyLine(modelClassName + " " + littleModel + " = this." + littleModel + "Service.queryObjById(id);");
		method.addBodyLine("view.addObject(\"" + littleModel + "\", " + littleModel + ");");

		String path = this.moduleName + littleModel;
		method.addBodyLine("view.setViewName(\"" + path + "/toAddView\");");
		method.addBodyLine("return view;");

		method.addAnnotation("@ApiIgnore");
		method.addAnnotation("@RequestMapping(method = RequestMethod.POST, value = \"register\",consumes =\"application/json\")");
		return method;
	}

	private Method methodCreate(IntrospectedTable introspectedTable, String modelClassName) throws SQLException {
		/*
		 * @ApiOperation(httpMethod="POST", value="创建字典组")
		 * 
		 * @RequestMapping("service/create")
		 * 
		 * @ResponseBody public ResponseResult<String> create(@RequestBody
		 * DicGroup dicGroup) {
		 * 
		 * this.dicGroupService.addObj(dicGroup); return
		 * ResponseResult.success(); }
		 */
		String littleModel = StringUtils.uncapitalize(modelClassName);

		Method method = new Method();
		method.setName("create");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(new FullyQualifiedJavaType("ResponseResult<String>"));

		Parameter parameter = new Parameter(this.getModelType(introspectedTable, modelClassName), littleModel);
		parameter.addAnnotation("@RequestBody");
		method.addParameter(parameter);

		method.addBodyLine("this." + littleModel + "Service.addObj(" + littleModel + ");");
		method.addBodyLine("return ResponseResult.success();");

		String tableComment = TableCommentStorage.getInstance().get(introspectedTable);
		if (StringUtils.isBlank(tableComment)) {
			tableComment = introspectedTable.getTableConfiguration().getDomainObjectName();
		}
		method.addAnnotation("@ApiOperation(httpMethod=\"POST\", value=\"创建" + tableComment + "\")");
		method.addAnnotation(
				"@RequestMapping(method = RequestMethod.POST, value = \"service/create\",consumes =\"application/json\")");
		method.addAnnotation("@ResponseBody");
		return method;
	}

	private Method methodEdit(IntrospectedTable introspectedTable, String modelClassName) throws SQLException {
		/*
		 * @ApiOperation(httpMethod="POST", value="编辑字典组")
		 * 
		 * @`("service/edit")
		 * 
		 * @ResponseBody public ResponseResult<String> edit(@RequestBody
		 * DicGroup dicGroup) { if (snsBanner.getId() == null ||
		 * snsBanner.getId() <=0) { throw new CommonException("id 为空，保存失败"); }
		 * 
		 * this.dicGroupService.modifyObj(dicGroup); return
		 * ResponseResult.success(); }
		 */

		String littleModel = StringUtils.uncapitalize(modelClassName);

		Method method = new Method();
		method.setName("edit");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(new FullyQualifiedJavaType("ResponseResult<String>"));

		Parameter parameter = new Parameter(this.getModelType(introspectedTable, modelClassName), littleModel);
		parameter.addAnnotation("@RequestBody");
		method.addParameter(parameter);
		if (GenerateUtils.getIdType(introspectedTable).equals("String")) {
			method.addBodyLine("if (StringUtils.isBlank(" + littleModel + ".getId())) {");
		} else {
			method.addBodyLine("if (" + littleModel + ".getId() == null || " + littleModel + ".getId() <=0) {");
		}
		method.addBodyLine("throw new CommonException(\"id 为空，保存失败\");");
		method.addBodyLine("}");

		method.addBodyLine("this." + littleModel + "Service.modifyObj(" + littleModel + ");");
		method.addBodyLine("return ResponseResult.success();");

		String tableComment = TableCommentStorage.getInstance().get(introspectedTable);
		if (StringUtils.isBlank(tableComment)) {
			tableComment = introspectedTable.getTableConfiguration().getDomainObjectName();
		}
		method.addAnnotation(
				"@RequestMapping(method = RequestMethod.POST, value = \"service/edit\",consumes =\"application/json\")");
		method.addAnnotation("@ResponseBody");
		return method;
	}

	private Method methodDeleteById(IntrospectedTable introspectedTable, String modelClassName) throws SQLException {
		/*
		 * @ApiOperation(httpMethod="POST", value="删除字典组")
		 * 
		 * @RequestMapping("service/deleteById")
		 * 
		 * @ResponseBody public ResponseResult<String> deleteById(int id){
		 * ResponseResult<String> result = new ResponseResult<>();
		 * this.dicGroupService.deleteObjById(id); result.setData(null);
		 * result.setMsg("ok"); result.setStatus("00"); return result; }
		 */

		String littleModel = StringUtils.uncapitalize(modelClassName);

		Method method = new Method();
		method.setName("deleteById");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(new FullyQualifiedJavaType("ResponseResult<String>"));

		String paramType = GenerateUtils.getIdType(introspectedTable).equals("String") ? "String" : "int";
		Parameter parameter = new Parameter(new FullyQualifiedJavaType(paramType), "id");
		parameter.addAnnotation("@RequestParam(required=true)");
		method.addParameter(parameter);

		method.addBodyLine("if (StringUtils.isBlank(id)) {");
		method.addBodyLine("throw new CommonException(\"删除失败,id不能为空\");");
		method.addBodyLine("}");
		method.addBodyLine("this." + littleModel + "Service.deleteObjById(id);");
		method.addBodyLine("return ResponseResult.success();");

		String tableComment = TableCommentStorage.getInstance().get(introspectedTable);
		if (StringUtils.isBlank(tableComment)) {
			tableComment = introspectedTable.getTableConfiguration().getDomainObjectName();
		}
		method.addAnnotation(
				"@RequestMapping(method = RequestMethod.GET, value = \"service/deleteById\")");
		method.addAnnotation("@ResponseBody");
		return method;

	}

	private Method methodFindByPage(IntrospectedTable introspectedTable, String modelClassName) throws SQLException {
		/*
		 * @ApiOperation(httpMethod="GET", value="查询部门信息")
		 * 
		 * @RequestMapping("service/findByPage")
		 * 
		 * @ResponseBody public ResponseResult<PageView<Department>> findByPage(
		 * 
		 * @ApiParam(value="所在页",defaultValue="0") @RequestParam(defaultValue=
		 * "0") int pageNo,
		 * 
		 * @ApiParam(value="每页数量",defaultValue="10") @RequestParam(defaultValue=
		 * "10") int pageSize) { DepartmentExample example = new
		 * DepartmentExample(); example.setPageView(new
		 * PageView<Department>(pageNo, pageSize));
		 * 
		 * if (CollectionUtils.isNotEmpty(params)) {
		 * OperationLogExample.Criteria criteria = example.createCriteria();
		 * CriteriaUtils.createCriteriaByQueryParams(criteria, params); }
		 * 
		 * PageView<Department> pageData =
		 * this.departmentService.queryObjByPage(example); return
		 * ResponseResult.success(pageData); }
		 */
		String littleModel = StringUtils.uncapitalize(modelClassName);

		Method method = new Method();
		method.setName("findByPage");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(new FullyQualifiedJavaType("ResponseResult<PageView<" + modelClassName + ">>"));

		Parameter p1 = new Parameter(new FullyQualifiedJavaType("int"), "pageNo");
		// p1.addAnnotation("@ApiParam(value=\"所在页\",defaultValue=\"0\")");
		p1.addAnnotation(" \n\t @RequestParam(defaultValue=\"0\")");

		method.addParameter(p1);

		Parameter p2 = new Parameter(new FullyQualifiedJavaType("int"), "pageSize");
		// p2.addAnnotation("@ApiParam(value=\"每页数量\",defaultValue=\"10\")");
		p2.addAnnotation(" \n\t @RequestParam(defaultValue=\"10\")");

		method.addParameter(p2);

		Parameter p3 = new Parameter(new FullyQualifiedJavaType("QueryParamWapper"), "wapper");
		// p3.addAnnotation("@ApiParam(value=\"查询条件,属性名请参考
		// "+modelClassName+"\")");
		p3.addAnnotation("\n\t @RequestBody(required=false)");
		method.addParameter(p3);

		method.addBodyLine(modelClassName + "Example example = new " + modelClassName + "Example();");
		method.addBodyLine("example.setPageView(new PageView<" + modelClassName + ">(pageNo, pageSize));");
		method.addBodyLine("");

		method.addBodyLine("if (wapper != null) {");
		method.addBodyLine(modelClassName + "Example.Criteria criteria = example.createCriteria();");
		method.addBodyLine("CriteriaUtils.addCriterion(criteria, wapper);");
		method.addBodyLine("}");

		method.addBodyLine("");
		method.addBodyLine(
				"PageView<" + modelClassName + "> pageData = this." + littleModel + "Service.queryObjByPage(example);");
		method.addBodyLine("return ResponseResult.success(pageData);");

		String tableComment = TableCommentStorage.getInstance().get(introspectedTable);
		if (StringUtils.isBlank(tableComment)) {
			tableComment = introspectedTable.getTableConfiguration().getDomainObjectName();
		}

		method.addAnnotation("@ApiOperation(httpMethod=\"POST\", value=\"查询" + tableComment + "\")");

		/*
		 * @ApiImplicitParams({
		 * 
		 * @ApiImplicitParam(name = "pageNo", required = false, value = "所在页",
		 * paramType = "query", dataType = "String", defaultValue = "0"),
		 * 
		 * @ApiImplicitParam(name = "pageSize", required = false, value =
		 * "每页数量", paramType = "query", dataType = "String", defaultValue =
		 * "10"),
		 * 
		 * @ApiImplicitParam(name = "wapper", required = false, value =
		 * "查询条件,属性名请参考 CompanyAddress", paramType = "body", dataType =
		 * "QueryParamWapper") })
		 */
		String pageNo = "@ApiImplicitParam( name = \"pageNo\",required = false,value = \"所在页\", paramType = \"query\", dataType = \"String\", defaultValue = \"0\" ), ";
		String pageSize = "@ApiImplicitParam( name = \"pageSize\", required = false, value = \"每页数量\", paramType = \"query\", dataType = \"String\", defaultValue = \"10\" ), ";
		String wapper = "@ApiImplicitParam( name = \"wapper\", required = false, value = \"查询条件,属性名请参考 "
				+ modelClassName + "\", paramType = \"body\", dataType = \"QueryParamWapper\" ) ";
		String annotation = "@ApiImplicitParams({ \n\t " + pageNo + " \n\t " + pageSize + " \n\t " + wapper + " \n}) ";

		method.addAnnotation(annotation);

		method.addAnnotation("@RequestMapping(method = RequestMethod.POST, value = \"service/findByPage\",consumes =\"application/json\")");
		method.addAnnotation("@ResponseBody");
		return method;
	}

	private List<FullyQualifiedJavaType> getImportList(IntrospectedTable introspectedTable, String modelClassName) {
		List<FullyQualifiedJavaType> list = new ArrayList<>();

		list.add(new FullyQualifiedJavaType("java.util.List"));
		list.add(new FullyQualifiedJavaType("javax.annotation.Resource"));
		list.add(new FullyQualifiedJavaType("org.apache.commons.lang.StringUtils"));
		list.add(new FullyQualifiedJavaType("org.apache.commons.collections.CollectionUtils"));

		list.add(new FullyQualifiedJavaType("org.springframework.stereotype.Controller"));
		list.add(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestBody"));
		list.add(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMapping"));
		list.add(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.ResponseBody"));
		list.add(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestParam"));
		list.add(new FullyQualifiedJavaType("org.springframework.web.servlet.ModelAndView"));
		list.add(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMethod"));

		list.add(new FullyQualifiedJavaType("com.mie.base.core.entity.ResponseResult"));
		list.add(new FullyQualifiedJavaType("com.mie.base.core.utils.ResponseCode"));
		list.add(new FullyQualifiedJavaType("com.mie.base.core.utils.CriteriaUtils"));
		list.add(new FullyQualifiedJavaType("com.mie.base.core.utils.query.QueryParamWapper"));
		list.add(new FullyQualifiedJavaType("com.mie.base.core.entity.PageView"));
		list.add(new FullyQualifiedJavaType("com.mie.base.core.exception.CommonException"));

		list.add(this.getModelType(introspectedTable, modelClassName));
		list.add(this.getExampleType(introspectedTable, modelClassName));
		list.add(this.getServiceType(introspectedTable, modelClassName));

		list.add(new FullyQualifiedJavaType("io.swagger.annotations.Api"));
		list.add(new FullyQualifiedJavaType("io.swagger.annotations.ApiOperation"));
		list.add(new FullyQualifiedJavaType("io.swagger.annotations.ApiParam"));
		list.add(new FullyQualifiedJavaType("springfox.documentation.annotations.ApiIgnore"));

		// io.swagger.annotations.ApiImplicitParam
		// io.swagger.annotations.ApiImplicitParams
		list.add(new FullyQualifiedJavaType("io.swagger.annotations.ApiImplicitParam"));
		list.add(new FullyQualifiedJavaType("io.swagger.annotations.ApiImplicitParams"));

		return list;
	}

	private FullyQualifiedJavaType getModelType(IntrospectedTable introspectedTable, String modelClassName) {

		String beanPackageStr = introspectedTable.getContext().getJavaModelGeneratorConfiguration().getTargetPackage();
		String fullModelClassName = beanPackageStr + "." + modelClassName;

		FullyQualifiedJavaType modelType = new FullyQualifiedJavaType(fullModelClassName);
		return modelType;
	}

	private FullyQualifiedJavaType getExampleType(IntrospectedTable introspectedTable, String modelClassName) {
		String beanPackageStr = introspectedTable.getContext().getJavaModelGeneratorConfiguration().getTargetPackage();
		String exampleName = modelClassName + "Example";

		String fullExampleNameStr = beanPackageStr + "." + exampleName;
		FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(fullExampleNameStr);
		return exampleType;
	}

	private FullyQualifiedJavaType getServiceType(IntrospectedTable introspectedTable, String modelClassName) {
		String serviceFullName = this.serviceTargetPackage + "." + modelClassName + "Service";
		FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType(serviceFullName);
		return mapperType;
	}

}
