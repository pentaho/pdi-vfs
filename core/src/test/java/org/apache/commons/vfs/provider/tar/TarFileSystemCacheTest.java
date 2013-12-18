package org.apache.commons.vfs.provider.tar;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.impl.VirtualFileName;
import org.apache.commons.vfs.provider.ArchivedFileSystemCacheTest;
import java.io.File;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TarFileSystemCacheTest extends ArchivedFileSystemCacheTest {

  public void initFs() throws Exception {
    FileSystem parentFs = mock( FileSystem.class );
    FileObject parentLayer = mock( FileObject.class );
    File file = mock( File.class );

    when( parentLayer.getFileSystem() ).thenReturn( parentFs );
    when( parentFs.replicateFile( parentLayer, Selectors.SELECT_SELF ) ).thenReturn( file );
    when( file.exists() ).thenReturn( true );
    fs = new TarFileSystem( new VirtualFileName( "/", "", FileType.FILE ), parentLayer, null );
  }

  public void initFileObjects() throws Exception {
    f1 = new TarFileObject( n1, null, (TarFileSystem) fs, false );
    f2 = new TarFileObject( n2, null, (TarFileSystem) fs, false );
    f3 = new TarFileObject( n3, null, (TarFileSystem) fs, false );
  }

  public void testGetFileFromCache() throws Exception {
    super.innerTestGetFileFromCache();
  }

}
