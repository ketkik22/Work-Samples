
package sales;

import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.Text;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;


public class salesReducer  extends Reducer <Text,Text,Text,Text> {
   
   String tempString=null;
   String tempTokens[];
   
   public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

      float tempSales =0, tempPromo=0, tempholiday=0,totalSales =0, totalPromo=0, totalholiday=0;
  
	   String output = null;
	   int count = 0;
	  float meanSales=0.0f;
       for(Text value: values)
       {
    	   tempTokens=value.toString().split("_");
    	   tempSales=Integer.parseInt(tempTokens[0]);
    	   tempPromo=Integer.parseInt(tempTokens[1]);
           tempTokens[2]=tempTokens[2].replace('"',' ');
    	   tempholiday=Integer.parseInt(tempTokens[2].trim());
    	   
    	   totalSales=totalSales+tempSales;
    	   totalPromo=totalPromo+tempPromo;
    	   totalholiday=totalholiday+tempholiday;
    	   count++;
       }
       meanSales=totalSales/(float)count;
       output=meanSales+"_"+totalPromo+"_"+totalholiday;
       context.write(key, new Text(output));
     
      
      }
}
