package com.rahul.spark.streaming.structured;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF0;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.sql.Timestamp;

import static org.apache.spark.sql.functions.callUDF;

public class UDFMimicEventTime {
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

        Dataset<Row> fileStreamDF = sparkSession.readStream()
                .option("header", "false")
                .option("maxFilesPerTrigger", 1)
                .schema(schema)
                .csv("/home/rahulbhatia/Rahul_Bhatia/intellij_workspace/SparkStructuredStreaming/datasets/droplocation");

        sparkSession.udf().register("add_timestamp", (UDF0<String>) () -> {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            return timestamp.toString();
        }, DataTypes.StringType);

        Dataset<Row> fileStreamWithTimestampDF = fileStreamDF.withColumn("timestamp", callUDF("add_timestamp"));

        Dataset<Row> trimmedDF = fileStreamWithTimestampDF.select("borough", "major_category", "value", "timestamp");

        trimmedDF.writeStream()
                .outputMode("append")
                .format("console")
                .option("truncate", "false")
                /*
                we have explicitly specified a processing time trigger.
                We want to process the input stream every 5 seconds.
                This trigger determines how often the processing of the input stream will take place.
                 */
                .trigger(Trigger.ProcessingTime("5 seconds"))
                .start()
                .awaitTermination();
    }
}
