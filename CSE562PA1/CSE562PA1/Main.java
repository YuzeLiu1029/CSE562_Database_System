package dubstep;

import java.io.*;
import java.sql.Array;
import java.sql.SQLException;
import net.sf.jsqlparser.eval.Eval;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yuzeliu on 2/6/17.
test
 */

public class Main {

    //public static String[] tableArray; //Store all the information about the table, the name of teh columns and the name of the table
    //public static String[] columnDataType;

    public static List<String> tableArray = new ArrayList<String>();
    public static List<String> columnDataType = new ArrayList<String>();
    //

    public static void main(String args[]) throws ParseException, SQLException,IOException{
        int queryType = 0;

      while(true) {
          System.out.print("$>");
          BufferedReader br = null;
          Scanner scan = new Scanner(System.in);
          String s = scan.nextLine();
          StringReader input  = new StringReader(s);
          CCJSqlParser parser = new CCJSqlParser(input);
          Statement query = parser.Statement();

          if(query instanceof CreateTable){
              tableArray = getTableColName(query); //Get the table information from the CREATE TABLE query
              columnDataType = getColumnDataType(query);

          } else if(query instanceof Select){

              queryType = getQueryType(query);
              Select selectStatement = (Select) query;
              PlainSelect ps = (PlainSelect)selectStatement.getSelectBody();
              Table tableTest = new Table();
              String fName = ps.getFromItem().toString();
              tableTest = (Table)ps.getFromItem();

              List selectItem = ps.getSelectItems();
              ArrayList<String> selectItemList = new ArrayList<String>();

              for(Object i : selectItem){
                  selectItemList.add(i.toString());
              }

              Expression whereClause = ps.getWhere();

              String[] selectItemArray = new String[selectItem.size()];
              selectItemList.toArray(selectItemArray);

              //String csvFile = "/Users/yuzeliu/Desktop/" + fName +".csv";
              String csvFile = "data/" + fName +".csv";
              br = new BufferedReader(new FileReader(csvFile));
              String line = "";
              PrimitiveValue[] sumResult = new PrimitiveValue[selectItemList.size()];
              for(int i =0;i<sumResult.length;i++){
                  sumResult[i] = new LongValue(0);
              }

              while ((line = br.readLine()) != null) {
                  String[] lineValue = line.split("\\|");
                  List<Integer> printList = new ArrayList<>();
                  List<Expression> selectExpressionList = new ArrayList<>();
                  PrimitiveValue result;
                  PrimitiveValue result2;
                  List<PrimitiveValue> printListPrimitiveVal = new ArrayList<>();

                  Eval eval = new Eval() {
                      @Override
                      public PrimitiveValue eval(Column column) throws SQLException {
                          String columnName = column.getColumnName();
                          int index = 0;
                          for(int i  =0 ; i < tableArray.size(); i++){
                              if(columnName.equals(tableArray.get(i))){
                                  index  = i;
                                  //continue;
                              }
                          }
                          String dataTypeTemp = columnDataType.get(index);
                          String dataVal = lineValue[index];
                          if(dataTypeTemp.equals("int")){
                              Long returnVal = Long.parseLong(dataVal);
                              return new LongValue(returnVal);
                          } else if( (dataTypeTemp.equals("varchar")) || (dataTypeTemp.equals("char")) || (dataTypeTemp.equals("string")) ){
                              //String returnVal = dataVal;
                              return new StringValue(dataVal);
                          } else if(dataTypeTemp.equals("decimal")){
                              Double returnVal = Double.parseDouble(dataVal);
                              return new DoubleValue(returnVal);
                          } else if(dataTypeTemp.equals("date")){
                              return new DateValue(dataVal);
                          }
                          return null;
                      }
                  };

                  switch (queryType){
                      case 1 :
                          //1. SELECT A, B,....... FROM R
                          for(int i = 0;i<selectItem.size();i++){
                              for(int j = 0;j<tableArray.size();j++){
                                  if(selectItemArray[i].equals(tableArray.get(j))){
                                      printList.add(j);
                                      //System.out.println(j);
                                  }
                              }
                          }
                          printResultV1(printList,lineValue);
                          break;
                      case 2:
                          //2. SELECT A, B,....... FROM R WHERE........
                          //PrimitiveValue result;
                          result = (BooleanValue)eval.eval(whereClause);
                          //System.out.println(result);
                          for(int i = 0; i< selectItem.size();i++){
                              for(int j = 0;j<tableArray.size();j++){
                                  if(selectItemArray[i].equals(tableArray.get(j))){
                                      if(result.toBool()){
                                          printList.add(j);
                                          //System.out.println(j);
                                      }
                                  }
                              }
                          }
                          printResultV1(printList,lineValue);
                          //System.out.println("This is type 2 query");
                          break;
                      case 3 : //3. SELECT A + B AS C,........ FROM R
                      case 4 : //4. SELECT A + B FROM R
                          for(Object i : selectItem){
                              Expression expression2 = ((SelectExpressionItem) i).getExpression();
                              //Expression expression3 = ((SelectExpressionItem) i).getExpression();
                              //System.out.println(expression2 + " AS " + aliasName);
                              selectExpressionList.add(expression2);
                          }
                          for(Expression i : selectExpressionList){
                              result = eval.eval(i);
                              printListPrimitiveVal.add(result);
                              //
//                              String aliasName = ((SelectExpressionItem) i).getAlias();
//                              if(tableArray.contains(aliasName)){
//                                  int indexTemp = tableArray.indexOf(aliasName);
//                                  lineValue[indexTemp] = result.toString();
//                              }else{
//                                  tableArray.add(aliasName);
//                                  lineValue  = Arrays.copyOf(lineValue, lineValue.length + 1);
//                                  lineValue[lineValue.length - 1] = result.toString();
//                              }
                              //
                          }
                          printResultPrimitiveVal(printListPrimitiveVal);
                          //System.out.println("This is type 3 query");
                          break;
                      //case 4:
                      //    System.out.println("This is type 4 query");
                      //    break;
                      case 5: // SELECT A + B AS C,........ FROM R WHERE.......
//                          System.out.println("This is type 5 query");
//                          break;
                      case 6: //SELECT A + B,......... FROM R WHERE.......
                          for(Object i : selectItem){
                              Expression expression2 = ((SelectExpressionItem) i).getExpression();
                              selectExpressionList.add(expression2);
                          }
                          for(Expression i : selectExpressionList){
                              result = eval.eval(i);
                              printListPrimitiveVal.add(result);
                          }
                          //printResultPrimitiveVal(printListPrimitiveVal);
                          result2 = (BooleanValue)eval.eval(whereClause);
                          if(result2.toBool()){
                              //printList.add(j);
                              //System.out.println(j);
                              printResultPrimitiveVal(printListPrimitiveVal);
                          }
                          //System.out.println("This is type 6 query");
                          break;
                      case 7://SELECT SUM(A+B) AS C,........FROM R
                      case 8://SELECT SUM(A+B)........FROM R
                          for(Object i : selectItem){
                              Expression expression2 = ((SelectExpressionItem) i).getExpression();
                              Function expression3 = (Function) expression2;
                              ExpressionList expressionListTemp = expression3.getParameters();
                              List<Expression> expression4 = expressionListTemp.getExpressions();
                              int indexTemp = selectItem.indexOf(i);
                              for(Expression j : expression4){
                                  result = eval.eval(j);
                                  sumResult[indexTemp] = eval.eval(new Addition(sumResult[indexTemp],result));
                              }
                          }
                          break;
                      case 9:
                      case 10:
                          result2 = (BooleanValue)eval.eval(whereClause);
                          if(result2.toBool()) {
                              for (Object i : selectItem) {
                                  Expression expression2 = ((SelectExpressionItem) i).getExpression();
                                  Function expression3 = (Function) expression2;
                                  ExpressionList expressionListTemp = expression3.getParameters();
                                  List<Expression> expression4 = expressionListTemp.getExpressions();
                                  int indexTemp = selectItem.indexOf(i);
                                  for (Expression j : expression4) {
                                      result = eval.eval(j);
                                      sumResult[indexTemp] = eval.eval(new Addition(sumResult[indexTemp], result));
                                  }
                              }
                          }
                          //System.out.println("This is type 10 query");
                          break;
                      default:
                          System.out.println("This is not a standard query.");
                  }
              }

              if(queryType == 7 || queryType == 8 || queryType == 9 || queryType == 10){
                  printResultPrimitiveValArray(sumResult);
              }
          } else{
              System.out.print("I can't understand the query.");
          }

      }

    }
    /*
    getQueryType will return an integer indicate the type of the query.
    In this function , we have 10 different queries(Additional 4 +the original 6), here is the list:
    1. SELECT A, B,....... FROM R
    2. SELECT A, B,....... FROM R WHERE........
    3. SELECT A + B AS C,........ FROM R
    4. SELECT A + B,......... FROM R
    5. SELECT A + B AS C,........ FROM R WHERE.......
    6. SELECT A + B,......... FROM R WHERE.......
    7. SELECT SUM(A+B) AS C,........FROM R
    8. SELECT SUM(A+B),....... FROM R
    9. SELECT SUM(A+B) AS C,........FROM R WHERE......
    10. SELECT SUM(A+B),....... FROM R WHERE.........
    */
    public static int getQueryType(Statement query) throws IOException{
        int queryType = 0;
        Select selectStatement = (Select) query;
        PlainSelect ps = (PlainSelect)selectStatement.getSelectBody();
        List selectItem = ps.getSelectItems();
        Expression whereClause = ps.getWhere();
        if(whereClause != null){ //The query contains a where clause
            for(Object i : selectItem){
                if(((SelectExpressionItem) i).getAlias() != null){ // It contains AS in the select query
                    Expression expression2 = ((SelectExpressionItem) i).getExpression();
                    if(expression2 instanceof Function){
                        queryType = 9;
                        break;
                        //return queryType;
                    } else if(expression2 instanceof BinaryExpression){
                        queryType = 5;
                        break;
                        //return queryType;
                    }

                } else {// It doesn't contain AS in the select query
                    Expression expression2 = ((SelectExpressionItem) i).getExpression();
                    if(expression2 instanceof Function){
                        queryType = 10;
                        break;
                        //return queryType;
                    } else if(expression2 instanceof BinaryExpression){
                        queryType = 6;
                        break;
                        //return queryType;
                    } else{
                        queryType = 2;
                        break;
                    }
                }
            }
        } else { //The query doesn't have a where clause
            for(Object i : selectItem){
                if(((SelectExpressionItem) i).getAlias() != null){ // It contains AS in the select query
                    Expression expression2 = ((SelectExpressionItem) i).getExpression();
                    if(expression2 instanceof Function){
                        queryType = 7;
                        break;
                    } else if(expression2 instanceof BinaryExpression){
                        queryType = 3;
                        break;
                    }
                } else {// It doesn't contain AS in the select query
                    Expression expression2 = ((SelectExpressionItem) i).getExpression();
                    if(expression2 instanceof Function){
                        queryType = 8;
                        break;
                    } else if(expression2 instanceof BinaryExpression){
                        queryType = 4;
                        break;
                    } else {
                        queryType = 1;
                        break;
                    }
                }
            }
        }
        return queryType;
    }

