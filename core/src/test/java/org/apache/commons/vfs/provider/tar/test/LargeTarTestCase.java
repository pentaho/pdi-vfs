package org.apache.commons.vfs.provider.tar.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.cache.SoftRefFilesCache;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs.provider.tar.TarFileProvider;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class LargeTarTestCase {
  private final static String baseDir = "core/target/test-classes/test-data/";
  
  private DefaultFileSystemManager manager;
  private final static String largeFilePath = baseDir;
  private final static String largeFileName = "largefile";
  
  @Before
  public void setup() throws Exception {
    manager = new DefaultFileSystemManager();

    manager.setFilesCache(new SoftRefFilesCache());
    manager.setCacheStrategy(CacheStrategy.ON_RESOLVE);
    
    manager.addProvider("file", new DefaultLocalFileProvider());
    manager.addProvider("tgz", new TarFileProvider());
    manager.addProvider("tar", new TarFileProvider());
    
    createLargeFile(largeFilePath, largeFileName);
  }
  
  @Test
  public void testLargeFile() throws Exception {
    File realFile = new File(largeFilePath + largeFileName + ".tar.gz");
    
    FileObject file = manager.resolveFile("tgz:file://" + realFile.getCanonicalPath() + "!/");
    
    assertNotNull(file);
    List<FileObject> files = Arrays.asList(file.getChildren());
    
    assertNotNull(files);
    assertEquals(1, files.size());
    
    assertTrue("Expected file not found: " + largeFileName + ".txt", files.get(0).getName().getBaseName().equals(largeFileName + ".txt"));
  }
  
//  @Test
  public void testFileCheck() throws Exception {
    String[] expectedFiles = {
      "plugins.tsv",
      "languages.tsv",
      "browser_type.tsv",
      "timezones.tsv",
      "color_depth.tsv",
      "resolution.tsv",
      "connection_type.tsv",
      "search_engines.tsv",
      "javascript_version.tsv",
      "operating_systems.tsv",
      "country.tsv",
      "browser.tsv"
    };
    
    fileCheck(expectedFiles, "tar:file://c:/temp/data/data/data-small.tar");
  }
    
  protected void fileCheck(String[] expectedFiles, String tarFile) throws Exception {
    assertNotNull(manager);
    FileObject file = manager.resolveFile(tarFile);
    
    assertNotNull(file);
    List<FileObject> files = Arrays.asList(file.getChildren());
    
    assertNotNull(files);
    for(String expectedFile : expectedFiles) {
      assertTrue("Expected file not found: " + expectedFile, fileExists(expectedFile, files));
    }
  }

  /**
   * Search for the expected file in a given list, without using the full path
   * @param expectedFile
   * @param files
   * @return
   */
  protected boolean fileExists(String expectedFile, List<FileObject> files) {
    for(FileObject file : files) {
      if(file.getName().getBaseName().equals(expectedFile)) {
        return true;
      }
    }
    
    return false;
  }
  
  protected boolean endsWith(String testString, String[] testList) {
    for(String testItem : testList) {
      if(testString.endsWith(testItem)) {
        return true;
      }
    }
    return false;
  }
  
  @SuppressWarnings("unused")
  protected void createLargeFile(String path, String name) throws Exception {
    long _1K = 1024;
    long _1M = 1024 * _1K;
    long _256M = 256 * _1M;
    long _512M = 512 * _1M;
    long _1G = 1024 * _1M;
    
    // File size of 3 GB
    long fileSize = 3 * _1G;
    
    File tarGzFile = new File(path + name + ".tar.gz");
    
    if(!tarGzFile.exists()) {
  
      // Create archive
      OutputStream outTarFileStream = new FileOutputStream(path + name + ".tar");
  
      TarArchiveOutputStream outTarStream = (TarArchiveOutputStream)new ArchiveStreamFactory()
      .createArchiveOutputStream(ArchiveStreamFactory.TAR, outTarFileStream);

      // Create archive contents
      TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(name + ".txt");
      tarArchiveEntry.setSize(fileSize);
      
      outTarStream.putArchiveEntry(tarArchiveEntry);
      for(long i = 0; i < fileSize; i++) {
        outTarStream.write('a');
      }
      
      outTarStream.closeArchiveEntry();
      outTarStream.close();
      
      outTarFileStream.close();
      
      // Create compressed archive
      OutputStream outGzipFileStream = new FileOutputStream(path + name + ".tar.gz");
      
      GzipCompressorOutputStream outGzipStream = (GzipCompressorOutputStream)new CompressorStreamFactory()
      .createCompressorOutputStream(CompressorStreamFactory.GZIP, outGzipFileStream);
      
      // Compress archive
      InputStream inTarFileStream = new FileInputStream(path + name + ".tar");
      // TODO: Change to a Piped Stream to conserve disk space
      IOUtils.copy(inTarFileStream, outGzipStream);
      inTarFileStream.close();
      
      outGzipStream.close();
      outGzipFileStream.close();      
      
      // Cleanup original tar
      File tarFile = new File(path + name + ".tar");
      if(tarFile.exists()) {
        tarFile.delete();
      }
    }      
  }
}
