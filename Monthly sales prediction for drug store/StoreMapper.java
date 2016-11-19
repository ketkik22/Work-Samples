package Store;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;

import java.io.IOException;
import java.util.StringTokenizer;

public class StoreMapper extends Mapper <LongWritable,Text,Text,Text> 
{
   public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException 
   {
      	String storeId, storeData;
      	String storeDataSet[] = value.toString().split("\\s+");
	
      	storeId = storeDataSet[0];
      	
      	storeData = storeDataSet[1] + "_" + storeDataSet[2] + "_" + storeDataSet[3] + "_" + storeDataSet[4];

      	context.write(new Text(storeId), new Text(storeData));
   }
}
