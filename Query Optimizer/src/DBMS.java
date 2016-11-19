import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.crypto.spec.OAEPParameterSpec;
import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;



/**
 * CS 267 - Project - Implements create index, drop index, list table, and
 * exploit the index in select statements.
 */
public class DBMS 
{
	private static final String COMMAND_FILE_LOC = "Commands.txt";
	private static final String OUTPUT_FILE_LOC = "Output.txt";

	private static final String TABLE_FOLDER_NAME = "tables";
	private static final String TABLE_FILE_EXT = ".tab";
	private static final String INDEX_FILE_EXT = ".idx";

	private DbmsPrinter out;
	private ArrayList<Table> tables;

	public DBMS() 
	{
		tables = new ArrayList<Table>();
	}

	/**
	 * Main method to run the DBMS engine.
	 * 
	 * @param args
	 *            arg[0] is input file, arg[1] is output file.
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) 
	{
		DBMS db = new DBMS();
		db.out = new DbmsPrinter();
		Scanner in = null;
		boolean isIndexUnique = false;
		try 
		{
			// set input file
			if (args.length > 0) 
			{
				in = new Scanner(new File(args[0]));
			} 
			else 
			{  
				in = new Scanner(new File(COMMAND_FILE_LOC));
			}

			// set output files
			if (args.length > 1) 
			{
				db.out.addPrinter(args[1]);
			} 
			else 
			{
				db.out.addPrinter(OUTPUT_FILE_LOC);
			}

			// Load data to memory
			db.loadTables();

			// Go through each line in the Command.txt file
			while (in.hasNextLine()) 
			{
				String sql = in.nextLine();
				StringTokenizer tokenizer = new StringTokenizer(sql);

				// Evaluate the SQL statement
				if (tokenizer.hasMoreTokens()) 
				{
					String command = tokenizer.nextToken();
					if (command.equalsIgnoreCase("CREATE")) 
					{
						if (tokenizer.hasMoreTokens()) 
						{
							command = tokenizer.nextToken();
							if (command.equalsIgnoreCase("TABLE")) 
							{
								db.createTable(sql, tokenizer);
							} 
							else if (command.equalsIgnoreCase("UNIQUE")) 
							{
								// TODO your PART 1 code goes here
								command = tokenizer.nextToken();
								if(command.equalsIgnoreCase("INDEX"))
								{
									isIndexUnique = true;
									db.createIndex(sql, tokenizer, isIndexUnique);
								}
								else
								{
									throw new DbmsError("Invalid CREATE " + command + " statement. '" + sql + "'.");
								}
							} 
							else if(command.equalsIgnoreCase("INDEX"))
							{
								isIndexUnique = false;
								db.createIndex(sql, tokenizer, isIndexUnique);
							}
							else 
							{
								throw new DbmsError("Invalid CREATE " + command + " statement. '" + sql + "'.");
							}
						} 
						else 
						{
							throw new DbmsError("Invalid CREATE statement. '"+ sql + "'.");
						}
					} 
					else if (command.equalsIgnoreCase("INSERT")) 
					{
						db.insertInto(sql, tokenizer);
					}
					else if (command.equalsIgnoreCase("DROP")) 
					{
						if (tokenizer.hasMoreTokens()) 
						{
							command = tokenizer.nextToken();
							if (command.equalsIgnoreCase("TABLE")) 
							{
								db.dropTable(sql, tokenizer);
							}
							else if (command.equalsIgnoreCase("INDEX")) 
							{
								// TODO your PART 1 code goes here
								db.dropIndex(sql, tokenizer);
							}
							else 
							{
								throw new DbmsError("Invalid DROP " + command + " statement. '" + sql + "'.");
							}
						} 
						else 
						{
							throw new DbmsError("Invalid DROP statement. '"+ sql + "'.");
						}
					}
					else if (command.equalsIgnoreCase("RUNSTATS")) 
					{
						// TODO your PART 1 code goes here
						
						String tableName = tokenizer.nextToken(); 
						db.executeRunstats(tableName, sql, tokenizer);
						
						// TODO replace the table name below with the table name
						// in the command to print the RUNSTATS output
						db.printRunstats(tableName);
					} 
					else if (command.equalsIgnoreCase("SELECT")) 
					{
						// TODO your PART 2 code goes here
						db.executeSelectStatement(sql, tokenizer);
					}
					else if (command.equalsIgnoreCase("--")) 
					{
						// Ignore this command as a comment
					} 
					else if (command.equalsIgnoreCase("COMMIT")) 
					{
						try 
						{	
							// Check for ";"
							if (!tokenizer.nextElement().equals(";")) 
							{
								throw new NoSuchElementException();
							}

							// Check if there are more tokens
							if (tokenizer.hasMoreTokens()) 
							{
								throw new NoSuchElementException();
							}

							// Save tables to files
							for (Table table : db.tables) 
							{
								db.storeTableFile(table);
							}
						} 
						catch (NoSuchElementException ex) 
						{
							throw new DbmsError("Invalid COMMIT statement. '" + sql + "'.");
						}
					} 
					else 
					{
						throw new DbmsError("Invalid statement. '" + sql + "'.");
					}
				}
			}

			// Save tables to files
			for (Table table : db.tables) 
			{
				db.storeTableFile(table);
			}
		}
		catch (DbmsError ex) 
		{
			db.out.println("DBMS ERROR:  " + ex.getMessage());
			ex.printStackTrace();
		}
		catch (Exception ex) 
		{
			db.out.println("JAVA ERROR:  " + ex.getMessage());
			ex.printStackTrace();
		}
		finally 
		{
			// clean up
			try 
			{
				in.close();
			}
			catch (Exception ex) {}

			try 
			{
				db.out.cleanup();
			} catch (Exception ex) {}
		}
	}

	/**
	 * Loads tables to memory
	 * 
	 * @throws Exception
	 */
	private void loadTables() throws Exception 
	{
		// Get all the available tables in the "tables" directory
		File tableDir = new File(TABLE_FOLDER_NAME);
		if (tableDir.exists() && tableDir.isDirectory()) 
		{
			for (File tableFile : tableDir.listFiles()) 
			{
				// For each file check if the file extension is ".tab"
				String tableName = tableFile.getName();
				int periodLoc = tableName.lastIndexOf(".");
				String tableFileExt = tableName.substring(tableName.lastIndexOf(".") + 1);
				if (tableFileExt.equalsIgnoreCase("tab")) 
				{
					// If it is a ".tab" file, create a table structure
					Table table = new Table(tableName.substring(0, periodLoc));
					Scanner in = new Scanner(tableFile);

					try 
					{
						// Read the file to get Column definitions
						int numCols = Integer.parseInt(in.nextLine());

						for (int i = 0; i < numCols; i++) 
						{
							StringTokenizer tokenizer = new StringTokenizer(in.nextLine());
							String name = tokenizer.nextToken();
							String type = tokenizer.nextToken();
							boolean nullable = Boolean.parseBoolean(tokenizer.nextToken());
							
							switch (type.charAt(0)) 
							{
								case 'C':
									table.addColumn(new Column(i + 1, name,
											Column.ColType.CHAR, Integer
													.parseInt(type.substring(1)),
											nullable));
									break;
								case 'I':
									table.addColumn(new Column(i + 1, name,
											Column.ColType.INT, 4, nullable));
									break;
								default:
									break;
							}
						}

						// Read the file for index definitions
						int numIdx = Integer.parseInt(in.nextLine());
						for (int i = 0; i < numIdx; i++) 
						{
							StringTokenizer tokenizer = new StringTokenizer(in.nextLine());
							Index index = new Index(tokenizer.nextToken());
							index.setIsUnique(Boolean.parseBoolean(tokenizer.nextToken()));

							int idxColPos = 1;
							while (tokenizer.hasMoreTokens()) 
							{
								String colDef = tokenizer.nextToken();
								Index.IndexKeyDef def = index.new IndexKeyDef();
								def.idxColPos = idxColPos;
								def.colId = Integer.parseInt(colDef.substring(0, colDef.length() - 1));
								
								switch (colDef.charAt(colDef.length() - 1)) 
								{
									case 'A':
										def.descOrder = false;
										break;
									case 'D':
										def.descOrder = true;
										break;
									default:
										break;
								}

								index.addIdxKey(def);
								idxColPos++;
							}

							table.addIndex(index);
							loadIndex(table, index);
						}

						// Read the data from the file
						int numRows = Integer.parseInt(in.nextLine());
						for (int i = 0; i < numRows; i++) 
						{
							table.addData(in.nextLine());
						}
						
						// Read RUNSTATS from the file
						while(in.hasNextLine()) 
						{
							String line = in.nextLine();
							StringTokenizer toks = new StringTokenizer(line);
							if(toks.nextToken().equals("STATS")) 
							{
								String stats = toks.nextToken();
								if(stats.equals("TABCARD")) 
								{
									table.setTableCard(Integer.parseInt(toks.nextToken()));
								}
								else if (stats.equals("COLCARD")) 
								{
									Column col = table.getColumns().get(Integer.parseInt(toks.nextToken()));
									col.setColCard(Integer.parseInt(toks.nextToken()));
									col.setHiKey(toks.nextToken());
									col.setLoKey(toks.nextToken());
								}
								else 
								{
									throw new DbmsError("Invalid STATS.");
								}
							} 
							else 
							{
								throw new DbmsError("Invalid STATS.");
							}
						}
					} 
					catch (DbmsError ex) 
					{
						throw ex;
					}
					catch (Exception ex) 
					{
						throw new DbmsError("Invalid table file format.");
					}
					finally 
					{
						in.close();
					}
					tables.add(table);
				}
			}
		} 
		else 
		{
			throw new FileNotFoundException("The system cannot find the tables directory specified.");
		}
	}

	/**
	 * Loads specified table to memory
	 * 
	 * @throws DbmsError
	 */
	private void loadIndex(Table table, Index index) throws DbmsError 
	{
		try 
		{
			Scanner in = new Scanner(new File(TABLE_FOLDER_NAME,table.getTableName() + index.getIdxName() + INDEX_FILE_EXT));
			String def = in.nextLine();
			String rows = in.nextLine();

			while (in.hasNext()) 
			{
				String line = in.nextLine();
				Index.IndexKeyVal val = index.new IndexKeyVal();
				val.rid = Integer.parseInt(new StringTokenizer(line).nextToken());
				val.value = line.substring(line.indexOf("'") + 1,line.lastIndexOf("'"));
				index.addKey(val);
			}
			in.close();
		} 
		catch (Exception ex)
		{
			throw new DbmsError("Invalid index file format.");
		}
	}

	/**
	 * CREATE TABLE
	 * <table name>
	 * ( <col name> < CHAR ( length ) | INT > <NOT NULL> ) ;
	 * 
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	private void createTable(String sql, StringTokenizer tokenizer)throws Exception 
	{
		try 
		{
			// Check the table name
			String tok = tokenizer.nextToken().toUpperCase();
			if (Character.isAlphabetic(tok.charAt(0))) 
			{
				// Check if the table already exists
				for (Table tab : tables) 
				{
					if (tab.getTableName().equals(tok) && !tab.delete) 
					{
						throw new DbmsError("Table " + tok + "already exists. '" + sql + "'.");
					}
				}

				// Create a table instance to store data in memory
				Table table = new Table(tok.toUpperCase());

				// Check for '('
				tok = tokenizer.nextToken();
				if (tok.equals("(")) 
				{
					// Look through the column definitions and add them to the
					// table in memory
					boolean done = false;
					int colId = 1;
					while (!done) 
					{
						tok = tokenizer.nextToken();
						if (Character.isAlphabetic(tok.charAt(0))) 
						{
							String colName = tok;
							Column.ColType colType = Column.ColType.INT;
							int colLength = 4;
							boolean nullable = true;

							tok = tokenizer.nextToken();
							if (tok.equalsIgnoreCase("INT")) 
							{
								// use the default Column.ColType and colLength

								// Look for NOT NULL or ',' or ')'
								tok = tokenizer.nextToken();
								if (tok.equalsIgnoreCase("NOT")) 
								{
									// look for NULL after NOT
									tok = tokenizer.nextToken();
									if (tok.equalsIgnoreCase("NULL")) 
									{
										nullable = false;
									}
									else 
									{
										throw new NoSuchElementException();
									}

									tok = tokenizer.nextToken();
									if (tok.equals(",")) 
									{
										// Continue to the next column
									} 
									else if (tok.equalsIgnoreCase(")")) 
									{
										done = true;
									}
									else 
									{
										throw new NoSuchElementException();
									}
								} 
								else if (tok.equalsIgnoreCase(",")) 
								{
									// Continue to the next column
								}
								else if (tok.equalsIgnoreCase(")")) 
								{
									done = true;
								}
								else 
								{
									throw new NoSuchElementException();
								}
							} 
							else if (tok.equalsIgnoreCase("CHAR")) 
							{
								colType = Column.ColType.CHAR;

								// Look for column length
								tok = tokenizer.nextToken();
								if (tok.equals("(")) 
								{
									tok = tokenizer.nextToken();
									try 
									{
										colLength = Integer.parseInt(tok);
									} 
									catch (NumberFormatException ex) 
									{
										throw new DbmsError("Invalid table column length for " + colName + ". '" + sql + "'.");
									}

									// Check for the closing ')'
									tok = tokenizer.nextToken();
									if (!tok.equals(")")) 
									{
										throw new DbmsError("Invalid table column definition for " + colName + ". '" + sql + "'.");
									}

									// Look for NOT NULL or ',' or ')'
									tok = tokenizer.nextToken();
									if (tok.equalsIgnoreCase("NOT")) 
									{
										// Look for NULL after NOT
										tok = tokenizer.nextToken();
										if (tok.equalsIgnoreCase("NULL")) 
										{
											nullable = false;

											tok = tokenizer.nextToken();
											if (tok.equals(",")) 
											{
												// Continue to the next column
											}
											else if (tok.equalsIgnoreCase(")")) 
											{
												done = true;
											}
											else 
											{
												throw new NoSuchElementException();
											}
										} 
										else 
										{
											throw new NoSuchElementException();
										}
									} 
									else if (tok.equalsIgnoreCase(",")) 
									{
										// Continue to the next column
									}
									else if (tok.equalsIgnoreCase(")")) 
									{
										done = true;
									}
									else 
									{
										throw new NoSuchElementException();
									}
								} 
								else 
								{
									throw new DbmsError("Invalid table column definition for " + colName + ". '" + sql + "'.");
								}
							} 
							else 
							{
								throw new NoSuchElementException();
							}

							// Everything is ok. Add the column to the table
							table.addColumn(new Column(colId, colName, colType,
									colLength, nullable));
							colId++;
						} 
						else 
						{
							// if(colId == 1) {
							throw new DbmsError("Invalid table column identifier " + tok + ". '" + sql + "'.");
							// }
						}
					}

					// Check for the semicolon
					tok = tokenizer.nextToken();
					if (!tok.equals(";")) 
					{
						throw new NoSuchElementException();
					}

					// Check if there are more tokens
					if (tokenizer.hasMoreTokens()) 
					{
						throw new NoSuchElementException();
					}

					if (table.getNumColumns() == 0) 
					{
						throw new DbmsError("No column descriptions specified. '" + sql + "'.");
					}

					// The table is stored into memory when this program exists.
					tables.add(table);

					out.println("Table " + table.getTableName() + " was created.");
				} 
				else 
				{
					throw new NoSuchElementException();
				}
			} 
			else 
			{
				throw new DbmsError("Invalid table identifier " + tok + ". '" + sql + "'.");
			}
		} 
		catch (NoSuchElementException ex) 
		{
			throw new DbmsError("Invalid CREATE TABLE statement. '" + sql + "'.");
		}
	}
	
	/**
	 * CREATE INDEX <index name>
	 * ON <table name>(column list)
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	public void createIndex(String sql, StringTokenizer tokenizer, boolean isUnique) throws Exception
	{
		try
		{
			String token = "";
			String idxName = tokenizer.nextToken();
			Index index = new Index(idxName);
			index.setIsUnique(isUnique);
			token = tokenizer.nextToken();
			
			if(token.equalsIgnoreCase("ON"))
			{
				/*
				 * 	Check 'ON'
				 */
				String tableName = tokenizer.nextToken();
				
				Table table = new Table();
				
				/*
				 *  Check the table on which index is being created exist or not
				 */
				boolean isTableExist = false;
				for(int i = 0; i < tables.size(); i++ )
				{
					Table t = tables.get(i);
					if(t.getTableName().equalsIgnoreCase(tableName))
					{
						isTableExist = true;
						table = t;
					}
				}
				
