/*!
* Copyright 2010 - 2013 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package org.apache.commons.vfs.provider.zip;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VfsLog;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A read-only file system for Zip/Jar files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 764356 $ $Date: 2009-04-13 00:06:01 -0400 (Mon, 13 Apr 2009) $
 */
public class ZipFileSystem
  extends AbstractFileSystem
  implements FileSystem {
  private static final Log log = LogFactory.getLog( ZipFileSystem.class );

  private final File file;
  private ZipFile zipFile;
  private Map cache = new HashMap();

  public ZipFileSystem( final FileName rootName,
                        final FileObject parentLayer,
                        final FileSystemOptions fileSystemOptions )
    throws FileSystemException {
    super( rootName, parentLayer, fileSystemOptions );

    // Make a local copy of the file
    file = parentLayer.getFileSystem().replicateFile( parentLayer, Selectors.SELECT_SELF );

    // Open the Zip file
    if ( !file.exists() ) {
      // Don't need to do anything
      zipFile = null;
      return;
    }

    // zipFile = createZipFile(this.file);
  }

  public void init() throws FileSystemException {
    super.init();

    try {
      // Build the index
      List strongRef = new ArrayList( 100 );
      Enumeration entries = getZipFile().entries();
      while ( entries.hasMoreElements() ) {
        ZipEntry entry = (ZipEntry) entries.nextElement();
        FileName name = getFileSystemManager().resolveName( getRootName(), UriParser.encode( entry.getName() ) );

        // Create the file
        ZipFileObject fileObj;
        if ( entry.isDirectory() && getFileFromCache( name ) != null ) {
          fileObj = (ZipFileObject) getFileFromCache( name );
          fileObj.setZipEntry( entry );
          continue;
        }

        fileObj = createZipFileObject( name, entry );
        putFileToCache( fileObj );
        strongRef.add( fileObj );
        fileObj.holdObject( strongRef );

        // Make sure all ancestors exist
        // TODO - create these on demand
        ZipFileObject parent;
        for ( FileName parentName = name.getParent();
              parentName != null;
              fileObj = parent, parentName = parentName.getParent() ) {
          // Locate the parent
          parent = (ZipFileObject) getFileFromCache( parentName );
          if ( parent == null ) {
            parent = createZipFileObject( parentName, null );
            putFileToCache( parent );
            strongRef.add( parent );
            parent.holdObject( strongRef );
          }

          // Attach child to parent
          parent.attachChild( fileObj.getName() );
        }
      }
    } finally {
      closeCommunicationLink();
    }
  }

  protected ZipFile getZipFile() throws FileSystemException {
    if ( zipFile == null && this.file.exists() ) {
      ZipFile zipFile = createZipFile( this.file );

      this.zipFile = zipFile;
    }

    return zipFile;
  }

  protected ZipFileObject createZipFileObject( final FileName name,
                                               final ZipEntry entry ) throws FileSystemException {
    return new ZipFileObject( name, entry, this, true );
  }

  protected ZipFile createZipFile( final File file ) throws FileSystemException {
    try {
      return new ZipFile( file );
    } catch ( IOException ioe ) {
      throw new FileSystemException( "vfs.provider.zip/open-zip-file.error", file, ioe );
    }
  }

  protected void doCloseCommunicationLink() {
    // Release the zip file
    try {
      if ( zipFile != null ) {
        zipFile.close();
        zipFile = null;
      }
    } catch ( final IOException e ) {
      // getLogger().warn("vfs.provider.zip/close-zip-file.error :" + file, e);
      VfsLog.warn( getLogger(), log, "vfs.provider.zip/close-zip-file.error :" + file, e );
    }
  }

  /**
   * Returns the capabilities of this file system.
   */
  protected void addCapabilities( final Collection caps ) {
    caps.addAll( ZipFileProvider.capabilities );
  }

  /**
   * Creates a file object.
   */
  @Override
  protected FileObject createFile( final FileName name ) throws FileSystemException {
    String path = UriParser.encode(name.getPath());
    if (path.startsWith("/"))
      path = path.substring(1);
    ZipEntry zipEntry = new ZipEntry(path);
    FileName rootName = getFileSystemManager().resolveName(getRootName(), path);
    return new ZipFileObject(rootName, zipEntry, this, true);
  }


  /**
   * Adds a file object to the cache.
   */
  protected void putFileToCache( final FileObject file ) {
    if ( file != null ) {
      synchronized ( cache ) {
        cache.put( file.getName(), file );
      }
    }
  }

  /**
   * Returns a cached file.
   */
  protected FileObject getFileFromCache( final FileName name ) {
    if ( name == null ) {
      return null;
    }
    synchronized ( cache ) {
      return (FileObject) cache.get( name );
    }
  }

  /**
   * remove a cached file.
   */
  protected void removeFileFromCache( final FileName name ) {
    if ( name != null ) {
      synchronized ( cache ) {
        cache.remove( name );
      }
    }
  }

  /**
   * will be called after all file-objects closed their streams.
   protected void notifyAllStreamsClosed()
   {
   closeCommunicationLink();
   }
   */
}
