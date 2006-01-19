package imcode.server.benchmark;

public class BenchmarkAverages {
    private Average queryAverage = new Average("query");
    private Average rowAverage = new Average("row");
    private Average totalAverage = new Average("total");

    public String toString() {
        Average[] averages = new Average[] {
                totalAverage, queryAverage, rowAverage,
        };
        StringBuffer buffer = new StringBuffer("\n");
        for ( int i = 0; i < averages.length; i++ ) {
            Average average = averages[i];
            if (average.getCount() > 0) {
                buffer.append(average).append('\n') ;
            }
        }
        return buffer.toString() ;
    }

    public Average getRowAverage() {
        return rowAverage;
    }

    public Average getQueryAverage() {
        return queryAverage;
    }

    public Average getTotalAverage() {
        return totalAverage;
    }

}
