/*
 * STUFF I WANT TO KNOW
 * - Total Population
 * - Adult Population
 * - Child Population
 * - Oldest Bug
 */
 
class SimpleData implements ILine2DEquation{
  private double data;
  private double multiplier;
  
  SimpleData(double multiplier){
    this.data = 0;
    this.multiplier = multiplier;
  }
  
  public double computePoint(double x,int pos) {
    return data * multiplier;
  }
  
  public void set(double input){
    data = input;
  }
  
  public void add(double input){
    data += input;
  }
  
  public void reset(){
    data = 0;
  }
  
  public double getRawData(){
    return data;
  }
  
  public int getRawDataInt(){
    return (int)data;
  }
  
  public double getMultiplier(){
    return multiplier;
  }
  
  public double getMData(){
    return data * multiplier;
  }
} 
