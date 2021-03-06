package com.ymkj.cms.biz.service.sign.base;

import com.bstek.uflo.model.task.Task;
import com.bstek.uflo.service.TaskOpinion;
import com.ymkj.base.core.biz.api.message.Response;
import com.ymkj.base.core.biz.common.Validate;
import com.ymkj.base.core.common.excption.CoreErrorCode;
import com.ymkj.base.core.common.http.HttpResponse;
import com.ymkj.cms.biz.api.enums.EnumConstants;
import com.ymkj.cms.biz.api.vo.request.sign.ReqLoanContractSignVo;
import com.ymkj.cms.biz.api.vo.response.sign.ResLoanContractSignVo;
import com.ymkj.cms.biz.common.sign.BaseSign;
import com.ymkj.cms.biz.common.util.JsonUtils;
import com.ymkj.cms.biz.common.util.StringUtils;
import com.ymkj.cms.biz.dao.finance.IBMSLoanBaseDao;
import com.ymkj.cms.biz.entity.finance.BMSLoanBase;
import com.ymkj.cms.biz.entity.master.BMSSysLoanLog;
import com.ymkj.cms.biz.entity.sign.BMSLoanBaseEntity;
import com.ymkj.cms.biz.entity.sign.HttpContractReturnEntity;
import com.ymkj.cms.biz.exception.BizErrorCode;
import com.ymkj.cms.biz.exception.BizException;
import com.ymkj.cms.biz.service.http.ICoreHttpService;
import com.ymkj.cms.biz.service.master.IBMSSysLoanLogService;
import com.ymkj.cms.biz.service.master.IBMSSysLogService;
import com.ymkj.cms.biz.service.process.ITaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 放款审核  通过
 * 
 * @author YM10138
 *
 */
@Service
public class PassAuditLoanImpl extends BaseSign<ResLoanContractSignVo> {

	@Autowired
	private IBMSSysLogService ibmsSysLogService;
	@Autowired
	private ICoreHttpService coreHttpService;
	@Autowired
	private ITaskService taskService;
	@Autowired
	private IBMSSysLoanLogService ibmsSysLoanLogService;
	@Autowired
	private IBMSLoanBaseDao loanBaseDao;

	@Override
	public boolean before(ReqLoanContractSignVo reqLoanContractSignVo, Response<ResLoanContractSignVo> res) {
		// 参数校验
		@SuppressWarnings("unchecked")
		Response<Object> validateResponse = Validate.getInstance().validate(reqLoanContractSignVo);
		if (!validateResponse.isSuccess()) {
			setError(new BizException(CoreErrorCode.REQUEST_PARAM_ISBLANK, validateResponse.getRepMsg()), res);
			return false;
		}
		if(reqLoanContractSignVo.getId() == null){
			setError(new BizException(CoreErrorCode.REQUEST_PARAM_ISNULL, new Object[]{"id"}), res);
			return false;
		}
		if(StringUtils.isBlank(reqLoanContractSignVo.getLoanNo())){
			setError(new BizException(CoreErrorCode.REQUEST_PARAM_ISNULL, new Object[]{"loanNo"}), res);
			return false;
		}
		//流程校验
		Task task=	taskService.findTaskByLoanBaseId(String.valueOf(reqLoanContractSignVo.getId()));
		if(task == null || !EnumConstants.WF_FKSH.equals(task.getTaskName())){
			setError(new BizException(BizErrorCode.UFLOTASK_EOERR), res);
			return false;
		}

		return true;
	}

