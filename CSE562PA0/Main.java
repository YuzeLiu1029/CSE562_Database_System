package dubstep;

import java.io.*;
import java.sql.SQLException;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;


import java.util.Scanner;

/**
 * Created by yuzeliu on 2/6/17.
 */
public class Main {
    public static void main(String args[]) throws ParseException, SQLException,IOException{

        BufferedReader br = null;
        Table tableTest = new Table();
        //String tablename = tableTest.getName();
        //BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Scanner scan = new Scanner(System.in);
        //StringReader input = new StringReader("SELECT * FROM R");
        String s = scan.nextLine();
        StringReader input = new StringReader(s);
        CCJSqlParser parser = new CCJSqlParser(input);
        Statement query = parser.Statement();

        if(query instanceof Select){
            Select selectStatement = (Select) query;
            /*Do sth. with the Select operator*/
            //FromItem fromItem = PlainSelect.getFromItem();
            PlainSelect ps = (PlainSelect)selectStatement.getSelectBody();
            tableTest = (Table)ps.getFromItem();
            String fName = tableTest.getName();
            //System.out.println(fName);

            //String csvFile = "/Users/yuzeliu/Desktop/" + fName +".csv";
            String csvFile = "data/" + fName +".csv";
            //String csvFile = fName +".csv";
            br = new BufferedReader(new FileReader(csvFile));
            String line = "";
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
            //System.out.println(ps);
            //System.out.println(ps.getSelectItems());

        } else {
            throw new java.sql.SQLException("I can't understand " + query);
        }
    }

}


