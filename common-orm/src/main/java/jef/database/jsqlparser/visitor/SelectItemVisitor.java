/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jef.database.jsqlparser.visitor;

import jef.database.jsqlparser.expression.JpqlParameter;
import jef.database.jsqlparser.expression.Table;
import jef.database.jsqlparser.statement.select.AllColumns;
import jef.database.jsqlparser.statement.select.AllTableColumns;
import jef.database.jsqlparser.statement.select.Join;
import jef.database.jsqlparser.statement.select.OrderBy;
import jef.database.jsqlparser.statement.select.OrderByElement;
import jef.database.jsqlparser.statement.select.SelectExpressionItem;
import jef.database.jsqlparser.statement.select.SubJoin;
import jef.database.jsqlparser.statement.select.SubSelect;

public interface SelectItemVisitor {

    public void visit(AllColumns allColumns);

    public void visit(AllTableColumns allTableColumns);

    public void visit(SelectExpressionItem selectExpressionItem);
    
    public void visit(Table tableName);

    public void visit(SubSelect subSelect);

    public void visit(SubJoin subjoin);
    
    public void visit(JpqlParameter tableClip);
    
    public void visit(OrderByElement orderBy);
    
    public void visit(OrderBy orderBy);
    
    public void visit(Join join);

}
