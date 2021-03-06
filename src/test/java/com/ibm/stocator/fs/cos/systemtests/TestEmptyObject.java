/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ibm.stocator.fs.cos.systemtests;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.junit.BeforeClass;
import org.junit.Test;
import static com.ibm.stocator.fs.common.FileSystemTestUtils.dumpStats;

/**
 * Test Globber operations on the data that was not created by Stocator
 */
public class TestEmptyObject extends COSFileSystemBaseTest {

  private static Path[] sTestData;
  private static Path[] sEmptyFiles;
  private static byte[] sData = "This is file".getBytes();
  private static Hashtable<String, String> sConf = new Hashtable<String, String>();

  @BeforeClass
  public static void setUpClass() throws Exception {
    sConf.put("fs.stocator.glob.bracket.support", "true");
    createCOSFileSystem(sConf);
    if (sFileSystem != null) {
      createTestData();
    }
  }

  private static void createTestData() throws IOException {

    sTestData = new Path[] {
        new Path(sBaseURI + "/test1/year=2012/month=1/data.csv"),
        new Path(sBaseURI + "/test1/year=2012/month=10/data.csv"),
        new Path(sBaseURI + "/test1/year=2012/month=11/data.csv"),
        new Path(sBaseURI + "/test2/_temporary/0/"
            + "_temporary/attempt_20191028154709_0001_m_000157_0/"
            + "COL2=myvalue1/COL1=my.org1/part-00157-ba0dc797-0230-"
            + "4037-b7f4-592fd006da8a.c000.snappy.parquet")};

    sEmptyFiles = new Path[] {
        new Path(sBaseURI + "/test1/year=2012/month=10")};

    for (Path path : sTestData) {
      createFile(path, sData);
    }
    for (Path path : sEmptyFiles) {
      createEmptyFile(path);
    }

  }

  @Test
  public void testListGlobber() throws Exception {
    Path lPath = new Path(sBaseURI + "/test1/year=2012/");
    FileStatus[] res = sFileSystem.listStatus(lPath);
    System.out.println("Stocator returned list of size: " + res.length);
    for (FileStatus fs: res) {
      System.out.println("Stocator" + fs.getPath() + " directory " + fs.isDirectory());
    }
    assertEquals(dumpStats(lPath.toString(), res), sTestData.length - 1, res.length);
  }

  @Test
  public void testFSWithDots() throws Exception {
    Path lPath = new Path(sBaseURI + "/test2/_temporary/0/_temporary/"
        + "attempt_20191028154709_0001_m_000157_0/COL2=myvalue1/"
        + "COL1=my.org1/part-00157-ba0dc797-0230-4037-b7f4-592fd006da8a.c000.snappy.parquet");
    FileStatus fs = sFileSystem.getFileStatus(lPath);
    Path expectedPath = new Path(sBaseURI +  "/test2/COL2=myvalue1/COL1=my.org1/"
        + "part-00157-ba0dc797-0230-4037-b7f4-592fd006da8a-attempt_20191028154709_"
        + "0001_m_000157_0.c000.snappy.parquet");
    assertEquals("Wrong get file status on the value with dot",
        expectedPath.toString(), fs.getPath().toString());
  }

}
