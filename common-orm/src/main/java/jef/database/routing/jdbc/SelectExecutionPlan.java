package jef.database.routing.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jef.common.PairSO;
import jef.common.log.LogUtil;
import jef.common.wrapper.IntRange;
import jef.database.ORMConfig;
import jef.database.OperateTarget.SqlTransformer;
import jef.database.Session;
import jef.database.annotation.PartitionResult;
import jef.database.jsqlparser.expression.Table;
import jef.database.jsqlparser.statement.select.Distinct;
import jef.database.jsqlparser.statement.select.Limit;
import jef.database.jsqlparser.statement.select.OrderBy;
import jef.database.jsqlparser.statement.select.PlainSelect;
import jef.database.jsqlparser.visitor.Expression;
import jef.database.jsqlparser.visitor.SelectItem;
import jef.database.wrapper.ResultIterator;
import jef.database.wrapper.clause.GroupByItem;
import jef.database.wrapper.clause.GroupFunctionType;
import jef.database.wrapper.clause.InMemoryDistinct;
import jef.database.wrapper.clause.InMemoryGroupByHaving;
import jef.database.wrapper.populator.ColumnMeta;
import jef.database.wrapper.populator.Transformer;
import jef.database.wrapper.result.IResultSet;
import jef.database.wrapper.result.MultipleResultSet;
import jef.tools.Assert;

/**
 * 路由查询执行计划
 * @author jiyi
 *
 */
public class SelectExecutionPlan implements ExecutionPlan {
	/**
	 * 路由结果
	 */
	private PartitionResult[] sites;
	/**
	 * 输入的SQL上下文
	 */
	private StatementContext<PlainSelect> context;
	
	
	/*  
	 * 动作和路由结果的关系
	//SQL操作(查询前)        
	//表名改写          条件：全部
	 //noGroup——SQL尾部以及Select部分中的聚合函数去除           条件:多表(不区分)
	//noHaving——位于延迟的SQL尾部                              条件:多库()  ——union部分
	//Order——位于子查询的尾部                                  条件：多表(不区分) 
	//limit延迟                                                 条件：单库多表                           
	//limit去除                                                 条件：多库
	
	//内存操作(查询后)
	//内存分页          条件：多库
	//内存排序：        条件：多库
	//内存排重          条件：多库
	//内存分组          条件：多库 
	 */

	

	public SelectExecutionPlan(PartitionResult[] results, StatementContext<PlainSelect> context) {
		LogUtil.show(results);
		this.sites = results;
		this.context=context;
	}



	/*
	 * 得到一个数据库上操作的SQL语句
	//表名改写          条件：全部
	 //noGroup延迟——SQL尾部以及Select部分中的聚合函数去除 条件:多表(不区分)
	//延迟Group消除——  当多库时(?) 延迟Group不添加
	//延迟noHaving消除——位于延迟的SQL尾部                              条件:多库()  ——union部分
	 * 
	//Order——位于子查询的尾部                                  条件：多表(不区分) 
	//limit延迟                                                 条件：单库多表                           
	//limit去除                                                 条件：多库
	 */
	public PairSO<List<Object>> getSql(PartitionResult site) {
		List<String> tables=site.getTables();
		boolean moreTable=tables.size()>1; //是否为多表
		boolean moreDatabase=isMultiDatabase();//是否为多库
		
		PlainSelect st=context.statement;
		if(moreTable){
			StringBuilder sb = new StringBuilder(200);
			for (int i = 0; i < tables.size(); i++) {
				if (i > 0) {
					sb.append("\n union all \n");
				}
				String tableName = site.getTables().get(i);
				appendSql(sb, tableName, true,true, true, true, true);  //union子查询
			}
			//绑定变量参数翻倍
			List<Object> params=SqlAnalyzer.repeat(context.params, tables.size());
			
			//聚合操作用Union外嵌查询实现
			if(hasAnyGroupDistinctOrderLimit()){
				StringBuilder sb2 = new StringBuilder();
				st.appendSelect(sb2, false);
				sb2.append(" from \n(").append(sb).append(") t");
				sb2.append(ORMConfig.getInstance().wrap);
				st.appendGroupHavingOrderLimit(sb2, moreDatabase, false, moreDatabase);
				sb = sb2;
			}
			return  new PairSO<List<Object>>(sb.toString(),params);
		}else{
			String table=site.getTables().get(0);
			StringBuilder sb=new StringBuilder();
			//单表情况下
			if(moreDatabase){//多库单表
				appendSql(sb, table, false, false, false, true, false);
			}else{
				appendSql(sb, table, false, false, false, false, false);
			}
			return new PairSO<List<Object>>(sb.toString(),context.params);
		}
	}

	/*
	 * 有没有Group/Having/Distinct/Order/Limit等会引起查询复杂的东西？
	 */
	private boolean hasAnyGroupDistinctOrderLimit() {
		PlainSelect st=context.statement;
		if(st.getDistinct()!=null){
			return true;
		}
		if(st.getLimit()!=null){
			return true;
		}
		if(st.getGroupByColumnReferences()!=null && !st.getGroupByColumnReferences().isEmpty()){
			return true;
		}
		if(st.getOrderBy() !=null && !st.getOrderBy().getOrderByElements().isEmpty()){
			return true;
		}
		return false;
	}

