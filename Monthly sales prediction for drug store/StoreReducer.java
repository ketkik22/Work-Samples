package Store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;


public class StoreReducer extends Reducer <Text,Text,Text,Text>
{
	private int storeId, newStoreId;
	
	private String fileName = "/user/user01/Project/Sales/OUT/part-r-00000";
	private String newValue = "";
	private String line = "";
	private String value1 = "", value = "";
	private String newKey = "";
	
	File reducer1OutputFile = new File(fileName);
	BufferedReader reader;
	
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	{
		storeId = Integer.parseInt(key.toString());
		Text text = new Text();
		for(Text t : values)
		{
			text = t;
			value = String.valueOf(text);
			reader = new BufferedReader(new FileReader(reducer1OutputFile));
			
			while((line = reader.readLine())!=null)
			{
				newValue = "";
				String data[] = line.split("\\s+");
				value1 = data[1];
				String data1[] = data[0].split("_");
				newStoreId = Integer.parseInt(data1[0]);
				
				if(newStoreId==storeId)
				{
					newValue = value1 + "_" + value;
					newKey = data[0];
					context.write(new Text(newKey), new Text(newValue));
				}
			}
		}
	}
}