				if(isTableExist)
				{
					/*
					 * 	Check whether index exist or not in whole DB
					 */
					List<String> columnInfoArray = new ArrayList<String>();
					
					boolean indexAlreadyExist = false;
					
					for(int k = 0; k < tables.size(); k++)
					{
						Table t = tables.get(k);
						ArrayList<Index> indexList = t.getIndexes();
						
						for(int i = 0; i < indexList.size(); i++)
						{
							Index indx1 = indexList.get(i);
							if(idxName.equals(indx1.getIdxName()) && !indx1.delete)
							{
								indexAlreadyExist = true;
								break;
							}
						}
						if(indexAlreadyExist)
							break;
					}
					
					if(!indexAlreadyExist)
					{
						/*
						 * 	Check '('
						 */
						token = tokenizer.nextToken();
						
						if(token.equalsIgnoreCase("("))
						{
							/*
							 * 	Check index columns
							 * 	Array Format --> idxColPos_colID_desc/asc_columnName
							 */
							int count=0;
							
							int idxColsPos = 0, colId = 0;
							String columnName = "", columnInfoRow;
							boolean descOrder = false;
							ArrayList<Column> columnList = table.getColumns();
							
							do
							{
								/*
								 * Get column list and check column exist in table or not
								 */
								count = 0;
								columnName = tokenizer.nextToken();
								
								for(int j = 0; j < columnList.size(); j++)
								{
									Column c = columnList.get(j);
									if(c.getColName().equalsIgnoreCase(columnName))
									{
										count++;
										colId = c.getColId();
									}
								}
								
								if(count==0)
								{
									/*
									 * Column does not exist in table
									 */
									break;
								}
								else
								{
									/*
									 * 	Inserting index columns information into array
									 */
									token = tokenizer.nextToken();
									if(token.equalsIgnoreCase(","))
									{
										descOrder = false;
									}
									else if(token.equalsIgnoreCase(")"))
									{	
										descOrder = false;
									}
									else if(token.equalsIgnoreCase("DESC"))
									{
										descOrder = true;
										token = tokenizer.nextToken();
									}
									idxColsPos++;
									columnInfoRow = colId + "_" + columnName+ "_" + idxColsPos + "_" + descOrder;
									columnInfoArray.add(columnInfoRow);
									index.addIdxKey(index.new IndexKeyDef(idxColsPos, colId, descOrder));
								}
							}
							while(token.equalsIgnoreCase(","));
							
							if(count!=0)
							{
								if(isUnique)
								{
									/*
									 * Checking table data for Unique index
									 */
									boolean isDuplicateRow = false;
									ArrayList<String> tableDataList = table.getData();
									
									count = 1;
									for(int j = 0; j < tableDataList.size(); j++)
									{
										//System.out.println("Iteration ::::::::: "+ count++);
										String row1 = tableDataList.get(j);
										String[] rowDataArray1 = row1.split("\\s+");
										
										for(int k = 0; k < tableDataList.size(); k++)
										{
											if(j!=k)
											{
												String row2 = tableDataList.get(k);
												String[] rowDataArray2 = row2.split("\\s+");
												String rowData1 = "", rowData2 = "";
												
												for(int m = 0; m < columnInfoArray.size(); m++)
												{
													String str[] = columnInfoArray.get(m).split("_");
													int cId = Integer.parseInt(str[0]);
													if(m==0)
													{
														rowData1 = rowDataArray1[cId+1] + " ";
														rowData2 = rowDataArray2[cId+1] + " ";
													}
													else if(m==columnInfoArray.size()-1)
													{
														rowData1 = rowData1 + rowDataArray1[cId+1];
														rowData2 = rowData2 + rowDataArray2[cId+1];
													}
													else
													{
														rowData1 = rowData1 + rowDataArray1[cId+1] + " ";
														rowData2 = rowData2 + rowDataArray2[cId+1] + " ";
													}
													//System.out.println("Row 1 data =  " + rowData1 + "\nRow 2 data =   " + rowData2+"\n");
												}
												if(rowData2.equalsIgnoreCase(rowData1))
												{
													isDuplicateRow = true;
													break;
												}
											}
										}
										if(isDuplicateRow)
											break;
									}
									
									if(!isDuplicateRow)
									{
										/*
										 * 	Generating composite key
										 */
										int rid, colLength = 0;
										String value = "", data;
										String colType = "";
										
										tableDataList = table.getData();
										String columnDataArray[] = new String[columnInfoArray.size()];
										
										for(String row : tableDataList)
										{
											String rowDataArray[] = row.split("\\s+");
											rid = Integer.parseInt(rowDataArray[1]);
											value = "";
											columnDataArray = new String[columnInfoArray.size()];
											
											for(int k = 0; k < columnInfoArray.size(); k++)
											{
												String columnInfo = columnInfoArray.get(k);
												String column[] = columnInfo.split("_");
												colId = Integer.parseInt(column[0]);
												
												columnName = column[1];
												
												idxColsPos = Integer.parseInt(column[2]);
												descOrder = Boolean.parseBoolean(column[3]);
												
												/*
												 *	Get column length
												 */
												columnList = table.getColumns();
												for(Column c : columnList)
												{
													if(colId==c.getColId())
													{
														colId = c.getColId();
														colLength = c.getColLength();
														colType = c.getColType().name();
														break;
													}
												}
												data = rowDataArray[colId+1].trim();
												
												if(descOrder)
												{
													/*
													 * 	Descending order
													 */
													if(data.equalsIgnoreCase("-"))
													{
														data = "~";
														if(colType.equalsIgnoreCase("INT"))
														{
															data = String.format("%-10s", data);
														}
														else if(colType.equalsIgnoreCase("CHAR"))
														{
															data = String.format("%-" + colLength + "s", data);
														}
														
														columnDataArray[idxColsPos-1] = data;
													}
													else
													{
														if(colType.equalsIgnoreCase("INT"))
														{
															data = String.format("%010d", Integer.parseInt(data));
															data = complement(data, colType);
															columnDataArray[idxColsPos-1] = data;
														}
														else if(colType.equalsIgnoreCase("CHAR"))
														{
															data = complement(data, colType);
															data = String.format("%-" + colLength + "s", data);
															columnDataArray[idxColsPos-1] = data;
														}
													}
												}
												else
												{
													/*
													 * 	Ascending order
													 */
													if(data.equalsIgnoreCase("-"))
													{
														data = "~";
														if(colType.equalsIgnoreCase("INT"))
														{
															data = String.format("%-10s", data);
														}
														else if(colType.equalsIgnoreCase("CHAR"))
														{
															data = String.format("%-" + colLength + "s", data);
														}
														
														columnDataArray[idxColsPos-1] = data;
													}
													else
													{
														if(colType.equalsIgnoreCase("INT"))
														{
															data = String.format("%010d", Integer.parseInt(data));
															columnDataArray[idxColsPos-1] = data;
														}
														else if(colType.equalsIgnoreCase("CHAR"))
														{
															data = String.format("%-" + colLength + "s", data);
															columnDataArray[idxColsPos-1] = data;
														}
													}
												}
											}
											for(int t = 0; t < columnDataArray.length; t++)
											{
												value = value + columnDataArray[t];
												//System.out.println(value);
											}
											index.addKey(index.new IndexKeyVal(rid, value));
										}
										Collections.sort(index.getKeys(), DBMS.sort());
										
										table.addIndex(index);
										out.println("Index " + idxName + " was created.");
									}
									else
									{
										throw new DbmsError("Invalid CREATE INDEX statement. Duplicate entry in table " + tableName + ". '" + sql + "'.");
									}
								}
								else
								{
									/*
									 * 	Not a unique index
									 * 	Generating composite key
									 */
									int rid, colLength = 0;
									String value = "", data;
									String colType = "";
									
									ArrayList<String> tableDataList = table.getData();
									String columnDataArray[] = new String[columnInfoArray.size()];
									
									for(String row : tableDataList)
									{
										String rowDataArray[] = row.split("\\s+");
										rid = Integer.parseInt(rowDataArray[1]);
										value = "";
										columnDataArray = new String[columnInfoArray.size()];
										
										for(int k = 0; k < columnInfoArray.size(); k++)
										{
											String columnInfo = columnInfoArray.get(k);
											String column[] = columnInfo.split("_");
											colId = Integer.parseInt(column[0]);
											
											columnName = column[1];
											
											idxColsPos = Integer.parseInt(column[2]);
											descOrder = Boolean.parseBoolean(column[3]);
											
											/*
											 *	Get column length
											 */
											columnList = table.getColumns();
											for(Column c : columnList)
											{
												if(colId==c.getColId())
												{
													colId = c.getColId();
													colLength = c.getColLength();
													colType = c.getColType().name();
													break;
												}
											}
											data = rowDataArray[colId+1];
											
											if(descOrder)
											{
												/*
												 * 	Descending order
												 */
												if(data.equalsIgnoreCase("-"))
												{
													data = "~";
													if(colType.equalsIgnoreCase("INT"))
													{
														data = String.format("%-10s", data);
													}
													else if(colType.equalsIgnoreCase("CHAR"))
													{
														data = String.format("%-" + colLength + "s", data);
													}
													
													columnDataArray[idxColsPos-1] = data;
												}
												else
												{
													if(colType.equalsIgnoreCase("INT"))
													{
														data = String.format("%010d", Integer.parseInt(data));
														data = complement(data, colType);
														columnDataArray[idxColsPos-1] = data;
													}
													else if(colType.equalsIgnoreCase("CHAR"))
													{
														data = complement(data, colType);
														data = String.format("%-" + colLength + "s", data);
														columnDataArray[idxColsPos-1] = data;
													}
												}
											}
											else
											{
												/*
												 * 	Ascending order
												 */
												if(data.equalsIgnoreCase("-"))
												{
													data = "~";
													if(colType.equalsIgnoreCase("INT"))
													{
														data = String.format("%-10s", data);
													}
													else if(colType.equalsIgnoreCase("CHAR"))
													{
														data = String.format("%-" + colLength + "s", data);
													}
													
													columnDataArray[idxColsPos-1] = data;
												}
												else
												{
													if(colType.equalsIgnoreCase("INT"))
													{
														data = String.format("%010d", Integer.parseInt(data));
														columnDataArray[idxColsPos-1] = data;
													}
													else if(colType.equalsIgnoreCase("CHAR"))
													{
														data = String.format("%-" + colLength + "s", data);
														columnDataArray[idxColsPos-1] = data;
													}
												}
											}
										}
										for(int t = 0; t < columnDataArray.length; t++)
										{
											value = value + columnDataArray[t];
										}
										index.addKey(index.new IndexKeyVal(rid, value));
									}
									Collections.sort(index.getKeys(), DBMS.sort());
									
									table.addIndex(index);
									out.println("Index " + idxName + " was created.");
								}
							}
							else
							{
								throw new DbmsError("Invalid CREATE INDEX statement. '" + sql + "'.");
							}
						}
						else
						{
							throw new DbmsError("Invalid CREATE INDEX statement. '" + sql + "'.");
						}
					}
					else
					{
						throw new DbmsError("INDEX " + idxName + " already exist.  '" + sql + "'.");
					}
				}
				else
				{
					throw new DbmsError("TABLE " + tableName + " does not exist. '" + sql + "'.");
				}
			}
			else
			{
				throw new DbmsError("Invalid CREATE INDEX statement. '" + sql + "'.");
			}
			
		}
		catch(NoSuchElementException e)
		{
			throw new DbmsError("Invalid CREATE INDEX statement. '" + sql + "'.");
		}
	}
	
	public void executeSelectStatement(String sql, StringTokenizer tokenizer) throws Exception
	{
		try
		{
			/*
			 * 		Semantic check starts here
			 * 
			 * 		Getting selected column list
			 */
			String token = "";
			int count = 0, i = 0, j = 0;
			Table table;
			
			boolean isOrderByClauseUsed = false;
			boolean isWhereClauseUsed = false;
			boolean andPredicatePresent = false;
			boolean orPredicateUsed = false;
			
			List<String> selectedColumnList = new ArrayList<String>();
			List<Table> selectedTableList = new ArrayList<Table>();
			ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
			List<String> predicateArray = new ArrayList<String>();
			List<Column> sortColumnList = new ArrayList<Column>();
			List<String> sortOrder = new ArrayList<String>();
			
			 
			while(tokenizer.hasMoreElements())
			{
				token = tokenizer.nextToken();
				
				if(token.equalsIgnoreCase("FROM"))
				{
					break;
				}
				else if(token.equalsIgnoreCase(","))
				{
					continue;
				}
				else if(token.equalsIgnoreCase("*"))
				{
					selectedColumnList.add("*");
				}
				else if(token.indexOf(".")!=-1)
				{
					/*
					 * 		Getting Column list
					 */
					
					String tableName = token.substring(0, token.indexOf("."));
					String columnName = token.substring(token.indexOf(".")+1);
					
					table = new Table();
					
					count = 0;
					
					for(Table t : tables)
					{
						if(t.getTableName().equalsIgnoreCase(tableName))
						{
							table = t;
							count++;
						}
					}
					
					if(count!=1)
						throw new DbmsError("Table " + tableName + " does not exist. '" + sql + "'.");
					
					count = 0;
					
					for(Column c : table.getColumns())
					{
						if(c.getColName().equalsIgnoreCase(columnName))
						{
							count++;
						}
					}
					
					if(count!=1)
						throw new DbmsError("Invalid SELECT statement. Column " + columnName + " does not exist. '" + sql + "'.");
					
					selectedColumnList.add(token);
				}
			}
			
			if(token.equalsIgnoreCase("FROM"))
			{
				/*
				 * 		Getting Table list
				 */
				
				while(tokenizer.hasMoreElements())
				{
					token = tokenizer.nextToken();
					
					if(token.equalsIgnoreCase("WHERE"))
					{
						/*
						 * 		End of query or WHERE clause begins
						 */
						break;
					}
					else if(token.equalsIgnoreCase(","))
					{
						predicateArray.add(token);
						continue;
					}
					else if(token.equalsIgnoreCase("ORDER"))
					{
						/*
						 * 		ORDER BY clause begins
						 */
						break;
					}
					else
					{
						count = 0;
						
						for(Table t : tables)
						{
							if(t.getTableName().equalsIgnoreCase(token))
							{
								selectedTableList.add(t);
								count++;
							}
						}
						
						if(count!=1)
						{
							/*
							 * 		table does not exist
							 */
							throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
						}
					}
				}
				
				/*
				 * 	 selected columns belong to tables specified in table list in FROM clause
				 */
				count = 0;
				for(String str : selectedColumnList)
				{
					String tableName = str.substring(0, str.indexOf("."));
					for(Table t : selectedTableList)
					{
						if(t.getTableName().equalsIgnoreCase(tableName))
						{
							count++;
						}
					}
				}
				if(count==selectedColumnList.size())
				{
					if(token.equalsIgnoreCase("WHERE"))
					{
						if(tokenizer.hasMoreElements())
						{

							/*
							 * 		Query having where clause
							 */
							String lhs = "", rhs = "", operator = "";
							String predicateText = "";
							boolean flag, isAbsolutePredicate = false;
							Predicate predicate;
							
							isWhereClauseUsed = true;
							
							while(tokenizer.hasMoreElements())
							{
								token = tokenizer.nextToken();
								predicateText = "";
								
								/*
								 * 		LHS
								 */
								lhs = token;
								if(lhs.indexOf(".")!=-1)
								{
									flag = checkPartOfPredicate(lhs, selectedTableList);
									
									if(flag)
									{
										predicateText = lhs;
									}
									else
									{
										/*
										 * 		The column does not exist in tables specified in table list
										 * 		OR
										 * 		Table does not exist
										 */
										throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
									}
								}
								else if(token.matches("[0-9]+") || token.matches("[a-zA-z]+"))
								{
									predicateText = predicateText + " " + lhs;
									isAbsolutePredicate = true;
								}
								
								/*
								 * 		Operator
								 */
								token = tokenizer.nextToken();
								operator = token;
								if(operator.equalsIgnoreCase("=") || operator.equalsIgnoreCase("<") || operator.equalsIgnoreCase(">"))
								{
									predicateText = predicateText + " " + token;
								}
								else if(operator.equalsIgnoreCase("IN"))
								{
									predicateText = predicateText + " " + operator;
								}
								
								/*
								 * 		RHS
								 */
								token = tokenizer.nextToken();
								rhs = token;
								
								if(rhs.indexOf(".")!=-1)
								{
									/*
									 * 		Join predicate
									 */
									flag = checkPartOfPredicate(rhs, selectedTableList);
									if(flag)
										predicateText = predicateText + " " + rhs;
								}
								else if(token.indexOf("'")!=-1)
								{
									/*
									 * 		invalid character '
									 */
									throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
								}
								else if(rhs.indexOf("(")!=-1)
								{
									/*
									 * 		In list predicate
									 */
									while(tokenizer.hasMoreElements())
									{
										token = tokenizer.nextToken();
										rhs = rhs + token;
										if(token.contains(")"))
												break;
									}
									if(rhs.indexOf(")")!=-1)
									{
										String list = rhs.substring(rhs.indexOf("(")+1, rhs.indexOf(")"));
										int numOfComma = 0;
										boolean invalidValue = false;
										
										for(char c : rhs.toCharArray())
										{
											if(c == ',')
												numOfComma++;
										}
										
										String listValues[] = list.split(",");
										if(rhs.indexOf("(")+1!=rhs.indexOf(")"))
										{
											if(listValues.length-1==numOfComma)
											{
												Column column = getColumn(lhs);
												
												String colType = column.getColType().name();
												int colLength = column.getColLength();
												
												if(colType.equalsIgnoreCase("CHAR"))
												{
													for(String value : listValues)
													{
														value = value.substring(0, value.length());
														if(value.length()<=colLength)
														{
															/*
															 * 		Do nothing
															 */
														}
														else
														{
															invalidValue = true;
															break;
														}
													}
												}
												else if(colType.equalsIgnoreCase("INT"))
												{
													for(String value : listValues)
													{
														value = value.substring(0, value.length());
														if(value.matches("[a-zA-Z]+"))
														{
															/*
															 * 		Integer column contains characters
															 */
															throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
														}
														
														int num = Integer.parseInt(value);
														if(num >= -2147483648 && num < 2147483647)
														{
															/*
															 * 		Do nothing
															 */
														}
														else
														{
															invalidValue = true;
															break;
														}
													}
												}
												
												if(invalidValue)
												{
													/*
													 * 		IN-list contains invalid value
													 */
													throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
												}
												else
												{
													predicateText = predicateText + " " + rhs;
												}
											}
											else
											{
												/*
												 * 		IN-list contains invalid no of values
												 */
												throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
											}
										}
										else
										{
											/*
											 * 		In-list does not contain any values
											 */
											throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
										}
									}
									else
									{
										/*
										 * 		')' is absent
										 */
										throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
									}
								}
								else //if(rhs.matches("[0-9]+") || rhs.matches("[a-zA-z]+"))
								{
									/*
									 * 		Local predicate
									 */
									if(isAbsolutePredicate)
									{
										if(lhs.matches("[0-9]+") && rhs.matches("[0-9]+"))
										{
											predicateText = predicateText + " " + rhs;
										}
										else if(lhs.matches("[a-zA-Z]+") && rhs.matches("[a-zA-Z]+"))
										{
											predicateText = predicateText + " " + rhs;
										}
										else
										{
											/*
											 * 		rhs type does not match lhs type
											 */
											throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
										}
									}
									else
									{
										Column column = getColumn(lhs);
										
										String colType = column.getColType().name();
										int colLength = column.getColLength();
										
										if(colType.equalsIgnoreCase("CHAR"))
										{
											if(rhs.length()<=colLength)
											{
												predicateText = predicateText + " " + rhs;
											}
											else
											{
												/*
												 * 		string value is greater than column length
												 */
												throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
											}
										}
										else if(colType.equalsIgnoreCase("INT"))
										{
											int num = Integer.parseInt(rhs);
											if(num >= -2147483648 && num < 2147483647)
											{
												predicateText = predicateText + " " + rhs;
											}
											else
											{
												/*
												 * 		number is out of range of int
												 */
												throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
											}
										}
									}
								}
								
								predicate = new Predicate();
								predicate.setText(predicateText);
								
								if(predicateText.indexOf("IN (")!=-1)
								{
									predicate.setInList(true);
									predicate.setType('I');
								}
								else
								{
									predicate.setInList(false);
								}
								predicateList.add(predicate);
								predicateArray.add(predicateText);
								
								if(tokenizer.hasMoreElements())
								{
									token = tokenizer.nextToken();
									if(token.equalsIgnoreCase("ORDER"))
									{
										break;
									}
									else if(token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR"))
									{
										predicateArray.add(token);
									}
									else
									{
										/*
										 * 		There should be either boolean operator or "ORDER BY" clause
										 */
										throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
									}
								}
							}
						}
						else
						{
							/*
							 * 		No predicates specified in where clause
							 */
							throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
						}
					}
					if(predicateList.size()<=8)
					{
						if(token.equalsIgnoreCase("ORDER"))
						{

							/*
							 * 		Query having ORDER BY clause 
							 */

							token = tokenizer.nextToken();
							
							if(token.equalsIgnoreCase("BY"))
							{
								/*
								 * 		Sort columns
								 */
								isOrderByClauseUsed = true;
								while(tokenizer.hasMoreElements())
								{
									token = tokenizer.nextToken();
									if(token.indexOf(".")!=-1)
									{

										/*
										 * 		Prefixed with table name
										 */
										boolean flag = checkPartOfPredicate(token, selectedTableList);
										if(flag)
										{
											sortOrder.add(token);
											
											Column column = getColumn(token);
											sortColumnList.add(column);
											
											if(tokenizer.hasMoreElements())
											{
												token = tokenizer.nextToken();
												if(token.equalsIgnoreCase("D"))
												{
													/*
													 * 		Descending order
													 */
													sortOrder.add(token);
												}
												else if(token.equalsIgnoreCase(","))
												{
													continue;
												}
											}
										}
										else
										{
											throw new DbmsError("Invalid SELECT statement. Column " + token + " does not exist. '" + sql + "'.");
										}
									}
									else if(token.equalsIgnoreCase(","))
									{
										if(tokenizer.hasMoreElements())
										{
											continue;
										}
										else
										{
											/*
											 * 		SQL query has unwanted characters
											 */
											throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
										}
									}
									else
									{
										/*
										 * 		column name not prefixed with table name
										 */
										throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
									}
								}
								if(sortColumnList.size()>=5 )
								{
									/*
									 * 		More than 4 or less than 1 sort columns
									 */
									throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
								}
							}
							else
							{
								/*
								 * 		Query does not have "BY" keyword
								 */
								throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
							}
						}
						
						/*
						 * 		Semantic check is done
						 * 		Query execution starts here
						 * 
						 * 
						 * 		Query and Predicate evaluation starts here
						 */
						
						PlanTable planTable = new PlanTable();
						ArrayList<Index> indexList = new ArrayList<Index>();
						
						int queryBlockNo = 1; // Always 1

						char accessType = 'R'; // 'R' Table space scan, 'I' Index scan, 'N'

						int matchCols = 0; // Number of matching columns in INDEX
						String accessName = ""; // Name of index
						char indexOnly = 'N'; // 'Y' if index only access

						char prefetch = ' '; // Blank - no prefetch, 'S' sequential prefetch
						char sortC_orderBy = 'N'; // 'Y' if sort required

						int table1Card = 0; // Table 1 cardinality
						int table2Card = 0;  // Table 2 cardinality

						String leadTable = ""; // leading outer table in NLJ
						String innerTable = ""; // new inner table in NLJ
						
						if(selectedTableList.size()==1)
						{
							/*
							 * 		Single table query execution
							 */
							innerTable = "";
							leadTable = "";
							
							
							if(isWhereClauseUsed)
							{
								/*
								 * 		WHERE clause used
								 * 
								 * 		Filling the predicate table
								 */
								boolean inList = false; // true if predicate is an inlist

								boolean join = false; // true if join predicate, false if local predicate
								int sequence = 0;

								// Related to output table
								char type = ' '; // E (equal), R (range), I (IN list)

								int card1 = 0; // left column cardinality

								double ff1 = 0; // left column filter factor

								String description = ""; 
								
								String operator, lhs="", rhs="";
								ArrayList<Double> sequenceOrder = new ArrayList<Double>();
								
								table = selectedTableList.get(0);
								
								for(Predicate p : predicateList)
								{
									String predicateText = p.getText();
									sequence = -1;
									if(predicateText.indexOf("=")!=-1)
									{
										/*
										 * 		Equal predicate
										 */
										
										lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
										rhs = predicateText.substring(predicateText.indexOf("=")+2);
										operator = "=";
										
										type = 'E';
										inList = false;
										description = "";
										
										if(lhs.indexOf(table.getTableName())!=-1)
										{
											Column column = getColumn(lhs);
											card1 = column.getColCard();
											
											if(column.getColType().name().equalsIgnoreCase("INT"))
											{
												int value = Integer.parseInt(rhs);
												int hk = Integer.parseInt(column.getHiKey());
												int lk = Integer.parseInt(column.getLoKey());
												
												if(value < lk || value > hk)
												{
													sequence = 0;
													ff1 = 0;
												}
												else
												{
													ff1 = calculateFilterFactor(lhs, rhs, operator);
													sequenceOrder.add(ff1);
													sequence = -1;
												}
											}
											else if(column.getColType().name().equalsIgnoreCase("CHAR"))
											{
												if(rhs.compareToIgnoreCase(column.getHiKey())>0 || rhs.compareToIgnoreCase(column.getLoKey())<0)
												{
													sequence = 0;
													ff1 = 0;
												}
												else
												{
													ff1 = calculateFilterFactor(lhs, rhs, operator);
													sequenceOrder.add(ff1);
													sequence = -1;
												}
											}
										}
										else
										{
											/*
											 * 		LITERAL VALUES
											 */
											type = 'L';
											card1 = 0;
											lhs = lhs.substring(1);
											if(lhs.equalsIgnoreCase(rhs))
											{
												sequence = 0;
												ff1 = 1;
											}
											else
											{
												sequence = 0;
												ff1 = 0;
											}
										}
									}
									else if(predicateText.indexOf(">")!=-1)
									{
										/*
										 * 		Range predicate
										 */
										lhs = predicateText.substring(0, predicateText.indexOf(">"));
										rhs = predicateText.substring(predicateText.indexOf(">")+2);
										operator = ">";
										
										type = 'R';
										inList = false;
										description = "";
										
										if(lhs.indexOf(table.getTableName())!=-1)
										{
											Column column = getColumn(lhs);
											card1 = column.getColCard();
											if(column.getColType().name().equalsIgnoreCase("INT"))
											{
												int value = Integer.parseInt(rhs);
												int hk = Integer.parseInt(column.getHiKey());
												int lk = Integer.parseInt(column.getLoKey());
												
												if(value < lk || value > hk)
												{
													sequence = 0;
													ff1 = 0;
												}
												else
												{
													ff1 = calculateFilterFactor(lhs, rhs, operator);
													sequenceOrder.add(ff1);
													sequence = -1;
												}
											}
											else
											{
												if(rhs.compareToIgnoreCase(column.getHiKey())>0 || rhs.compareToIgnoreCase(column.getLoKey())<0)
												{
													sequence = 0;
													ff1 = 0;
												}
												else
												{
													ff1 = calculateFilterFactor(lhs, rhs, operator);
													sequenceOrder.add(ff1);
													sequence = -1;
												}
											}
										}
										else
										{
											/*
											 * 		LITERAL VALUE
											 */
											type = 'L';
											card1 = 0;
											if(lhs.contains("[0-9]+") && rhs.contains("[0-9]+"))
											{
												int l = Integer.parseInt(lhs);
												int r = Integer.parseInt(rhs);
												
												if(l == r)
												{
													sequence = 0;
													ff1 = 1;
												}
												else
												{
													sequence = 0;
													ff1 = 0;
												}
											}
											else if(lhs.contains("[a-zA-Z]+") && rhs.contains("[a-zA-z]+"))
											{
												if(lhs.equalsIgnoreCase(rhs))
												{
													sequence = 0;
													ff1 = 1;
												}
												else
												{
													sequence = 0;
													ff1 = 0;
												}
											}
											else
											{
												sequence = 0;
												ff1 = 0;
											}
										}
									}
									else if(predicateText.indexOf("<")!=-1)
									{
										/*
										 * 		Range predicate
										 */
										lhs = predicateText.substring(0, predicateText.indexOf("<"));
										rhs = predicateText.substring(predicateText.indexOf("<")+2);
										operator = "<";
										
										type = 'R';
										inList = false;
										description = "";
										
										if(lhs.indexOf(table.getTableName())!=-1)
										{
											Column column = getColumn(lhs);
											card1 = column.getColCard();
											if(column.getColType().name().equalsIgnoreCase("INT"))
											{
												int value = Integer.parseInt(rhs);
												int hk = Integer.parseInt(column.getHiKey());
												int lk = Integer.parseInt(column.getLoKey());
												
												if(value < lk || value > hk)
												{
													sequence = 0;
													ff1 = 0;
												}
												else
												{
													ff1 = calculateFilterFactor(lhs, rhs, operator);
													sequenceOrder.add(ff1);
													sequence = -1;
												}
											}
											else
											{
												if(rhs.compareToIgnoreCase(column.getHiKey())>0 || rhs.compareToIgnoreCase(column.getLoKey())<0)
												{
													sequence = 0;
													ff1 = 0;
												}
												else
												{
													ff1 = calculateFilterFactor(lhs, rhs, operator);
													sequenceOrder.add(ff1);
													sequence = -1;
												}
											}
										}
										else
										{
											/*
											 * 		LITERAL VALUE
											 */
											type = 'L';
											card1 = 0;
											if(lhs.contains("[0-9]+") && rhs.contains("[0-9]+"))
											{
												int l = Integer.parseInt(lhs);
												int r = Integer.parseInt(rhs);
												
												if(l == r)
												{
													sequence = 0;
													ff1 = 1;
												}
												else
												{
													sequence = 0;
													ff1 = 0;
												}
											}
											else if(lhs.contains("[a-zA-Z]+") && rhs.contains("[a-zA-z]+"))
											{
												if(lhs.equalsIgnoreCase(rhs))
												{
													sequence = 0;
													ff1 = 1;
												}
												else
												{
													sequence = 0;
													ff1 = 0;
												}
											}
											else
											{
												sequence = 0;
												ff1 = 0;
											}
										}
									}
									else if(predicateText.indexOf("IN (")!=-1)
									{
										/*
										 * 		IN-list predicate
										 */
										lhs = predicateText.substring(0, predicateText.indexOf("IN ("));
										rhs = predicateText.substring(predicateText.indexOf("IN (")+3);
										
										
										//		REVERSE IN LIST TRANSFORM
										String value = rhs.substring(rhs.indexOf("(")+1, rhs.indexOf(")"));
										
										if(value.contains(","))
										{
											description = "";
											operator = "I";
											type = 'I';
											ff1 = calculateFilterFactor(lhs, rhs, operator);
										}
										else
										{
											description = lhs + " = " + rhs.substring(rhs.indexOf("(")+1, rhs.indexOf(")"));
											operator = "=";
											type = 'E';
											ff1 = calculateFilterFactor(lhs, value, operator);
										}
										
										
										sequenceOrder.add(ff1);
										
										Column column = getColumn(lhs);
										card1 = column.getColCard();
										inList = true;
									}
									
									p.setCard1(card1);
									p.setFf1(ff1);
									p.setJoin(join);
									p.setType(type);
									p.setInList(inList);
									p.setSequence(sequence);
									p.setDescription(description);
								}
								
								/*
								 *  checking if predicates list contains both ORed and ANDed predicates
								 */
								for(String str : predicateArray)
								{
									if(str.equalsIgnoreCase("AND"))
										andPredicatePresent = true;
									if(str.equalsIgnoreCase("OR"))
										orPredicateUsed = true;
								}
								
								/*
								 * 		SIMPLE ERROR TESTING ON PREDICATES
								 */
								errorTesting(predicateList, andPredicatePresent, orPredicateUsed, predicateArray);
								
								
								if(andPredicatePresent && !orPredicateUsed)
								{
									/*
									 * 		ONLY AND PREDICATE
									 */
									/*
									 * 		INDEX AND PREDICATE EVALUATION
									 */
																
									boolean indexPhase = true;  //	true if matching phase else screening
									boolean isIndexable = true;
									
									HashMap<String, ArrayList<String>> matchingPredicatesMap = new HashMap<String, ArrayList<String>>(); 
									HashMap<String, ArrayList<String>> screeningPredictesMap = new HashMap<String, ArrayList<String>>();
									
									ArrayList<String> matchingPredicateList = new ArrayList<String>();
									ArrayList<String> screeningPredicateList = new ArrayList<String>();
									
									indexList = table.getIndexes();
									operator = "";
									
									if(!indexList.isEmpty())
									{
										for(Index index : indexList)
										{
											indexPhase = true;
											matchingPredicateList = new ArrayList<String>();
											screeningPredicateList = new ArrayList<String>();
											
											for(Index.IndexKeyDef indexKeyDef :  index.getIdxKey())
											{
												int colId = indexKeyDef.colId;
												isIndexable = false;
												
												for(Predicate p : predicateList)
												{
													if(p.getType() != 'L')
													{

														String predicateText = p.getText();
														
														if(predicateText.indexOf("=")!=-1)
														{
															lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
															operator = "=";
														}
														else if(predicateText.indexOf("<")!=-1)
														{
															lhs = predicateText.substring(0, predicateText.indexOf("<")-1);
															operator = "<";
														}
														else if(predicateText.indexOf(">")!=-1)
														{
															lhs = predicateText.substring(0, predicateText.indexOf(">")-1);
															operator = ">";
														}
														else if(predicateText.indexOf("IN (")!=-1)
														{
															lhs = predicateText.substring(0, predicateText.indexOf("IN (")-1);
															operator = "IN (";
														}
														
														Column column = getColumn(lhs);
														
														if(colId == column.getColId())
														{
															/*
															 * 		Predicate contains index column
															 */
															isIndexable = true;
															if(indexPhase)
															{
																/*
																 * 		matching predicates
																 * 
																 */
																if(operator.equalsIgnoreCase("=") || operator.equalsIgnoreCase("IN ("))
																{
																	matchingPredicateList.add(predicateText);
																}
																else if(operator.equalsIgnoreCase(">") || operator.equalsIgnoreCase("<"))
																{
																	matchingPredicateList.add(predicateText);
																	indexPhase = false;
																}
															}
															else
															{
																/*
																 * 		Screening predicates
																 */
																screeningPredicateList.add(predicateText);
															}
														}
													
													}
												}
												if(!isIndexable)
												{
													indexPhase = false;
												}
											}
											matchingPredicatesMap.put(index.getIdxName(), matchingPredicateList);
											screeningPredictesMap.put(index.getIdxName(), screeningPredicateList);
										}
										
										Map<String,Integer> temp1 = new HashMap<String,Integer>();
										Map<String,Integer> temp2 = new HashMap<String,Integer>();
										
										
										boolean matchingIndexUsed = false;
										boolean screeningIndexUsed = false;
										
										Object keySet[] = matchingPredicatesMap.keySet().toArray();
										for(Object obj : keySet)
										{
											int listSize = matchingPredicatesMap.get(String.valueOf(obj)).size();
											if(listSize!=0)
											{
												matchingIndexUsed = true;
												temp1.put(String.valueOf(obj), listSize);
											}
										}
										
										keySet = screeningPredictesMap.keySet().toArray();
										for(Object obj : keySet)
										{
											int listSize = screeningPredictesMap.get(String.valueOf(obj)).size();
											if(listSize!=0)
											{
												screeningIndexUsed = true;
												temp2.put(String.valueOf(obj), listSize);
											}
										}
										
										
										if(matchingIndexUsed)
										{
											/*
											 * 		predicate uses matching index only
											 */
											temp1 = sortMapByValue(temp1);
											keySet = temp1.keySet().toArray();
											
											int noOfMatchingPredicates = temp1.get(String.valueOf(keySet[keySet.length-1]));
											count = 0;
											for(Object obj : keySet)
											{
												int num = temp1.get(String.valueOf(obj));
												if(num==noOfMatchingPredicates)
													count++;
											}
											if(count==1)
											{
												accessType = 'I';
												accessName = String.valueOf(keySet[keySet.length-1]);
												
												Set<String> s = new HashSet<String>();
												for(String str : matchingPredicatesMap.get(accessName))
												{
													if(str.indexOf("=")!=-1)
													{
														lhs = str.substring(0, str.indexOf("=")-1);
													}
													else if(str.indexOf("<")!=-1)
													{
														lhs = str.substring(0, str.indexOf("<")-1);
													}
													else if(str.indexOf(">")!=-1)
													{
														lhs = str.substring(0, str.indexOf(">")-1);
													}
													else if(str.indexOf("IN (")!=-1)
													{
														lhs = str.substring(0, str.indexOf("IN (")-1);
													}
													s.add(lhs);
												}
												matchCols = s.size();
												table1Card = table.getTableCard();
												
												/*
												 * 		Checking whether index only access
												 */
												indexOnly = isIndexOnlyAccess(selectedColumnList, accessName, predicateList, table);
											}
											else
											{
												/*
												 * 		Tie between 2 or more matching predicates
												 */
												
												/*
												 * 		breaking tie with screening predicatesS
												 */
												Map<String, Integer> tmp = new HashMap<String, Integer>();
												keySet = temp1.keySet().toArray();
												for(Object obj : keySet)
												{
													int num = temp1.get(String.valueOf(obj));
													if(num == noOfMatchingPredicates)
													{
														if(temp2.containsKey(obj))
														{
															int noOfScreeningPredicates = temp2.get(String.valueOf(obj));
															tmp.put(String.valueOf(obj), noOfScreeningPredicates);
														}
													}
												}
												
												if(tmp.isEmpty())
												{
													/*
													 * 		THERE ARE NO SCREENING PREDICATES TO BREAK TIE
													 */

													/*
													 * 		Breaking tie with filter factor
													 */
													
													 keySet = matchingPredicatesMap.keySet().toArray();
													 Map<String, Double> map = new HashMap<String, Double>();
													 double ff = (double)table.getTableCard();
													 for(Object obj : keySet)
													 {
														 String indexName = String.valueOf(obj);
														 ArrayList<String> pList = matchingPredicatesMap.get(String.valueOf(obj));
														 ff = (double)table.getTableCard();
														 if(pList.size()==noOfMatchingPredicates)
														 {
															 for(String p : pList)
															 {
																 if(p.indexOf("=")!=-1)
																{
																	lhs = p.substring(0, p.indexOf("=")-1);
																	rhs = p.substring(p.indexOf("=")+2);
																	operator = "=";
																}
																else if(p.indexOf("<")!=-1)
																{
																	lhs = p.substring(0, p.indexOf("<")-1);
																	rhs = p.substring(p.indexOf("<")+2);
																	operator = "<";
																}
																else if(p.indexOf(">")!=-1)
																{
																	lhs = p.substring(0, p.indexOf(">")-1);
																	rhs = p.substring(p.indexOf(">")+2);
																	operator = ">";
																}
																else if(p.indexOf("IN (")!=-1)
																{
																	lhs = p.substring(0, p.indexOf("IN (")-1);
																	rhs = p.substring(p.indexOf("(")+1);
																	operator = "IN (";
																}
																
																 ff = ff * calculateFilterFactor(lhs, rhs, operator);
															 }
														 }
														 map.put(indexName, ff);
													 }
													 
													map = sortMapByValue(map);
													keySet = map.keySet().toArray();
													
													accessType = 'I';
													accessName = String.valueOf(keySet[0]);
													
													Set<String> s = new HashSet<String>();
													for(String str : matchingPredicatesMap.get(accessName))
													{
														if(str.indexOf("=")!=-1)
														{
															lhs = str.substring(0, str.indexOf("=")-1);
														}
														else if(str.indexOf("<")!=-1)
														{
															lhs = str.substring(0, str.indexOf("<")-1);
														}
														else if(str.indexOf(">")!=-1)
														{
															lhs = str.substring(0, str.indexOf(">")-1);
														}
														else if(str.indexOf("IN (")!=-1)
														{
															lhs = str.substring(0, str.indexOf("IN (")-1);
														}
														s.add(lhs);
													}
													matchCols = s.size();
													
													prefetch = ' ';
													table1Card = table.getTableCard();
													sortC_orderBy = 'N';
													
													/*
													 * 		Checking whether index only access
													 */
													indexOnly = isIndexOnlyAccess(selectedColumnList, accessName, predicateList, table);
												}
												else
												{
													/*
													 * 		TIE BREAKING BASED ON FILTER FACTOR
													 */
													tmp = sortMapByValue(tmp);
													keySet = tmp.keySet().toArray();
													int noOfScreeningPredicates = tmp.get(String.valueOf(keySet[keySet.length-1]));
													count = 0;
													
													for(Object obj : keySet)
													{
														int num = tmp.get(String.valueOf(obj));
														if(num == noOfScreeningPredicates)
														{
															count++;
														}
													}
													
													if(count==1)
													{
														accessType = 'I';
														accessName = String.valueOf(keySet[keySet.length-1]);
														
														Set<String> s = new HashSet<String>();
														for(String str : matchingPredicatesMap.get(accessName))
														{
															if(str.indexOf("=")!=-1)
															{
																lhs = str.substring(0, str.indexOf("=")-1);
															}
															else if(str.indexOf("<")!=-1)
															{
																lhs = str.substring(0, str.indexOf("<")-1);
															}
															else if(str.indexOf(">")!=-1)
															{
																lhs = str.substring(0, str.indexOf(">")-1);
															}
															else if(str.indexOf("IN (")!=-1)
															{
																lhs = str.substring(0, str.indexOf("IN (")-1);
															}
															s.add(lhs);
														}
														matchCols = s.size();
														
														prefetch = ' ';
														table1Card = table.getTableCard();
														sortC_orderBy = 'N';
													}
													else
													{
														/*
														 * 		Breaking tie with filter factor
														 */
														
														 keySet = matchingPredicatesMap.keySet().toArray();
														 Map<String, Double> map = new HashMap<String, Double>();
														 double ff = (double)table.getTableCard();
														 for(Object obj : keySet)
														 {
															 String indexName = String.valueOf(obj);
															 ArrayList<String> pList = matchingPredicatesMap.get(String.valueOf(obj));
															 ff = (double)table.getTableCard();
															 if(pList.size()==noOfMatchingPredicates)
															 {
																 for(String p : pList)
																 {
																	 if(p.indexOf("=")!=-1)
																	{
																		lhs = p.substring(0, p.indexOf("=")-1);
																		rhs = p.substring(p.indexOf("=")+2);
																		operator = "=";
																	}
																	else if(p.indexOf("<")!=-1)
																	{
																		lhs = p.substring(0, p.indexOf("<")-1);
																		rhs = p.substring(p.indexOf("<")+2);
																		operator = "<";
																	}
																	else if(p.indexOf(">")!=-1)
																	{
																		lhs = p.substring(0, p.indexOf(">")-1);
																		rhs = p.substring(p.indexOf(">")+2);
																		operator = ">";
																	}
																	else if(p.indexOf("IN (")!=-1)
																	{
																		lhs = p.substring(0, p.indexOf("IN (")-1);
																		rhs = p.substring(p.indexOf("(")+1);
																		operator = "IN (";
																	}
																	
																	 ff = ff * calculateFilterFactor(lhs, rhs, operator);
																 }
															 }
															 map.put(indexName, ff);
														 }
														 
														map = sortMapByValue(map);
														keySet = map.keySet().toArray();
														 
														accessType = 'I';
														accessName = String.valueOf(keySet[0]);
														
														Set<String> s = new HashSet<String>();
														for(String str : matchingPredicatesMap.get(accessName))
														{
															if(str.indexOf("=")!=-1)
															{
																lhs = str.substring(0, str.indexOf("=")-1);
															}
															else if(str.indexOf("<")!=-1)
															{
																lhs = str.substring(0, str.indexOf("<")-1);
															}
															else if(str.indexOf(">")!=-1)
															{
																lhs = str.substring(0, str.indexOf(">")-1);
															}
															else if(str.indexOf("IN (")!=-1)
															{
																lhs = str.substring(0, str.indexOf("IN (")-1);
															}
															s.add(lhs);
														}
														matchCols = s.size();
														
														prefetch = ' ';
														table1Card = table.getTableCard();
														sortC_orderBy = 'N';
													}
													
													
													/*
													 * 		Checking whether index only access
													 */
													indexOnly = isIndexOnlyAccess(selectedColumnList, accessName, predicateList, table);
												}
											}
										}
										else if(!matchingIndexUsed && screeningIndexUsed)
										{
											/*
											 * 		predicate uses screening index only
											 */
											temp2 = sortMapByValue(temp2);
											keySet = temp2.keySet().toArray();
											int maxNoScreeningPredicate = temp2.get(String.valueOf(keySet[keySet.length-1]));
											count = 0;
											
											for(Object obj : keySet)
											{
												int num = temp2.get(String.valueOf(obj));
												if(num == maxNoScreeningPredicate)
												{
													count++;
												}
											}
											
											if(count==1)
											{
												accessType = 'I';
												matchCols = 0;
												accessName = String.valueOf(keySet[keySet.length-1]);
												
												prefetch = ' ';
												sortC_orderBy = 'N';
												table1Card = table.getTableCard();
											}
											else
											{
												/*
												 * 		Break tie of 2 or more screening predicate with FF
												 */
												 keySet = screeningPredictesMap.keySet().toArray();
												 Map<String, Double> map = new HashMap<String, Double>();
												 double ff = (double)table.getTableCard();
												 for(Object obj : keySet)
												 {
													 String indexName = String.valueOf(obj);
													 ArrayList<String> pList = matchingPredicatesMap.get(String.valueOf(obj));
													 if(pList.size()==maxNoScreeningPredicate)
													 {
														 for(String p : pList)
														 {
															 if(p.indexOf("=")!=-1)
															{
																lhs = p.substring(0, p.indexOf("=")-1);
																rhs = p.substring(p.indexOf("=")+2);
																operator = "=";
															}
															else if(p.indexOf("<")!=-1)
															{
																lhs = p.substring(0, p.indexOf("<")-1);
																rhs = p.substring(p.indexOf("<")+2);
																operator = "<";
															}
															else if(p.indexOf(">")!=-1)
															{
																lhs = p.substring(0, p.indexOf(">")-1);
																rhs = p.substring(p.indexOf(">")+2);
																operator = ">";
															}
															else if(p.indexOf("IN (")!=-1)
															{
																lhs = p.substring(0, p.indexOf("IN (")-1);
																rhs = p.substring(p.indexOf("(")+1);
																operator = "IN (";
															}
														 }
													 }
													 map.put(indexName, ff);
												 }
												 
												map = sortMapByValue(map);
												keySet = map.keySet().toArray();
												 
												accessType = 'I';
												accessName = String.valueOf(keySet[keySet.length-1]);
												matchCols = 0;
												
												prefetch = ' ';
												table1Card = table.getTableCard();
												sortC_orderBy = 'N';
											}
											
											/*
											 * 		Checking whether index only access
											 */
											indexOnly = isIndexOnlyAccess(selectedColumnList, accessName, predicateList, table);
										}
										else if (!matchingIndexUsed && !screeningIndexUsed)
										{
											/*
											 * 		No predicate uses any index
											 */
											accessType = 'R';
											matchCols = 0;
											indexOnly = ' ';
											accessName = "";
											table1Card = table.getTableCard();
											prefetch = 'S';
										}
									}
									else
									{
										/*
										 * 		NO INDEX DEFINED ON TABLE
										 */
										
										//		FILLING PLAN TABLE
										accessType = 'R';
										matchCols = 0;
										
										accessName = "";
										indexOnly = ' ';
										
										prefetch = 'S';
										sortC_orderBy = 'N';
										
										table1Card = table.getTableCard();
									}
								}
								else if(andPredicatePresent && orPredicateUsed)
								{
									/*
									 * 		BOTH AND and OR PREDICATE
									 */ 
																		
									//		FILLING PLAN TABLE
									accessType = 'R';
									matchCols = 0;
									
									accessName = "";
									indexOnly = ' ';
									
									prefetch = 'S';
									sortC_orderBy = 'N';
									
									table1Card = table.getTableCard();
								}
								else if(!andPredicatePresent && orPredicateUsed)
								{
									/*
									 * 		ONLY OR PREDICATE
									 * 
									 */
									
									indexList = table.getIndexes();
									
									if(indexList.isEmpty())
									{
										/*
										 * 		NO INDEX DEFINED ON TABLE
										 */
										accessType = 'R';
										accessName = "";
										matchCols = 0;
										
										prefetch = 'S';
										indexOnly = ' ';
										sortC_orderBy = 'N';
										table1Card = table.getTableCard();
									}
									else
									{
										/*
										 * 		CHECKING WHETHER SAME COLUMN IS SEPARATED BY OR OPERATOR
										 */
										ArrayList<Predicate> inListTransformationList = new ArrayList<Predicate>();
										ArrayList<Predicate> remainingPredicateList = new ArrayList<Predicate>();
										
										for(i=0; i<predicateList.size(); i++)
										{
											Predicate p = predicateList.get(i);
											String text = p.getText();
											if(p.getType()=='E')
											{
												lhs = text.substring(0, text.indexOf("=")-1);
												
												for(j=i+1; j<predicateList.size(); j++)
												{
													Predicate p1 = predicateList.get(j);
													String text1 = p1.getText();
													
													if(p1.getType()=='E')
													{
														if(text1.indexOf(lhs)!=-1)
														{
															inListTransformationList.add(p1);
														}
														else
														{
															remainingPredicateList.add(p1);
														}
													}
													else
													{
														remainingPredicateList.add(p1);
													}
												}
												
												if(!inListTransformationList.isEmpty())
													inListTransformationList.add(p);
												break;
											}
										}
										
										
										if(!inListTransformationList.isEmpty())
										{
											/*
											 * 		OR PREDICATES TRANSFORMED TO IN-LIST PREDICATES
											 */
											boolean isIndexable = false;
											boolean indexPhase = false;
											
											int colId = getColumn(inListTransformationList.get(0).getText().substring(0, inListTransformationList.get(0).getText().indexOf("=")-1)).getColId();
											List<String> matchingPredicateList = new ArrayList<String>();
											List<String> screeningPredicateList = new ArrayList<String>();
											
											for(Index index : indexList)
											{
												indexPhase = true;
												for(Index.IndexKeyDef idk : index.getIdxKey())
												{
													isIndexable = false;
													if(colId == idk.colId)
													{
														isIndexable = true;
														if(indexPhase)
														{
															matchingPredicateList.add(index.getIdxName());
														}
														else
														{
															screeningPredicateList.add(index.getIdxName());
														}
													}
													
													if(!isIndexable)
													{
														indexPhase = false;
													}
												}
											}
											
											if(!matchingPredicateList.isEmpty())
											{
												/*
												 * 		MATCHING PREDICATE
												 */
												accessName = matchingPredicateList.get(0);
												accessType = 'N';
												matchCols = 1;
												prefetch = ' ';
												
												sequenceOrder = new ArrayList<Double>();
												
												table1Card = table.getTableCard();
												sortC_orderBy = 'N';
												
												String predicateText = "";
												description = predicateArray.get(0).substring(0, predicateArray.get(0).indexOf("=")) + " IN ( ";
												count = 0;
												for(Predicate p : inListTransformationList)
												{
													String str = p.getText();
													String str1 = str.substring(str.indexOf("=")+1);
													if(count==0)
													{
														predicateText = str;
														description = description + str1;
													}
													else
													{
														predicateText = predicateText + " OR " + str;
														description = description + ", " + str1;
													}
													count++;
												}
												
												description = description + " )";
												
												lhs = description.substring(0, description.indexOf("IN (")-1);
												rhs = description.substring(description.indexOf("("));
												operator = "I";
												double filterFactor = calculateFilterFactor(lhs, rhs, operator);
												Column column = getColumn(lhs);
												
												predicateList = new ArrayList<Predicate>();
												Predicate predicate = new Predicate();
												
												predicate.setCard1(column.getColCard());
												predicate.setDescription(description);
												predicate.setFf1(filterFactor);
												predicate.setInList(true);
												predicate.setText(predicateText);
												predicate.setType('I');
												predicate.setSequence(-1);
												sequenceOrder.add(filterFactor);
												
												
												predicateList.add(predicate);
												
												for(Predicate p : remainingPredicateList)
												{
													predicateList.add(p);
													p.setSequence(-1);
													sequenceOrder.add(p.ff1);
												}
												
												/*
												 * 		Checking whether index only access
												 */
												indexOnly = isIndexOnlyAccess(selectedColumnList, accessName, predicateList, table);
											}
											else if(!screeningPredicateList.isEmpty())
											{
												/*
												 * 		SCREENING PREDICATES
												 */
												accessName = matchingPredicateList.get(0);
												accessType = 'N';
												matchCols = 1;
												prefetch = ' ';
												
												table1Card = table.getTableCard();
												sortC_orderBy = 'N';
												
												/*
												 * 		Checking whether index only access
												 */
												indexOnly = isIndexOnlyAccess(selectedColumnList, accessName, predicateList, table);
											}
											else
											{
												/*
												 * 		RELATIONAL SCAN
												 */
												accessType = 'R';
												accessName = "";
												matchCols = 0;
												
												sortC_orderBy = 'N';
												table1Card = table.getTableCard();
												prefetch = 'S';
												indexOnly = ' ';
											}
										}
										else
										{
											/*
											 * 		DIFFERENT COLUMNS SEPARATED BY OR OPERATOR
											 */
											
											accessType = 'R';
											accessName = "";
											matchCols = 0;
											
											sortC_orderBy = 'N';
											table1Card = table.getTableCard();
											prefetch = 'S';
											indexOnly = ' ';
										}
									}
								}
								else if(!andPredicatePresent && !orPredicateUsed)
								{
									/*
									 * 		SINGLE PREDICATE
									 */
									indexList = table.getIndexes();
									
									if(indexList.isEmpty())
									{
										/*
										 * 		NO INDEXES ON TABLE
										 */
										
										accessType = 'R';
										accessName = "";
										prefetch = 'S';
										matchCols = 0;
										indexOnly = ' ';
										table1Card = table.getTableCard();
										sortC_orderBy = 'N';
									}
									else
									{
										ArrayList<String> matchingPredicateList = new  ArrayList<String>();
										ArrayList<String> screeningPredicateList = new  ArrayList<String>();
										boolean indexPhase = true;
										
										Predicate predicate = predicateList.get(0);
										String predicateText = predicate.getText();
										operator = "";
										if(predicateText.indexOf("=")!=-1)
										{
											lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
											operator = "=";
										}
										else if(predicateText.indexOf("<")!=-1)
										{
											lhs = predicateText.substring(0, predicateText.indexOf("<")-1);
											operator = "<";
										}
										else if(predicateText.indexOf(">")!=-1)
										{
											lhs = predicateText.substring(0, predicateText.indexOf(">")-1);
											operator = ">";
										}
										else if(predicateText.indexOf("IN (")!=-1)
										{
											lhs = predicateText.substring(0, predicateText.indexOf("IN (")-1);
											operator = "IN (";
										}
										
										int colId = getColumn(lhs).getColId();
										boolean isIndexable = false;
										
										for(Index index : indexList)
										{
											indexPhase = true;
											for(Index.IndexKeyDef idk : index.getIdxKey())
											{
												isIndexable = false;
												if(colId == idk.colId)
												{
													isIndexable = true;
													if(indexPhase)
													{
														if(operator.equalsIgnoreCase("=") || operator.equalsIgnoreCase("IN ("))
														{
															matchingPredicateList.add(index.getIdxName());
														}
														else if(operator.equalsIgnoreCase(">") || operator.equalsIgnoreCase("<"))
														{
															matchingPredicateList.add(index.getIdxName());
															indexPhase = false;
														}
													}
													else
													{
														screeningPredicateList.add(index.getIdxName());
													}
												}
												
												if(!isIndexable)
												{
													indexPhase = false;
												}
											}
										}
										if(!matchingPredicateList.isEmpty())
										{
											/*
											 * 		MATCHING INDEX IS USED
											 */
											Map<String, Integer> temp = new HashMap<String, Integer>();
											if(selectedColumnList.contains("*"))
											{
												for(String str : matchingPredicateList)
												{
													Index index = new Index("");
													count = 0;
													
													for(Index ind : table.getIndexes())
													{
														if(ind.getIdxName().equalsIgnoreCase(str))
														{
															index = ind;
															break;
														}
													}
													
													for(Column c : table.getColumns())
													{
														colId = c.getColId();
														for(Index.IndexKeyDef ikd : index.getIdxKey())
														 {
															 if(colId == ikd.colId)
																 count++;
														 }
													}
													
													temp.put(str, count);
												}
											}
											else
											{
												
												for(String str : matchingPredicateList)
												{
													Index index = new Index("");
													count = 0;
													
													for(Index ind : table.getIndexes())
													{
														if(ind.getIdxName().equalsIgnoreCase(str))
														{
															index = ind;
															break;
														}
													}
													
													for(String c : selectedColumnList)
													{
														Column col = getColumn(c);
														colId = col.getColId();
														for(Index.IndexKeyDef ikd : index.getIdxKey())
														 {
															 if(colId == ikd.colId)
																 count++;
														 }
													}
													
													temp.put(str, count);
												}
											}
											
											temp = sortMapByValue(temp);
											Object[] keySet = temp.keySet().toArray();
											count = temp.get(String.valueOf(keySet[keySet.length-1]));
											
											matchCols = 1;
											accessType = 'I';
											accessName = String.valueOf(keySet[keySet.length-1]);
											indexOnly = isIndexOnlyAccess(selectedColumnList, accessName, predicateList, table);
											
											prefetch = ' ';
											table1Card = table.getTableCard();
											sortC_orderBy = 'N';
										}
										else if(!screeningPredicateList.isEmpty())
										{
											/*
											 * 		SCREENING INDEX IS USED	
											 */
											
											Map<String, Integer> temp = new HashMap<String, Integer>();
											if(selectedColumnList.contains("*"))
											{
												for(String str : screeningPredicateList)
												{
													Index index = new Index("");
													count = 0;
													
													for(Index ind : table.getIndexes())
													{
														if(ind.getIdxName().equalsIgnoreCase(str))
														{
															index = ind;
															break;
														}
													}
													
													for(Column c : table.getColumns())
													{
														colId = c.getColId();
														for(Index.IndexKeyDef ikd : index.getIdxKey())
														 {
															 if(colId == ikd.colId)
																 count++;
														 }
													}
													
													temp.put(str, count);
												}
											}
											else
											{
												
												for(String str : screeningPredicateList)
												{
													Index index = new Index("");
													count = 0;
													
													for(Index ind : table.getIndexes())
													{
														if(ind.getIdxName().equalsIgnoreCase(str))
														{
															index = ind;
															break;
														}
													}
													
													for(String c : selectedColumnList)
													{
														Column col = getColumn(c);
														colId = col.getColId();
														for(Index.IndexKeyDef ikd : index.getIdxKey())
														 {
															 if(colId == ikd.colId)
																 count++;
														 }
													}
													
													temp.put(str, count);
												}
											}
											
											temp = sortMapByValue(temp);
											Object[] keySet = temp.keySet().toArray();
											count = temp.get(String.valueOf(keySet[keySet.length-1]));
											
											
											matchCols = 0;
											accessType = 'I';
											accessName = String.valueOf(keySet[keySet.length-1]);
											indexOnly = isIndexOnlyAccess(selectedColumnList, accessName, predicateList, table);
											
											prefetch = ' ';
											table1Card = table.getTableCard();
											sortC_orderBy = 'N';
										}
										else
										{
											/*
											 * 		NO INDEX IS USED
											 */
											
											accessType = 'R';
											accessName = "";
											matchCols = 0;
											
											sortC_orderBy = 'N';
											indexOnly = ' ';
											prefetch = 'S';
											table1Card = table.getTableCard();
										}
									}
								}
								
								/*
								 * 		SET PREDICATE SEQUENCE
								 */
								setPredicateSequence(sequenceOrder, predicateList);
								
								if(isOrderByClauseUsed)
								{
									/*
									 * 		ORDER BY clause used
									 *		
									 */
									Index index = new Index("");
									for(Index ind : table.getIndexes())
									{
										if(ind.getIdxName().equalsIgnoreCase(accessName))
										{
											index = ind;
											break;
										}
									}
									
									ArrayList<Index.IndexKeyDef> list = index.getIdxKey();
									boolean isDescOrder = false;	// true if order is opposite of index
									boolean indexUsedForOrderBy = true;
									boolean descOrder = false;
									
									for(i=0; i<sortColumnList.size(); i++)
									{
										Index.IndexKeyDef indexKeyDef = list.get(i);
										Column column = sortColumnList.get(i);
										String sortColumn = sortOrder.get(i); 
										
										if(sortColumn.contains(" D "))
											descOrder = true;
										else
											descOrder = false;
										
										if(column.getColId()==indexKeyDef.colId)
										{
											if(i==0)
											{
												if(descOrder==indexKeyDef.descOrder)
												{
													isDescOrder = false;
												}
												else
												{
													isDescOrder = true;
												}
											}
											else
											{
												if(isDescOrder)
												{
													if(descOrder)
														descOrder = false;
													else
														descOrder = true;
													
													if(descOrder!=indexKeyDef.descOrder)
													{
														indexUsedForOrderBy = false;
													}
												}
												else
												{
													if(descOrder!=indexKeyDef.descOrder)
													{
														indexUsedForOrderBy = false;
													}
												}
											}
										}
										else
										{
											indexUsedForOrderBy = false;
											break;
										}
									}
									
									if(indexUsedForOrderBy)
									{
										sortC_orderBy = 'N';
									}
									else
									{
										sortC_orderBy = 'Y';
									}
								}
							}
							else
							{
								/*
								 * 		NO WHERE clause used
								 */
								table = selectedTableList.get(0);
								indexList = table.getIndexes();
								if(isOrderByClauseUsed)
								{
									/*
									 * 		ORDER BY clause used
									 *		
									 */
									boolean isDescOrder = false;	// true if order is opposite of index
									boolean indexUsedForOrderBy = true;
									boolean descOrder = false;
									ArrayList<Index> idxList = new ArrayList<Index>();
									
									for(Index idx : indexList)
									{
										ArrayList<Index.IndexKeyDef> list = idx.getIdxKey();
										isDescOrder = false;
										indexUsedForOrderBy = true;
										descOrder = false;
										
										for(i=0; i<sortColumnList.size(); i++)
										{
											Index.IndexKeyDef indexKeyDef = list.get(i);
											Column column = sortColumnList.get(i);
											String sortColumn = sortOrder.get(i); 
											
											
											if(sortColumn.contains(" D "))
												descOrder = true;
											else
												descOrder = false;
											
											if(column.getColId()==indexKeyDef.colId)
											{
												if(i==0)
												{
													if(descOrder==indexKeyDef.descOrder)
													{
														isDescOrder = false;
													}
													else
													{
														isDescOrder = true;
													}
												}
												else
												{
													if(isDescOrder)
													{
														if(descOrder)
															descOrder = false;
														else
															descOrder = true;
														
														if(descOrder!=indexKeyDef.descOrder)
														{
															indexUsedForOrderBy = false;
															break;
														}
													}
													else
													{
														if(descOrder!=indexKeyDef.descOrder)
														{
															indexUsedForOrderBy = false;
															break;
														}
													}
												}
											}
											else
											{
												indexUsedForOrderBy = false;
												break;
											}
										}
										
										if(indexUsedForOrderBy)
										{
											idxList.add(idx);
										}
									}
									
									if(idxList.isEmpty())
									{
										/*
										 * 		NO INDEX USED TO AVOID SORT
										 */
										Map<String, Integer> map = new HashMap<String, Integer>();
										
										for(Index index : table.getIndexes())
										{
											count = 0;
											
											for(Index.IndexKeyDef indexKeyDef : index.getIdxKey())
											{
												for(String cName : selectedColumnList)
												{
													Column column = getColumn(cName);
													if(column.getColId() == indexKeyDef.colId)
													{
														count++;
													}
												}
											}
										}
										
										if(map.isEmpty())
										{
											/*
											 * 		INDEX IS NOT USED TO EXECUTE QUERY
											 */
											
											accessName = "";
											accessType = 'R';
											matchCols = 0;
											prefetch = 'S';
											sortC_orderBy = 'Y';
											indexOnly = ' ';
											table1Card = table.getTableCard();
										}
										else
										{
											/*
											 * 		INDEX IS USED TO EXECUTE QUERY
											 */
											
											map = sortMapByValue(map);
											Object[] keySet = map.keySet().toArray();
											
											count = map.get(String.valueOf(keySet[keySet.length-1]));
											
											if(count == selectedColumnList.size())
											{
												indexOnly = 'Y';
											}
											else
											{
												indexOnly = 'N';
											}
											
											accessType = 'I';
											matchCols = 0;
											prefetch = ' ';
											accessName = String.valueOf(keySet[keySet.length-1]);
											sortC_orderBy = 'N';
											table1Card = table.getTableCard();
										}
									
									}
									else
									{
										Index idx = idxList.get(0);
										
										accessName = idx.getIdxName();
										accessType = 'I';
										prefetch = ' ';
										sortC_orderBy = 'N';
										matchCols = sortColumnList.size();
										table1Card = table.getTableCard();
										
										indexOnly = isIndexOnlyAccess(selectedColumnList, accessName, predicateList, table);
									}
								}
								else
								{
									/*
									 * 		NO ORDER BY clause used
									 */
									
									Map<String, Integer> map = new HashMap<String, Integer>();
									
									for(Index index : table.getIndexes())
									{
										count = 0;
										
										for(Index.IndexKeyDef indexKeyDef : index.getIdxKey())
										{
											for(String cName : selectedColumnList)
											{
												Column column = getColumn(cName);
												if(column.getColId() == indexKeyDef.colId)
												{
													count++;
												}
											}
										}
									}
									
									if(map.isEmpty())
									{
										/*
										 * 		INDEX IS NOT USED TO EXECUTE QUERY
										 */
										
										accessName = "";
										accessType = 'R';
										matchCols = 0;
										prefetch = 'S';
										sortC_orderBy = 'N';
										indexOnly = ' ';
										table1Card = table.getTableCard();
									}
									else
									{
										/*
										 * 		INDEX IS USED TO EXECUTE QUERY
										 */
										
										map = sortMapByValue(map);
										Object[] keySet = map.keySet().toArray();
										
										count = map.get(String.valueOf(keySet[keySet.length-1]));
										
										if(count == selectedColumnList.size())
										{
											indexOnly = 'Y';
										}
										else
										{
											indexOnly = 'N';
										}
										
										accessType = 'I';
										matchCols = 0;
										prefetch = ' ';
										accessName = String.valueOf(keySet[keySet.length-1]);
										sortC_orderBy = 'N';
										table1Card = table.getTableCard();
									}
								}
							}
						}
						else if(selectedTableList.size()==2)
						{
							/*
							 * 		Two tables join query execution
							 */
							
							Table table1 = selectedTableList.get(0);
							Table table2 = selectedTableList.get(1);
							
							table1Card = table1.getTableCard();
							table2Card = table2.getTableCard();
							
							if(isWhereClauseUsed)
							{
								/*
								 * 		WHERE CLAUSE IS USED
								 */
								
								/*
								 * 		FILLING PREDICATE TABLE
								 */
								
								boolean inList = false; // true if predicate is an inlist

								boolean join = false; // true if join predicate, false if local predicate

								char type = ' '; // E (equal), R (range), I (IN list)

								int card1 = 0; // left column cardinality
								int card2 = 0; // right column cardinality

								double ff1 = 0; // left column filter factor
								double ff2 = 0; // right column filter factor

								int sequence = -1; // order of predicate evaluation

								String text = ""; // original text
								String description = ""; // description for added predicates and
								
								String rhs = "", lhs = "", operator;
								Column column;
								
								ArrayList<Index> indexList1 = table1.getIndexes();
								ArrayList<Index> indexList2 = table2.getIndexes();
								
								Map<String, ArrayList<String>> matchingPredicatesMap1 = new HashMap<String, ArrayList<String>>();
								Map<String, ArrayList<String>> screeningPredicatesMap1 = new HashMap<String, ArrayList<String>>();
								
								Map<String, ArrayList<String>> matchingPredicatesMap2 = new HashMap<String, ArrayList<String>>();
								Map<String, ArrayList<String>> screeningPredicatesMap2 = new HashMap<String, ArrayList<String>>();
								
								ArrayList<String> matchingPredicateList1 = new ArrayList<String>();
								ArrayList<String> screeningPredicateList1 = new ArrayList<String>();
								ArrayList<String> matchingPredicateList2 = new ArrayList<String>();
								ArrayList<String> screeningPredicateList2 = new ArrayList<String>();
								
								boolean isLocalPredicateUsed = false;
								
								for(Predicate predicate : predicateList)
								{
									String predicateText = predicate.getText();
									
									if(predicateText.indexOf("=")!=-1)
									{
										/*
										 * 		EQUAL PREDICATE
										 */
										
										lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
										rhs = predicateText.substring(predicateText.indexOf("=")+2);
										
										type = 'E';
										operator = "=";
										join = false;
										
										ff1 = calculateFilterFactor(lhs, rhs, operator);
										ff2 = 0;
										
										column = getColumn(lhs);
										card1 = column.getColCard();
										card2 = 0;
										
										description = "";
										inList = false;
										
										if(rhs.indexOf(table1.getTableName())!=-1 || rhs.indexOf(table2.getTableName())!=-1)
										{
											ff2 = calculateFilterFactor(rhs, lhs, operator);
											join = true;
											column = getColumn(rhs);
											card2 = column.getColCard();
										}
										else if(lhs.indexOf(table1.getTableName())!=-1 || lhs.indexOf(table2.getTableName())!=-1)
										{
											column = getColumn(lhs);
											card1 = column.getColCard();
											
											if(column.getColType().name().equalsIgnoreCase("INT"))
											{
												int value = Integer.parseInt(rhs);
												int hk = Integer.parseInt(column.getHiKey());
												int lk = Integer.parseInt(column.getLoKey());
												
												if(value < lk || value > hk)
												{
													sequence = 0;
													ff1 = 0;
												}
												else
												{
													ff1 = calculateFilterFactor(lhs, rhs, operator);
													sequence = -1;
												}
											}
											else if(column.getColType().name().equalsIgnoreCase("CHAR"))
											{
												if(rhs.compareToIgnoreCase(column.getHiKey())>0 || rhs.compareToIgnoreCase(column.getLoKey())<0)
												{
													sequence = 0;
													ff1 = 0;
												}
												else
												{
													ff1 = calculateFilterFactor(lhs, rhs, operator);
													sequence = -1;
												}
											}
										}
										else
										{
											/*
											 * 		LITERAL VALUES
											 */
											type = 'L';
											card1 = 0;
											card2 = 0;
											if(lhs.equalsIgnoreCase(rhs))
											{
												sequence = 0;
												ff1 = 1;
											}
											else
											{
												sequence = 0;
												ff1 = 0;
											}
										}
									}
									else if(predicateText.indexOf("<")!=-1)
									{
										/*
										 * 		RANGE PREDICATE	:	<
										 */
										lhs = predicateText.substring(0, predicateText.indexOf("<")-1);
										rhs = predicateText.substring(predicateText.indexOf("<")+2);
										operator = "<";
										
										type = 'R';
										join = false;
										
										ff2 = 0;
										sequence = -1;
										
										column = getColumn(lhs);
										card1 = column.getColCard();
										card2 = 0;
										
										description = "";
										inList = false;
										
										if(lhs.indexOf(table1.getTableName())!=-1 || lhs.indexOf(table2.getTableName())!=-1)
										{
											column = getColumn(lhs);
											card1 = column.getColCard();
											if(column.getColType().name().equalsIgnoreCase("INT"))
											{
												int value = Integer.parseInt(rhs);
												int hk = Integer.parseInt(column.getHiKey());
												int lk = Integer.parseInt(column.getLoKey());
												
												if(value < lk || value > hk)
												{
													sequence = 0;
													ff1 = 0;
												}
												else
												{
													ff1 = calculateFilterFactor(lhs, rhs, operator);
													sequence = -1;
												}
											}
											else
											{
												if(rhs.compareToIgnoreCase(column.getHiKey())>0 || rhs.compareToIgnoreCase(column.getLoKey())<0)
												{
													sequence = 0;
													ff1 = 0;
												}
												else
												{
													ff1 = calculateFilterFactor(lhs, rhs, operator);
													sequence = -1;
												}
											}
										}
										else
										{
											/*
											 * 		LITERAL VALUE
											 */
											type = 'L';
											card1 = 0;
											card2 = 0;
											if(lhs.contains("[0-9]+") && rhs.contains("[0-9]+"))
											{
												int l = Integer.parseInt(lhs);
												int r = Integer.parseInt(rhs);
												
												if(l == r)
												{
													sequence = 0;
													ff1 = 1;
												}
												else
												{
													sequence = 0;
													ff1 = 0;
												}
											}
											else if(lhs.contains("[a-zA-Z]+") && rhs.contains("[a-zA-z]+"))
											{
												if(lhs.equalsIgnoreCase(rhs))
												{
													sequence = 0;
													ff1 = 1;
												}
												else
												{
													sequence = 0;
													ff1 = 0;
												}
											}
											else
											{
												sequence = 0;
												ff1 = 0;
											}
										}
									}
									else if(predicateText.indexOf(">")!=-1)
									{
										/*
										 * 		RANGE PREDICATE	:	>
										 */
										
										lhs = predicateText.substring(0, predicateText.indexOf(">")-1);
										rhs = predicateText.substring(predicateText.indexOf(">")+2);
										operator = ">";
										
										type = 'R';
										operator = ">";
										join = false;
										
										ff2 = 0;
										sequence = -1;
										
										column = getColumn(lhs);
										card1 = column.getColCard();
										card2 = 0;
										
										description = "";
										inList = false;
										
										if(lhs.indexOf(table1.getTableName())!=-1 || lhs.indexOf(table2.getTableName())!=-1)
										{
											column = getColumn(lhs);
											card1 = column.getColCard();
											if(column.getColType().name().equalsIgnoreCase("INT"))
											{
												int value = Integer.parseInt(rhs);
												int hk = Integer.parseInt(column.getHiKey());
												int lk = Integer.parseInt(column.getLoKey());
												
												if(value < lk || value > hk)
												{
													sequence = 0;
													ff1 = 0;
												}
												else
												{
													ff1 = calculateFilterFactor(lhs, rhs, operator);
													sequence = -1;
												}
											}
											else
											{
												if(rhs.compareToIgnoreCase(column.getHiKey())>0 || rhs.compareToIgnoreCase(column.getLoKey())<0)
												{
													sequence = 0;
													ff1 = 0;
												}
												else
												{
													ff1 = calculateFilterFactor(lhs, rhs, operator);
													sequence = -1;
												}
											}
										}
										else
										{
											/*
											 * 		LITERAL VALUE
											 */
											type = 'L';
											card1 = 0;
											card2 = 0;
											if(lhs.contains("[0-9]+") && rhs.contains("[0-9]+"))
											{
												int l = Integer.parseInt(lhs);
												int r = Integer.parseInt(rhs);
												
												if(l == r)
												{
													sequence = 0;
													ff1 = 1;
												}
												else
												{
													sequence = 0;
													ff1 = 0;
												}
											}
											else if(lhs.contains("[a-zA-Z]+") && rhs.contains("[a-zA-z]+"))
											{
												if(lhs.equalsIgnoreCase(rhs))
												{
													sequence = 0;
													ff1 = 1;
												}
												else
												{
													sequence = 0;
													ff1 = 0;
												}
											}
											else
											{
												sequence = 0;
												ff1 = 0;
											}
										}
									}
									else if(predicateText.indexOf("IN (")!=-1)
									{
										/*
										 * 		IN-LIST PREDICATE
										 */
										
										lhs = predicateText.substring(0, predicateText.indexOf("IN (")-1);
										rhs = predicateText.substring(predicateText.indexOf("("));
										operator = "I";
										
										inList = true;
										join = false;
										
										ff2 = 0;
										
										column = getColumn(lhs);
										card1 = column.getColCard();
										card2 = 0;
										
										description = "";
										operator = "I";
										type = 'I';
										ff1 = calculateFilterFactor(lhs, rhs, operator);
										
									}
									
									predicate.setCard1(card1);
									predicate.setCard2(card2);
									predicate.setSequence(sequence);
									
									predicate.setDescription(description);
									predicate.setText(predicateText);
									
									predicate.setType(type);
									predicate.setInList(inList);
									predicate.setJoin(join);
									
									predicate.setFf1(ff1);
									predicate.setFf2(ff2);
								}
								
								
								/*
								 * 		SETTING PREDICATE TRANSITIVE CLOSURE
								 */
								ArrayList<Predicate> TCPPredicateList = new ArrayList<Predicate>();
								Predicate p = new Predicate();
								for(Predicate predicate : predicateList)
								{
									if(predicate.isJoin())
									{
										p = predicate;
										break;
									}
								}
								String predicateText = p.getText();
								lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
								rhs = predicateText.substring(predicateText.indexOf("=")+2);
								
								for(Predicate predicate : predicateList)
								{
									if(!predicate.isJoin())
									{
										String txt = predicate.getText();
										String l = "", r = "";
										operator = "";
										
										if(txt.indexOf("=")!=-1)
										{
											/*
											 * 		EQUAL OPERATOR
											 */
											l = txt.substring(0, txt.indexOf("=")-1);
											r = txt.substring(txt.indexOf("=")+2);
											operator = "=";
										}
										else if(txt.indexOf(">")!=-1)
										{
											/*
											 * 		RANGE PREDICATE	:	>
											 */
											l = txt.substring(0, txt.indexOf(">")-1);
											r = txt.substring(txt.indexOf(">")+2);
											operator = ">";
										}
										else if(txt.indexOf("<")!=-1)
										{
											/*
											 * 		RANGE PREDICATE	:	<
											 */
											l = txt.substring(0, txt.indexOf("<")-1);
											r = txt.substring(txt.indexOf("<")+2);
											operator = "<";
										}
										
										if(l.equalsIgnoreCase(rhs))
										{
											Predicate pr = new Predicate();
											
											description ="TCP";
											sequence = 0;
											
											type = predicate.getType();
											join = false;
											inList = false;
											
											text = lhs + " " + operator + " " + r;
											ff1 = calculateFilterFactor(lhs, r, operator);
											ff2 = 0;
											
											column = getColumn(lhs);
											card1 = column.getColCard();
											card2 = 0;
											
											pr.setCard1(card1);
											pr.setCard2(card2);
											
											pr.setDescription(description);
											pr.setText(text);
											
											pr.setFf1(ff1);
											pr.setFf2(ff2);
											
											pr.setType(type);
											pr.setSequence(sequence);
											
											pr.setInList(inList);
											pr.setJoin(join);
											
											TCPPredicateList.add(pr);
										}
										else if(l.equalsIgnoreCase(lhs))
										{
											Predicate pr = new Predicate();
											
											description ="TCP";
											sequence = 0;
											
											type = predicate.getType();
											join = false;
											inList = false;
											
											text = rhs + " " + operator + " " + r;
											ff1 = calculateFilterFactor(rhs, r, operator);
											ff2 = 0;
											
											column = getColumn(rhs);
											card1 = column.getColCard();
											card2 = 0;
											
											pr.setCard1(card1);
											pr.setCard2(card2);
											
											pr.setDescription(description);
											pr.setText(text);
											
											pr.setFf1(ff1);
											pr.setFf2(ff2);
											
											pr.setType(type);
											pr.setSequence(sequence);
											
											pr.setInList(inList);
											pr.setJoin(join);
											
											TCPPredicateList.add(pr);
										}
									}
								}
								
								andPredicatePresent = false;
								orPredicateUsed = false;
								for(String str : predicateArray)
								{
									if(str.equalsIgnoreCase("AND"))
										andPredicatePresent = true;
									if(str.equalsIgnoreCase("OR"))
										orPredicateUsed = true;
								}
								errorTesting(predicateList, andPredicatePresent, orPredicateUsed, predicateArray);
								
								/*
								 * 		CHECK LOCAL PREDICATES ARE USED
								 */
								if(predicateList.size()==1)
								{
									isLocalPredicateUsed = false;
								}
								else
								{
									isLocalPredicateUsed = true;
								}
								
								
								if(isLocalPredicateUsed)
								{
									/*
									 * 		LOCAL PREDICATES ARE USED
									 */
									andPredicatePresent = false;
									orPredicateUsed = false;
									
									for(String str : predicateArray)
									{
										if(str.equalsIgnoreCase("AND"))
											andPredicatePresent = true;
										if(str.equalsIgnoreCase("OR"))
											orPredicateUsed = true;
									}
									
									/*
									 * 		ONLY PREDICATES SEPARATED BY AND
									 */
									if(indexList1.isEmpty() && indexList2.isEmpty())
									{
										/*
										 * 		NO INDEX ON BOTH TABLES
										 */
										
										accessType = 'R';
										accessName = "";
										matchCols = 0;
										indexOnly = ' ';
										prefetch = 'S';
										sortC_orderBy = 'N';
										
										double filterFactor1 = table1.getTableCard(), filterFactor2 = table2.getTableCard();
										operator = "";
										
										/*
										 * 		CALCULATING FILTER FACTORS OF BOTH TABLES
										 */
										for(Predicate predicate : predicateList)
										{
											predicateText = predicate.getText();
											
											if(predicate.isJoin())
											{
												lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
												rhs = predicateText.substring(predicateText.indexOf("=")+2);
												operator = "=";
												
												if(lhs.indexOf(table1.getTableName())!=-1)
												{
													filterFactor1 *= calculateFilterFactor(lhs, rhs, operator); 
													filterFactor2 *= calculateFilterFactor(rhs, lhs, operator);
												}
												else
												{
													filterFactor2 *= calculateFilterFactor(lhs, rhs, operator); 
													filterFactor1 *= calculateFilterFactor(rhs, lhs, operator);
												}
											}
											else
											{
												if(predicateText.indexOf("=")!=-1)
												{
													lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
													rhs = predicateText.substring(predicateText.indexOf("=")+2);
													operator = "=";
												}
												else if(predicateText.indexOf("<")!=-1)
												{
													lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
													rhs = predicateText.substring(predicateText.indexOf("=")+2);
													operator = "<";
												}
												else if(predicateText.indexOf(">")!=-1)
												{
													lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
													rhs = predicateText.substring(predicateText.indexOf("=")+2);
													operator = ">";
												}
												else if(predicateText.indexOf("IN (")!=-1)
												{
													lhs = predicateText.substring(0, predicateText.indexOf("IN (")-1);
													rhs = predicateText.substring(predicateText.indexOf("("));
													operator = "I";
												}
												
												if(predicate.getDescription().isEmpty())
												{
													/*
													 * 		CALCULATE FILTER FACTOR OF THOSE PREEDICATES WHICH ARE NOT TCP
													 */
													if(lhs.indexOf(table1.getTableName())!=-1)
													{
														filterFactor1 *= calculateFilterFactor(lhs, rhs, operator); 
													}
													else
													{
														filterFactor2 *= calculateFilterFactor(lhs, rhs, operator);
													}
												}
											}
										}
										
										/*
										 * 		DECIDING LEADING TABLE
										 */
										if(filterFactor1>filterFactor2)
										{
											leadTable = table1.getTableName();
											innerTable = table2.getTableName();
										}
										else
										{
											leadTable = table2.getTableName();
											innerTable = table1.getTableName();
										}
										
										/*
										 * 		DECIDING PREDICATE SEQUENCE
										 * 
										 */
										decidePredicateSequence(predicateList, TCPPredicateList, leadTable, innerTable);
									}
									else if(!indexList1.isEmpty() && !indexList2.isEmpty())
									{
										/*
										 * 		BOTH TABLES HAVE INDEXES
										 */
										matchingPredicateList1 = new ArrayList<String>();
										matchingPredicateList2 = new ArrayList<String>();
										
										matchingPredicatesMap1 = new HashMap<String, ArrayList<String>>();
										matchingPredicatesMap2 = new HashMap<String, ArrayList<String>>();
										
										screeningPredicateList1 = new ArrayList<String>();
										screeningPredicateList2 = new ArrayList<String>();
										ArrayList<String> matchingJoinColumnIndexes1 = new ArrayList<String>();
										ArrayList<String> matchingJoinColumnIndexes2 = new ArrayList<String>();
										
										screeningPredicatesMap1 = new HashMap<String, ArrayList<String>>();
										screeningPredicatesMap2 = new HashMap<String, ArrayList<String>>();
										
										boolean indexPhase = true;
										boolean isIndexable = false;
										
										
										/*
										 * 		INDEXABLE PREDICATE EVALUATION FOR TABLE 1
										 */
										for(Index index : indexList1)
										{
											String indexName = index.getIdxName();
											
											indexPhase = true;
											matchingPredicateList1 = new ArrayList<String>();
											screeningPredicateList1 = new ArrayList<String>();
											
											for(Index.IndexKeyDef indexKeyDef : index.getIdxKey())
											{
												isIndexable = false;
												int colId = indexKeyDef.colId;
												int colPos = indexKeyDef.idxColPos;
												
												for(Predicate predicate : predicateList)
												{
													if(predicate.getType() != 'L')
													{

														predicateText = predicate.getText();
														operator = "";
														
														if(predicate.isJoin() && colPos==1)
														{
															lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
															rhs = predicateText.substring(predicateText.indexOf("=")+2);
															
															if(lhs.indexOf(table1.getTableName())!=-1)
															{
																column = getColumn(lhs);
																if(colId == column.getColId())
																{
																	isIndexable = true;
																	matchingJoinColumnIndexes1.add(indexName);
																	matchingPredicateList1.add(predicateText);
																}
															}
															else if(rhs.indexOf(table1.getTableName())!=-1)
															{
																column = getColumn(rhs);
																if(colId == column.getColId())
																{
																	isIndexable = true;
																	matchingJoinColumnIndexes1.add(indexName);
																	matchingPredicateList1.add(predicateText);
																}
															}
														}
														else
														{
															if(predicateText.indexOf("=")!=-1)
															{
																lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
																operator = "=";
															}
															else if(predicateText.indexOf("<")!=-1)
															{
																lhs = predicateText.substring(0, predicateText.indexOf("<")-1);
																operator = "<";
															}
															else if(predicateText.indexOf(">")!=-1)
															{
																lhs = predicateText.substring(0, predicateText.indexOf(">")-1);
																operator = ">";
															}
															else if(predicateText.indexOf("IN (")!=-1)
															{
																lhs = predicateText.substring(0, predicateText.indexOf("IN (")-1);
																operator = "IN (";
															}
															
															column = getColumn(lhs);
															
															if(colId == column.getColId())
															{
																/*
																 * 		Predicate contains index column
																 */
																isIndexable = true;
																if(indexPhase)
																{
																	/*
																	 * 		matching predicates
																	 * 
																	 */
																	if(operator.equalsIgnoreCase("=") || operator.equalsIgnoreCase("IN ("))
																	{
																		matchingPredicateList1.add(predicateText);
																	}
																	else if(operator.equalsIgnoreCase(">") || operator.equalsIgnoreCase("<"))
																	{
																		matchingPredicateList1.add(predicateText);
																		indexPhase = false;
																	}
																}
																else
																{
																	/*
																	 * 		Screening predicates
																	 */
																	screeningPredicateList1.add(predicateText);
																}
															}
														}
													
													}
												}
												
												if(!isIndexable)
												{
													indexPhase = false;
												}
											}
											
											matchingPredicatesMap1.put(indexName, matchingPredicateList1);
											screeningPredicatesMap1.put(indexName, screeningPredicateList1);
										}
										
										isIndexable = false;
										indexPhase = true;
										
										for(Index index : indexList2)
										{
											String indexName = index.getIdxName();
											
											indexPhase = true;
											matchingPredicateList2 = new ArrayList<String>();
											screeningPredicateList2 = new ArrayList<String>();
											
											for(Index.IndexKeyDef indexKeyDef : index.getIdxKey())
											{
												isIndexable = false;
												int colId = indexKeyDef.colId;
												int colPos = indexKeyDef.idxColPos;
												
												for(Predicate predicate : predicateList)
												{
													predicateText = predicate.getText();
													operator = "";
													
													if(predicate.isJoin() && colPos==1)
													{
														lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
														rhs = predicateText.substring(predicateText.indexOf("=")+2);
														
														if(lhs.indexOf(table2.getTableName())!=-1)
														{
															column = getColumn(lhs);
															if(colId == column.getColId())
															{
																isIndexable = true;
																matchingJoinColumnIndexes2.add(indexName);
																matchingPredicateList2.add(predicateText);
															}
														}
														else if(rhs.indexOf(table2.getTableName())!=-1)
														{
															column = getColumn(rhs);
															if(colId == column.getColId())
															{
																isIndexable = true;
																matchingJoinColumnIndexes2.add(indexName);
																matchingPredicateList2.add(predicateText);
															}
														}
													}
													else
													{
														if(predicateText.indexOf("=")!=-1)
														{
															lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
															operator = "=";
														}
														else if(predicateText.indexOf("<")!=-1)
														{
															lhs = predicateText.substring(0, predicateText.indexOf("<")-1);
															operator = "<";
														}
														else if(predicateText.indexOf(">")!=-1)
														{
															lhs = predicateText.substring(0, predicateText.indexOf(">")-1);
															operator = ">";
														}
														else if(predicateText.indexOf("IN (")!=-1)
														{
															lhs = predicateText.substring(0, predicateText.indexOf("IN (")-1);
															operator = "IN (";
														}
														
														column = getColumn(lhs);
														
														if(colId == column.getColId())
														{
															/*
															 * 		Predicate contains index column
															 */
															isIndexable = true;
															if(indexPhase)
															{
																/*
																 * 		matching predicates
																 * 
																 */
																if(operator.equalsIgnoreCase("=") || operator.equalsIgnoreCase("IN ("))
																{
																	matchingPredicateList2.add(predicateText);
																}
																else if(operator.equalsIgnoreCase(">") || operator.equalsIgnoreCase("<"))
																{
																	matchingPredicateList2.add(predicateText);
																	indexPhase = false;
																}
															}
															else
															{
																/*
																 * 		Screening predicates
																 */
																screeningPredicateList2.add(predicateText);
															}
														}
													}
												}
												
												if(!isIndexable)
												{
													indexPhase = false;
												}
											}
											
											matchingPredicatesMap2.put(indexName, matchingPredicateList2);
											screeningPredicatesMap2.put(indexName, screeningPredicateList2);
										}
										
										
										if(matchingJoinColumnIndexes1.isEmpty() && matchingJoinColumnIndexes2.isEmpty())
										{
											/*
											 * 		NO MATCHING INDEXES ON BOTH TABLES
											 * 
											 * 		RELATION SCAN
											 */
											
											accessType = 'R';
											indexOnly = ' ';
											sortC_orderBy = 'N';
											
											accessName = "";
											matchCols = 0;
											prefetch = 'S';
											
											double filterFactor1 = table1.getTableCard(), filterFactor2 = table2.getTableCard();
											operator = "";
											
											/*
											 * 		CALCULATING FILTER FACTORS OF BOTH TABLES
											 */
											for(Predicate predicate : predicateList)
											{
												predicateText = predicate.getText();
												
												if(predicate.isJoin())
												{
													lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
													rhs = predicateText.substring(predicateText.indexOf("=")+2);
													operator = "=";
													
													if(lhs.indexOf(table1.getTableName())!=-1)
													{
														filterFactor1 *= calculateFilterFactor(lhs, rhs, operator); 
														filterFactor2 *= calculateFilterFactor(rhs, lhs, operator);
													}
													else
													{
														filterFactor2 *= calculateFilterFactor(lhs, rhs, operator); 
														filterFactor1 *= calculateFilterFactor(rhs, lhs, operator);
													}
												}
												else
												{
													if(predicateText.indexOf("=")!=-1)
													{
														lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
														rhs = predicateText.substring(predicateText.indexOf("=")+2);
														operator = "=";
													}
													else if(predicateText.indexOf("<")!=-1)
													{
														lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
														rhs = predicateText.substring(predicateText.indexOf("=")+2);
														operator = "<";
													}
													else if(predicateText.indexOf(">")!=-1)
													{
														lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
														rhs = predicateText.substring(predicateText.indexOf("=")+2);
														operator = ">";
													}
													else if(predicateText.indexOf("IN (")!=-1)
													{
														lhs = predicateText.substring(0, predicateText.indexOf("IN (")-1);
														rhs = predicateText.substring(predicateText.indexOf("("));
														operator = "I";
													}
													
													if(predicate.getDescription().isEmpty())
													{
														/*
														 * 		CALCULATE FILTER FACTOR OF THOSE PREEDICATES WHICH ARE NOT TCP
														 */
														if(lhs.indexOf(table1.getTableName())!=-1)
														{
															filterFactor1 *= calculateFilterFactor(lhs, rhs, operator); 
														}
														else
														{
															filterFactor2 *= calculateFilterFactor(lhs, rhs, operator);
														}
													}
												}
											}
											
											/*
											 * 		DECIDING LEADING TABLE
											 */
											if(filterFactor1>filterFactor2)
											{
												leadTable = table1.getTableName();
												innerTable = table2.getTableName();
											}
											else
											{
												leadTable = table2.getTableName();
												innerTable = table1.getTableName();
											}
											
											decidePredicateSequence(predicateList, TCPPredicateList, leadTable, innerTable);
										}
										else if(!matchingJoinColumnIndexes1.isEmpty() && !matchingJoinColumnIndexes2.isEmpty())
										{
											/*
											 * 		INDEXES ON BOTH TABLES
											 */
											String indexSelected1 = matchingJoinColumnIndexes1.get(0);
											String indexSelected2 = matchingJoinColumnIndexes2.get(0);
											
											int noOfMatchingPredicates1 = matchingPredicatesMap1.get(indexSelected1).size();
											int noOfMatchingPredicates2 = matchingPredicatesMap2.get(indexSelected2).size();
											
											if(noOfMatchingPredicates1>noOfMatchingPredicates2)
											{
												innerTable = table1.getTableName();
												leadTable = table2.getTableName();
												matchCols = noOfMatchingPredicates1;
												accessName = indexSelected1;
												indexOnly = 'N';
												prefetch = 'S';
												sortC_orderBy = 'N';
												accessType = 'I';
											}
											else if(noOfMatchingPredicates1<noOfMatchingPredicates2)
											{
												innerTable = table2.getTableName();
												leadTable = table1.getTableName();
												matchCols = noOfMatchingPredicates2;
												accessName = indexSelected2;
												indexOnly = 'N';
												accessType = 'I';
												prefetch = 'S';
												sortC_orderBy = 'N';
											}
											else if(noOfMatchingPredicates1==noOfMatchingPredicates2)
											{
												int noOfScreeningPredicates1 = screeningPredicatesMap1.get(indexSelected1).size();
												int noOfScreeningPredicates2 = screeningPredicatesMap2.get(indexSelected2).size();
												
												if(noOfScreeningPredicates1>noOfScreeningPredicates2)
												{
													innerTable = table1.getTableName();
													leadTable = table2.getTableName();
													matchCols = noOfMatchingPredicates1;
													accessName = indexSelected1;
													indexOnly = 'N';
													prefetch = 'S';
													sortC_orderBy = 'N';
													accessType = 'I';
												}
												else if(noOfScreeningPredicates1<noOfScreeningPredicates2)
												{
													innerTable = table2.getTableName();
													leadTable = table1.getTableName();
													matchCols = noOfMatchingPredicates2;
													accessName = indexSelected2;
													indexOnly = 'N';
													accessType = 'I';
													prefetch = 'S';
													sortC_orderBy = 'N';
												}
												else if(noOfScreeningPredicates1==noOfScreeningPredicates2)
												{
													double filterFactor1 = table1.getTableCard();
													double filterFactor2 = table2.getTableCard();
													operator = "=";
													
													for(Predicate predicate : predicateList)
													{
														predicateText = predicate.getText();
														
														if(predicate.isJoin())
														{
															lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
															rhs = predicateText.substring(predicateText.indexOf("=")+2);
															operator = "=";
															
															if(lhs.indexOf(table1.getTableName())!=-1)
															{
																filterFactor1 *= calculateFilterFactor(lhs, rhs, operator); 
																filterFactor2 *= calculateFilterFactor(rhs, lhs, operator);
															}
															else
															{
																filterFactor2 *= calculateFilterFactor(lhs, rhs, operator); 
																filterFactor1 *= calculateFilterFactor(rhs, lhs, operator);
															}
														}
														else
														{
															if(predicateText.indexOf("=")!=-1)
															{
																lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
																rhs = predicateText.substring(predicateText.indexOf("=")+2);
																operator = "=";
															}
															else if(predicateText.indexOf("<")!=-1)
															{
																lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
																rhs = predicateText.substring(predicateText.indexOf("=")+2);
																operator = "<";
															}
															else if(predicateText.indexOf(">")!=-1)
															{
																lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
																rhs = predicateText.substring(predicateText.indexOf("=")+2);
																operator = ">";
															}
															else if(predicateText.indexOf("IN (")!=-1)
															{
																lhs = predicateText.substring(0, predicateText.indexOf("IN (")-1);
																rhs = predicateText.substring(predicateText.indexOf("("));
																operator = "I";
															}
															
															if(predicate.getDescription().isEmpty())
															{
																/*
																 * 		CALCULATE FILTER FACTOR OF THOSE PREEDICATES WHICH ARE NOT TCP
																 */
																if(lhs.indexOf(table1.getTableName())!=-1)
																{
																	filterFactor1 *= calculateFilterFactor(lhs, rhs, operator); 
																}
																else
																{
																	filterFactor2 *= calculateFilterFactor(lhs, rhs, operator);
																}
															}
														}
													}
													if(filterFactor1>filterFactor2)
													{
														leadTable = table1.getTableName();
														innerTable = table2.getTableName();
														matchCols = noOfMatchingPredicates2;
														accessName = indexSelected2;
														indexOnly = 'N';
														prefetch = 'S';
														sortC_orderBy = 'N';
														accessType = 'I';
													}
													else
													{
														leadTable = table2.getTableName();
														innerTable = table1.getTableName();
														matchCols = noOfMatchingPredicates1;
														accessName = indexSelected1;
														indexOnly = 'N';
														accessType = 'I';
														prefetch = 'S';
														sortC_orderBy = 'N';
													}
												}
											}
											decidePredicateSequence(predicateList, TCPPredicateList, leadTable, innerTable);
										}
										else
										{
											/*
											 * 		ONE OF THE TABLE HAS MATCHING JOIN COLUMN INDEX
											 */
											if(!matchingJoinColumnIndexes1.isEmpty() && matchingJoinColumnIndexes2.isEmpty())
											{
												accessName = matchingJoinColumnIndexes1.get(0);
												innerTable = table1.getTableName();
												leadTable = table2.getTableName();
												matchCols = matchingPredicatesMap1.get(accessName).size();
												indexOnly = 'N';
											}
											else if(matchingJoinColumnIndexes1.isEmpty() && !matchingJoinColumnIndexes2.isEmpty())
											{
												accessName = matchingJoinColumnIndexes2.get(0);
												innerTable = table2.getTableName();
												leadTable = table1.getTableName();
												matchCols = matchingPredicatesMap2.get(accessName).size();
												indexOnly = 'N';
											}
											
											accessType = 'I';
											prefetch = 'S';
											sortC_orderBy = 'N';
											
											decidePredicateSequence(predicateList, TCPPredicateList, leadTable, innerTable);
										}
									}
									else
									{
										/*
										 * 		ONE OF THE TABLE HAS INDEX
										 */

										Table outerTable = new Table();
										Table inTable = new Table();
										
										if(!indexList1.isEmpty() && indexList2.isEmpty())
										{
											inTable = table1;
											outerTable = table2;
										}
										else if(indexList1.isEmpty() && !indexList2.isEmpty())
										{
											inTable = table2;
											outerTable = table1;
										}
										/*
										 * 		INDEXABLE PREDICATE EVALUATION
										 */
										boolean indexPhase = true;
										boolean isIndexable = false;
										
										ArrayList<String> matchingJoinColumnIndexes = new ArrayList<String>();
										
										for(Index index : inTable.getIndexes())
										{
											String indexName = index.getIdxName();
											
											indexPhase = true;
											matchingPredicateList2 = new ArrayList<String>();
											screeningPredicateList2 = new ArrayList<String>();
											
											for(Index.IndexKeyDef indexKeyDef : index.getIdxKey())
											{
												isIndexable = false;
												int colId = indexKeyDef.colId;
												
												for(Predicate predicate : predicateList)
												{
													if(predicate.getType() != 'L')
													{

														predicateText = predicate.getText();
														operator = "";
														
														if(predicate.isJoin())
														{
															lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
															rhs = predicateText.substring(predicateText.indexOf("=")+2);
															
															if(lhs.indexOf(inTable.getTableName())!=-1)
															{
																column = getColumn(lhs);
																if(colId == column.getColId())
																{
																	isIndexable = true;
																	matchingJoinColumnIndexes.add(indexName);
																	matchingPredicateList2.add(predicateText);
																}
															}
															else if(rhs.indexOf(inTable.getTableName())!=-1)
															{
																column = getColumn(rhs);
																if(colId == column.getColId())
																{
																	isIndexable = true;
																	matchingJoinColumnIndexes.add(indexName);
																	matchingPredicateList2.add(predicateText);
																}
															}
														}
														else
														{
															if(predicateText.indexOf("=")!=-1)
															{
																lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
																operator = "=";
															}
															else if(predicateText.indexOf("<")!=-1)
															{
																lhs = predicateText.substring(0, predicateText.indexOf("<")-1);
																operator = "<";
															}
															else if(predicateText.indexOf(">")!=-1)
															{
																lhs = predicateText.substring(0, predicateText.indexOf(">")-1);
																operator = ">";
															}
															else if(predicateText.indexOf("IN (")!=-1)
															{
																lhs = predicateText.substring(0, predicateText.indexOf("IN (")-1);
																operator = "IN (";
															}
															
															column = getColumn(lhs);
															
															if(colId == column.getColId())
															{
																/*
																 * 		Predicate contains index column
																 */
																isIndexable = true;
																if(indexPhase)
																{
																	/*
																	 * 		matching predicates
																	 * 
																	 */
																	if(operator.equalsIgnoreCase("=") || operator.equalsIgnoreCase("IN ("))
																	{
																		matchingPredicateList2.add(predicateText);
																	}
																	else if(operator.equalsIgnoreCase(">") || operator.equalsIgnoreCase("<"))
																	{
																		matchingPredicateList2.add(predicateText);
																		indexPhase = false;
																	}
																}
																else
																{
																	/*
																	 * 		Screening predicates
																	 */
																	screeningPredicateList2.add(predicateText);
																}
															}
														}
													
													}
												}
												
												if(!isIndexable)
												{
													indexPhase = false;
												}
											}
											
											matchingPredicatesMap2.put(indexName, matchingPredicateList2);
											screeningPredicatesMap2.put(indexName, screeningPredicateList2);
										}
										
										if(matchingJoinColumnIndexes.isEmpty())
										{
											/*
											 * 		NO MATCHING PREDICATE ON JOIN COLUMN
											 * 
											 * 		RELATION SCAN WILL BE PERFORMED
											 */
											
											accessType = 'R';
											accessName = "";
											matchCols = 0;
											indexOnly = ' ';
											prefetch = 'S';
											sortC_orderBy = 'N';
											
											double filterFactor1 = table1.getTableCard(), filterFactor2 = table2.getTableCard();
											operator = "";
											
											/*
											 * 		CALCULATING FILTER FACTORS OF BOTH TABLES
											 */
											for(Predicate predicate : predicateList)
											{
												predicateText = predicate.getText();
												
												if(predicate.isJoin())
												{
													lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
													rhs = predicateText.substring(predicateText.indexOf("=")+2);
													operator = "=";
													
													if(lhs.indexOf(table2.getTableName())!=-1)
													{
														filterFactor1 *= calculateFilterFactor(lhs, rhs, operator); 
														filterFactor2 *= calculateFilterFactor(rhs, lhs, operator);
													}
													else
													{
														filterFactor2 *= calculateFilterFactor(lhs, rhs, operator); 
														filterFactor1 *= calculateFilterFactor(rhs, lhs, operator);
													}
												}
												else
												{
													if(predicateText.indexOf("=")!=-1)
													{
														lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
														rhs = predicateText.substring(predicateText.indexOf("=")+2);
														operator = "=";
													}
													else if(predicateText.indexOf("<")!=-1)
													{
														lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
														rhs = predicateText.substring(predicateText.indexOf("=")+2);
														operator = "<";
													}
													else if(predicateText.indexOf(">")!=-1)
													{
														lhs = predicateText.substring(0, predicateText.indexOf("=")-1);
														rhs = predicateText.substring(predicateText.indexOf("=")+2);
														operator = ">";
													}
													else if(predicateText.indexOf("IN (")!=-1)
													{
														lhs = predicateText.substring(0, predicateText.indexOf("IN (")-1);
														rhs = predicateText.substring(predicateText.indexOf("("));
														operator = "I";
													}
													
													if(predicate.getDescription().isEmpty())
													{
														/*
														 * 		CALCULATE FILTER FACTOR OF THOSE PREEDICATES WHICH ARE NOT TCP
														 */
														if(lhs.indexOf(table2.getTableName())!=-1)
														{
															filterFactor1 *= calculateFilterFactor(lhs, rhs, operator); 
														}
														else
														{
															filterFactor2 *= calculateFilterFactor(lhs, rhs, operator);
														}
													}
												}
											}
											
											/*
											 * 		DECIDING LEADING TABLE
											 */
											if(filterFactor1>filterFactor2)
											{
												leadTable = table1.getTableName();
												innerTable = table2.getTableName();
											}
											else
											{
												leadTable = table2.getTableName();
												innerTable = table1.getTableName();
											}
											
											/*
											 * 		DECIDING PREDICATE SEQUENCE
											 * 
											 */
											decidePredicateSequence(predicateList, TCPPredicateList, leadTable, innerTable);
										}
										else
										{
											/*
											 * 			MATCHING JOIN COLUMN INDEX EXISTS
											 */
											
											accessName = matchingJoinColumnIndexes.get(0);
											accessType = 'I';
											matchCols = matchingPredicatesMap2.get(accessName).size();
											
											prefetch = 'S';
											sortC_orderBy = 'N';
											
											innerTable = inTable.getTableName();
											leadTable = outerTable.getTableName();
											
											indexOnly = 'N';
											
											decidePredicateSequence(predicateList, TCPPredicateList, leadTable, innerTable);
										}
									}
								}
								else
								{
									/*
									 * 		LOCAL PREDICATES ARE NOT USED
									 */
									
									if(indexList1.isEmpty() && indexList2.isEmpty())
									{
										/*
										 * 		NO INDEX ON BOTH TABLES
										 */
										accessType = 'R';
										accessName = "";
										matchCols = 0;
										
										indexOnly = ' ';
										prefetch = 'S';
										sortC_orderBy = 'N';
										
										if(table1Card>table2Card)
										{
											leadTable = table1.getTableName();
											innerTable = table2.getTableName();
										}
										else
										{
											leadTable = table2.getTableName();
											innerTable = table1.getTableName();
										}
										
										p = predicateList.get(0);
										p.setSequence(1);
									}
									else if(!indexList1.isEmpty() && !indexList2.isEmpty())
									{
										/*
										 * 		BOTH TABLES HAVE INDEXES
										 */
										
										matchingPredicateList1 = new ArrayList<String>();
										matchingPredicateList2 = new ArrayList<String>();
										/*
										 * 		INDEXABLE PREDICATE EVALUATION TABLE 1
										 */
										for(Index index : indexList1)
										{
											String indexName = index.getIdxName();
											
											Index.IndexKeyDef indexKeyDef = index.getIdxKey().get(0);
											int colId = indexKeyDef.colId;
											
											for(Predicate predicate : predicateList)
											{
												if(predicate.isJoin())
												{
													text = predicate.getText();
													
													lhs = text.substring(0, text.indexOf("=")-1);
													rhs = text.substring(text.indexOf("=")+2);
													
													if(lhs.indexOf(table1.getTableName())!=-1)
													{
														column = getColumn(lhs);
														
														if(column.getColId() == colId)
															matchingPredicateList1.add(indexName);
													}
													else if(rhs.indexOf(table1.getTableName())!=-1)
													{
														column = getColumn(rhs);
														
														if(column.getColId() == colId)
															matchingPredicateList1.add(indexName);
													}
												}
											}
										}
										
										/*
										 * 		INDEXABLE PREDICATE EVALUATION FOR TABLE 2
										 */
										for(Index index : indexList2)
										{
											String indexName = index.getIdxName();
											
											Index.IndexKeyDef indexKeyDef = index.getIdxKey().get(0);
											int colId = indexKeyDef.colId;
											
											for(Predicate predicate : predicateList)
											{
												if(predicate.isJoin())
												{
													text = predicate.getText();
													
													lhs = text.substring(0, text.indexOf("=")-1);
													rhs = text.substring(text.indexOf("=")+2);
													if(lhs.indexOf(table2.getTableName())!=-1)
													{
														column = getColumn(lhs);
														if(column.getColId() == colId)
															matchingPredicateList2.add(indexName);
													}
													else if(rhs.indexOf(table2.getTableName())!=-1)
													{
														column = getColumn(rhs);
														
														if(column.getColId() == colId)
															matchingPredicateList2.add(indexName);
													}
												}
											}
										}
										
										if(matchingPredicateList1.isEmpty() && matchingPredicateList2.isEmpty())
										{
											/*
											 * 		NO USEFUL INDEX ON BOTH TABLE
											 * 
											 * 		RELATION SCAN
											 */
											accessName = "";
											accessType = 'R';
											prefetch = 'S';
											
											matchCols = 0;
											indexOnly = ' ';
											sortC_orderBy = 'N';
											
											if(table1Card>table2Card)
											{
												leadTable = table1.getTableName();
												innerTable = table2.getTableName();
											}
											else
											{
												leadTable = table2.getTableName();
												innerTable = table1.getTableName();
											}
											
											p = predicateList.get(0);
											p.setSequence(1);
										}
										else if(!matchingPredicateList1.isEmpty() && !matchingPredicateList2.isEmpty())
										{
											/*
											 * 		INDEX ON BOTH TABLES
											 */
											boolean isTable1Outer = false;
											if(table1Card>table2Card)
											{
												leadTable = table2.getTableName();
												innerTable = table1.getTableName();
											}
											else
											{
												isTable1Outer = true;
												leadTable = table1.getTableName();
												innerTable = table2.getTableName();
											}
											
											if(isTable1Outer)
											{
												accessName = matchingPredicateList2.get(0);
											}
											else
											{
												accessName = matchingPredicateList1.get(0);
											}
											
											accessType = 'I';
											matchCols = 1;
											prefetch = 'S';
											
											sortC_orderBy = 'N';
											indexOnly = 'N';
											
											p = predicateList.get(0);
											p.setSequence(1);
										}
										else
										{
											/*
											 * 		ONE OF THE TABLE HAS INDEX
											 */
											
											accessType = 'I';
											matchCols = 1;
											prefetch = 'S';
											
											sortC_orderBy = 'N';
											indexOnly = 'N';
											p = predicateList.get(0);
											p.setSequence(1);
											
											if(!matchingPredicateList1.isEmpty())
											{
												accessName = matchingPredicateList1.get(0);
												innerTable = table1.getTableName();
												leadTable = table2.getTableName();
											}
											else
											{
												accessName = matchingPredicateList2.get(0);
												innerTable = table2.getTableName();
												leadTable = table1.getTableName();
											}
										}
									}
									else
									{
										/*
										 * 		ONE OF THE TABLE HAS INDEX
										 */
										
										/*
										 * 		INDEXABLE MATCHING JOIN PREDICATE EVALUATION
										 */
										Table outerTable = new Table();
										Table inTable = new Table();
										
										if(!indexList1.isEmpty())
										{
											inTable = table1;
											outerTable = table2;
										}
										else
										{
											inTable = table2;
											outerTable = table1;
										}
										
										boolean indexPhase = true;
										boolean isIndexable = false;
										operator = "";
										
										ArrayList<String> matchingJoinPredicateIndexes = new ArrayList<String>();
										
										for(Index index : inTable.getIndexes())
										{
											String indexName = index.getIdxName();
											indexPhase = true;
											
											Index.IndexKeyDef indexKeyDef = index.getIdxKey().get(0);
											int colId = indexKeyDef.colId;
											
											for(Predicate predicate : predicateList)
											{
												text = predicate.getText();
												if(predicate.isJoin())
												{
													lhs = text.substring(0, text.indexOf("=")-1);
													rhs = text.substring(text.indexOf("=")+2);
													operator = "=";
													
													if(lhs.indexOf(inTable.getTableName())!=-1)
													{
														column = getColumn(lhs);
														if(colId == column.getColId())
														{
															matchingJoinPredicateIndexes.add(indexName);
														}
													}
													else if(rhs.indexOf(inTable.getTableName())!=-1)
													{
														column = getColumn(rhs);
														if(colId == column.getColId())
														{
															matchingJoinPredicateIndexes.add(indexName);
														}
													}
												}
											}
										}
										
										if(matchingJoinPredicateIndexes.isEmpty())
										{
											/*
											 * 		NO INDEX SATISFY MATCHING JOIN PREDICATE
											 * 
											 * 		RELATIONAL SCAN WILL BE PERFORMED
											 */
											accessType = 'R';
											accessName = "";
											matchCols = 0;
											
											indexOnly = ' ';
											prefetch = 'S';
											sortC_orderBy = 'N';
											
											if(table1Card>table2Card)
											{
												leadTable = table1.getTableName();
												innerTable = table2.getTableName();
											}
											else
											{
												leadTable = table2.getTableName();
												innerTable = table1.getTableName();
											}
											
											p = predicateList.get(0);
											p.setSequence(1);
										}
										else
										{
											/*
											 * 		INDEX WITH MATCHING JOIN PREDICATES EXIST
											 */
											
											accessName = matchingJoinPredicateIndexes.get(0);
											accessType = 'I';
											prefetch = 'S';
											sortC_orderBy = 'N';
											
											matchCols = 1;
											
											leadTable = outerTable.getTableName();
											innerTable = inTable.getTableName();
											
											indexOnly = 'N';
										}
									}
								}
								
								if(isOrderByClauseUsed)
								{
									/*
									 * 		ORDER BY CLAUSE IS USED
									 */
									Table inTable = new Table();
									for(Table t : tables)
									{
										if(t.getTableName().equalsIgnoreCase(innerTable))
										{
											inTable = t;
											break;
										}
									}
									
									Index index = new Index("");
									for(Index ind : inTable.getIndexes())
									{
										if(ind.getIdxName().equalsIgnoreCase(accessName))
										{
											index = ind;
											break;
										}
									}
									
									ArrayList<Index.IndexKeyDef> list = index.getIdxKey();
									boolean isDescOrder = false;	// true if order is opposite of index
									boolean indexUsedForOrderBy = true;
									boolean descOrder = false;
									
									for(i=0; i<sortColumnList.size(); i++)
									{
										Index.IndexKeyDef indexKeyDef = list.get(i);
										column = sortColumnList.get(i);
										String sortColumn = sortOrder.get(i); 
										
										if(sortColumn.contains(" D "))
											descOrder = true;
										else
											descOrder = false;
										
										if(column.getColId()==indexKeyDef.colId)
										{
											if(i==0)
											{
												if(descOrder==indexKeyDef.descOrder)
												{
													isDescOrder = false;
												}
												else
												{
													isDescOrder = true;
												}
											}
											else
											{
												if(isDescOrder)
												{
													if(descOrder)
														descOrder = false;
													else
														descOrder = true;
													
													if(descOrder!=indexKeyDef.descOrder)
													{
														indexUsedForOrderBy = false;
													}
												}
												else
												{
													if(descOrder!=indexKeyDef.descOrder)
													{
														indexUsedForOrderBy = false;
													}
												}
											}
										}
										else
										{
											indexUsedForOrderBy = false;
											break;
										}
									}
									
									if(indexUsedForOrderBy)
									{
										sortC_orderBy = 'N';
									}
									else
									{
										sortC_orderBy = 'Y';
									}
								}
								/*else
									sortC_orderBy = ' ';*/
								
								for(Predicate predicate : TCPPredicateList)
								{
									predicateList.add(predicate);
								}
							}
							else
							{
								/*
								 * 		WHERE CLAUSE IS NOT USED
								 * 
								 * 		NO JOIN PREDICATE
								 */
								
								throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
							}
							
							Table inTable = new Table();
							Table outTable = new Table();
							for(Table t : tables)
							{
								if(t.getTableName().equalsIgnoreCase(innerTable))
								{
									inTable = t;
									break;
								}
							}
							
							for(Table t : tables)
							{
								if(t.getTableName().equalsIgnoreCase(leadTable))
								{
									outTable = t;
									break;
								}
							}
							
							indexList = new ArrayList<Index>();
							for(Index index : inTable.getIndexes())
							{
								indexList.add(index);
							}
							
							for(Index index : outTable.getIndexes())
							{
								indexList.add(index);
							}
						}
						
						
						/*
						 * 		PRINTING THE INDEX 
						 */
						IndexList list = new IndexList(indexList);
						list.printTable(out);
						
						/*
						 * 		PRINTING PLAN TABLE
						 */
						planTable.setQueryBlockNo(queryBlockNo);
						planTable.setAccessType(accessType);
						planTable.setPrefetch(prefetch);
						
						planTable.setAccessName(accessName);
						planTable.setIndexOnly(indexOnly);
						
						planTable.setMatchCols(matchCols);
						planTable.setSortC_orderBy(sortC_orderBy);
						
						planTable.setTable1Card(table1Card);
						planTable.setTable2Card(table2Card);
						
						planTable.setLeadTable(leadTable);
						planTable.setInnerTable(innerTable);
						
						planTable.printTable(out);
						
						
						/*
						 * 		PRINTING PREDICATE TABLE
						 */
						Predicate predicate = new Predicate();
						
						for(Predicate pr : predicateList)
						{
							if(pr.getType() == 'L')
							{
								if(pr.getText().indexOf("=")!=-1)
								{
									pr.setType('E');
								}
								if(pr.getText().indexOf("<")!=-1 || pr.getText().indexOf(">")!=-1)
								{
									pr.setType('R');
								}
							}
						}
						predicate.printTable(out, predicateList);
					}
					else
					{
						/*
						 * 		There should be only 8 predicates
						 */
						throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
					}
				}
				else
				{
					/*
					 * 		Error because query returns columns of those tables which are not specified in selected table list
					 */
					throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
				}
			}
			else
			{
				/*
				 * 		Error because query should contain "FROM" token
				 */
				throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
			}
		}
		catch(NoSuchElementException e)
		{
			throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");
		}
	}
	
	/*public char isIndexOnly(List<String> selectedColumnList, String accessName, Table table)
	{
		
		 * 		DECIDING INDEX ONLY ACCESS
		 
		int count = 0;
		Index index = new Index("");
		
		for(Index ind : table.getIndexes())
		{
			if(ind.getIdxName().equalsIgnoreCase(accessName))
			{
				index = ind;
				break;
			}
		}
		
		if(selectedColumnList.contains("*"))
		{
			for(Column c : table.getColumns())
			{
				int colId = c.getColId();
				for(Index.IndexKeyDef ikd : index.getIdxKey())
				 {
					 if(colId == ikd.colId)
						 count++;
				 }
			}
		}
		else
		{
			for(String s : selectedColumnList)
			{
				if(s.indexOf(table.getTableName())!=-1)
				{
					int colId = getColumn(s).getColId();
					
					 for(Index.IndexKeyDef ikd : index.getIdxKey())
					 {
						 if(colId == ikd.colId)
							 count++;
					 }
				}
			}
		}
		if(count>=index.getIdxKey().size())
		{
			return 'Y';
		}
		else
		{
			return 'N';
		}
	}*/
	
	public char isIndexOnlyAccess(List<String> selectedColumnList, String accessName, ArrayList<Predicate> predicateList, Table table)
	{
		int count = 0, count1 = 0;
		Index index = new Index("");
		
		for(Index ind : table.getIndexes())
		{
			if(ind.getIdxName().equalsIgnoreCase(accessName))
			{
				index = ind;
				break;
			}
		}
		
		if(selectedColumnList.contains("*"))
		{
			for(Column c : table.getColumns())
			{
				int cId = c.getColId();
				for(Index.IndexKeyDef indexKeyDef : index.getIdxKey())
				{
					if(cId == indexKeyDef.colId)
						count++;
				}
			}
		}
		else
		{
			for(String c : selectedColumnList)
			{
				int cId = getColumn(c).getColId();
				for(Index.IndexKeyDef indexKeyDef : index.getIdxKey())
				{
					if(cId == indexKeyDef.colId)
						count++;
				}
			}
		}
		
		count1 = count;
		count = 0;
		
		if(!predicateList.isEmpty())
		{
			for(Predicate predicate : predicateList)
			{
				char type = predicate.getType();
				String text = predicate.getText();
				String desc = predicate.getDescription();
				String lhs = "", rhs = "", operator = "";
				if(type == 'E')
				{
					if(text.indexOf("IN (")!=-1)
					{
						lhs = desc.substring(0, desc.indexOf("=")-1);
						rhs = desc.substring(desc.indexOf("="));
					}
					else
					{
						lhs = text.substring(0, text.indexOf("=")-1);
						rhs = text.substring(text.indexOf("=")+2);
					}
					
					operator = "=";
				}
				else if(text.indexOf("<")!=-1)
				{
					lhs = text.substring(0, text.indexOf("<")-1);
					rhs = text.substring(text.indexOf("<")+2);
					operator = "<";
				}
				else if(text.indexOf(">")!=-1)
				{
					lhs = text.substring(0, text.indexOf(">")-1);
					rhs = text.substring(text.indexOf(">")+2);
					operator = ">";
				}
				else if(type == 'I')
				{
					if(desc.indexOf("IN (")!=-1)
					{
						lhs = desc.substring(0, desc.indexOf("IN (")-1);
						rhs = desc.substring(desc.indexOf("("));
					}
					else
					{
						lhs = text.substring(0, text.indexOf("IN (")-1);
						rhs = text.substring(text.indexOf("("));
					}
					operator = "I";
				}
				Column column = getColumn(lhs);
				int colId = column.getColId();
				
				for(Index.IndexKeyDef indexKeyDef : index.getIdxKey())
				{
					if(colId == indexKeyDef.colId)
						count++;
				}
			}
		}
		
		count1 += count;
		int total = selectedColumnList.size() + predicateList.size();
		if(count1==total)
			return 'Y';
		else
			return 'N';
	}
	
	public void setPredicateSequence(ArrayList<Double> sequenceOrder, ArrayList<Predicate> predicateList)
	{
		System.out.println(sequenceOrder);
		Collections.sort(sequenceOrder);
		
		
		boolean isPredicateExexute = false, flag = false;
		for(Predicate pr : predicateList)
		{
			double ff1 = pr.getFf1();
			int sequence = pr.getSequence();
			
			if(sequence == -1)
			{
				for(int i=0; i<sequenceOrder.size(); i++)
				{
					double ff = sequenceOrder.get(i);
					if(ff1 == ff)
					{
						pr.setSequence(i+1);
					}
				}
				
			}
			
			if(ff1 == 0 && sequence == 0)
				isPredicateExexute = true;
		}
		
		if(isPredicateExexute)
		{
			for(Predicate predicate : predicateList)
			{
				predicate.setSequence(0);
			}
		}
	}
	
	public void decidePredicateSequence(ArrayList<Predicate> predicateList, ArrayList<Predicate> TCPPredicateList, String leadTable, String innerTable)
	{
		/*
		 * 		DECIDING PREDICATE SEQUENCE
		 * 
		 */
		if(TCPPredicateList.isEmpty())
		{
			/*
			 * 		NO TCP PREDICATES
			 */
			ArrayList<Double> leadTablePredicatesList = new ArrayList<Double>();
			ArrayList<Double> innerTablePredicatesList = new ArrayList<Double>();
			
			for(Predicate predicate : predicateList)
			{
				String predicateText = predicate.getText();
				if(!predicate.isJoin())
				{
					if(predicateText.indexOf(leadTable)!=-1)
					{
						leadTablePredicatesList.add(predicate.getFf1());
					}
					else
					{
						innerTablePredicatesList.add(predicate.getFf1());
					}
				}
			}
			
			Collections.sort(leadTablePredicatesList);
			Collections.sort(innerTablePredicatesList);
			
			int sequence = 1;
			boolean isPredicateExecute1 = false, isPredicateExecute2 = false;
			
			/*
			 * 		LOCAL PREDICATES ON OUTER TABLE
			 */
			if(!leadTablePredicatesList.isEmpty())
			{
				for(Double ff : leadTablePredicatesList)
				{
					for(Predicate predicate : predicateList)
					{
						if(!predicate.isJoin() && predicate.getDescription().isEmpty())
						{
							if(predicate.getSequence() == -1)
							{
								if(predicate.getFf1() == 0 && sequence == 0)
									isPredicateExecute1 = true;
								
								if(ff == predicate.getFf1())
								{
									predicate.setSequence(sequence);
									sequence++;
									break;
								}
							}
						}
					}
				}
			}
			
			
			
			/*
			 * 		JOIN PREDICATE
			 */
			for(Predicate predicate : predicateList)
			{
				if(predicate.isJoin())
				{
					predicate.setSequence(sequence);
					sequence++;
					break;
				}
			}
 			
			/*
			 * 		LOCAL PREDICATES ON INNER TABLE
			 */
			if(!innerTablePredicatesList.isEmpty())
			{
				for(Double ff : innerTablePredicatesList)
				{
					for(Predicate predicate : predicateList)
					{
						if(!predicate.isJoin() && predicate.getDescription().isEmpty())
						{
							if(predicate.getFf1() == 0 && sequence == 0)
								isPredicateExecute2 = true;
							
							if(ff == predicate.getFf1())
							{
								predicate.setSequence(sequence);
								sequence++;
								break;
							}
						}
					}
				}
			}
			
			if(isPredicateExecute1 || isPredicateExecute2)
			{
				for(Predicate p : predicateList)
				{
					p.setSequence(0);
				}
			}
		}
		else
		{
			/*
			 * 		TCP PREDICATE EXIST
			 */
			
			ArrayList<Double> leadTablePredicatesList = new ArrayList<Double>();
			ArrayList<Double> innerTablePredicatesList = new ArrayList<Double>();
			boolean isPredicateExecute2 = false, isPredicateExecute1 = false;
			
			for(Predicate predicate : predicateList)
			{
				String predicateText = predicate.getText();
				if(!predicate.isJoin())
				{
					if(predicateText.indexOf(leadTable)!=-1)
					{
						leadTablePredicatesList.add(predicate.getFf1());
					}
					else
					{
						innerTablePredicatesList.add(predicate.getFf1());
					}
				}
			}
			
			/*
			 * 		TCP PREDICATE EVALUATION
			 */
			
			for(Predicate predicate : TCPPredicateList)
			{
				String predicateText = predicate.getText();
				
				if(predicateText.indexOf(leadTable)!=-1)
				{
					/*
					 * 		TCP PREDICATE ON LEAD TABLE
					 */
					leadTablePredicatesList.add(predicate.getFf1());
				}
				else if(predicateText.indexOf(innerTable)!=-1)
				{
					predicate.setSequence(0);
				}
			}
			
			Collections.sort(leadTablePredicatesList);
			Collections.sort(innerTablePredicatesList);
			
			int sequence = 1;
			
			/*
			 * 		LOCAL PREDICATES ON OUTER TABLE
			 */
			
			if(!leadTablePredicatesList.isEmpty())
			{
				for(Double ff : leadTablePredicatesList)
				{
					for(Predicate predicate : predicateList)
					{
						if(!predicate.isJoin() && predicate.getDescription().isEmpty())
						{
							if(predicate.getFf1() == 0 && sequence == 0)
								isPredicateExecute1 = true;
							if(ff == predicate.getFf1())
							{
								predicate.setSequence(sequence);
								sequence++;
								break;
							}
						}
					}
				}
			}
			
			/*
			 * 		JOIN PREDICATE
			 */
			for(Predicate predicate : predicateList)
			{
				if(predicate.isJoin())
				{
					predicate.setSequence(sequence);
					sequence++;
					break;
				}
			}
			
			/*
			 * 		LOCAL PREDICATES ON INNER TABLE
			 */
			if(!innerTablePredicatesList.isEmpty())
			{
				for(Double ff : innerTablePredicatesList)
				{
					for(Predicate predicate : predicateList)
					{
						if(!predicate.isJoin() && predicate.getDescription().isEmpty())
						{
							if(predicate.getFf1() == 0 && sequence == 0)
								isPredicateExecute2 = true;
							if(ff == predicate.getFf1())
							{
								predicate.setSequence(sequence);
								sequence++;
								break;
							}
						}
					}
				}
			}
			
			if(isPredicateExecute1 || isPredicateExecute2)
			{
				for(Predicate p : predicateList)
				{
					p.setSequence(0);
				}
			}
		}
	}
	
	public <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue( Map<K, V> map )
	{
	    List<Map.Entry<K, V>> list = new LinkedList<>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<K, V>>()
	    {
	        @Override
	        public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
	        {
	            return (o1.getValue()).compareTo( o2.getValue() );
	        }
	    } );
	
	    Map<K, V> result = new LinkedHashMap<>();
	    for (Map.Entry<K, V> entry : list)
	    {
	        result.put( entry.getKey(), entry.getValue() );
	    }
	    return result;
	}
	
	private static Comparator<Index.IndexKeyVal> sort() 
	{
		Comparator<Index.IndexKeyVal> comparator = new Comparator<Index.IndexKeyVal>() {
			@Override
			public int compare(Index.IndexKeyVal idxKeyVal1, Index.IndexKeyVal idxKeyVal2) 
			{
				int comparison = idxKeyVal1.value.compareToIgnoreCase(idxKeyVal2.value);
				return comparison;
			}
		};
		return comparator;
	}
	
	public Column getColumn(String lhs)
	{
		/*
		 * 		GETS THE COLUMN BASED ON NAME
		 */
		Column column = new Column();
		Table table = new Table();
		
		String tableName = lhs.substring(0, lhs.indexOf("."));
		String columnName = lhs.substring(lhs.indexOf(".")+1).trim();
		for(Table t : tables)
		{
			if(tableName.equalsIgnoreCase(t.getTableName()))
			{
				table = t;
				break;
			}
		}
		for(Column c : table.getColumns())
		{
			String cname = c.getColName().trim();
			if(cname.equalsIgnoreCase(columnName))
			{
				column = c;
				break;
			}
		}
		
		return column;
	}
	
	public void errorTesting(ArrayList<Predicate> predicateList, boolean andPredicatePresent, boolean orPredicateUsed, List<String> predicateArray)
	{
		for(int i=0; i<predicateList.size(); i++)
		{

			Predicate p1 = predicateList.get(i);
			
			if(!p1.isJoin())
			{
				String text1 = p1.getText();
				String op1 = "", l1 = "", r1 = "";
				
				if(text1.indexOf("<")!=-1)
				{
					l1 = text1.substring(0, text1.indexOf("<"));
					r1 = text1.substring(text1.indexOf("<")+2);
					op1 = "<";
				}
				else if(text1.indexOf(">")!=-1)
				{
					l1 = text1.substring(0, text1.indexOf(">"));
					r1 = text1.substring(text1.indexOf(">")+2);
					op1 = ">";
				}
				else if(text1.indexOf("=")!=-1)
				{
					l1 = text1.substring(0, text1.indexOf("="));
					r1 = text1.substring(text1.indexOf("=")+2);
					op1 = "=";
				}
				
				for(int j=i+1; j<predicateList.size(); j++)
				{
					Predicate p2 = predicateList.get(j);
					String text2 = p2.getText();
					String op2 = "", l2 = "", r2 = "";
					
					if(text2.indexOf("<")!=-1)
					{
						l2 = text2.substring(0, text2.indexOf("<"));
						r2 = text2.substring(text2.indexOf("<")+2);
						op2 = "<";
					}
					else if(text2.indexOf(">")!=-1)
					{
						l2 = text2.substring(0, text2.indexOf(">"));
						r2 = text2.substring(text2.indexOf(">")+2);
						op2 = ">";
					}
					else if(text2.indexOf("=")!=-1)
					{
						l2 = text2.substring(0, text2.indexOf("="));
						r2 = text2.substring(text2.indexOf("=")+2);
						op2 = "=";
					}
					
					if(l1.equalsIgnoreCase(l2))
					{
						if(op1.equalsIgnoreCase(op2))
						{
							if(andPredicatePresent && !orPredicateUsed)
							{
								/*
								 * 		ONLY AND OPERATOR
								 */
								if(op1.equalsIgnoreCase("="))
								{
									if(!r1.equalsIgnoreCase(r2))
									{
										p1.setSequence(0);
										p2.setSequence(0);
									}
								}
								else if(op1.equalsIgnoreCase(">"))
								{
									if(!r1.equalsIgnoreCase(r2))
									{
										p1.setSequence(0);
										p2.setSequence(0);
									}
								}
								else if(op1.equalsIgnoreCase("<"))
								{
									if(!r1.equalsIgnoreCase(r2))
									{
										p1.setSequence(0);
										p2.setSequence(0);
									}
								}
							}
							else if(!andPredicatePresent && orPredicateUsed)
							{
								/*
								 * 		ONLY OR OPERATOR
								 */
								if(op1.equalsIgnoreCase(">"))
								{
									if(!r1.equalsIgnoreCase(r2))
									{
										boolean flag = true;
										for(String str : predicateArray)
										{
											if(str.equalsIgnoreCase(text1))
											{
												flag = true;
												break;
											}
											if(str.equalsIgnoreCase(text2))
											{
												flag = false;
												break;
											}
										}
										if(flag)
										{
											p2.setSequence(0);
											p1.setSequence(1);
										}
										else
										{
											p1.setSequence(0);
											p2.setSequence(1);
										}
									}
								}
								else if(op1.equalsIgnoreCase("<"))
								{
									
									if(!r1.equalsIgnoreCase(r2))
									{
										boolean flag = true;
										for(String str : predicateArray)
										{
											if(str.equalsIgnoreCase(text1))
											{
												flag = true;
												break;
											}
											if(str.equalsIgnoreCase(text2))
											{
												flag = false;
												break;
											}
										}
										if(flag)
										{
											p2.setSequence(0);
											p1.setSequence(1);
										}
										else
										{
											p1.setSequence(0);
											p2.setSequence(1);
										}
									}
								}
							}
						}
						else if(op1.equalsIgnoreCase(">") && op2.equalsIgnoreCase("<"))
						{
							if(andPredicatePresent && !orPredicateUsed)
							{
								/*
								 * 		ONLY AND OPERATOR
								 */
								
								Column column = getColumn(l1);
								
								if(column.getColType().name().equalsIgnoreCase("CHAR"))
								{
									if(r1.equalsIgnoreCase(r2))
									{
										p1.setSequence(0);
										p2.setSequence(0);
									}
								}
								else
								{
									int a = Integer.parseInt(r1);
									int b = Integer.parseInt(r2);
									
									if(a==b)
									{
										p1.setSequence(0);
										p2.setSequence(0);
									}
								}
							}
						}
						else if(op1.equalsIgnoreCase("<") && op2.equalsIgnoreCase(">"))
						{

							if(andPredicatePresent && !orPredicateUsed)
							{
								/*
								 * 		ONLY AND OPERATOR
								 */
								Column column = getColumn(l1);
								
								if(column.getColType().name().equalsIgnoreCase("CHAR"))
								{
									if(r1.equalsIgnoreCase(r2))
									{
										p1.setSequence(0);
										p2.setSequence(0);
									}
								}
								else
								{
									int a = Integer.parseInt(r1);
									int b = Integer.parseInt(r2);
									
									if(a==b)
									{
										p1.setSequence(0);
										p2.setSequence(0);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public double calculateFilterFactor(String lhs, String rhs, String operator)
	{
		/*
		 * 		Calculates filter factor
		 */
		double filterFactor = 0;
		Column column = getColumn(lhs);
		int colCard = column.getColCard();
		
		if(operator.equalsIgnoreCase("="))
		{
			/*
			 * 		Equal predicate
			 */
			filterFactor = (float)(1 / (float)colCard);
		}
		else if(operator.equalsIgnoreCase("<"))
		{
			/*
			 * 			Less than predicate
			 */
			if(column.getColType().name().equalsIgnoreCase("CHAR"))
			{
				char charArray[] = rhs.toCharArray();
				int firstTermValue = getIntValueOfChar(charArray[0]);
				int secondTermValue = getIntValueOfChar(charArray[1]);
				int literalValue = firstTermValue * 26 + secondTermValue;
				
				charArray = column.getHiKey().toCharArray();
				firstTermValue = getIntValueOfChar(charArray[0]);
				secondTermValue = getIntValueOfChar(charArray[1]);
				int highKey = firstTermValue * 26 + secondTermValue;
				
				charArray = column.getLoKey().toCharArray();
				firstTermValue = getIntValueOfChar(charArray[0]);
				secondTermValue = getIntValueOfChar(charArray[1]);
				int lowKey = firstTermValue * 26 + secondTermValue;
				
				filterFactor = (double)((literalValue - lowKey) / (float)(highKey - lowKey));
			}
			else if(column.getColType().name().equalsIgnoreCase("INT"))
			{
				int literalValue = Integer.parseInt(rhs);
				int highKey = Integer.parseInt(column.getHiKey());
				int lowKey = Integer.parseInt(column.getLoKey());
				
				filterFactor = (double)((literalValue - lowKey) / (float)(highKey - lowKey));
			}
		}
		else if(operator.equals(">"))
		{
			/*
			 * 		Greater than predicate
			 */
			if(column.getColType().name().equalsIgnoreCase("CHAR"))
			{
				char charArray[] = rhs.toCharArray();
				int firstTermValue = getIntValueOfChar(charArray[0]);
				int secondTermValue = getIntValueOfChar(charArray[1]);
				int literalValue = firstTermValue * 26 + secondTermValue;
				
				charArray = column.getHiKey().toCharArray();
				firstTermValue = getIntValueOfChar(charArray[0]);
				secondTermValue = getIntValueOfChar(charArray[1]);
				int highKey = firstTermValue * 26 + secondTermValue;
				
				charArray = column.getLoKey().toCharArray();
				firstTermValue = getIntValueOfChar(charArray[0]);
				secondTermValue = getIntValueOfChar(charArray[1]);
				int lowKey = firstTermValue * 26 + secondTermValue;
				
				
				filterFactor = (double)((highKey - literalValue) / (float)(highKey - lowKey)); 
			}
			else if(column.getColType().name().equalsIgnoreCase("INT"))
			{
				int literalValue = Integer.parseInt(rhs);
				int highKey = Integer.parseInt(column.getHiKey());
				int lowKey = Integer.parseInt(column.getLoKey());
				
				filterFactor = (double)((highKey - literalValue) / (float)(highKey - lowKey)); 
			}
		}
		else if(operator.equalsIgnoreCase("I"))
		{
			/*
			 * 		In-list predicate
			 */
			int numOfElements = rhs.substring(rhs.indexOf("(")+1, rhs.indexOf(")")).split(",").length;
			filterFactor = (float)(numOfElements / (float)colCard);
		}
		
		return filterFactor;
	}
	
	private int getIntValueOfChar(char ch)
	{
		int value = 1;
		
		if((int)ch>=65 && (int)ch<=90)
		{
			for(char c='A'; c<='Z'; c++)
			{
				if(c==ch)
				{
					break;
				}
				value++;
			}
		}
		if((int)ch>=97 && (int)ch<=122)
		{
			for(char c='a'; c<='z'; c++)
			{
				if(c==ch)
				{
					break;
				}
				value++;
			}
		}
		
		return value;
	}
	
	private boolean checkPartOfPredicate(String partOfPredicate, List<Table> selectedTableList)
	{
		boolean result = true;
		
		String tableName = partOfPredicate.substring(0, partOfPredicate.indexOf("."));
		String columnName = partOfPredicate.substring(partOfPredicate.indexOf(".")+1);
		
		if(tableName.isEmpty() && columnName.isEmpty())
		{
			result = false;
		}
		else
		{
			/*
			 * 		Table exist or not
			 */
			boolean flag = false;
			Table table = new Table();
			for(Table t : selectedTableList)
			{
				if(t.getTableName().equalsIgnoreCase(tableName))
				{
					table = t;
					flag = true;
				}
			}
			
			if(flag)
			{
				/*
				 * 		Column exists or not
				 */
				flag = false;
				for(Column c : table.getColumns())
				{
					if(c.getColName().equalsIgnoreCase(columnName))
					{
						flag = true;
						
					}
				}
				
				if(flag)
				{
					result = true;
				}
				else
				{
					result = false;
				}
			}
			else
			{
				result = false;
			}
		}
		return result;
	}

	private String complement(String data, String colType)
	{
		/*
		 * 	Get complement of the data if column is in descending order
		 */
		String result = "";
		char arrayOfData[] = data.toCharArray();
		int highestAsciiValue, lowestAsciiValue;
		
		if(colType.equalsIgnoreCase("INT"))
		{
			highestAsciiValue = 57;
			lowestAsciiValue = 48;
			for(int i = 0; i < arrayOfData.length; i++)
			{
				int charIntValue = (int) (arrayOfData[i]);
				charIntValue = highestAsciiValue - charIntValue + lowestAsciiValue;
				arrayOfData[i] = (char)(charIntValue);
				result = result + arrayOfData[i];
			}
		}
		else if(colType.equalsIgnoreCase("CHAR"))
		{
			for(int i = 0; i < arrayOfData.length; i++)
			{
				char character = arrayOfData[i];
				
				if(character>='a' && character<='z')
				{
					highestAsciiValue = 122;
					lowestAsciiValue = 97;
					
					int charIntValue = (int) (arrayOfData[i]);
					charIntValue = highestAsciiValue - charIntValue + lowestAsciiValue;
					arrayOfData[i] = (char)(charIntValue);
					
					result = result + arrayOfData[i];
				}
				else if(character>='A' && character<='Z')
				{
					highestAsciiValue = 90;
					lowestAsciiValue = 65;
					
					int charIntValue = (int) (arrayOfData[i]);
					charIntValue = highestAsciiValue - charIntValue + lowestAsciiValue;
					arrayOfData[i] = (char)(charIntValue);
					
					result = result + arrayOfData[i];
				}
			}
		}
		return result;
	}
	
	
	/**
	 * INSERT INTO
	 * <table name>
	 * VALUES ( val1 , val2, .... ) ;
	 * 
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	private void insertInto(String sql, StringTokenizer tokenizer) throws Exception 
	{
		try 
		{
			String tok = tokenizer.nextToken();
			if (tok.equalsIgnoreCase("INTO")) 
			{
				tok = tokenizer.nextToken().trim().toUpperCase();
				Table table = null;
				for (Table tab : tables) 
				{
					if (tab.getTableName().equals(tok)) 
					{
						table = tab;
					 	break;
					}
				}

				if (table == null) 
				{
					throw new DbmsError("Table " + tok + " does not exist.");
				}

				tok = tokenizer.nextToken();
				if (tok.equalsIgnoreCase("VALUES")) 
				{
					tok = tokenizer.nextToken();
					if (tok.equalsIgnoreCase("(")) 
					{
						tok = tokenizer.nextToken();
						String values = String.format("%3s", table.getData().size() + 1) + " ";
						int colId = 0;
						boolean done = false;
						while (!done) 
						{
							if (tok.equals(")")) 
							{
								done = true;
								break;
							}
							else if (tok.equals(",")) 
							{
								// Continue to the next value
							}
							else 
							{
								if (colId == table.getNumColumns()) 
								{
									throw new DbmsError("Invalid number of values were given.");
								}

								Column col = table.getColumns().get(colId);

								if (tok.equals("-") && !col.isColNullable()) 
								{
									throw new DbmsError("A NOT NULL column cannot have null. '" + sql + "'.");
								}

								if (col.getColType() == Column.ColType.INT) 
								{
									try 
									{
										if(!tok.equals("-")) 
										{
											int temp = Integer.parseInt(tok);
										}
									} 
									catch (Exception ex) 
									{
										throw new DbmsError("An INT column cannot hold a CHAR. '" + sql + "'.");
									}

									tok = String.format("%10s", tok.trim());
								}
								else if (col.getColType() == Column.ColType.CHAR) 
								{
									int length = tok.length();
									if (length > col.getColLength()) 
									{
										throw new DbmsError("A CHAR column cannot exceede its length. '" + sql + "'.");
									}

									tok = String.format("%-" + col.getColLength() + "s", tok.trim());
								}

								values += tok + " ";
								colId++;
							}
							tok = tokenizer.nextToken().trim();
						}

						if (colId != table.getNumColumns()) 
						{
							throw new DbmsError("Invalid number of values were given.");
						}

						// Check for the semicolon
						tok = tokenizer.nextToken();
						if (!tok.equals(";")) 
						{
							throw new NoSuchElementException();
						}

						// Check if there are more tokens
						if (tokenizer.hasMoreTokens()) 
						{
							throw new NoSuchElementException();
						}

						// insert the value to table
						ArrayList<Index> indexList = table.getIndexes();
						boolean shouldInsertRow = true;
						
						if(indexList.isEmpty())
						{
							/*
							 * 	No index on table
							 */
							table.addData(values);
							out.println("One line was saved to the table. " + table.getTableName() + ": " + values);
						}
						else
						{
							/*
							 * 	Index present
							 */
							ArrayList<String> dataList =  table.getData();
							boolean descOrder = false;
							String dataRow1 = "", dataRow2 = "";
							
							for(Index index : indexList)
							{
								/*
								 * 	Checking unique key constraint for each index
								 */
								ArrayList<Index.IndexKeyDef> idxKeyList = index.getIdxKey();
								boolean isIndexUnique = index.getIsUnique();
								String[] newRecordArray = values.split("\\s+");
								
								if(isIndexUnique)
								{
									for(String tableData : dataList)
									{
										String[] tableDataArray = tableData.split("\\s+");
										
										dataRow1 = "";
										dataRow2 = "";
										
										for(Index.IndexKeyDef idxKey : idxKeyList)
										{
											colId = idxKey.colId;
											descOrder = idxKey.descOrder;
											
											dataRow1 = dataRow1 + tableDataArray[colId+1] + " ";
											dataRow2 = dataRow2 + newRecordArray[colId+1] + " ";
										}
										
										if(dataRow1.equalsIgnoreCase(dataRow2))
										{
											shouldInsertRow = false;
											break;
										}
									}
								}
								
								if(!shouldInsertRow)
									break;
							}
								
							if(shouldInsertRow)
							{
								/*
								 * 	Row is inserted
								 */
								table.addData(values);
								out.println("One line was saved to the table. " + table.getTableName() + ": " + values);
								
								String keyValue = "", data = "", colType = "";
								int idxColsPos = 0, colLength = 0;
								String[] newRecordArray = values.split("\\s+");
								colId = 0;
								int rid = table.getData().size();
								
								for(Index idx : indexList)
								{
									ArrayList<Index.IndexKeyDef> idxKeyList = idx.getIdxKey();
									keyValue = "";
									String[] columnDataArray = new String[idxKeyList.size()];
									
									for(Index.IndexKeyDef idxKey : idxKeyList)
									{
										colId = idxKey.colId;
										descOrder = idxKey.descOrder;
										idxColsPos = idxKey.idxColPos;
										
										/*
										 *	Get column length
										 */
										ArrayList<Column> columnList = table.getColumns();
										for(Column c : columnList)
										{
											if(colId==c.getColId())
											{
												colId = c.getColId();
												colLength = c.getColLength();
												colType = c.getColType().name();
												break;
											}
										}
										
										data = newRecordArray[colId+1];
										
										if(descOrder)
										{
											/*
											 * 	Descending order
											 */
											if(data.equalsIgnoreCase("-"))
											{
												data = "~";
												if(colType.equalsIgnoreCase("INT"))
												{
													data = String.format("%-10s", data);
												}
												else if(colType.equalsIgnoreCase("CHAR"))
												{
													data = String.format("%-" + colLength + "s", data);
												}
												
												columnDataArray[idxColsPos-1] = data;
											}
											else
											{
												if(colType.equalsIgnoreCase("INT"))
												{
													data = String.format("%010d", Integer.parseInt(data));
													data = complement(data, colType);
													columnDataArray[idxColsPos-1] = data;
												}
												else if(colType.equalsIgnoreCase("CHAR"))
												{
													data = complement(data, colType);
													data = String.format("%-" + colLength + "s", data);
													columnDataArray[idxColsPos-1] = data;
												}
											}
										}
										else
										{
											/*
											 * 	Ascending order
											 */
											if(data.equalsIgnoreCase("-"))
											{
												data = "~";
												if(colType.equalsIgnoreCase("INT"))
												{
													data = String.format("%10s", data);
												}
												else if(colType.equalsIgnoreCase("CHAR"))
												{
													data = String.format("%-" + colLength + "s", data);
												}
												
												columnDataArray[idxColsPos-1] = data;
											}
											else
											{
												if(colType.equalsIgnoreCase("INT"))
												{
													data = String.format("%010d", Integer.parseInt(data));
													columnDataArray[idxColsPos-1] = data;
												}
												else if(colType.equalsIgnoreCase("CHAR"))
												{
													data = String.format("%-" + colLength + "s", data);
													columnDataArray[idxColsPos-1] = data;
												}
											}
										}
										
									}
									for(int t = 0; t < columnDataArray.length; t++)
									{
										keyValue = keyValue + columnDataArray[t];
									}
									
									idx.addKey(idx.new IndexKeyVal(rid, keyValue));
									Collections.sort(idx.getKeys(), DBMS.sort());
								}
							}
							else
							{
								/*
								 * 	Row is not inserted because of index unique key constraint
								 */
								throw new DbmsError("Can not execute INSERT INTO statement. Unique key constraint. '"+sql+"'.");
							}
						}
					} 
					else 
					{
						throw new NoSuchElementException();
					}
				} 
				else 
				{
					throw new NoSuchElementException();
				}
			} 
			else 
			{
				throw new NoSuchElementException();
			}
		} 
		catch (NoSuchElementException ex) 
		{
			throw new DbmsError("Invalid INSERT INTO statement. '" + sql + "'.");
		}
	}

	/**
	 * DROP TABLE
	 * <table name>
	 * ;
	 * 
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	private void dropTable(String sql, StringTokenizer tokenizer) throws Exception 
	{
		try 
		{
			// Get table name
			String tableName = tokenizer.nextToken();

			// Check for the semicolon
			String tok = tokenizer.nextToken();
			if (!tok.equals(";")) 
			{
				throw new NoSuchElementException();
			}

			// Check if there are more tokens
			if (tokenizer.hasMoreTokens()) 
			{
				throw new NoSuchElementException();
			}

			// Delete the table if everything is ok
			boolean dropped = false;
			for (Table table : tables) 
			{
				if (table.getTableName().equalsIgnoreCase(tableName)) 
				{
					for(Index index : table.getIndexes())
					{
						index.delete = true;
					}
					table.delete = true;
					dropped = true;
					break;
				}
			}

			if (dropped) 
			{
				out.println("Table " + tableName + " was dropped.");
			} 
			else 
			{
				throw new DbmsError("Table " + tableName + "does not exist. '" + sql + "'."); 
			}
		}
		catch (NoSuchElementException ex) 
		{
			throw new DbmsError("Invalid DROP TABLE statement. '" + sql + "'.");
		}
	}
	
	private void dropIndex(String sql, StringTokenizer tokenizer) throws Exception
	{
		try
		{
			// Get index name
			String idxName = tokenizer.nextToken();
			
			//	Get semicolon ;
			String token = tokenizer.nextToken();
			if(!token.equalsIgnoreCase(";"))
			{
				throw new NoSuchElementException();
			}
			
			//	Check if there are more tokens
			if (tokenizer.hasMoreTokens()) 
			{
				throw new NoSuchElementException();
			}
			
			//	Check if index exist
			boolean isIndexExist = false;
			for(Table table : tables)
			{
				ArrayList<Index> indexList = table.getIndexes();
				for(Index index : indexList)
				{
					String indexName = index.getIdxName();
					
					if(idxName.equalsIgnoreCase(indexName))
					{
						int numIndexes = table.getNumIndexes() - 1;
						table.setNumIndexes(numIndexes);
						index.delete = true;
						isIndexExist = true;
						break;
					}
					
					if(isIndexExist)
					{
						break;
					}
				}
			}
			
			if(isIndexExist)
			{
				out.println("Index " + idxName + " was dropped.");
			}
			else
			{
				throw new DbmsError("Index " + idxName + "does not exist. '" + sql + "'.");
			}
		}
		catch (NoSuchElementException ex) 
		{
			throw new DbmsError("Invalid DROP TABLE statement. '" + sql + "'.");
		}
	}
	
	private void executeRunstats(String tableName, String sql, StringTokenizer tokenizer) throws Exception
	{
		//	Check for ; semicolon
		if(tokenizer.hasMoreElements())
		{
			String tok = tokenizer.nextToken();
			
			if(tok.equalsIgnoreCase(";"))
			{
				//	Check whether table exist 
				Table table = new Table();
				boolean isTableExist = false;
				for(Table t : tables)
				{
					String tName = t.getTableName();
					if(tName.equalsIgnoreCase(tableName))
					{
						table = t;
						isTableExist = true;
						break;
					}
				}
				
				if(isTableExist)
				{
					ArrayList<String> dataList = table.getData();
					if(dataList.size()!=0)
					{
						ArrayList<Column> columnList = table.getColumns();
						
						int tableCard = dataList.size();
						table.setTableCard(tableCard);
						
						int colCard, colId, count;
						String hiKey;
						String loKey;
						
						for(Column column : columnList)
						{
							colId = column.getColId();
							List<String> uniqueData = new ArrayList<String>();
							count = 0;
							
							for(String data : dataList)
							{
								String columnDataArray[] = data.split("\\s+");
								String columnData = columnDataArray[colId+1];
								
								
								if(!uniqueData.contains(columnData))
									uniqueData.add(columnData);
							}
							colCard = uniqueData.size();
							
							Collections.sort(uniqueData, new Comparator<String>() {

								@Override
								public int compare(String s1, String s2) 
								{
									// TODO Auto-generated method stub
									return s1.compareToIgnoreCase(s2);
								}
								
							});
							loKey = uniqueData.get(0);
							hiKey = uniqueData.get(uniqueData.size()-1);
							
							column.setColCard(colCard);
							column.setHiKey(hiKey);
							column.setLoKey(loKey);
						}
					}
					else
					{
						throw new DbmsError("Table " + tableName + " is empty. '" + sql + "'.");
					}
				}
				else
				{
					throw new DbmsError("Invalid RUNSTATS statement. Table " + tableName + " does not exist. '" + sql + "'.");
				}
			}
			else
			{
				throw new DbmsError("Invalid RUNSTATS statement. '" + sql + "'.");
			}
		}
		else
		{
			throw new DbmsError("Invalid RUNSTATS statement. '" + sql + "'.");
		}
	}

	private void printRunstats(String tableName) 
	{
		for (Table table : tables) 
		{
			if (table.getTableName().equals(tableName)) 
			{
				out.println("TABLE CARDINALITY: " + table.getTableCard());
				for (Column column : table.getColumns()) 
				{
					out.println(column.getColName());
					out.println("\tCOLUMN CARDINALITY: " + column.getColCard());
					out.println("\tCOLUMN HIGH KEY: " + column.getHiKey());
					out.println("\tCOLUMN LOW KEY: " + column.getLoKey());
				}
				break;
			}
		}
	}

	private void storeTableFile(Table table) throws FileNotFoundException 
	{
		File tableFile = new File(TABLE_FOLDER_NAME, table.getTableName() + TABLE_FILE_EXT);

		// Delete the file if it was marked for deletion
		if (table.delete)
		{
			try 
			{
				tableFile.delete();
			}
			catch (Exception ex) 
			{
				out.println("Unable to delete table file for " + table.getTableName() + ".");
			}
			
			// Delete the index files too
			for (Index index : table.getIndexes()) 
			{
				File indexFile = new File(TABLE_FOLDER_NAME, table.getTableName() + index.getIdxName() + INDEX_FILE_EXT);
				
				try 
				{
					indexFile.delete();
				}
				catch (Exception ex)
				{
					out.println("Unable to delete table file for " + indexFile.getName() + ".");
				}
			}
		}
		else 
		{
			// Create the table file writer
			PrintWriter out = new PrintWriter(tableFile);

			// Write the column descriptors
			out.println(table.getNumColumns());
			for (Column col : table.getColumns()) 
			{
				if (col.getColType() == Column.ColType.INT) 
				{
					out.println(col.getColName() + " I " + col.isColNullable());
				}
				else if (col.getColType() == Column.ColType.CHAR) 
				{
					out.println(col.getColName() + " C" + col.getColLength() + " " + col.isColNullable());
				}
			}

			// Write the index info
			out.println(table.getNumIndexes());
			for (Index index : table.getIndexes()) 
			{
				if(!index.delete) 
				{
					String idxInfo = index.getIdxName() + " " + index.getIsUnique() + " ";

					for (Index.IndexKeyDef def : index.getIdxKey()) 
					{
						idxInfo += def.colId;
						if (def.descOrder) 
						{
							idxInfo += "D ";
						} 
						else 
						{
							idxInfo += "A ";
						}
					}
					out.println(idxInfo);
				}
			}

			// Write the rows of data
			out.println(table.getData().size());
			for (String data : table.getData()) 
			{
				out.println(data);
			}

			// Write RUNSTATS
			out.println("STATS TABCARD " + table.getTableCard());
			for (int i = 0; i < table.getColumns().size(); i++) 
			{
				Column col = table.getColumns().get(i);
				if(col.getHiKey() == null)
					col.setHiKey("-");
				if(col.getLoKey() == null)
					col.setLoKey("-");
				out.println("STATS COLCARD " + i + " " + col.getColCard() + " " + col.getHiKey() + " " + col.getLoKey());
			}
			
			out.flush();
			out.close();
		}

		// Save indexes to file
		for (Index index : table.getIndexes()) 
		{

			File indexFile = new File(TABLE_FOLDER_NAME, table.getTableName()+ index.getIdxName() + INDEX_FILE_EXT);

			// Delete the file if it was marked for deletion
			if (index.delete) 
			{
				try 
				{
					indexFile.delete();
				}
				catch (Exception ex) 
				{
					out.println("Unable to delete index file for " + indexFile.getName() + ".");
				}
			} 
			else 
			{
				PrintWriter out = new PrintWriter(indexFile);
				String idxInfo = index.getIdxName() + " " + index.getIsUnique() + " ";

				// Write index definition
				for (Index.IndexKeyDef def : index.getIdxKey()) 
				{
					idxInfo += def.colId;
					if (def.descOrder) 
					{
						idxInfo += "D ";
					}
					else 
					{
						idxInfo += "A ";
					}
				}
				out.println(idxInfo);

				// Write index keys
				out.println(index.getKeys().size());
				for (Index.IndexKeyVal key : index.getKeys()) 
				{
					String rid = String.format("%3s", key.rid);
					out.println(rid + " '" + key.value + "'");
				}

				out.flush();
				out.close();
			}
		}
	}
}