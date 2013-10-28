package org.apache.commons.vfs.provider.zip;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.impl.VirtualFileName;
import org.apache.commons.vfs.provider.ArchivedFileSystemCacheTest;

import static org.mockito.Mockito.*;

import java.io.File;

public class ZipFileSystemCacheTest extends ArchivedFileSystemCacheTest {

  public void initFs() throws Exception {
    FileSystem parentFs = mock( FileSystem.class );
    FileObject parentLayer = mock( FileObject.class );
    File file = mock( File.class );

    when( parentLayer.getFileSystem() ).thenReturn( parentFs );
    when( parentFs.replicateFile( parentLayer, Selectors.SELECT_SELF ) ).thenReturn( file );
    when( file.exists() ).thenReturn( true );
    fs = new ZipFileSystem( new VirtualFileName( "/", "", FileType.FILE ), parentLayer, null );
  }

  public void initFileObjects() throws Exception {
    f1 = new ZipFileObject( n1, null, (ZipFileSystem) fs, false );
    f2 = new ZipFileObject( n2, null, (ZipFileSystem) fs, false );
    f3 = new ZipFileObject( n3, null, (ZipFileSystem) fs, false );
  }

  public void testGetFileFromCache() throws Exception {
    super.innerTestGetFileFromCache();
  }

}