    /*
    getColumnName will retun an Array with all the column names in the where clause of the query
    */
    public static String[] getColName(Expression expression, ArrayList<String> allColumnNames) {

        String columnName = null;
        if(expression instanceof BinaryExpression){
            Expression leftExpression = ((BinaryExpression) expression).getLeftExpression();
            if(leftExpression  instanceof Column){
                columnName = ((Column) leftExpression).getColumnName();
                allColumnNames.add(columnName);
            }
            else if(leftExpression instanceof BinaryExpression){
                getColName((BinaryExpression)leftExpression,allColumnNames);
            }

            Expression rightExpression = ((BinaryExpression) expression).getRightExpression();

            if(rightExpression instanceof BinaryExpression){
                getColName((BinaryExpression)rightExpression,allColumnNames);
            }
        }
        String[] columnNamesArray = new String[allColumnNames.size()];
        return allColumnNames.toArray(columnNamesArray);
    }

    /*
    This function will take the CREATE TABLE query and return the column name in the query as well as the table name as an array
    */
    public static List<String> getTableColName(Statement query){

        List<String> fieldName = new ArrayList<String>();

        String tableName;

        CreateTable createTableStat = (CreateTable) query;
        tableName = createTableStat.getTable().toString();
        Iterator itr = createTableStat.getColumnDefinitions().iterator();
        while(itr.hasNext()){
            ColumnDefinition columnDefinition = (ColumnDefinition) itr.next();
            fieldName.add(columnDefinition.getColumnName());
        }
        //fieldName.add(tableName);
        //String[] tableInfo = new String[fieldName.size()];
        //fieldName.toArray(tableInfo);
        return fieldName;
    }

