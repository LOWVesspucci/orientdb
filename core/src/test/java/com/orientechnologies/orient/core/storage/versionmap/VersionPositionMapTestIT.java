package com.orientechnologies.orient.core.storage.versionmap;

import com.orientechnologies.common.io.OFileUtils;
import com.orientechnologies.orient.core.db.*;
import com.orientechnologies.orient.core.index.engine.OBaseIndexEngine;
import com.orientechnologies.orient.core.storage.impl.local.OAbstractPaginatedStorage;
import com.orientechnologies.orient.core.storage.impl.local.paginated.atomicoperations.OAtomicOperation;
import com.orientechnologies.orient.core.storage.impl.local.paginated.atomicoperations.OAtomicOperationsManager;
import com.orientechnologies.orient.core.storage.index.versionmap.OVersionPositionMap;
import com.orientechnologies.orient.core.storage.index.versionmap.OVersionPositionMapV0;
import java.io.File;
import java.util.Random;
import org.junit.*;

public class VersionPositionMapTestIT {
  public static final String DIR_NAME = "/versionPositionMapTest";
  public static final String DB_NAME = "versionPositionMapTest";
  private static OrientDB orientDB;
  private static OAtomicOperationsManager atomicOperationsManager;
  private static OAbstractPaginatedStorage storage;
  private static String buildDirectory;

  private OVersionPositionMapV0 versionPositionMap;

  @BeforeClass
  public static void beforeClass() {
    buildDirectory = System.getProperty("buildDirectory");
    if (buildDirectory == null) {
      buildDirectory = "./target" + DIR_NAME;
    } else {
      buildDirectory += DIR_NAME;
    }
    OFileUtils.deleteRecursively(new File(buildDirectory));
    orientDB = new OrientDB("plocal:" + buildDirectory, OrientDBConfig.defaultConfig());
    if (orientDB.exists(DB_NAME)) {
      orientDB.drop(DB_NAME);
    }
    orientDB.create(DB_NAME, ODatabaseType.PLOCAL);

    ODatabaseSession databaseSession = orientDB.open(DB_NAME, "admin", "admin");
    storage = (OAbstractPaginatedStorage) ((ODatabaseInternal<?>) databaseSession).getStorage();
    atomicOperationsManager = storage.getAtomicOperationsManager();
    databaseSession.close();
  }

  @AfterClass
  public static void afterClass() {
    orientDB.drop(DB_NAME);
    orientDB.close();
    OFileUtils.deleteRecursively(new File(buildDirectory));
  }

  @Before
  public void setUp() throws Exception {
    final String name = "Person.name";
    versionPositionMap =
        new OVersionPositionMapV0(storage, name, name + ".cbt", OVersionPositionMap.DEF_EXTENSION);
    final OAtomicOperation atomicOperation = atomicOperationsManager.startAtomicOperation(null);
    versionPositionMap.create(atomicOperation);
    versionPositionMap.open();
  }

  @After
  public void tearDown() throws Exception {
    final OAtomicOperation atomicOperation = atomicOperationsManager.getCurrentOperation();
    versionPositionMap.delete(atomicOperation);
    OAtomicOperationsManager.alarmClearOfAtomicOperation();
  }

  @Test
  public void testIncrementVersion() throws Exception {
    final int maxVPMSize = OBaseIndexEngine.DEFAULT_VERSION_ARRAY_SIZE;
    for (int hash = 0; hash <= maxVPMSize; hash++) {
      final int version = versionPositionMap.getVersion(hash);
      versionPositionMap.updateVersion(hash);
      Assert.assertEquals(version + 1, versionPositionMap.getVersion(hash));
    }
  }

  @Test
  public void testMultiIncrementVersion() throws Exception {
    final int maxVPMSize = OBaseIndexEngine.DEFAULT_VERSION_ARRAY_SIZE;
    final int maxVersionNumber = 100;
    for (int hash = 0; hash <= maxVPMSize; hash++) {
      for (int j = 0; j < maxVersionNumber; j++) {
        versionPositionMap.updateVersion(hash);
      }
      Assert.assertEquals(maxVersionNumber, versionPositionMap.getVersion(hash));
    }
  }

  @Test
  public void testRandomIncrementVersion() throws Exception {
    final int maxVPMSize = OBaseIndexEngine.DEFAULT_VERSION_ARRAY_SIZE;
    final long seed = System.nanoTime();
    System.out.printf("incrementVersion seed :%d%n", seed);
    final Random random = new Random(seed);
    for (int i = 0; i <= maxVPMSize; i++) {
      int randomNum = 0 + random.nextInt((maxVPMSize - 0) + 1);
      final int version = versionPositionMap.getVersion(randomNum);
      versionPositionMap.updateVersion(randomNum);
      Assert.assertEquals(version + 1, versionPositionMap.getVersion(randomNum));
    }
  }
}
