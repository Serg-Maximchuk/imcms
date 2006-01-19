package imcode.server.benchmark;

public class Average {
    private String unit ;
    private long total ;
    private int count ;

    public Average(String unit) {
        this.unit = unit;
    }

    public void add(long time, int count) {
        total+= time ;
        this.count+= count;
    }

    public float getAverage() {
        if (count == 0) {
            return 0 ;
        }
        return (float) ( total / (double) count ) ;
    }

    public int getCount() {
        return count ;
    }

    public long getTotal() {
        return total;
    }

    public String toString() {
        return "("+total+"ms/"+count+"="+getAverage()+"ms/"+unit+")" ;
    }
}