	private void appendSql(StringBuilder sb,String table,boolean noGroup,boolean noHaving,boolean noOrder,boolean noLimit,boolean noDistinct) {
		for (Table tb : context.modifications) {
			tb.setReplace(table);
		}
		context.statement.appendTo(sb, noGroup,noHaving, noOrder, noLimit, noDistinct);
		//清理现场
		for (Table tb : context.modifications) {
			tb.removeReplace();
		}
	}



	public PartitionResult[] getSites() {
		return sites;
	}

	public <X> ResultIterator<X> getIteratorResult(IntRange range, SqlTransformer<X> resultTransformer, int i, int fetchSize) {
		return null;
	}

	//多库查询下，进行内存中的后处理
	//内存操作(查询后)
	//内存分页          条件：多库
	//内存排序：        条件：多库
	//内存排重          条件：多库
	//内存分组          条件：多库 
	public <X> List<X> getListResult(IntRange range,Transformer rst,MultipleResultSet rs) throws SQLException {
		Assert.isTrue(isMultiDatabase());
		ColumnMeta meta=rs.getColumns();
		parepareInMemoryProcess(meta,range,rs);
		IResultSet rsw=rs.toSimple(null,rst.getStrategy());
		return context.db.populateResultSet(rsw, null, rst);
	}
	

	/**
	 * 根据查询结果，对照查询语句分析，是否需要内存操作
	 * @return  return true if need In Memory Process.
	 */
	private void parepareInMemoryProcess(ColumnMeta meta,IntRange range,MultipleResultSet rs) {
		PlainSelect st=context.statement;
		if(st.getGroupByColumnReferences()!=null && !st.getGroupByColumnReferences().isEmpty()){
			rs.setInMemoryGroups(processGroupBy(meta));
		}
		if(st.getDistinct()!=null){
			rs.setInMemoryDistinct(processDistinct(st.getDistinct(),meta));
		}
		if(st.getOrderBy()!=null){
			processOrder(st.getOrderBy(),meta);
		}
		if(st.getLimit()!=null){
			processPage(st.getLimit(),meta,range);
		}
	}	

	public long processCount(PartitionResult site, Session session) {
		return 0;
	}

	public int processUpdate(PartitionResult site, Session session) {
		return 0;
	}
	private InMemoryGroupByHaving processGroupBy(ColumnMeta meta) {
		PlainSelect select=context.statement;
		return generateGroupHavingProcess(select.getSelectItems(), select.getGroupByColumnReferences(), meta);
	}
	
	/**
	 * 只有当确定select语句中使用了groupBy后才走入当前分支，解析出当前的内存分组任务
	 * @param selects
	 * @return
	 */
	private InMemoryGroupByHaving generateGroupHavingProcess(List<SelectItem> selects,List<Expression> groupExps,ColumnMeta meta) {
		List<GroupByItem> keys=new ArrayList<GroupByItem>();
		List<GroupByItem> values=new ArrayList<GroupByItem>();
		
		
		
		//解析出SQL修改句柄，当延迟操作group时，必然要将原先的分组函数去除，配合将groupBy去除
		
		Set<String> groups=new HashSet<String>();
		for(Expression exp: groupExps){
			groups.add(exp.toString().toUpperCase());
		}
		for(int i=0;i<selects.size();i++){
			SelectItem e=selects.get(i);
			Expression ex=e.getExpression();
			String alias=e.getAlias();
			if(ex==null)
				continue;//基本上是不可能的，在group的语句中
			
			//TODO 先用简单粗暴的方式做出来再说，性能什么的再说了……
			String sql=ex.toString().toUpperCase();
			if(groups.contains(sql)){
				keys.add(new GroupByItem(i,GroupFunctionType.GROUP,alias));
			}else{
				GroupFunctionType type;
				String exp=sql.toUpperCase();
				if(exp.startsWith("AVG(")){
					type=GroupFunctionType.AVG;
				}else if(exp.startsWith("COUNT(")){
					type=GroupFunctionType.COUNT;
				}else if(exp.startsWith("SUM(")){
					type=GroupFunctionType.SUM;
				}else if(exp.startsWith("MIN(")){
					type=GroupFunctionType.MIN;
				}else if(exp.startsWith("MAX(")){
					type=GroupFunctionType.MAX;
				}else if(exp.startsWith("ARRAY_TO_STRING(")){	
					type=GroupFunctionType.ARRAY_TO_STRING;
				}else{
					type=GroupFunctionType.NORMAL;
				}	
				values.add(new GroupByItem(i,type,alias));
			}
		}
		return new InMemoryGroupByHaving(keys,values);
	}
	
	
	/**
	 * 是否为多库查询。
	 * @return
	 */
	public boolean isMultiDatabase() {
		return sites.length > 1;
	}

	/**
	 * 是否为简单查询——路由结果指向单数据库的单表。这意味着所有的内存处理和SQL改写都不用做了。
	 * （除了表名改写以外）
	 * @return
	 */
	public boolean isSingleTable() {
		return sites.length == 1 && sites[0].getTables().size() == 1;
	}
	
	//多库Distinct
	private InMemoryDistinct processDistinct(Distinct distinct,ColumnMeta meta){
		return InMemoryDistinct.instance;
	}

	//多库排序
	private void processOrder(OrderBy orderBy,ColumnMeta meta) {
		List<SelectItem> selects=context.statement.getSelectItems();
		
		
		//inMemoryOrder
		
	}

	private void processPage(Limit limit,ColumnMeta meta,IntRange range) {
		// TODO Auto-generated method stub
		
	}


}