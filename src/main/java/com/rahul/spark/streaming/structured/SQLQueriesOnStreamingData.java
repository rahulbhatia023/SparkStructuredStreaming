package com.rahul.spark.streaming.structured;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

public class SQLQueriesOnStreamingData {
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
                .option("maxFilesPerTrigger", 2)
                .schema(schema)
                .csv("/home/rahulbhatia/Rahul_Bhatia/intellij_workspace/SparkStructuredStreaming/datasets/droplocation");

        fileStreamDF.createOrReplaceTempView("LondonCrimeData");

        Dataset<Row> categoryDF = sparkSession.sql("SELECT major_category, value FROM LondonCrimeData WHERE year = '2016'");

        categoryDF.writeStream()
                .format("console")
                .option("truncate", "false")
                .option("numRows", 30)
                .start()
                .awaitTermination();
    }
}
