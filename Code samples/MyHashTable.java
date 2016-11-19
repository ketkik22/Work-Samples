import java.util.ArrayList;
import java.util.Scanner;


public class MyHashTable<K, V> 
{
	public static void main(String[] args) 
	{
		new TestHarness(new MyHashTable<String, String>()).run();
	}
	
	ArrayList<KeyValue> list = new ArrayList<KeyValue>();
	
	public void put(K key, V value)
	{
		if(key==null || value==null)
			return;
		boolean flag = true;
		for(KeyValue a : list)
		{
			if(a.key == key)
				flag = false;
		}
		if(flag)
		{
			KeyValue a = new KeyValue(key, value);
			list.add(a);
		}
	}
	
	public V get(K key)
	{
		for(KeyValue a : list)
		{
			if(a.key.equals(key))
			{
				System.out.println("dfsf");
				V value = (V)a.value;
				return value;
			}
		}
		return null;
	}
}

class KeyValue<K, V>
{
	K key;
	V value;
	
	public KeyValue(K k, V v)
	{
		key = k;
		value = v;
	}
}

class TestHarness implements Runnable
{
	MyHashTable<String, String> hashTable;
	
	public TestHarness(MyHashTable<String, String> table) 
	{
		hashTable = table;
	}
	
	public void run()
	{
		Scanner sc = new Scanner(System.in);
		while(sc.hasNext())
		{
			String k = sc.nextLine();
			String v = null;
			
			if(k.indexOf("=")!=-1)
			{
				String s[] = k.split("=");
				k = s[0];
				v = s[1];
			}
			
			if(v == null)
				System.out.println(hashTable.get(k));
			else
				hashTable.put(k, v);
		}
	}
}