    public static List<String> getColumnDataType(Statement query){

        List<String> dataType = new ArrayList<String>();
        CreateTable createTableStat = (CreateTable) query;
        Iterator itr = createTableStat.getColumnDefinitions().iterator();
        while(itr.hasNext()){
            ColumnDefinition columnDefinition = (ColumnDefinition) itr.next();
            dataType.add(columnDefinition.getColDataType().getDataType());
        }
        //String[] columnDataType = new String[dataType.size()];
        //dataType.toArray(columnDataType);
        return dataType;
    }

    public static void printResultV1( List<Integer> printList, String[] lineValue){
        if(printList.size() > 1){
            for(int i = 0 ;i < printList.size() -1;i++){
                int indexTemp = printList.get(i);
                System.out.print(lineValue[indexTemp] + "|");
            }
            System.out.print(lineValue[printList.get(printList.size()-1)] + "\n");
        } else if(printList.size() == 1){
            System.out.print(lineValue[printList.get(printList.size()-1)] + "\n");
        }
    }

    public static void printResultPrimitiveVal( List<PrimitiveValue> printList){
        if(printList.size() == 1) {
            System.out.print(printList.get(printList.size() - 1) + "\n");
        } else {
            for(int i = 0 ;i < printList.size() -1;i++) {
                System.out.print(printList.get(i) + "|");
            }
            System.out.print(printList.get(printList.size() - 1) + "\n");
        }
    }

    public static void printResultPrimitiveValArray( PrimitiveValue[] printList){
        if(printList.length == 1) {
            System.out.print(printList[printList.length - 1] + "\n");
        } else {
            for(int i = 0 ;i < printList.length -1;i++) {
                System.out.print(printList[i] + "|");
            }
            System.out.print(printList[printList.length - 1] + "\n");
        }
    }
}




