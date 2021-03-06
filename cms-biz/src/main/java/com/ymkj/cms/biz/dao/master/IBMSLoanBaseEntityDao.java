package com.ymkj.cms.biz.dao.master;

import java.util.List;
import java.util.Map;

import com.ymkj.base.core.biz.dao.BaseDao;
import com.ymkj.cms.biz.api.vo.response.master.ResBMSLoanBaseVO;
import com.ymkj.cms.biz.entity.master.BMSLoanBase;


public interface IBMSLoanBaseEntityDao extends BaseDao<BMSLoanBase>{

	 public BMSLoanBase findBYLoanNo(String loanNo);
	 /**
	  * 查询数量
	  * @param paramMap
	  * @return
	  * @author lix YM10160
	  * @date 2017年7月17日下午7:53:38
	  */
	public Integer findLoanBaseCount(Map<String, Object> paramMap);
	/**
	 * 为job提供的查询
	 * @param paramMap
	 * @return
	 * @author lix YM10160
	 * @date 2017年8月11日下午8:41:15
	 */
	public List<ResBMSLoanBaseVO> findGrantLoanUpdateByCoreJob(Map<String, Object> paramMap);
	/**
	 * 为job提供的查询
	 * @param paramMap
	 * @return
	 * @author lix YM10160
	 * @date 2017年8月11日下午8:41:15
	 */
	public List<ResBMSLoanBaseVO> findLoanBaseJob(Map<String, Object> paramMap);
	/**
	 * 自动绑定征信报告定时任务
	 * @param paramMap
	 * @return
	 * @author lix YM10160
	 * @date 2017年8月21日上午11:04:47
	 */
	public List<ResBMSLoanBaseVO> findBindCreditReportJob(Map<String, Object> paramMap);
	
	/**
	 * 数据条数
	 * @param paramMap
	 * @return
	 * @author lix YM10160
	 * @date 2017年9月11日上午10:44:04
	 */
	public Integer findLoanBaseJobCount(Map<String, Object> paramMap);
	
	/**
	 * 数据条数
	 * @param paramMap
	 * @return
	 * @author lix YM10160
	 * @date 2017年9月11日上午10:44:04
	 */
	public Integer findGrantLoanUpdateByCoreJobCount(Map<String, Object> paramMap);
	
	/**
	 * 数据条数
	 * @param paramMap
	 * @return
	 * @author lix YM10160
	 * @date 2017年9月11日上午10:44:04
	 */
	public Integer findBindCreditReportJobCount(Map<String, Object> paramMap);
}
