
import java.util.ArrayList;

/**
 * CS 267 - Project - Implements an Index for the Table.
 */
public class Index 
{
	private String idxName;
	private ArrayList<IndexKeyDef> idxKey;
	private boolean isUnique;
	private ArrayList<IndexKeyVal> keys;
	
	public boolean delete = false;

	public class IndexKeyDef 
	{
		public int idxColPos;
		public int colId;
		public boolean descOrder;
		
		public IndexKeyDef()
		{}
		
		public IndexKeyDef(int idxColPos1, int colId1, boolean descOrder1)
		{
			idxColPos = idxColPos1;
			colId = colId1;
			descOrder = descOrder1;
		}
	}

	public class IndexKeyVal 
	{
		public int rid;
		public String value;
		
		public IndexKeyVal()
		{}
		
		public IndexKeyVal(int rid1, String value1)
		{
			rid = rid1;
			value = value1;
		}
	}

	public Index(String name) 
	{
		idxName = name;
		idxKey = new ArrayList<IndexKeyDef>();
		keys = new ArrayList<IndexKeyVal>();
	}

	public String getIdxName() {
		return idxName;
	}

	public void setIdxName(String idxName) {
		this.idxName = idxName;
	}

	public ArrayList<IndexKeyDef> getIdxKey() {
		return idxKey;
	}

	public void addIdxKey(IndexKeyDef idxKeyDef) {
		idxKey.add(idxKeyDef);
	}

	public ArrayList<IndexKeyVal> getKeys() {
		return keys;
	}

	public void addKey(IndexKeyVal key) {
		keys.add(key);
	}

	public boolean getIsUnique() {
		return isUnique;
	}

	public void setIsUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}
}
