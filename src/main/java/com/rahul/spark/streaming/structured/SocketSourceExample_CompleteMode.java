package com.rahul.spark.streaming.structured;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;

import java.util.Arrays;

public class SocketSourceExample_CompleteMode {
    public static void main(String[] args) throws StreamingQueryException {
        SparkSession sparkSession = SparkSession.builder().master("local[*]").appName("WordCountApp").getOrCreate();

        sparkSession.sparkContext().setLogLevel("ERROR");

        /*
        Create DataFrame representing the stream of input lines from connection to localhost:9999
        This 'lines' DataFrame represents an unbounded table containing the streaming text data.
        This table contains one column of strings named “value”, and each line in the streaming text data becomes a row in the table.
         */
        Dataset<Row> lines = sparkSession.readStream().format("socket").option("host", "localhost").option("port", 9999).load();

        /*
        Split the lines into words.
        We have converted the DataFrame to a Dataset of String using .as(Encoders.STRING()), so that we can apply the
        flatMap operation to split each line into multiple words.
        The resultant words Dataset contains all the words.
         */
        Dataset<String> words = lines.as(Encoders.STRING())
                .flatMap((FlatMapFunction<String, String>) x -> Arrays.asList(x.split(" ")).iterator(), Encoders.STRING());

        /*
        Finally, we have defined the wordCounts DataFrame by grouping by the unique values in the Dataset and counting them.
         */
        Dataset<Row> wordCounts = words.groupBy("value").count();

        /*
        Start running the query that prints the running counts to the console.
        We have now set up the query on the streaming data.
        We set it up to print the complete set of counts (specified by outputMode("complete")) to the console every time they are updated.
         */
        StreamingQuery query = wordCounts.writeStream().outputMode("complete").format("console").start();

        /*
        We have decided to wait for the termination of the query using awaitTermination() to prevent the process from
        exiting while the query is active.
         */
        query.awaitTermination();
    }
}
