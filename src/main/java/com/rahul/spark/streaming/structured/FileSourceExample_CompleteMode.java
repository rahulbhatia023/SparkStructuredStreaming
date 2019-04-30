package com.rahul.spark.streaming.structured;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

public class FileSourceExample_CompleteMode {
    public static void main(String[] args) throws StreamingQueryException {
        SparkSession sparkSession = SparkSession.builder().master("local[*]").appName("LondonCrimeRecordsReader").getOrCreate();

        sparkSession.sparkContext().setLogLevel("ERROR");

        StructType schema = new StructType(new StructField[]{
                new StructField("lsoa_code", DataTypes.StringType, true, Metadata.empty()),
                new StructField("borough", DataTypes.StringType, true, Metadata.empty()),
                new StructField("major_category", DataTypes.StringType, true, Metadata.empty()),
                new StructField("minor_category", DataTypes.StringType, true, Metadata.empty()),
                new StructField("value", DataTypes.StringType, true, Metadata.empty()),
                new StructField("year", DataTypes.StringType, true, Metadata.empty()),
                new StructField("month", DataTypes.StringType, true, Metadata.empty())
        });

        /*
        The maxFilesPerTrigger is the number of files read in by the fileStream source at one time.
        This serves as a kind of rate limiting for our input file source.
        This basically means that each time we run the queries to transform our input data,
        the data will be limited to no more than what is present in a single file that we added to the droplocation.
         */
        Dataset<Row> fileStreamDF = sparkSession.readStream()
                .option("header", "false")
                .option("maxFilesPerTrigger", 1)
                .schema(schema)
                .csv("/home/rahulbhatia/Rahul_Bhatia/intellij_workspace/SparkStructuredStreaming/datasets/droplocation");

        Dataset<Row> recordsPerBorough = fileStreamDF.groupBy("borough").count().orderBy("count");

        recordsPerBorough.writeStream()
                .outputMode("complete")
                .format("console")
                .option("truncate", "false")
                .option("numRows", 30)
                .start()
                .awaitTermination();
    }
}
