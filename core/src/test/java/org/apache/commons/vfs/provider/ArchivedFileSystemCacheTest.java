package org.apache.commons.vfs.provider;

import junit.framework.TestCase;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.impl.VirtualFileName;

/**
 * Created with IntelliJ IDEA. User: Dzmitry_Bahdanovich Date: 11/18/13 Time: 1:53 PM To change this template use File |
 * Settings | File Templates.
 */
public abstract class ArchivedFileSystemCacheTest extends TestCase {

  protected AbstractFileSystem fs = null;

  protected FileName n1 = null;
  protected FileName n2 = null;
  protected FileName n3 = null;

  protected FileObject f1 = null;
  protected FileObject f2 = null;
  protected FileObject f3 = null;

  public void setUp() throws Exception {

    initFs();

    n1 = new VirtualFileName( "", "f1", FileType.FILE );
    n2 = new VirtualFileName( "", "f2", FileType.FILE );
    n3 = new VirtualFileName( "", "f3", FileType.FILE );

    initFileObjects();

  }

  /**
   * If filesystem cache is based on weak references (old implementation), then this method will sometimes clean up the
   * cache
   */
  private void cleanStrongReferences() {
    f1 = null;
    f2 = null;
    f3 = null;
    System.gc(); // not guaranteed
  }

  private void initCache() throws Exception {
    fs.putFileToCache( f1 );
    fs.putFileToCache( f2 );
    fs.putFileToCache( f3 );
  }

  protected void innerTestGetFileFromCache() throws Exception {
    initCache();
    cleanStrongReferences();
    assertNotNull( fs.getFileFromCache( n1 ) );
    assertNotNull( fs.getFileFromCache( n2 ) );
    assertNotNull( fs.getFileFromCache( n3 ) );
  }

  protected abstract void initFs() throws Exception;

  protected abstract void initFileObjects() throws Exception;

}
