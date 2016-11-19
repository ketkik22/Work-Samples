package sales;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;

import java.io.IOException;


public class salesMapper  extends Mapper <LongWritable,Text,Text,Text> {
   public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      
      //TODO create array of string tokens from record assuming space-separated fields using split() method of String class
      String recordString=null;
      String recordTokenArray[], dateToken[];
      recordString=value.toString();
      recordTokenArray= recordString.split("\\s+");
      
      
      // TODO pull out Store id from data
      String storeId= recordTokenArray[0];
      String date=recordTokenArray[1];
      dateToken=date.split("-");
      String month=dateToken[1];
      String year=dateToken[0];
      
      //To form value
      String sales=recordTokenArray[2];
      String promo=recordTokenArray[3];
      String holiday=recordTokenArray[4];
      
       
      // TODO pull out date from data
     context.write(new Text(storeId+"_"+month+"_"+year), new Text(sales+"_"+promo+"_"+holiday));
   }
}
