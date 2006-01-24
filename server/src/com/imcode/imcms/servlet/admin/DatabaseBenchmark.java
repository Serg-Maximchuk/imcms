package com.imcode.imcms.servlet.admin;

import com.imcode.db.benchmark.BenchmarkDatabase;
import com.imcode.db.benchmark.BenchmarkAverages;
import imcode.server.Imcms;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.TransformerUtils;
import org.apache.commons.collections.comparators.TransformingComparator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class DatabaseBenchmark extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BenchmarkDatabase benchmarkDatabase = (BenchmarkDatabase) Imcms.getServices().getDatabase();
        Map benchmarks = benchmarkDatabase.getBenchmarks();
        List list = new ArrayList(benchmarks.size());
        list.addAll(benchmarks.entrySet()) ;
        Transformer transformer = TransformerUtils.chainedTransformer(new MapEntryToValueTransformer(), new AverageToTotalTransformer());
        TransformingComparator comparator = new TransformingComparator(transformer);
        Collections.sort(list, comparator);

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        for ( Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iterator.next();
            out.println(entry);
            out.println();
        }
        out.flush();
        out.close();
    }

    private static class MapEntryToValueTransformer implements Transformer {

        public Object transform(Object input) {
            Map.Entry entry = (Map.Entry) input ;
            return entry.getValue() ;
        }
    }
    
    private static class AverageToTotalTransformer implements Transformer {

        public Object transform(Object input) {
            BenchmarkAverages averages = (BenchmarkAverages) input;
            return new Long(averages.getTotalAverage().getTotal()) ;
        }
    }
}
