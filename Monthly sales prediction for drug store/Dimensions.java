import java.util.HashMap;

public class Dimensions {

	int store;
	int month;
	int year;
	int promo=0;
	int holiday=0;
	int competition=0;
	int promo2=0;
	int promo2since=0;
	int promoInterval=0;
	HashMap<String,Integer> monthMap = new HashMap<String,Integer>();
	public Dimensions()
	{
		monthMap.put("Jan", 1);
		monthMap.put("Feb", 2);
		monthMap.put("Mar", 3);
		monthMap.put("Apr", 4);
		monthMap.put("May", 5);
		monthMap.put("Jun", 6);
		monthMap.put("Jul", 7);
		monthMap.put("Aug", 8);
		monthMap.put("Sept", 9);
		monthMap.put("Oct", 10);
		monthMap.put("Nov", 11);
		monthMap.put("Dec", 12);
		
		
	}
	public void setStore(int store)
	{
	   this.store = store;	
	}
	public int getStore()
	{
	   return this.store ;	
	}
	public void setPromo(int promo)
	{
	   this.promo = promo;	
	}
	public void setMonth(int month)
	{
	   this.month = month;	
	}
	public int getMonth()
	{
	  return this.month;	
	}
	public void setYear(int year)
	{
	   this.year = year;	
	}
	public int getYear()
	{
	  return this.year;	
	}
	public int getPromo()
	{
	  return this.promo;	
	}
	public void setHoliday(int holiday)
	{
	   this.holiday = holiday;	
	}
	public int getHoliday()
	{
	  return this.holiday;	
	}
	public void setCompetition(int competition)
	{
	   this.competition = competition;	
	}
	public int getCompetition()
	{
	  return this.competition;	
	}
	public void setPromo2(int promo2)
	{
	   this.promo2 = promo2 ;	
	}
	public int getPromo2()
	{
	  return this.promo2;	
	}
	public void setPromo2Since(int promo2Since)
	{
	   this.promo2since = promo2Since ;	
	}
	public int getPromo2Since()
	{
	  return this.promo2since;	
	}
	public void setPromoInterval(String interval)
	{
	  interval=interval.trim();
	  if(interval.contains("-"))
		  this.promoInterval=0;
	  else
	  {
	  String tokens[]=interval.split("_");
	  int a= monthMap.get(tokens[0]);
	  int b= monthMap.get(tokens[1]);
	  int c= monthMap.get(tokens[2]);
	  int d =monthMap.get(tokens[3]);
	  this.promoInterval=a+b+c+d;
	  }
	  
	}
	public int getPromoInterval()
	{
	  return this.promoInterval;	
	}
}

