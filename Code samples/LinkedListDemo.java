class Node
{
	/*
		Linked list consist of number of nodes
		Each node has 2 fields 
		1 data field : stores actual data/information
		2 node field : stores reference of next node
	*/
	String data;
	Node next;
	
	/*
		Constructor
	*/
	
	public Node()
	{}
	
	public Node(String data, Node node)
	{
		this.data = data;
		this.next = node;
	}
	
	public Node addNode(Node head, String data)
	{
		if(head==null)
		{
			Node n = new Node();
			n.data = data;
			n.next = null;
			head = n;
		}
		else
		{
			Node node = head;
			while(node.next!=null)
			{
				node = node.next;
			}
			Node n = new Node();
			n.data = data;
			n.next = null;
			node.next = n;
		}
		return head;
	}
	
	/*
		Getters and Setters method
	*/
	public void setNode(Node node)
	{
		this.next = node;
	}
	
	public Node getNode()
	{
		return this.next;
	}
	
	public void setData(String data)
	{
		this.data = data;
	}
	
	public String getData()
	{
		return this.data;
	}
}

public class LinkedListDemo
{
	/*
		Assuming linked list is consist of 1500 nodes
		This value can be changed or to make dynamic we can use list to store list of nodes
	*/
	
	int noOfNodes = 1500;
	Node linkedList[] = new Node[noOfNodes];
	
	Node firstNode = null;
	Node lastNode = null;
	
	int currentNoOfNodes = 0;
	
	public void addNode(String data, Node previousNode, Node nextNode)
	{
		if(linkedList.length==0)
		{
			/*
				Adding first node in linked list
			*/
			Node node = new Node(data, nextNode);
			firstNode = node;
			lastNode = node;
			
			linkedList[currentNoOfNodes] = node;
			currentNoOfNodes++;
		}
		else if(previousNode==null)
		{
			/*
				Adding node at first position
			*/
			if(nextNode==null)
			{
				System.out.println("Cannot create node at 1st position. Illegal reference to node.");
			}
			else
			{
				Node node = new Node(data, nextNode);
				firstNode = node;
				
				linkedList[currentNoOfNodes] = node;
				currentNoOfNodes++;
			}
		}
	}
	
}