	@Override
	public boolean execute(ReqLoanContractSignVo reqLoanContractSignVo, Response<ResLoanContractSignVo> res) {
		try {
			//更新借款系统借款状态
			//构造 loanBaseId 和 借款状态RtfNodeState
			BMSLoanBase  loanBaseEntity = new BMSLoanBase(reqLoanContractSignVo.getId(),EnumConstants.RtfState.FKQR.getValue(),
					EnumConstants.RtfNodeState.FKSHSUBMIT.getValue());
			loanBaseEntity.setFinanceAuditTime(new Date());
			boolean updatFlag=	updateBMSLoanState(loanBaseEntity, res);
			if(updatFlag){
				//调用接口更新合同
				reqLoanContractSignVo.setState(EnumConstants.FINANCE_AUDIT_PASS);
				boolean flag = updateCoreLoanState(reqLoanContractSignVo, res);
				if(!flag){
					throw new BizException(BizErrorCode.FACADE_RESPONSE_FAIL, "接口调用返回失败消息");
				}			
			}else{
				throw new BizException(BizErrorCode.DB_UPDATE_RESULT_0, "更新失败，请刷新页面");
			}	
			//借款日志
			BMSSysLoanLog loanLog= new BMSSysLoanLog();
			loanLog.setLoanBaseId(reqLoanContractSignVo.getId());
			loanLog.setOperationModule(EnumConstants.OptionModule.FINANCE_AUDIT.getValue());
			loanLog.setLoanNo(reqLoanContractSignVo.getLoanNo());
			loanLog.setOperationTime(new Date());
			loanLog.setOperator(reqLoanContractSignVo.getServiceName());
			loanLog.setOperatorCode(reqLoanContractSignVo.getServiceCode());
			loanLog.setStatus(EnumConstants.LoanStatus.PASS.getValue());
			loanLog.setRtfState(EnumConstants.RtfState.FKSH.getValue());
			loanLog.setRtfNodeState(EnumConstants.RtfNodeState.FKSHSUBMIT.getValue());
			loanLog.setOperationType(EnumConstants.OptionType.PASS.getValue());
			ibmsSysLoanLogService.saveOrUpdate(loanLog);
			//系统日志
			ibmsSysLogService.recordSysLog(reqLoanContractSignVo, "财务管理|财务审核|通过|update","PassAuditLoanImpl","放款审核通过");
			

		} catch (BizException e) {
			throw new BizException(CoreErrorCode.UNKNOWN_ERROR, e.getMessage() + e.getArguments()[0]);
		}
		return true;
	}

	@Override
	public boolean after(ReqLoanContractSignVo reqLoanContractSignVo, Response<ResLoanContractSignVo> res) {
		//完成竞争任务
		TaskOpinion option =new TaskOpinion("通过");
		taskService.compUsersTaskByLoanBaseId(reqLoanContractSignVo.getId(), reqLoanContractSignVo.getServiceCode(), EnumConstants.WFPH_TG,option);
		return true;
	}

	//更新核心系统借款状态
	private boolean  updateCoreLoanState(ReqLoanContractSignVo reqLoanContractSignVo, Response<ResLoanContractSignVo> res){
		Map<String,Object> loanDataMap  = new HashMap<String,Object>();
		
		//构造 LoanNo 和 借款状态useCode 和 state
		BMSLoanBaseEntity bmsLoanBaseEntity = new BMSLoanBaseEntity(reqLoanContractSignVo.getLoanNo(),
				reqLoanContractSignVo.getServiceCode(),reqLoanContractSignVo.getState());
		
		loanDataMap.put("loanNo", bmsLoanBaseEntity.getLoanNo());
		loanDataMap.put("userCode",bmsLoanBaseEntity.getUserCode());
		loanDataMap.put("stateCode",bmsLoanBaseEntity.getState());
		//调用核心更新借款状态
		HttpResponse httpResponse = coreHttpService.updateCoreLoanState(loanDataMap);
		
		HttpContractReturnEntity contractReturnEntity= JsonUtils.decode(httpResponse.getContent(), HttpContractReturnEntity.class);
		if(!"000000".equals(contractReturnEntity.getCode())){//是否请求成功
			setError(new BizException(BizErrorCode.FACADE_RESPONSE_FAIL, "接口调用返回失败消息"), res);
			return false;
		}else{
			return true;
		}
		
	}
	//更新借款系统借款状态
	private boolean  updateBMSLoanState(BMSLoanBase loanBaseEntity, Response<ResLoanContractSignVo> res){
		long count=  loanBaseDao.update(loanBaseEntity);
		if(count !=1){
			setError(new BizException(BizErrorCode.DB_UPDATE_RESULT_0, "更新失败，请刷新页面"), res);
			return false;
		}
		return true;
	}
}
