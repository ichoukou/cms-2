<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head.jsp"%>
<script type="text/javascript"
	src="${ctx}/resources/js/master/bank/bankAllList.js"></script>
<script type="text/javascript"
	src="${ctx}/resources/js/common.js"></script>
<div id="queryBankDiv" class="easyui-panel W80"
	data-options="collapsible:true">
	<form id="manualDispatch_queryForm" class="margin_20">
		<table style="border-collapse: separate; border-spacing: 15px;">
			<tr align="left">
				<th>银行名称:</th>
				<td><input type="text" class="easyui-textbox input" name="name" ></td>
				<td><a class="easyui-linkbutton" iconCls="icon-search" onclick="queryBMSBankInfo();">
					<span style="font-size: 12px">搜&nbsp;索</span>
				</a> </td>
			</tr>
			<!-- <tr>
				<td colspan="3">
					<div style="text-align: center; padding: 5px">
						 <a class="easyui-linkbutton c6" onclick="queryBMSBankInfo();"><i class="fa fa-search" aria-hidden="true"></i>搜&nbsp;索</a>
					</div>
				</td>
			</tr> -->
		</table>
	</form>
</div>
<div id="bankPage" title="银行查询" style="height: 92px; padding-top: 8px;">
	<table id="new_datagrid_bank" toolbar="#toolbar"></table>
</div>



